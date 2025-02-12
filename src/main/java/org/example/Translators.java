package org.example;

import static org.example.Strings.trim;

import org.example.impl.DeepSeekTranslatorImpl;
import org.example.impl.OpenAITranslatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baoyi Chen
 */
public class Translators {
	
	static Logger logger = LoggerFactory.getLogger(Translators.class);
	
	private Configure configure;
	
	public Translators(Configure configure) {
		this.configure = configure;
	}
	
	public Translator getTranslator() {
		Translator r = null;
		switch (configure.getTranslator()) {
			case "deepseek":
			case "doubao":
				r = new DeepSeekTranslatorImpl();
				break;
			case "openai":
				r = new OpenAITranslatorImpl();
				break;
			default:
				throw new UnsupportedOperationException();
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
		public void setHints(String hints) {
			translator.setHints(hints);
		}
		
		@Override
		public String translate(String text) {
			logger.info("[TRANSLATING] {}", text);
			String r = null;
			try {
				r = translator.translate(text);
			} catch (Exception e) {
				logger.error("[FAILED] translate: {}, cause :{}", text, e.getMessage());
				r = text;
			}
			logger.info("[TRANSLATED] {}", r);
			return trim(r, '\n');
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
	}
}
