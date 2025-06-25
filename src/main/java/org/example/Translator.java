package org.example;

import java.util.List;
import java.util.Map;

/**
 * @author Baoyi Chen
 */
public interface Translator {
	
	void start();
	
	void stop();
	
	void setHints(String hints);
	
	String translate(String text, Map<String, String> options);
	
	void setApiKey(String apiKey);
	
	void setMaxTokens(int maxTokens);
	
	void setBaseUrl(String baseUrl);
	
	void setModel(String model);
	
	void setTemperature(double temperature);
	
	List<Map.Entry<String, String>> translates(List<Map.Entry<String, String>> texts, Map<String, String> options);
}
