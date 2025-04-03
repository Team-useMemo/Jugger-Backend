package com.usememo.jugger.domain.photo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.photo.entity.Photo;

public interface PhotoRepository extends ReactiveMongoRepository<Photo, String> {
}
