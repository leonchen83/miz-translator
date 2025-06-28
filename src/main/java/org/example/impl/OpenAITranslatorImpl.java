package org.example.impl;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;

/**
 * @author Baoyi Chen
 */
public class OpenAITranslatorImpl extends AbstractTranslator {
	private static final Logger logger = LoggerFactory.getLogger(AbstractTranslator.class);
	private OpenAIClient client;
	
	public OpenAITranslatorImpl(Configure configure) {
		super(configure);
	}
	
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
		
		if (!text.contains(SPLITER)) {
			r = r.replaceAll(SPLITER, "");
		}
		if (options != null) {
			options.put(text, r);
		}
		return r;
	}
	
	@Override
	public List<Map.Entry<String, String>> translates(List<Map.Entry<String, String>> texts, Map<String, String> options) {
		if (texts.size() < 4) {
			logger.warn("Less than 4 texts to translate, fallback to individual translation. texts:{}", texts);
			return fallbackTranslates(texts, options);
		}
		List<String> values = texts.stream().map(Map.Entry::getValue).toList();
		String joinedText = String.join(SPLITER, values);
		String translatedText = translate(joinedText, null);
		String[] parts = translatedText.split(SPLITER);
		if (parts.length != values.size()) {
			logger.warn("Translated text parts count {} does not match original values count {}. split translation", parts.length, values.size());
			int half = values.size() / 2;
			List<Map.Entry<String, String>> list1 = new ArrayList<>(half);
			List<Map.Entry<String, String>> list2 = new ArrayList<>(texts.size() - half);
			for (int i = 0; i < half; i++) {
				list1.add(texts.get(i));
			}
			for (int i = half; i < texts.size(); i++) {
				list2.add(texts.get(i));
			}
			List<Map.Entry<String, String>> t1 = translates(list1, options);
			List<Map.Entry<String, String>> t2 = translates(list2, options);
			List<Map.Entry<String, String>> combined = new ArrayList<>(t1.size() + t2.size());
			combined.addAll(t1);
			combined.addAll(t2);
			return combined;
		}
		return texts.stream()
				.map(entry -> {
					int index = values.indexOf(entry.getValue());
					options.put(entry.getValue(), parts[index]);
					return Map.entry(entry.getKey(), parts[index]);
				})
				.toList();
	}
	
	private List<Map.Entry<String, String>> fallbackTranslates(List<Map.Entry<String, String>> texts, Map<String, String> options) {
		List<Map.Entry<String, String>> r = new ArrayList<>(texts.size());
		for (Map.Entry<String, String> entry : texts) {
			r.add(Map.entry(entry.getKey(), translate(entry.getValue(), options)));
		}
		return r;
	}
	
	private String localeLanguage() {
		String locale = configure.getLanguageCode();
		if (locale == null || locale.isEmpty() || "zh".equals(locale)) {
			return "。保留原分隔符" + SPLITER + "例如：片段1" + SPLITER + "片段2" + SPLITER + "片段3";
		} else if( "ja".equals(locale)) {
			return "。区切り文字を保持します" + SPLITER + "例：フラグメント1" + SPLITER + "フラグメント2" + SPLITER + "フラグメント3";
		} else if ("ko".equals(locale)) {
			return "。구분 기호를 유지합니다" + SPLITER + "예: 조각 1" + SPLITER + "조각 2" + SPLITER + "조각 3";
		} else {
			return "。保留原分隔符" + SPLITER + "例如：片段1" + SPLITER + "片段2" + SPLITER + "片段3";
		}
	}
}
