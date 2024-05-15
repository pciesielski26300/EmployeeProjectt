package validator;

import org.example.exception.ValidationException;
import org.example.model.Person;
import org.example.validation.EmployeeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeValidatorTest {

    private EmployeeValidator underTest;
    @BeforeEach
    void setUp() {
        underTest = new EmployeeValidator();
    }

    @Test
    void validateEmployee_NullEmployee_ThrowsValidationException() {

        assertThrows(ValidationException.class, () -> underTest.validateEmployee(null));
    }

    @Test
    void validateEmployee_IncorrectEmployeeData_ThrowsValidationException() {

        Person employee = new Person(null, null, null, null, null, null, false);
        assertThrows(ValidationException.class, () -> underTest.validateEmployee(employee));
    }

    @Test
    void isPersonIdUnique_NonUniquePersonId_ReturnsFalse() {
        assertFalse(underTest.isPersonIdUnique("1"));
    }

    @Test
    void isPersonIdUnique_UniquePersonId_ReturnsTrue() {
        assertTrue(underTest.isPersonIdUnique("nonExistingPersonId"));
    }

    @Test
    void validateSearchCriteria_NullCriteria_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> underTest.validateSearchCriteria(null));
    }

    @Test
    void validateSearchCriteria_InvalidCriteriaKey_ThrowsValidationException() {
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("invalidKey", "value");
        assertThrows(ValidationException.class, () -> underTest.validateSearchCriteria(searchCriteria));
    }

    @Test
    void validateSearchCriteria_InvalidBooleanFormat_ThrowsValidationException() {
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("isInternal", "invalidBoolean");
        assertThrows(ValidationException.class, () -> underTest.validateSearchCriteria(searchCriteria));
    }

    @Test
    void validatePersonId_NullPersonId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> underTest.validatePersonId(null));
    }

    @Test
    void validatePersonId_EmptyPersonId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> underTest.validatePersonId(""));
    }
}
