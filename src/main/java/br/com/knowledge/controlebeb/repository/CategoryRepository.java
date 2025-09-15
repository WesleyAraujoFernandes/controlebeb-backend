package br.com.knowledge.controlebeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.controlebeb.model.product.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByDescription(String description);
    Category findByDescription(String description);
    Category findById(long id);
}
