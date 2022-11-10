package ru.practicum.main_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.dto.CategoryResponseDto;
import ru.practicum.main_server.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponseDto> getCategories(@RequestParam(defaultValue = "0") int from,
                                                   @RequestParam(defaultValue = "10") int size) {
        log.info("Get categories() from " + from + " ,size " + size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{id}")
    public CategoryResponseDto getCategoryById(@PathVariable long id) {
        log.info("Get categoryById() " + id);
        return categoryService.getCategoryById(id);
    }
}
