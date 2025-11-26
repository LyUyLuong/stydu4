package com.lul.Stydu4.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugHelper {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGES_DASHES = Pattern.compile("(^-|-$)");

    /**
     * Generate a URL-friendly slug from a given string
     * Supports Vietnamese characters
     *
     * @param input the input string to convert to slug
     * @return the generated slug
     */
    public static String toSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Convert Vietnamese characters to ASCII equivalents
        String noVietnamese = removeVietnameseAccents(input);

        // Convert to lowercase
        String slug = noVietnamese.toLowerCase(Locale.ENGLISH);

        // Replace whitespace with dashes
        slug = WHITESPACE.matcher(slug).replaceAll("-");

        // Remove non-latin characters (keep letters, numbers, and dashes)
        slug = NONLATIN.matcher(slug).replaceAll("");

        // Remove dashes from edges
        slug = EDGES_DASHES.matcher(slug).replaceAll("");

        // Replace multiple consecutive dashes with single dash
        slug = slug.replaceAll("-+", "-");

        return slug;
    }

    /**
     * Generate a unique slug by appending timestamp if needed
     *
     * @param input the input string to convert to slug
     * @return the generated unique slug
     */
    public static String toUniqueSlug(String input) {
        String baseSlug = toSlug(input);
        if (baseSlug.isEmpty()) {
            baseSlug = "test";
        }
        return baseSlug + "-" + System.currentTimeMillis();
    }

    /**
     * Remove Vietnamese accents and convert to ASCII
     *
     * @param str the string with Vietnamese characters
     * @return the string with ASCII characters
     */
    private static String removeVietnameseAccents(String str) {
        // Normalize Vietnamese characters
        str = str.replaceAll("à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ", "a");
        str = str.replaceAll("è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ", "e");
        str = str.replaceAll("ì|í|ị|ỉ|ĩ", "i");
        str = str.replaceAll("ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ", "o");
        str = str.replaceAll("ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ", "u");
        str = str.replaceAll("ỳ|ý|ỵ|ỷ|ỹ", "y");
        str = str.replaceAll("đ", "d");

        str = str.replaceAll("À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ", "A");
        str = str.replaceAll("È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ", "E");
        str = str.replaceAll("Ì|Í|Ị|Ỉ|Ĩ", "I");
        str = str.replaceAll("Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ", "O");
        str = str.replaceAll("Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ", "U");
        str = str.replaceAll("Ỳ|Ý|Ỵ|Ỷ|Ỹ", "Y");
        str = str.replaceAll("Đ", "D");

        return str;
    }

    /**
     * Generate slug with optional suffix for uniqueness
     *
     * @param input the input string
     * @param suffix optional suffix to append
     * @return the generated slug
     */
    public static String toSlug(String input, String suffix) {
        String slug = toSlug(input);
        if (suffix != null && !suffix.isEmpty()) {
            slug = slug + "-" + toSlug(suffix);
        }
        return slug;
    }
}
