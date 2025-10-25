package com.lul.Stydu4.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SlugHelperTest {

    @Test
    void testToSlug_SimpleEnglish() {
        assertEquals("hello-world", SlugHelper.toSlug("Hello World"));
        assertEquals("test-123", SlugHelper.toSlug("Test 123"));
        assertEquals("hello-world-2024", SlugHelper.toSlug("Hello World 2024"));
    }

    @Test
    void testToSlug_Vietnamese() {
        assertEquals("bai-thi-toeic", SlugHelper.toSlug("Bài thi TOEIC"));
        assertEquals("de-thi-tieng-anh", SlugHelper.toSlug("Đề thi tiếng Anh"));
        assertEquals("luyen-thi-listening", SlugHelper.toSlug("Luyện thi Listening"));
        assertEquals("bai-tap-ngu-phap", SlugHelper.toSlug("Bài tập ngữ pháp"));
    }

    @Test
    void testToSlug_SpecialCharacters() {
        assertEquals("hello-world", SlugHelper.toSlug("Hello!!! World???"));
        assertEquals("test-123", SlugHelper.toSlug("Test@#$123"));
        assertEquals("a-b-c", SlugHelper.toSlug("A   B   C"));
    }

    @Test
    void testToSlug_EdgeCases() {
        assertEquals("", SlugHelper.toSlug(""));
        assertEquals("", SlugHelper.toSlug(null));
        assertEquals("123", SlugHelper.toSlug("123"));
        assertEquals("test", SlugHelper.toSlug("   test   "));
    }

    @Test
    void testToSlug_MultipleSpaces() {
        assertEquals("hello-world", SlugHelper.toSlug("hello     world"));
        assertEquals("a-b-c", SlugHelper.toSlug("a - b - c"));
    }

    @Test
    void testToUniqueSlug() {
        String slug1 = SlugHelper.toUniqueSlug("Test");
        String slug2 = SlugHelper.toUniqueSlug("Test");
        
        assertTrue(slug1.startsWith("test-"));
        assertTrue(slug2.startsWith("test-"));
        assertNotEquals(slug1, slug2); // Should be different due to timestamp
    }

    @Test
    void testToSlug_WithSuffix() {
        assertEquals("test-2024", SlugHelper.toSlug("Test", "2024"));
        assertEquals("hello-world", SlugHelper.toSlug("Hello", "World"));
        assertEquals("test", SlugHelper.toSlug("Test", ""));
        assertEquals("test", SlugHelper.toSlug("Test", null));
    }

    @Test
    void testToSlug_VietnameseAccents() {
        assertEquals("toan-hoc", SlugHelper.toSlug("Toán học"));
        assertEquals("van-hoc", SlugHelper.toSlug("Văn học"));
        assertEquals("lich-su", SlugHelper.toSlug("Lịch sử"));
        assertEquals("dia-ly", SlugHelper.toSlug("Địa lý"));
        assertEquals("ngu-van", SlugHelper.toSlug("Ngữ văn"));
    }

    @Test
    void testToSlug_MixedCase() {
        assertEquals("hello-world", SlugHelper.toSlug("HeLLo WoRLd"));
        assertEquals("test-toeic-2024", SlugHelper.toSlug("TEST TOEIC 2024"));
    }

    @Test
    void testToSlug_Numbers() {
        assertEquals("toeic-part-1", SlugHelper.toSlug("TOEIC Part 1"));
        assertEquals("test-123-abc", SlugHelper.toSlug("Test 123 ABC"));
    }

    @Test
    void testToSlug_LeadingTrailingDashes() {
        assertEquals("test", SlugHelper.toSlug("---test---"));
        assertEquals("hello-world", SlugHelper.toSlug("---hello world---"));
    }
}
