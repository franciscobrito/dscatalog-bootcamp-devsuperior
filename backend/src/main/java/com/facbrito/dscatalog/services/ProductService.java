package com.facbrito.dscatalog.services;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.facbrito.dscatalog.dto.CategoryDTO;
import com.facbrito.dscatalog.dto.ProductDTO;
import com.facbrito.dscatalog.dto.UriDTO;
import com.facbrito.dscatalog.entities.Category;
import com.facbrito.dscatalog.entities.Product;
import com.facbrito.dscatalog.repositories.CategoryRepository;
import com.facbrito.dscatalog.repositories.ProductRepository;
import com.facbrito.dscatalog.services.exceptions.DatabaseException;
import com.facbrito.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class ProductService {

	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private S3Service s3Service;

	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Long categoryId, String name, PageRequest pageRequest) {
		List<Category> categories = (categoryId == 0) ? null : Arrays.asList(categoryRepository.getOne(categoryId));
		Page<Product> list = repository.find(categories, name, pageRequest);
		return list.map(c -> new ProductDTO(c));
	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Product product = repository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new ProductDTO(product, product.getCategories());
	}

	@Transactional
	public ProductDTO insert(ProductDTO productDTO) {
		Product product = new Product();
		copyDtoToEntity(productDTO, product);
		product = repository.save(product);
		return new ProductDTO(product);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO productDTO) {
		try {
			Product product = repository.getOne(id);
			copyDtoToEntity(productDTO, product);
			product = repository.save(product);
			return new ProductDTO(product);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
	}

	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id not found" + id);
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}
	
	private void copyDtoToEntity(ProductDTO productDTO, Product product) {
		product.setName(productDTO.getName());
		product.setDescription(productDTO.getDescription());
		product.setDate(productDTO.getDate());
		product.setImgUrl(productDTO.getImgUrl());
		product.setPrice(productDTO.getPrice());
		
		product.getCategories().clear();
		for (CategoryDTO catDto : productDTO.getCategories()) {
			Category category = categoryRepository.getOne(catDto.getId());
			product.getCategories().add(category);
		}
	}

	public UriDTO uploadFile(MultipartFile file) {
		URL url = s3Service.uploadFile(file);
		return new UriDTO(url.toString());
	}

}
