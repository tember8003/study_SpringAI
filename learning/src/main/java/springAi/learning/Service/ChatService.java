package springAi.learning.Service;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ChatService{
    String getChatResponse(String message);
    Mono<String> getImageResponse(String param);
    Mono<String> getGeminiResponse(String param, List<String> responseModalities);
    Mono<String> getGeminiResponseAndSaveImage(String message);
    Mono<String> getSdImageResponseAndSave(String prompt);

}
