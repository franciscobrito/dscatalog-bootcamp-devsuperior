package com.facbrito.dscatalog.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.facbrito.dscatalog.dto.CategoryDTO;
import com.facbrito.dscatalog.entities.Category;
import com.facbrito.dscatalog.repositories.CategoryRepository;
import com.facbrito.dscatalog.services.exceptions.EntityNotFoundException;

@Service
public class CategoryService {

	@Autowired
	private CategoryRepository repository;

	@Transactional(readOnly = true)
	public List<CategoryDTO> findAll() {
		List<Category> list = repository.findAll();
		return list.stream().map(c -> new CategoryDTO(c)).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public CategoryDTO findById(Long id) {
		Category category = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Entity not found"));
		return new CategoryDTO(category);
	}

}
