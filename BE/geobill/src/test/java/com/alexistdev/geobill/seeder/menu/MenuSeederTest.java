package com.alexistdev.geobill.seeder.menu;

import com.alexistdev.geobill.models.entity.Menu;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.RoleMenu;
import com.alexistdev.geobill.models.repository.MenuRepo;
import com.alexistdev.geobill.models.repository.RoleMenuRepo;
import com.alexistdev.geobill.seeder.SeedAudit;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MenuSeederTest {

    private static final String ROOT_CLASS_LINK = "menu-title d-flex align-items-center";

    @Mock
    private MenuRepo menuRepo;

    @Mock
    private RoleMenuRepo roleMenuRepo;

    @InjectMocks
    private MenuSeeder menuSeeder;

    @BeforeEach
    void setUp() {
        // The seeder reads back the generated id to link a child to its parent.
        org.mockito.Mockito.lenient().when(menuRepo.save(any(Menu.class))).thenAnswer(invocation -> {
            Menu menu = invocation.getArgument(0);
            menu.setId(UUID.randomUUID());
            return menu;
        });
    }

    private static int countDefinitions(List<MenuDefinition> definitions) {
        int total = 0;
        for (MenuDefinition definition : definitions) {
            total += 1 + countDefinitions(definition.children());
        }
        return total;
    }

    private static int expectedMenuCount() {
        int total = 0;
        for (MenuSection section : MenuCatalog.sections()) {
            total += countDefinitions(section.items());
        }
        return total;
    }

    private List<Menu> captureSavedMenus() {
        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepo, times(expectedMenuCount())).save(captor.capture());
        return captor.getAllValues();
    }

    @SuppressWarnings("unchecked")
    private List<RoleMenu> captureSavedRoleMenus() {
        ArgumentCaptor<List<RoleMenu>> captor = ArgumentCaptor.forClass(List.class);
        verify(roleMenuRepo, times(1)).saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    @Order(1)
    @DisplayName("1:Test Seeder Name And Order")
    void testNameAndOrder() {
        Assertions.assertEquals("menus", menuSeeder.name());
        Assertions.assertEquals(10, menuSeeder.order());
    }

    @Test
    @Order(2)
    @DisplayName("2:Test ShouldRun Is True When The Table Is Empty")
    void testShouldRunOnEmptyTable() {
        when(menuRepo.count()).thenReturn(0L);

        Assertions.assertTrue(menuSeeder.shouldRun());
        verify(menuRepo, times(1)).count();
    }

    @Test
    @Order(3)
    @DisplayName("3:Test ShouldRun Is False When Menus Already Exist")
    void testShouldNotRunWhenMenusExist() {
        when(menuRepo.count()).thenReturn(15L);

        Assertions.assertFalse(menuSeeder.shouldRun());
        verify(menuRepo, times(1)).count();
    }

    @Test
    @Order(4)
    @DisplayName("4:Test Seed Saves Every Catalog Menu")
    void testSeedSavesEveryMenu() {
        menuSeeder.seed();

        List<Menu> saved = captureSavedMenus();

        Assertions.assertEquals(expectedMenuCount(), saved.size());
        List<String> codes = saved.stream().map(Menu::getCode).toList();
        Assertions.assertTrue(codes.contains("ADM_DASHBOARD"));
        Assertions.assertTrue(codes.contains("ADM_PRODUCT_TYPE"));
        Assertions.assertTrue(codes.contains("USR_INVOICE"));
        Assertions.assertEquals(codes.size(), codes.stream().distinct().count());
    }

    @Test
    @Order(5)
    @DisplayName("5:Test Seed Maps The Catalog Values Onto The Menu")
    void testSeedMapsCatalogValues() {
        menuSeeder.seed();

        Map<String, Menu> byCode = captureSavedMenus().stream()
                .collect(Collectors.toMap(Menu::getCode, Function.identity()));

        Menu dashboard = byCode.get("ADM_DASHBOARD");
        Assertions.assertEquals("Dashboard", dashboard.getName());
        Assertions.assertEquals("/admin/dashboard", dashboard.getUrlink());
        Assertions.assertEquals("bx bx-home-alt", dashboard.getIcon());
        Assertions.assertEquals(MenuType.ADMIN.value(), dashboard.getTypeMenu());

        Menu invoice = byCode.get("USR_INVOICE");
        Assertions.assertEquals("My Invoices", invoice.getName());
        Assertions.assertEquals("/users/billings", invoice.getUrlink());
        Assertions.assertEquals(MenuType.USER.value(), invoice.getTypeMenu());
    }

    @Test
    @Order(6)
    @DisplayName("6:Test Root Menus Carry The Root Class And No Parent")
    void testRootMenus() {
        menuSeeder.seed();

        Map<String, Menu> byCode = captureSavedMenus().stream()
                .collect(Collectors.toMap(Menu::getCode, Function.identity()));

        Menu root = byCode.get("ADM_MASTER");
        Assertions.assertEquals(ROOT_CLASS_LINK, root.getClasslink());
        Assertions.assertNull(root.getParentId());
    }

    @Test
    @Order(7)
    @DisplayName("7:Test Child Menus Point To Their Parent And Carry No Class")
    void testChildMenus() {
        menuSeeder.seed();

        Map<String, Menu> byCode = captureSavedMenus().stream()
                .collect(Collectors.toMap(Menu::getCode, Function.identity()));

        Menu parent = byCode.get("ADM_MASTER");
        for (String code : List.of("ADM_PRODUCT_TYPE", "ADM_PRODUCT", "ADM_USER")) {
            Menu child = byCode.get(code);
            Assertions.assertEquals(parent.getId(), child.getParentId(), code);
            Assertions.assertEquals("", child.getClasslink(), code);
        }
    }

    @Test
    @Order(8)
    @DisplayName("8:Test Sort Order Restarts At One On Every Level")
    void testSortOrder() {
        menuSeeder.seed();

        Map<String, Menu> byCode = captureSavedMenus().stream()
                .collect(Collectors.toMap(Menu::getCode, Function.identity()));

        Assertions.assertEquals(1, byCode.get("ADM_DASHBOARD").getSortOrder());
        Assertions.assertEquals(2, byCode.get("ADM_MASTER").getSortOrder());
        Assertions.assertEquals(3, byCode.get("ADM_SUPPORT").getSortOrder());

        Assertions.assertEquals(1, byCode.get("ADM_PRODUCT_TYPE").getSortOrder());
        Assertions.assertEquals(2, byCode.get("ADM_PRODUCT").getSortOrder());
        Assertions.assertEquals(3, byCode.get("ADM_USER").getSortOrder());

        Assertions.assertEquals(1, byCode.get("USR_DASHBOARD").getSortOrder());
        Assertions.assertEquals(1, byCode.get("USR_SERVICE_LIST").getSortOrder());
        Assertions.assertEquals(2, byCode.get("USR_SERVICE_ORDER").getSortOrder());
    }

    @Test
    @Order(9)
    @DisplayName("9:Test Every Menu Gets A Role Menu From Its Section")
    void testRoleMenus() {
        menuSeeder.seed();

        List<RoleMenu> roleMenus = captureSavedRoleMenus();

        Assertions.assertEquals(expectedMenuCount(), roleMenus.size());

        List<String> adminCodes = new ArrayList<>();
        List<String> userCodes = new ArrayList<>();
        for (RoleMenu roleMenu : roleMenus) {
            Assertions.assertNotNull(roleMenu.getMenu());
            if (roleMenu.getRole() == Role.ADMIN) {
                adminCodes.add(roleMenu.getMenu().getCode());
            } else if (roleMenu.getRole() == Role.USER) {
                userCodes.add(roleMenu.getMenu().getCode());
            }
        }

        Assertions.assertEquals(7, adminCodes.size());
        Assertions.assertEquals(8, userCodes.size());
        Assertions.assertTrue(adminCodes.stream().allMatch(code -> code.startsWith("ADM_")));
        Assertions.assertTrue(userCodes.stream().allMatch(code -> code.startsWith("USR_")));
    }

    @Test
    @Order(10)
    @DisplayName("10:Test Seeded Rows Carry The Audit Columns")
    void testSeededRowsAreStamped() {
        menuSeeder.seed();

        for (Menu menu : captureSavedMenus()) {
            Assertions.assertEquals(SeedAudit.SYSTEM_USER, menu.getCreatedBy());
            Assertions.assertEquals(SeedAudit.SYSTEM_USER, menu.getModifiedBy());
            Assertions.assertNotNull(menu.getCreatedDate());
            Assertions.assertFalse(menu.getDeleted());
        }
        for (RoleMenu roleMenu : captureSavedRoleMenus()) {
            Assertions.assertEquals(SeedAudit.SYSTEM_USER, roleMenu.getCreatedBy());
            Assertions.assertEquals(SeedAudit.SYSTEM_USER, roleMenu.getModifiedBy());
            Assertions.assertNotNull(roleMenu.getCreatedDate());
            Assertions.assertFalse(roleMenu.getDeleted());
        }
    }

    @Test
    @Order(11)
    @DisplayName("11:Test Duplicate Menu Code Is Rejected")
    void testDuplicateCodeIsRejected() {
        MenuDefinition dashboard =
                MenuDefinition.menu("ADM_DASHBOARD", "Dashboard", "/admin/dashboard", "bx bx-home-alt");
        MenuDefinition duplicate =
                MenuDefinition.menu("ADM_DASHBOARD", "Copy", "/admin/copy", "bx bx-home-alt");
        List<MenuSection> sections =
                List.of(MenuSection.section(MenuType.ADMIN, Role.ADMIN, dashboard, duplicate));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(menuSeeder, "verifyCodesAreUnique", sections));

        Assertions.assertEquals("Duplicate menu code in MenuCatalog: ADM_DASHBOARD",
                exception.getMessage());
    }

    @Test
    @Order(12)
    @DisplayName("12:Test Duplicate Code Inside A Submenu Is Rejected")
    void testDuplicateCodeInSubmenuIsRejected() {
        MenuDefinition child =
                MenuDefinition.menu("ADM_PRODUCT", "Product", "/admin/product", "bx bx-server");
        MenuDefinition group =
                MenuDefinition.group("ADM_MASTER", "Master Data", "bx bx-book-alt", child, child);
        List<MenuSection> sections =
                List.of(MenuSection.section(MenuType.ADMIN, Role.ADMIN, group));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(menuSeeder, "verifyCodesAreUnique", sections));

        Assertions.assertEquals("Duplicate menu code in MenuCatalog: ADM_PRODUCT",
                exception.getMessage());
    }

    @Test
    @Order(13)
    @DisplayName("13:Test Unique Codes Pass The Check")
    void testUniqueCodesPass() {
        Assertions.assertDoesNotThrow(() ->
                ReflectionTestUtils.invokeMethod(menuSeeder, "verifyCodesAreUnique", MenuCatalog.sections()));
    }
}
