package br.com.knowledge.controlebeb.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerDTO {

    private Long id;

    @NotBlank(message = "O nome do cliente é obrigatório")
    private String name;

    @Email(message = "Email inválido")
    private String email;

    private String phone;
}
