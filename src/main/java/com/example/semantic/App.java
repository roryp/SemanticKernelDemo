// Copyright (c) Microsoft. All rights reserved.
package com.example.semantic;

import java.util.List;
import java.util.Scanner;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.contextvariables.converters.ContextVariableJacksonConverter;
import com.microsoft.semantickernel.hooks.KernelHooks;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationContext.Builder;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

public class App {

    public static void main(String[] args) throws Exception {

        String AZURE_CLIENT_KEY = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_API_KEY");
        String CLIENT_ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String MODEL_ID = "gpt-4o";

        OpenAIAsyncClient client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
                .endpoint(CLIENT_ENDPOINT)
                .buildAsyncClient();

        // Create your AI service client
        ChatCompletionService chatService = OpenAIChatCompletion.builder()
                .withModelId(MODEL_ID)
                .withOpenAIAsyncClient(client)
                .build();

        // Create a plugin (the LightsPlugin class is defined separately)
        KernelPlugin lightPlugin = KernelPluginFactory.createFromObject(new LightsPlugin(),
                "LightsPlugin");

        // Create a kernel with OpenAI chat completion and plugin
        Kernel.Builder builder = Kernel.builder();
        builder.withAIService(ChatCompletionService.class, chatService);
        builder.withPlugin(lightPlugin);
        // Build the kernel
        Kernel kernel = builder.build();

        ChatCompletionService chatCompletionService = kernel.getService(
                ChatCompletionService.class);

        // Register a global converter for the LightModel class to enable serialization
        // and deserialization of context variables.
        ContextVariableTypes.addGlobalConverter(ContextVariableJacksonConverter.create(LightModel.class));

        // add a hook when the light plugin is called
        KernelHooks hook = new KernelHooks();
        hook.addPreToolCallHook((context) -> {
            System.out.println("Called Plugin: " + context.getFunction().getMetadata().getName());
            return context;
        });
        kernel.getGlobalKernelHooks().addHooks(hook);

        // Enable planning
        InvocationContext invocationContext = new Builder()
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
            // Add user input to the chat history
            history.addUserMessage(userInput);
            // Get the chat message contents asynchronously
            List<ChatMessageContent<?>> results = chatCompletionService.getChatMessageContentsAsync(
                    history, kernel, invocationContext).block();
            for (ChatMessageContent<?> result : results) {
                // Print the results from the assistant
                if (result.getAuthorRole() == AuthorRole.ASSISTANT && result.getContent() != null) {
                    System.out.println("Assistant > " + result);
                }
                // Add the message from the agent to the chat history
                history.addMessage(result);
            }
        } while (userInput != null && !userInput.isEmpty());
    }
}
