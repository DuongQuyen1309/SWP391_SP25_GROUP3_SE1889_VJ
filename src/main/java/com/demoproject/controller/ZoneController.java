package com.demoproject.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import org.springframework.data.domain.Pageable;

import com.demoproject.entity.Zone;
import com.demoproject.service.ZoneService;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ZoneController {
    private ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @GetMapping("/zone")
    public String getAllZone(Model model, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size, @RequestParam(defaultValue = "") String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Zone> zonePage;
        if (search.isEmpty()) {
            zonePage = this.zoneService.getAllZones(pageable);
        } else {
            zonePage = this.zoneService.getZonesByName(search, pageable);
        }
        model.addAttribute("zonePage", zonePage);
        model.addAttribute("search", search);
        return "zone";
    }

    @GetMapping("/create")
    public String getCreateZonePage(Model model) {
        model.addAttribute("newZone", new Zone());
        return "create";
    }

    @PostMapping("/create")
    public String postMethodName(Model model, @ModelAttribute("newZone") Zone newZone) {
        // TODO: process POST request
        this.zoneService.handleSaveZone(newZone);

        return "redirect:/zone";
    }

    @GetMapping("/zone/{id}")
    public String getZoneDetail(Model model, @PathVariable long id) {
        Zone zone = this.zoneService.getZoneById(id);
        model.addAttribute("zone", zone);
        model.addAttribute("id", id);
        return "zoneDetail";

    }

    @GetMapping("/zone/update/{id}")
    public String getUpdateZonePage(Model model, @PathVariable long id) {
        Zone currentZone = this.zoneService.getZoneById(id);
        model.addAttribute("currentZone", currentZone);
        return "updateZone";
    }

    @PostMapping("/zone/update")
    public String postUpdateZone(Model model, @ModelAttribute("currentZone") Zone zone) {
        // TODO: process POST request
        Zone currentZone = this.zoneService.getZoneById(zone.getId());
        if (currentZone != null) {
            currentZone.setName(zone.getName());
            currentZone.setWarehouseId(zone.getWarehouseId());
            currentZone.setProductId(zone.getProductId());
            currentZone.setAmount(zone.getAmount());
            this.zoneService.handleSaveZone(currentZone);
        }

        return "redirect:/zone";
    }

    @GetMapping("/zone/delete/{id}")
    public String getDeleteZonePage(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        model.addAttribute("newZone", new Zone());
        return "delete";

    }

    @PostMapping("/zone/delete")
    public String postDeleteZone(@RequestParam("id") long id) {
        this.zoneService.deleteById(id);

        return "redirect:/zone";
    }

}