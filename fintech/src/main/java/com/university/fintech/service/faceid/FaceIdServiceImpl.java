package com.university.fintech.service.faceid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.university.fintech.domain.port.primary.FaceIdService;
import com.university.fintech.exception.domain.AuthException;
import com.university.fintech.infrastructure.secondary.redis.RedisRestTokenStore;
import com.university.fintech.shared.constant.Constant;
import com.university.fintech.wire.FaceIdConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Triển khai FaceIdService — giao tiếp với FaceID Python server qua HTTP REST.
 *
 * <p>Luồng xác thực giao dịch cao (≥ 10M VNĐ):
 * <ol>
 *   <li>Client gọi {@code POST /wallet/faceid/verify} → server gọi Python → trả FaceID token</li>
 *   <li>Client gửi token kèm request giao dịch</li>
 *   <li>{@link #isTokenValid} xác nhận token trong Redis → {@link #consumeToken} xóa token (one-time use)</li>
 * </ol>
 */
@Service
@Slf4j
public class FaceIdServiceImpl implements FaceIdService {

    private final FaceIdConfig faceIdConfig;
    private final RestTemplate faceIdRestTemplate;
    private final RedisRestTokenStore redisTokenStore;

    public FaceIdServiceImpl(
            FaceIdConfig faceIdConfig,
            @Qualifier("faceIdRestTemplate") RestTemplate faceIdRestTemplate,
            RedisRestTokenStore redisTokenStore
    ) {
        this.faceIdConfig = faceIdConfig;
        this.faceIdRestTemplate = faceIdRestTemplate;
        this.redisTokenStore = redisTokenStore;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // eKYC Registration
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void registerEkyc(UUID userId, List<String> imagesBase64) {
        if (!faceIdConfig.isEnabled()) {
            log.warn("[FaceID] FaceID disabled — bỏ qua đăng ký eKYC cho user {}", userId);
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId.toString());
        body.put("images", imagesBase64);

        String url = faceIdConfig.getServerUrl() + "/api/register_ekyc";
        log.info("[FaceID] Gọi đăng ký eKYC cho user {} — {} ảnh", userId, imagesBase64.size());

        try {
            FaceIdApiResponse response = faceIdRestTemplate.postForObject(url, body, FaceIdApiResponse.class);
            validateResponse(response, Constant.ERROR_FACEID_VERIFICATION_FAILED, Constant.MESSAGE_FACEID_VERIFICATION_FAILED);
            log.info("[FaceID] Đăng ký eKYC thành công cho user {}", userId);
        } catch (ResourceAccessException e) {
            log.error("[FaceID] Không kết nối được FaceID server khi đăng ký eKYC user {}: {}", userId, e.getMessage());
            throw new AuthException(Constant.MESSAGE_FACEID_SERVICE_UNAVAILABLE, Constant.ERROR_FACEID_SERVICE_UNAVAILABLE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FaceID Verification + Token Issuance
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String verifyAndIssueToken(UUID userId, List<String> imagesBase64, boolean activeLivenessPassed) {
        if (!faceIdConfig.isEnabled()) {
            log.warn("[FaceID] FaceID disabled — phát token bypass cho user {}", userId);
            return issueFaceIdToken(userId);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId.toString());
        body.put("images", imagesBase64);
        body.put("active_liveness_passed", activeLivenessPassed);

        String url = faceIdConfig.getServerUrl() + "/api/verify_faceid";
        log.info("[FaceID] Gọi xác thực FaceID cho user {} — {} frames, activeLiveness={}",
                userId, imagesBase64.size(), activeLivenessPassed);

        try {
            FaceIdApiResponse response = faceIdRestTemplate.postForObject(url, body, FaceIdApiResponse.class);

            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                String reason = response != null ? response.getMessage() : "Không nhận được phản hồi từ FaceID server";
                log.warn("[FaceID] Xác thực thất bại cho user {}: {}", userId, reason);
                throw new AuthException(Constant.MESSAGE_FACEID_VERIFICATION_FAILED, Constant.ERROR_FACEID_VERIFICATION_FAILED);
            }

            log.info("[FaceID] Xác thực thành công cho user {} — cosine={}, liveness={}",
                    userId, response.getCosineScore(), response.getLivenessScore());

            return issueFaceIdToken(userId);
        } catch (ResourceAccessException e) {
            log.error("[FaceID] Không kết nối được FaceID server khi xác thực user {}: {}", userId, e.getMessage());
            throw new AuthException(Constant.MESSAGE_FACEID_SERVICE_UNAVAILABLE, Constant.ERROR_FACEID_SERVICE_UNAVAILABLE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Token Lifecycle (Redis)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public boolean isTokenValid(UUID userId, String faceIdToken) {
        if (faceIdToken == null || faceIdToken.isBlank()) {
            return false;
        }
        String stored = redisTokenStore.get(redisKey(userId));
        return faceIdToken.equals(stored);
    }

    @Override
    public void consumeToken(UUID userId, String faceIdToken) {
        String stored = redisTokenStore.get(redisKey(userId));
        if (faceIdToken != null && faceIdToken.equals(stored)) {
            redisTokenStore.delete(redisKey(userId));
            log.info("[FaceID] Đã tiêu thụ token của user {}", userId);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String issueFaceIdToken(UUID userId) {
        String token = UUID.randomUUID().toString();
        redisTokenStore.set(redisKey(userId), token, Constant.FACEID_TOKEN_TTL_SECONDS);
        log.info("[FaceID] Đã phát FaceID token cho user {} (TTL {}s)", userId, Constant.FACEID_TOKEN_TTL_SECONDS);
        return token;
    }

    private String redisKey(UUID userId) {
        return Constant.FACEID_TOKEN_REDIS_PREFIX + userId;
    }

    private void validateResponse(FaceIdApiResponse response, String errorCode, String message) {
        if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            String reason = response != null ? response.getMessage() : "Không nhận được phản hồi";
            log.warn("[FaceID] Phản hồi thất bại: {}", reason);
            throw new AuthException(message, errorCode);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FaceID Python API Response DTO (nội bộ)
    // ─────────────────────────────────────────────────────────────────────────

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FaceIdApiResponse {
        private Boolean success;
        private String status;
        private String message;
        @JsonProperty("liveness_score")
        private String livenessScore;
        @JsonProperty("cosine_score")
        private String cosineScore;
        @JsonProperty("user_id")
        private String userId;
        @JsonProperty("tx_id")
        private String txId;
        @JsonProperty("vector_hash")
        private String vectorHash;
        @JsonProperty("image_cloud_urls")
        private List<String> imageCloudUrls;
    }
}
