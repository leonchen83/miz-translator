package org.example.impl;

import java.io.IOException;
import java.net.Proxy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.volcengine.ark.runtime.Const;
import com.volcengine.ark.runtime.exception.ArkAPIError;
import com.volcengine.ark.runtime.exception.ArkException;
import com.volcengine.ark.runtime.exception.ArkHttpException;
import com.volcengine.ark.runtime.interceptor.ArkResourceStsAuthenticationInterceptor;
import com.volcengine.ark.runtime.interceptor.AuthenticationInterceptor;
import com.volcengine.ark.runtime.interceptor.BatchInterceptor;
import com.volcengine.ark.runtime.interceptor.RequestIdInterceptor;
import com.volcengine.ark.runtime.interceptor.RetryInterceptor;
import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionChunk;
import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionRequest;
import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionResult;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChunk;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionResult;
import com.volcengine.ark.runtime.model.content.generation.CreateContentGenerationTaskRequest;
import com.volcengine.ark.runtime.model.content.generation.CreateContentGenerationTaskResult;
import com.volcengine.ark.runtime.model.content.generation.DeleteContentGenerationTaskRequest;
import com.volcengine.ark.runtime.model.content.generation.DeleteContentGenerationTaskResponse;
import com.volcengine.ark.runtime.model.content.generation.GetContentGenerationTaskRequest;
import com.volcengine.ark.runtime.model.content.generation.GetContentGenerationTaskResponse;
import com.volcengine.ark.runtime.model.content.generation.ListContentGenerationTasksRequest;
import com.volcengine.ark.runtime.model.content.generation.ListContentGenerationTasksResponse;
import com.volcengine.ark.runtime.model.context.CreateContextRequest;
import com.volcengine.ark.runtime.model.context.CreateContextResult;
import com.volcengine.ark.runtime.model.context.chat.ContextChatCompletionRequest;
import com.volcengine.ark.runtime.model.embeddings.EmbeddingRequest;
import com.volcengine.ark.runtime.model.embeddings.EmbeddingResult;
import com.volcengine.ark.runtime.model.multimodalembeddings.MultimodalEmbeddingRequest;
import com.volcengine.ark.runtime.model.multimodalembeddings.MultimodalEmbeddingResult;
import com.volcengine.ark.runtime.model.tokenization.TokenizationRequest;
import com.volcengine.ark.runtime.model.tokenization.TokenizationResult;
import com.volcengine.ark.runtime.service.ArkApi;
import com.volcengine.ark.runtime.service.ArkBaseService;
import com.volcengine.ark.runtime.service.ArkBaseServiceImpl;
import com.volcengine.ark.runtime.utils.ResponseBodyCallback;
import com.volcengine.ark.runtime.utils.SSE;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author Baoyi Chen
 */
public class XArkService extends ArkBaseService implements ArkBaseServiceImpl {
	private static final ObjectMapper mapper = defaultObjectMapper();
	private final ArkApi api;
	private final ExecutorService executorService;
	
	public XArkService(final String apiKey) {
		this(apiKey, DEFAULT_TIMEOUT);
	}
	
	public XArkService(final String apiKey, final Duration timeout) {
		ObjectMapper mapper = defaultObjectMapper();
		OkHttpClient client = defaultApiKeyClient(apiKey, timeout);
		Retrofit retrofit = defaultRetrofit(client, mapper, BASE_URL, null);
		
		this.api = retrofit.create(ArkApi.class);
		this.executorService = client.dispatcher().executorService();
	}
	
	public XArkService(final String ak, final String sk) {
		this(ak, sk, DEFAULT_TIMEOUT);
	}
	
	public XArkService(final String ak, final String sk, final Duration timeout) {
		ObjectMapper mapper = defaultObjectMapper();
		OkHttpClient client = defaultResourceStsClient(ak, sk, timeout, BASE_REGION);
		Retrofit retrofit = defaultRetrofit(client, mapper, BASE_URL, null);
		
		this.api = retrofit.create(ArkApi.class);
		this.executorService = client.dispatcher().executorService();
	}
	
	public XArkService(final ArkApi api) {
		this.api = api;
		this.executorService = null;
	}
	
	public XArkService(final ArkApi api, final ExecutorService executorService) {
		this.api = api;
		this.executorService = executorService;
	}
	
