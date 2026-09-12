package com.alexistdev.geobill.service.ticket_system;

import com.alexistdev.geobill.dto.ticket_system.TicketDTO;
import com.alexistdev.geobill.dto.ticket_system.TicketDetailDTO;
import com.alexistdev.geobill.dto.ticket_system.TicketReplyDTO;
import com.alexistdev.geobill.dto.ticket_system.TicketTabDTO;
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
import com.alexistdev.geobill.services.ticket_system.TicketService;
import com.alexistdev.geobill.utils.MessagesUtils;
import com.alexistdev.geobill.utils.TicketCodeGenerator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketServiceTest {

    private static final long ONE_DAY = 24L * 60 * 60 * 1000;

    @Mock private TicketRepo ticketRepo;
    @Mock private TicketReplyRepo replyRepo;
    @Mock private TicketHistoryRepo historyRepo;
    @Mock private TicketAttachmentRepo attachmentRepo;
    @Mock private TicketDepartmentRepo departmentRepo;
    @Mock private TicketDepartmentStaffRepo departmentStaffRepo;
    @Mock private UserRepo userRepo;
    @Mock private HostingRepo hostingRepo;
    @Mock private InvoiceRepo invoiceRepo;
    @Mock private TicketCodeGenerator ticketCodeGenerator;
    @Mock private MessagesUtils messagesUtils;

    @InjectMocks private TicketService ticketService;

    private User client;
    private User staff;
    private TicketDepartment department;
    private Ticket ticket;

    /** Hasil group by status yang dikembalikan repository sebagai projection. */
    private record StatusCount(TicketStatus status, Long total) implements TicketStatusCount {
        @Override
        public TicketStatus getStatus() {
            return status;
        }

        @Override
        public Long getTotal() {
            return total;
        }
    }

    @BeforeEach
    void setUp() {
        lenient().when(messagesUtils.getMessage(anyString())).thenReturn("message");
        lenient().when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("message");

        client = createUser("Client One", "client1@geobill.test", Role.USER);
        staff = createUser("Staff One", "staff1@geobill.test", Role.STAFF);

        department = new TicketDepartment();
        department.setId(UUID.randomUUID());
        department.setName("Technical Support");
        department.setCode("TECH");
        department.setIsActive(Boolean.TRUE);

        ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setTicketNumber("TKT-202609-000001");
        ticket.setSubject("Website down");
        ticket.setUser(client);
        ticket.setDepartment(department);
        ticket.setStatus(TicketStatus.AWAITING_STAFF);
        ticket.setPriority(TicketPriority.HIGH);
    }

    private User createUser(String fullName, String email, Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private void stubSaves() {
        lenient().when(ticketRepo.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(UUID.randomUUID());
            }
            return saved;
        });
        lenient().when(replyRepo.save(any(TicketReply.class))).thenAnswer(invocation -> {
            TicketReply saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(UUID.randomUUID());
            }
            return saved;
        });
    }

    private TicketRequest ticketRequest() {
        TicketRequest request = new TicketRequest();
        request.setUserId(client.getId().toString());
        request.setDepartmentId(department.getId().toString());
        request.setSubject("Website down");
        request.setMessage("Situs saya error 500");
        request.setPriority("HIGH");
        return request;
    }

    private TicketReplyRequest replyRequest(User author, String message, boolean internalNote) {
        TicketReplyRequest request = new TicketReplyRequest();
        request.setTicketId(ticket.getId().toString());
        request.setUserId(author.getId().toString());
        request.setMessage(message);
        request.setInternalNote(internalNote);
        return request;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Create Ticket")
    void testCreateTicket() {
        stubSaves();
        when(userRepo.findById(client.getId())).thenReturn(Optional.of(client));
        when(departmentRepo.findById(department.getId())).thenReturn(Optional.of(department));
        when(ticketCodeGenerator.generateTicketCode()).thenReturn("TKT-202609-000001");

        TicketDetailDTO result = ticketService.createTicket(ticketRequest());

        Assertions.assertEquals("TKT-202609-000001", result.getNumber());
        Assertions.assertEquals("awaiting_staff", result.getStatus());
        Assertions.assertEquals("HIGH", result.getPriority());
        Assertions.assertEquals("Unassigned", result.getAssignedTo());

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepo, times(2)).save(captor.capture());
        Ticket saved = captor.getValue();
        Assertions.assertEquals(1, saved.getReplyCount());
        Assertions.assertNotNull(saved.getLastReplyAt());
        Assertions.assertEquals(TicketAuthorType.CLIENT, saved.getLastReplyAuthorType());
        Assertions.assertNull(saved.getFirstResponseAt(), "Belum ada balasan staff");

        verify(replyRepo, times(1)).save(any(TicketReply.class));
        verify(historyRepo, times(1)).save(any(TicketHistory.class));
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Create Ticket On Inactive Department")
    void testCreateTicketOnInactiveDepartment() {
        department.setIsActive(Boolean.FALSE);
        when(userRepo.findById(client.getId())).thenReturn(Optional.of(client));
        when(departmentRepo.findById(department.getId())).thenReturn(Optional.of(department));

        TicketRequest request = ticketRequest();
        Assertions.assertThrows(ConflictException.class, () -> ticketService.createTicket(request));
        verify(ticketRepo, never()).save(any(Ticket.class));
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Create Ticket With Unknown User")
    void testCreateTicketWithUnknownUser() {
        when(userRepo.findById(client.getId())).thenReturn(Optional.empty());

        TicketRequest request = ticketRequest();
        Assertions.assertThrows(NotFoundException.class, () -> ticketService.createTicket(request));
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Staff Reply Moves Ticket To Awaiting Client")
    void testStaffReply() {
        stubSaves();
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(userRepo.findById(staff.getId())).thenReturn(Optional.of(staff));

        TicketReplyDTO result = ticketService.addReply(replyRequest(staff, "Sedang kami cek", false));

        Assertions.assertEquals("STAFF", result.getAuthorType());
        Assertions.assertFalse(result.isInternalNote());
        Assertions.assertEquals(TicketStatus.AWAITING_CLIENT, ticket.getStatus());
        Assertions.assertEquals(1, ticket.getReplyCount());
        Assertions.assertNotNull(ticket.getFirstResponseAt(), "Respons pertama staff dicatat untuk SLA");
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Client Reply Moves Ticket Back To Awaiting Staff")
    void testClientReply() {
        stubSaves();
        ticket.setStatus(TicketStatus.AWAITING_CLIENT);
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(userRepo.findById(client.getId())).thenReturn(Optional.of(client));

        ticketService.addReply(replyRequest(client, "Masih error", false));

        Assertions.assertEquals(TicketStatus.AWAITING_STAFF, ticket.getStatus());
        Assertions.assertEquals(TicketAuthorType.CLIENT, ticket.getLastReplyAuthorType());
        Assertions.assertNull(ticket.getFirstResponseAt(), "Balasan klien bukan respons pertama staff");
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Internal Note Keeps Status And Reply Count")
    void testInternalNoteKeepsStatus() {
        stubSaves();
        ticket.setStatus(TicketStatus.AWAITING_CLIENT);
        ticket.setReplyCount(2);
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(userRepo.findById(staff.getId())).thenReturn(Optional.of(staff));

        TicketReplyDTO result = ticketService.addReply(replyRequest(staff, "Catatan internal", true));

        Assertions.assertTrue(result.isInternalNote());
        Assertions.assertEquals(TicketStatus.AWAITING_CLIENT, ticket.getStatus(),
                "Catatan internal tidak memindahkan tiket ke tab lain");
        Assertions.assertEquals(2, ticket.getReplyCount());
        Assertions.assertNull(ticket.getLastReplyAt());
        verify(ticketRepo, never()).save(any(Ticket.class));
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Reply Rules For Client")
    void testReplyRulesForClient() {
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(userRepo.findById(client.getId())).thenReturn(Optional.of(client));

        TicketReplyRequest internalNote = replyRequest(client, "diam-diam", true);
        Assertions.assertThrows(ConflictException.class, () -> ticketService.addReply(internalNote));

        User otherClient = createUser("Client Two", "client2@geobill.test", Role.USER);
        when(userRepo.findById(otherClient.getId())).thenReturn(Optional.of(otherClient));
        TicketReplyRequest otherTicket = replyRequest(otherClient, "numpang", false);
        Assertions.assertThrows(ConflictException.class, () -> ticketService.addReply(otherTicket));

        verify(replyRepo, never()).save(any(TicketReply.class));
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Reply To Trashed Ticket Is Rejected")
    void testReplyToTrashedTicket() {
        ticket.setStatus(TicketStatus.TRASH);
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(userRepo.findById(staff.getId())).thenReturn(Optional.of(staff));

        TicketReplyRequest request = replyRequest(staff, "Halo", false);
        Assertions.assertThrows(ConflictException.class, () -> ticketService.addReply(request));
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Client Reply Reopens Closed Ticket")
    void testClientReplyReopensClosedTicket() {
        stubSaves();
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(new Date());
        ticket.setClosedBy(staff);
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(userRepo.findById(client.getId())).thenReturn(Optional.of(client));

        ticketService.addReply(replyRequest(client, "Masih bermasalah", false));

        Assertions.assertEquals(TicketStatus.AWAITING_STAFF, ticket.getStatus());
        Assertions.assertNull(ticket.getClosedAt());
        Assertions.assertNull(ticket.getClosedBy());

        ArgumentCaptor<TicketHistory> captor = ArgumentCaptor.forClass(TicketHistory.class);
        verify(historyRepo, atLeastOnce()).save(captor.capture());
        Assertions.assertTrue(captor.getAllValues().stream()
                .anyMatch(history -> history.getAction() == TicketHistoryAction.REOPENED));
    }

    @Test
    @Order(10)
    @DisplayName("10. Test Assign Ticket Puts It In Progress")
    void testAssignTicket() {
        stubSaves();
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(userRepo.findById(staff.getId())).thenReturn(Optional.of(staff));
        when(departmentStaffRepo.existsByDepartment_IdAndUser_Id(department.getId(), staff.getId())).thenReturn(true);

        TicketDetailDTO result = ticketService.assignTicket(ticket.getId(), staff.getId(), staff);

        Assertions.assertEquals("in_progress", result.getStatus());
        Assertions.assertEquals("Staff One", result.getAssignedTo());
        Assertions.assertEquals(staff, ticket.getAssignedTo());
    }

    @Test
    @Order(11)
    @DisplayName("11. Test Assign Rejects Client And Staff Outside Department")
    void testAssignRejectsInvalidStaff() {
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(userRepo.findById(client.getId())).thenReturn(Optional.of(client));

        UUID ticketId = ticket.getId();
        UUID clientId = client.getId();
        Assertions.assertThrows(ConflictException.class,
                () -> ticketService.assignTicket(ticketId, clientId, staff));

        User outsider = createUser("Outsider", "outsider@geobill.test", Role.STAFF);
        when(userRepo.findById(outsider.getId())).thenReturn(Optional.of(outsider));
        when(departmentStaffRepo.existsByDepartment_IdAndUser_Id(department.getId(), outsider.getId()))
                .thenReturn(false);
        UUID outsiderId = outsider.getId();
        Assertions.assertThrows(ConflictException.class,
                () -> ticketService.assignTicket(ticketId, outsiderId, staff));

        Assertions.assertNull(ticket.getAssignedTo());
    }

    @Test
    @Order(12)
    @DisplayName("12. Test Unassign Ticket")
    void testUnassignTicket() {
        stubSaves();
        ticket.setAssignedTo(staff);
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        TicketDetailDTO result = ticketService.unassignTicket(ticket.getId(), staff);

        Assertions.assertEquals("Unassigned", result.getAssignedTo());
        Assertions.assertNull(ticket.getAssignedTo());
    }

    @Test
    @Order(13)
    @DisplayName("13. Test Close Ticket Records Who And When")
    void testCloseTicket() {
        stubSaves();
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        TicketDetailDTO result = ticketService.changeStatus(ticket.getId(), "closed", staff);

        Assertions.assertEquals("closed", result.getStatus());
        Assertions.assertNotNull(ticket.getClosedAt());
        Assertions.assertEquals(staff, ticket.getClosedBy());
    }

    @Test
    @Order(14)
    @DisplayName("14. Test Change Priority")
    void testChangePriority() {
        stubSaves();
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        TicketDetailDTO result = ticketService.changePriority(ticket.getId(), "urgent", staff);

        Assertions.assertEquals("URGENT", result.getPriority());

        ArgumentCaptor<TicketHistory> captor = ArgumentCaptor.forClass(TicketHistory.class);
        verify(historyRepo).save(captor.capture());
        Assertions.assertEquals(TicketHistoryAction.PRIORITY_CHANGED, captor.getValue().getAction());
        Assertions.assertEquals("HIGH", captor.getValue().getOldValue());
    }

    @Test
    @Order(15)
    @DisplayName("15. Test Change Department Releases Outsider Assignee")
    void testChangeDepartmentReleasesAssignee() {
        stubSaves();
        ticket.setAssignedTo(staff);
        TicketDepartment billing = new TicketDepartment();
        billing.setId(UUID.randomUUID());
        billing.setName("Billing");
        billing.setCode("BILLING");

        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(departmentRepo.findById(billing.getId())).thenReturn(Optional.of(billing));
        when(departmentStaffRepo.existsByDepartment_IdAndUser_Id(billing.getId(), staff.getId())).thenReturn(false);

        TicketDetailDTO result = ticketService.changeDepartment(ticket.getId(), billing.getId(), staff);

        Assertions.assertEquals("Billing", result.getDepartment());
        Assertions.assertEquals("Unassigned", result.getAssignedTo());
    }

    @Test
    @Order(16)
    @DisplayName("16. Test Move To Trash Does Not Soft Delete")
    void testMoveToTrash() {
        stubSaves();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        TicketDetailDTO result = ticketService.moveToTrash(ticket.getId(), staff);

        Assertions.assertEquals("trash", result.getStatus());
        Assertions.assertFalse(ticket.getDeleted(), "Trash memakai status, bukan is_deleted");
        verify(ticketRepo, never()).delete(any(Ticket.class));

        ArgumentCaptor<TicketHistory> captor = ArgumentCaptor.forClass(TicketHistory.class);
        verify(historyRepo).save(captor.capture());
        Assertions.assertEquals(TicketHistoryAction.TRASHED, captor.getValue().getAction());
        Assertions.assertEquals("IN_PROGRESS", captor.getValue().getOldValue(),
                "Status sebelumnya disimpan supaya bisa dipulihkan");
    }

    @Test
    @Order(17)
    @DisplayName("17. Test Restore Reads Previous Status From History")
    void testRestoreFromTrash() {
        stubSaves();
        ticket.setStatus(TicketStatus.TRASH);
        TicketHistory trashed = new TicketHistory();
        trashed.setAction(TicketHistoryAction.TRASHED);
        trashed.setOldValue(TicketStatus.ON_HOLD.name());

        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(historyRepo.findFirstByTicket_IdAndActionOrderByCreatedDateDesc(ticket.getId(),
                TicketHistoryAction.TRASHED)).thenReturn(Optional.of(trashed));

        TicketDetailDTO result = ticketService.restoreFromTrash(ticket.getId(), staff);

        Assertions.assertEquals("on_hold", result.getStatus());
    }

    @Test
    @Order(18)
    @DisplayName("18. Test Restore Without History Falls Back To Awaiting Staff")
    void testRestoreWithoutHistory() {
        stubSaves();
        ticket.setStatus(TicketStatus.TRASH);
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(historyRepo.findFirstByTicket_IdAndActionOrderByCreatedDateDesc(ticket.getId(),
                TicketHistoryAction.TRASHED)).thenReturn(Optional.empty());

        Assertions.assertEquals("awaiting_staff", ticketService.restoreFromTrash(ticket.getId(), staff).getStatus());
    }

    @Test
    @Order(19)
    @DisplayName("19. Test Restore Rejects Ticket Outside Trash")
    void testRestoreRejectsTicketOutsideTrash() {
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        UUID ticketId = ticket.getId();
        Assertions.assertThrows(ConflictException.class, () -> ticketService.restoreFromTrash(ticketId, staff));
    }

    @Test
    @Order(20)
    @DisplayName("20. Test Delete Only Works From Trash")
    void testDeleteOnlyFromTrash() {
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        UUID ticketId = ticket.getId();
        Assertions.assertThrows(ConflictException.class, () -> ticketService.deleteTicket(ticketId));
        verify(ticketRepo, never()).delete(any(Ticket.class));

        ticket.setStatus(TicketStatus.TRASH);
        ticketService.deleteTicket(ticketId);

        verify(ticketRepo, times(1)).delete(ticket);
    }

    @Test
    @Order(21)
    @DisplayName("21. Test Filter Values Any Are Translated To No Criteria")
    void testFilterAnyBecomesNull() {
        when(ticketRepo.findByFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        TicketFilterRequest filter = new TicketFilterRequest();
        filter.setTicketNumber("  ");
        filter.setSummary("");
        filter.setPriority("Any");
        filter.setDepartmentId("Any");
        filter.setAssignedTo("Any");
        filter.setLastReply("Any");

        ticketService.getTickets("in_progress", filter, PageRequest.of(0, 10));

        verify(ticketRepo).findByFilter(eq(TicketStatus.IN_PROGRESS), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @Order(22)
    @DisplayName("22. Test Filter Criteria Are Passed To Repository")
    void testFilterCriteriaArePassed() {
        UUID assigneeId = staff.getId();
        Ticket listed = ticket;
        listed.setLastReplyAt(new Date());
        when(ticketRepo.findByFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(listed)));

        TicketFilterRequest filter = new TicketFilterRequest();
        filter.setTicketNumber("000001");
        filter.setSummary("website");
        filter.setPriority("high");
        filter.setDepartmentId(department.getId().toString());
        filter.setAssignedTo(assigneeId.toString());
        filter.setLastReply("Last 7 days");

        Page<TicketDTO> result = ticketService.getTickets("in_progress", filter, PageRequest.of(0, 10));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("TKT-202609-000001", result.getContent().get(0).getNumber());
        Assertions.assertEquals("awaiting_staff", result.getContent().get(0).getStatus());
        Assertions.assertEquals("Unassigned", result.getContent().get(0).getAssignedTo());

        ArgumentCaptor<Date> lastReply = ArgumentCaptor.forClass(Date.class);
        verify(ticketRepo).findByFilter(eq(TicketStatus.IN_PROGRESS), eq("000001"), eq("website"),
                eq(TicketPriority.HIGH), eq(department.getId()), eq(assigneeId), lastReply.capture(),
                any(Pageable.class));

        long sevenDaysAgo = System.currentTimeMillis() - 7 * ONE_DAY;
        Assertions.assertTrue(Math.abs(lastReply.getValue().getTime() - sevenDaysAgo) < ONE_DAY,
                "Last 7 days diterjemahkan menjadi batas tanggal tujuh hari lalu");
    }

    @Test
    @Order(23)
    @DisplayName("23. Test Unknown Status Key Is Rejected")
    void testUnknownStatusKey() {
        Pageable pageable = PageRequest.of(0, 10);
        TicketFilterRequest filter = new TicketFilterRequest();

        Assertions.assertThrows(NotFoundException.class,
                () -> ticketService.getTickets("unknown_tab", filter, pageable));
        verify(ticketRepo, never()).findByFilter(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @Order(24)
    @DisplayName("24. Test Tabs Always Carry Six Statuses")
    void testTabsAlwaysCarrySixStatuses() {
        when(ticketRepo.countGroupByStatus()).thenReturn(List.of(
                new StatusCount(TicketStatus.IN_PROGRESS, 2L),
                new StatusCount(TicketStatus.TRASH, 1L)));

        List<TicketTabDTO> tabs = ticketService.getTabs();

        Assertions.assertEquals(6, tabs.size());
        Assertions.assertEquals("awaiting_staff", tabs.get(0).getKey());
        Assertions.assertEquals("Awaiting Staff", tabs.get(0).getLabel());
        Assertions.assertEquals(0, tabs.get(0).getCount(), "Status tanpa tiket tetap dikirim bernilai nol");
        Assertions.assertEquals(2, tabs.stream().filter(tab -> "in_progress".equals(tab.getKey()))
                .findFirst().orElseThrow().getCount());
        Assertions.assertEquals(1, tabs.stream().filter(tab -> "trash".equals(tab.getKey()))
                .findFirst().orElseThrow().getCount());
    }

    @Test
    @Order(25)
    @DisplayName("25. Test Tabs By User")
    void testTabsByUser() {
        when(ticketRepo.countGroupByStatusForUser(client))
                .thenReturn(List.of(new StatusCount(TicketStatus.AWAITING_CLIENT, 3L)));

        List<TicketTabDTO> tabs = ticketService.getTabsByUser(client);

        Assertions.assertEquals(6, tabs.size());
        Assertions.assertEquals(3, tabs.stream().filter(tab -> "awaiting_client".equals(tab.getKey()))
                .findFirst().orElseThrow().getCount());
    }

    @Test
    @Order(26)
    @DisplayName("26. Test Ticket Detail Hides Internal Note From Client")
    void testTicketDetailHidesInternalNote() {
        TicketReply publicReply = new TicketReply();
        publicReply.setId(UUID.randomUUID());
        publicReply.setTicket(ticket);
        publicReply.setUser(client);
        publicReply.setAuthorType(TicketAuthorType.CLIENT);
        publicReply.setMessage("Pesan pertama");

        when(ticketRepo.findByIdAndUser(ticket.getId(), client)).thenReturn(Optional.of(ticket));
        when(replyRepo.findPublicByTicketId(ticket.getId())).thenReturn(List.of(publicReply));

        TicketDetailDTO result = ticketService.getTicketDetail(ticket.getId(), client);

        Assertions.assertEquals(1, result.getReplies().size());
        verify(replyRepo, never()).findByTicketId(ticket.getId());
    }

    @Test
    @Order(27)
    @DisplayName("27. Test Ticket Detail For Staff Includes Internal Note")
    void testTicketDetailForStaff() {
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        ticketService.getTicketDetail(ticket.getId(), staff);

        verify(replyRepo, times(1)).findByTicketId(ticket.getId());
        verify(replyRepo, never()).findPublicByTicketId(ticket.getId());
    }

    @Test
    @Order(28)
    @DisplayName("28. Test Client Cannot Open Ticket Of Another Client")
    void testClientCannotOpenOtherTicket() {
        User otherClient = createUser("Client Two", "client2@geobill.test", Role.USER);
        when(ticketRepo.findByIdAndUser(ticket.getId(), otherClient)).thenReturn(Optional.empty());

        UUID ticketId = ticket.getId();
        Assertions.assertThrows(NotFoundException.class,
                () -> ticketService.getTicketDetail(ticketId, otherClient));
    }

    @Test
    @Order(29)
    @DisplayName("29. Test Get Tickets By User With And Without Status")
    void testGetTicketsByUser() {
        when(ticketRepo.findByUser(eq(client), any(Pageable.class))).thenReturn(Page.empty());
        when(ticketRepo.findByUserAndStatus(eq(client), eq(TicketStatus.CLOSED), any(Pageable.class)))
                .thenReturn(Page.empty());

        ticketService.getTicketsByUser(client, null, PageRequest.of(0, 10));
        ticketService.getTicketsByUser(client, "closed", PageRequest.of(0, 10));

        verify(ticketRepo).findByUser(eq(client), any(Pageable.class));
        verify(ticketRepo).findByUserAndStatus(eq(client), eq(TicketStatus.CLOSED), any(Pageable.class));
    }

    @Test
    @Order(30)
    @DisplayName("30. Test Auto Close Only Touches Ticket Past Its Own Deadline")
    void testAutoCloseIdleTickets() {
        stubSaves();
        department.setAutoCloseDays(7);

        Ticket idle = new Ticket();
        idle.setId(UUID.randomUUID());
        idle.setTicketNumber("TKT-202609-000002");
        idle.setSubject("Idle");
        idle.setUser(client);
        idle.setDepartment(department);
        idle.setStatus(TicketStatus.AWAITING_CLIENT);
        idle.setLastReplyAt(new Date(System.currentTimeMillis() - 10 * ONE_DAY));

        Ticket stillWaiting = new Ticket();
        stillWaiting.setId(UUID.randomUUID());
        stillWaiting.setTicketNumber("TKT-202609-000003");
        stillWaiting.setSubject("Fresh");
        stillWaiting.setUser(client);
        stillWaiting.setDepartment(department);
        stillWaiting.setStatus(TicketStatus.AWAITING_CLIENT);
        stillWaiting.setLastReplyAt(new Date(System.currentTimeMillis() - 2 * ONE_DAY));

        when(departmentRepo.findByIsActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(department));
        when(ticketRepo.findIdleAwaitingClient(any(Date.class))).thenReturn(List.of(idle, stillWaiting));

        int closed = ticketService.autoCloseIdleTickets();

        Assertions.assertEquals(1, closed);
        Assertions.assertEquals(TicketStatus.CLOSED, idle.getStatus());
        Assertions.assertNotNull(idle.getClosedAt());
        Assertions.assertEquals(TicketStatus.AWAITING_CLIENT, stillWaiting.getStatus(),
                "Tiket yang belum melewati batas departemennya tidak ditutup");
    }

    @Test
    @Order(31)
    @DisplayName("31. Test Auto Close Skips When No Department Sets A Deadline")
    void testAutoCloseWithoutDeadline() {
        when(departmentRepo.findByIsActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(department));

        Assertions.assertEquals(0, ticketService.autoCloseIdleTickets());
        verify(ticketRepo, never()).findIdleAwaitingClient(any(Date.class));
    }
}
