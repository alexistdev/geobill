package com.alexistdev.geobill.models.entity;

import com.alexistdev.geobill.config.DatabaseTableNames;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = DatabaseTableNames.TB_PRODUCT_TYPES)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_PRODUCT_TYPES + " SET is_deleted = true WHERE uuid = ?")
@Where(clause = "is_deleted = false")
public class ProductType  extends BaseEntity<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    @NotNull
    @Column(length = 255, nullable = false)
    private String name;
}
