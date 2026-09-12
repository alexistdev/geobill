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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

/**
 * Satu pesan dalam percakapan tiket. Pesan pertama dari klien juga disimpan di sini,
 * sehingga tampilan percakapan cukup membaca satu tabel terurut created_date.
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(
        name = DatabaseTableNames.TB_TICKET_REPLIES,
        indexes = {
                @Index(name = "idx_ticket_replies_ticket", columnList = "ticket_id, created_date")
        }
)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_TICKET_REPLIES + " SET is_deleted = true WHERE uuid = ?")
@Where(clause = "is_deleted = false")
@SuppressWarnings("SqlResolve")
public class TicketReply extends BaseEntity<String> implements Serializable {

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

    /** Null bila authorType SYSTEM. */
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "author_type", length = 20, nullable = false)
    private TicketAuthorType authorType;

    /** Snapshot nama penulis, tetap terbaca bila usernya dihapus. */
    @Nullable
    @Column(name = "author_name", length = 150)
    private String authorName;

    @NotBlank
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Catatan internal staff. Tidak pernah dikirim ke klien, dan tidak boleh mengubah
     * status tiket maupun memperbarui lastReplyAt.
     */
    @NotNull
    @Column(name = "is_internal_note", nullable = false)
    private Boolean isInternalNote = Boolean.FALSE;

    @NotNull
    @Column(name = "is_email_sent", nullable = false)
    private Boolean isEmailSent = Boolean.FALSE;

    /** Panjang 45 karakter agar muat alamat IPv6. */
    @Nullable
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

}
