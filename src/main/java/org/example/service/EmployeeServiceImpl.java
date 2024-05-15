package org.example.service;

import org.example.exception.ValidationException;
import org.example.model.Person;
import org.example.repository.EmployeeRepository;
import org.example.validation.EmployeeValidator;

import java.util.List;
import java.util.Map;

public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeValidator employeeValidator;
    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeValidator employeeValidator, EmployeeRepository employeeRepository) {
        this.employeeValidator = employeeValidator;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void create(Person person) {

            employeeValidator.validateEmployee(person);
            if (!employeeValidator.isPersonIdUnique(person.getPersonId())) {
                throw new ValidationException("An employee with the specified ID already exists.");
            }

            employeeRepository.create(person);
            System.out.println("The employee was created");
    }

    @Override
    public List<Person> find(Map<String, String> searchCriteria) {
        employeeValidator.validateSearchCriteria(searchCriteria);
        return employeeRepository.find(searchCriteria);
    }

    @Override
    public void delete(String personId) {
        employeeValidator.validatePersonId(personId);
        employeeRepository.delete(personId);
    }

    @Override
    public void update(Person updatedPerson) {
        try {
            employeeValidator.validateEmployee(updatedPerson);


            employeeRepository.update(updatedPerson);
            System.out.println("Employee with ID: " + updatedPerson.getPersonId() + " has been updated");
        } catch (ValidationException e) {
            throw new ValidationException("Failed to update employee: " + e.getMessage(), e);
        }
    }
}
