package com.usememo.jugger.domain.photo.dto;

public record PhotoUpdateRequest(String photoId, String categoryId, String description) {
}
