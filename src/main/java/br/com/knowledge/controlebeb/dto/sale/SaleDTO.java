package br.com.knowledge.controlebeb.dto.sale;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.knowledge.controlebeb.model.sale.SaleStatus;

@Data
public class SaleDTO {

    private Long id;
    private Long customerId;
    private LocalDateTime saleDate;
    private List<SaleItemDTO> items;
    private SaleStatus status;
    private BigDecimal totalBruto;   // soma sem desconto
    private BigDecimal desconto;     // desconto aplicado
    private BigDecimal totalLiquido; // total final com desconto
}
