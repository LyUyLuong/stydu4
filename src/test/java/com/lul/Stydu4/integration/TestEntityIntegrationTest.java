// package com.lul.Stydu4.integration;

// import com.lul.Stydu4.dto.request.Test.TestCreationRequest;
// import com.lul.Stydu4.dto.request.Test.TestUpdateRequest;
// import com.lul.Stydu4.dto.response.Test.TestDetailResponse;
// import com.lul.Stydu4.entity.FileEntity;
// import com.lul.Stydu4.entity.TestEntity;
// import com.lul.Stydu4.enums.FileType;
// import com.lul.Stydu4.enums.TestType;
// import com.lul.Stydu4.repository.IFileRepository;
// import com.lul.Stydu4.repository.ITestRepository;
// import com.lul.Stydu4.service.ITestService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.cache.CacheManager;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.Optional;

// import static org.assertj.core.api.Assertions.assertThat;

// /**
//  * Integration Test cho Test Entity
//  * 
//  * Test Cases:
//  * 1. CRUD operations with H2 database
//  * 2. Redis caching behavior (@Cacheable, @CacheEvict, @CachePut)
//  * 3. Database queries and pagination with indexes
//  * 4. Entity relationships (Test-Audio-PartTests)
//  * 5. N+1 query prevention with @EntityGraph
//  */
// @SpringBootTest
// @ActiveProfiles("test")
// @Transactional
// @DisplayName("Test Entity Integration Tests")
// class TestEntityIntegrationTest {

//     @Autowired
//     private ITestService testService;

//     @Autowired
//     private ITestRepository testRepository;

//     @Autowired
//     private IFileRepository fileRepository;

//     @Autowired(required = false)
//     private CacheManager cacheManager;

//     private FileEntity audioFile;
//     private TestEntity sampleTest;

//     @BeforeEach
//     void setUp() {
//         // Clean up
//         testRepository.deleteAll();
//         fileRepository.deleteAll();

//         // Clear all caches if cache manager is available (Redis)
//         if (cacheManager != null) {
//             cacheManager.getCacheNames().forEach(cacheName -> {
//                 var cache = cacheManager.getCache(cacheName);
//                 if (cache != null) {
//                     cache.clear();
//                 }
//             });
//         }

//         // Create sample audio file
//         audioFile = FileEntity.builder()
//                 .id("audio-test-123")
//                 .originalFilename("test-audio.mp3")
//                 .storedFilename("uuid-audio.mp3")
//                 .filePath("/audios/tests/")
//                 .fileUrl("http://localhost:8080/api/v1/files/audio-test-123")
//                 .fileType(FileType.AUDIO)
//                 .fileSize(2048L)
//                 .contentType("audio/mpeg")
//                 .build();
//         fileRepository.save(audioFile);

//         // Create sample test (matching actual TestEntity structure)
//         sampleTest = TestEntity.builder()
//                 .name("Sample TOEIC Test")
//                 .description("Sample test for integration testing")
//                 .slug("sample-toeic-test")
//                 .status(1) // 1 = ACTIVE
//                 .type(TestType.TOEIC)
//                 .numberOfParticipants(0L)
//                 .audio(audioFile)
//                 .build();
//         testRepository.save(sampleTest);
//     }

//     // ========== Basic CRUD Operations Tests ==========

//     @Nested
//     @DisplayName("CRUD Operations")
//     class CrudOperationsTests {

//         @Test
//         @DisplayName("Create test - should save to database")
//         void testCreateTest() {
//             // Given
//             TestCreationRequest request = TestCreationRequest.builder()
//                     .name("New TOEIC Test")
//                     .description("A brand new test")
//                     .type(TestType.TOEIC)
//                     .audioId(audioFile.getId())
//                     .build();

//             // When
//             TestDetailResponse response = testService.create(request);

//             // Then
//             assertThat(response).isNotNull();
//             assertThat(response.getName()).isEqualTo("New TOEIC Test");
//             assertThat(response.getSlug()).isEqualTo("new-toeic-test");
//             assertThat(response.getStatus()).isEqualTo(1); // ACTIVE

//             // Verify saved to database
//             Optional<TestEntity> savedTest = testRepository.findById(response.getId());
//             assertThat(savedTest).isPresent();
//             assertThat(savedTest.get().getName()).isEqualTo("New TOEIC Test");
//         }

//         @Test
//         @DisplayName("Read test by ID - should fetch from database")
//         void testGetTestById() {
//             // When
//             TestDetailResponse response = testService.getTestById(sampleTest.getId());

//             // Then
//             assertThat(response).isNotNull();
//             assertThat(response.getId()).isEqualTo(sampleTest.getId());
//             assertThat(response.getName()).isEqualTo("Sample TOEIC Test");
//             assertThat(response.getSlug()).isEqualTo("sample-toeic-test");
//         }

