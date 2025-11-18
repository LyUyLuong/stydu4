package com.lul.Stydu4.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // ============================================
    // GENERAL ERRORS (1xxx)
    // ============================================
    UNCATEGORIZED_EXCEPTION(1000, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid key", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_BODY(1002, "Request body is invalid or missing", HttpStatus.BAD_REQUEST),

    // ============================================
    // AUTHENTICATION & AUTHORIZATION ERRORS (10xx)
    // ============================================
    INVALID_CREDENTIALS(1010, "Invalid credentials", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED(1011, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1012, "Don't have permission", HttpStatus.FORBIDDEN),

    // ============================================
    // USER ERRORS (11xx)
    // ============================================
    USER_EXISTED(1100, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1101, "User not found", HttpStatus.NOT_FOUND),
    USERNAME_REQUIRED(1102, "Username is required", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1103, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1104, "Password is required", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1105, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1106, "Email already exists", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1107, "Email is required", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1108, "Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_DOB(1109, "Age must be at least {min}", HttpStatus.BAD_REQUEST),
    USER_BANNED(1110, "User account has been banned", HttpStatus.FORBIDDEN),

    // ============================================
    // ROLE ERRORS (12xx)
    // ============================================
    INVALID_ROLE(1200, "Invalid role", HttpStatus.BAD_REQUEST),

    // ============================================
    // TEST ERRORS (20xx)
    // ============================================
    TEST_NOT_FOUND(2000, "Test not found", HttpStatus.NOT_FOUND),
    INVALID_TEST_TYPE(2001, "Invalid test type", HttpStatus.BAD_REQUEST),

    // ============================================
    // PART TEST ERRORS (21xx)
    // ============================================
    PART_TEST_NOT_FOUND(2100, "Part test not found", HttpStatus.NOT_FOUND),
    INVALID_PART_TYPE(2101, "Invalid part type", HttpStatus.BAD_REQUEST),
    INVALID_PART_SELECTION(2102, "Invalid part selection", HttpStatus.BAD_REQUEST),

    // ============================================
    // QUESTION ERRORS (22xx)
    // ============================================
    QUESTION_NOT_FOUND(2200, "Question not found", HttpStatus.NOT_FOUND),
    INVALID_QUESTION_TYPE(2201, "Invalid question type", HttpStatus.BAD_REQUEST),

    // ============================================
    // QUESTION GROUP ERRORS (23xx)
    // ============================================
    QUESTION_GROUP_NOT_FOUND(2300, "Question group not found", HttpStatus.NOT_FOUND),

    // ============================================
    // ANSWER ERRORS (24xx)
    // ============================================
    ANSWER_NOT_FOUND(2400, "Answer not found", HttpStatus.NOT_FOUND),

    // ============================================
    // RESULT ERRORS (25xx)
    // ============================================
    RESULT_NOT_FOUND(2500, "Result not found", HttpStatus.NOT_FOUND),
    CALCULATION_ERROR(2501, "Calculation error occurred", HttpStatus.INTERNAL_SERVER_ERROR),

    // ============================================
    // FILE ERRORS (40xx)
    // ============================================
    FILE_EMPTY(4000, "File is empty", HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND(4001, "File not found", HttpStatus.NOT_FOUND),
    FILE_INVALID_EXTENSION(4002, "Invalid file extension", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(4003, "File size exceeds maximum allowed", HttpStatus.PAYLOAD_TOO_LARGE),
    FILE_STORE_FAILED(4004, "Failed to store file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED(4005, "Failed to delete file", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE_TYPE(4006, "Invalid file type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    // ============================================
    // COURSE ERRORS (90xx)
    // ============================================
    COURSE_NOT_FOUND(9000, "Course not found", HttpStatus.NOT_FOUND),
    COURSE_ALREADY_PURCHASED(9001, "Course already purchased", HttpStatus.CONFLICT),
    COURSE_ALREADY_IN_CART(9002, "Course already in cart", HttpStatus.CONFLICT),
    COURSE_NOT_AVAILABLE(9003, "Course is not available for purchase", HttpStatus.BAD_REQUEST),
    COURSE_NOT_PUBLISHED(9004, "Course is not published", HttpStatus.BAD_REQUEST),
    INVALID_COURSE_PRICE(9005, "Invalid course price", HttpStatus.BAD_REQUEST),

    // ============================================
    // ENROLLMENT ERRORS (91xx)
    // ============================================
    ENROLLMENT_NOT_FOUND(9100, "Enrollment not found", HttpStatus.NOT_FOUND),
    ENROLLMENT_ALREADY_EXISTS(9101, "User already enrolled in this course", HttpStatus.CONFLICT),
    ENROLLMENT_EXPIRED(9102, "Enrollment has expired", HttpStatus.FORBIDDEN),

    // ============================================
    // PAYMENT ERRORS (92xx)
    // ============================================
    PAYMENT_FAILED(9200, "Payment failed", HttpStatus.PAYMENT_REQUIRED),
    PAYMENT_VERIFICATION_FAILED(9201, "Payment verification failed", HttpStatus.UNPROCESSABLE_ENTITY),
    PAYMENT_SESSION_NOT_FOUND(9202, "Payment session not found", HttpStatus.NOT_FOUND),
    PAYMENT_PROCESSING_FAILED(9203, "Payment processing failed", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_UNAUTHORIZED(9204, "Unauthorized payment access", HttpStatus.FORBIDDEN),

    // ============================================
    // CART ERRORS (93xx)
    // ============================================
    CART_EMPTY(9300, "Cart is empty", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND(9301, "Cart item not found", HttpStatus.NOT_FOUND),

    // ============================================
    // ORDER ERRORS (94xx)
    // ============================================
    ORDER_NOT_FOUND(9400, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_COMPLETED(9401, "Order already completed", HttpStatus.CONFLICT),
    ORDER_ALREADY_CANCELLED(9402, "Order already cancelled", HttpStatus.CONFLICT),

    // ============================================
    // LECTURE ERRORS (95xx)
    // ============================================
    LECTURE_NOT_FOUND(9500, "Lecture not found", HttpStatus.NOT_FOUND),
    LECTURE_ALREADY_PUBLISHED(9501, "Lecture already published", HttpStatus.CONFLICT),
    LECTURE_NOT_PUBLISHED(9502, "Lecture is not published", HttpStatus.BAD_REQUEST),

    // ============================================
    // VALIDATION ERRORS (99xx)
    // ============================================
    INVALID_REQUEST(9900, "Invalid request", HttpStatus.BAD_REQUEST)
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
