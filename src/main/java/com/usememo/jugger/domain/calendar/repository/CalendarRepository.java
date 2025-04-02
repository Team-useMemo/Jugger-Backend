package com.usememo.jugger.domain.calendar.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.calendar.entity.Calendar;

public interface CalendarRepository extends ReactiveMongoRepository<Calendar, String> {
}
