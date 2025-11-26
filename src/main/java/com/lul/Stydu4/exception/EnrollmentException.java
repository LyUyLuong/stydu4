package com.lul.Stydu4.exception;

import com.lul.Stydu4.enums.ErrorCode;

/**
 * Custom exception for enrollment-related business logic errors
 * 
 * This exception is thrown when enrollment business rules are violated:
 * - User already enrolled in course
 * - Enrollment has expired
 * - Course prerequisites not met
 * 
 * @author Stydu4 Team
 */
public class EnrollmentException extends AppException {
    
    /**
     * Create enrollment exception with error code
     * 
     * @param errorCode The specific error code for the enrollment issue
     */
    public EnrollmentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
