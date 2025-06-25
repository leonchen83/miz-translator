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
		List<String> values = texts.stream().map(Map.Entry::getValue).toList();
		String joinedText = String.join(SPLITER, values);
		String translatedText = translate(joinedText, null);
		String[] parts = translatedText.split(SPLITER);
		if (parts.length != values.size()) {
			logger.warn("Translated text parts count {} does not match original values count {}. fallback to individual translation.", parts.length, values.size());
			List<Map.Entry<String, String>> r = new ArrayList<>(texts.size());
			for (Map.Entry<String, String> entry : texts) {
				r.add(Map.entry(entry.getKey(), translate(entry.getValue(), options)));
			}
			return r;
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
