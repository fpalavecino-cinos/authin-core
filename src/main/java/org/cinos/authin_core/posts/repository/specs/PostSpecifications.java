package org.cinos.authin_core.posts.repository.specs;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import org.cinos.authin_core.posts.entity.CommentEntity;
import org.cinos.authin_core.posts.entity.PostEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class PostSpecifications {

    public static Specification<PostEntity> postFeedSpec(
            List<Long> followingsIds,
            Double userLatitude,
            Double userLongitude,
            Long currentUserId
    ) {
        return (root, query, criteriaBuilder) -> {
            var accountJoin = root.join("userAccount");

            // 1. Predicados base
            Predicate excludeCurrentUserPredicate = criteriaBuilder.notEqual(
                    accountJoin.get("id"), currentUserId
            );
            Predicate activePostsPredicate = criteriaBuilder.isTrue(root.get("active"));

            // --- Cálculo de relevancia ---
            // Factor de tiempo
            Expression<Long> currentTimeMillis = criteriaBuilder.function(
                    "UNIX_TIMESTAMP", Long.class, criteriaBuilder.currentTimestamp()
            );
            Expression<Long> publicationTimeMillis = criteriaBuilder.function(
                    "UNIX_TIMESTAMP", Long.class, root.get("publicationDate")
            );
            Expression<Long> hoursSincePublication = criteriaBuilder.toLong(
                    criteriaBuilder.quot(
                            criteriaBuilder.diff(currentTimeMillis, publicationTimeMillis),
                            3600L
                    )
            );
            Expression<Double> timeFactor = criteriaBuilder.toDouble(
                    criteriaBuilder.quot(1.0, criteriaBuilder.sum(hoursSincePublication, 1))
            );

            // Factor de comentarios
            var subquery = query.subquery(Long.class);
            var commentRoot = subquery.from(CommentEntity.class);
            subquery.select(criteriaBuilder.count(commentRoot));
            subquery.where(criteriaBuilder.equal(commentRoot.get("postId"), root.get("id")));
            Expression<Double> commentsFactor = criteriaBuilder.toDouble(subquery.getSelection());

            // Factor de proximidad
            var locationJoin = root.join("location", JoinType.LEFT);
            Expression<Double> distance = criteriaBuilder.sqrt(
                    criteriaBuilder.sum(
                            criteriaBuilder.power(criteriaBuilder.diff(locationJoin.get("lat"), userLatitude), 2),
                            criteriaBuilder.power(criteriaBuilder.diff(locationJoin.get("lng"), userLongitude), 2)
                    )
            );
            Expression<Double> proximityScore = criteriaBuilder.toDouble(
                    criteriaBuilder.quot(1.0, criteriaBuilder.sum(distance, 1))
            );

            // Factor de verificación (nuevo)
            Expression<Double> verificationFactor = criteriaBuilder.<Double>selectCase()
                    .when(criteriaBuilder.isTrue(root.get("isVerified")), 1.5) // Boost para verificados
                    .otherwise(1.0); // Valor base para no verificados

            // Factor de relación (modificado)
            Expression<Double> relationshipFactor = criteriaBuilder.<Double>selectCase()
                    .when(criteriaBuilder.in(accountJoin.get("id")).value(followingsIds), 1.5) // Boost para seguidos
                    .otherwise(1.0); // Valor base para no seguidos

            // Puntuación de relevancia modificada (ahora con multiplicación de factores)
            Expression<Double> relevanceScore = criteriaBuilder.prod(
                    verificationFactor, // Multiplica primero por el factor de verificación
                    criteriaBuilder.prod(
                            relationshipFactor, // Luego por el factor de relación
                            criteriaBuilder.sum(
                                    criteriaBuilder.sum(
                                            criteriaBuilder.sum(
                                                    criteriaBuilder.prod(timeFactor, 0.4),
                                                    criteriaBuilder.prod(commentsFactor, 0.3)
                                            ),
                                            criteriaBuilder.prod(proximityScore, 0.2)
                                    ),
                                    criteriaBuilder.prod(
                                            criteriaBuilder.<Double>selectCase()
                                                    .when(criteriaBuilder.isTrue(root.get("isVerified")), 0.5) // Bonus adicional para verificados
                                                    .otherwise(0.0),
                                            0.1
                                    )
                            )
                    )
            );

            // Ordenar por relevancia
            query.orderBy(criteriaBuilder.desc(relevanceScore));

            return criteriaBuilder.and(
                    excludeCurrentUserPredicate,
                    activePostsPredicate
            );
        };
    }
}
