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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Header sebuah tiket support. Isi percakapannya, termasuk pesan pertama dari klien,
 * disimpan di {@link TicketReply}.
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@DynamicUpdate
@Table(
        name = DatabaseTableNames.TB_TICKETS,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tickets_number", columnNames = {"ticket_number"})
        },
        indexes = {
                @Index(name = "idx_tickets_status_department", columnList = "status, department_id, is_deleted"),
                @Index(name = "idx_tickets_assigned", columnList = "assigned_to, status"),
                @Index(name = "idx_tickets_user", columnList = "user_id, status"),
                @Index(name = "idx_tickets_last_reply", columnList = "last_reply_at")
        }
)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_TICKETS + " SET is_deleted = true WHERE uuid = ?")
@Where(clause = "is_deleted = false")
@SuppressWarnings("SqlResolve")
public class Ticket extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    @NotBlank
    @Column(name = "ticket_number", updatable = false, unique = true, nullable = false, length = 50)
    private String ticketNumber;

    /** Klien pembuka tiket. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", referencedColumnName = "uuid", nullable = false)
    private TicketDepartment department;

    /** Staff penanggung jawab. Null berarti belum ditugaskan. */
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", referencedColumnName = "id")
    private User assignedTo;

    /** Layanan yang dikeluhkan, bila tiket menyangkut satu hosting tertentu. */
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hosting_id", referencedColumnName = "uuid")
    private Hosting hosting;

    /** Tagihan terkait, untuk tiket bertema billing. */
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", referencedColumnName = "uuid")
    private Invoice invoice;

    /** Kolom "Summary" pada daftar tiket. */
    @NotBlank
    @Column(name = "subject", length = 200, nullable = false)
    private String subject;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "priority", length = 20, nullable = false)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", length = 30, nullable = false)
    private TicketStatus status = TicketStatus.AWAITING_STAFF;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "source", length = 20, nullable = false)
    private TicketSource source = TicketSource.WEB;

    /** Daftar email tembusan, dipisah koma. */
    @Nullable
    @Column(name = "cc_emails", length = 500)
    private String ccEmails;

    /** Denormalisasi agar daftar tiket tidak perlu COUNT per baris. */
    @NotNull
    @Column(name = "reply_count", nullable = false)
    private Integer replyCount = 0;

    /** Mengisi kolom "Last Reply" dan filter Today / Last 7 days. */
    @Nullable
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_reply_at")
    private Date lastReplyAt;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_reply_by", referencedColumnName = "id")
    private User lastReplyBy;

    @Nullable
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "last_reply_author_type", length = 20)
    private TicketAuthorType lastReplyAuthorType;

    /** Dasar perhitungan SLA respons pertama. */
    @Nullable
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "first_response_at")
    private Date firstResponseAt;

    @Nullable
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "due_at")
    private Date dueAt;

    @Nullable
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "closed_at")
    private Date closedAt;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by", referencedColumnName = "id")
    private User closedBy;

    /** Penilaian klien 1..5 setelah tiket ditutup. */
    @Nullable
    @Column(name = "rating", columnDefinition = "TINYINT")
    private Integer rating;

    @NotNull
    @Column(name = "is_flagged", nullable = false)
    private Boolean isFlagged = Boolean.FALSE;

}
