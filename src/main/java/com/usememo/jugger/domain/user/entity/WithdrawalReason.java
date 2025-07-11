package com.usememo.jugger.domain.user.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;

@Document(collection = "withdrawal_reasons")
public class WithdrawalReason {

	@Id
	private String uuid;
	private String userUuid;
	private ReasonCode reasonCode;
	private String reasonDetail;
	private Instant withdrawAt;

	public enum ReasonCode {
		NOT_USED,
		INCONVENIENT,
		NO_FEATURE,
		ETC
	}

	@Builder
	public WithdrawalReason(String uuid, String userUuid, ReasonCode reasonCode, String reasonDetail,
		Instant withdrawAt) {
		this.uuid = uuid;
		this.userUuid = userUuid;
		this.reasonCode = reasonCode;
		this.reasonDetail = reasonDetail;
		this.withdrawAt = withdrawAt;
	}
}

