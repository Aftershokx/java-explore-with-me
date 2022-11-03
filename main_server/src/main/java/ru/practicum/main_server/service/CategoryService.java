package ru.practicum.main_server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.dto.CategoryDto;
import ru.practicum.main_server.dto.NewCategoryDto;
import ru.practicum.main_server.exception.ObjectNotFoundException;
import ru.practicum.main_server.mapper.CategoryMapper;
import ru.practicum.main_server.model.Category;
import ru.practicum.main_server.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getCategories(int from, int size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(long id) {
        return CategoryMapper.toCategoryDto(categoryRepository.findById(id).orElseThrow(() ->
                new ObjectNotFoundException("Category not found")));
    }

    @Transactional
    public CategoryDto updateCategory(CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryDto.getId())
                .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
        category.setName(categoryDto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long catId) {
        if (categoryRepository.findById(catId).isPresent()) {
            categoryRepository.deleteById(catId);
        } else {
            throw new ObjectNotFoundException("Category not found");
        }
    }

    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.toCategoryFromNewCategoryDto(newCategoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }
}
