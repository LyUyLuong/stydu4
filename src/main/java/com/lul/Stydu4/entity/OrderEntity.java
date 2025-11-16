package com.lul.Stydu4.entity;

import com.lul.Stydu4.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "orders", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_stripe_session_id", columnList = "stripeSessionId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_status_created", columnList = "status, createdDate"),
    @Index(name = "idx_course_user", columnList = "course_id, user_id")
})
public class OrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String stripeSessionId;      // Stripe Checkout Session ID
    private String stripePaymentIntentId; // Stripe Payment Intent ID
    private String stripeCustomerId;     // Stripe Customer ID (optional)
}
