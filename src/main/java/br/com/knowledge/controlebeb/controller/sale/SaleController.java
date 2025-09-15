package br.com.knowledge.controlebeb.controller.sale;

import br.com.knowledge.controlebeb.dto.sale.SaleDTO;
import br.com.knowledge.controlebeb.model.sale.SaleStatus;
import br.com.knowledge.controlebeb.service.sale.SaleService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public ResponseEntity<List<SaleDTO>> getAll() {
        return ResponseEntity.ok(saleService.findAll());
    }

    @PostMapping
    public ResponseEntity<SaleDTO> create(@RequestBody SaleDTO dto) {
        return ResponseEntity.ok(saleService.createSale(dto));
    }

    // =========================
    // Alterar status da venda
    // =========================
    @PatchMapping("/{id}/status")
    public ResponseEntity<SaleDTO> updateStatus(@PathVariable Long id, @RequestParam SaleStatus status) {
        return ResponseEntity.ok(saleService.updateStatus(id, status));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SaleDTO>> getSalesByStatus(@PathVariable SaleStatus status) {
        return ResponseEntity.ok(saleService.getSalesByStatus(status));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SaleDTO>> getSalesByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(saleService.getSalesByCustomer(customerId));
    }

}
