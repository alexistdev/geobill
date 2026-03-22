package com.alexistdev.geobill.utils;

import com.alexistdev.geobill.dto.UserDTO;
import com.alexistdev.geobill.models.entity.*;
import com.alexistdev.geobill.models.repository.*;
import com.alexistdev.geobill.request.RegisterRequest;
import com.alexistdev.geobill.services.ProductTypeService;
import com.alexistdev.geobill.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.awt.SystemColor.menu;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {


    private final MenuRepo menuRepo;
    private final RoleMenuRepo roleMenuRepo;
    private final UserRepo userRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ProductTypeRepo productTypeRepo;
    private final UserService userService;

    private static final String SYSTEM_USER = "System";
    private static final String DEFAULT_PASSWORD = "password";
    private final ProductTypeService productTypeService;
    private final ProductRepo productRepo;

    @Override
    public void run(String... args) {
        if (menuRepo.count() == 0) {
            seedMenus();
            seedRoleMenus();
            seedUsers();
            seedProductType();
            seedProduct();
        }
    }

    private List<Menu> menuAdmin(){
        Menu menuAdmin1 = createMenu("Dashboard", "/admin/dashboard", "menu-title d-flex align-items-center", 1, null,1, "ad1","bx bx-home-alt");
        Menu menuAdmin2 = createMenu("Master Data", "#", "menu-title d-flex align-items-center", 2, null,1,"ad2","bx bx-book-alt");

        return List.of(menuAdmin1, menuAdmin2);
    }

    private void seedProductType(){
        log.info("Seeding product types");
        List<ProductType> allProductTypes = new ArrayList<>();
        ProductType productType1 = createProductType("Shared Hosting");
        ProductType productType2 = createProductType("VPS");
        allProductTypes.addAll(List.of(productType1, productType2));
        productTypeRepo.saveAll(allProductTypes);
        log.info("Finished seeding product types");
    }

    private void seedProduct(){
        log.info("Seeding product");

        Optional<ProductType> productType = productTypeRepo.findByNameIncludingDeleted("Shared Hosting");
        productType.ifPresent(productType1 -> {
            Product product1 = createProduct(
                    "NVME-1", productType1, 50000.0,
                    12, "10GB", "1000 Mbps", "1",
                    "1", "1",
                    "1 GB RAM",
                    "2 Core",
                    "PHP, Ruby, Python, NodeJS",
                    null, null
            );

            Product product2 = createProduct(
                    "NVME-2", productType1, 150000.0,
                    12, "40GB", "5000 Mbps", "1",
                    "1", "1",
                    "4 GB RAM",
                    "4 Core",
                    "PHP, Ruby, Python, NodeJS",
                    null, null
            );

            Product product3 = createProduct(
                    "NVME-3", productType1, 200000.0,
                    12, "80GB", "Unlimited", "Unlimited",
                    "Unlimited", "5",
                    "6 GB RAM",
                    "8 Core",
                    "PHP, Ruby, Python, NodeJS",
                    null, null
            );
            List<Product> allProducts = new ArrayList<>(List.of(product1, product2, product3));
            productRepo.saveAll(allProducts);
        });

        log.info("Finished seeding product");
    }

    private Product createProduct(
            String name, ProductType productType, double price,
            int cycle, String capacity, String bandwith,
            String addon_domain, String database_account, String ftp_account,
            String info1, String info2, String info3, String info4, String info5
    ) {
        Product createdProduct = new Product();
        createdProduct.setName(name);
        createdProduct.setProductType(productType);
        createdProduct.setPrice(price);
        createdProduct.setCycle(cycle);
        createdProduct.setCapacity(capacity);
        createdProduct.setBandwith(bandwith);
        createdProduct.setAddon_domain(addon_domain);
        createdProduct.setDatabase_account(database_account);
        createdProduct.setFtp_account(ftp_account);
        createdProduct.setInfo1(info1);
        createdProduct.setInfo2(info2);
        createdProduct.setInfo3(info3);
        createdProduct.setInfo4(info4);
        createdProduct.setInfo5(info5);
        createdProduct.setCreatedBy(SYSTEM_USER);
        createdProduct.setCreatedDate(new java.util.Date());
        createdProduct.setModifiedDate(new java.util.Date());
        createdProduct.setIsDeleted(false);
        return createdProduct;
    }

    private ProductType createProductType(String name) {
        ProductType productType = new ProductType();
        productType.setName(name);
        productType.setCreatedBy(SYSTEM_USER);
        productType.setModifiedBy(SYSTEM_USER);
        productType.setCreatedDate(new java.util.Date());
        productType.setModifiedDate(new java.util.Date());
        return productType;
    }

    private void seedMenus() {
        log.info("Seeding menus");
        List<Menu> allMenus = new ArrayList<>(menuAdmin());
        Menu menu2 = createMenu("Dashboard", "/users/dashboard", "menu-title d-flex align-items-center", 1, null,2,"us1","bx bx-home-alt");
        Menu menu3 = createMenu("Services", "#", "menu-title d-flex align-items-center", 2, null,2,"us2","bx bx-collection");
        Menu menu4 = createMenu("Billing", "#", "menu-title d-flex align-items-center", 2, null,2,"us3","bx bx-money");
        Menu menu5 = createMenu("Support", "#", "menu-title d-flex align-items-center", 2, null,2,"us4","bx bx-headphone");

        allMenus.addAll(List.of(menu2, menu3, menu4, menu5));
        menuRepo.saveAll(allMenus);

        Optional<Menu> menuParentServices = menuRepo.findByCode("us2");
        Optional<Menu> menuParentBillings = menuRepo.findByCode("us3");
        Optional<Menu> menuParentSupports = menuRepo.findByCode("us4");

        Optional<Menu> menuParentAdmin = menuRepo.findByCode("ad2");

        menuParentServices.ifPresent(menu -> {
            Menu userChildren1 = createMenu("My Services", "/users/services", "", 1, menu.getId(),2,"uc1","bx bx-server");
            Menu userChildren2 = createMenu("Order New Services", "/users/services/order", "", 2, menu.getId(),2,"uc2","bx bx-cart");

            List<Menu> allMenuChildren = new ArrayList<>(List.of(userChildren1,userChildren2));
            menuRepo.saveAll(allMenuChildren);
        });

        menuParentBillings.ifPresent(menu -> {
            Menu userChildrenBilling = createMenu("My Invoices", "/users/billings", "", 1, menu.getId(),2,"uc3","bx bx-barcode");
            menuRepo.save(userChildrenBilling);
        });

        menuParentAdmin.ifPresent(menu -> {
            Menu adminChildren1 = createMenu("Product Type", "/admin/product_type", "", 1, menu.getId(),2,"ad3","bx bx-server");
            Menu adminChildren2 = createMenu("Product", "/admin/product", "", 2, menu.getId(),2,"ad4","bx bx-server");
            Menu adminChildren3 = createMenu("Users", "/admin/users", "", 2, menu.getId(),2,"ad5","bx bx-server");
            List<Menu> allMenuChildren = new ArrayList<>(List.of(adminChildren1,adminChildren2,adminChildren3));

            menuRepo.saveAll(allMenuChildren);
        });

        menuParentSupports.ifPresent(menu ->{
                Menu userChildrenSupport = createMenu("My Tickets", "/users/support/tickets", "", 1, menu.getId(),2,"uc4","bx bx-comment-edit");
                List<Menu> allMenuChildren = new ArrayList<>(List.of(userChildrenSupport));
                menuRepo.saveAll(allMenuChildren);
        });

        log.info("Finished seeding menus");
    }

    private void seedRoleMenus() {
        log.info("Seeding role menus");

        Map<Role, List<String>> roleMenuCodes = Map.of(
                Role.ADMIN, List.of("ad1", "ad2", "ad3", "ad4", "ad5"),
                Role.USER, List.of("us1", "us2", "us3", "us4", "uc1", "uc2", "uc3", "uc4")
        );

        List<RoleMenu> roleMenus = roleMenuCodes.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(code -> createRoleMenu(entry.getKey(), getMenuOrThrow(code))))
                .toList();

        roleMenuRepo.saveAll(roleMenus);
        log.info("Finished seeding role menus");
    }

    private Menu getMenuOrThrow(String code){
        return menuRepo.findByCode(code).orElseThrow(() -> new RuntimeException("Menu not found with code: " + code));
    }

    private void seedUsers() {
        log.info("Seeding users");
        RegisterRequest user = createUser("user", "user@gmail.com");
        userService.registerUser(user);
        RegisterRequest testUser2 = createUser("user2", "user2@gmail.com");
        userService.registerUser(testUser2);
        RegisterRequest staff = createUser("staff", "staff@gmail.com");
        UserDTO staffUser = userService.registerUser(staff);
        RegisterRequest admin = createUser("admin", "admin@gmail.com");
        UserDTO adminUser = userService.registerUser(admin);

        String emailAdmin = adminUser.getEmail();
        Optional<User> user1 = userRepo.findByEmail(emailAdmin);
        user1.ifPresent(user11 -> user11.setRole(Role.ADMIN));
        user1.ifPresent(userRepo::save);

        String emailStaff = staffUser.getEmail();
        Optional<User> user2 = userRepo.findByEmail(emailStaff);
        user2.ifPresent(user22 -> user22.setRole(Role.STAFF));
        user2.ifPresent(userRepo::save);
        log.info("Finished seeding users");
    }

    private Menu createMenu(String name, String url, String classLink, int sortOrder, UUID parentId, int typeMenu, String code, String icon) {
        Menu menu = new Menu();
        menu.setName(name);
        menu.setUrlink(url);
        menu.setClasslink(classLink);
        menu.setIsDeleted(false);
        menu.setCreatedBy(SYSTEM_USER);
        menu.setModifiedBy(SYSTEM_USER);
        menu.setParentId(parentId);
        menu.setCreatedDate(new java.util.Date());
        menu.setModifiedDate(new java.util.Date());
        menu.setSortOrder(sortOrder);
        menu.setTypeMenu(typeMenu);
        menu.setIcon(icon);
        menu.setCode(code);
        return menu;
    }

    private RoleMenu createRoleMenu(Role role, Menu menu) {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRole(role);
        roleMenu.setMenu(menu);
        roleMenu.setCreatedBy(SYSTEM_USER);
        roleMenu.setModifiedBy(SYSTEM_USER);
        roleMenu.setCreatedDate(new java.util.Date());
        roleMenu.setModifiedDate(new java.util.Date());
        roleMenu.setIsDeleted(false);
        return roleMenu;
    }

    private RegisterRequest createUser(String fullName, String email) {
        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setFullName(fullName);
        userRequest.setEmail(email);
        userRequest.setPassword(DEFAULT_PASSWORD);
        return userRequest;
    }
}
