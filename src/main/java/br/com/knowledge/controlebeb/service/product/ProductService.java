package br.com.knowledge.controlebeb.service.product;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.knowledge.controlebeb.dto.product.CategoryDTO;
import br.com.knowledge.controlebeb.dto.product.ProductDTO;
import br.com.knowledge.controlebeb.model.product.Category;
import br.com.knowledge.controlebeb.model.product.Product;
import br.com.knowledge.controlebeb.repository.CategoryRepository;
import br.com.knowledge.controlebeb.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private ProductDTO toDTO(Product product) {
        Category category = product.getCategory();
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setDescription(category.getDescription());

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setCategory(categoryDTO);
        dto.setMinimumStock(product.getMinimumStock());
        return dto;
    }

    private Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());

        Category category = categoryRepository.findById(dto.getCategory().getId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        product.setCategory(category);
        product.setMinimumStock(dto.getMinimumStock());
        return product;
    }

    // ============================
    // CRUD
    // ============================
    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public ProductDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        return toDTO(product);
    }    

    public ProductDTO save(ProductDTO dto) {
        if (dto.getId() != null) {
            throw new IllegalArgumentException("Novo produto não deve ter ID definido");
        }
        Product product = toEntity(dto);
        Product saved = productRepository.save(product);
        return toDTO(saved);
    }

    public ProductDTO update(Long id, ProductDTO dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice().doubleValue() >= 0 ? dto.getPrice() : existing.getPrice());
        existing.setQuantity(dto.getQuantity());

        Category category = categoryRepository.findById(dto.getCategory().getId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        existing.setCategory(category);
        existing.setMinimumStock(dto.getMinimumStock());
        Product updated = productRepository.save(existing);
        return toDTO(updated);
    }
    
    public void delete(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        productRepository.delete(existing);
    }    
}