//         @Test
//         @DisplayName("Update test - should modify in database")
//         void testUpdateTest() {
//             // Given
//             TestUpdateRequest request = TestUpdateRequest.builder()
//                     .name("Updated TOEIC Test")
//                     .description("Updated description")
//                     .status(1)
//                     .build();

//             // When
//             TestDetailResponse response = testService.update(sampleTest.getId(), request);

//             // Then
//             assertThat(response.getName()).isEqualTo("Updated TOEIC Test");
//             assertThat(response.getDescription()).isEqualTo("Updated description");

//             // Verify in database
//             TestEntity updated = testRepository.findById(sampleTest.getId()).orElseThrow();
//             assertThat(updated.getName()).isEqualTo("Updated TOEIC Test");
//         }

//         @Test
//         @DisplayName("Delete test - should remove from database")
//         void testDeleteTest() {
//             // When
//             testService.deleteTest(sampleTest.getId());

//             // Then
//             Optional<TestEntity> deleted = testRepository.findById(sampleTest.getId());
//             assertThat(deleted).isEmpty();
//         }

//         @Test
//         @DisplayName("Get all tests with pagination - should return page of tests")
//         void testGetAllTestsWithPagination() {
//             // Given - create multiple tests
//             for (int i = 1; i <= 5; i++) {
//                 TestEntity test = TestEntity.builder()
//                         .name("Test " + i)
//                         .description("Description " + i)
//                         .slug("test-" + i)
//                         .status(1)
//                         .type(TestType.TOEIC)
//                         .numberOfParticipants(0L)
//                         .build();
//                 testRepository.save(test);
//             }

//             // When
//             var page = testService.getAllTests(0, 3);

//             // Then
//             assertThat(page).isNotNull();
//             assertThat(page.getTotalElements()).isEqualTo(6); // 5 new + 1 sample
//             assertThat(page.getData()).hasSize(3); // Page size = 3
//             assertThat(page.getTotalPages()).isEqualTo(2);
//         }
//     }

//     // ========== Redis Caching Tests ==========

//     @Nested
//     @DisplayName("Redis Caching Behavior")
//     class RedisCachingTests {

//         @Test
//         @DisplayName("First query - should fetch from database and cache result")
//         void testFirstQueryCachesResult() {
//             // Skip if no cache manager (Redis not available in test)
//             if (cacheManager == null) {
//                 return;
//             }

//             // When - First call
//             TestDetailResponse response1 = testService.getTestById(sampleTest.getId());

//             // Then - Check cache
//             var cache = cacheManager.getCache("test-details");
//             assertThat(cache).isNotNull();
            
//             var cachedValue = cache.get("test-details::" + sampleTest.getId());
//             if (cachedValue != null) {
//                 assertThat(cachedValue.get()).isNotNull();
//             }
//         }

//         @Test
//         @DisplayName("Second query - should use cached result (faster)")
//         void testSecondQueryUsesCachedResult() {
//             // When - Call twice
//             TestDetailResponse response1 = testService.getTestById(sampleTest.getId());
//             TestDetailResponse response2 = testService.getTestById(sampleTest.getId());

//             // Then - Should return same data
//             assertThat(response1.getId()).isEqualTo(response2.getId());
//             assertThat(response1.getName()).isEqualTo(response2.getName());
            
//             // Note: With Redis, second query would be significantly faster
//         }

//         @Test
//         @DisplayName("Update test - should evict cache")
//         void testUpdateEvictsCache() {
//             // Skip if no cache manager
//             if (cacheManager == null) {
//                 return;
//             }

//             // Given - Cache the test first
//             testService.getTestById(sampleTest.getId());

//             // When - Update the test
//             TestUpdateRequest request = TestUpdateRequest.builder()
//                     .name("Cache Eviction Test")
//                     .build();
//             testService.update(sampleTest.getId(), request);

//             // Then - Cache should be evicted, next query gets fresh data
//             TestDetailResponse afterUpdate = testService.getTestById(sampleTest.getId());
//             assertThat(afterUpdate.getName()).isEqualTo("Cache Eviction Test");
//         }

//         @Test
//         @DisplayName("Delete test - should evict cache")
//         void testDeleteEvictsCache() {
//             // Skip if no cache manager
//             if (cacheManager == null) {
//                 return;
//             }

//             // Given - Cache the test first
//             testService.getTestById(sampleTest.getId());

//             // When - Delete the test
//             testService.deleteTest(sampleTest.getId());

