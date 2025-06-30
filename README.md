# SEPA-CT (pain.001) Parser Java Project

This project provides a Java library to parse SEPA-CT (pain.001) payment files and extract information needed for account statements (such as account number, IBAN, transaction details, etc.).

## Features
- Parse SEPA-CT (pain.001) XML files with a StAX streaming parser
- JAXB-annotated statement model for easy marshalling
- Generate CAMT and MT outputs using XMLStreamWriter
- Extract account and transaction details for statement generation

## Getting Started

### Prerequisites
- Java 8 or higher
- Maven

### Build
```
mvn clean install
```

### Usage
- Add this library as a dependency in your project
- Use the provided parser class to parse pain.001 files
- Generate CAMT053 v3 and v8 statements using `SepaCtToCamt53Converter`

```java
File painFile = new File("payment.xml");
SepaCtToCamt53Converter.ConversionResult result =
        SepaCtToCamt53Converter.convert(painFile);
String camt53v3 = result.getCamt53V3Xml();
String camt53v8 = result.getCamt53V8Xml();
```

## Project Structure
- `src/main/java/com/serrala/sepa/model` - Data models for extracted information
- `src/main/java/com/serrala/sepa/parser` - SEPA-CT parser implementation
- `src/main/java/com/serrala/sepa/App.java` - Example usage

## License
MIT
