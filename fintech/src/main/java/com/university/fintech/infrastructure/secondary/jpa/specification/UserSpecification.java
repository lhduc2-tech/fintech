package com.university.fintech.infrastructure.secondary.jpa.specification;

import com.university.fintech.domain.entity.RoleEntity;
import com.university.fintech.domain.entity.UserEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<UserEntity> filterUsers(
            String search,
            String status,
            Integer tier,
            String ekycStatus,
            String roleCode,
            OffsetDateTime fromCreatedAt,
            OffsetDateTime toCreatedAt
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";
                Predicate searchEmail = cb.like(cb.lower(root.get("email")), keyword);
                Predicate searchPhone = cb.like(root.get("phoneNumber"), keyword);
                Predicate searchFullName = cb.like(cb.lower(root.get("fullName")), keyword);
                Predicate searchIdentity = cb.like(root.get("identityNumber"), keyword);
                predicates.add(cb.or(searchEmail, searchPhone, searchFullName, searchIdentity));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }

            if (tier != null) {
                predicates.add(cb.equal(root.get("tier"), tier));
            }

            if (ekycStatus != null && !ekycStatus.isBlank()) {
                if ("NOT_VERIFIED".equalsIgnoreCase(ekycStatus.trim())) {
                    predicates.add(cb.isNull(root.get("ekycStatus")));
                } else {
                    predicates.add(cb.equal(cb.upper(root.get("ekycStatus")), ekycStatus.trim().toUpperCase()));
                }
            }

            if (roleCode != null && !roleCode.isBlank()) {
                Join<UserEntity, RoleEntity> rolesJoin = root.join("roles", JoinType.INNER);
                predicates.add(cb.equal(cb.upper(rolesJoin.get("code")), roleCode.trim().toUpperCase()));
                if (query != null) {
                    query.distinct(true);
                }
            }

            if (fromCreatedAt != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromCreatedAt));
            }

            if (toCreatedAt != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toCreatedAt));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
