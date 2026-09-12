package com.alexistdev.geobill.models.entity;

/** Jenis perubahan yang dicatat pada jejak audit tiket. */
public enum TicketHistoryAction {
    CREATED,
    STATUS_CHANGED,
    PRIORITY_CHANGED,
    ASSIGNED,
    UNASSIGNED,
    DEPARTMENT_CHANGED,
    REPLIED,
    TRASHED,
    RESTORED,
    CLOSED,
    REOPENED;
}
