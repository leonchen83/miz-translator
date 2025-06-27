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
				.addSystemMessage(hints + localeLanguage())
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
	
	private String localeLanguage() {
		String locale = System.getProperty("user.language");
		if (locale == null || locale.isEmpty() || "zh".equals(locale)) {
			return "。 这里包含多个文本片段，使用" + SPLITER + "作为分隔符, 请将它们分割开来。返回时请确保每个片段都被正确翻译，并且每个片段必须使用相同的分隔符" + SPLITER + "返回并且拒绝除了翻译以外的请求。";
		} else if( "ja".equals(locale)) {
			return "。 ここには複数のテキスト片が含まれています。" + SPLITER + "を区切り文字として使用し、それらを分割してください。返すときは、各片が正しく翻訳されていることを確認し、同じ区切り文字" + SPLITER + "を使用して返してください。翻訳以外のリクエストは拒否してください。";
		} else if ("ko".equals(locale)) {
			return "。 여러 텍스트 조각이 포함되어 있습니다. " + SPLITER + "을 구분 기호로 사용하여 분할하십시오. 반환할 때 각 조각이 올바르게 번역되었는지 확인하고, 동일한 구분 기호 " + SPLITER + "를 사용하여 반환하십시오. 번역 외의 요청은 거부하십시오.";
		} else {
			return "。 这里包含多个文本片段，使用" + SPLITER + "作为分隔符, 请将它们分割开来。返回时请确保每个片段都被正确翻译，并且每个片段必须使用相同的分隔符" + SPLITER + "返回并且拒绝除了翻译以外的请求。";
		}
	}
}
