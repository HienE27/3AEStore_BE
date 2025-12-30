package com.nguyenviethien.exercise201.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenviethien.exercise201.entity.Gallery;
import com.nguyenviethien.exercise201.entity.Product;
import com.nguyenviethien.exercise201.entity.VariantOption;
import com.nguyenviethien.exercise201.repository.VariantOptionRepository;
import com.nguyenviethien.exercise201.service.VariantOptionService;

@Service
@Transactional
public class VariantOptionServiceImpl implements VariantOptionService {

    @Autowired
    private VariantOptionRepository variantOptionRepository;

    @Override
    public List<VariantOption> findAll() {
        return variantOptionRepository.findAll();
    }

    @Override
    public Optional<VariantOption> findById(UUID id) {
        return variantOptionRepository.findById(id);
    }

    @Override
    public List<VariantOption> findByProduct(Product product) {
        return variantOptionRepository.findByProduct(product);
    }

    @Override
    public List<VariantOption> findByProductAndActiveTrue(Product product) {
        return variantOptionRepository.findByProductAndActiveTrue(product);
    }

    @Override
    public VariantOption save(VariantOption variantOption) {
        return variantOptionRepository.save(variantOption);
    }

    @Override
    public void deleteById(UUID id) {
        variantOptionRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return variantOptionRepository.existsById(id);
    }

    @Override
    @Transactional
    public void updateVariantImage(VariantOption variantOption, Gallery image) {
        variantOption.setImage(image);
        save(variantOption);
    }

    @Override
    @Transactional
    public void toggleActive(UUID id) {
        findById(id).ifPresent(variantOption -> {
            variantOption.setActive(!variantOption.getActive());
            save(variantOption);
        });
    }
}