package com.hotelbooking.auth.dto;

public record ApiError(
	    String code,
	    String message
	) {}