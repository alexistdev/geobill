package com.alexistdev.geobill.service.ticket_system;

import com.alexistdev.geobill.dto.ticket_system.TicketDepartmentDTO;
import com.alexistdev.geobill.dto.ticket_system.TicketStaffDTO;
import com.alexistdev.geobill.exceptions.ConflictException;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.exceptions.NotFoundException;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.TicketDepartment;
import com.alexistdev.geobill.models.entity.TicketDepartmentStaff;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.UserRepo;
import com.alexistdev.geobill.models.repository.ticket_system.TicketDepartmentRepo;
import com.alexistdev.geobill.models.repository.ticket_system.TicketDepartmentStaffRepo;
import com.alexistdev.geobill.models.repository.ticket_system.TicketRepo;
import com.alexistdev.geobill.request.ticket_system.TicketDepartmentRequest;
import com.alexistdev.geobill.request.ticket_system.TicketStaffRequest;
import com.alexistdev.geobill.services.ticket_system.TicketDepartmentService;
import com.alexistdev.geobill.utils.MessagesUtils;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketDepartmentServiceTest {

    @Mock private TicketDepartmentRepo departmentRepo;
    @Mock private TicketDepartmentStaffRepo departmentStaffRepo;
    @Mock private TicketRepo ticketRepo;
    @Mock private UserRepo userRepo;
    @Mock private MessagesUtils messagesUtils;

    @InjectMocks private TicketDepartmentService departmentService;

    private TicketDepartment department;
    private User staff;

    @BeforeEach
    void setUp() {
        lenient().when(messagesUtils.getMessage(anyString())).thenReturn("message");
        lenient().when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("message");

        department = new TicketDepartment();
        department.setId(UUID.randomUUID());
        department.setName("Technical Support");
        department.setCode("TECH");
        department.setSortOrder(1);
        department.setIsActive(Boolean.TRUE);

        staff = new User();
        staff.setId(UUID.randomUUID());
        staff.setFullName("Staff One");
        staff.setEmail("staff1@geobill.test");
        staff.setRole(Role.STAFF);
    }

    private TicketDepartmentRequest request(String name, String code) {
        TicketDepartmentRequest request = new TicketDepartmentRequest();
        request.setName(name);
        request.setCode(code);
        request.setEmail("support@geobill.test");
        request.setSortOrder(2);
        request.setIsActive(Boolean.TRUE);
        request.setAutoCloseDays(7);
        return request;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Get All Departments")
    void testGetAllDepartments() {
        Page<TicketDepartment> page = new PageImpl<>(List.of(department));
        when(departmentRepo.findByIsDeletedFalse(any(Pageable.class))).thenReturn(page);

        Page<TicketDepartmentDTO> result = departmentService.getAllDepartments(PageRequest.of(0, 10));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("Technical Support", result.getContent().get(0).getName());
        Assertions.assertTrue(result.getContent().get(0).isActive());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Get Departments By Filter Lowercases The Keyword")
    void testGetDepartmentsByFilter() {
        when(departmentRepo.findByFilter(eq("tech"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(department)));

        Page<TicketDepartmentDTO> result = departmentService
                .getAllDepartmentsByFilter(PageRequest.of(0, 10), "TECH");

        Assertions.assertEquals(1, result.getTotalElements());
        verify(departmentRepo).findByFilter(eq("tech"), any(Pageable.class));
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Get Active Departments For Dropdown")
    void testGetActiveDepartments() {
        when(departmentRepo.findByIsActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(department));

        List<TicketDepartmentDTO> result = departmentService.getActiveDepartments();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("TECH", result.get(0).getCode());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Add Department")
    void testAddDepartment() {
        when(departmentRepo.existsByName("Billing")).thenReturn(false);
        when(departmentRepo.existsByCode("BILLING")).thenReturn(false);
        when(departmentRepo.save(any(TicketDepartment.class))).thenAnswer(invocation -> {
            TicketDepartment saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        TicketDepartmentDTO result = departmentService.addDepartment(request("Billing", "BILLING"));

        Assertions.assertEquals("Billing", result.getName());
        Assertions.assertEquals("BILLING", result.getCode());
        Assertions.assertEquals(2, result.getSortOrder());
        Assertions.assertEquals(7, result.getAutoCloseDays());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Add Department Rejects Duplicate Name And Code")
    void testAddDepartmentRejectsDuplicate() {
        when(departmentRepo.existsByName("Technical Support")).thenReturn(true);
        TicketDepartmentRequest duplicateName = request("Technical Support", "OTHER");
        Assertions.assertThrows(DuplicateException.class, () -> departmentService.addDepartment(duplicateName));

        when(departmentRepo.existsByName("Other")).thenReturn(false);
        when(departmentRepo.existsByCode("TECH")).thenReturn(true);
        TicketDepartmentRequest duplicateCode = request("Other", "TECH");
        Assertions.assertThrows(DuplicateException.class, () -> departmentService.addDepartment(duplicateCode));

        verify(departmentRepo, never()).save(any(TicketDepartment.class));
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Update Department Keeps Its Own Name")
    void testUpdateDepartment() {
        when(departmentRepo.findById(department.getId())).thenReturn(Optional.of(department));
        when(departmentRepo.save(any(TicketDepartment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketDepartmentRequest request = request("Technical Support", "TECH");
        request.setIsActive(Boolean.FALSE);
        TicketDepartmentDTO result = departmentService.updateDepartment(department.getId(), request);

        Assertions.assertEquals("Technical Support", result.getName());
        Assertions.assertFalse(result.isActive());
        verify(departmentRepo, never()).existsByName(anyString());
        verify(departmentRepo, never()).existsByCode(anyString());
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Update Department Rejects Name Of Another Department")
    void testUpdateDepartmentRejectsOtherName() {
        when(departmentRepo.findById(department.getId())).thenReturn(Optional.of(department));
        when(departmentRepo.existsByName("Billing")).thenReturn(true);

        UUID departmentId = department.getId();
        TicketDepartmentRequest request = request("Billing", "TECH");
        Assertions.assertThrows(DuplicateException.class,
                () -> departmentService.updateDepartment(departmentId, request));
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Find Unknown Department")
    void testFindUnknownDepartment() {
        UUID unknownId = UUID.randomUUID();
        when(departmentRepo.findById(unknownId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> departmentService.getDepartmentById(unknownId));
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Delete Department Still Holding Ticket Is Rejected")
    void testDeleteDepartmentWithTicket() {
        when(departmentRepo.findById(department.getId())).thenReturn(Optional.of(department));
        when(ticketRepo.countByDepartment_Id(department.getId())).thenReturn(3L);

        UUID departmentId = department.getId();
        Assertions.assertThrows(ConflictException.class, () -> departmentService.deleteDepartment(departmentId));
        verify(departmentRepo, never()).delete(any(TicketDepartment.class));
    }

    @Test
    @Order(10)
    @DisplayName("10. Test Delete Department Also Releases Its Staff")
    void testDeleteDepartment() {
        TicketDepartmentStaff link = new TicketDepartmentStaff();
        link.setId(UUID.randomUUID());
        link.setDepartment(department);
        link.setUser(staff);

        when(departmentRepo.findById(department.getId())).thenReturn(Optional.of(department));
        when(ticketRepo.countByDepartment_Id(department.getId())).thenReturn(0L);
        when(departmentStaffRepo.findByDepartment_IdOrderByUser_FullNameAsc(department.getId()))
                .thenReturn(List.of(link));

        departmentService.deleteDepartment(department.getId());

        verify(departmentStaffRepo, times(1)).delete(link);
        verify(departmentRepo, times(1)).delete(department);
    }

    @Test
    @Order(11)
    @DisplayName("11. Test Assign Staff")
    void testAssignStaff() {
        when(departmentRepo.findById(department.getId())).thenReturn(Optional.of(department));
        when(userRepo.findById(staff.getId())).thenReturn(Optional.of(staff));
        when(departmentStaffRepo.existsByDepartment_IdAndUser_Id(department.getId(), staff.getId())).thenReturn(false);
        when(departmentStaffRepo.save(any(TicketDepartmentStaff.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TicketStaffRequest request = new TicketStaffRequest();
        request.setDepartmentId(department.getId().toString());
        request.setUserId(staff.getId().toString());
        request.setSupervisor(true);

        TicketStaffDTO result = departmentService.assignStaff(request);

        Assertions.assertEquals("Staff One", result.getFullName());
        Assertions.assertEquals("STAFF", result.getRole());
        Assertions.assertTrue(result.isSupervisor());

        ArgumentCaptor<TicketDepartmentStaff> captor = ArgumentCaptor.forClass(TicketDepartmentStaff.class);
        verify(departmentStaffRepo).save(captor.capture());
        Assertions.assertEquals(department, captor.getValue().getDepartment());
    }

    @Test
    @Order(12)
    @DisplayName("12. Test Assign Staff Rejects Client And Duplicate Assignment")
    void testAssignStaffRejections() {
        User client = new User();
        client.setId(UUID.randomUUID());
        client.setFullName("Client One");
        client.setRole(Role.USER);

        when(departmentRepo.findById(department.getId())).thenReturn(Optional.of(department));
        when(userRepo.findById(client.getId())).thenReturn(Optional.of(client));

        TicketStaffRequest clientRequest = new TicketStaffRequest();
        clientRequest.setDepartmentId(department.getId().toString());
        clientRequest.setUserId(client.getId().toString());
        Assertions.assertThrows(ConflictException.class, () -> departmentService.assignStaff(clientRequest));

        when(userRepo.findById(staff.getId())).thenReturn(Optional.of(staff));
        when(departmentStaffRepo.existsByDepartment_IdAndUser_Id(department.getId(), staff.getId())).thenReturn(true);

        TicketStaffRequest duplicateRequest = new TicketStaffRequest();
        duplicateRequest.setDepartmentId(department.getId().toString());
        duplicateRequest.setUserId(staff.getId().toString());
        Assertions.assertThrows(ConflictException.class, () -> departmentService.assignStaff(duplicateRequest));

        verify(departmentStaffRepo, never()).save(any(TicketDepartmentStaff.class));
    }

    @Test
    @Order(13)
    @DisplayName("13. Test Remove Staff")
    void testRemoveStaff() {
        TicketDepartmentStaff link = new TicketDepartmentStaff();
        link.setId(UUID.randomUUID());
        link.setDepartment(department);
        link.setUser(staff);

        when(departmentStaffRepo.findByDepartment_IdAndUser_Id(department.getId(), staff.getId()))
                .thenReturn(Optional.of(link));

        departmentService.removeStaff(department.getId(), staff.getId());

        verify(departmentStaffRepo, times(1)).delete(link);
    }

    @Test
    @Order(14)
    @DisplayName("14. Test Remove Staff That Was Never Assigned")
    void testRemoveStaffNotAssigned() {
        UUID departmentId = department.getId();
        UUID staffId = staff.getId();
        when(departmentStaffRepo.findByDepartment_IdAndUser_Id(departmentId, staffId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> departmentService.removeStaff(departmentId, staffId));
    }

    @Test
    @Order(15)
    @DisplayName("15. Test Get Staff By Department For Assigned To Dropdown")
    void testGetStaffByDepartment() {
        TicketDepartmentStaff link = new TicketDepartmentStaff();
        link.setId(UUID.randomUUID());
        link.setDepartment(department);
        link.setUser(staff);
        link.setIsSupervisor(Boolean.FALSE);

        when(departmentRepo.findById(department.getId())).thenReturn(Optional.of(department));
        when(departmentStaffRepo.findByDepartment_IdOrderByUser_FullNameAsc(department.getId()))
                .thenReturn(List.of(link));

        List<TicketStaffDTO> result = departmentService.getStaffByDepartment(department.getId());

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("staff1@geobill.test", result.get(0).getEmail());
        Assertions.assertFalse(result.get(0).isSupervisor());
    }
}
