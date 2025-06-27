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
				.addSystemMessage(spliterLanguage())
				.addUserMessage(text)
				.model(model)
				.maxCompletionTokens(maxTokens);
		if (temperature >= 0) {
			builder.temperature(temperature);
		}
		ChatCompletionCreateParams params = builder.build();
		ChatCompletion response = client.chat().completions().create(params);
		String r = response.choices().get(0).message().content().orElseThrow();
		r = r.replaceAll(SPLITER, "");
		if (options != null) {
			options.put(text, r);
		}
		return r;
	}
	
	private String spliterLanguage() {
		String locale = System.getProperty("user.language");
		if (locale == null || locale.isEmpty() || "zh".equals(locale)) {
			return "这里包含多个文本片段，请将它们分割开来，使用" + SPLITER + "作为分隔符。返回时请确保每个片段都被正确翻译，并且使用相同的分隔符。";
		} else if( "ja".equals(locale)) {
			return "複数のテキストセグメントが含まれています。区切り文字として" + SPLITER + "を使用して、それらを分割してください。各セグメントが正しく翻訳され、同じ区切り文字を使用していることを確認してください。";
		} else if ("ko".equals(locale)) {
			return "여러 텍스트 조각이 포함되어 있습니다. " + SPLITER + "을(를) 구분 기호로 사용하여 조각을 분할하십시오. 각 조각이 올바르게 번역되고 동일한 구분 기호를 사용하고 있는지 확인하십시오.";
		} else {
			return "这里包含多个文本片段，请将它们分割开来，使用" + SPLITER + "作为分隔符。返回时请确保每个片段都被正确翻译，并且使用相同的分隔符。";
		}
	}
}
