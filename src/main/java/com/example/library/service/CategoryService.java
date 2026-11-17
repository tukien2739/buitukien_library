
package com.example.library.service;

import com.example.library.entity.Category;
import com.example.library.exception.BusinessException;
import com.example.library.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy thể loại"
                        ));
    }

    @Transactional
    public Category save(Category category) {

        String name = category.getName() == null
                ? ""
                : category.getName().trim();

        if (name.isBlank()) {
            throw new BusinessException(
                    "Tên thể loại không được để trống"
            );
        }

        boolean duplicate;

        if (category.getId() == null) {
            duplicate =
                    categoryRepository.existsByNameIgnoreCase(name);
        } else {
            duplicate =
                    categoryRepository.existsByNameIgnoreCaseAndIdNot(
                            name, category.getId()
                    );
        }

        if (duplicate) {
            throw new BusinessException(
                    "Tên thể loại đã tồn tại"
            );
        }

        category.setName(name);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteById(Long id) {

        Category category = findById(id);

        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new BusinessException(
                    "Không thể xóa thể loại đang có sách"
            );
        }
    }
}
