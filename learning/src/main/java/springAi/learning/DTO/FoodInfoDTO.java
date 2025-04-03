package springAi.learning.DTO;

import lombok.Data;

import java.util.List;

@Data
public class FoodInfoDTO {
    private String food_name;
    private NutritionInfo nutritionInfo;
    private String description;

    @Data
    public static class NutritionInfo {
        private double calories;
        private double protein;
        private double fat;
        private double carbohydrates;
        private double calcium;
        private double omega_3;
    }
}
