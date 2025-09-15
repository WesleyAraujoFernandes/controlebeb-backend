package br.com.knowledge.controlebeb.model.sale;

import br.com.knowledge.controlebeb.model.customer.Customer;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales")
@Data
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDateTime saleDate = LocalDateTime.now();

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items;

    @Enumerated(EnumType.STRING)
    private SaleStatus status = SaleStatus.ABERTA;

    //private BigDecimal total = BigDecimal.ZERO;

    private BigDecimal totalBruto = BigDecimal.ZERO;

    private BigDecimal desconto = BigDecimal.ZERO;

    private BigDecimal totalLiquido = BigDecimal.ZERO;

}
