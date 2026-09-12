package com.alexistdev.geobill.models.entity;

/**
 * Status tiket. Setiap nilai memetakan satu tab pada halaman admin ticket;
 * {@link #getKey()} menghasilkan key yang dipakai komponen Angular.
 *
 * <p>TRASH bukan soft delete. Tiket yang dibuang tetap terbaca query karena
 * is_deleted masih false, sehingga masih bisa di-restore. Soft delete
 * (is_deleted = true) dipakai untuk hapus permanen dari Trash.</p>
 */
public enum TicketStatus {
    AWAITING_STAFF,
    AWAITING_CLIENT,
    IN_PROGRESS,
    ON_HOLD,
    CLOSED,
    TRASH;

    public String getKey() {
        return name().toLowerCase();
    }
}
