package com.alexistdev.geobill.models.entity;

import com.alexistdev.geobill.config.DatabaseTableNames;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@DynamicUpdate
@Table(name = DatabaseTableNames.TB_CURRENCY)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_CURRENCY + " SET is_deleted = true WHERE uuid = ?")
@Where(clause = "is_deleted = false")
public class Currency {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String name;

    @NotBlank
    @Size(max = 5)
    @Column(length = 5, nullable = false)
    private String code;

    @NotBlank
    @Size(max = 10)
    @Column(length = 10, nullable = false)
    private String symbol;

    @NotNull
    @Positive
    @Column(name="exchange_rate", columnDefinition = "DOUBLE PRECISION", nullable = false)
    private Double exchangeRate;

    @NotNull
    @Column(name="is_default", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDefault = false;
}
