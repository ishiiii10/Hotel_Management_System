package com.hotelbooking.api_gateway.security;



import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PublicRoute {
    private String method;
    private String pathPrefix;
}