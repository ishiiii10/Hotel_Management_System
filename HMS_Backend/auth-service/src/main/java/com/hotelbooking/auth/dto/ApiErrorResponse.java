package com.hotelbooking.auth.dto;

public record ApiErrorResponse(
	    boolean success,
	    ApiError error
	) {}
