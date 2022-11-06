package ru.practicum.main_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.dto.CategoryRequestDto;
import ru.practicum.main_server.dto.CategoryResponseDto;
import ru.practicum.main_server.exception.ObjectNotFoundException;
import ru.practicum.main_server.mapper.CategoryMapper;
import ru.practicum.main_server.model.Category;
import ru.practicum.main_server.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDto> getCategories(int from, int size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryResponseDto getCategoryById(long id) {
        return CategoryMapper.toCategoryDto(categoryRepository.findById(id).orElseThrow(() ->
                new ObjectNotFoundException("Category not found")));
    }

    @Transactional
    public CategoryResponseDto updateCategory(CategoryResponseDto categoryResponseDto) {
        Category category = categoryRepository.findById(categoryResponseDto.getId())
                .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
        category.setName(categoryResponseDto.getName());
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
    public CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto) {
        Category category = CategoryMapper.toCategoryFromNewCategoryDto(categoryRequestDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }
}
