package com.demoproject.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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



    public Page<Package> getAllPackageByStoreIdAndDateRange(LocalDate startDate, LocalDate endDate,
                                                            Long storeId, Pageable pageable) {

        return packageRepository.findByStoreIdAndCreatedAtBetween(storeId, startDate, endDate, pageable);
    }
    public Page<Package> getPackagesWithFilters(Long idFrom, Long idTo, String packageName, String color,
                                                String description, LocalDate startDate, LocalDate endDate, Long storeId, Pageable pageable) {

        Specification<Package> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (idFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("id"), idFrom));
            if (idTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("id"), idTo));
            if (packageName != null && !packageName.isEmpty())
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + packageName.toLowerCase() + "%"));
            if (color != null && !color.isEmpty())
                predicates.add(cb.like(cb.lower(root.get("color")), "%" + color.toLowerCase() + "%"));
            if (description != null && !description.isEmpty())
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
            if (startDate != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            if (endDate != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            predicates.add(cb.equal(root.get("storeId"), storeId));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return packageRepository.findAll(spec, pageable);
    }

    public String getName(Long id){
        Package pc= new Package();
        pc= packageRepository.findById(id).get();
        return pc.getName();
    }
}