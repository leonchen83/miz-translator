package org.example;

import java.util.List;
import java.util.Map;

/**
 * @author Baoyi Chen
 */
public interface Translator {
	
	void start();
	
	void stop();
	
	int hintSize();
	
	void setHints(String hints);
	
	void setModel(String model);
	
	void setApiKey(String apiKey);
	
	void setMaxTokens(int maxTokens);
	
	void setBaseUrl(String baseUrl);
	
	void setTemperature(double temperature);
	
	String translate(String text, Map<String, String> options);
	
	List<Map.Entry<String, String>> translates(List<Map.Entry<String, String>> texts, Map<String, String> options);
}
