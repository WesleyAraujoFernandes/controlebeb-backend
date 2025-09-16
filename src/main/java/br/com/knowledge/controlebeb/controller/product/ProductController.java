package br.com.knowledge.controlebeb.controller.product;

import br.com.knowledge.controlebeb.dto.product.ProductDTO;
import br.com.knowledge.controlebeb.service.product.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ============================
    // Listar todos os produtos
    // ============================
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    // ============================
    // Buscar produto por ID
    // ============================
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    // ============================
    // Criar um novo produto
    // ============================
    @PostMapping
    public ResponseEntity<ProductDTO> create(@RequestBody ProductDTO dto) {
        ProductDTO created = productService.save(dto);
        return ResponseEntity.ok(created);
    }

    // ============================
    // Atualizar produto existente
    // ============================
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @RequestBody ProductDTO dto) {
        ProductDTO updated = productService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    // ============================
    // Apagar produtos
    // ============================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
