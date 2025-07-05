package com.usememo.jugger.domain.user.dto;

import com.usememo.jugger.domain.user.entity.WithdrawalReason;

public record WithdrawalRequest(
	WithdrawalReason.ReasonCode reasonCode,
	String reasonDetail
) {
}