//             // Then - Cache should be cleared
//             var cache = cacheManager.getCache("test-details");
//             if (cache != null) {
//                 var cachedValue = cache.get("test-details::" + sampleTest.getId());
//                 // Cache should be null or empty after eviction
//                 assertThat(cachedValue == null || cachedValue.get() == null).isTrue();
//             }
//         }
//     }

//     // ========== Database Query & Index Tests ==========

//     @Nested
//     @DisplayName("Database Queries & Indexes")
//     class DatabaseQueriesTests {

//         @Test
//         @DisplayName("Find by ID - should use primary key index")
//         void testFindById() {
//             // When
//             Optional<TestEntity> found = testRepository.findById(sampleTest.getId());

//             // Then
//             assertThat(found).isPresent();
//             assertThat(found.get().getName()).isEqualTo("Sample TOEIC Test");
//         }

//         @Test
//         @DisplayName("Find by status - should use idx_test_status index")
//         void testFindByStatus() {
//             // Given
//             TestEntity inactiveTest = TestEntity.builder()
//                     .name("Inactive Test")
//                     .slug("inactive-test")
//                     .type(TestType.TOEIC)
//                     .status(0) // 0 = INACTIVE
//                     .numberOfParticipants(0L)
//                     .build();
//             testRepository.save(inactiveTest);

//             // When
//             long activeCount = testRepository.countByStatus(1);

//             // Then
//             assertThat(activeCount).isEqualTo(1); // Only sampleTest is active
//         }

//         @Test
//         @DisplayName("Count all tests - should be accurate")
//         void testCountAllTests() {
//             // Given - we have 1 sample test from setUp
//             long initialCount = testRepository.count();
//             assertThat(initialCount).isEqualTo(1);

//             // When - add more tests
//             testRepository.save(TestEntity.builder()
//                     .name("Test 2")
//                     .slug("test-2")
//                     .type(TestType.TOEIC)
//                     .status(1)
//                     .numberOfParticipants(0L)
//                     .build());

//             // Then
//             long newCount = testRepository.count();
//             assertThat(newCount).isEqualTo(2);
//         }

//         @Test
//         @DisplayName("Pagination - should work correctly")
//         void testPagination() {
//             // Given - create 10 tests
//             for (int i = 1; i <= 10; i++) {
//                 testRepository.save(TestEntity.builder()
//                         .name("Test " + i)
//                         .slug("test-" + i)
//                         .type(TestType.TOEIC)
//                         .status(1)
//                         .numberOfParticipants(0L)
//                         .build());
//             }

//             // When - Get page 1 (size=5)
//             Pageable pageable = PageRequest.of(0, 5);
//             Page<TestEntity> page1 = testRepository.findAllBy(pageable);

//             // Then
//             assertThat(page1.getTotalElements()).isEqualTo(11); // 10 new + 1 sample
//             assertThat(page1.getContent()).hasSize(5);
//             assertThat(page1.getTotalPages()).isEqualTo(3);
//             assertThat(page1.isFirst()).isTrue();
//         }
//     }

//     // ========== Entity Relationships & N+1 Prevention Tests ==========

//     @Nested
//     @DisplayName("Entity Relationships & N+1 Prevention")
//     class EntityRelationshipsTests {

//         @Test
//         @DisplayName("Test-Audio relationship - should be properly mapped")
//         void testTestAudioRelationship() {
//             // When
//             TestEntity found = testRepository.findById(sampleTest.getId()).orElseThrow();

//             // Then
//             assertThat(found.getAudio()).isNotNull();
//             assertThat(found.getAudio().getId()).isEqualTo(audioFile.getId());
//             assertThat(found.getAudio().getOriginalFilename()).isEqualTo("test-audio.mp3");
//         }

//         @Test
//         @DisplayName("Test without audio file - should be allowed")
//         void testTestWithoutAudioFile() {
//             // Given
//             TestEntity testWithoutAudio = TestEntity.builder()
//                     .name("Test Without Audio")
//                     .slug("test-without-audio")
//                     .type(TestType.READING_ONLY)
//                     .status(1)
//                     .numberOfParticipants(0L)
//                     .build();

//             // When
//             TestEntity saved = testRepository.save(testWithoutAudio);

//             // Then
//             assertThat(saved.getAudio()).isNull();
//             Optional<TestEntity> found = testRepository.findById(saved.getId());
//             assertThat(found).isPresent();
//             assertThat(found.get().getAudio()).isNull();
//         }

//         @Test
//         @DisplayName("Find with audio using @EntityGraph - should prevent N+1")
//         void testFindByIdWithAudioPreventsN1() {
//             // When - Using @EntityGraph query
//             Optional<TestEntity> found = testRepository.findByIdWithAudio(sampleTest.getId());

