package org.cinos.authin_core.posts.entity;

import org.cinos.authin_core.posts.models.CurrencySymbol;
import jakarta.persistence.*;
import lombok.*;
import org.cinos.authin_core.users.entity.AccountEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "POSTS")
public class PostEntity implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String make;
    private String model;
    private String year;
    private Boolean isUsed;
    private Double price;
    @Enumerated(EnumType.STRING)
    private CurrencySymbol currencySymbol;
    private String kilometers;
    private String fuel;
    private String transmission;
    private String description;
    @Column(name = "publication_date")
    private LocalDateTime publicationDate;
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity userAccount;
    private Integer likes;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImageEntity> images = new ArrayList<>();
    private Boolean active;
    @ManyToMany
    @JoinTable(
            name = "account_saved_posts",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id")
    )
    private List<AccountEntity> usersSaved;
    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private PostLocationEntity location;
}
