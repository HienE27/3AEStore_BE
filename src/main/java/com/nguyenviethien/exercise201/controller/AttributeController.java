package com.nguyenviethien.exercise201.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nguyenviethien.exercise201.entity.Attribute;
import com.nguyenviethien.exercise201.entity.AttributeValue;
import com.nguyenviethien.exercise201.service.AttributeService;

@RestController
@RequestMapping("/api/attributes")
public class AttributeController {

    @Autowired
    private AttributeService attributeService;

    @GetMapping
    public ResponseEntity<List<Attribute>> getAllAttributes() {
        return ResponseEntity.ok(attributeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Attribute> getAttributeById(@PathVariable UUID id) {
        return attributeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createAttribute(@RequestBody Attribute attribute) {
        if (attributeService.existsByAttributeName(attribute.getAttributeName())) {
            return ResponseEntity.badRequest()
                    .body("Attribute name already exists");
        }
        return ResponseEntity.ok(attributeService.save(attribute));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAttribute(
            @PathVariable UUID id,
            @RequestBody Attribute attribute) {
        if (!attributeService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (!attribute.getAttributeName().equals(attributeService.findById(id).get().getAttributeName()) &&
                attributeService.existsByAttributeName(attribute.getAttributeName())) {
            return ResponseEntity.badRequest()
                    .body("Attribute name already exists");
        }
        attribute.setId(id);
        return ResponseEntity.ok(attributeService.save(attribute));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttribute(@PathVariable UUID id) {
        if (!attributeService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        attributeService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/values")
    public ResponseEntity<AttributeValue> addAttributeValue(
            @PathVariable UUID id,
            @RequestParam String value,
            @RequestParam(required = false) String color) {
        return attributeService.findById(id)
                .map(attribute -> ResponseEntity.ok(
                        attributeService.addAttributeValue(attribute, value, color)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/values/{valueId}")
    public ResponseEntity<Void> removeAttributeValue(
            @PathVariable UUID valueId,
            @RequestBody AttributeValue attributeValue) {
        if (!attributeService.findById(valueId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        attributeService.removeAttributeValue(attributeValue);
        return ResponseEntity.ok().build();
    }
}