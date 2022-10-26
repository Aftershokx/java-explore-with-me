package ru.practicum.main_server.controller;

import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.dto.CategoryDto;
import ru.practicum.main_server.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping(path = "/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size) {
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{id}")
    CategoryDto getCategoryById(@PathVariable long id) {
        return categoryService.getCategoryById(id);
    }
}
