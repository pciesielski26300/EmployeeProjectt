package org.example.exception;

public class EmployeeRepositoryException extends RuntimeException{
    public EmployeeRepositoryException(String message) {
        super(message);
    }

    public EmployeeRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
