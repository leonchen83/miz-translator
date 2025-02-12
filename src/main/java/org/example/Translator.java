package org.example;

/**
 * @author Baoyi Chen
 */
public interface Translator {
	
	void start();
	
	void stop();
	
	void setHints(String hints);
	
	String translate(String text);
	
	void setApiKey(String apiKey);
	
	void setMaxTokens(int maxTokens);
	
	void setBaseUrl(String baseUrl);
	
	void setModel(String model);
	
	void setTemperature(double temperature);
}
