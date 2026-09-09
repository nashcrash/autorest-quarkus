# Autorest Quarkus

Autorest Quarkus is an annotation-processor-based library for [Quarkus](https://quarkus.io) that automatically generates a full CRUD REST API — Repository, Mapper, Service and JAX-RS Controller (plus an optional REST Client) — for an annotated entity/DTO pair, backed by either a SQL database (via Hibernate ORM with Panache) or MongoDB (via Panache), with optional support for reactive endpoints, aggregation pipelines, and historical (time-validity) entities.

For the full technical reference — every annotation, utility class and feature in detail, with diagrams — see the **[project wiki](https://github.com/nashcrash/autorest-quarkus/wiki)**.

## Requirements

- Java 21
- Quarkus 3.22.x (the library aligns with the Quarkus BOM version it was built against)
- Lombok and MapStruct (used internally by the generated code)

## Installation

Add the API dependency and register the annotation processor:

```xml
<dependency>
    <groupId>io.github.nashcrash.autorest</groupId>
    <artifactId>autorest-quarkus-api</artifactId>
    <version>1.5.10</version>
</dependency>
```

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>io.github.nashcrash.autorest</groupId>
                        <artifactId>autorest-quarkus-processor</artifactId>
                        <version>1.5.10</version>
                    </path>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${mapstruct.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

To write YAML-driven integration tests against the generated API, also add `autorest-quarkus-test` in `test` scope — see [Testing](https://github.com/nashcrash/autorest-quarkus/wiki/Testing) on the wiki.

## Quick start

Create an entity extending one of the abstract base classes and annotate it with `@ResourceAPI`:

```java
@MongoEntity(collection = "orders")
@ResourceAPI(basePath = "/orders", dto = OrderDTO.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Order extends AbstractEntityMongo {
    private String customerCode;
    private BigDecimal amount;
}
```

Create the matching DTO extending `AbstractDTO`:

```java
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO extends AbstractDTO {
    private String customerCode;
    private BigDecimal amount;
}
```

At compile time the annotation processor generates `OrderRepository`, `OrderMapper`, `OrderService` and `OrderResource`, exposing a full CRUD API under `/orders` (create, get by id, search/find with pagination and generic filtering, patch, upsert, delete) — fully described in the generated OpenAPI/Swagger document.

See [Getting Started](https://github.com/nashcrash/autorest-quarkus/wiki/Getting-Started) on the wiki for a step-by-step walkthrough, including the SQL and historical-entity variants.

## Documentation

Full documentation lives in the **[wiki](https://github.com/nashcrash/autorest-quarkus/wiki)**:

- [Core Concepts](https://github.com/nashcrash/autorest-quarkus/wiki/Core-Concepts)
- [Entities and DTOs](https://github.com/nashcrash/autorest-quarkus/wiki/Entities-and-DTOs)
- [`@ResourceAPI` Annotation](https://github.com/nashcrash/autorest-quarkus/wiki/ResourceAPI-Annotation)
- [Identifiers](https://github.com/nashcrash/autorest-quarkus/wiki/Identifiers)
- [Querying and Filtering](https://github.com/nashcrash/autorest-quarkus/wiki/Querying-and-Filtering)
- [Reactive APIs and REST Clients](https://github.com/nashcrash/autorest-quarkus/wiki/Reactive-and-Client)
- [Aggregate Pipelines](https://github.com/nashcrash/autorest-quarkus/wiki/Aggregate-Pipelines)
- [Context and Headers](https://github.com/nashcrash/autorest-quarkus/wiki/Context-and-Headers)
- [Validation](https://github.com/nashcrash/autorest-quarkus/wiki/Validation)
- [Exception Handling and Strategy](https://github.com/nashcrash/autorest-quarkus/wiki/Exception-Handling-and-Strategy)
- [Testing](https://github.com/nashcrash/autorest-quarkus/wiki/Testing)

## Contributing / release process

### Bumping the version

```commandline
mvn versions:set -DnewVersion=1.0.2-SNAPSHOT
```

### Deploying

```commandline
mvn deploy -P release -s .settings.xml
```

The artifact is published to the Maven repository at:
https://repo.maven.apache.org/maven2/io/github/nashcrash/autorest/

### GPG keystore

Make sure the signing key is available in the local secret store:

```commandline
gpg --list-secret-keys --keyid-format LONG
```

If it isn't, import it:

```commandline
gpg --import chiave_privata.asc
```
