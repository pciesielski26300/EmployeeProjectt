package repository;

import org.example.exception.EmployeeRepositoryException;
import org.example.model.Person;
import org.example.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class EmployeeRepositoryTest {

    private final String TEST_INTERNAL_DIR_PATH = "src/test/resources/internal";
    private final String TEST_EXTERNAL_DIR_PATH = "src/test/resources/external";

    private EmployeeRepository underTest;

    @BeforeEach
    void setUp() {
        underTest = new EmployeeRepository(TEST_INTERNAL_DIR_PATH, TEST_EXTERNAL_DIR_PATH);
        deleteDirectoryContents(TEST_INTERNAL_DIR_PATH);
        deleteDirectoryContents(TEST_EXTERNAL_DIR_PATH);
    }

    @Test
    void testThatEmployeeWithInternalDirectoryWasCreatedSuccessfully() {
        // Given
        Person newEmployee = new Person("123", "John", "Doe", "123456789", "john@example.com", "12345", true);

        // When
        underTest.create(newEmployee);

        // Then
        String filePath = TEST_INTERNAL_DIR_PATH + File.separator + "123.xml";
        assertTrue(new File(filePath).exists());
    }

    @Test
    void testThatEmployeeWithExternalDirectoryWasCreatedSuccessfully() {
        // Given
        File directory = new File(TEST_EXTERNAL_DIR_PATH);
        directory.mkdirs();
        Person newEmployee = new Person("456", "Jane", "Smith", "987654321", "jane@example.com", "54321", false);

        // When
        underTest.create(newEmployee);

        // Then
        String filePath = TEST_EXTERNAL_DIR_PATH + File.separator + "456.xml";
        assertTrue(new File(filePath).exists());
    }

    @Test
    void testThatEmployeeWasCreatedSuccessfullyInExistingInternalDir() {
        // Given
        File directory = new File(TEST_INTERNAL_DIR_PATH);
        directory.mkdirs();
        Person newEmployee = new Person("123", "John", "Doe", "123456789", "john@example.com", "12345", true);

        // When
        underTest.create(newEmployee);

        // Then
        String filePath = TEST_INTERNAL_DIR_PATH + File.separator + "123.xml";
        assertTrue(new File(filePath).exists());
    }

    @Test
    void testThatEmployeeWasCreatedSuccessfullyInExistingExternalDir() {
        // Given
        Person newEmployee = new Person("123", "John", "Doe", "123456789", "john@example.com", "12345", true);

        // When
        underTest.create(newEmployee);

        // Then
        String filePath = TEST_INTERNAL_DIR_PATH + File.separator + "123.xml";
        assertTrue(new File(filePath).exists());
    }

    @Test
    void testThatEmployeeWithNotUniqueIdWasNotCreated() {
        // Given
        Person employee = new Person("1", "John", "Doe", "123456789", "john@example.com", "123456789", true);
        underTest.create(employee);
        Person duplicatedEmployee = new Person("1", "Jane", "Doe", "123456789", "jane@example.com", "987654321", true);

        // Then
        assertThrows(EmployeeRepositoryException.class, () -> underTest.create(duplicatedEmployee));
    }

    @Test
    void testThatFindByIdWorksCorrect() {
        // Given
        Person employee = new Person("1", "John", "Doe", "123456789", "john@example.com", "123456789", true);
        underTest.create(employee);

        // When
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("personId", "1");
        List<Person> foundEmployees = underTest.find(searchCriteria);

        // Then
        assertEquals(1, foundEmployees.size());
        assertEquals(employee, foundEmployees.get(0));
    }

    @Test
    void testThatFindByTwoParametersWorksCorrect() {
        // Given
        Person employee1 = new Person("1", "John", "Doe", "123456789", "john@example.com", "123456789", true);
        Person employee2 = new Person("2", "Jane", "Smith", "123456789", "jane@example.com", "987654321", true);
        Person employee3 = new Person("3", "John", "Smith", "123456789", "john@example.com", "123456789", true);
        underTest.create(employee1);
        underTest.create(employee2);
        underTest.create(employee3);

        // When
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("lastName", "Smith");
        searchCriteria.put("mobile", "123456789");
        List<Person> foundEmployees = underTest.find(searchCriteria);

        // Then
        assertEquals(2, foundEmployees.size());
        assertTrue(foundEmployees.contains(employee2));
        assertTrue(foundEmployees.contains(employee3));
    }

    @Test
    void testThatFindByNullInSearchCriteriaReturnsEmptyArray() {
        // Given
        Person employee = new Person("1", "John", "Doe", "123456789", "john@example.com", "123456789", true);
        underTest.create(employee);

        // When
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("personId", null);
        List<Person> foundEmployees = underTest.find(searchCriteria);

        // Then
        assertEquals(0, foundEmployees.size());
    }

    @Test
    void testThatDeleteWorksCorrect() {
        // Given
        Person employee = new Person("1", "John", "Doe", "123456789", "john@example.com", "123456789", true);
        underTest.create(employee);

        // When
        underTest.delete("1");

        // Then
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("personId", "1");
        List<Person> foundEmployees = underTest.find(searchCriteria);
        assertEquals(0, foundEmployees.size());
    }

    @Test
    void testThatDeleteNonExistingIdThrowsException() {
        // Given
        // When
        // Then
        assertThrows(EmployeeRepositoryException.class, () -> underTest.delete(""));
    }

    @Test
    void testModifyExistingEmployee() {
        // Given
        Person existingEmployee = new Person("1", "John", "Doe", "123456789", "john@example.com", "123456789", true);
        underTest.create(existingEmployee);

        // When
        Person modifiedEmployee = new Person("1", "Jane", "Doe", "987654321", "jane@example.com", "987654321", true);
        underTest.update(modifiedEmployee);

        // Then
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("personId", "1");
        Person foundEmployee = underTest.find(searchCriteria).get(0);
        assertEquals(modifiedEmployee, foundEmployee);
    }

    @Test
    void testModifyNonExistingEmployee() {
        // Given
        Person nonExistingEmployee = new Person("2", "John", "Doe", "123456789", "john@example.com", "123456789", true);

        // When, Then
        assertThrows(EmployeeRepositoryException.class, () -> underTest.update(nonExistingEmployee));
    }


    private void deleteDirectoryContents(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }
}
