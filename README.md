# 🫘 Beanpeek

Beanpeek is a lightweight Java project for learning how dependency injection and reflection frameworks like Spring work internally.

It attempts to implement a basic "mini Spring" that can:
- Discover classes annotated with custom annotations
- Create instances (beans) of these classes
- Automatically run methods annotated with `@MiniPostConstruct`
- Provide a simple container to retrieve created beans

## Why?

The goal of Beanpeek is educational:  
To understand reflection, bean lifecycle, dependency injection, and how a framework like Spring Boot operates internally.

---

## How it works

- Beans are registered manually
- Each bean is instantiated once and stored in a container (`BeanContainer`)
- `@MiniPostConstruct` methods are invoked after instantiation

---

## Custom Annotations

| Annotation | Purpose |
|------------|---------|
| `@MiniService` | Marks a class as a bean to be managed |
| `@MiniPostConstruct` | Marks a method to run after bean creation |

##  Dependencies

- Java 21+
- Spring Boot 3.2
- Maven
- Lombok

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/beanpeek.git
   cd beanpeek

2. Run the application
    ```bash
   ./mvnw spring-boot:run
