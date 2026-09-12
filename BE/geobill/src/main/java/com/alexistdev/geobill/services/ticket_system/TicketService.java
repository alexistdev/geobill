package com.alexistdev.geobill.services.ticket_system;

import com.alexistdev.geobill.dto.ticket_system.*;
import com.alexistdev.geobill.exceptions.ConflictException;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.*;
import com.alexistdev.geobill.models.repository.HostingRepo;
import com.alexistdev.geobill.models.repository.InvoiceRepo;
import com.alexistdev.geobill.models.repository.UserRepo;
import com.alexistdev.geobill.models.repository.ticket_system.*;
import com.alexistdev.geobill.request.ticket_system.TicketFilterRequest;
import com.alexistdev.geobill.request.ticket_system.TicketReplyRequest;
import com.alexistdev.geobill.request.ticket_system.TicketRequest;
import com.alexistdev.geobill.utils.MessagesUtils;
import com.alexistdev.geobill.utils.TicketCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TicketService {

    private static final String DATE_PATTERN = "dd-MM-yyyy HH:mm";
    private static final String FILTER_ANY = "Any";
    private static final String LAST_REPLY_TODAY = "Today";
    private static final String LAST_REPLY_7_DAYS = "Last 7 days";
    private static final String LAST_REPLY_30_DAYS = "Last 30 days";
    private static final String UNASSIGNED = "Unassigned";
    private static final long ONE_DAY = 24L * 60 * 60 * 1000;

    private final TicketRepo ticketRepo;
    private final TicketReplyRepo replyRepo;
    private final TicketHistoryRepo historyRepo;
    private final TicketAttachmentRepo attachmentRepo;
    private final TicketDepartmentRepo departmentRepo;
    private final TicketDepartmentStaffRepo departmentStaffRepo;
    private final UserRepo userRepo;
    private final HostingRepo hostingRepo;
    private final InvoiceRepo invoiceRepo;
    private final TicketCodeGenerator ticketCodeGenerator;
    private final MessagesUtils messagesUtils;

    public TicketService(TicketRepo ticketRepo,
                         TicketReplyRepo replyRepo,
                         TicketHistoryRepo historyRepo,
                         TicketAttachmentRepo attachmentRepo,
                         TicketDepartmentRepo departmentRepo,
                         TicketDepartmentStaffRepo departmentStaffRepo,
                         UserRepo userRepo,
                         HostingRepo hostingRepo,
                         InvoiceRepo invoiceRepo,
                         TicketCodeGenerator ticketCodeGenerator,
                         MessagesUtils messagesUtils) {
        this.ticketRepo = ticketRepo;
        this.replyRepo = replyRepo;
        this.historyRepo = historyRepo;
        this.attachmentRepo = attachmentRepo;
        this.departmentRepo = departmentRepo;
        this.departmentStaffRepo = departmentStaffRepo;
        this.userRepo = userRepo;
        this.hostingRepo = hostingRepo;
        this.invoiceRepo = invoiceRepo;
        this.ticketCodeGenerator = ticketCodeGenerator;
        this.messagesUtils = messagesUtils;
    }

    /* ---------------------------------------------------------------- pembuatan */

    @Transactional
    public TicketDetailDTO createTicket(TicketRequest request) {
        User client = findUser(UUID.fromString(request.getUserId()));
        TicketDepartment department = findDepartment(UUID.fromString(request.getDepartmentId()));

        if (!Boolean.TRUE.equals(department.getIsActive())) {
            throw new ConflictException(
                    messagesUtils.getMessage("ticketservice.department_inactive", department.getName()));
        }

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(ticketCodeGenerator.generateTicketCode());
        ticket.setUser(client);
        ticket.setDepartment(department);
        ticket.setSubject(request.getSubject());
        ticket.setPriority(parsePriority(request.getPriority(), TicketPriority.MEDIUM));
        ticket.setStatus(TicketStatus.AWAITING_STAFF);
        ticket.setSource(TicketSource.WEB);
        ticket.setCcEmails(request.getCcEmails());
        ticket.setHosting(findHosting(request.getHostingId()));
        ticket.setInvoice(findInvoice(request.getInvoiceId()));

        Ticket saved = ticketRepo.save(ticket);
        TicketReply firstReply = saveReply(saved, client, TicketAuthorType.CLIENT, request.getMessage(), false);
        applyReplyToTicket(saved, firstReply);
        ticketRepo.save(saved);

        writeHistory(saved, client, TicketHistoryAction.CREATED, null, TicketStatus.AWAITING_STAFF.name(), null);
        return convertToDetailDTO(saved, true);
    }

    /* ----------------------------------------------------------------- balasan */

    /**
     * Menyimpan satu balasan lalu memindahkan tiket ke tab lawan bicara. Catatan internal
     * sengaja tidak mengubah status maupun lastReplyAt, supaya tiket yang sedang menunggu
     * klien tidak berpindah tab hanya karena staff menulis catatan.
     */
    @Transactional
    public TicketReplyDTO addReply(TicketReplyRequest request) {
        Ticket ticket = findTicket(UUID.fromString(request.getTicketId()));
        User author = findUser(UUID.fromString(request.getUserId()));
        boolean authorIsStaff = isStaff(author);

        if (!authorIsStaff && !ticket.getUser().getId().equals(author.getId())) {
            throw new ConflictException(messagesUtils.getMessage("ticketservice.reply_not_allowed"));
        }
        if (request.isInternalNote() && !authorIsStaff) {
            throw new ConflictException(messagesUtils.getMessage("ticketservice.internal_note_not_allowed"));
        }
        if (ticket.getStatus() == TicketStatus.TRASH) {
            throw new ConflictException(messagesUtils.getMessage("ticketservice.ticket_in_trash"));
        }

        TicketAuthorType authorType = authorIsStaff ? TicketAuthorType.STAFF : TicketAuthorType.CLIENT;
        TicketReply reply = saveReply(ticket, author, authorType, request.getMessage(), request.isInternalNote());

        if (!request.isInternalNote()) {
            TicketStatus previousStatus = ticket.getStatus();
            applyReplyToTicket(ticket, reply);
            ticket.setStatus(authorIsStaff ? TicketStatus.AWAITING_CLIENT : TicketStatus.AWAITING_STAFF);

            if (previousStatus == TicketStatus.CLOSED) {
                ticket.setClosedAt(null);
                ticket.setClosedBy(null);
                writeHistory(ticket, author, TicketHistoryAction.REOPENED, previousStatus.name(),
                        ticket.getStatus().name(), null);
            } else if (previousStatus != ticket.getStatus()) {
                writeHistory(ticket, author, TicketHistoryAction.STATUS_CHANGED, previousStatus.name(),
                        ticket.getStatus().name(), null);
            }
            ticketRepo.save(ticket);
        }

        writeHistory(ticket, author, TicketHistoryAction.REPLIED, null, null,
                request.isInternalNote() ? "Catatan internal" : null);
        return convertToReplyDTO(reply);
    }

    /* -------------------------------------------------------------- penanganan */

    @Transactional
    public TicketDetailDTO assignTicket(UUID ticketId, UUID staffId, User actor) {
        Ticket ticket = findTicket(ticketId);
        User staff = findUser(staffId);

        if (!isStaff(staff)) {
            throw new ConflictException(messagesUtils.getMessage("ticketservice.staff_not_allowed"));
        }
        if (staff.getRole() == Role.STAFF
                && !departmentStaffRepo.existsByDepartment_IdAndUser_Id(ticket.getDepartment().getId(), staffId)) {
            throw new ConflictException(messagesUtils.getMessage("ticketservice.staff_not_in_department"));
        }

        String previousAssignee = ticket.getAssignedTo() == null ? null : ticket.getAssignedTo().getFullName();
        ticket.setAssignedTo(staff);

        if (ticket.getStatus() == TicketStatus.AWAITING_STAFF) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        ticketRepo.save(ticket);
        writeHistory(ticket, actor, TicketHistoryAction.ASSIGNED, previousAssignee, staff.getFullName(), null);
        return convertToDetailDTO(ticket, true);
    }

    @Transactional
    public TicketDetailDTO unassignTicket(UUID ticketId, User actor) {
        Ticket ticket = findTicket(ticketId);
        String previousAssignee = ticket.getAssignedTo() == null ? null : ticket.getAssignedTo().getFullName();

        ticket.setAssignedTo(null);
        ticketRepo.save(ticket);

        writeHistory(ticket, actor, TicketHistoryAction.UNASSIGNED, previousAssignee, null, null);
        return convertToDetailDTO(ticket, true);
    }

    @Transactional
    public TicketDetailDTO changeStatus(UUID ticketId, String statusKey, User actor) {
        Ticket ticket = findTicket(ticketId);
        TicketStatus newStatus = parseStatus(statusKey);
        TicketStatus previousStatus = ticket.getStatus();

        if (newStatus == TicketStatus.TRASH) {
            return moveToTrash(ticketId, actor);
        }
        if (previousStatus == TicketStatus.TRASH) {
            throw new ConflictException(messagesUtils.getMessage("ticketservice.ticket_in_trash"));
        }

        applyStatus(ticket, newStatus, actor);
        ticketRepo.save(ticket);

        if (previousStatus != newStatus) {
            TicketHistoryAction action = switch (newStatus) {
                case CLOSED -> TicketHistoryAction.CLOSED;
                case AWAITING_STAFF, IN_PROGRESS -> previousStatus == TicketStatus.CLOSED
                        ? TicketHistoryAction.REOPENED
                        : TicketHistoryAction.STATUS_CHANGED;
                default -> TicketHistoryAction.STATUS_CHANGED;
            };
            writeHistory(ticket, actor, action, previousStatus.name(), newStatus.name(), null);
        }
        return convertToDetailDTO(ticket, true);
    }

    @Transactional
    public TicketDetailDTO changePriority(UUID ticketId, String priorityKey, User actor) {
        Ticket ticket = findTicket(ticketId);
        TicketPriority newPriority = parsePriority(priorityKey, null);
        TicketPriority previousPriority = ticket.getPriority();

        ticket.setPriority(newPriority);
        ticketRepo.save(ticket);

        if (previousPriority != newPriority) {
            writeHistory(ticket, actor, TicketHistoryAction.PRIORITY_CHANGED, previousPriority.name(),
                    newPriority.name(), null);
        }
        return convertToDetailDTO(ticket, true);
    }

    /** Memindahkan departemen juga melepas assignee, karena staff lama belum tentu menangani departemen baru. */
    @Transactional
    public TicketDetailDTO changeDepartment(UUID ticketId, UUID departmentId, User actor) {
        Ticket ticket = findTicket(ticketId);
        TicketDepartment newDepartment = findDepartment(departmentId);
        TicketDepartment previousDepartment = ticket.getDepartment();

        if (previousDepartment.getId().equals(departmentId)) {
            return convertToDetailDTO(ticket, true);
        }

        ticket.setDepartment(newDepartment);
        if (ticket.getAssignedTo() != null
                && !departmentStaffRepo.existsByDepartment_IdAndUser_Id(departmentId, ticket.getAssignedTo().getId())) {
            ticket.setAssignedTo(null);
        }
        ticketRepo.save(ticket);

        writeHistory(ticket, actor, TicketHistoryAction.DEPARTMENT_CHANGED, previousDepartment.getName(),
                newDepartment.getName(), null);
        return convertToDetailDTO(ticket, true);
    }

    /* ------------------------------------------------------------ trash & hapus */

    /**
     * Membuang tiket hanya mengubah statusnya, bukan menyalakan is_deleted, supaya tiket
     * masih terbaca query dan bisa dipulihkan dari tab Trash.
     */
    @Transactional
    public TicketDetailDTO moveToTrash(UUID ticketId, User actor) {
        Ticket ticket = findTicket(ticketId);
        TicketStatus previousStatus = ticket.getStatus();

        if (previousStatus == TicketStatus.TRASH) {
            throw new ConflictException(messagesUtils.getMessage("ticketservice.ticket_in_trash"));
        }

        ticket.setStatus(TicketStatus.TRASH);
        ticketRepo.save(ticket);

        writeHistory(ticket, actor, TicketHistoryAction.TRASHED, previousStatus.name(), TicketStatus.TRASH.name(), null);
        return convertToDetailDTO(ticket, true);
    }

    /** Status pemulihan dibaca dari catatan TRASHED terakhir, bukan ditebak. */
    @Transactional
    public TicketDetailDTO restoreFromTrash(UUID ticketId, User actor) {
        Ticket ticket = findTicket(ticketId);

        if (ticket.getStatus() != TicketStatus.TRASH) {
            throw new ConflictException(messagesUtils.getMessage("ticketservice.ticket_not_in_trash"));
        }

        TicketStatus restoredStatus = historyRepo
                .findFirstByTicket_IdAndActionOrderByCreatedDateDesc(ticketId, TicketHistoryAction.TRASHED)
                .map(TicketHistory::getOldValue)
                .map(TicketStatus::valueOf)
                .orElse(TicketStatus.AWAITING_STAFF);

        ticket.setStatus(restoredStatus);
        ticketRepo.save(ticket);

        writeHistory(ticket, actor, TicketHistoryAction.RESTORED, TicketStatus.TRASH.name(), restoredStatus.name(),
                null);
        return convertToDetailDTO(ticket, true);
    }

    /** Hapus permanen dari Trash: barulah soft delete dipakai, dan tiket hilang dari semua query. */
    @Transactional
    public void deleteTicket(UUID ticketId) {
        Ticket ticket = findTicket(ticketId);

        if (ticket.getStatus() != TicketStatus.TRASH) {
            throw new ConflictException(messagesUtils.getMessage("ticketservice.ticket_not_in_trash"));
        }

        replyRepo.findByTicketId(ticketId).forEach(replyRepo::delete);
        attachmentRepo.findByTicket_IdOrderByCreatedDateAsc(ticketId).forEach(attachmentRepo::delete);
        historyRepo.findByTicketId(ticketId).forEach(historyRepo::delete);
        ticketRepo.delete(ticket);
    }

    /* ------------------------------------------------------------------ daftar */

    public Page<TicketDTO> getTickets(String statusKey, TicketFilterRequest filter, Pageable pageable) {
        TicketStatus status = parseStatus(statusKey);
        TicketFilterRequest criteria = filter == null ? new TicketFilterRequest() : filter;

        Page<Ticket> result = ticketRepo.findByFilter(
                status,
                blankToNull(criteria.getTicketNumber()),
                blankToNull(criteria.getSummary()),
                parsePriorityFilter(criteria.getPriority()),
                parseUuidFilter(criteria.getDepartmentId()),
                parseUuidFilter(criteria.getAssignedTo()),
                parseLastReplyFilter(criteria.getLastReply()),
                pageable);

        return toTicketPage(result, pageable);
    }

    public Page<TicketDTO> getTicketsByUser(User user, String statusKey, Pageable pageable) {
        Page<Ticket> result = statusKey == null || statusKey.isBlank()
                ? ticketRepo.findByUser(user, pageable)
                : ticketRepo.findByUserAndStatus(user, parseStatus(statusKey), pageable);
        return toTicketPage(result, pageable);
    }

    public Page<TicketDTO> getOpenTicketsByStaff(UUID staffId, Pageable pageable) {
        return toTicketPage(ticketRepo.findOpenByAssignedTo(staffId, pageable), pageable);
    }

    /** Angka pada keenam tab. Status tanpa tiket tetap dikirim bernilai nol. */
    public List<TicketTabDTO> getTabs() {
        return buildTabs(ticketRepo.countGroupByStatus());
    }

    public List<TicketTabDTO> getTabsByUser(User user) {
        return buildTabs(ticketRepo.countGroupByStatusForUser(user));
    }

    /** Klien hanya boleh membuka tiketnya sendiri, dan tidak pernah melihat catatan internal. */
    public TicketDetailDTO getTicketDetail(UUID ticketId, User actor) {
        boolean actorIsStaff = isStaff(actor);
        Ticket ticket = actorIsStaff
                ? findTicket(ticketId)
                : ticketRepo.findByIdAndUser(ticketId, actor)
                        .orElseThrow(() -> new NotFoundException(
                                messagesUtils.getMessage("ticketservice.ticket_not_found", ticketId.toString())));
        return convertToDetailDTO(ticket, actorIsStaff);
    }

    public TicketDetailDTO getTicketByNumber(String ticketNumber, User actor) {
        Ticket ticket = ticketRepo.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("ticketservice.ticket_number_not_found", ticketNumber)));
        return getTicketDetail(ticket.getId(), actor);
    }

    /* -------------------------------------------------------------- auto close */

    /**
     * Menutup tiket yang menunggu klien melewati autoCloseDays departemennya. Satu query
     * mengambil kandidat dengan batas paling longgar, lalu tiap tiket diuji dengan batas
     * departemennya sendiri.
     */
    @Transactional
    public int autoCloseIdleTickets() {
        Map<UUID, Integer> autoCloseByDepartment = departmentRepo.findByIsActiveTrueOrderBySortOrderAsc().stream()
                .filter(department -> department.getAutoCloseDays() != null)
                .collect(Collectors.toMap(TicketDepartment::getId, TicketDepartment::getAutoCloseDays));

        if (autoCloseByDepartment.isEmpty()) {
            return 0;
        }

        int longestIdleDays = Collections.max(autoCloseByDepartment.values());
        Date now = new Date();
        List<Ticket> candidates = ticketRepo.findIdleAwaitingClient(new Date(now.getTime() - longestIdleDays * ONE_DAY));

        int closed = 0;
        for (Ticket ticket : candidates) {
            Integer idleDays = autoCloseByDepartment.get(ticket.getDepartment().getId());
            if (idleDays == null || ticket.getLastReplyAt() == null) {
                continue;
            }
            if (ticket.getLastReplyAt().getTime() > now.getTime() - idleDays * ONE_DAY) {
                continue;
            }

            TicketStatus previousStatus = ticket.getStatus();
            ticket.setStatus(TicketStatus.CLOSED);
            ticket.setClosedAt(now);
            ticketRepo.save(ticket);
            writeHistory(ticket, null, TicketHistoryAction.CLOSED, previousStatus.name(), TicketStatus.CLOSED.name(),
                    "Ditutup otomatis karena klien tidak membalas");
            closed++;
        }

        log.info("Auto close finished, {} ticket(s) closed", closed);
        return closed;
    }

    /* ------------------------------------------------------------------ bantuan */

    public Ticket findTicket(UUID ticketId) {
        return ticketRepo.findById(ticketId)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("ticketservice.ticket_not_found", ticketId.toString())));
    }

    private User findUser(UUID userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(messagesUtils.getMessage("ticketservice.user_not_found")));
    }

    private TicketDepartment findDepartment(UUID departmentId) {
        return departmentRepo.findById(departmentId)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("ticketservice.department_not_found")));
    }

    private Hosting findHosting(String hostingId) {
        if (hostingId == null || hostingId.isBlank()) {
            return null;
        }
        return hostingRepo.findById(UUID.fromString(hostingId))
                .orElseThrow(() -> new NotFoundException(messagesUtils.getMessage("ticketservice.hosting_not_found")));
    }

    private Invoice findInvoice(String invoiceId) {
        if (invoiceId == null || invoiceId.isBlank()) {
            return null;
        }
        return invoiceRepo.findById(UUID.fromString(invoiceId))
                .orElseThrow(() -> new NotFoundException(messagesUtils.getMessage("ticketservice.invoice_not_found")));
    }

    private boolean isStaff(User user) {
        return user != null && (user.getRole() == Role.STAFF || user.getRole() == Role.ADMIN);
    }

    private TicketReply saveReply(Ticket ticket, User author, TicketAuthorType authorType, String message,
                                  boolean internalNote) {
        TicketReply reply = new TicketReply();
        reply.setTicket(ticket);
        reply.setUser(author);
        reply.setAuthorType(authorType);
        reply.setAuthorName(author == null ? null : author.getFullName());
        reply.setMessage(message);
        reply.setIsInternalNote(internalNote);
        return replyRepo.save(reply);
    }

    private void applyReplyToTicket(Ticket ticket, TicketReply reply) {
        ticket.setReplyCount(ticket.getReplyCount() + 1);
        ticket.setLastReplyAt(new Date());
        ticket.setLastReplyBy(reply.getUser());
        ticket.setLastReplyAuthorType(reply.getAuthorType());

        if (reply.getAuthorType() == TicketAuthorType.STAFF && ticket.getFirstResponseAt() == null) {
            ticket.setFirstResponseAt(ticket.getLastReplyAt());
        }
    }

    private void applyStatus(Ticket ticket, TicketStatus newStatus, User actor) {
        ticket.setStatus(newStatus);

        if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(new Date());
            ticket.setClosedBy(actor);
        } else {
            ticket.setClosedAt(null);
            ticket.setClosedBy(null);
        }
    }

    private void writeHistory(Ticket ticket, User actor, TicketHistoryAction action, String oldValue, String newValue,
                              String note) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setActor(actor);
        history.setAction(action);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setNote(note);
        historyRepo.save(history);
    }

    private List<TicketTabDTO> buildTabs(List<TicketStatusCount> counts) {
        Map<TicketStatus, Long> countByStatus = counts.stream()
                .collect(Collectors.toMap(TicketStatusCount::getStatus, TicketStatusCount::getTotal));

        return Arrays.stream(TicketStatus.values())
                .map(status -> {
                    TicketTabDTO tab = new TicketTabDTO();
                    tab.setKey(status.getKey());
                    tab.setLabel(toLabel(status));
                    tab.setCount(countByStatus.getOrDefault(status, 0L));
                    return tab;
                })
                .toList();
    }

    private String toLabel(TicketStatus status) {
        return Arrays.stream(status.name().split("_"))
                .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private TicketStatus parseStatus(String statusKey) {
        if (statusKey == null || statusKey.isBlank()) {
            throw new NotFoundException(messagesUtils.getMessage("ticketservice.ticket_not_found", "null"));
        }
        try {
            return TicketStatus.valueOf(statusKey.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(messagesUtils.getMessage("ticketservice.ticket_not_found", statusKey));
        }
    }

    private TicketPriority parsePriority(String priorityKey, TicketPriority fallback) {
        if (priorityKey == null || priorityKey.isBlank()) {
            if (fallback == null) {
                throw new ConflictException(messagesUtils.getMessage("ticketservice.ticket_not_found", "priority"));
            }
            return fallback;
        }
        try {
            return TicketPriority.valueOf(priorityKey.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            if (fallback == null) {
                throw new ConflictException(messagesUtils.getMessage("ticketservice.ticket_not_found", priorityKey));
            }
            return fallback;
        }
    }

    private TicketPriority parsePriorityFilter(String priority) {
        String value = blankToNull(priority);
        if (value == null || FILTER_ANY.equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return TicketPriority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private UUID parseUuidFilter(String value) {
        String candidate = blankToNull(value);
        if (candidate == null || FILTER_ANY.equalsIgnoreCase(candidate)) {
            return null;
        }
        try {
            return UUID.fromString(candidate);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Date parseLastReplyFilter(String lastReply) {
        String value = blankToNull(lastReply);
        if (value == null || FILTER_ANY.equalsIgnoreCase(value)) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        if (LAST_REPLY_TODAY.equalsIgnoreCase(value)) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        }
        if (LAST_REPLY_7_DAYS.equalsIgnoreCase(value)) {
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            return calendar.getTime();
        }
        if (LAST_REPLY_30_DAYS.equalsIgnoreCase(value)) {
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            return calendar.getTime();
        }
        return null;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String formatDate(Date date) {
        return date == null ? null : new SimpleDateFormat(DATE_PATTERN).format(date);
    }

    /* -------------------------------------------------------------- konversi */

    private Page<TicketDTO> toTicketPage(Page<Ticket> result, Pageable pageable) {
        List<TicketDTO> content = result.getContent().stream()
                .map(this::convertToDTO)
                .toList();
        return new PageImpl<>(content, pageable, result.getTotalElements());
    }

    private TicketDTO convertToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId().toString());
        dto.setNumber(ticket.getTicketNumber());
        dto.setSummary(ticket.getSubject());
        dto.setPriority(ticket.getPriority().name());
        dto.setDepartment(ticket.getDepartment().getName());
        dto.setDepartmentId(ticket.getDepartment().getId().toString());
        dto.setAssignedTo(ticket.getAssignedTo() == null ? UNASSIGNED : ticket.getAssignedTo().getFullName());
        dto.setAssignedToId(ticket.getAssignedTo() == null ? null : ticket.getAssignedTo().getId().toString());
        dto.setRequester(ticket.getUser().getFullName());
        dto.setLastReply(formatDate(ticket.getLastReplyAt()));
        dto.setStatus(ticket.getStatus().getKey());
        dto.setReplyCount(ticket.getReplyCount());
        return dto;
    }

    private TicketDetailDTO convertToDetailDTO(Ticket ticket, boolean includeInternalNote) {
        TicketDetailDTO dto = new TicketDetailDTO();
        dto.setId(ticket.getId().toString());
        dto.setNumber(ticket.getTicketNumber());
        dto.setSubject(ticket.getSubject());
        dto.setPriority(ticket.getPriority().name());
        dto.setStatus(ticket.getStatus().getKey());
        dto.setSource(ticket.getSource().name());
        dto.setDepartment(ticket.getDepartment().getName());
        dto.setDepartmentId(ticket.getDepartment().getId().toString());
        dto.setRequester(ticket.getUser().getFullName());
        dto.setRequesterId(ticket.getUser().getId().toString());
        dto.setAssignedTo(ticket.getAssignedTo() == null ? UNASSIGNED : ticket.getAssignedTo().getFullName());
        dto.setAssignedToId(ticket.getAssignedTo() == null ? null : ticket.getAssignedTo().getId().toString());
        dto.setHostingId(ticket.getHosting() == null ? null : ticket.getHosting().getId().toString());
        dto.setInvoiceId(ticket.getInvoice() == null ? null : ticket.getInvoice().getId().toString());
        dto.setCcEmails(ticket.getCcEmails());
        dto.setCreatedDate(formatDate(ticket.getCreatedDate()));
        dto.setLastReply(formatDate(ticket.getLastReplyAt()));
        dto.setClosedDate(formatDate(ticket.getClosedAt()));
        dto.setRating(ticket.getRating());
        dto.setFlagged(Boolean.TRUE.equals(ticket.getIsFlagged()));

        List<TicketReply> replies = includeInternalNote
                ? replyRepo.findByTicketId(ticket.getId())
                : replyRepo.findPublicByTicketId(ticket.getId());
        dto.setReplies(replies.stream().map(this::convertToReplyDTO).toList());
        dto.setAttachments(attachmentRepo.findByTicket_IdOrderByCreatedDateAsc(ticket.getId()).stream()
                .map(this::convertToAttachmentDTO)
                .toList());
        return dto;
    }

    private TicketReplyDTO convertToReplyDTO(TicketReply reply) {
        TicketReplyDTO dto = new TicketReplyDTO();
        dto.setId(reply.getId().toString());
        dto.setTicketId(reply.getTicket().getId().toString());
        dto.setAuthorType(reply.getAuthorType().name());
        dto.setAuthorName(reply.getAuthorName());
        dto.setAuthorId(reply.getUser() == null ? null : reply.getUser().getId().toString());
        dto.setMessage(reply.getMessage());
        dto.setInternalNote(Boolean.TRUE.equals(reply.getIsInternalNote()));
        dto.setCreatedDate(formatDate(reply.getCreatedDate()));
        dto.setAttachments(attachmentRepo.findByReply_Id(reply.getId()).stream()
                .map(this::convertToAttachmentDTO)
                .toList());
        return dto;
    }

    private TicketAttachmentDTO convertToAttachmentDTO(TicketAttachment attachment) {
        TicketAttachmentDTO dto = new TicketAttachmentDTO();
        dto.setId(attachment.getId().toString());
        dto.setTicketId(attachment.getTicket().getId().toString());
        dto.setReplyId(attachment.getReply() == null ? null : attachment.getReply().getId().toString());
        dto.setName(attachment.getOriginalName());
        dto.setMimeType(attachment.getMimeType());
        dto.setFileSize(attachment.getFileSize());
        dto.setUploadedBy(attachment.getUploadedBy() == null ? null : attachment.getUploadedBy().getFullName());
        dto.setCreatedDate(formatDate(attachment.getCreatedDate()));
        return dto;
    }
}
