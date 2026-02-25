package com.alexistdev.geobill.models.entity;

import com.alexistdev.geobill.config.DatabaseTableNames;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.hibernate.generator.EventType;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = DatabaseTableNames.TB_HOSTING)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_HOSTING + " SET is_deleted = true WHERE uuid = ?")
@Where(clause = "is_deleted = false")
public class Hosting extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "uuid", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Generated(event = EventType.INSERT)
    @Column(name = "hosting_code", updatable = false, insertable = false, columnDefinition = "SERIAL")
    private Long hostingCode;

    @NotNull
    @Column(length = 100, nullable = false)
    private String name;

    @NotNull
    @Column(length = 100, nullable = false)
    private String domain;

    @NotNull
    private Double price;

    @NotNull
    private Date startDate;

    @NotNull
    private Date endDate;

    @NotNull
    @Column(nullable = false, columnDefinition = "TINYINT(1)", length = 1)
    private int status = 0;

}
