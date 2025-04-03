package springAi.learning.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import springAi.learning.DTO.Base64ImageDTO;
import springAi.learning.DTO.FoodInfoDTO;
import springAi.learning.Service.ChatService;
import springAi.learning.Util.ImageSaveUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/api")
@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService){
        this.chatService=chatService;
    }

    @PostMapping("/chat")
    public Mono<Map<String, String>> messageChat(@RequestBody String message){
        Mono<String> chatGptResponse = Mono.just(chatService.getChatResponse(message));
        Mono<String> GeminiResponse = chatService.getGeminiResponse(message, List.of("TEXT"));

        return Mono.zip(chatGptResponse, GeminiResponse)
                .map(tuple -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("ChatGpt 응답", tuple.getT1());
                    response.put("Gemini 응답", tuple.getT2());
                    return response;
                });
    }

    @PostMapping("/gemini-image")
    public Mono<String> generateGeminiImage(@RequestBody String param){
        return chatService.getGeminiResponse(param,List.of("TEXT","IMAGE"));
    }

    @PostMapping("/sd-image")
    public Mono<String> generateImage(@RequestBody String param){
            return chatService.getImageResponse(param);
    }

    @PostMapping("/save-sd-image")
    public Mono<String> uploadBase64(@RequestBody Base64ImageDTO request) {
        try {
            return chatService.getSdImageResponseAndSave(request.getImages()[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return Mono.just("실패: " + e.getMessage());
        }
    }

    @PostMapping("/save-gemini-image")
    public Mono<String> saveGeminiImage(@RequestBody String geminiResponseJson) {
        try {
            return chatService.getGeminiResponseAndSaveImage(geminiResponseJson);
        } catch (Exception e) {
            return Mono.just("파일 저장 실패:"+e);
        }
    }

    @PostMapping(value = "/food-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<List<FoodInfoDTO>> generateFoodInfo(@RequestPart("image") MultipartFile image) {
        return chatService.getFoodInfo(image)
                .onErrorResume(e -> {
                    // 로그 남기고 빈 리스트 반환 (또는 적절한 예외 처리)
                    log.error("음식 정보 생성 실패", e);
                    return Mono.just(List.of());  // 또는 Mono.error(e)로 에러 그대로 던질 수도 있음
                });
    }

}
