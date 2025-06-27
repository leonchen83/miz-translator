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
}
