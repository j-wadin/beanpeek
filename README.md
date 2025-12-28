# Beanpeek

Beanpeek is a **pedagogical Java project** that reimplements core ideas from the Spring Framework in a simplified, explicit way.

The goal is not to build a production framework, but to understand and explain how Spring works internally.

---

## Purpose

Beanpeek exists to answer questions like:

- What actually happens when Spring creates a bean?
- How does constructor injection work?
- When are `@PostConstruct` and `@PreDestroy` methods invoked?
- Why does Spring sometimes return proxies instead of real objects?
- How can annotations drive behavior at runtime?

---

## What Beanpeek Reimplements (in simplified form)

Beanpeek recreates several core Spring concepts:

- **Component scanning**  
  Discover classes annotated with `@MiniService`

- **Dependency injection**  
  Constructor-based injection with dependency resolution

- **Bean lifecycle**
   - `@MiniPostConstruct`
   - `@MiniPreDestroy`

- **Configuration property injection**
   - `@MiniConfigProperty` backed by `application.properties`

- **AOP via proxies**
   - Method interception using JDK dynamic proxies
   - Example: `@LogExecutionTime`

All behavior is implemented manually using:
- Java reflection
- Classpath scanning
- Dynamic proxies

---

## Custom Annotations
| Annotation | Purpose |
|-----------|---------|
| `@MiniService` | Marks a class as a managed bean |
| `@MiniPostConstruct` | Runs after bean construction |
| `@MiniPreDestroy` | Runs during shutdown |
| `@MiniConfigProperty` | Injects values from `application.properties` |
| `@LogExecutionTime` | Measures method execution time via proxy |


## How It Works

1. `BeanScanner` scans a package for classes annotated with `@MiniService`
2. `BeanContainer`:
   - Resolves constructor dependencies
   - Instantiates beans
   - Injects configuration properties
   - Runs lifecycle hooks
3. Beans with AOP annotations are wrapped in proxies
4. Beans are retrieved from the container via type lookup

## What This Project Is Not
- Not Spring
- Not Spring Boot
- Not production-ready
- Not optimized for performance

This is a learning and teaching tool.

##  Dependencies

- Java 21+
- Maven
- Lombok

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/j-wadin/beanpeek.git
   cd beanpeek

2. Run the application
    ```bash
   ./mvnw spring-boot:run
