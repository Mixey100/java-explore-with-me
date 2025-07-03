package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(NewCategoryDto categoryDto);

    CategoryDto update(NewCategoryDto categoryDto, Long categoryId);

    void delete(Long categoryId);

    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getById(Long categoryId);
}
