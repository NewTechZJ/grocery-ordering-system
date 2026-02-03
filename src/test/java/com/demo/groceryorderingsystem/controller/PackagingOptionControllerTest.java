package com.demo.groceryorderingsystem.controller;

import com.demo.groceryorderingsystem.dto.PackagingOptionDTO;
import com.demo.groceryorderingsystem.entity.PackagingOption;
import com.demo.groceryorderingsystem.entity.Product;
import com.demo.groceryorderingsystem.repository.PackagingOptionRepository;
import com.demo.groceryorderingsystem.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.assertj.core.api.Assertions;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Packaging Option Controller Integration Tests (Full Integration - No Mocks)")
class PackagingOptionControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PackagingOptionRepository packagingOptionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    // Shared test products
    private static Product product1;
    private static Product product2;
    private static Product product3;

    @BeforeAll
    static void setupProducts(@Autowired ProductRepository repository) {
        // Create products once for all tests
        product1 = new Product("PROD001", "Apple", new BigDecimal("2.99"));
        product2 = new Product("PROD002", "Banana", new BigDecimal("1.99"));
        product3 = new Product("PROD003", "Orange", new BigDecimal("3.49"));

        repository.save(product1);
        repository.save(product2);
        repository.save(product3);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        // Only clear packaging options before each test, not products
        packagingOptionRepository.deleteAll();
    }

    @AfterEach
    void clearPackagingOptions() {
        // Only clear packaging options after each test
        packagingOptionRepository.deleteAll();
    }

    @AfterAll
    static void cleanupProducts(@Autowired ProductRepository repository) {
        // Clean up products after all tests
        repository.deleteAll();
    }

    @Nested
    @DisplayName("GET /api/packaging - Get All Packaging Options")
    class GetAllPackagingOptionsTests {

        @Test
        @DisplayName("Should return all packaging options with status 200 OK")
        void shouldReturnAllPackagingOptionsSuccessfully() throws Exception {
            // Create packaging options
            PackagingOption opt1 = new PackagingOption(product1, 6, new BigDecimal("15.99"));
            PackagingOption opt2 = new PackagingOption(product2, 12, new BigDecimal("19.99"));
            packagingOptionRepository.save(opt1);
            packagingOptionRepository.save(opt2);

            mockMvc.perform(get("/api/packaging")
                    .param("start", "0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].productCode", is("PROD001")))
                    .andExpect(jsonPath("$[0].quantity", is(6)))
                    .andExpect(jsonPath("$[0].price", is(15.99)))
                    .andExpect(jsonPath("$[1].quantity", is(12)))
                    .andExpect(jsonPath("$[1].price", is(19.99)));
        }

        @Test
        @DisplayName("Should return empty list when no packaging options exist")
        void shouldReturnEmptyListWhenNoPackagingOptions() throws Exception {
            mockMvc.perform(get("/api/packaging").param("start", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return packaging options with pagination - start position 1")
        void shouldReturnPackagingOptionsWithPaginationStartOne() throws Exception {
            // Create 15 packaging options
            for (int i = 1; i <= 15; i++) {
                PackagingOption opt = new PackagingOption(product1, i, new BigDecimal(String.valueOf(i * 2.5)));
                packagingOptionRepository.save(opt);
            }

            // Second page (start=1) should return 5 items
            mockMvc.perform(get("/api/packaging").param("start", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(5)));
        }
    }

    @Nested
    @DisplayName("GET /api/packaging/{productCode} - Get Packaging Options By Product")
    class GetPackagingOptionsByProductTests {

        @Test
        @DisplayName("Should return packaging options for a product")
        void shouldReturnPackagingOptionsForProduct() throws Exception {
            // Create packaging options
            PackagingOption opt1 = new PackagingOption(product1, 6, new BigDecimal("15.99"));
            PackagingOption opt2 = new PackagingOption(product1, 12, new BigDecimal("28.99"));
            packagingOptionRepository.save(opt1);
            packagingOptionRepository.save(opt2);

            mockMvc.perform(get("/api/packaging/PROD001")
                    .param("start", "0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].productCode", is("PROD001")))
                    .andExpect(jsonPath("$[0].quantity", is(6)));
        }

        @Test
        @DisplayName("Should return empty list when no packaging options exist for product")
        void shouldReturnEmptyListWhenNoPackagingOptionsForProduct() throws Exception {
            mockMvc.perform(get("/api/packaging/PROD001").param("start", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 404 when product does not exist")
        void shouldReturn404WhenProductNotFound() throws Exception {
            mockMvc.perform(get("/api/packaging/NONEXISTENT")
                    .param("start", "0"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return packaging options with pagination")
        void shouldReturnPackagingOptionsWithPaginationByProduct() throws Exception {
            // Create 15 packaging options
            for (int i = 1; i <= 15; i++) {
                PackagingOption opt = new PackagingOption(product1, i, new BigDecimal(String.valueOf(i * 2.5)));
                packagingOptionRepository.save(opt);
            }

            // Second page (start=1) should return 5 items
            mockMvc.perform(get("/api/packaging/PROD001").param("start", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(5)));
        }
    }

    @Nested
    @DisplayName("POST /api/packaging - Create Packaging Option")
    class CreatePackagingOptionTests {

        @Test
        @DisplayName("Should create new packaging option with status 201 Created")
        void shouldCreateNewPackagingOptionSuccessfully() throws Exception {
            PackagingOptionDTO dto = new PackagingOptionDTO(null, "PROD001", 6, new BigDecimal("15.99"));

            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should reject packaging option with non-existent product code")
        void shouldRejectPackagingOptionWithNonExistentProduct() throws Exception {
            PackagingOptionDTO dto = new PackagingOptionDTO(null, "NONEXISTENT", 6, new BigDecimal("15.99"));

            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound());

        }

        @Test
        @DisplayName("Should reject packaging option with null product code")
        void shouldRejectPackagingOptionWithNullProductCode() throws Exception {
            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"quantity\":6,\"price\":15.99}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject packaging option with null quantity")
        void shouldRejectPackagingOptionWithNullQuantity() throws Exception {
            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productCode\":\"PROD001\",\"price\":15.99}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject packaging option with zero quantity")
        void shouldRejectPackagingOptionWithZeroQuantity() throws Exception {
            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productCode\":\"PROD001\",\"quantity\":0,\"price\":15.99}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject packaging option with negative quantity")
        void shouldRejectPackagingOptionWithNegativeQuantity() throws Exception {
            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productCode\":\"PROD001\",\"quantity\":-1,\"price\":15.99}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject packaging option with zero price")
        void shouldRejectPackagingOptionWithZeroPrice() throws Exception {
            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productCode\":\"PROD001\",\"quantity\":6,\"price\":0}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject packaging option with negative price")
        void shouldRejectPackagingOptionWithNegativePrice() throws Exception {
            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productCode\":\"PROD001\",\"quantity\":6,\"price\":-1}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject duplicate packaging option (same product code and quantity)")
        void shouldRejectDuplicatePackagingOption() throws Exception {
            // Create first packaging option
            PackagingOption opt = new PackagingOption(product1, 6, new BigDecimal("15.99"));
            packagingOptionRepository.save(opt);

            // Try to create duplicate
            PackagingOptionDTO dto = new PackagingOptionDTO(null, "PROD001", 6, new BigDecimal("20.00"));

            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should create multiple packaging options for same product")
        void shouldCreateMultiplePackagingOptionsForSameProduct() throws Exception {
            // Create first packaging option
            PackagingOptionDTO dto1 = new PackagingOptionDTO(null, "PROD001", 6, new BigDecimal("15.99"));
            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto1)))
                    .andExpect(status().isCreated());

            // Create second packaging option with different quantity
            PackagingOptionDTO dto2 = new PackagingOptionDTO(null, "PROD001", 12, new BigDecimal("28.99"));
            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto2)))
                    .andExpect(status().isCreated());

            Assertions.assertThat(packagingOptionRepository.findAll()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("DELETE /api/packaging/{id} - Delete Packaging Option")
    class DeletePackagingOptionTests {

        @Test
        @DisplayName("Should delete packaging option with status 204 No Content")
        void shouldDeletePackagingOptionSuccessfully() throws Exception {
            PackagingOption opt = new PackagingOption(product1, 6, new BigDecimal("15.99"));
            packagingOptionRepository.save(opt);
            UUID optionId = opt.getId();

            Assertions.assertThat(packagingOptionRepository.existsById(optionId)).isTrue();

            mockMvc.perform(delete("/api/packaging/" + optionId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            // Verify deleted
            Assertions.assertThat(packagingOptionRepository.existsById(optionId)).isFalse();
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent packaging option")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(delete("/api/packaging/" + nonExistentId))
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    @DisplayName("Integration Tests - Full Workflow")
    class FullWorkflowTests {

        @Test
        @DisplayName("Should maintain data integrity across multiple operations")
        void shouldMaintainDataIntegrityAcrossOperations() throws Exception {
            // Create multiple packaging options
            PackagingOptionDTO dto1 = new PackagingOptionDTO(null, "PROD001", 6, new BigDecimal("15.99"));
            PackagingOptionDTO dto2 = new PackagingOptionDTO(null, "PROD001", 12, new BigDecimal("28.99"));
            PackagingOptionDTO dto3 = new PackagingOptionDTO(null, "PROD001", 24, new BigDecimal("55.99"));

            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto2)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/packaging")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto3)))
                    .andExpect(status().isCreated());

            // Verify all created
            mockMvc.perform(get("/api/packaging/PROD001").param("start", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));

            // Delete one
            UUID optionToDelete = packagingOptionRepository.findAll().get(0).getId();
            mockMvc.perform(delete("/api/packaging/" + optionToDelete))
                    .andExpect(status().isNoContent());

            // Verify final state
            mockMvc.perform(get("/api/packaging/PROD001").param("start", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));

            // Verify correct options remain
            List<PackagingOption> remaining = packagingOptionRepository.findAll();
            Assertions.assertThat(remaining).hasSize(2);
            Assertions.assertThat(remaining.stream().map(PackagingOption::getQuantity).toList())
                    .containsExactlyInAnyOrder(12, 24);
        }
    }
}
