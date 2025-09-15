package br.com.knowledge.controlebeb.service.product;

import br.com.knowledge.controlebeb.dto.product.CategoryDTO;
import br.com.knowledge.controlebeb.exception.CategoryDeletionException;
import br.com.knowledge.controlebeb.model.product.Category;
import br.com.knowledge.controlebeb.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setDescription(category.getDescription());
        return dto;
    }

    private Category toEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setDescription(dto.getDescription());
        return category;
    }

    // ============================
    // CRUD
    // ============================
    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        CategoryDTO dto = toDTO(category);
        System.out.println("dto = " + dto);
        return toDTO(category);
    }

    public CategoryDTO save(CategoryDTO dto) {
        if (categoryRepository.existsByDescription(dto.getDescription())) {
            throw new IllegalArgumentException("Categoria já existe");
        }
        Category category = toEntity(dto);
        Category saved = categoryRepository.save(category);
        return toDTO(saved);
    }

    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        existing.setDescription(dto.getDescription());
        Category updated = categoryRepository.save(existing);

        // Retornar DTO corretamente
        CategoryDTO responseDto = new CategoryDTO();
        responseDto.setId(updated.getId());
        responseDto.setDescription(updated.getDescription());
        return responseDto;
    }

    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        try {
            categoryRepository.delete(category);
        } catch (DataIntegrityViolationException ex) {
            // Lança exceção amigável para o controller
            throw new CategoryDeletionException(
                    "Não é possível excluir a categoria, pois ela está vinculada a um produto.");
        }
    }
}
