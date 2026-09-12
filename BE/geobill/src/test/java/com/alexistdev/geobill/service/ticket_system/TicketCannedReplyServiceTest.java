package com.alexistdev.geobill.service.ticket_system;

import com.alexistdev.geobill.dto.ticket_system.TicketCannedReplyDTO;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.TicketCannedReply;
import com.alexistdev.geobill.models.entity.TicketDepartment;
import com.alexistdev.geobill.models.repository.ticket_system.TicketCannedReplyRepo;
import com.alexistdev.geobill.models.repository.ticket_system.TicketDepartmentRepo;
import com.alexistdev.geobill.request.ticket_system.TicketCannedReplyRequest;
import com.alexistdev.geobill.services.ticket_system.TicketCannedReplyService;
import com.alexistdev.geobill.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketCannedReplyServiceTest {

    @Mock private TicketCannedReplyRepo cannedReplyRepo;
    @Mock private TicketDepartmentRepo departmentRepo;
    @Mock private MessagesUtils messagesUtils;

    @InjectMocks private TicketCannedReplyService cannedReplyService;

    private TicketDepartment department;
    private TicketCannedReply cannedReply;

    @BeforeEach
    void setUp() {
        lenient().when(messagesUtils.getMessage(anyString())).thenReturn("message");
        lenient().when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("message");

        department = new TicketDepartment();
        department.setId(UUID.randomUUID());
        department.setName("Technical Support");
        department.setCode("TECH");

        cannedReply = new TicketCannedReply();
        cannedReply.setId(UUID.randomUUID());
        cannedReply.setDepartment(department);
        cannedReply.setTitle("Minta akses cPanel");
        cannedReply.setBody("Mohon kirimkan akses cPanel sementara");
        cannedReply.setSortOrder(1);
        cannedReply.setIsActive(Boolean.TRUE);
    }

    private TicketCannedReplyRequest request(String title, String departmentId) {
        TicketCannedReplyRequest request = new TicketCannedReplyRequest();
        request.setTitle(title);
        request.setBody("Isi template");
        request.setDepartmentId(departmentId);
        request.setSortOrder(2);
        request.setIsActive(Boolean.TRUE);
        return request;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Get All Canned Replies")
    void testGetAllCannedReplies() {
        Page<TicketCannedReply> page = new PageImpl<>(List.of(cannedReply));
        when(cannedReplyRepo.findByIsDeletedFalse(any(Pageable.class))).thenReturn(page);

        Page<TicketCannedReplyDTO> result = cannedReplyService.getAllCannedReplies(PageRequest.of(0, 10));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("Minta akses cPanel", result.getContent().get(0).getTitle());
        Assertions.assertEquals("Technical Support", result.getContent().get(0).getDepartment());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Get Available For Department")
    void testGetAvailableForDepartment() {
        TicketCannedReply global = new TicketCannedReply();
        global.setId(UUID.randomUUID());
        global.setTitle("Sapaan global");
        global.setBody("Halo");
        global.setSortOrder(0);

        when(cannedReplyRepo.findAvailableForDepartment(department.getId()))
                .thenReturn(List.of(global, cannedReply));

        List<TicketCannedReplyDTO> result = cannedReplyService.getAvailableForDepartment(department.getId());

        Assertions.assertEquals(2, result.size());
        Assertions.assertNull(result.get(0).getDepartmentId(), "Template global tidak punya departemen");
        Assertions.assertEquals(department.getId().toString(), result.get(1).getDepartmentId());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Get Active Canned Replies")
    void testGetActiveCannedReplies() {
        when(cannedReplyRepo.findByIsActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(cannedReply));

        List<TicketCannedReplyDTO> result = cannedReplyService.getActiveCannedReplies();

        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.get(0).isActive());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Add Canned Reply For One Department")
    void testAddCannedReply() {
        when(cannedReplyRepo.existsByTitle("Konfirmasi pembayaran")).thenReturn(false);
        when(departmentRepo.findById(department.getId())).thenReturn(Optional.of(department));
        when(cannedReplyRepo.save(any(TicketCannedReply.class))).thenAnswer(invocation -> {
            TicketCannedReply saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        TicketCannedReplyDTO result = cannedReplyService
                .addCannedReply(request("Konfirmasi pembayaran", department.getId().toString()));

        Assertions.assertEquals("Konfirmasi pembayaran", result.getTitle());
        Assertions.assertEquals("Technical Support", result.getDepartment());
        Assertions.assertEquals(2, result.getSortOrder());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Add Canned Reply Without Department Is Global")
    void testAddGlobalCannedReply() {
        when(cannedReplyRepo.existsByTitle("Sapaan global")).thenReturn(false);
        when(cannedReplyRepo.save(any(TicketCannedReply.class))).thenAnswer(invocation -> {
            TicketCannedReply saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        TicketCannedReplyDTO result = cannedReplyService.addCannedReply(request("Sapaan global", null));

        Assertions.assertNull(result.getDepartmentId());
        Assertions.assertNull(result.getDepartment());
        verify(departmentRepo, never()).findById(any(UUID.class));
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Add Canned Reply Rejects Duplicate Title")
    void testAddCannedReplyRejectsDuplicate() {
        when(cannedReplyRepo.existsByTitle("Minta akses cPanel")).thenReturn(true);

        TicketCannedReplyRequest request = request("Minta akses cPanel", null);
        Assertions.assertThrows(DuplicateException.class, () -> cannedReplyService.addCannedReply(request));
        verify(cannedReplyRepo, never()).save(any(TicketCannedReply.class));
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Add Canned Reply With Unknown Department")
    void testAddCannedReplyUnknownDepartment() {
        UUID unknownId = UUID.randomUUID();
        when(cannedReplyRepo.existsByTitle("Template baru")).thenReturn(false);
        when(departmentRepo.findById(unknownId)).thenReturn(Optional.empty());

        TicketCannedReplyRequest request = request("Template baru", unknownId.toString());
        Assertions.assertThrows(NotFoundException.class, () -> cannedReplyService.addCannedReply(request));
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Update Canned Reply Keeps Its Own Title")
    void testUpdateCannedReply() {
        when(cannedReplyRepo.findById(cannedReply.getId())).thenReturn(Optional.of(cannedReply));
        when(departmentRepo.findById(department.getId())).thenReturn(Optional.of(department));
        when(cannedReplyRepo.save(any(TicketCannedReply.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketCannedReplyRequest request = request("Minta akses cPanel", department.getId().toString());
        request.setIsActive(Boolean.FALSE);
        TicketCannedReplyDTO result = cannedReplyService.updateCannedReply(cannedReply.getId(), request);

        Assertions.assertFalse(result.isActive());
        verify(cannedReplyRepo, never()).existsByTitle(anyString());
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Update Canned Reply Rejects Title Of Another Template")
    void testUpdateCannedReplyRejectsOtherTitle() {
        when(cannedReplyRepo.findById(cannedReply.getId())).thenReturn(Optional.of(cannedReply));
        when(cannedReplyRepo.existsByTitle("Sapaan global")).thenReturn(true);

        UUID id = cannedReply.getId();
        TicketCannedReplyRequest request = request("Sapaan global", null);
        Assertions.assertThrows(DuplicateException.class, () -> cannedReplyService.updateCannedReply(id, request));
    }

    @Test
    @Order(10)
    @DisplayName("10. Test Delete Canned Reply")
    void testDeleteCannedReply() {
        when(cannedReplyRepo.findById(cannedReply.getId())).thenReturn(Optional.of(cannedReply));

        cannedReplyService.deleteCannedReply(cannedReply.getId());

        verify(cannedReplyRepo, times(1)).delete(cannedReply);
    }

    @Test
    @Order(11)
    @DisplayName("11. Test Get Unknown Canned Reply")
    void testGetUnknownCannedReply() {
        UUID unknownId = UUID.randomUUID();
        when(cannedReplyRepo.findById(unknownId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> cannedReplyService.getCannedReplyById(unknownId));
    }
}
