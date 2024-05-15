package org.example.service;

import org.example.model.Person;

import java.util.List;
import java.util.Map;

public interface EmployeeService{

    void create(Person person);

    List<Person> find(Map<String, String> searchCriteria);

    void delete(String personId);

    void update(Person updatedPerson);
}
