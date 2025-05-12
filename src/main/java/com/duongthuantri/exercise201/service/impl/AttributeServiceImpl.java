package com.duongthuantri.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duongthuantri.exercise201.entity.Attribute;
import com.duongthuantri.exercise201.entity.AttributeValue;
import com.duongthuantri.exercise201.exception.ResourceNotFoundException;
import com.duongthuantri.exercise201.repository.AttributeRepository;
import com.duongthuantri.exercise201.repository.AttributeValueRepository;
import com.duongthuantri.exercise201.service.AttributeService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
public class AttributeServiceImpl implements AttributeService {

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private AttributeValueRepository attributeValueRepository;

    @Override
    public List<Attribute> findAll() {
        return attributeRepository.findAll();
    }

    @Override
    public Optional<Attribute> findById(UUID id) {
        return attributeRepository.findById(id);
    }

    @Override
    public Attribute save(Attribute attribute) {
        return attributeRepository.save(attribute);
    }

    @Override
    public void deleteById(UUID id) {
        attributeRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return attributeRepository.existsById(id);
    }

    @Override
    public boolean existsByAttributeName(String attributeName) {
        return attributeRepository.existsByAttributeName(attributeName);
    }

    @Override
    @Transactional
    public AttributeValue addAttributeValue(Attribute attribute, String value, String color) {
        if (attributeValueRepository.existsByAttributeAndAttributeValue(attribute, value)) {
            throw new IllegalArgumentException("Value already exists for this attribute");
        }

        AttributeValue attributeValue = new AttributeValue();
        attributeValue.setAttribute(attribute);
        attributeValue.setAttributeValue(value);
        attributeValue.setColor(color);

        return attributeValueRepository.save(attributeValue);
    }

    @Override
    @Transactional
    public void removeAttributeValue(AttributeValue attributeValue) {
        attributeValueRepository.delete(attributeValue);
    }
}