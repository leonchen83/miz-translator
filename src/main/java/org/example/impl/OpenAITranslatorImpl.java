package org.example.impl;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;

/**
 * @author Baoyi Chen
 */
public class OpenAITranslatorImpl extends AbstractTranslator {
	private OpenAIClient client;
	
	@Override
	public void start() {
		client = OpenAIOkHttpClient.builder().baseUrl(baseUrl).apiKey(apiKey).timeout(Duration.of(30, ChronoUnit.MINUTES)).build();
	}
	
	@Override
	public void stop() {
		if (client != null) client.close();
	}
	
	@Override
	public String translate(String text) {
		ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
				.addSystemMessage(hints)
				.addUserMessage(text)
				.model(model)
				.maxCompletionTokens(maxTokens);
		if (temperature >= 0) {
			builder.temperature(temperature);
		}
		ChatCompletionCreateParams params = builder.build();
		ChatCompletion response = client.chat().completions().create(params);
		return response.choices().get(0).message().content().orElseThrow();
	}
}
