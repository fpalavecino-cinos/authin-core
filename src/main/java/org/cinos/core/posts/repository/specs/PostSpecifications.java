package org.cinos.core.posts.repository.specs;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import org.cinos.core.posts.dto.PostFilterDTO;
import org.cinos.core.posts.entity.CommentEntity;
import org.cinos.core.posts.entity.PostEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PostSpecifications {

    public static Specification<PostEntity> postFeedSpec(
            List<Long> followingsIds,
            Double userLatitude,
            Double userLongitude,
            Long currentUserId,
            String preferredBrand,
            Boolean wantsUsedCars,
            Boolean wantsNewCars,
            Boolean useLocationForRecommendations
    ) {
        return (root, query, criteriaBuilder) -> {
            var accountJoin = root.join("userAccount");

            List<Predicate> predicates = new ArrayList<>();
            // 1. Predicados base
            Predicate excludeCurrentUserPredicate = criteriaBuilder.notEqual(
                    accountJoin.get("id"), currentUserId
            );
            Predicate activePostsPredicate = criteriaBuilder.isTrue(root.get("active"));
            predicates.add(excludeCurrentUserPredicate);
            predicates.add(activePostsPredicate);

            // Preferencia de marca
            // (Eliminado el filtro que solo devolvía la marca preferida)

            // Preferencia de usados/nuevos
            // (Eliminado el filtro que solo devolvía usados o nuevos)

            // --- Cálculo de relevancia ---
            // Factor de marca preferida
            Expression<Double> brandFactor = (preferredBrand != null && !preferredBrand.isEmpty())
                ? criteriaBuilder.<Double>selectCase()
                    .when(criteriaBuilder.equal(root.get("make"), preferredBrand), 1.5) // boost si coincide
                    .otherwise(1.0)
                : criteriaBuilder.literal(1.0);

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
            Expression<Double> proximityScore = useLocationForRecommendations ? criteriaBuilder.toDouble(
                    criteriaBuilder.quot(1.0, criteriaBuilder.sum(distance, 1))
            ) : criteriaBuilder.literal(1.0);

            // Factor de verificación (nuevo)
            Expression<Double> verificationFactor = criteriaBuilder.<Double>selectCase()
                    .when(criteriaBuilder.isTrue(root.get("isVerified")), 3.0) // Boost para verificados
                    .otherwise(1.0); // Valor base para no verificados

            // Factor de relación (modificado)
            Expression<Double> relationshipFactor = criteriaBuilder.<Double>selectCase()
                    .when(criteriaBuilder.in(accountJoin.get("id")).value(followingsIds), 1.5) // Boost para seguidos
                    .otherwise(1.0); // Valor base para no seguidos

            // Factor de preferencia usados/nuevos
            Expression<Double> usedNewFactor = criteriaBuilder.literal(1.0);
            if (Boolean.TRUE.equals(wantsUsedCars) && !Boolean.TRUE.equals(wantsNewCars)) {
                usedNewFactor = criteriaBuilder.<Double>selectCase()
                    .when(criteriaBuilder.isTrue(root.get("isUsed")), 1.5)
                    .otherwise(1.0);
            } else if (!Boolean.TRUE.equals(wantsUsedCars) && Boolean.TRUE.equals(wantsNewCars)) {
                usedNewFactor = criteriaBuilder.<Double>selectCase()
                    .when(criteriaBuilder.isFalse(root.get("isUsed")), 1.5)
                    .otherwise(1.0);
            } else if (Boolean.TRUE.equals(wantsUsedCars) && Boolean.TRUE.equals(wantsNewCars)) {
                usedNewFactor = criteriaBuilder.literal(1.2); // pequeño boost si le da igual
            }

            // Factor de preferencia de ubicación
            Expression<Double> locationFactor = criteriaBuilder.literal(1.0);
            if (Boolean.TRUE.equals(useLocationForRecommendations)
                && userLatitude != null && userLongitude != null
                && userLatitude != 0.0 && userLongitude != 0.0) {
                // Si la distancia es menor a cierto umbral, boost
                locationFactor = criteriaBuilder.<Double>selectCase()
                    .when(criteriaBuilder.lessThan(distance, 0.5), 1.5) // boost si está cerca (ajusta el umbral según tu escala)
                    .otherwise(1.0);
            }

            // Puntuación de relevancia modificada (ahora con multiplicación de factores)
            Expression<Double> relevanceScore = criteriaBuilder.prod(
                    brandFactor, // Multiplica primero por el factor de marca preferida
                    criteriaBuilder.prod(
                        usedNewFactor, // Multiplica por el factor de usados/nuevos
                        criteriaBuilder.prod(
                            locationFactor, // Multiplica por el factor de ubicación
                            criteriaBuilder.prod(
                                verificationFactor, // Multiplica por el factor de verificación
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
                            )
                        )
                    )
            );

            // Ordenar por relevancia
            query.orderBy(criteriaBuilder.desc(relevanceScore));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<PostEntity> postFilterSpec(PostFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.make() != null) {
                predicates.add(root.get("make").in(filter.make()));
            }

            if (filter.model() != null) {
                predicates.add(root.get("model").in(filter.model()));
            }

            if (filter.minYear() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("year"), filter.minYear()));
            }

            if (filter.maxYear() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxYear()));
            }

            if (filter.fuelType() != null) {
                predicates.add(cb.equal(root.get("fuel"), filter.fuelType()));
            }

            if (filter.transmission() != null) {
                predicates.add(cb.equal(root.get("transmission"), filter.transmission()));
            }


            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }

            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }

            if (filter.minMileage() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("kilometers"), filter.minMileage()));
            }

            if (filter.maxMileage() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("kilometers"), filter.maxMileage()));
            }

            if (Boolean.TRUE.equals(filter.isUsed())) {
                predicates.add(cb.isTrue(root.get("isUsed")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
