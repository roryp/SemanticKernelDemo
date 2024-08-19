// Copyright (c) Microsoft. All rights reserved.
package com.example.semantic;


import java.util.List;
import java.util.Scanner;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.KeyCredential;
import com.google.gson.Gson;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
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

    private static final String CLIENT_KEY = "your key";
    private static final String MODEL_ID = "gpt-4";

    public static void main(String[] args) throws Exception {

        OpenAIAsyncClient client = new OpenAIClientBuilder()
                .credential(new KeyCredential(CLIENT_KEY))
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

        ContextVariableTypes
            .addGlobalConverter(ContextVariableTypeConverter.builder(LightModel.class)
                .toPromptString(new Gson()::toJson)
                .build());

        KernelHooks hook = new KernelHooks();

        hook.addPreToolCallHook((context) -> {
            System.out.println("Pre-tool call hook");
            return context;
        });

        hook.addPreChatCompletionHook(
            (context) -> {
                System.out.println("Pre-chat completion hook");
                return context;
            });

        hook.addPostChatCompletionHook(
            (context) -> {
                System.out.println("Post-chat completion hook");
                return context;
            });

        kernel.getGlobalKernelHooks().addHooks(hook);

        // Enable planning
        InvocationContext invocationContext = new Builder()
            .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
            .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
            .withContextVariableConverter(ContextVariableTypeConverter.builder(LightModel.class)
                .toPromptString(new Gson()::toJson)
                .build())
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
            List<ChatMessageContent<?>> results = chatCompletionService.getChatMessageContentsAsync(
                history, kernel, invocationContext).block();
            for (ChatMessageContent<?> result : results) {
                // Print the results
                if (result.getAuthorRole() == AuthorRole.ASSISTANT && result.getContent() != null) {
                    System.out.println("Assistant > " + result);
                }
                // Add the message from the agent to the chat history
                history.addMessage(result);
            }
        } while (userInput != null && !userInput.isEmpty());
    }
}
