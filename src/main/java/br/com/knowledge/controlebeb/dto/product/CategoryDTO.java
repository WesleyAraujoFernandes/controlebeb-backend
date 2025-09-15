package br.com.knowledge.controlebeb.dto.product;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {

    private Long id;

    //@NotBlank(message = "A descrição da categoria é obrigatória")
    private String description;
}
