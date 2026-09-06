package com.alexistdev.geobill.seeder.user;

import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.UserRepo;
import com.alexistdev.geobill.request.RegisterRequest;
import com.alexistdev.geobill.seeder.Seeder;
import com.alexistdev.geobill.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creates the accounts listed in {@link UserCatalog}.
 *
 * <p>Registration goes through {@link UserService} so each account gets the same customer record
 * and password hashing as a real sign-up; the role is applied afterwards because registration
 * always starts at {@link Role#USER}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSeeder implements Seeder {

    private final UserService userService;
    private final UserRepo userRepo;

    @Override
    public String name() {
        return "users";
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public boolean shouldRun() {
        return userRepo.count() == 0;
    }

    @Override
    public void seed() {
        List<UserDefinition> definitions = UserCatalog.users();

        for (UserDefinition definition : definitions) {
            userService.registerUser(toRegisterRequest(definition));

            if (definition.role() != Role.USER) {
                applyRole(definition);
            }
        }

        log.info("Seeded {} user(s)", definitions.size());
    }

    private void applyRole(UserDefinition definition) {
        User user = userRepo.findByEmail(definition.email())
                .orElseThrow(() -> new IllegalStateException(
                        "Seeded user not found: " + definition.email()));
        user.setRole(definition.role());
        userRepo.save(user);
    }

    private RegisterRequest toRegisterRequest(UserDefinition definition) {
        RegisterRequest request = new RegisterRequest();
        request.setFullName(definition.fullName());
        request.setEmail(definition.email());
        request.setPassword(UserCatalog.DEFAULT_PASSWORD);
        return request;
    }
}
