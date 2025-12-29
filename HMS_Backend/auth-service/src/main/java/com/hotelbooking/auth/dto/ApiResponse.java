package com.hotelbooking.auth.dto;

public record ApiResponse<T>(
	    boolean success,
	    T data
	) {}