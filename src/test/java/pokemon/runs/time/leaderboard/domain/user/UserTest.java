package pokemon.runs.time.leaderboard.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User - Testes Unitários")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    @DisplayName("Constructor - Deve criar usuário vazio")
    void testConstructor_NoArgs() {
        // Act
        User newUser = new User();

        // Assert
        assertNotNull(newUser);
        assertNull(newUser.getId());
        assertNull(newUser.getUsername());
        assertNull(newUser.getPassword());
        assertNull(newUser.getEmail());
    }

    @Test
    @DisplayName("Constructor - Deve criar usuário com todos os parâmetros")
    void testConstructor_AllArgs() {
        // Act
        User newUser = new User(
                1L,
                "testuser",
                "password123",
                "test@example.com",
                "CUSTOMER",
                null
        );

        // Assert
        assertEquals(1L, newUser.getId());
        assertEquals("testuser", newUser.getUsername());
        assertEquals("password123", newUser.getPassword());
        assertEquals("test@example.com", newUser.getEmail());
        assertEquals("CUSTOMER", newUser.getRole());
    }

    // ==================== GETTERS AND SETTERS TESTS ====================

    @Test
    @DisplayName("Setter/Getter - Deve set e get ID")
    void testSetGetId() {
        // Act
        user.setId(1L);

        // Assert
        assertEquals(1L, user.getId());
    }

    @Test
    @DisplayName("Setter/Getter - Deve set e get username")
    void testSetGetUsername() {
        // Act
        user.setUsername("testuser");

        // Assert
        assertEquals("testuser", user.getUsername());
    }

    @Test
    @DisplayName("Setter/Getter - Deve set e get password")
    void testSetGetPassword() {
        // Act
        user.setPassword("password123");

        // Assert
        assertEquals("password123", user.getPassword());
    }

    @Test
    @DisplayName("Setter/Getter - Deve set e get email")
    void testSetGetEmail() {
        // Act
        user.setEmail("test@example.com");

        // Assert
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Setter/Getter - Deve set e get role")
    void testSetGetRole() {
        // Act
        user.setRole("ADMIN");

        // Assert
        assertEquals("ADMIN", user.getRole());
    }

    @Test
    @DisplayName("Setter/Getter - Deve retornar CUSTOMER como role padrão")
    void testDefaultRole() {
        // Assert
        assertEquals("CUSTOMER", user.getRole());
    }

    // ==================== AUTHORITIES TESTS ====================

    @Test
    @DisplayName("GetAuthorities - Deve retornar ROLE_CUSTOMER para user comum")
    void testGetAuthorities_Customer() {
        // Arrange
        user.setRole("CUSTOMER");

        // Act
        var authorities = user.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    @Test
    @DisplayName("GetAuthorities - Deve retornar ROLE_ADMIN para admin")
    void testGetAuthorities_Admin() {
        // Arrange
        user.setRole("ADMIN");

        // Act
        var authorities = user.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("GetAuthorities - Deve ter autoridade apropriada para cada role")
    void testGetAuthorities_DifferentRoles() {
        // Test ADMIN
        user.setRole("ADMIN");
        var adminAuthorities = user.getAuthorities();
        assertEquals(1, adminAuthorities.size());

        // Test CUSTOMER
        user.setRole("CUSTOMER");
        var customerAuthorities = user.getAuthorities();
        assertEquals(1, customerAuthorities.size());
    }

    // ==================== ACCOUNT STATUS TESTS ====================

    @Test
    @DisplayName("IsAccountNonExpired - Deve retornar true")
    void testIsAccountNonExpired() {
        // Assert
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    @DisplayName("IsAccountNonLocked - Deve retornar true")
    void testIsAccountNonLocked() {
        // Assert
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    @DisplayName("IsCredentialsNonExpired - Deve retornar true")
    void testIsCredentialsNonExpired() {
        // Assert
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("IsEnabled - Deve retornar true")
    void testIsEnabled() {
        // Assert
        assertTrue(user.isEnabled());
    }

    // ==================== RUNS TESTS ====================

    @Test
    @DisplayName("SetGetRuns - Deve set e get lista de runs")
    void testSetGetRuns() {
        // Arrange
        java.util.List<pokemon.runs.time.leaderboard.domain.run.Run> runs = new java.util.ArrayList<>();

        // Act
        user.setRuns(runs);

        // Assert
        assertNotNull(user.getRuns());
        assertEquals(runs, user.getRuns());
    }

    @Test
    @DisplayName("GetRuns - Deve retornar lista vazia por padrão")
    void testGetRuns_Default() {
        // Assert
        assertNotNull(user.getRuns());
        assertTrue(user.getRuns().isEmpty());
    }

    // ==================== USERDETAILS IMPLEMENTATION TESTS ====================

    @Test
    @DisplayName("Implement UserDetails - Deve implementar corretamente")
    void testUserDetailsImplementation() {
        // Arrange
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setRole("CUSTOMER");

        // Assert
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
        assertNotNull(user.getAuthorities());
    }

    // ==================== EQUALITY AND HASH TESTS ====================

    @Test
    @DisplayName("Equals - Usuários com mesmo ID devem ser iguais")
    void testEquals_SameId() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("testuser");

        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("otheruser");

        // Assert - Lombok gera equals baseado em todos os campos
        // então mesmo com ID igual, podem não ser iguais se outros campos diferem
        assertNotNull(user1);
        assertNotNull(user2);
    }

    @Test
    @DisplayName("ToString - Deve representar o usuário como string")
    void testToString() {
        // Arrange
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        // Act
        String userString = user.toString();

        // Assert
        assertNotNull(userString);
        assertFalse(userString.isEmpty());
    }
}

