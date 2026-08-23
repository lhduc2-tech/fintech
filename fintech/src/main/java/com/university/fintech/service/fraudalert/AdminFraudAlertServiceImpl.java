package com.university.fintech.service.fraudalert;

import com.university.fintech.domain.entity.FraudAlertEntity;
import com.university.fintech.domain.port.primary.AdminFraudAlertService;
import com.university.fintech.infrastructure.primary.restful.request.admin.fraudalert.AdminFraudAlertSearchRequest;
import com.university.fintech.infrastructure.primary.restful.request.admin.fraudalert.AdminUpdateFraudAlertStatusRequest;
import com.university.fintech.infrastructure.primary.restful.response.PageResponse;
import com.university.fintech.infrastructure.primary.restful.response.admin.fraudalert.AdminFraudAlertDetailResponse;
import com.university.fintech.infrastructure.primary.restful.response.admin.fraudalert.AdminFraudAlertSummaryResponse;
import com.university.fintech.infrastructure.primary.restful.response.auth.MessageResponse;
import com.university.fintech.infrastructure.secondary.jpa.repository.FraudAlertRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Triển khai AdminFraudAlertService — quản lý Fraud Alerts cho Admin.
 */
@Service
@RequiredArgsConstructor
public class AdminFraudAlertServiceImpl implements AdminFraudAlertService {

    private final FraudAlertRepository fraudAlertRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminFraudAlertSummaryResponse> searchAlerts(AdminFraudAlertSearchRequest request) {
        AdminFraudAlertSearchRequest safeRequest = request == null ? new AdminFraudAlertSearchRequest() : request;
        Specification<FraudAlertEntity> spec = buildSpecification(safeRequest);
        Pageable pageable = safeRequest.toPageable();
        Page<FraudAlertEntity> page = fraudAlertRepository.findAll(spec, pageable);
        return PageResponse.of(page, this::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminFraudAlertDetailResponse getAlertDetail(UUID id) {
        FraudAlertEntity entity = fraudAlertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Fraud Alert với ID: " + id));
        return toDetailResponse(entity);
    }

    @Override
    @Transactional
    public MessageResponse updateAlertStatus(UUID id, AdminUpdateFraudAlertStatusRequest request) {
        FraudAlertEntity entity = fraudAlertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Fraud Alert với ID: " + id));
        entity.setStatus(request.getStatus().toUpperCase());
        fraudAlertRepository.save(entity);
        return new MessageResponse("Cập nhật trạng thái Fraud Alert thành công: " + request.getStatus());
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private Specification<FraudAlertEntity> buildSpecification(AdminFraudAlertSearchRequest req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Keyword search: referenceCode, userEmail, userFullName
            if (StringUtils.hasText(req.getSearch())) {
                String like = "%" + req.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("referenceCode")), like),
                        cb.like(cb.lower(root.get("userEmail")), like),
                        cb.like(cb.lower(root.get("userFullName")), like)
                ));
            }

            // Filter by riskLevel
            if (StringUtils.hasText(req.getRiskLevel())) {
                predicates.add(cb.equal(root.get("riskLevel"), req.getRiskLevel().toUpperCase()));
            }

            // Filter by status
            if (StringUtils.hasText(req.getStatus())) {
                predicates.add(cb.equal(root.get("status"), req.getStatus().toUpperCase()));
            }

            // Date range
            if (req.getFromCreatedAt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), req.getFromCreatedAt()));
            }
            if (req.getToCreatedAt() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), req.getToCreatedAt()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private AdminFraudAlertSummaryResponse toSummaryResponse(FraudAlertEntity e) {
        return AdminFraudAlertSummaryResponse.builder()
                .id(e.getId())
                .correlationId(e.getCorrelationId())
                .referenceCode(e.getReferenceCode())
                .userId(e.getUserId())
                .userEmail(e.getUserEmail())
                .userFullName(e.getUserFullName())
                .transactionType(e.getTransactionType())
                .amount(e.getAmount())
                .bankCode(e.getBankCode())
                .accountNumber(e.getAccountNumber())
                .recipientFullName(e.getRecipientFullName())
                .finalRiskScore(e.getFinalRiskScore())
                .riskLevel(e.getRiskLevel())
                .systemAction(e.getSystemAction())
                .ruleScore(e.getRuleScore())
                .aiScore(e.getAiScore())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private AdminFraudAlertDetailResponse toDetailResponse(FraudAlertEntity e) {
        return AdminFraudAlertDetailResponse.builder()
                .id(e.getId())
                .correlationId(e.getCorrelationId())
                .referenceCode(e.getReferenceCode())
                .userId(e.getUserId())
                .userEmail(e.getUserEmail())
                .userFullName(e.getUserFullName())
                .transactionType(e.getTransactionType())
                .amount(e.getAmount())
                .bankCode(e.getBankCode())
                .accountNumber(e.getAccountNumber())
                .recipientFullName(e.getRecipientFullName())
                .recipientPhoneNumber(e.getRecipientPhoneNumber())
                .finalRiskScore(e.getFinalRiskScore())
                .riskLevel(e.getRiskLevel())
                .systemAction(e.getSystemAction())
                .ruleScore(e.getRuleScore())
                .aiScore(e.getAiScore())
                .lgbmProbPct(e.getLgbmProbPct())
                .aeReconstructionError(e.getAeReconstructionError())
                .aeThreshold(e.getAeThreshold())
                .triggeredRules(e.getTriggeredRules())
                .threatWarnings(e.getThreatWarnings())
                .payloadSnapshot(e.getPayloadSnapshot())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
