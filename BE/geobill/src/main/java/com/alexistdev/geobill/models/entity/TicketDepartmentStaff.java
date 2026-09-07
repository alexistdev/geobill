package com.alexistdev.geobill.models.entity;

import com.alexistdev.geobill.config.DatabaseTableNames;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.UUID;

/**
 * Staff atau admin yang berhak menangani sebuah departemen. Dropdown "Assigned To"
 * pada halaman ticket dibaca dari tabel ini, difilter oleh departemen terpilih.
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(
        name = DatabaseTableNames.TB_TICKET_DEPARTMENT_STAFFS,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ticket_department_staff", columnNames = {"department_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_ticket_department_staff_department", columnList = "department_id"),
                @Index(name = "idx_ticket_department_staff_user", columnList = "user_id")
        }
)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_TICKET_DEPARTMENT_STAFFS + " SET is_deleted = true WHERE uuid = ?")
@Where(clause = "is_deleted = false")
@SuppressWarnings("SqlResolve")
public class TicketDepartmentStaff extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", referencedColumnName = "uuid", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TicketDepartment department;

    /** Wajib berrole STAFF atau ADMIN; divalidasi di service, bukan di database. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @NotNull
    @Column(name = "is_supervisor", nullable = false)
    private Boolean isSupervisor = Boolean.FALSE;

}
