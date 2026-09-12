package com.alexistdev.geobill.services.ticket_system;

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
import com.alexistdev.geobill.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TicketDepartmentService {

    private final TicketDepartmentRepo departmentRepo;
    private final TicketDepartmentStaffRepo departmentStaffRepo;
    private final TicketRepo ticketRepo;
    private final UserRepo userRepo;
    private final MessagesUtils messagesUtils;

    public TicketDepartmentService(TicketDepartmentRepo departmentRepo,
                                   TicketDepartmentStaffRepo departmentStaffRepo,
                                   TicketRepo ticketRepo,
                                   UserRepo userRepo,
                                   MessagesUtils messagesUtils) {
        this.departmentRepo = departmentRepo;
        this.departmentStaffRepo = departmentStaffRepo;
        this.ticketRepo = ticketRepo;
        this.userRepo = userRepo;
        this.messagesUtils = messagesUtils;
    }

    public Page<TicketDepartmentDTO> getAllDepartments(Pageable pageable) {
        Page<TicketDepartment> result = departmentRepo.findByIsDeletedFalse(pageable);
        return toPage(result, pageable);
    }

    public Page<TicketDepartmentDTO> getAllDepartmentsByFilter(Pageable pageable, String keyword) {
        Page<TicketDepartment> result = departmentRepo.findByFilter(keyword.toLowerCase(), pageable);
        return toPage(result, pageable);
    }

    /** Isi dropdown Department; departemen non-aktif tidak boleh dipilih untuk tiket baru. */
    public List<TicketDepartmentDTO> getActiveDepartments() {
        return departmentRepo.findByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<TicketStaffDTO> getStaffByDepartment(UUID departmentId) {
        findEntityById(departmentId);
        return departmentStaffRepo.findByDepartment_IdOrderByUser_FullNameAsc(departmentId).stream()
                .map(this::convertToStaffDTO)
                .toList();
    }

    public TicketDepartment findEntityById(UUID id) {
        return departmentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("ticketdepartmentservice.department_not_found", id.toString())));
    }

    public TicketDepartmentDTO getDepartmentById(UUID id) {
        return convertToDTO(findEntityById(id));
    }

    @Transactional
    public TicketDepartmentDTO addDepartment(TicketDepartmentRequest request) {
        if (departmentRepo.existsByName(request.getName())) {
            throw new DuplicateException(
                    messagesUtils.getMessage("ticketdepartmentservice.name_exist", request.getName()));
        }
        if (departmentRepo.existsByCode(request.getCode())) {
            throw new DuplicateException(
                    messagesUtils.getMessage("ticketdepartmentservice.code_exist", request.getCode()));
        }

        TicketDepartment department = new TicketDepartment();
        applyRequest(department, request);
        return convertToDTO(departmentRepo.save(department));
    }

    @Transactional
    public TicketDepartmentDTO updateDepartment(UUID id, TicketDepartmentRequest request) {
        TicketDepartment department = findEntityById(id);

        if (!department.getName().equals(request.getName()) && departmentRepo.existsByName(request.getName())) {
            throw new DuplicateException(
                    messagesUtils.getMessage("ticketdepartmentservice.name_exist", request.getName()));
        }
        if (!department.getCode().equals(request.getCode()) && departmentRepo.existsByCode(request.getCode())) {
            throw new DuplicateException(
                    messagesUtils.getMessage("ticketdepartmentservice.code_exist", request.getCode()));
        }

        applyRequest(department, request);
        return convertToDTO(departmentRepo.save(department));
    }

    /**
     * Departemen yang masih memiliki tiket tidak boleh dihapus, karena tiketnya akan
     * kehilangan relasi wajibnya. Non-aktifkan saja supaya tidak bisa dipilih lagi.
     */
    @Transactional
    public void deleteDepartment(UUID id) {
        TicketDepartment department = findEntityById(id);

        if (ticketRepo.countByDepartment_Id(id) > 0) {
            throw new ConflictException(messagesUtils.getMessage("ticketdepartmentservice.department_has_ticket"));
        }

        departmentStaffRepo.findByDepartment_IdOrderByUser_FullNameAsc(id)
                .forEach(departmentStaffRepo::delete);
        departmentRepo.delete(department);
    }

    @Transactional
    public TicketStaffDTO assignStaff(TicketStaffRequest request) {
        TicketDepartment department = findEntityById(UUID.fromString(request.getDepartmentId()));
        User user = findStaffUser(UUID.fromString(request.getUserId()));

        if (departmentStaffRepo.existsByDepartment_IdAndUser_Id(department.getId(), user.getId())) {
            throw new ConflictException(
                    messagesUtils.getMessage("ticketdepartmentservice.staff_already_assigned"));
        }

        TicketDepartmentStaff departmentStaff = new TicketDepartmentStaff();
        departmentStaff.setDepartment(department);
        departmentStaff.setUser(user);
        departmentStaff.setIsSupervisor(request.isSupervisor());
        return convertToStaffDTO(departmentStaffRepo.save(departmentStaff));
    }

    @Transactional
    public void removeStaff(UUID departmentId, UUID userId) {
        TicketDepartmentStaff departmentStaff = departmentStaffRepo
                .findByDepartment_IdAndUser_Id(departmentId, userId)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("ticketdepartmentservice.staff_not_assigned")));
        departmentStaffRepo.delete(departmentStaff);
    }

    private User findStaffUser(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("ticketdepartmentservice.user_not_found")));

        if (user.getRole() != Role.STAFF && user.getRole() != Role.ADMIN) {
            throw new ConflictException(messagesUtils.getMessage("ticketservice.staff_not_allowed"));
        }
        return user;
    }

    private void applyRequest(TicketDepartment department, TicketDepartmentRequest request) {
        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setEmail(request.getEmail());
        department.setDescription(request.getDescription());
        department.setAutoCloseDays(request.getAutoCloseDays());
        if (request.getSortOrder() != null) {
            department.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            department.setIsActive(request.getIsActive());
        }
    }

    private Page<TicketDepartmentDTO> toPage(Page<TicketDepartment> result, Pageable pageable) {
        List<TicketDepartmentDTO> content = result.getContent().stream()
                .map(this::convertToDTO)
                .toList();
        return new PageImpl<>(content, pageable, result.getTotalElements());
    }

    private TicketDepartmentDTO convertToDTO(TicketDepartment department) {
        TicketDepartmentDTO dto = new TicketDepartmentDTO();
        dto.setId(department.getId().toString());
        dto.setName(department.getName());
        dto.setCode(department.getCode());
        dto.setEmail(department.getEmail());
        dto.setDescription(department.getDescription());
        dto.setSortOrder(department.getSortOrder());
        dto.setActive(Boolean.TRUE.equals(department.getIsActive()));
        dto.setAutoCloseDays(department.getAutoCloseDays());
        return dto;
    }

    private TicketStaffDTO convertToStaffDTO(TicketDepartmentStaff departmentStaff) {
        User user = departmentStaff.getUser();
        TicketStaffDTO dto = new TicketStaffDTO();
        dto.setId(user.getId().toString());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() == null ? null : user.getRole().name());
        dto.setSupervisor(Boolean.TRUE.equals(departmentStaff.getIsSupervisor()));
        return dto;
    }
}
