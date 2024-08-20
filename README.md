# Getting started with Semantic Kernel

In just a few steps, you can build your first AI agent with Semantic Kernel in Java. This guide will show you how to:

- Install the necessary packages
- Create a back-and-forth conversation with an AI
- Give an AI agent the ability to run your code
- Watch the AI create plans on the fly

### Installing the SDK

Instructions for accessing the SemanticKernel Java package is available here. It's as easy as:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.microsoft.semantic-kernel</groupId>
            <artifactId>semantickernel-bom</artifactId>
            <version>${sk.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Writing your first console app

```java
OpenAIAsyncClient client = new OpenAIClientBuilder()
    .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
    .endpoint(CLIENT_ENDPOINT)
    .buildAsyncClient();

// Import the LightsPlugin
KernelPlugin lightPlugin = KernelPluginFactory.createFromObject(new LightsPlugin(),
    "LightsPlugin");

// Create your AI service client
ChatCompletionService chatCompletionService = OpenAIChatCompletion.builder()
    .withModelId(MODEL_ID)
    .withOpenAIAsyncClient(client)
    .build();

// Create a kernel with Azure OpenAI chat completion and plugin
Kernel kernel = Kernel.builder()
    .withAIService(ChatCompletionService.class, chatCompletionService)
    .withPlugin(lightPlugin)
    .build();

// Add a converter to the kernel to show it how to serialise LightModel objects into a prompt
ContextVariableTypes
    .addGlobalConverter(
        ContextVariableTypeConverter.builder(LightModel.class)
            .toPromptString(new Gson()::toJson)
            .build());

// Enable planning
InvocationContext invocationContext = new InvocationContext.Builder()
    .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
    .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
    .build();

// Create a history to store the conversation
ChatHistory history = new ChatHistory();

// Initiate a back-and-forth chat
Scanner scanner = new Scanner(System.in);
String userInput;
do {
  // Collect user input
  System.out.print("User > ");

  userInput = scanner.nextLine();
  // Add user input
  history.addUserMessage(userInput);

  // Prompt AI for response to users input
  List<ChatMessageContent<?>> results = chatCompletionService
      .getChatMessageContentsAsync(history, kernel, invocationContext)
      .block();

  for (ChatMessageContent<?> result : results) {
    // Print the results
    if (result.getAuthorRole() == AuthorRole.ASSISTANT && result.getContent() != null) {
      System.out.println("Assistant > " + result);
    }
    // Add the message from the agent to the chat history
    history.addMessage(result);
  }
} while (userInput != null && !userInput.isEmpty());
```

The following back-and-forth chat should be similar to what you see in the console. The function calls have been added below to demonstrate how the AI leverages the plugin behind the scenes.

| Role      | Message                          |
|-----------|----------------------------------|
| 🔵 User   | Please toggle the light          |
| 🔴 Assistant (function call) | LightsPlugin.GetState() |
| 🟢 Tool   | off                              |
| 🔴 Assistant (function call) | LightsPlugin.ChangeState(true) |
| 🟢 Tool   | on                               |
| 🔴 Assistant | The light is now on           |

If you're interested in understanding more about the code above, we'll break it down in the next section.

### Understanding the code

To make it easier to get started building enterprise apps with Semantic Kernel, we've created a step-by-step that guides you through the process of creating a kernel and using it to interact with AI services.

In the following sections, we'll unpack the above sample by walking through steps 1, 2, 3, 4, 6, 9, and 10. Everything you need to build a simple agent that is powered by an AI service and can run your code.

#### 1) Import packages

For this sample, we first started by importing the following packages:

```java
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
```

#### 2) Add AI services

Afterwards, we add the most important part of a kernel: the AI services that you want to use. In this example, we added an Azure OpenAI chat completion service to the kernel builder.

> **Note**
> In this example, we used Azure OpenAI, but you can use any other chat completion service. To see the full list of supported services, refer to the supported languages article. If you need help creating a different service, refer to the AI services article. There, you'll find guidance on how to use OpenAI or Azure OpenAI models as services.

```java
// Create your AI service client
ChatCompletionService chatCompletionService = OpenAIChatCompletion.builder()
    .withModelId(MODEL_ID)
    .withOpenAIAsyncClient(client)
    .build();

// Create a kernel with Azure OpenAI chat completion and plugin
Kernel kernel = Kernel.builder()
    .withAIService(ChatCompletionService.class, chatCompletionService)
    .withPlugin(lightPlugin)
    .build();
```

#### 4) Build the kernel and retrieve services

```java
// Create a kernel with Azure OpenAI chat completion and plugin
Kernel kernel = Kernel.builder()
    .withAIService(ChatCompletionService.class, chatCompletionService)
    .withPlugin(lightPlugin)
    .build();
```

#### 6) Add plugins

With plugins, can give your AI agent the ability to run your code to retrieve information from external sources or to perform actions. In the above example, we added a plugin that allows the AI agent to interact with a light bulb. Below, we'll show you how to create this plugin.

##### Create a native plugin

Below, you can see that creating a native plugin is as simple as creating a new class.

In this example, we've created a plugin that can manipulate a light bulb. While this is a simple example, this plugin quickly demonstrates how you can support both...

- Retrieval Augmented Generation (RAG) by providing the AI agent with the state of the light bulb
- And task automation by allowing the AI agent to turn the light bulb on or off.

In your own code, you can create a plugin that interacts with any external service or API to achieve similar results.

```java
public class LightsPlugin {

  // Mock data for the lights
  private final Map<Integer, LightModel> lights = new HashMap<>();

  public LightsPlugin() {
    lights.put(1, new LightModel(1, "Table Lamp", false));
    lights.put(2, new LightModel(2, "Porch light", false));
    lights.put(3, new LightModel(3, "Chandelier", true));
  }

  @DefineKernelFunction(name = "get_lights", description = "Gets a list of lights and their current state")
  public List<LightModel> getLights() {
    System.out.println("Getting lights");
    return new ArrayList<>(lights.values());
  }

  @DefineKernelFunction(name = "change_state", description = "Changes the state of the light")
  public LightModel changeState(
      @KernelFunctionParameter(name = "id", description = "The ID of the light to change") int id,
      @KernelFunctionParameter(name = "isOn", description = "The new state of the light") boolean isOn) {
    System.out.println("Changing light " + id + " " + isOn);
    if (!lights.containsKey(id)) {
      throw new IllegalArgumentException("Light not found");
    }

    lights.get(id).setIsOn(isOn);

    return lights.get(id);
  }
}
```

##### Add the plugin to the kernel

Once you've created your plugin, you can add it to the kernel so the AI agent can access it. In the sample, we added the LightsPlugin class to the kernel.

```java
// Import the LightsPlugin
KernelPlugin lightPlugin = KernelPluginFactory.createFromObject(new LightsPlugin(),
    "LightsPlugin");
```

#### 9) Planning

Semantic Kernel leverages function calling–a native feature of most LLMs–to provide planning. With function calling, LLMs can request (or call) a particular function to satisfy a user's request. Semantic Kernel then marshals the request to the appropriate function in your codebase and returns the results back to the LLM so the AI agent can generate a final response.

To enable automatic function calling, we first need to create the appropriate execution settings so that Semantic Kernel knows to automatically invoke the functions in the kernel when the AI agent requests them.

```java
// Enable planning
InvocationContext invocationContext = new InvocationContext.Builder()
    .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
    .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
    .build();
```

#### 10) Invoke

Finally, we invoke the AI agent with the plugin. The sample code demonstrates how to generate a non-streaming response, but you can also generate a streaming response by using the GetStreamingChatMessageContentAsync method.

```java
userInput = scanner.nextLine();
// Add user input
history.addUserMessage(userInput);

// Prompt AI for response to users input
List<ChatMessageContent<?>> results = chatCompletionService
    .getChatMessageContentsAsync(history, kernel, invocationContext)
    .block();
```
#### 11) Summary Steps to Create a Chat Application

1. Import necessary packages and classes.
2. Define the `App` class and declare constants for `CLIENT_KEY` and `MODEL_ID`.
3. In the `main` method, create an `OpenAIAsyncClient` using `OpenAIClientBuilder`.
4. Create a `ChatCompletionService` using `OpenAIChatCompletion.builder()`.
5. Create a `KernelPlugin` instance using `KernelPluginFactory.createFromObject()`.
6. Build a `Kernel` instance using `Kernel.Builder` and add the AI service and plugin.
7. Retrieve the `ChatCompletionService` from the kernel.
8. Add a global context variable converter for `LightModel` using `ContextVariableTypes.addGlobalConverter()`.
9. Create a `KernelHooks` instance and add pre-tool, pre-chat, and post-chat hooks.
10. Add the hooks to the kernel's global hooks.
11. Enable planning by creating an `InvocationContext` with specific settings.
12. Create a `ChatHistory` instance to store the conversation.
13. Use a `Scanner` to collect user input in a loop.
14. Add user input to the chat history and get chat message contents asynchronously.
15. Print the assistant's response and add it to the chat history.
16. Repeat the loop until the user input is empty.

These steps outline the process of creating a chat application using the Semantic Kernel framework.
