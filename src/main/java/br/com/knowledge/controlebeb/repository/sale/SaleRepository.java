package br.com.knowledge.controlebeb.repository.sale;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.controlebeb.model.customer.Customer;
import br.com.knowledge.controlebeb.model.sale.Sale;
import br.com.knowledge.controlebeb.model.sale.SaleStatus;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByStatus(SaleStatus status);
    List<Sale> findByCustomerId(Long customerId);
    boolean existsByCustomerAndStatus(Customer customer, SaleStatus status);

}
