package br.com.knowledge.controlebeb.service.sale;

import br.com.knowledge.controlebeb.dto.sale.SaleDTO;
import br.com.knowledge.controlebeb.dto.sale.SaleItemDTO;
import br.com.knowledge.controlebeb.model.customer.Customer;
import br.com.knowledge.controlebeb.model.product.Product;
import br.com.knowledge.controlebeb.model.sale.Sale;
import br.com.knowledge.controlebeb.model.sale.SaleItem;
import br.com.knowledge.controlebeb.model.sale.SaleStatus;
import br.com.knowledge.controlebeb.repository.ProductRepository;
import br.com.knowledge.controlebeb.repository.customer.CustomerRepository;
import br.com.knowledge.controlebeb.repository.sale.SaleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SaleService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public SaleService(SaleRepository saleRepository, CustomerRepository customerRepository,
            ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    // =========================
    // Criar venda
    // =========================
    public SaleDTO createSale(SaleDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        // 🚨 Verifica se já existe venda aberta para o cliente
        boolean hasOpenSale = saleRepository.existsByCustomerAndStatus(customer, SaleStatus.ABERTA);
        if (hasOpenSale) {
            throw new IllegalStateException("Já existe uma venda em aberto para este cliente.");
        }

        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setStatus(SaleStatus.ABERTA);

        List<SaleItem> items = new ArrayList<>();

        for (SaleItemDTO itemDTO : dto.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

            if (product.getQuantity() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException("Estoque insuficiente para o produto: " + product.getName());
            }

            // decrementa estoque
            product.setQuantity(product.getQuantity() - itemDTO.getQuantity());
            productRepository.save(product);

            // verifica se produto já existe na lista
            SaleItem existingItem = items.stream()
                    .filter(i -> i.getProduct().getId().equals(product.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + itemDTO.getQuantity());
            } else {
                SaleItem newItem = new SaleItem();
                newItem.setSale(sale);
                newItem.setProduct(product);
                newItem.setQuantity(itemDTO.getQuantity());
                newItem.setPrice(product.getPrice());
                newItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
                items.add(newItem);
            }
        }

        sale.setItems(items);

        // =========================
        // Cálculo de totais
        // =========================
        BigDecimal totalBruto = items.stream()
                .map(i -> i.getProduct().getPrice().multiply(new BigDecimal(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal desconto = dto.getDesconto() != null ? dto.getDesconto() : BigDecimal.ZERO;

        BigDecimal totalLiquido = totalBruto.subtract(desconto);

        sale.setTotalBruto(totalBruto);
        sale.setDesconto(desconto);
        sale.setTotalLiquido(totalLiquido);

        Sale saved = saleRepository.save(sale);

        return toDTO(saved);
    }

    // =========================
    // Alterar status da venda
    // =========================
    public SaleDTO updateStatus(Long saleId, SaleStatus newStatus) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new EntityNotFoundException("Venda não encontrada"));

        if (sale.getStatus() == SaleStatus.CANCELADA || sale.getStatus() == SaleStatus.FECHADA) {
            throw new IllegalStateException("Não é possível alterar o status de uma venda já " + sale.getStatus());
        }

        if (newStatus == SaleStatus.FECHADA) {
            // ✅ Bloqueia alterações futuras
            sale.setStatus(SaleStatus.FECHADA);

        } else if (newStatus == SaleStatus.CANCELADA) {
            // ✅ Devolve o estoque dos produtos
            for (SaleItem item : sale.getItems()) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepository.save(product);
            }
            sale.setStatus(SaleStatus.CANCELADA);

        } else if (newStatus == SaleStatus.SUSPENSA) {
            // ✅ Apenas marca como suspensa (estoque já foi debitado)
            sale.setStatus(SaleStatus.SUSPENSA);

        } else if (newStatus == SaleStatus.ABERTA) {
            // Só pode voltar para aberta se estava suspensa
            if (sale.getStatus() != SaleStatus.SUSPENSA) {
                throw new IllegalStateException("Só é possível reabrir vendas que estavam suspensas");
            }
            sale.setStatus(SaleStatus.ABERTA);
        }

        Sale updated = saleRepository.save(sale);
        return toDTO(updated);
    }

    public List<SaleDTO> findAll() {
        return saleRepository.findAll().stream().map(sale -> {
            SaleDTO dto = new SaleDTO();
            dto.setId(sale.getId());
            dto.setCustomerId(sale.getCustomer().getId());
            dto.setSaleDate(sale.getSaleDate());
            dto.setStatus(sale.getStatus());
            dto.setItems(sale.getItems().stream().map(i -> {
                var itemDTO = new SaleItemDTO();
                itemDTO.setProductId(i.getProduct().getId());
                itemDTO.setQuantity(i.getQuantity());
                return itemDTO;
            }).collect(Collectors.toList()));
            dto.setTotalBruto(sale.getItems().stream()
                    .map(i -> i.getProduct().getPrice().multiply(new BigDecimal(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            return dto;
        }).collect(Collectors.toList());
    }

    public List<SaleDTO> getSalesByStatus(SaleStatus status) {
        return saleRepository.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<SaleDTO> getSalesByCustomer(Long customerId) {
        return saleRepository.findByCustomerId(customerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private SaleDTO toDTO(Sale sale) {
        SaleDTO dto = new SaleDTO();
        dto.setId(sale.getId());
        dto.setCustomerId(sale.getCustomer().getId());
        dto.setSaleDate(sale.getSaleDate());
        dto.setStatus(sale.getStatus());
        dto.setTotalBruto(sale.getTotalBruto());
        dto.setDesconto(sale.getDesconto());
        dto.setTotalLiquido(sale.getTotalLiquido());
        dto.setItems(sale.getItems().stream().map(i -> {
            SaleItemDTO itemDTO = new SaleItemDTO();
            itemDTO.setProductId(i.getProduct().getId());
            itemDTO.setQuantity(i.getQuantity());
            return itemDTO;
        }).toList());
        return dto;
    }

    /*
     * 
     * @param saleId
     * 
     * @param productId
     * 
     * @param quantity
     * 
     * @return
     */
    // IMPLEMENTAR ADICIONAR ITEM NA VENDA ABERTA (VERIFICAR SE JA EXISTE O ITEM, SE
    // SIM, SOMA A QUANTIDADE)
    @Transactional
    public Sale addItem(Long saleId, Long productId, int quantity) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new EntityNotFoundException("Venda não encontrada"));

        if (sale.getStatus() != SaleStatus.ABERTA) {
            throw new IllegalStateException("Só é possível adicionar itens em vendas ABERTAS");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Estoque insuficiente para o produto: " + product.getName());
        }
        //product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        // Procura se já existe o item desse produto na venda
        Optional<SaleItem> existingItem = sale.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Soma na quantidade já existente
            SaleItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        } else {
            // Cria novo item
            SaleItem newItem = new SaleItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice());
            newItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            newItem.setSale(sale);

            sale.getItems().add(newItem);
        }
        return saleRepository.save(sale);
    }

}