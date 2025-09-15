package br.com.knowledge.controlebeb.service.customer;

import br.com.knowledge.controlebeb.dto.customer.CustomerDTO;
import br.com.knowledge.controlebeb.model.customer.Customer;
import br.com.knowledge.controlebeb.repository.customer.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    private CustomerDTO toDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        return dto;
    }

    private Customer toEntity(CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        return customer;
    }

    // =====================
    // CRUD
    // =====================
    public List<CustomerDTO> findAll() {
        return customerRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CustomerDTO findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        return toDTO(customer);
    }

    public CustomerDTO save(CustomerDTO dto) {
        if (dto.getId() != null) {
            throw new IllegalArgumentException("Novo cliente não deve ter ID definido");
        }
        if (dto.getName() != null && customerRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Nome já cadastrado");
        }
        if (dto.getEmail() != null && customerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        Customer saved = customerRepository.save(toEntity(dto));
        return toDTO(saved);
    }

    public CustomerDTO update(Long id, CustomerDTO dto) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        if (!existing.getName().equals(dto.getName()) && customerRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Nome já cadastrado");
        }
        if (!existing.getEmail().equals(dto.getEmail()) && customerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());

        Customer updated = customerRepository.save(existing);
        return toDTO(updated);
    }

    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Cliente não encontrado");
        }
        customerRepository.deleteById(id);
    }
}
