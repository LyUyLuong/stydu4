package com.lul.Stydu4.util;

import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class EnumValidator {

    /**
     * Validate và convert string thành enum (case-insensitive)
     *
     * @param value String value từ request
     * @param enumClass Class của enum cần convert
     * @param errorCode ErrorCode để throw khi invalid
     * @param <T> Enum type
     * @return Enum value
     * @throws AppException nếu invalid
     */
    public static <T extends Enum<T>> T validateAndConvert(
            String value,
            Class<T> enumClass,
            ErrorCode errorCode
    ) {
        if (value == null || value.isBlank()) {
            log.error("{} is null or blank", enumClass.getSimpleName());
            throw new AppException(errorCode);
        }

        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues = getValidValues(enumClass);
            log.error("Invalid {}: '{}'. Valid values: {}",
                    enumClass.getSimpleName(),
                    value,
                    validValues
            );
            throw new AppException(errorCode);
        }
    }


    /**
     * Convert string thành enum (case-insensitive) hoặc return null nếu invalid
     * Không throw exception
     */
    public static <T extends Enum<T>> T convertOrNull(String value, Class<T> enumClass) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Cannot convert '{}' to {}, returning null", value, enumClass.getSimpleName());
            return null;
        }
    }

    /**
     * Check xem string có phải valid enum value không
     */
    public static <T extends Enum<T>> boolean isValid(String value, Class<T> enumClass) {
        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            Enum.valueOf(enumClass, value.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Lấy danh sách valid values của enum
     */
    public static <T extends Enum<T>> String getValidValues(Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    /**
     * Lấy danh sách valid values dưới dạng array
     */
    public static <T extends Enum<T>> String[] getValidValuesArray(Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .toArray(String[]::new);
    }
}
