package com.lul.Stydu4.exception;

import com.lul.Stydu4.enums.ErrorCode;

/**
 * Custom exception for payment-related business logic errors
 * 
 * This exception is thrown when payment business rules are violated:
 * - Payment verification fails
 * - Unauthorized payment access
 * - Payment processing errors
 * - Stripe API errors
 * 
 * @author Stydu4 Team
 */
public class PaymentException extends AppException {
    
    /**
     * Create payment exception with error code
     * 
     * @param errorCode The specific error code for the payment issue
     */
    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
