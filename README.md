# aistack

## Running the Application

To run the Semantic Kernel application, follow these steps:

1. Clone the repository:
   ```sh
   git clone https://github.com/roryp/aistack.git
   cd aistack
   ```

2. Build the project using Maven:
   ```sh
   mvn clean install
   ```

3. Run the application:
   ```sh
   mvn exec:java -Dexec.mainClass="com.example.SemanticKernelApp"
   ```

## SemanticKernelApp.java

The `SemanticKernelApp.java` file contains the main class for the application. It includes the necessary imports, client creation, chat completion service creation, and kernel initialization.

## Dependencies and Setup

Make sure you have the following dependencies in your `pom.xml` file:

```xml
<dependencies>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-ai-openai</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.microsoft.semantickernel</groupId>
        <artifactId>semantickernel</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

Additionally, ensure you have the necessary credentials and endpoint information for the Azure OpenAI client:

```java
String azureOpenAIClientCredentials = "<your-azure-openai-client-credentials>";
String azureOpenAIClientEndpoint = "<your-azure-openai-client-endpoint>";
String modelId = "<your-model-id>";
```