	public static ObjectMapper defaultObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		return mapper;
	}
	
	public static OkHttpClient defaultApiKeyClient(String apiKey, Duration timeout) {
		return new OkHttpClient.Builder()
				.addInterceptor(new AuthenticationInterceptor(apiKey))
				.addInterceptor(new RequestIdInterceptor())
				.addInterceptor(new RetryInterceptor(DEFAULT_RETRY_TIMES))
				.connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
				.readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
				.build();
	}
	
	public static OkHttpClient defaultResourceStsClient(String ak, String sk, Duration timeout, String region) {
		return new OkHttpClient.Builder()
				.addInterceptor(new ArkResourceStsAuthenticationInterceptor(ak, sk, region))
				.addInterceptor(new RequestIdInterceptor())
				.addInterceptor(new RetryInterceptor(DEFAULT_RETRY_TIMES))
				.connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
				.readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
				.build();
	}
	
	public static Retrofit defaultRetrofit(OkHttpClient client, ObjectMapper mapper, String baseUrl, Executor callbackExecutor) {
		Retrofit.Builder builder = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.client(client)
				.addConverterFactory(JacksonConverterFactory.create(mapper))
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
		
		if (callbackExecutor != null) {
			// to avoid NPE
			builder.callbackExecutor(callbackExecutor);
		}
		
		return builder.build();
	}
	
	public static <T> T execute(Single<T> apiCall) {
		try {
			T resp = apiCall.blockingGet();
			return resp;
		} catch (HttpException e) {
			String requestId = "";
			try {
				Headers headers = e.response().raw().request().headers();
				requestId = headers.get(Const.CLIENT_REQUEST_HEADER);
			} catch (Exception ignored) {}
			
			try {
				if (e.response() == null || e.response().errorBody() == null) {
					throw e;
				}
				String errorBody = e.response().errorBody().string();
				
				ArkAPIError error = mapper.readValue(errorBody, ArkAPIError.class);
				throw new ArkHttpException(error, e, e.code(), requestId);
			} catch (IOException ex) {
				throw e;
			}
		}
	}
	
	public static Flowable<SSE> stream(Call<ResponseBody> apiCall) {
		return stream(apiCall, false);
	}
	
	public static Flowable<SSE> stream(Call<ResponseBody> apiCall, boolean emitDone) {
		return Flowable.create(emitter -> apiCall.enqueue(new ResponseBodyCallback(emitter, emitDone)), BackpressureStrategy.BUFFER);
	}
	
	public static <T> Flowable<T> stream(Call<ResponseBody> apiCall, Class<T> cl) {
		return stream(apiCall).map(sse -> mapper.readValue(sse.getData(), cl));
	}
	
	public ChatCompletionResult createChatCompletion(ChatCompletionRequest request) {
		return execute(api.createChatCompletion(request, request.getModel(), new HashMap<>()));
	}
	
	public ChatCompletionResult createBatchChatCompletion(ChatCompletionRequest request) {
		return execute(api.createBatchChatCompletion(request, request.getModel(), new HashMap<>()));
	}
	
	public ChatCompletionResult createChatCompletion(ChatCompletionRequest request, Map<String, String> customHeaders) {
		return execute(api.createChatCompletion(request, request.getModel(), customHeaders));
	}
	
	public Flowable<ChatCompletionChunk> streamChatCompletion(ChatCompletionRequest request) {
		request.setStream(true);
		
		return stream(api.createChatCompletionStream(request, request.getModel(), new HashMap<>()), ChatCompletionChunk.class);
	}
	
	public Flowable<ChatCompletionChunk> streamChatCompletion(ChatCompletionRequest request, Map<String, String> customHeaders) {
		request.setStream(true);
		
		return stream(api.createChatCompletionStream(request, request.getModel(), customHeaders), ChatCompletionChunk.class);
	}
	
	public EmbeddingResult createEmbeddings(EmbeddingRequest request) {
		return execute(api.createEmbeddings(request, request.getModel(), new HashMap<>()));
	}
	
	public EmbeddingResult createEmbeddings(EmbeddingRequest request, Map<String, String> customHeaders) {
		return execute(api.createEmbeddings(request, request.getModel(), customHeaders));
	}
	
	public MultimodalEmbeddingResult createMultiModalEmbeddings(MultimodalEmbeddingRequest request) {
		return execute(api.createMultiModalEmbeddings(request, request.getModel(), new HashMap<>()));
	}
	
	public MultimodalEmbeddingResult createMultiModalEmbeddings(MultimodalEmbeddingRequest request, Map<String, String> customHeaders) {
		return execute(api.createMultiModalEmbeddings(request, request.getModel(), customHeaders));
	}
	
	@Override
	public BotChatCompletionResult createBotChatCompletion(BotChatCompletionRequest request) {
		return execute(api.createBotChatCompletion(request, request.getModel(), new HashMap<>()));
	}
	
	public BotChatCompletionResult createBotChatCompletion(BotChatCompletionRequest request, Map<String, String> customHeaders) {
		return execute(api.createBotChatCompletion(request, request.getModel(), customHeaders));
	}
	
	
	@Override
	public Flowable<BotChatCompletionChunk> streamBotChatCompletion(BotChatCompletionRequest request) {
		request.setStream(true);
		return stream(api.createBotChatCompletionStream(request, request.getModel(), new HashMap<>()), BotChatCompletionChunk.class);
	}
	
	public Flowable<BotChatCompletionChunk> streamBotChatCompletion(BotChatCompletionRequest request, Map<String, String> customHeaders) {
		request.setStream(true);
		return stream(api.createBotChatCompletionStream(request, request.getModel(), customHeaders), BotChatCompletionChunk.class);
	}
	
	@Override
	public CreateContextResult createContext(CreateContextRequest request) {
		return execute(api.createContext(request, request.getModel(), new HashMap<>()));
	}
	
	public CreateContextResult createContext(CreateContextRequest request, Map<String, String> customHeaders) {
		return execute(api.createContext(request, request.getModel(), customHeaders));
	}
	
	@Override
	public ChatCompletionResult createContextChatCompletion(ContextChatCompletionRequest request) {
		return execute(api.createContextChatCompletion(request, request.getModel(), new HashMap<>()));
	}
	
	public ChatCompletionResult createContextChatCompletion(ContextChatCompletionRequest request, Map<String, String> customHeaders) {
		return execute(api.createContextChatCompletion(request, request.getModel(), customHeaders));
	}
	
	@Override
	public Flowable<ChatCompletionChunk> streamContextChatCompletion(ContextChatCompletionRequest request) {
		request.setStream(true);
		return stream(api.createContextChatCompletionStream(request, request.getModel(), new HashMap<>()), ChatCompletionChunk.class);
	}
	
	public Flowable<ChatCompletionChunk> streamContextChatCompletion(ContextChatCompletionRequest request, Map<String, String> customHeaders) {
		request.setStream(true);
		return stream(api.createContextChatCompletionStream(request, request.getModel(), customHeaders), ChatCompletionChunk.class);
	}
	
	public TokenizationResult createTokenization(TokenizationRequest request) {
		return execute(api.createTokenization(request, request.getModel(), new HashMap<>()));
	}
	
	public TokenizationResult createTokenization(TokenizationRequest request, Map<String, String> customHeaders) {
		return execute(api.createTokenization(request, request.getModel(), customHeaders));
	}
	
	public CreateContentGenerationTaskResult createContentGenerationTask(CreateContentGenerationTaskRequest request) {
		return execute(api.createContentGenerationTask(request, request.getModel(), new HashMap<>()));
	}
	
	public CreateContentGenerationTaskResult createContentGenerationTask(CreateContentGenerationTaskRequest request, Map<String, String> customHeaders) {
		return execute(api.createContentGenerationTask(request, request.getModel(), customHeaders));
	}
	
	
	public GetContentGenerationTaskResponse getContentGenerationTask(GetContentGenerationTaskRequest request) {
		return execute(api.getContentGenerationTask(request.getTaskId(), new HashMap<>()));
	}
	
	public GetContentGenerationTaskResponse getContentGenerationTask(GetContentGenerationTaskRequest request, Map<String, String> customHeaders) {
		return execute(api.getContentGenerationTask(request.getTaskId(), customHeaders));
	}
	
	@Override
	public ListContentGenerationTasksResponse listContentGenerationTasks(ListContentGenerationTasksRequest request) {
		return execute(
				api.listContentGenerationTasks(
						request.getPageNum(),
						request.getPageSize(),
						request.getStatus(),
						request.getModel(),
						request.getTaskIds(),
						new HashMap<>()
				)
		);
	}
	
	public ListContentGenerationTasksResponse listContentGenerationTasks(
			ListContentGenerationTasksRequest request,
			Map<String, String> customHeaders
	) {
		return execute(
				api.listContentGenerationTasks(
						request.getPageNum(),
						request.getPageSize(),
						request.getStatus(),
						request.getModel(),
						request.getTaskIds(),
						customHeaders
				)
		);
	}
	
	
	public DeleteContentGenerationTaskResponse deleteContentGenerationTask(DeleteContentGenerationTaskRequest request) {
		return execute(api.deleteContentGenerationTask(request.getTaskId(), new HashMap<>()));
	}
	
	public DeleteContentGenerationTaskResponse deleteContentGenerationTask(DeleteContentGenerationTaskRequest request, Map<String, String> customHeaders) {
		return execute(api.deleteContentGenerationTask(request.getTaskId(), customHeaders));
	}
	
	public void shutdownExecutor() {
		Objects.requireNonNull(this.executorService, "executorService must be set in order to shut down");
		this.executorService.shutdown();
	}
	
	public static XArkService.Builder builder() {
		return new XArkService.Builder();
	}
	
	public static class Builder {
		private String ak;
		private String sk;
		private String apiKey;
		private String region = BASE_REGION;
		private String baseUrl = BASE_URL;
		private Duration timeout = DEFAULT_TIMEOUT;
		private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		private int retryTimes = DEFAULT_RETRY_TIMES;
		private Proxy proxy;
		private ConnectionPool connectionPool;
		private Dispatcher dispatcher;
		private Executor callbackExecutor;
		
		public XArkService.Builder ak(String ak) {
			this.ak = ak;
			return this;
		}
		
		public XArkService.Builder sk(String sk) {
			this.sk = sk;
			return this;
		}
		
		public XArkService.Builder apiKey(String apiKey) {
			this.apiKey = apiKey;
			return this;
		}
		
		public XArkService.Builder region(String region) {
			this.region = region;
			return this;
		}
		
		public XArkService.Builder baseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
			if (!baseUrl.endsWith("/")) {
				this.baseUrl = baseUrl + "/";
			}
			return this;
		}
		
		public XArkService.Builder timeout(Duration timeout) {
			this.timeout = timeout;
			return this;
		}
		
		public XArkService.Builder connectTimeout(Duration connectTimeout) {
			this.connectTimeout = connectTimeout;
			return this;
		}
		
		public XArkService.Builder retryTimes(int retryTimes) {
			this.retryTimes = retryTimes;
			return this;
		}
		
		public XArkService.Builder proxy(Proxy proxy) {
			this.proxy = proxy;
			return this;
		}
		
		public XArkService.Builder connectionPool(ConnectionPool connectionPool) {
			this.connectionPool = connectionPool;
			return this;
		}
		
		public XArkService.Builder dispatcher(Dispatcher dispatcher) {
			this.dispatcher = dispatcher;
			return this;
		}
		
		public XArkService.Builder callbackExecutor(Executor callbackExecutor) {
			this.callbackExecutor = callbackExecutor;
			return this;
		}
		
		public XArkService build() {
			ObjectMapper mapper = defaultObjectMapper();
			OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
			if (apiKey != null && apiKey.length() > 0) {
				clientBuilder.addInterceptor(new AuthenticationInterceptor(apiKey));
			} else if (ak != null && sk != null && ak.length() > 0 && sk.length() > 0) {
				clientBuilder.addInterceptor(new ArkResourceStsAuthenticationInterceptor(ak, sk, region));
			} else {
				throw new ArkException("missing api_key or ak&sk.");
			}
			
			if (proxy != null) {
				clientBuilder.proxy(proxy);
			}
			
			if (connectionPool != null) {
				clientBuilder.connectionPool(connectionPool);
			} else {
				clientBuilder.connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS));
			}
			
			if (dispatcher != null) {
				clientBuilder.dispatcher(dispatcher);
			}
			
			OkHttpClient client = clientBuilder
					.addInterceptor(new RequestIdInterceptor())
					.addInterceptor(new RetryInterceptor(retryTimes))
					.addInterceptor(new LoggingInterceptor())
					.addInterceptor(new BatchInterceptor())
					.readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
					.callTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
					.connectTimeout(connectTimeout)
					.build();
			Retrofit retrofit = defaultRetrofit(client, mapper, baseUrl, callbackExecutor);
			
			return new XArkService(
					retrofit.create(ArkApi.class),
					client.dispatcher().executorService()
			);
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger("HTTP_LOGGER");
	
	public static class LoggingInterceptor implements Interceptor {
		
		@Override
		public Response intercept(Chain chain) throws IOException {
			Request request = chain.request();
			
			long startTime = System.currentTimeMillis();
			if (request.body() != null) {
				logger.debug("[REQ] Headers: {}", request.headers());
				logger.debug("[REQ] Body: {}", bodyToString(request.body()));
			}
			
			Response response = chain.proceed(request);
			long endTime = System.currentTimeMillis();
			
			ResponseBody responseBody = response.body();
			String responseBodyString = responseBody != null ? responseBody.string() : "null";
			
			logger.debug("[RES] Code: {}", response.code());
			logger.debug("[RES] Headers: {}", response.headers());
			logger.debug("[RES] Body: {}", responseBodyString);
			logger.debug("[REQ -> RES] in {} ms", response.request().url(), endTime - startTime);
			
			return response.newBuilder()
					.body(ResponseBody.create(responseBody.contentType(), responseBodyString))
					.build();
		}
		
		private String bodyToString(RequestBody body) {
			try {
				Buffer buffer = new Buffer();
				body.writeTo(buffer);
				return buffer.readUtf8();
			} catch (IOException e) {
				return "Could not read request body";
			}
		}
	}
}