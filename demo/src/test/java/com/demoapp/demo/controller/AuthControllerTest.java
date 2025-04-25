package com.demoapp.demo.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demoapp.demo.dto.EmailDTO;
import com.demoapp.demo.dto.UserDTO;
import com.demoapp.demo.model.User;
import com.demoapp.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private UserService userService;

  @InjectMocks
  private AuthController authController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
  }

  @Nested
  class signup {

    @Test
    @DisplayName("Should return created user when input is valid")
    void signup_ShouldReturnCreatedUser_WhenInputIsValid() throws Exception {
      UserDTO userDTO = new UserDTO();
      userDTO.setEmail("valid@email.com");
      userDTO.setPassword("ValidPass1!");
      User mockUser = new User();
      mockUser.setEmail(userDTO.getEmail());
      mockUser.setPassword(userDTO.getPassword());

      when(userService.isEmailValid(anyString())).thenReturn(true);
      when(userService.isPasswordValid(anyString())).thenReturn(true);
      when(userService.findByEmail(anyString())).thenReturn(null);
      when(userService.createUser(anyString(), anyString())).thenReturn(mockUser);

      mockMvc.perform(post("/auth/signup")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userDTO)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email").value(userDTO.getEmail()));
    }

    @Test
    @DisplayName("Should return 422 when e-mail is invalid")
    void signup_ShouldReturn422_WhenEmailIsInvalid() throws Exception {
      UserDTO userDTO = new UserDTO();
      userDTO.setEmail("invalid-email");
      userDTO.setPassword("ValidPass1!");

      when(userService.isEmailValid(anyString())).thenReturn(false);

      mockMvc.perform(post("/auth/signup")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userDTO)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.message").value("E-mail inválido"));
    }

    @Test
    @DisplayName("Should return 422 when password is invalid")
    void signup_ShouldReturn422_WhenPasswordIsInvalid() throws Exception {
      UserDTO userDTO = new UserDTO();
      userDTO.setEmail("valid@email.com");
      userDTO.setPassword("weak");

      when(userService.isEmailValid(anyString())).thenReturn(true);
      when(userService.isPasswordValid(anyString())).thenReturn(false);

      mockMvc.perform(post("/auth/signup")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userDTO)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.message").value("Senha inválida"));
    }

    @Test
    @DisplayName("Should return 409 when e-mail already exists")
    void signup_ShouldReturn409_WhenEmailAlreadyExists() throws Exception {
      UserDTO userDTO = new UserDTO();
      userDTO.setEmail("existing@email.com");
      userDTO.setPassword("ValidPass1!");
      User existingUser = new User();
      existingUser.setEmail(userDTO.getEmail());

      when(userService.isEmailValid(anyString())).thenReturn(true);
      when(userService.isPasswordValid(anyString())).thenReturn(true);
      when(userService.findByEmail(anyString())).thenReturn(existingUser);

      mockMvc.perform(post("/auth/signup")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userDTO)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.message").value("E-mail já está em uso"));
    }
  }

  @Nested
  class signin {

    @Test
    @DisplayName("Should return token when credentials are valid")
    void signin_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
      UserDTO userDTO = new UserDTO();
      userDTO.setEmail("valid@email.com");
      userDTO.setPassword("ValidPass1!");
      User mockUser = new User();
      mockUser.setEmail(userDTO.getEmail());
      mockUser.setPassword(userDTO.getPassword());

      when(userService.isEmailValid(anyString())).thenReturn(true);
      when(userService.isPasswordValid(anyString())).thenReturn(true);
      when(userService.findByEmail(anyString())).thenReturn(mockUser);

      mockMvc.perform(post("/auth/signin")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userDTO)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Should return 401 when user not found")
    void signin_ShouldReturn401_WhenUserNotFound() throws Exception {
      UserDTO userDTO = new UserDTO();
      userDTO.setEmail("nonexistent@email.com");
      userDTO.setPassword("anyPassword");

      when(userService.isEmailValid(anyString())).thenReturn(true);
      when(userService.isPasswordValid(anyString())).thenReturn(true);
      when(userService.findByEmail(anyString())).thenReturn(null);

      mockMvc.perform(post("/auth/signin")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userDTO)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("Should return 401 when password is wrong")
    void signin_ShouldReturn401_WhenPasswordIsWrong() throws Exception {
      UserDTO userDTO = new UserDTO();
      userDTO.setEmail("valid@email.com");
      userDTO.setPassword("WrongPass1!");
      User mockUser = new User();
      mockUser.setEmail(userDTO.getEmail());
      mockUser.setPassword("CorrectPass1!"); // Senha diferente

      when(userService.isEmailValid(anyString())).thenReturn(true);
      when(userService.isPasswordValid(anyString())).thenReturn(true);
      when(userService.findByEmail(anyString())).thenReturn(mockUser);

      mockMvc.perform(post("/auth/signin")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userDTO)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("Should return 422 when e-mail is invalid")
    void signin_ShouldReturn422_WhenEmailIsInvalid() throws Exception {
      UserDTO userDTO = new UserDTO();
      userDTO.setEmail("invalid-email");
      userDTO.setPassword("ValidPass1!");

      when(userService.isEmailValid(anyString())).thenReturn(false);

      mockMvc.perform(post("/auth/signin")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userDTO)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.message").value("E-mail inválido"));
    }

    @Test
    @DisplayName("Should return 422 when password is invalid")
    void signin_ShouldReturn422_WhenPasswordIsInvalid() throws Exception {
      UserDTO userDTO = new UserDTO();
      userDTO.setEmail("valid@email.com");
      userDTO.setPassword("weak");

      when(userService.isEmailValid(anyString())).thenReturn(true);
      when(userService.isPasswordValid(anyString())).thenReturn(false);

      mockMvc.perform(post("/auth/signin")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userDTO)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.message").value("Senha inválida"));
    }
  }

  @Nested
  class resetPassword {

    @Test
    @DisplayName("Should return success when e-mail is valid")
    void resetPassword_ShouldReturnSuccess_WhenEmailIsValid() throws Exception {
      EmailDTO emailDTO = new EmailDTO();
      emailDTO.setEmail("valid@email.com");
      User mockUser = new User();
      mockUser.setEmail(emailDTO.getEmail());

      when(userService.isEmailValid(anyString())).thenReturn(true);
      when(userService.findByEmail(anyString())).thenReturn(mockUser);

      mockMvc.perform(post("/auth/reset-password")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(emailDTO)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso (fake)"));
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void resetPassword_ShouldReturn404_WhenUserNotFound() throws Exception {
      EmailDTO emailDTO = new EmailDTO();
      emailDTO.setEmail("nonexistent@email.com");

      when(userService.isEmailValid(anyString())).thenReturn(true);
      when(userService.findByEmail(anyString())).thenReturn(null);

      mockMvc.perform(post("/auth/reset-password")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(emailDTO)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Usuário não encontrado"));
    }

    @Test
    @DisplayName("Should return 422 when e-mail is invalid")
    void resetPassword_ShouldReturn422_WhenEmailIsInvalid() throws Exception {
      EmailDTO emailDTO = new EmailDTO();
      emailDTO.setEmail("invalid-email");

      when(userService.isEmailValid(anyString())).thenReturn(false);

      mockMvc.perform(post("/auth/reset-password")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(emailDTO)))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.message").value("E-mail inválido"));
    }
  }
}
