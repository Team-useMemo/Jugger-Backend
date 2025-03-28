package com.usememo.jugger.domain.versionHistory.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Document(collection = "version_histories")
@AllArgsConstructor
@Builder
public class VersionHistory {
	@Id
	private String uuid;
	private String data;
	private String prev;
	private String modify;
}
