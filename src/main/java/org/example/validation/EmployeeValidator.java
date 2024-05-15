package org.example.validation;

import org.example.exception.ValidationException;
import org.example.model.Person;

import java.io.File;
import java.util.*;

public class EmployeeValidator {

    private final String internalDirPath = "src/main/resources/internal";
    private final String externalDirPath = "src/main/resources/external";

    public void validateEmployee(Person employee) {
        if (employee == null) {
            throw new ValidationException("Employee data cannot be null.");
        }

        if (employee.getPersonId() == null || employee.getPersonId().isEmpty() ||
                employee.getFirstName() == null || employee.getFirstName().isEmpty() ||
                employee.getLastName() == null || employee.getLastName().isEmpty() ||
                employee.getMobile() == null || employee.getMobile().isEmpty() ||
                employee.getEmail() == null || employee.getEmail().isEmpty() ||
                employee.getPesel() == null || employee.getPesel().isEmpty()) {
            throw new ValidationException("Incorrect employee data.");
        }
    }

    private static boolean isValidBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    public boolean isPersonIdUnique(String personId) {
        List<String> allFilePaths = getAllFilePaths(internalDirPath);
        allFilePaths.addAll(getAllFilePaths(externalDirPath));

        for (String filePath : allFilePaths) {
            String filename = new File(filePath).getName();
            String idFromFilename = filename.substring(0, filename.lastIndexOf('.'));
            if (idFromFilename.equals(personId)) {
                return false;
            }
        }
        return true;
    }

    public void validateSearchCriteria(Map<String, String> searchCriteria) {
        if (searchCriteria == null || searchCriteria.isEmpty()) {
            throw new ValidationException("Search criteria cannot be null or empty");
        }

        Set<String> validKeys = new HashSet<>(Arrays.asList(
                "personId", "firstName", "lastName", "mobile", "email", "pesel", "isInternal"
        ));

        for (String key : searchCriteria.keySet()) {
            if (!validKeys.contains(key)) {
                throw new ValidationException("Invalid search criteria key: " + key);
            }
        }

        for (Map.Entry<String, String> entry : searchCriteria.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.equals("isInternal") && !isValidBoolean(value)) {
                throw new ValidationException("Invalid boolean format for isInternal: " + value);
            }
        }
    }

    private List<String> getAllFilePaths(String directoryPath) {
        List<String> filePaths = new ArrayList<>();
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    filePaths.add(file.getPath());
                }
            }
        }
        return filePaths;
    }

    public void validatePersonId(String personId) {
        if (personId == null || personId.isEmpty()) {
            throw new ValidationException("Person ID cannot be null or empty");
        }
    }
}
