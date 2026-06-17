package com.metaextract.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NvidiaApiClient {

    private static final String BASE_URL =
            "https://integrate.api.nvidia.com/v1/chat/completions";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String       apiKey;
    private final String       model;
    private final int          maxTokens;
    private final OkHttpClient http;
    private final ObjectMapper mapper;

    public NvidiaApiClient(String apiKey, String model, int maxTokens) {
        this.apiKey    = apiKey;
        this.model     = model;
        this.maxTokens = maxTokens;
        this.mapper    = new ObjectMapper();

        this.http = new OkHttpClient.Builder()
                .connectTimeout(60,  TimeUnit.SECONDS)
                .writeTimeout(60,    TimeUnit.SECONDS)
                .readTimeout(300,    TimeUnit.SECONDS)
                .build();
    }

    public String complete(String userPrompt) throws IOException {
        String requestBody = buildRequestBody(userPrompt);

        System.out.println("      Sending request to model: " + model);

        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type",  "application/json")
                .addHeader("Accept",        "application/json")
                .post(RequestBody.create(requestBody, JSON))
                .build();

        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null
                        ? response.body().string() : "(empty)";
                throw new IOException(
                    "NVIDIA API error " + response.code() + ": " + errorBody
                );
            }
            String body = response.body().string();
            return extractContent(body);
        }
    }

    private String buildRequestBody(String userPrompt) throws IOException {
        ObjectNode root = mapper.createObjectNode();
        root.put("model",       model);
        root.put("max_tokens",  maxTokens);
        root.put("temperature", 0.1);
        root.put("top_p",       0.9);

        ArrayNode messages = root.putArray("messages");

        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role",    "system");
        systemMsg.put("content", LlmPromptBuilder.SYSTEM_PROMPT.strip());

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role",    "user");
        userMsg.put("content", userPrompt);

        return mapper.writeValueAsString(root);
    }

    private String extractContent(String responseBody) throws IOException {
        JsonNode root = mapper.readTree(responseBody);

        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IOException(
                "Unexpected API response - no choices found: " + responseBody
            );
        }

        String content = choices.get(0)
                                .path("message")
                                .path("content")
                                .asText("");

        if (content.isBlank()) {
            throw new IOException(
                "LLM returned empty content. Full response: " + responseBody
            );
        }

        return stripMarkdownFences(content);
    }

    private String stripMarkdownFences(String text) {
        String trimmed = text.strip();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.lastIndexOf("```")).strip();
            }
        }
        return trimmed;
    }
}