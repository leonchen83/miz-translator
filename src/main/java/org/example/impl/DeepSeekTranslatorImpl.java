package org.example.impl;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;

/**
 * @author Baoyi Chen
 */
public class DeepSeekTranslatorImpl extends AbstractTranslator {
	private XArkService client;
	
	@Override
	public void start() {
		ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
		Dispatcher dispatcher = new Dispatcher();
		client = XArkService.builder().dispatcher(dispatcher).connectionPool(connectionPool).baseUrl(baseUrl).apiKey(apiKey).timeout(Duration.of(30, ChronoUnit.MINUTES)).build();
	}
	
	@Override
	public void stop() {
		if (client != null) client.shutdownExecutor();
	}
	
	@Override
	public String translate(String text, Map<String, String> options) {
		final List<ChatMessage> messages = new ArrayList<>();
		final ChatMessage hint = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(hints).build();
		final ChatMessage splitHint = ChatMessage.builder()
				.role(ChatMessageRole.SYSTEM)
				.content("这里包含多个文本片段，请将它们分割开来，使用" + SPLITER + "作为分隔符。返回时请确保每个片段都被正确翻译，并且使用相同的分隔符。")
				.build();
		final ChatMessage chat = ChatMessage.builder().role(ChatMessageRole.USER).content(text).build();
		messages.add(hint);
		messages.add(splitHint);
		messages.add(chat);
		
		ChatCompletionRequest.Builder builder = ChatCompletionRequest.builder()
				.model(model)
				.maxTokens(maxTokens)
				.messages(messages);
		if (temperature >= 0) {
			builder.temperature(temperature);
		}
		
		ChatCompletionRequest request = builder.build();
		String r = client.createChatCompletion(request).getChoices().get(0).getMessage().getContent().toString();
		if (options != null) {
			options.put(text, r);
		}
		return r;
	}
}
