package com.tabletennis.app.common.response;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, Meta meta, ApiError error) {
    public record Meta(int page, int size, long total) {}
    public record ApiError(String code, String message) {}
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true,data,null,null); }
    public static <T> ApiResponse<T> page(T data, int page, int size, long total) { return new ApiResponse<>(true,data,new Meta(page,size,total),null); }
    public static ApiResponse<Void> fail(String code,String message) { return new ApiResponse<>(false,null,null,new ApiError(code,message)); }
}
