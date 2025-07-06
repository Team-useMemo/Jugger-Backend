package com.usememo.jugger.domain.user.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.user.entity.WithdrawalReason;

public interface WithdrawalReasonRepository extends ReactiveMongoRepository<WithdrawalReason, String> {
}
