package com.demoproject.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demoproject.entity.Bill;
import com.demoproject.entity.Package; // Correct import
import com.demoproject.entity.Zone;
import com.demoproject.repository.PackageRepository;

@Service
public class PackageService {
    private final PackageRepository packageRepository;

    @Transactional
    public Package handldeSavePackage(Package package1) {
        Optional<Package> existingPackage = packageRepository.findByName(package1.getName());
        if (existingPackage.isPresent() && !existingPackage.get().getId().equals(package1.getId())) {
            throw new IllegalArgumentException("Package with the same name already exists");
        }

        return this.packageRepository.save(package1);

    }

    @Autowired
    public PackageService(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    public List<Package> getAllPackages() {
        return this.packageRepository.findAll();
    }

    public boolean isPackageNameAlreadyExists(String name, Long id) {
        // Check for duplicate name, excluding the current zone being updated
        return packageRepository.existsByNameAndIdNot(name, id);
    }

    public void deletePackageById(Long id) {
        packageRepository.deleteById(id);
    }

    public Page<Package> getAllPackageByStoreId(Pageable pageable, Long storeID) {

        return this.packageRepository.findAllByStoreId(storeID, pageable);
    }

    public Package getPackageById(Long id) {
        Optional<Package> packageOptional = this.packageRepository.findById(id);
        return packageOptional.orElse(null);
    }

    public void updatePackage(Package package1) {
        if (isPackageNameAlreadyExists(package1.getName(), package1.getId())) {
            throw new RuntimeException("Package with this name already exists!");
        }

        packageRepository.save(package1);
    }

    public Page<Package> getAllPackageByStoreIdAndName(String name, Long storeId, Pageable pageable) {
        return this.packageRepository.findByNameAndStoreId(name, storeId, pageable);
    }

    public Page<Package> getAllPackageByStoreIdAndDescription(String name, Long storeId, Pageable pageable) {
        return this.packageRepository.findByNameAndStoreId(name, storeId, pageable);
    }

    public Page<Package> getAllPackageByStoreIdAndColor(String name, Long storeId, Pageable pageable) {
        return this.packageRepository.findByColorAndStoreId(name, storeId, pageable);
    }

    public Page<Package> getAllPackageByStoreIdAndDateRange(LocalDate startDate, LocalDate endDate,
                                                            Long storeId, Pageable pageable) {

        return packageRepository.findByStoreIdAndCreatedAtBetween(storeId, startDate, endDate, pageable);
    }
}