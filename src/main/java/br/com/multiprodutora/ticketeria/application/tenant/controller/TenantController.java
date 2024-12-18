package br.com.multiprodutora.ticketeria.application.tenant.controller;

import br.com.multiprodutora.ticketeria.domain.model.tenant.Tenant;
import br.com.multiprodutora.ticketeria.application.tenant.dto.RegisterTenantDTO;
import br.com.multiprodutora.ticketeria.application.tenant.dto.TenantDTO;
import br.com.multiprodutora.ticketeria.application.tenant.dto.TenantsDTO;
import br.com.multiprodutora.ticketeria.application.event.repository.EventRepository;
import br.com.multiprodutora.ticketeria.application.lot.repository.LotRepository;
import br.com.multiprodutora.ticketeria.application.tenant.repository.TenantRepository;
import br.com.multiprodutora.ticketeria.application.ticket.repository.TicketRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TenantController {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private LotRepository lotRepository;

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        } else {
            clientIp = clientIp.split(",")[0];
        }
        return clientIp;
    }

    @PostMapping("/tenants/register")
    @Transactional
    public ResponseEntity<RegisterTenantDTO> registerTenant(@RequestBody @Valid RegisterTenantDTO data, UriComponentsBuilder uriBuilder, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        logger.info("Received request to register tenant with email: {} from IP: {}", data.email(), clientIp);
        var tenant = new Tenant(data);
        tenantRepository.save(tenant);
        logger.info("Tenant registered successfully with id: {}", tenant.getId());

        return ResponseEntity.created(uriBuilder.path("/tenants/{id}").buildAndExpand(tenant.getId()).toUri()).build();
    }

    @PostMapping("/tenant/login")
    public ResponseEntity<?> loginTenant(@RequestBody Tenant tenant, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        logger.info("Received login request for tenant with email: {} from IP: {}", tenant.getEmail(), clientIp);
        Tenant authenticatedTenant = tenantRepository.findByEmailAndPassword(tenant.getEmail(), tenant.getPassword());
        if (authenticatedTenant == null) {
            logger.warn("Login failed for tenant with email: {} from IP: {}", tenant.getEmail(), clientIp);
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", authenticatedTenant.getId());
        response.put("name", authenticatedTenant.getName());
        logger.info("Login successful for tenant with email: {} from IP: {}", tenant.getEmail(), clientIp);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tenants")
    public ResponseEntity<List<TenantsDTO>> listTenants(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        logger.info("Received request to list all tenants from IP: {}", clientIp);
        List<Tenant> tenants = tenantRepository.findAll();

        List<TenantsDTO> tenantsDTOs = tenants.stream().map(tenant -> new TenantsDTO(tenant.getId(), tenant.getName(), tenant.getSubdomain(), tenant.getCreatedAt(), tenant.getIsTenantActive())).collect(Collectors.toList());

        logger.info("Listed {} tenants from IP: {}", tenantsDTOs.size(), clientIp);
        return ResponseEntity.ok(tenantsDTOs);
    }

    @GetMapping("/tenants/{id}")
    public ResponseEntity<TenantDTO> getTenant(@PathVariable Long id, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        logger.info("Received request to get tenant with id: {} from IP: {}", id, clientIp);
        Optional<Tenant> tenantOptional = tenantRepository.findById(id);

        if (tenantOptional.isPresent()) {
            Tenant tenant = tenantOptional.get();
            TenantDTO tenantDTO = new TenantDTO(tenant.getId(), tenant.getName(), tenant.getSubdomain(), tenant.getEmail(), tenant.getPassword(), tenant.getCreatedAt(), tenant.getIsTenantActive(), tenant.getEvents().size());
            logger.info("Tenant found with id: {} from IP: {}", id, clientIp);
            return ResponseEntity.ok(tenantDTO);
        } else {
            logger.warn("Tenant not found with id: {} from IP: {}", id, clientIp);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/tenants/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable Long id, @RequestBody Tenant tenant, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        logger.info("Received request to update tenant with id: {} from IP: {}", id, clientIp);
        Tenant tenantToUpdate = tenantRepository.findById(id).orElseThrow(() -> {
            logger.error("Tenant not found with id: {} from IP: {}", id, clientIp);
            return new RuntimeException("Tenant not found");
        });

        tenantToUpdate.setName(tenant.getName());
        tenantToUpdate.setSubdomain(tenant.getSubdomain());
        tenantToUpdate.setEmail(tenant.getEmail());
        tenantToUpdate.setPassword(tenant.getPassword());
        tenantToUpdate.setIsTenantActive(tenant.getIsTenantActive());
        tenantRepository.save(tenantToUpdate);
        logger.info("Tenant updated successfully with id: {} from IP: {}", id, clientIp);

        return ResponseEntity.ok(tenantToUpdate);
    }

    @DeleteMapping("/tenants/{id}")
    @Transactional
    public ResponseEntity<?> deleteTenant(@PathVariable Long id, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        logger.info("Received request to delete tenant with id: {} from IP: {}", id, clientIp);
        tenantRepository.findById(id).orElseThrow(() -> {
            logger.error("Tenant not found with id: {} from IP: {}", id, clientIp);
            return new RuntimeException("Tenant not found");
        });

        tenantRepository.deleteById(id);
        logger.info("Tenant deleted successfully with id: {} from IP: {}", id, clientIp);
        return ResponseEntity.ok().build();
    }

}
