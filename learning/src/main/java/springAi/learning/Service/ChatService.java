package springAi.learning.Service;

import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import springAi.learning.DTO.FoodInfoDTO;

import java.util.List;
import java.util.Map;

public interface ChatService{
    String getChatResponse(String message);
    Mono<String> getImageResponse(String param);
    Mono<String> getGeminiResponse(String param, List<String> responseModalities);
    Mono<String> getGeminiResponseAndSaveImage(String message);
    Mono<String> getSdImageResponseAndSave(String prompt);
    Mono<String> getFoodInfo(MultipartFile image);

}
