package com.alexistdev.geobill.models.entity;

import com.alexistdev.geobill.config.DatabaseTableNames;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@Table(name = DatabaseTableNames.TB_DOMAINTLD)
@Entity
public class Domaintld extends BaseEntity<String> implements Serializable {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(length = 20, nullable = false)
    private String name;
}
