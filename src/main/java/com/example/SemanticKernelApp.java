package com.example;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.OpenAIChatCompletion;

public class SemanticKernelApp {

    public static void main(String[] args) {
        // Create the client
        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .credential(azureOpenAIClientCredentials)
            .endpoint(azureOpenAIClientEndpoint)
            .buildAsyncClient();

        // Create the chat completion service
        ChatCompletionService openAIChatCompletion = OpenAIChatCompletion.builder()
            .withOpenAIAsyncClient(client)
            .withModelId(modelId)
            .build();

        // Initialize the kernel
        Kernel kernel = Kernel.builder()
            .withAIService(ChatCompletionService.class, openAIChatCompletion)
            .build();
    }
}
