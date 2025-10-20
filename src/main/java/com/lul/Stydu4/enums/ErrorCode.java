package com.lul.Stydu4.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error",HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error",HttpStatus.BAD_REQUEST),

    USER_EXISTED(1002, "User existed",HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1003, "Username must be at least {min} characters",HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters",HttpStatus.BAD_REQUEST),

    USER_NOT_EXISTED(1005, "User not existed",HttpStatus.NOT_FOUND),

    UNAUTHENTICATED(1006, "Unauthenticated",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "Don't have permission",HttpStatus.FORBIDDEN),

    INVALID_DOB(1008, "Age must be at least {min}",HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_BODY(1009, "Request body is invalid or missing", HttpStatus.BAD_REQUEST),

    USERNAME_REQUIRED(1010, "Username is required", HttpStatus.BAD_REQUEST),

    PASSWORD_REQUIRED(1020, "Password is required", HttpStatus.BAD_REQUEST),


    //TestErrorCode
    TEST_NOT_FOUND(2008, "Test not found",HttpStatus.NOT_FOUND),
    INVALID_TEST_TYPE(2009, "Invalid test type", HttpStatus.BAD_REQUEST),

    //PartTestErrorCode
    PART_TEST_NOT_FOUND(3010, "Part test not found",HttpStatus.NOT_FOUND),
    INVALID_PART_TYPE(3014, "Invalid part type", HttpStatus.BAD_REQUEST),
    INVALID_PART_SELECTION(3015, "Invalid part selection", HttpStatus.BAD_REQUEST),

    // Role ErrorCode
    INVALID_ROLE(4013, "Invalid role", HttpStatus.BAD_REQUEST),

    QUESTION_NOT_FOUND(5001, "Question not found", HttpStatus.NOT_FOUND),
    INVALID_QUESTION_TYPE(5002, "Invalid question type", HttpStatus.BAD_REQUEST),

    QUESTION_GROUP_NOT_FOUND(6001, "Question group not found", HttpStatus.NOT_FOUND),

    ANSWER_NOT_FOUND(7001, "Answer not found", HttpStatus.NOT_FOUND),

    RESULT_NOT_FOUND(8001, "Result not found", HttpStatus.NOT_FOUND),
    CALCULATION_ERROR(8002, "Calculation error occurred", HttpStatus.INTERNAL_SERVER_ERROR)
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
