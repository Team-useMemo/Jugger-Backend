package com.usememo.jugger.domain.category.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import com.usememo.jugger.global.utils.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "category")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@Setter
public class Category extends BaseTimeEntity implements Persistable<String> {

	@Id
	private String uuid;
	private String name;
	private String color;
	private Boolean isPinned;
	private String userUuid;

	public void setPinned(Boolean pinned) {
		isPinned = pinned;
	}

	@Override
	public String getId() {
		return getUuid();
	}

	@Override
	public boolean isNew() {
		return getCreatedAt() == null;
	}

}
