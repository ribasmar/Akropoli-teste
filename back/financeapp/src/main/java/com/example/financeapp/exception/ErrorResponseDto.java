package com.example.financeapp.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

    private final String code;
    private final String message;
    private final int status;
    private final String path;
    private final LocalDateTime timestamp;
    private List<FieldErrorDto> fieldErrors;

    private ErrorResponseDto(Builder builder) {
        this.code        = builder.code;
        this.message     = builder.message;
        this.status      = builder.status;
        this.path        = builder.path;
        this.timestamp   = LocalDateTime.now();
        this.fieldErrors = builder.fieldErrors;
    }

    public String getCode()                     { return code; }
    public String getMessage()                  { return message; }
    public int getStatus()                      { return status; }
    public String getPath()                     { return path; }
    public LocalDateTime getTimestamp()         { return timestamp; }
    public List<FieldErrorDto> getFieldErrors() { return fieldErrors; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String code;
        private String message;
        private int status;
        private String path;
        private List<FieldErrorDto> fieldErrors;

        public Builder code(String code)                       { this.code = code; return this; }
        public Builder message(String message)                 { this.message = message; return this; }
        public Builder status(int status)                      { this.status = status; return this; }
        public Builder path(String path)                       { this.path = path; return this; }
        public Builder fieldErrors(List<FieldErrorDto> errors) { this.fieldErrors = errors; return this; }
        public ErrorResponseDto build()                        { return new ErrorResponseDto(this); }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FieldErrorDto(String field, String message, Object rejectedValue) {}
}
