package com.usememo.jugger.domain.category.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.category.entity.Category;

public interface CategoryRepository extends ReactiveMongoRepository<Category, String> {
	
}