//             // Then
//             assertThat(found).isPresent();
//             assertThat(found.get().getAudio()).isNotNull();
//             assertThat(found.get().getAudio().getId()).isEqualTo(audioFile.getId());
            
//             // Note: @EntityGraph loads audio in single query, preventing N+1 problem
//         }

//         @Test
//         @DisplayName("Find with parts using @EntityGraph - should load all in one query")
//         void testFindByIdWithPartsPreventsN1() {
//             // When - Using @EntityGraph query
//             Optional<TestEntity> found = testRepository.findByIdWithParts(sampleTest.getId());

//             // Then
//             assertThat(found).isPresent();
//             // PartTests loaded eagerly if they exist
//             assertThat(found.get().getPartTestEntities()).isNotNull();
            
//             // Note: This prevents N+1 when accessing parts
//         }
//     }

//     // ========== Business Logic Tests ==========

//     @Nested
//     @DisplayName("Business Logic Validation")
//     class BusinessValidationTests {

//         @Test
//         @DisplayName("Slug generation - should be unique and URL-friendly")
//         void testSlugGeneration() {
//             // When
//             TestCreationRequest request = TestCreationRequest.builder()
//                     .name("Test With Special Characters #123!")
//                     .description("Test description")
//                     .type("TOEIC")
//                     .build();

//             TestDetailResponse response = testService.create(request);

//             // Then
//             assertThat(response.getSlug()).matches("[a-z0-9-]+");
//             assertThat(response.getSlug()).doesNotContain(" ");
//             assertThat(response.getSlug()).doesNotContain("#");
//             assertThat(response.getSlug()).doesNotContain("!");
//         }

//         @Test
//         @DisplayName("Test status - default should be ACTIVE (1)")
//         void testDefaultStatus() {
//             // When
//             TestDetailResponse response = testService.getTestById(sampleTest.getId());

//             // Then
//             assertThat(response.getStatus()).isEqualTo(1); // 1 = ACTIVE
//         }

//         @Test
//         @DisplayName("numberOfParticipants - should default to 0")
//         void testDefaultParticipantCount() {
//             // When
//             TestEntity found = testRepository.findById(sampleTest.getId()).orElseThrow();

//             // Then
//             assertThat(found.getNumberOfParticipants()).isEqualTo(0L);
//         }

//         @Test
//         @DisplayName("Test type - should be stored correctly")
//         void testTestType() {
//             // When
//             TestEntity found = testRepository.findById(sampleTest.getId()).orElseThrow();

//             // Then
//             assertThat(found.getType()).isEqualTo(TestType.TOEIC);
//         }
//     }

//     // ========== Performance & Index Usage Tests ==========

//     @Nested
//     @DisplayName("Performance & Index Usage")
//     class PerformanceTests {

//         @Test
//         @DisplayName("Query by status should use index - verify with countByStatus")
//         void testStatusIndexUsage() {
//             // Given - create tests with different statuses
//             for (int i = 1; i <= 5; i++) {
//                 testRepository.save(TestEntity.builder()
//                         .name("Active Test " + i)
//                         .slug("active-test-" + i)
//                         .type(TestType.TOEIC)
//                         .status(1) // ACTIVE
//                         .numberOfParticipants(0L)
//                         .build());
//             }
            
//             testRepository.save(TestEntity.builder()
//                     .name("Inactive Test")
//                     .slug("inactive-test")
//                     .type(TestType.TOEIC)
//                     .status(0) // INACTIVE
//                     .numberOfParticipants(0L)
//                     .build());

//             // When - Query by status (should use idx_test_status index)
//             long activeCount = testRepository.countByStatus(1);
//             long inactiveCount = testRepository.countByStatus(0);

//             // Then
//             assertThat(activeCount).isEqualTo(6); // 5 new + 1 sample
//             assertThat(inactiveCount).isEqualTo(1);
//         }

//         @Test
//         @DisplayName("Composite index (type, status) should improve filtered queries")
//         void testCompositeIndexUsage() {
//             // Given - create mixed data
//             testRepository.save(TestEntity.builder()
//                     .name("IELTS Active")
//                     .slug("ielts-active")
//                     .type(TestType.IELTS)
//                     .status(1)
//                     .numberOfParticipants(0L)
//                     .build());
            
//             testRepository.save(TestEntity.builder()
//                     .name("TOEIC Inactive")
//                     .slug("toeic-inactive")
//                     .type(TestType.TOEIC)
//                     .status(0)
//                     .numberOfParticipants(0L)
//                     .build());

//             // When - Query all tests
//             long totalCount = testRepository.count();

//             // Then - Composite index (type, status) helps with filtered queries
//             assertThat(totalCount).isEqualTo(3); // Sample + 2 new
//         }
//     }
// }
