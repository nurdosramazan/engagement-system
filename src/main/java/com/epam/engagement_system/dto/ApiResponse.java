package com.epam.engagement_system.dto;

public record ApiResponse<T>(boolean success, String message, T data) {
}
