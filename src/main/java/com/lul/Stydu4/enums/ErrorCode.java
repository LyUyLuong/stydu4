package com.lul.Stydu4.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error",HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error",HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1002, "Invalid credentials",HttpStatus.UNAUTHORIZED),

    USER_EXISTED(1003, "User existed",HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1004, "Username must be at least {min} characters",HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1005, "Password must be at least {min} characters",HttpStatus.BAD_REQUEST),

    USER_NOT_EXISTED(1006, "User not existed",HttpStatus.NOT_FOUND),

    UNAUTHENTICATED(1007, "Unauthenticated",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1008, "Don't have permission",HttpStatus.FORBIDDEN),

    INVALID_DOB(1009, "Age must be at least {min}",HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_BODY(1010, "Request body is invalid or missing", HttpStatus.BAD_REQUEST),

    USERNAME_REQUIRED(1011, "Username is required", HttpStatus.BAD_REQUEST),

    EMAIL_EXISTED(1012, "Email already exists", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1013, "Email is required", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1014, "Invalid email format", HttpStatus.BAD_REQUEST),

    PASSWORD_REQUIRED(1020, "Password is required", HttpStatus.BAD_REQUEST),


    //TestErrorCode
    TEST_NOT_FOUND(2008, "Test not found",HttpStatus.NOT_FOUND),
    INVALID_TEST_TYPE(2009, "Invalid test type", HttpStatus.BAD_REQUEST),

    //PartTestErrorCode
    PART_TEST_NOT_FOUND(3010, "Part test not found",HttpStatus.NOT_FOUND),
    INVALID_PART_TYPE(3014, "Invalid part type", HttpStatus.BAD_REQUEST),
    INVALID_PART_SELECTION(3015, "Invalid part selection", HttpStatus.BAD_REQUEST),

    FILE_EMPTY(4001, "File is empty", HttpStatus.BAD_REQUEST),
    FILE_INVALID_EXTENSION(4002, "Invalid file extension", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(4003, "File size exceeds maximum allowed", HttpStatus.BAD_REQUEST),
    FILE_STORE_FAILED(4004, "Failed to store file", HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND(4005, "File not found", HttpStatus.BAD_REQUEST),
    FILE_DELETE_FAILED(4006, "Failed to delete file", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(1031, "Invalid file type", HttpStatus.BAD_REQUEST),

    // Role ErrorCode
    INVALID_ROLE(4013, "Invalid role", HttpStatus.BAD_REQUEST),

    QUESTION_NOT_FOUND(5001, "Question not found", HttpStatus.NOT_FOUND),
    INVALID_QUESTION_TYPE(5002, "Invalid question type", HttpStatus.BAD_REQUEST),

    QUESTION_GROUP_NOT_FOUND(6001, "Question group not found", HttpStatus.NOT_FOUND),

    ANSWER_NOT_FOUND(7001, "Answer not found", HttpStatus.NOT_FOUND),

    RESULT_NOT_FOUND(8001, "Result not found", HttpStatus.NOT_FOUND),
    CALCULATION_ERROR(8002, "Calculation error occurred", HttpStatus.INTERNAL_SERVER_ERROR),

    // Course ErrorCode
    COURSE_NOT_FOUND(9001, "Course not found", HttpStatus.NOT_FOUND),
    COURSE_ALREADY_PURCHASED(9002, "Course already purchased", HttpStatus.BAD_REQUEST),
    ENROLLMENT_NOT_FOUND(9003, "Enrollment not found", HttpStatus.NOT_FOUND),
    PAYMENT_FAILED(9004, "Payment failed", HttpStatus.BAD_REQUEST)
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
