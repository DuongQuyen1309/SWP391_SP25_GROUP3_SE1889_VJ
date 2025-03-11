package com.demoproject.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
}
