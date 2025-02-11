package com.demoproject.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.demoproject.entity.Zone;
import com.demoproject.repository.ZoneRepository;

@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;

    public Zone handleSaveZone(Zone zone) {
        Zone newZone = this.zoneRepository.save(zone);
        return newZone;
    }

    public void save(Zone zone) {
        this.zoneRepository.save(zone);
    }

    public ZoneService(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    public List<Zone> getAllZones() {
        return this.zoneRepository.findAll();
    }

    public Zone getZoneById(Long id) {
        Optional<Zone> zone = this.zoneRepository.findById(id);
        return zone.orElse(null);
    }

    public List<Zone> getZonesByWarehouseId(int warehouseId) {
        return this.zoneRepository.findByWarehouseId(warehouseId);
    }

    public void deleteById(long id) {
        this.zoneRepository.deleteById(id);
    }

    public Page<Zone> getAllZones(Pageable pageable) {
        return this.zoneRepository.findAll(pageable);
    }

    public Page<Zone> getZonesByName(String name, Pageable pageable) {
        return this.zoneRepository.findByName(name, pageable);
    }
}
