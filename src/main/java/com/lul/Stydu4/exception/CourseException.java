package com.lul.Stydu4.exception;

import com.lul.Stydu4.enums.ErrorCode;

/**
 * Custom exception for course-related business logic errors
 * 
 * This exception is thrown when course business rules are violated:
 * - Course not available for purchase
 * - Course not published
 * - Invalid course price
 * - Course access restrictions
 * 
 * @author Stydu4 Team
 */
public class CourseException extends AppException {
    
    /**
     * Create course exception with error code
     * 
     * @param errorCode The specific error code for the course issue
     */
    public CourseException(ErrorCode errorCode) {
        super(errorCode);
    }
}
