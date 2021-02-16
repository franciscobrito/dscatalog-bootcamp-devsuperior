package com.facbrito.dscatalog.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.facbrito.dscatalog.entities.Category;
import com.facbrito.dscatalog.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query("SELECT DISTINCT o FROM Product o INNER JOIN o.categories cats WHERE "
			+ "(COALESCE(:categories) IS NULL OR cats IN :categories) AND "
			+ "(LOWER(o.name) LIKE LOWER(CONCAT('%',:name,'%')) )")
	Page<Product> find(List<Category> categories, String name, Pageable pageable);
}
