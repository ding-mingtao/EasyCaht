package com.easychat.service;

import com.easychat.entity.config.Appconfig;
import com.easychat.utils.JsonUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: DeepSeekService
 * @Description: DeepSeek AI服务
 * @Author: 丁铭涛
 * @DateTime: 2026/2/24 10:34
 **/
@Service("deepSeekService")
public class DeepSeekService {
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekService.class);

    @Resource
    private Appconfig appconfig;

    private OkHttpClient client;

    @PostConstruct
    public void init() {
        // 配置 OkHttpClient 超时参数
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)    // 连接超时
                .readTimeout(60, TimeUnit.SECONDS)       // 读取超时
                .writeTimeout(30, TimeUnit.SECONDS)      // 写入超时
                .retryOnConnectionFailure(true)          // 连接失败时重试
                .build();
    }

    /**
     * 调用DeepSeek API获取AI回复
     * @param userMessage 用户消息
     * @return AI回复内容
     */
    public String getAIResponse(String userMessage) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", appconfig.getDeepseekModel());

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个智能聊天助手，能够友好地回答用户的各种问题。");
            messages.add(systemMessage);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            // 构建请求
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, JsonUtils.convertObj2Json(requestBody));
            Request request = new Request.Builder()
                    .url(appconfig.getDeepseekApiUrl())
                    .post(body)
                    .addHeader("Authorization", "Bearer " + appconfig.getDeepseekApiKey())
                    .build();

            // 执行请求
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                logger.error("DeepSeek API request failed: {} - {}", response.code(), response.message());
                return "抱歉，AI服务暂时不可用，请稍后再试。";
            }

            // 解析响应
            String responseBody = response.body().string();
            Map<String, Object> responseMap = JsonUtils.convertJson2Obj(responseBody, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                return (String) message.get("content");
            }

            return "抱歉，AI服务暂时不可用，请稍后再试。";

        } catch (IOException e) {
            logger.error("Error calling DeepSeek API", e);
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        } catch (Exception e) {
            logger.error("Error processing AI response", e);
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }
}
