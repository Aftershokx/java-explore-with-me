package ru.practicum.main_server.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.main_server.dto.CategoryRequestDto;
import ru.practicum.main_server.dto.CategoryResponseDto;
import ru.practicum.main_server.model.Category;

@UtilityClass
public class CategoryMapper {
    public static Category toCategoryFromNewCategoryDto(CategoryRequestDto categoryRequestDto) {
        return Category.builder()
                .name(categoryRequestDto.getName())
                .build();
    }

    public static CategoryResponseDto toCategoryDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
