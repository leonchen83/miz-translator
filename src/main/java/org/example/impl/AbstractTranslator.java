package org.example.impl;

import org.example.Configure;
import org.example.Translator;

import com.google.common.util.concurrent.RateLimiter;

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
	protected Configure configure;
	protected RateLimiter rateLimit;
	
	public AbstractTranslator(Configure configure) {
		this.configure = configure;
		this.rateLimit = RateLimiter.create(configure.getRateLimitPerMinute()/60.0);
	}
	
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
