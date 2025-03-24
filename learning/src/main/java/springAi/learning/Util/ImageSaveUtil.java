package springAi.learning.Util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageSaveUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void saveGeminiImageFromResponse(String json, String outputFilePath) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode dataNode = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("inlineData").path("data");

        if (dataNode.isMissingNode()) {
            throw new IllegalArgumentException("Gemini 응답에서 base64 데이터를 찾을 수 없습니다.");
        }

        saveBase64ToFile(dataNode.asText(), outputFilePath);
    }

    public static void saveBase64ToFile(String base64Data, String outputFilePath) throws Exception {
        // 필요 시 공백 제거
        base64Data = base64Data.replaceAll("\\s+", "");

        Path parentDir = Paths.get(outputFilePath).getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        try (OutputStream stream = new FileOutputStream(outputFilePath)) {
            stream.write(imageBytes);
            System.out.println("이미지 저장 성공: " + outputFilePath);
        }
    }

    public static String createOutputPath(String folderName, String suffix) {
        String baseFolder = System.getProperty("user.dir") + "\\" + folderName + "\\";
        File folder = new File(baseFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return baseFolder + System.currentTimeMillis() + suffix + ".png";
    }
}
