# Semantic Kernel Demo

![Semantic Kernel](the-kernel-is-at-the-center-of-everything.png)

## Introduction
This project is a Java application that interacts with a set of lights using a Semantic Kernel OpenAI plugin system.

## Installation
### Prerequisites
- Java Development Kit (JDK)
- Maven
- Set the `OPENAI_API_KEY` environment variable

### Steps
1. Clone the repository:
    ```sh
    git clone https://github.com/roryp/aistack.git
    cd aistack
    ```

2. Set the `OPENAI_API_KEY` environment variable:
    ```sh
    set OPENAI_API_KEY=your_key_value
    ```

3. Build the project:
    ```sh
    mvn clean install
    ```

## Usage
### Running the Application
To run the application, use the following command:
```sh
java -cp target/classes com.example.semantic.App
```

## Learn More
For more information, visit the [Semantic Kernel Documentation](https://learn.microsoft.com/en-us/semantic-kernel/get-started/quick-start-guide?pivots=programming-language-java).

## Appendix - detailed design

- Setup Environment:
  - Ensure the `OPENAI_API_KEY` environment variable is set, which is required for the OpenAI API.
- Initialize OpenAI Client:
  - The application initializes an `OpenAIAsyncClient` using the `OpenAIClientBuilder` with the provided API key.
- Create Kernel:
  - An instance of `Kernel` from the `com.microsoft.semantickernel` package is created to manage plugins and orchestrate tasks.
- Define Plugins:
  - The `LightsPlugin` class is instantiated, which contains methods to get the list of lights and change their states.
- Register Plugins:
  - The `LightsPlugin` instance is registered with the kernel, making its functions available for invocation.
- Setup Chat Completion Service:
  - The `ChatCompletionService` is set up to handle chat interactions, using the OpenAI model specified by `MODEL_ID`.
- Initialize Chat History:
  - A `ChatHistory` object is created to maintain the conversation context between the user and the assistant.
- User Input Loop:
  - The application enters a loop where it reads user input from the console.
- Process User Input:
  - The user input is sent to the `ChatCompletionService`, which processes it and generates responses using the OpenAI model.
- Output and Update History:
  - The assistant's responses are printed to the console and added to the chat history to maintain context for future interactions.

