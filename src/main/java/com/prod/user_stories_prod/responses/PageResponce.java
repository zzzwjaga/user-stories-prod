package com.prod.user_stories_prod.responses;

import java.util.List;

public record PageResponce<T>(List<T> content,
                              int page,
                              int size,
                              long totalElements,
                              int totalPages) {
}
