package com.usememo.jugger.domain.link.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkListResponse {
		private String categoryId;
		private String linkId;
		private String link;
}
