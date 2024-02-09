package com.langchain4j.example.openai;

import java.util.concurrent.CompletableFuture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.SystemMessage;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.model.output.Response;

import com.langchain4j.example.openai.ApiKeys;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;

@SpringBootApplication
public class ChatMemoryApplication {
	
	public static void main(String[] args) throws ExecutionException, InterruptedException {
		SpringApplication.run(ChatMemoryApplication.class, args);
		AzureOpenAiStreamingChatModel model = AzureOpenAiStreamingChatModel.builder()
		.apiKey(ApiKeys.AZURE_OPENAI_KEY)
		.endpoint(ApiKeys.AZURE_OPENAI_ENDPOINT)
		.deploymentName(ApiKeys.AZURE_OPENAI_DEPLOYMENT_NAME)
		.temperature(0.3)
		.logRequestsAndResponses(true)
		.build();
		/* First chat 
		String answer =model.generate("Provide 3 short bullet points explaining why Java is awesome");
		System.out.println(answer); */ 
		
		Tokenizer tokenizer = new OpenAiTokenizer(GPT_3_5_TURBO);
		ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(1000,tokenizer);
		
		//String userMessage = "Write a 100-word poem about Java and AI";
		UserMessage userMessage1 = userMessage("Write a 100-word poem about Java and AI");
		chatMemory.add(userMessage1);
		
		CompletableFuture<AiMessage> futureResponse = new CompletableFuture<>();
		
		StreamingResponseHandler<AiMessage> handler = new StreamingResponseHandler<AiMessage>() {
			@Override
			public void onNext(String token) {
				System.out.print(token);
			}
			
			@Override
			public void onComplete(Response<AiMessage> response) {
				futureResponse.complete(response.content());
			}
			
			@Override
			public void onError(Throwable t) {
			}
		};
		model.generate(chatMemory.messages(), handler);
		chatMemory.add(futureResponse.get());
		
		UserMessage userMessage2 = userMessage("Change the poem to be about Python and AI");
		chatMemory.add(userMessage2);
		model.generate(chatMemory.messages(), handler);
		
	}
	
}
