package com.alexistdev.geobill.models.entity;

import com.alexistdev.geobill.config.DatabaseTableNames;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

/**
 * Jejak audit tiket. Kolom audit pada BaseEntity mencatat siapa dan kapan, tabel ini
 * mencatat apa yang berubah, yang tidak bisa direkonstruksi dari kolom audit.
 * Restore dari Trash membaca status sebelumnya dari sini.
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(
        name = DatabaseTableNames.TB_TICKET_HISTORIES,
        indexes = {
                @Index(name = "idx_ticket_histories_ticket", columnList = "ticket_id, created_date")
        }
)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_TICKET_HISTORIES + " SET is_deleted = true WHERE uuid = ?")
@Where(clause = "is_deleted = false")
@SuppressWarnings("SqlResolve")
public class TicketHistory extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", referencedColumnName = "uuid", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Ticket ticket;

    /** Null untuk aksi yang dijalankan sistem, misalnya auto close. */
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", referencedColumnName = "id")
    private User actor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "action", length = 40, nullable = false)
    private TicketHistoryAction action;

    @Nullable
    @Column(name = "old_value", length = 150)
    private String oldValue;

    @Nullable
    @Column(name = "new_value", length = 150)
    private String newValue;

    @Nullable
    @Column(name = "note", length = 255)
    private String note;

}
