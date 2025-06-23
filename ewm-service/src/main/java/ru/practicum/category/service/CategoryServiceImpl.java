package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto categoryDto) {
        log.info("Запрос на создание категории name {}", categoryDto.getName());
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Такая категория уже существует");
        }
        Category category = categoryRepository.save(CategoryMapper.mapToCategory(categoryDto));
        return CategoryMapper.mapToCategoryDto(category);
    }

    @Override
    public CategoryDto updateCategory(NewCategoryDto categoryDto, Long categoryId) {
        log.info("Запрос на изменение категории с id = {}", categoryId);
        Category existingCategory = categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Категория с id = " + categoryId + " не найдена"));
        if (!existingCategory.getName().equals(categoryDto.getName())) {
            categoryRepository.findByName(categoryDto.getName()).ifPresent(c -> {
                throw new ConflictException("Такая категория уже существует");
            });
        }
        existingCategory.setName(categoryDto.getName());
        return CategoryMapper.mapToCategoryDto(categoryRepository.save(existingCategory));
    }

    @Override
    public void deleteCategory(Long categoryId) {
        log.info("Запрос на удаление категории с id = {}", categoryId);
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Категория с id = " + categoryId + " не найдена");
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();
        return categories.stream()
                .map(CategoryMapper::mapToCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Категория с id = " + categoryId + " не найдена"));
        return CategoryMapper.mapToCategoryDto(category);
    }
}
