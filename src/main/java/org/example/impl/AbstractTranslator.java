package org.example.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baoyi Chen
 */
public abstract class AbstractTranslator implements Translator {
	
	public static final String SPLITER = "%%%%%%";
	private static final Logger logger = LoggerFactory.getLogger(AbstractTranslator.class);
	
	protected String hints;
	protected String apiKey;
	protected int maxTokens;
	protected String model;
	protected String baseUrl;
	protected double temperature;
	
	@Override
	public void setHints(String hints) {
		this.hints = hints;
	}
	
	@Override
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	@Override
	public void setMaxTokens(int maxTokens) {
		this.maxTokens = maxTokens;
	}
	
	@Override
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	@Override
	public void setModel(String model) {
		this.model = model;
	}
	
	@Override
	public void setTemperature(double temperature) {
		this.temperature = temperature;
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
}
