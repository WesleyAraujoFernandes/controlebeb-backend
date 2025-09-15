package br.com.knowledge.controlebeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.controlebeb.model.product.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
    Product findByName(String name);
    Product findById(long id);
    Product findByCategoryId(long categoryId);
}
