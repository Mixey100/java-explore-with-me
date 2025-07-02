package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto create(NewCategoryDto categoryDto) {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Категория %s уже существует".formatted(categoryDto.getName()));
        }
        Category category = categoryRepository.save(CategoryMapper.mapToCategory(categoryDto));
        return CategoryMapper.mapToCategoryDto(category);
    }

    @Override
    public CategoryDto update(NewCategoryDto categoryDto, Long categoryId) {
        Category existingCategory = categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Категория id = %d не найдена".formatted(categoryId)));
        if (!existingCategory.getName().equals(categoryDto.getName())) {
            categoryRepository.findByName(categoryDto.getName()).ifPresent(c -> {
                throw new ConflictException("Категория  %s уже существует".formatted(categoryDto.getName()));
            });
        }
        existingCategory.setName(categoryDto.getName());
        return CategoryMapper.mapToCategoryDto(categoryRepository.save(existingCategory));
    }

    @Override
    public void delete(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Категория id = %d не найдена".formatted(categoryId));
        }
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("Попытка удалить категорию с привязанными событиями");
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();
        return categories.stream()
                .map(CategoryMapper::mapToCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Категория id = %d не найдена".formatted(categoryId)));
        return CategoryMapper.mapToCategoryDto(category);
    }
}
