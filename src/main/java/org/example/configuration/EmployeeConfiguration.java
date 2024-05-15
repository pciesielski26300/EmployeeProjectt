package org.example.configuration;

import org.example.repository.EmployeeRepository;
import org.example.service.EmployeeServiceImpl;
import org.example.validation.EmployeeValidator;

public class EmployeeConfiguration {

    private final String INTERNAL_DIR_PATH = "src/main/resources/internal";
    private final String EXTERNAL_DIR_PATH = "src/main/resources/external";


    public EmployeeServiceImpl employeeService(){
        return new EmployeeServiceImpl(employeeValidator(), employeeRepository());
    }

    public EmployeeValidator employeeValidator() {
        return new EmployeeValidator();
    }

    public EmployeeRepository employeeRepository() {
        return new EmployeeRepository(INTERNAL_DIR_PATH, EXTERNAL_DIR_PATH);
    }
}
