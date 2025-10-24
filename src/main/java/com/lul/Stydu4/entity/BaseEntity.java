package com.lul.Stydu4.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;


@MappedSuperclass
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
public abstract class BaseEntity implements java.io.Serializable {

    @org.springframework.data.annotation.CreatedDate
    @jakarta.persistence.Column(name = "createdDate", updatable = false)
    private java.time.LocalDateTime createdDate;

    @org.springframework.data.annotation.CreatedBy
    @jakarta.persistence.Column(name = "createdBy", updatable = false)
    private String createdBy;

    @org.springframework.data.annotation.LastModifiedDate
    @jakarta.persistence.Column(name = "modifiedDate")
    private java.time.LocalDateTime modifiedDate;

    @org.springframework.data.annotation.LastModifiedBy
    @jakarta.persistence.Column(name = "modifiedBy")
    private String modifiedBy;

    // getters/setters...
}

