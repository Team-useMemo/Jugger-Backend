package com.usememo.jugger.domain.calendar.repository;

import java.time.Instant;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.calendar.entity.Calendar;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CalendarRepository extends ReactiveMongoRepository<Calendar, String> {
	Flux<Calendar> findByUserUuidAndStartDateTimeBetween(String userUuid, Instant start, Instant end);
	Mono<Void> deleteByUserUuid(String userId);
}
