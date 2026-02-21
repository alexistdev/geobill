package com.alexistdev.geobill.service;

import com.alexistdev.geobill.models.entity.User;
import com.alexistdev.geobill.models.repository.UserRepo;
import com.alexistdev.geobill.services.UserService;
import com.alexistdev.geobill.utils.MessagesUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceEmailTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private UserService userService;

    @Test
    void isEmailAvailable_whenEmailExists_shouldThrowException() {
        String email = "admin@gmail.com";
        User user = new User();
        user.setEmail(email);

        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(messagesUtils.getMessage("userservice.email.exist")).thenReturn("Email already exists");

        Assertions.assertThrows(RuntimeException.class, () -> userService.isEmailAvailable(email));
    }

    @Test
    void isEmailAvailable_whenEmailDoesNotExist_shouldReturnTrue() {
        String email = "new@gmail.com";

        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        boolean result = userService.isEmailAvailable(email);
        Assertions.assertTrue(result);
    }
}
