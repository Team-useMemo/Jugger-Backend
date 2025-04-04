package com.usememo.jugger.global.exception.chat;

import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;

public class CategoryNullException extends BaseException {
	public CategoryNullException() {
		super(ErrorCode.CATEGORY_IS_NULL);
	}
}
