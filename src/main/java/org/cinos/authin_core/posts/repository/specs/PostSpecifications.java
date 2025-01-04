package org.cinos.authin_core.posts.repository.specs;

import org.cinos.authin_core.posts.entity.PostEntity;
import jakarta.persistence.criteria.Predicate;
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
