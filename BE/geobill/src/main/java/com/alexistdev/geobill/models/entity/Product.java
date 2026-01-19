package com.alexistdev.geobill.models.entity;

import com.alexistdev.geobill.config.DatabaseTableNames;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@DynamicUpdate
@Table(name = DatabaseTableNames.TB_PRODUCTS)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_PRODUCTS + " SET is_deleted = true WHERE uuid = ?")
@Where(clause = "is_deleted = false")
public class Product extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    @NotBlank
    @Column(length = 255, nullable = false)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producttype_id", referencedColumnName = "uuid", nullable = false)
    private ProductType productType;

    @NotNull
    @Column(nullable = false)
    private Double price;

    @NotNull
    @Column(nullable = false)
    private Integer cycle;

    @Nullable
    @Column(name = "capacity", length = 255)
    private String capacity;

    @Nullable
    @Column(name = "bandwith", length = 255)
    private String bandwith;

    @Nullable
    @Column(name = "addon_domain", length = 255)
    private String addon_domain;

    @Nullable
    @Column(name = "database_account", length = 255)
    private String database_account;

    @Nullable
    @Column(name = "ftp_account", length = 255)
    private String ftp_account;

    @Nullable
    @Column(name = "info1", length = 255)
    private String info1;

    @Nullable
    @Column(name = "info2", length = 255)
    private String info2;

    @Nullable
    @Column(name = "info3", length = 255)
    private String info3;

    @Nullable
    @Column(name = "info4", length = 255)
    private String info4;

    @Nullable
    @Column(name = "info5", length = 255)
    private String info5;

}
