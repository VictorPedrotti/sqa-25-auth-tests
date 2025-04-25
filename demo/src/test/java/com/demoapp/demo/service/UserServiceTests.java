package com.demoapp.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.demoapp.demo.model.User;
import com.demoapp.demo.repository.UserRepository;

@SpringBootTest
public class UserServiceTests {

  @Test
  @DisplayName("Test if password is valid according to regex")
  void testIsPasswordValid() {
    String password = "Password123!";
    UserService userService = new UserService(null);
    boolean isValid = userService.isPasswordValid(password);
    assertTrue(isValid, "Password should be valid according to the regex.");
  }

  @Test
  @DisplayName("Test if password is invalid according to regex")
  void testIsPasswordInvalid() {
    String password = "password123";
    UserService userService = new UserService(null);
    boolean isValid = userService.isPasswordValid(password);
    assertTrue(!isValid, "Password should be invalid according to the regex.");
  }

  @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("ValidPass1!");
    }

    @Test
    @DisplayName("Should return true when e-mail is valid")
    void isEmailValid_ShouldReturnTrue_WhenEmailIsValid() {
        assertTrue(userService.isEmailValid("user@example.com"));
    }

    @Test
    @DisplayName("Should return false when e-mail is invalid")
    void isEmailValid_ShouldReturnFalse_WhenEmailIsInvalid() {
        assertFalse(userService.isEmailValid("invalid-email"));
    }

    @Test
    @DisplayName("Should return true when password meets requirements")
    void isPasswordValid_ShouldReturnTrue_WhenPasswordMeetsRequirements() {
        assertTrue(userService.isPasswordValid("ValidPass1!"));
    }

    @Test
    @DisplayName("Should return false when password does not meet requirements")
    void isPasswordValid_ShouldReturnFalse_WhenPasswordDoesNotMeetRequirements() {
        assertFalse(userService.isPasswordValid("invalid1!"));
        assertFalse(userService.isPasswordValid("INVALID1!"));
        assertFalse(userService.isPasswordValid("Invalid!!"));
        assertFalse(userService.isPasswordValid("Invalid1"));
        assertFalse(userService.isPasswordValid("Inv1!"));
        assertFalse(userService.isPasswordValid(null));
    }

    @Test
    @DisplayName("Should create user when input is valid")
    void createUser_ShouldSaveAndReturnUser_WhenInputIsValid() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User createdUser = userService.createUser("test@example.com", "ValidPass1!");

        assertNotNull(createdUser);
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("ValidPass1!", createdUser.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should return user when user(e-mail) exists")
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User foundUser = userService.findByEmail("test@example.com");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should return null when user(e-mail) does not exist")
    void findByEmail_ShouldReturnNull_WhenUserDoesNotExist() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        User foundUser = userService.findByEmail("nonexistent@example.com");

        assertNull(foundUser);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should return null when e-mail is null")
    void findByEmail_ShouldReturnNull_WhenEmailIsNull() {
        User foundUser = userService.findByEmail(null);

        assertNull(foundUser);
        verify(userRepository, never()).findByEmail(anyString());
    }
}
