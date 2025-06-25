package org.example.impl;

import java.util.List;
import java.util.Map;

import org.example.Translator;

/**
 * @author Baoyi Chen
 */
public abstract class AbstractTranslator implements Translator {
	
	public static final String SPLITER = "%%%%%%";
	
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
		List<String> values = texts.stream().map(Map.Entry::getValue).toList();
		String joinedText = String.join(SPLITER, values);
		String translatedText = translate(joinedText, null);
		String[] parts = translatedText.split(SPLITER);
		if (parts.length != values.size()) {
			throw new IllegalStateException("The number of translated parts does not match the number of input texts.");
		}
		return texts.stream()
				.map(entry -> {
					int index = values.indexOf(entry.getValue());
					options.put(entry.getValue(), parts[index]);
					return Map.entry(entry.getKey(), parts[index]);
				})
				.toList();
	}
}
