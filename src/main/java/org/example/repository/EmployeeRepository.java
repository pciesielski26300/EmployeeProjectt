package org.example.repository;

import org.example.exception.EmployeeRepositoryException;
import org.example.model.Person;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class EmployeeRepository {

    public static final String PERSON_ID = "personId";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String MOBILE = "mobile";
    public static final String EMAIL = "email";
    public static final String PESEL = "pesel";
    public static final String IS_INTERNAL = "isInternal";
    public static final String EMPLOYEE = "employee";
    private final String internalDirPath;
    private final String externalDirPath;

    public EmployeeRepository(String internalDirPath, String externalDirPath) {
        this.internalDirPath = internalDirPath;
        this.externalDirPath = externalDirPath;
    }

    public void create(Person newEmployee) {
        String directoryPath = newEmployee.isInternal() ? internalDirPath : externalDirPath;
        String filePath = directoryPath + File.separator + newEmployee.getPersonId() + ".xml";

        if (Files.exists(Paths.get(filePath))) {
            throw new EmployeeRepositoryException("Employee with ID " + newEmployee.getPersonId() + " already exists.");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element employeeElement = doc.createElement(EMPLOYEE);
            employeeElement.setAttribute(PERSON_ID, newEmployee.getPersonId());
            employeeElement.setAttribute(IS_INTERNAL, String.valueOf(newEmployee.isInternal()));

            addElement(doc, employeeElement, FIRST_NAME, newEmployee.getFirstName());
            addElement(doc, employeeElement, LAST_NAME, newEmployee.getLastName());
            addElement(doc, employeeElement, MOBILE, newEmployee.getMobile());
            addElement(doc, employeeElement, EMAIL, newEmployee.getEmail());
            addElement(doc, employeeElement, PESEL, newEmployee.getPesel());

            doc.appendChild(employeeElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new EmployeeRepositoryException("An error occurred while processing repository operations", e);
        }
    }

    private void addElement(Document doc, Element parentElement, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(textContent));
        parentElement.appendChild(element);
    }

    public List<Person> find(Map<String, String> searchCriteria) {
        List<Person> foundEmployees = new ArrayList<>();
        foundEmployees.addAll(readEmployeesFromXML(internalDirPath, searchCriteria));
        foundEmployees.addAll(readEmployeesFromXML(externalDirPath, searchCriteria));
        return foundEmployees;
    }

    public void delete(String personId) {
        List<String> allFilePaths = getAllFilePaths(internalDirPath);
        allFilePaths.addAll(getAllFilePaths(externalDirPath));

        boolean found = false;
        for (String filePath : allFilePaths) {
            String filename = new File(filePath).getName();
            String idFromFilename = filename.substring(0, filename.lastIndexOf('.'));
            if (idFromFilename.equals(personId)) {
                try {
                    Files.deleteIfExists(Paths.get(filePath));
                    found = true;
                } catch (IOException e) {
                    throw new EmployeeRepositoryException("An error occurred while processing repository operations", e);
                }
            }
        }

        if (!found) {
            throw new EmployeeRepositoryException("Employee with ID " + personId + " does not exist.");
        }
    }


    private void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            throw new EmployeeRepositoryException("An error occurred while processing repository operations", e);
        }
    }

    public void update(Person updatedPerson) {
        List<String> allFilePaths = getAllFilePaths(internalDirPath);
        allFilePaths.addAll(getAllFilePaths(externalDirPath));

        boolean found = false;
        for (String filePath : allFilePaths) {
            String filename = new File(filePath).getName();
            String idFromFilename = filename.substring(0, filename.lastIndexOf('.'));
            if (idFromFilename.equals(updatedPerson.getPersonId())) {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(new File(filePath));

                    NodeList nodeList = doc.getElementsByTagName(EMPLOYEE);

                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Element element = (Element) nodeList.item(i);
                        String personId = element.getAttribute(PERSON_ID);
                        if (personId.equals(updatedPerson.getPersonId())) {
                            element.getElementsByTagName(FIRST_NAME).item(0).setTextContent(updatedPerson.getFirstName());
                            element.getElementsByTagName(LAST_NAME).item(0).setTextContent(updatedPerson.getLastName());
                            element.getElementsByTagName(MOBILE).item(0).setTextContent(updatedPerson.getMobile());
                            element.getElementsByTagName(EMAIL).item(0).setTextContent(updatedPerson.getEmail());
                            element.getElementsByTagName(PESEL).item(0).setTextContent(updatedPerson.getPesel());
                            element.setAttribute(IS_INTERNAL, String.valueOf(updatedPerson.isInternal()));
                        }
                    }

                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(new File(filePath));
                    transformer.transform(source, result);

                    moveFileToCorrectDirectory(filePath, updatedPerson);

                    found = true;
                    break;
                } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                    throw new EmployeeRepositoryException("An error occurred while processing repository operations", e);
                }
            }
        }

        if (!found) {
            throw new EmployeeRepositoryException("Employee with ID: " + updatedPerson.getPersonId() + " does not exist");
        }
    }

    private void moveFileToCorrectDirectory(String filePath, Person updatedPerson) throws IOException {
        String newDirPath = updatedPerson.isInternal() ? internalDirPath : externalDirPath;
        Path sourcePath = Paths.get(filePath);
        Path destinationPath = Paths.get(newDirPath, sourcePath.getFileName().toString());
        Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private List<Person> readEmployeesFromXML(String directoryPath, Map<String, String> searchCriteria) {
        List<Person> employees = new ArrayList<>();

        try {
            List<String> filePaths = getAllFilePaths(directoryPath);
            for (String filePath : filePaths) {
                File file = new File(filePath);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(file);

                NodeList nodeList = doc.getElementsByTagName(EMPLOYEE);

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);
                    String personId = element.getAttribute(PERSON_ID);
                    String firstName = element.getElementsByTagName(FIRST_NAME).item(0).getTextContent();
                    String lastName = element.getElementsByTagName(LAST_NAME).item(0).getTextContent();
                    String mobile = element.getElementsByTagName(MOBILE).item(0).getTextContent();
                    String email = element.getElementsByTagName(EMAIL).item(0).getTextContent();
                    String pesel = element.getElementsByTagName(PESEL).item(0).getTextContent();
                    boolean isInternal = Boolean.parseBoolean(element.getAttribute(IS_INTERNAL));

                    Person employee = new Person(personId, firstName, lastName, mobile, email, pesel, isInternal);
                    if (matchesSearchCriteria(employee, searchCriteria)) {
                        employees.add(employee);
                    }
                }
            }
        } catch (Exception e) {
            throw new EmployeeRepositoryException("An error occurred while processing repository operations", e);
        }

        return employees;
    }

    private List<String> getAllFilePaths(String directoryPath) {
        List<String> filePaths = new ArrayList<>();

        try {
            Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .forEach(filePaths::add);
        } catch (IOException e) {
            throw new EmployeeRepositoryException("An error occurred while processing repository operations", e);
        }

        return filePaths;
    }

    private boolean matchesSearchCriteria(Person employee, Map<String, String> searchCriteria) {
        if (searchCriteria.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, String> entry : searchCriteria.entrySet()) {
            String attributeName = entry.getKey();
            String attributeValue = entry.getValue();
            String employeeAttributeValue = getAttributeValue(employee, attributeName);
            if (employeeAttributeValue == null || !employeeAttributeValue.equalsIgnoreCase(attributeValue)) {
                return false;
            }
        }
        return true;
    }

    private String getAttributeValue(Person employee, String attributeName) {
        switch (attributeName) {
            case PERSON_ID:
                return employee.getPersonId();
            case FIRST_NAME:
                return employee.getFirstName();
            case LAST_NAME:
                return employee.getLastName();
            case MOBILE:
                return employee.getMobile();
            case EMAIL:
                return employee.getEmail();
            case PESEL:
                return employee.getPesel();
            default:
                return null;
        }
    }
}
