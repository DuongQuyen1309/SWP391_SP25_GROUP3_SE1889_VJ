package com.demoproject.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.demoproject.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demoproject.entity.Zone;
import com.demoproject.repository.ProductRepository;
import com.demoproject.repository.ZoneRepository;

@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    @Transactional
    public Zone handleSaveZone(Zone zone) {
        Optional<Zone> existingZone = zoneRepository.findByName(zone.getName());
        if (existingZone.isPresent() && !existingZone.get().getId().equals(zone.getId())) {
            throw new IllegalArgumentException("Zone with the same name already exists");
        }
        System.out.println("zoneid: " + zone.getId());
        return this.zoneRepository.save(zone);

    }

    // public void save(Zone zone) {
    // this.zoneRepository.save(zone);
    // }




    public ZoneService(ZoneRepository zoneRepository, ProductRepository productRepository, UserService userService) {
        this.zoneRepository = zoneRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    public Zone getZoneById(Long id) {
        Optional<Zone> zone = this.zoneRepository.findById(id);
        return zone.orElse(null);
    }

    // public List<Zone> getZonesByWarehouseId(int warehouseId) {
    // return this.zoneRepository.findByWarehouseId(warehouseId);
    // }

    @Transactional
    public void deleteById(long id) {
        this.zoneRepository.deleteById(id);
    }

    public Page<Zone> getAllZones(Pageable pageable) {
        return this.zoneRepository.findAll(pageable);
    }
    public Page<Zone> getAllZonesByStoreID(Pageable pageable,Long storeID) {
        // Fetch current user's warehouse name


        // Fetch zones associated with the warehouse name
        return this.zoneRepository.findAllByStoreId(storeID, pageable);
    }

    public Page<Zone> getZonesByName(String name,Long storeID, Pageable pageable) {
        // Fetch the warehouse name of the current logged-in user


        // Fetch zones filtered by name and warehouse name
        return this.zoneRepository.findByNameAndStoreId(name, storeID, pageable);
    }


    public boolean isZoneNameAlreadyExists(String name, Long id) {
        // Check for duplicate name, excluding the current zone being updated
        return zoneRepository.existsByNameAndIdNot(name, id);
    }

    public void updateZone(Zone zone) {
        if (isZoneNameAlreadyExists(zone.getName(), zone.getId())) {
            throw new RuntimeException("Zone with this name already exists!");
        }

        zoneRepository.save(zone);
    }

    public boolean existsById(Long productId) {
        return productRepository.existsById(productId);
    }

    public Page<Zone> getZonesByPosition(String position, Long storeID, Pageable pageable) {
        return this.zoneRepository.findByPositionAndStoreId(position, storeID, pageable);
    }

    public List<Zone> getZonesByStoreId(Long storeId) {
        return zoneRepository.findByStoreId(storeId);
    }
    public Page<Zone> getZonesWithFilters(Long idFrom, Long idTo, String name, String position,
                                          LocalDate startDate, LocalDate endDate, Long storeId, Pageable pageable) {

        Specification<Zone> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (idFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("id"), idFrom));
            if (idTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("id"), idTo));
            if (name != null && !name.isEmpty())
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            if (position != null && !position.isEmpty())
                predicates.add(cb.like(cb.lower(root.get("position")), "%" + position.toLowerCase() + "%"));
            if (startDate != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            if (endDate != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            predicates.add(cb.equal(root.get("storeId"), storeId));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return zoneRepository.findAll(spec, pageable);
    }

    @Transactional
    public Product getProductByZoneId(Long zoneId) {
        return zoneRepository.findProductByZoneId(zoneId);
    }
}
