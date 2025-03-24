package springAi.learning.Controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import springAi.learning.DTO.Base64ImageDTO;
import springAi.learning.Service.ChatService;
import springAi.learning.Util.ImageSaveUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

}
