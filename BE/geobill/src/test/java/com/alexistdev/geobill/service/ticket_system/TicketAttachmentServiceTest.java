package com.alexistdev.geobill.service.ticket_system;

import com.alexistdev.geobill.dto.ticket_system.TicketAttachmentDTO;
import com.alexistdev.geobill.exceptions.ConflictException;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.*;
import com.alexistdev.geobill.models.repository.ticket_system.TicketAttachmentRepo;
import com.alexistdev.geobill.models.repository.ticket_system.TicketReplyRepo;
import com.alexistdev.geobill.models.repository.ticket_system.TicketRepo;
import com.alexistdev.geobill.services.ticket_system.TicketAttachmentService;
import com.alexistdev.geobill.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketAttachmentServiceTest {

    private static final long QUOTA_BYTES = 1024L;

    @Mock private TicketAttachmentRepo attachmentRepo;
    @Mock private TicketReplyRepo replyRepo;
    @Mock private TicketRepo ticketRepo;
    @Mock private MessagesUtils messagesUtils;

    private TicketAttachmentService attachmentService;

    private Ticket ticket;
    private TicketReply reply;
    private User uploader;

    @BeforeEach
    void setUp() {
        lenient().when(messagesUtils.getMessage(anyString())).thenReturn("message");
        lenient().when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("message");

        // Kuota dipasang lewat konstruktor, bukan @InjectMocks, karena nilainya berasal dari properti.
        attachmentService = new TicketAttachmentService(attachmentRepo, replyRepo, ticketRepo, messagesUtils,
                QUOTA_BYTES);

        uploader = new User();
        uploader.setId(UUID.randomUUID());
        uploader.setFullName("Client One");

        TicketDepartment department = new TicketDepartment();
        department.setId(UUID.randomUUID());
        department.setName("Technical Support");

        ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setTicketNumber("TKT-202609-000001");
        ticket.setSubject("Website down");
        ticket.setUser(uploader);
        ticket.setDepartment(department);

        reply = new TicketReply();
        reply.setId(UUID.randomUUID());
        reply.setTicket(ticket);
        reply.setAuthorType(TicketAuthorType.CLIENT);
        reply.setMessage("Terlampir tangkapan layar");
    }

    private TicketAttachment attachment(String originalName, String storedName, long fileSize) {
        TicketAttachment attachment = new TicketAttachment();
        attachment.setId(UUID.randomUUID());
        attachment.setTicket(ticket);
        attachment.setReply(reply);
        attachment.setOriginalName(originalName);
        attachment.setStoredName(storedName);
        attachment.setStoragePath("/var/geobill/tickets/" + storedName);
        attachment.setMimeType("image/png");
        attachment.setFileSize(fileSize);
        attachment.setUploadedBy(uploader);
        return attachment;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Add Attachment")
    void testAddAttachment() {
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(replyRepo.findById(reply.getId())).thenReturn(Optional.of(reply));
        when(attachmentRepo.sumFileSizeByTicketId(ticket.getId())).thenReturn(0L);
        when(attachmentRepo.save(any(TicketAttachment.class))).thenAnswer(invocation -> {
            TicketAttachment saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        TicketAttachmentDTO result = attachmentService.addAttachment(ticket.getId(), reply.getId(), "error.png",
                "stored-1.png", "/var/geobill/tickets/stored-1.png", "image/png", 200L, uploader);

        Assertions.assertEquals("error.png", result.getName());
        Assertions.assertEquals(200L, result.getFileSize());
        Assertions.assertEquals("Client One", result.getUploadedBy());

        ArgumentCaptor<TicketAttachment> captor = ArgumentCaptor.forClass(TicketAttachment.class);
        verify(attachmentRepo).save(captor.capture());
        Assertions.assertEquals("stored-1.png", captor.getValue().getStoredName());
        Assertions.assertEquals(ticket, captor.getValue().getTicket());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Add Attachment Without Reply")
    void testAddAttachmentWithoutReply() {
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(attachmentRepo.sumFileSizeByTicketId(ticket.getId())).thenReturn(0L);
        when(attachmentRepo.save(any(TicketAttachment.class))).thenAnswer(invocation -> {
            TicketAttachment saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        TicketAttachmentDTO result = attachmentService.addAttachment(ticket.getId(), null, "log.txt", "stored-2.txt",
                "/var/geobill/tickets/stored-2.txt", "text/plain", 100L, uploader);

        Assertions.assertNull(result.getReplyId());
        Assertions.assertEquals(ticket.getId().toString(), result.getTicketId());
        verify(replyRepo, never()).findById(any(UUID.class));
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Add Attachment Beyond Quota Is Rejected")
    void testAddAttachmentBeyondQuota() {
        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(attachmentRepo.sumFileSizeByTicketId(ticket.getId())).thenReturn(1000L);

        UUID ticketId = ticket.getId();
        Assertions.assertThrows(ConflictException.class, () -> attachmentService.addAttachment(ticketId, null,
                "big.png", "stored-3.png", "/var/geobill/tickets/stored-3.png", "image/png", 100L, uploader));
        verify(attachmentRepo, never()).save(any(TicketAttachment.class));
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Add Attachment To Reply Of Another Ticket Is Rejected")
    void testAddAttachmentToOtherTicketReply() {
        Ticket otherTicket = new Ticket();
        otherTicket.setId(UUID.randomUUID());
        reply.setTicket(otherTicket);

        when(ticketRepo.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(replyRepo.findById(reply.getId())).thenReturn(Optional.of(reply));

        UUID ticketId = ticket.getId();
        UUID replyId = reply.getId();
        Assertions.assertThrows(ConflictException.class, () -> attachmentService.addAttachment(ticketId, replyId,
                "error.png", "stored-4.png", "/var/geobill/tickets/stored-4.png", "image/png", 10L, uploader));
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Add Attachment To Unknown Ticket")
    void testAddAttachmentToUnknownTicket() {
        UUID unknownId = UUID.randomUUID();
        when(ticketRepo.findById(unknownId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> attachmentService.addAttachment(unknownId, null,
                "error.png", "stored-5.png", "/var/geobill/tickets/stored-5.png", "image/png", 10L, uploader));
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Get Attachments By Ticket And Reply")
    void testGetAttachments() {
        when(attachmentRepo.findByTicket_IdOrderByCreatedDateAsc(ticket.getId()))
                .thenReturn(List.of(attachment("error.png", "stored-1.png", 200L)));
        when(attachmentRepo.findByReply_Id(reply.getId()))
                .thenReturn(List.of(attachment("log.txt", "stored-2.txt", 100L)));

        Assertions.assertEquals(1, attachmentService.getAttachmentsByTicket(ticket.getId()).size());
        Assertions.assertEquals("log.txt", attachmentService.getAttachmentsByReply(reply.getId()).get(0).getName());
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Find By Stored Name")
    void testFindByStoredName() {
        TicketAttachment stored = attachment("error.png", "stored-1.png", 200L);
        when(attachmentRepo.findByStoredName("stored-1.png")).thenReturn(Optional.of(stored));
        when(attachmentRepo.findByStoredName("error.png")).thenReturn(Optional.empty());

        Assertions.assertEquals("error.png", attachmentService.findByStoredName("stored-1.png").getOriginalName());
        Assertions.assertThrows(NotFoundException.class, () -> attachmentService.findByStoredName("error.png"));
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Delete Attachment")
    void testDeleteAttachment() {
        TicketAttachment stored = attachment("error.png", "stored-1.png", 200L);
        when(attachmentRepo.findById(stored.getId())).thenReturn(Optional.of(stored));

        attachmentService.deleteAttachment(stored.getId());

        verify(attachmentRepo, times(1)).delete(stored);
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Delete Unknown Attachment")
    void testDeleteUnknownAttachment() {
        UUID unknownId = UUID.randomUUID();
        when(attachmentRepo.findById(unknownId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> attachmentService.deleteAttachment(unknownId));
        verify(attachmentRepo, never()).delete(any(TicketAttachment.class));
    }

    @Test
    @Order(10)
    @DisplayName("10. Test Quota Report")
    void testQuotaReport() {
        when(attachmentRepo.sumFileSizeByTicketId(ticket.getId())).thenReturn(300L);

        Assertions.assertEquals(300L, attachmentService.getUsedQuota(ticket.getId()));
        Assertions.assertEquals(QUOTA_BYTES, attachmentService.getQuotaPerTicket());
    }
}
