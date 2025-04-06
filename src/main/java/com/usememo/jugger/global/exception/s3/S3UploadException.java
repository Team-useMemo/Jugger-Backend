package com.usememo.jugger.global.exception.s3;

import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;

public class S3UploadException extends BaseException {
	public S3UploadException() {
		super(ErrorCode.FILE_UPLOAD_FAIL);
	}
}
