package com.demo.groceryorderingsystem.controller;

import com.demo.groceryorderingsystem.dto.ProductDTO;
import com.demo.groceryorderingsystem.entity.Product;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Product Controller Integration Tests (Full Integration - No Mocks)")
class ProductControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        productRepository.deleteAll();
    }

    @AfterEach
    void clearDB() {
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /api/products - Get All Products")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return all products with status 200 OK")
        void shouldReturnAllProductsSuccessfully() throws Exception {
            Product p1 = new Product("PROD001", "Apple", new BigDecimal("2.99"));
            Product p2 = new Product("PROD002", "Banana", new BigDecimal("1.99"));
            productRepository.save(p1);
            productRepository.save(p2);

            mockMvc.perform(get("/api/products")
                    .param("start", "0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].code", is("PROD001")))
                    .andExpect(jsonPath("$[0].name", is("Apple")))
                    .andExpect(jsonPath("$[0].price", is(2.99)))
                    .andExpect(jsonPath("$[1].code", is("PROD002")))
                    .andExpect(jsonPath("$[1].name", is("Banana")))
                    .andExpect(jsonPath("$[1].price", is(1.99)));
        }

        @Test
        @DisplayName("Should return empty list when no products exist")
        void shouldReturnEmptyListWhenNoProducts() throws Exception {
            mockMvc.perform(get("/api/products").param("start", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return products with pagination - start position 1")
        void shouldReturnProductsWithPaginationStartOne() throws Exception {
            for (int i = 1; i <= 15; i++) {
                Product p = new Product("PROD" + String.format("%03d", i), "Product " + i, new BigDecimal(String.valueOf(i * 1.5)));
                productRepository.save(p);
            }

            // Second page (start=1) should return 5 items
            mockMvc.perform(get("/api/products").param("start", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(5)));
        }
    }

    @Nested
    @DisplayName("GET /api/products/{code} - Get Product By Code")
    class GetProductByCodeTests {

        @Test
        @DisplayName("Should return product when code exists")
        void shouldReturnProductWhenCodeExists() throws Exception {
            Product p = new Product("PROD001", "Apple", new BigDecimal("2.99"));
            productRepository.save(p);

            mockMvc.perform(get("/api/products/PROD001")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is("PROD001")))
                    .andExpect(jsonPath("$.name", is("Apple")))
                    .andExpect(jsonPath("$.price", is(2.99)));
        }

        @Test
        @DisplayName("Should return 404 when product code does not exist")
        void shouldReturn404WhenProductNotFound() throws Exception {
            mockMvc.perform(get("/api/products/NOPE"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/products - Add New Product")
    class AddNewProductTests {

        @Test
        @DisplayName("Should create new product with status 201 Created")
        void shouldCreateNewProductSuccessfully() throws Exception {
            ProductDTO dto = new ProductDTO("PROD001", "Apple", new BigDecimal("2.99"));

            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());

            Product savedProduct = productRepository.findByCode("PROD001").orElseThrow();
            Assertions.assertThat(savedProduct.getCode()).isEqualTo("PROD001");
            Assertions.assertThat(savedProduct.getName()).isEqualTo("Apple");
            Assertions.assertThat(savedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("2.99"));
        }

        @Test
        @DisplayName("Should reject product with null code")
        void shouldRejectProductWithNullCode() throws Exception {
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Apple\",\"price\":2.99}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject product with empty name")
        void shouldRejectProductWithEmptyName() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\":\"PROD001\",\"name\":\"\",\"price\":2.99}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject product with zero price")
        void shouldRejectProductWithZeroPrice() throws Exception {
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"code\":\"PROD001\",\"name\":\"Apple\",\"price\":0}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject product with negative price")
        void shouldRejectProductWithNegativePrice() throws Exception {
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"code\":\"PROD001\",\"name\":\"Apple\",\"price\":-1}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject duplicate product code with 400 Bad Request")
        void shouldRejectDuplicateProductCode() throws Exception {
            // Arrange - First product exists
            Product p = new Product("PROD001", "Apple", new BigDecimal("2.99"));
            productRepository.save(p);

            ProductDTO dto = new ProductDTO("PROD001", "Apple Duplicate", new BigDecimal("3.49"));

            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());

            // Verify only one product exists
            List<Product> products = productRepository.findAll();
            Assertions.assertThat(products).hasSize(1);
        }
    }

    @Nested
    @DisplayName("PUT /api/products - Update Product")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update existing product with status 201 Created")
        void shouldUpdateProductSuccessfully() throws Exception {
            Product p = new Product("PROD001", "Apple", new BigDecimal("2.99"));
            productRepository.save(p);

            ProductDTO updated = new ProductDTO("PROD001", "Red Apple", new BigDecimal("3.49"));

            mockMvc.perform(put("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updated)))
                    .andExpect(status().isCreated());

            Product updatedProduct = productRepository.findByCode("PROD001").orElseThrow();
            Assertions.assertThat(updatedProduct.getName()).isEqualTo("Red Apple");
            Assertions.assertThat(updatedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("3.49"));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent product")
        void shouldReturn404WhenUpdatingNonExistent() throws Exception {
            ProductDTO updated = new ProductDTO("NOPE", "Name", new BigDecimal("1.00"));

            mockMvc.perform(put("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updated)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject update with invalid price")
        void shouldRejectUpdateWithInvalidPrice() throws Exception {
            mockMvc.perform(put("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"code\":\"PROD001\",\"name\":\"Apple\",\"price\":0}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject update with empty name")
        void shouldRejectUpdateWithEmptyName() throws Exception {
            mockMvc.perform(put("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"code\":\"PROD001\",\"name\":\"\",\"price\":2.99}"))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    @DisplayName("DELETE /api/products/{code} - Delete Product By Code")
    class DeleteProductByCodeTests {

        @Test
        @DisplayName("Should delete product with status 201 Created")
        void shouldDeleteProductSuccessfully() throws Exception {
            // Arrange
            Product p = new Product("PROD001", "Apple", new BigDecimal("2.99"));
            productRepository.save(p);
            Assertions.assertThat(productRepository.existsByCode("PROD001")).isTrue();

            // Act
            mockMvc.perform(delete("/api/products/PROD001")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            // Assert - Verify product is deleted from DB
            Assertions.assertThat(productRepository.existsByCode("PROD001")).isFalse();
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent product code")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            mockMvc.perform(delete("/api/products/PROD999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Integration Tests - Full Workflow")
    class FullWorkflowTests {

        @Test
        @DisplayName("Should maintain data integrity across multiple operations")
        void shouldMaintainDataIntegrityAcrossOperations() throws Exception {
            // Create 3 products
            for (int i = 1; i <= 3; i++) {
                ProductDTO dto = new ProductDTO("PROD" + String.format("%03d", i), "Product " + i, new BigDecimal(String.valueOf(i * 1.5)));
                mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isCreated());
            }

            // Verify all created
            mockMvc.perform(get("/api/products").param("start", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));

            // Update one
            ProductDTO updateDto = new ProductDTO("PROD002", "Product 2 Updated", new BigDecimal("5.00"));
            mockMvc.perform(put("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isCreated());

            // Delete one
            mockMvc.perform(delete("/api/products/PROD001"))
                    .andExpect(status().isCreated());

            // Verify final state
            mockMvc.perform(get("/api/products").param("start", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));

            // Verify correct products exist
            mockMvc.perform(get("/api/products/PROD002"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Product 2 Updated")))
                    .andExpect(jsonPath("$.price", is(5.00)));

            mockMvc.perform(get("/api/products/PROD003"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/products/PROD001"))
                    .andExpect(status().isNotFound());
        }
    }
}
