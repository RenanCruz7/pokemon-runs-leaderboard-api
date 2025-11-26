package pokemon.runs.time.leaderboard.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.repository.user.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("UserRepository - Testes de Integração")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123"); // Plain password for testing
        testUser.setRole("CUSTOMER");
    }

    // ==================== SAVE TESTS ====================

    @Test
    @DisplayName("Save - Deve salvar um usuário com sucesso")
    void testSave_Success() {
        // Act
        User savedUser = userRepository.save(testUser);

        // Assert
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("CUSTOMER", savedUser.getRole());
    }

    @Test
    @DisplayName("Save - Deve salvar múltiplos usuários")
    void testSave_Multiple() {
        // Arrange
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password123");
        user1.setRole("CUSTOMER");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password123");
        user2.setRole("CUSTOMER");

        // Act
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        // Assert
        assertNotNull(savedUser1.getId());
        assertNotNull(savedUser2.getId());
        assertNotEquals(savedUser1.getId(), savedUser2.getId());
    }

    @Test
    @DisplayName("Save - Deve atualizar usuário existente")
    void testSave_Update() {
        // Arrange
        User savedUser = userRepository.save(testUser);
        String newEmail = "newemail@example.com";

        // Act
        savedUser.setEmail(newEmail);
        User updatedUser = userRepository.save(savedUser);

        // Assert
        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals(newEmail, updatedUser.getEmail());
    }

    // ==================== FIND BY USERNAME TESTS ====================

    @Test
    @DisplayName("FindByUsername - Deve encontrar usuário por username")
    void testFindByUsername_Success() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("FindByUsername - Deve retornar vazio para username inexistente")
    void testFindByUsername_NotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("FindByUsername - Deve ser case-sensitive")
    void testFindByUsername_CaseSensitive() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByUsername("TESTUSER");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("FindByUsername - Deve encontrar apenas um usuário por username")
    void testFindByUsername_UniqueConstraint() {
        // Arrange
        User user1 = new User();
        user1.setUsername("testuser");
        user1.setEmail("email1@example.com");
        user1.setPassword("password123");
        user1.setRole("CUSTOMER");
        userRepository.save(user1);

        // Username duplicado deve violar constraint
        User user2 = new User();
        user2.setUsername("testuser");
        user2.setEmail("email2@example.com");
        user2.setPassword("password123");
        user2.setRole("CUSTOMER");

        // Act & Assert
        assertThrows(Exception.class, () -> userRepository.save(user2));
    }

    // ==================== FIND BY EMAIL TESTS ====================

    @Test
    @DisplayName("FindByEmail - Deve encontrar usuário por email")
    void testFindByEmail_Success() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("FindByEmail - Deve retornar vazio para email inexistente")
    void testFindByEmail_NotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("FindByEmail - Deve ser case-sensitive")
    void testFindByEmail_CaseSensitive() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("TEST@EXAMPLE.COM");

        // Assert
        // PostgreSQL is case-sensitive by default for email
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("FindByEmail - Deve encontrar apenas um usuário por email")
    void testFindByEmail_UniqueConstraint() {
        // Arrange
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("test@example.com");
        user1.setPassword("password123");
        user1.setRole("CUSTOMER");
        userRepository.save(user1);

        // Email duplicado deve violar constraint
        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("test@example.com");
        user2.setPassword("password123");
        user2.setRole("CUSTOMER");

        // Act & Assert
        assertThrows(Exception.class, () -> userRepository.save(user2));
    }

    // ==================== FIND BY ID TESTS ====================

    @Test
    @DisplayName("FindById - Deve encontrar usuário por ID")
    void testFindById_Success() {
        // Arrange
        User savedUser = userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    @DisplayName("FindById - Deve retornar vazio para ID inexistente")
    void testFindById_NotFound() {
        // Act
        Optional<User> foundUser = userRepository.findById(999L);

        // Assert
        assertFalse(foundUser.isPresent());
    }

    // ==================== DELETE TESTS ====================

    @Test
    @DisplayName("Delete - Deve deletar um usuário")
    void testDelete_Success() {
        // Arrange
        User savedUser = userRepository.save(testUser);

        // Act
        userRepository.delete(savedUser);

        // Assert
        assertFalse(userRepository.findById(savedUser.getId()).isPresent());
    }

    @Test
    @DisplayName("Delete - Deve deletar por ID")
    void testDeleteById_Success() {
        // Arrange
        User savedUser = userRepository.save(testUser);

        // Act
        userRepository.deleteById(savedUser.getId());

        // Assert
        assertFalse(userRepository.findById(savedUser.getId()).isPresent());
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    @DisplayName("FindAll - Deve retornar todos os usuários")
    void testFindAll_Success() {
        // Arrange
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password123");
        user1.setRole("CUSTOMER");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password123");
        user2.setRole("CUSTOMER");

        userRepository.save(user1);
        userRepository.save(user2);

        // Act
        var allUsers = userRepository.findAll();

        // Assert
        assertNotNull(allUsers);
        assertTrue(allUsers.size() >= 2);
    }

    @Test
    @DisplayName("FindAll - Deve retornar lista vazia quando não há usuários")
    void testFindAll_Empty() {
        // Act
        var allUsers = userRepository.findAll();

        // Assert
        assertNotNull(allUsers);
        assertTrue(allUsers.isEmpty());
    }

    // ==================== COUNT TESTS ====================

    @Test
    @DisplayName("Count - Deve contar total de usuários")
    void testCount_Success() {
        // Arrange
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password123");
        user1.setRole("CUSTOMER");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password123");
        user2.setRole("CUSTOMER");

        userRepository.save(user1);
        userRepository.save(user2);

        // Act
        long count = userRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Count - Deve retornar zero quando não há usuários")
    void testCount_Empty() {
        // Act
        long count = userRepository.count();

        // Assert
        assertEquals(0, count);
    }

    // ==================== EXISTS TESTS ====================

    @Test
    @DisplayName("ExistsById - Deve retornar true para ID existente")
    void testExistsById_True() {
        // Arrange
        User savedUser = userRepository.save(testUser);

        // Act
        boolean exists = userRepository.existsById(savedUser.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("ExistsById - Deve retornar false para ID inexistente")
    void testExistsById_False() {
        // Act
        boolean exists = userRepository.existsById(999L);

        // Assert
        assertFalse(exists);
    }
}

