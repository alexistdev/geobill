package com.alexistdev.geobill.seeder.user;

import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.UserRepo;
import com.alexistdev.geobill.request.RegisterRequest;
import com.alexistdev.geobill.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserSeederTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserSeeder userSeeder;

    private User staff;
    private User admin;

    @BeforeEach
    void setUp() {
        staff = buildUser("staff", "staff@gmail.com");
        admin = buildUser("admin", "admin@gmail.com");
    }

    private User buildUser(String fullName, String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(Role.USER);
        return user;
    }

    @Test
    @Order(1)
    @DisplayName("1:Test Seeder Name And Order")
    void testNameAndOrder() {
        Assertions.assertEquals("users", userSeeder.name());
        Assertions.assertEquals(20, userSeeder.order());
    }

    @Test
    @Order(2)
    @DisplayName("2:Test ShouldRun Is True When The Table Is Empty")
    void testShouldRunOnEmptyTable() {
        when(userRepo.count()).thenReturn(0L);

        Assertions.assertTrue(userSeeder.shouldRun());
        verify(userRepo, times(1)).count();
    }

    @Test
    @Order(3)
    @DisplayName("3:Test ShouldRun Is False When Users Already Exist")
    void testShouldNotRunWhenUsersExist() {
        when(userRepo.count()).thenReturn(4L);

        Assertions.assertFalse(userSeeder.shouldRun());
        verify(userRepo, times(1)).count();
    }

    @Test
    @Order(4)
    @DisplayName("4:Test Seed Registers Every Catalog Account")
    void testSeedRegistersEveryAccount() {
        when(userRepo.findByEmail("staff@gmail.com")).thenReturn(Optional.of(staff));
        when(userRepo.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));

        userSeeder.seed();

        verify(userService, times(UserCatalog.users().size())).registerUser(any(RegisterRequest.class));
    }

    @Test
    @Order(5)
    @DisplayName("5:Test Seed Sends The Catalog Values To The Register Request")
    void testSeedBuildsRegisterRequest() {
        when(userRepo.findByEmail("staff@gmail.com")).thenReturn(Optional.of(staff));
        when(userRepo.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));

        userSeeder.seed();

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(userService, times(4)).registerUser(captor.capture());

        List<RegisterRequest> requests = captor.getAllValues();
        RegisterRequest first = requests.getFirst();

        Assertions.assertEquals("user", first.getFullName());
        Assertions.assertEquals("user@gmail.com", first.getEmail());
        for (RegisterRequest request : requests) {
            Assertions.assertEquals(UserCatalog.DEFAULT_PASSWORD, request.getPassword());
        }
    }

    @Test
    @Order(6)
    @DisplayName("6:Test Seed Applies The Role Only To Non User Accounts")
    void testSeedAppliesRoleForNonUserAccounts() {
        when(userRepo.findByEmail("staff@gmail.com")).thenReturn(Optional.of(staff));
        when(userRepo.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));

        userSeeder.seed();

        verify(userRepo, never()).findByEmail("user@gmail.com");
        verify(userRepo, never()).findByEmail("user2@gmail.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo, times(2)).save(captor.capture());

        List<User> savedUsers = captor.getAllValues();
        Assertions.assertEquals(Role.STAFF, savedUsers.get(0).getRole());
        Assertions.assertEquals("staff@gmail.com", savedUsers.get(0).getEmail());
        Assertions.assertEquals(Role.ADMIN, savedUsers.get(1).getRole());
        Assertions.assertEquals("admin@gmail.com", savedUsers.get(1).getEmail());
    }

    @Test
    @Order(7)
    @DisplayName("7:Test Seed Fails When A Seeded User Cannot Be Found")
    void testSeedFailsWhenUserIsMissing() {
        when(userRepo.findByEmail("staff@gmail.com")).thenReturn(Optional.empty());

        IllegalStateException exception =
                Assertions.assertThrows(IllegalStateException.class, () -> userSeeder.seed());

        Assertions.assertEquals("Seeded user not found: staff@gmail.com", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }
}
