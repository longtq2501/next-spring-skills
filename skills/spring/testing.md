# Skill: Backend Testing - Spring Boot

Guidelines for implementing a robust testing pyramid (Unit, Integration, and API tests).

## TL;DR - Quick Reference

### Critical Rules
1. **Testing Pyramid**: Focus on many Unit tests, fewer Integration tests, and even fewer E2E tests.
2. **First Law**: Tests must be **Fast**, **Independent**, **Repeatable**, **Self-validating**, and **Timely**.
3. **Mocking**: Use `@Mock` and `@InjectMocks` for unit tests. Never use `@SpringBootTest` for unit tests.
4. **Data Isolation**: Always use `@DataJpaTest` for repository testing and avoid modifying production databases.
5. **Assertions**: Use **AssertJ** (`assertThat`) for readable and powerful assertions.

---

## 1. Unit Testing (JUnit 5 + Mockito)
Test business logic in isolation. **No Spring context loaded.**

// Good: Testing Service logic with Mocks
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository repo;
    
    @InjectMocks
    private ProductService service;

    @Test
    void shouldCalculateTaxCorrect() {
        // Given
        Product p = new Product(100.0);
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        // When
        double result = service.getTaxedPrice(1L);

        // Then
        assertThat(result).isEqualTo(110.0);
        verify(repo).findById(1L);
    }
}

---

## 2. Slice/API Testing (@WebMvcTest)
Testing controllers without booting the whole application. Uses **MockMvc**.

// Good: Testing Controller endpoints
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService service;

    @Test
    void shouldReturnProductList() throws Exception {
        when(service.getAll()).thenReturn(List.of(new ProductDTO("PC")));

        mockMvc.perform(get("/api/products"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].name").value("PC"));
    }
}

---

## 3. Integration Testing (@SpringBootTest)
Testing the full application stack, including database and security.

// Good: Integration test with Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateOrderRealInDB() {
        OrderRequest req = new OrderRequest("User1", 500);
        ResponseEntity<OrderResponse> res = restTemplate.postForEntity("/api/orders", req, OrderResponse.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody().getId()).isNotNull();
    }
}

---

## 4. Best Practices Checklist
- **Coverage**: Aim for 80%+ line coverage in Services, but focus on the "Happy Path" and "Edge Cases" rather than just the number.
- **Naming**: Use `should_DoSomething_When_Scenario` or `methodName_stateUnderTest_expectedBehavior`.
- **No Flaky Tests**: Avoid tests that depend on system time, random numbers, or network latency without mocking.

---

## Related Skills
- **Service Design**: `skills/spring/service_design.md`
- **Error Handling**: `skills/spring/error_handling.md`
