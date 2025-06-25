package org.example.impl;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

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
	public String translate(String text, Map<String, String> options) {
		ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
				.addSystemMessage(hints)
				.addSystemMessage("这里包含多个文本片段，请将它们分割开来，使用" + SPLITER + "作为分隔符。返回时请确保每个片段都被正确翻译，并且使用相同的分隔符。")
				.addUserMessage(text)
				.model(model)
				.maxCompletionTokens(maxTokens);
		if (temperature >= 0) {
			builder.temperature(temperature);
		}
		ChatCompletionCreateParams params = builder.build();
		ChatCompletion response = client.chat().completions().create(params);
		String r = response.choices().get(0).message().content().orElseThrow();
		if (options != null) {
			options.put(text, r);
		}
		return r;
	}
}
