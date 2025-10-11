package com.lul.Stydu4.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error",HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error",HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed",HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least 3 characters",HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least 8 characters",HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed",HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "Don't have permission",HttpStatus.FORBIDDEN),

    //TestErrorCode
    TEST_NOT_FOUND(1008, "Test not found",HttpStatus.NOT_FOUND),
    INVALID_TEST_TYPE(1009, "Invalid test type", HttpStatus.BAD_REQUEST),

    //PartTestErrorCode
    PART_TEST_NOT_FOUND(1010, "Part test not found",HttpStatus.NOT_FOUND),
    INVALID_PART_TYPE(1014, "Invalid part type", HttpStatus.BAD_REQUEST),

    // Role ErrorCode
    INVALID_ROLE(1013, "Invalid role", HttpStatus.BAD_REQUEST),

    ;

    ErrorCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatus statusCode;

}
