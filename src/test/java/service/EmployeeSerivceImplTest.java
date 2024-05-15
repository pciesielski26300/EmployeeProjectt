package service;

import org.example.exception.ValidationException;
import org.example.model.Person;
import org.example.repository.EmployeeRepository;
import org.example.service.EmployeeServiceImpl;
import org.example.validation.EmployeeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EmployeeSerivceImplTest {

    private final String TEST_INTERNAL_DIR_PATH = "src/test/resources/internal";
    private final String TEST_EXTERNAL_DIR_PATH = "src/test/resources/external";

    private EmployeeServiceImpl underTest;
    private EmployeeRepository employeeRepository;
    private EmployeeValidator employeeValidator;

    @BeforeEach
    void setUp() {
        employeeRepository = Mockito.mock(EmployeeRepository.class);
        employeeValidator = Mockito.mock(EmployeeValidator.class);
        underTest = new EmployeeServiceImpl(employeeValidator, employeeRepository);
        deleteDirectoryContents(TEST_INTERNAL_DIR_PATH);
        deleteDirectoryContents(TEST_EXTERNAL_DIR_PATH);
    }

    @Test
    void testThatCreateEmployeeWorksFineWithValidData() {
        // Given
        Person person = new Person("1", "John", "Doe", "123456789", "john@example.com", "123456789", true);
        when(employeeValidator.isPersonIdUnique(anyString())).thenReturn(true);
        doNothing().when(employeeValidator).validateEmployee(any());

        // When
        underTest.create(person);

        // Then
        verify(employeeValidator, times(1)).validateEmployee(person);
        verify(employeeRepository, times(1)).create(person);
    }

    @Test
    void testThatCreateEmployeeWithNonUniqueIdThrowsException() {
        // Given
        Person person = new Person("1", "s", "Doe", "123456789", "john@example.com", "123456789", true);
        doThrow(new ValidationException("Invalid employee data")).when(employeeValidator).validateEmployee(person);

        // When
        // Then
        assertThrows(ValidationException.class, () -> underTest.create(person));
        verify(employeeValidator, times(1)).validateEmployee(person);
        verifyNoInteractions(employeeRepository);
    }

    @Test
    void testThatCreateEmployeeWithNullIdThrowsException() {
        // Given
        Person personWithEmptyId = new Person(null, "John", "Doe", "123456789", "john@example.com", "123456789", true);
        doThrow(new ValidationException("Invalid employee data")).when(employeeValidator).validateEmployee(personWithEmptyId);

        // When & Then
        ValidationException thrown = assertThrows(ValidationException.class, () -> underTest.create(personWithEmptyId));
        verify(employeeValidator, times(1)).validateEmployee(personWithEmptyId);
        verifyNoInteractions(employeeRepository);
    }

    @Test
    void testFindWithValidSearchCriteria() {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("personId", "1");

        // When
        List<Person> result = underTest.find(searchCriteria);

        //then
        verify(employeeValidator, times(1)).validateSearchCriteria(searchCriteria);
        verify(employeeRepository, times(1)).find(searchCriteria);
    }

    @Test
    void testFindWithInvalidSearchCriteria() {
        // Given
        Map<String, String> invalidCriteria = new HashMap<>();
        invalidCriteria.put("s", "1");
        doThrow(new ValidationException("Invalid employee data")).when(employeeValidator).validateSearchCriteria(invalidCriteria);

        // When, Then
        assertThrows(ValidationException.class, () -> underTest.find(invalidCriteria));
        verify(employeeValidator, times(1)).validateSearchCriteria(invalidCriteria);
        verify(employeeRepository, times(0)).find(invalidCriteria);
    }

    @Test
    void testDeleteWithValidPersonId() {
        // Given
        Person person = new Person("1", "John", "Doe", "123456789", "john@example.com", "123456789", true);
        when(employeeValidator.isPersonIdUnique(anyString())).thenReturn(true);
        doNothing().when(employeeValidator).validateEmployee(any());
        underTest.create(person);
        String personId = "1";
        doNothing().when(employeeValidator).validatePersonId(personId);
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("personId", "1");

        // When
        underTest.delete(personId);
        List<Person> people = underTest.find(searchCriteria);

        // Then
        verify(employeeValidator, times(1)).validatePersonId(personId);
        verify(employeeRepository, times(1)).delete(personId);
        assertTrue(people.isEmpty());
    }

    @Test
    void testDeleteWithInvalidPersonId() {
        // Given
        String personId = "invalidId";
        doThrow(new ValidationException("Invalid employee data")).when(employeeValidator).validatePersonId(personId);

        // When
        // Then
        assertThrows(ValidationException.class, () -> underTest.delete(personId));
        verify(employeeValidator, times(1)).validatePersonId(personId);
        verifyNoInteractions(employeeRepository);
    }

    @Test
    void testModify_InvalidEmployee() {
        // Given
        Person invalidPerson = new Person("1", null, "Doe", "123456789", "john@example.com", "123456789", true);
        doThrow(new ValidationException("Invalid employee data")).when(employeeValidator).validateEmployee(invalidPerson);

        // When/Then
        assertThrows(ValidationException.class, () -> underTest.update(invalidPerson));
        verify(employeeValidator, times(1)).validateEmployee(invalidPerson);
        verifyNoInteractions(employeeRepository);
    }

    @Test
    void testModify_ValidEmployee() {
        // Given
        Person validPerson = new Person("1", "John", "Doe", "123456789", "john@example.com", "123456789", true);

        // When
        underTest.update(validPerson);

        // Then
        verify(employeeValidator, times(1)).validateEmployee(validPerson);
        verify(employeeRepository, times(1)).update(validPerson);
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
