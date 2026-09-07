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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.UUID;

/**
 * Lampiran tiket. Yang disimpan adalah lokasi file, bukan isinya, sehingga penyimpanan
 * bisa dipindah dari disk lokal ke object storage tanpa mengubah skema.
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(
        name = DatabaseTableNames.TB_TICKET_ATTACHMENTS,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ticket_attachment_stored_name", columnNames = {"stored_name"})
        },
        indexes = {
                @Index(name = "idx_ticket_attachments_ticket", columnList = "ticket_id"),
                @Index(name = "idx_ticket_attachments_reply", columnList = "reply_id")
        }
)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_TICKET_ATTACHMENTS + " SET is_deleted = true WHERE uuid = ?")
@Where(clause = "is_deleted = false")
@SuppressWarnings("SqlResolve")
public class TicketAttachment extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    /** Selalu diisi walau lampiran menempel pada sebuah reply, agar bisa dilist tanpa join. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", referencedColumnName = "uuid", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Ticket ticket;

    /** Null bila lampiran menempel langsung pada tiket, bukan pada satu balasan. */
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id", referencedColumnName = "uuid")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TicketReply reply;

    /** Nama asli dari klien, dipakai saat file diunduh kembali. */
    @NotBlank
    @Column(name = "original_name", length = 255, nullable = false)
    private String originalName;

    /** Nama acak di penyimpanan; memisahkannya dari nama asli mencegah path traversal. */
    @NotBlank
    @Column(name = "stored_name", length = 255, nullable = false)
    private String storedName;

    @NotBlank
    @Column(name = "storage_path", length = 500, nullable = false)
    private String storagePath;

    @NotBlank
    @Column(name = "mime_type", length = 100, nullable = false)
    private String mimeType;

    /** Ukuran file dalam byte. */
    @NotNull
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", referencedColumnName = "id")
    private User uploadedBy;

}
