package springAi.learning.ServiceImpl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import springAi.learning.Service.ChatService;
import springAi.learning.Util.ImageSaveUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    private final OpenAiChatModel openAiChatModel;
    private final WebClient webClient;
    private final WebClient.Builder webClientBuilder;

    @Value("${spring.ai.google.api-key}")
    private String apiKey;

    private static final String GOOGLE_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp-image-generation:generateContent";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ChatServiceImpl(OpenAiChatModel openAiChatModel, WebClient webClient, WebClient.Builder webClientBuilder) {
        this.openAiChatModel = openAiChatModel;
        this.webClient = webClient;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public String getChatResponse(String message) {
        return openAiChatModel.call(message);
    }

    @Override
    public Mono<String> getGeminiResponse(String message,List<String> responseModalities) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.just("Google AI API Key가 설정되지 않았습니다.");
        }

        // Google Gemini API가 요구하는 JSON 형식 변환하기
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", message))
                )),
                "generationConfig", Map.of(
                        "responseModalities", responseModalities
                )
        );
        /*
        원하면 해당 값을 넣어 상세히 조절 가능
                        "temperature", 1,
                        "topP", 0.95,
                        "topK", 40,
                        "maxOutputTokens", 8192
         */

        /*
        WebClient를 활용하여 Google Gemini API에 요청을 보내고 응답을 JSON → Map으로 변환하는 코드
        retrieve() :  WebClient가 요청을 보내고 응답을 받을 준비함
        bodyToMono() : 응답 데이터를 비동기적으로 변환하는 Reactive 방식의 Mono 객체를 반환
          -> Map.class 지정시 Map<String, Object> 형태로 반환
        block() : 즉시 응답을 받아와 동기 방식으로 처리함
        */

        return webClientBuilder
                .baseUrl(GOOGLE_API_URL + "?key=" + apiKey)
                .exchangeStrategies(
                        builder -> builder
                                .codecs(configurer -> configurer
                                        .defaultCodecs()
                                        .maxInMemorySize(10 * 1024 * 1024) // 10MB
                                )
                )
                .build()
                .post()
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        status-> !status.is2xxSuccessful(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Gemini API 오류 응답: " + errorBody)))
                )
                .bodyToMono(String.class)
                .switchIfEmpty(Mono.just("Google AI API 응답 비어 있음"))
                .doOnError(e -> log.error("Google AI API 호출 중 예외 발생", e))
                .onErrorResume(e -> Mono.just("Google AI API 호출 중 예외 발생: " + e.getMessage()));
    }

    /**
     * Google Gemini API 응답에서 'text' 데이터만 추출하여 String으로 반환
     */
    private String extractTextFromGeminiResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("candidates")) {
            return "Google AI 응답 없음";
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates.isEmpty()) {
            return "Google AI 응답 없음";
        }

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null || !content.containsKey("parts")) {
            return "Google AI 응답 없음";
        }

        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts.isEmpty() || !parts.get(0).containsKey("text")) {
            return "Google AI 응답 없음";
        }

        return (String) parts.get(0).get("text");
    }

    @Override
    public Mono<String> getImageResponse(String prompt) {
        WebClient webClient = webClientBuilder.baseUrl("http://localhost:7860").build();

        return webClient.post().uri("/sdapi/v1/txt2img")
                .bodyValue(new Txt2ImgRequest(prompt,256,256,20))
                .retrieve()
                .bodyToMono(String.class);
    }
    record Txt2ImgRequest(
            @JsonProperty("prompt") String prompt,
            @JsonProperty("width") int width,
            @JsonProperty("height") int height,
            @JsonProperty("steps") int steps
    ) {}

    @Override
    public Mono<String> getGeminiResponseAndSaveImage(String geminiResponseJson) {
        return Mono.fromCallable(() -> {
            String outputPath = ImageSaveUtil.createOutputPath("Image_save_forGemini", "_gemini");
            try {
                ImageSaveUtil.saveGeminiImageFromResponse(geminiResponseJson, outputPath);
                return "저장 완료! 파일 경로: " + outputPath;
            } catch (Exception e) {
                return "Gemini 이미지 저장 실패: " + e.getMessage();
            }
        });
    }

    @Override
    public Mono<String> getSdImageResponseAndSave(String sdResponseJson) {
        return Mono.fromCallable(() -> {
            String outputPath = ImageSaveUtil.createOutputPath("Image_save_forSD", "_SD");
            try {
                ImageSaveUtil.saveBase64ToFile(sdResponseJson, outputPath);
                return "저장 완료! 파일 경로: " + outputPath;
            } catch (Exception e) {
                return "SD 이미지 저장 실패: " + e.getMessage();
            }
        });
    }
}
