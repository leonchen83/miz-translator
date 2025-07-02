package org.example;

import static org.example.Strings.trim;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.example.impl.OpenAITranslatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baoyi Chen
 */
public class Translators {
	
	static Logger logger = LoggerFactory.getLogger(Translators.class);
	
	private Configure configure;
	private Set<String> nounsSet;
	
	public Translators(Configure configure, Set<String> nounsSet) {
		this.configure = configure;
		this.nounsSet = nounsSet;
	}
	
	public Translator getTranslator() {
		Translator r = null;
		switch (configure.getTranslator()) {
			case "deepseek":
			case "doubao":
			case "openai":
				r = new OpenAITranslatorImpl(configure, nounsSet);
				break;
			default:
				r = new OpenAITranslatorImpl(configure, nounsSet);
				break;
		}
		r.setModel(configure.getModel());
		r.setBaseUrl(configure.getBaseURL());
		r.setHints(configure.getHint());
		r.setApiKey(configure.getApiKey());
		r.setMaxTokens(configure.getMaxTokens());
		r.setTemperature(configure.getTemperature());
		return new XTranslator(r);
	}
	
	private static class XTranslator implements Translator {
		
		private Translator translator;
		
		public XTranslator(Translator translator) {
			this.translator = translator;
		}
		
		@Override
		public void start() {
			translator.start();
		}
		
		@Override
		public void stop() {
			translator.stop();
		}
		
		@Override
		public int hintSize() {
			return translator.hintSize();
		}
		
		@Override
		public void setHints(String hints) {
			translator.setHints(hints);
		}
		
		@Override
		public String translate(String text, Map<String, String> options) {
			logger.info("[TRANSLATING] {}", text);
			String r = null;
			try {
				r = translator.translate(text, null);
			} catch (Exception e) {
				logger.error("[FAILED] translate: {}, cause :{}", text, e.getMessage());
				r = text;
			}
			logger.info("[TRANSLATED] {}", r);
			r = trim(r, '\n');
			if (options != null) {
				options.put(text, r);
			}
			return r;
		}
		
		@Override
		public void setApiKey(String apiKey) {
			translator.setApiKey(apiKey);
		}
		
		@Override
		public void setMaxTokens(int maxTokens) {
			translator.setMaxTokens(maxTokens);
		}
		
		@Override
		public void setBaseUrl(String baseUrl) {
			translator.setBaseUrl(baseUrl);
		}
		
		@Override
		public void setModel(String model) {
			translator.setModel(model);
		}
		
		@Override
		public void setTemperature(double temperature) {
			translator.setTemperature(temperature);
		}
		
		@Override
		public List<Map.Entry<String, String>> translates(List<Map.Entry<String, String>> texts, Map<String, String> options) {
			logger.info("[TRANSLATING] {}", texts);
			for(int i = 0; i < 5; i++) {
				try {
					List<Map.Entry<String, String>> r = translator.translates(texts, options);
					logger.info("[TRANSLATED] {}", r);
					return r;
				} catch (Exception e) {
					logger.error("[FAILED] retry: {}, translate: {}, cause :{}", i, texts, e.getMessage());
				}
			}
			throw new IllegalStateException("Failed to translate after 5 retries: " + texts);
		}
	}
}
