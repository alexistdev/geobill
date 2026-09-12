package com.alexistdev.geobill.models.repository.ticket_system;

import com.alexistdev.geobill.models.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class TicketAttachmentRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketAttachmentRepo attachmentRepo;

    private Ticket ticket;
    private Ticket otherTicket;
    private TicketReply reply;
    private User client;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser, null,
                new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        client = new User();
        client.setFullName("Client One");
        client.setEmail("client1@geobill.test");
        client.setPassword("password");
        client.setRole(Role.USER);
        entityManager.persist(client);

        TicketDepartment department = new TicketDepartment();
        department.setName("Technical Support");
        department.setCode("TECH");
        entityManager.persist(department);

        ticket = createTicket("TKT-202609-000001", department);
        otherTicket = createTicket("TKT-202609-000002", department);

        reply = new TicketReply();
        reply.setTicket(ticket);
        reply.setUser(client);
        reply.setAuthorType(TicketAuthorType.CLIENT);
        reply.setMessage("Terlampir tangkapan layarnya");
        entityManager.persist(reply);

        entityManager.flush();
    }

    private Ticket createTicket(String number, TicketDepartment department) {
        Ticket newTicket = new Ticket();
        newTicket.setTicketNumber(number);
        newTicket.setSubject("Subject " + number);
        newTicket.setUser(client);
        newTicket.setDepartment(department);
        return entityManager.persist(newTicket);
    }

    private TicketAttachment createAttachment(Ticket target, TicketReply owner, String originalName,
                                              String storedName, String mimeType, long fileSize) {
        TicketAttachment attachment = new TicketAttachment();
        attachment.setTicket(target);
        attachment.setReply(owner);
        attachment.setOriginalName(originalName);
        attachment.setStoredName(storedName);
        attachment.setStoragePath("/var/geobill/tickets/" + storedName);
        attachment.setMimeType(mimeType);
        attachment.setFileSize(fileSize);
        attachment.setUploadedBy(client);
        return attachment;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Save Attachment")
    void testSaveAttachment() {
        TicketAttachment saved = attachmentRepo
                .save(createAttachment(ticket, reply, "error.png", "stored-1.png", "image/png", 2048L));

        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(ticket.getId(), saved.getTicket().getId());
        Assertions.assertEquals(reply.getId(), saved.getReply().getId());
        Assertions.assertEquals("error.png", saved.getOriginalName());
        Assertions.assertEquals(2048L, saved.getFileSize());
        Assertions.assertFalse(saved.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Find By Ticket")
    void testFindByTicket() {
        entityManager.persist(createAttachment(ticket, reply, "error.png", "stored-1.png", "image/png", 2048L));
        entityManager.persist(createAttachment(ticket, null, "log.txt", "stored-2.txt", "text/plain", 512L));
        entityManager.persist(createAttachment(otherTicket, null, "lain.png", "stored-3.png", "image/png", 100L));
        entityManager.flush();
        entityManager.clear();

        List<TicketAttachment> attachments = attachmentRepo.findByTicket_IdOrderByCreatedDateAsc(ticket.getId());

        Assertions.assertEquals(2, attachments.size(),
                "Lampiran tiket bisa dilist tanpa join karena ticket_id selalu diisi");
        Assertions.assertEquals(2, attachmentRepo.countByTicket_Id(ticket.getId()));
        Assertions.assertEquals(1, attachmentRepo.countByTicket_Id(otherTicket.getId()));
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Find By Reply")
    void testFindByReply() {
        entityManager.persist(createAttachment(ticket, reply, "error.png", "stored-1.png", "image/png", 2048L));
        entityManager.persist(createAttachment(ticket, null, "log.txt", "stored-2.txt", "text/plain", 512L));
        entityManager.flush();
        entityManager.clear();

        List<TicketAttachment> onReply = attachmentRepo.findByReply_Id(reply.getId());

        Assertions.assertEquals(1, onReply.size());
        Assertions.assertEquals("error.png", onReply.get(0).getOriginalName());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Find By Stored Name")
    void testFindByStoredName() {
        entityManager.persist(createAttachment(ticket, reply, "error.png", "stored-1.png", "image/png", 2048L));
        entityManager.flush();
        entityManager.clear();

        Assertions.assertTrue(attachmentRepo.findByStoredName("stored-1.png").isPresent());
        Assertions.assertEquals("error.png", attachmentRepo.findByStoredName("stored-1.png").orElseThrow()
                .getOriginalName());
        Assertions.assertTrue(attachmentRepo.findByStoredName("error.png").isEmpty(),
                "Nama asli bukan kunci penyimpanan");
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Total Ukuran Lampiran Satu Tiket")
    void testSumFileSize() {
        entityManager.persist(createAttachment(ticket, reply, "error.png", "stored-1.png", "image/png", 2048L));
        entityManager.persist(createAttachment(ticket, null, "log.txt", "stored-2.txt", "text/plain", 512L));
        entityManager.flush();
        entityManager.clear();

        Assertions.assertEquals(2560L, attachmentRepo.sumFileSizeByTicketId(ticket.getId()));
        Assertions.assertEquals(0L, attachmentRepo.sumFileSizeByTicketId(UUID.randomUUID()),
                "Tiket tanpa lampiran harus mengembalikan nol, bukan null");
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Delete Attachment")
    void testDeleteAttachment() {
        TicketAttachment attachment = createAttachment(ticket, reply, "error.png", "stored-1.png", "image/png", 2048L);
        entityManager.persist(attachment);
        entityManager.flush();

        attachmentRepo.delete(attachment);
        entityManager.flush();
        entityManager.clear();

        Assertions.assertTrue(attachmentRepo.findByStoredName("stored-1.png").isEmpty());
        Assertions.assertEquals(0L, attachmentRepo.sumFileSizeByTicketId(ticket.getId()));

        Object isDeleted = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_ticket_attachments WHERE uuid = ?1")
                .setParameter(1, attachment.getId())
                .getSingleResult();

        Assertions.assertTrue(isDeleted instanceof Boolean ? (Boolean) isDeleted : ((Number) isDeleted).intValue() == 1,
                "Attachment should be soft-deleted in the database");
    }
}
