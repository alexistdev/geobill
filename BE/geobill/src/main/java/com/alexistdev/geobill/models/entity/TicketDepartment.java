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
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@DynamicUpdate
@Table(
        name = DatabaseTableNames.TB_TICKET_DEPARTMENTS,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ticket_department_code", columnNames = {"code"})
        }
)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_TICKET_DEPARTMENTS + " SET is_deleted = true WHERE uuid = ?")
@Where(clause = "is_deleted = false")
@SuppressWarnings("SqlResolve")
public class TicketDepartment extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    @NotBlank
    @Column(length = 100, nullable = false)
    private String name;

    @NotBlank
    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Nullable
    @Column(length = 150)
    private String email;

    @Nullable
    @Column(length = 255)
    private String description;

    @NotNull
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    /** Tutup tiket otomatis bila klien tidak membalas sekian hari. Null berarti tidak pernah. */
    @Nullable
    @Column(name = "auto_close_days")
    private Integer autoCloseDays;

}
