package com.argctech.core.posts.repository.specs;

import com.argctech.core.posts.entity.PostEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class PostSpecifications {

    public static Specification<PostEntity> postsOfFollowingsOrAll(List<Long> followingsIds, LocalDateTime startDate) {
        return (root, query, criteriaBuilder) -> {
            Predicate followingsPredicate = root.get("userId").in(followingsIds);
            Predicate datePredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("publicationDate"), startDate);

            // Combina las condiciones
            return criteriaBuilder.and(followingsPredicate, datePredicate);
        };
    }
}
