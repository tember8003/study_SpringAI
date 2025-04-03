package springAi.learning.DTO;

import lombok.Data;

import java.util.List;

@Data
public class FoodInfoListDTO {
    private List<FoodInfoDTO> foods;
}