package com.tabletennis.app.common.response;
import java.util.List;
public record PageResponse<T>(List<T> content, int page, int size, long total) {
    public ApiResponse<List<T>> response() { return ApiResponse.page(content,page,size,total); }
}
