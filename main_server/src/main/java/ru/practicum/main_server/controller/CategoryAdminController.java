package ru.practicum.main_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.dto.CategoryRequestDto;
import ru.practicum.main_server.dto.CategoryResponseDto;
import ru.practicum.main_server.service.CategoryService;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/categories")
public class CategoryAdminController {
    private final CategoryService categoryService;

    public CategoryAdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PatchMapping
    public CategoryResponseDto updateCategory(@RequestBody @Valid CategoryResponseDto categoryResponseDto) {
        log.info("Patch category() " + categoryResponseDto);
        return categoryService.updateCategory(categoryResponseDto);
    }

    @PostMapping
    public CategoryResponseDto createCategory(@RequestBody @Valid CategoryRequestDto categoryRequestDto) {
        log.info("Post category() " + categoryRequestDto);
        return categoryService.createCategory(categoryRequestDto);
    }

    @DeleteMapping("/{catId}")
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Delete categoryById() " + catId);
        categoryService.deleteCategory(catId);
    }
}
