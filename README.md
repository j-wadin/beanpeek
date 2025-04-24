# 🫘 Beanpeek

**Beanpeek** is an educational Spring Boot project designed to inspect and explore Spring Beans using Java Reflection.  
It was built as a learning exercise to better understand how Spring manages components, and how to introspect runtime metadata in a Spring application.

##  Features

- List all Spring Beans in the application context
- Filter and display only your own application beans
- Group beans by source (application, Spring Boot, internal)
- Display annotations on beans and their methods

##  Learning Goals

This project helps you learn to:

- Use `ApplicationContext` to interact with Spring Beans
- Apply Java Reflection to inspect class and method metadata
- Understand annotations and how frameworks like Spring use them

##  Dependencies

- Java 21+
- Spring Boot 3.2
- Maven

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/beanpeek.git
   cd beanpeek

2. Run the application
    ```bash
   ./mvnw spring-boot:run

3. Open your browser
   ```bash
    http://localhost:8080/inspect/beans

## Available Endpoints
| Endpoint                                | Description                                  |
|:----------------------------------------|:---------------------------------------------|
| `/inspect/beans`                        | Lists all Spring beans                       |
| `/inspect/beans/own`                    | Lists only your own application beans        |
| `/inspect/beans/grouped`                | Groups beans by source (application, etc.)   |
| `/inspect/beans/{beanName}/details`     | Shows annotations on the specified bean      |
