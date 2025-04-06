package com.usememo.jugger.global.exception.category;

import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;

public class CategoryExistException extends BaseException {
	public CategoryExistException() {
		super(ErrorCode.CATEGORY_ALREADY_EXIST);
	}
}
