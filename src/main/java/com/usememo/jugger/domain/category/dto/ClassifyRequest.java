package com.usememo.jugger.domain.category.dto;

import java.util.List;

public record ClassifyRequest(String paragraph,
							  List<String> userCategories,
							  Double threshold
	// , String k
) {
}
