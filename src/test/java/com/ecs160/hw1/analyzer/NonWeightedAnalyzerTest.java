package com.ecs160.hw1.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecs160.hw1.Author;
import com.ecs160.hw1.Post;
import com.ecs160.hw1.Thread;

class NonWeightedAnalyzerTest {
    private NonWeightedAnalyzer analyzer;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        analyzer = new NonWeightedAnalyzer();
        testAuthor = new Author("test-did", "test-handle", "Test Author", "test-avatar");
    }

    @Test
    void testEmptyThread() {
        Post rootPost = new Post(null, null, "", null, testAuthor);
        Thread thread = new Thread(rootPost);
        
        assertEquals(1.0, analyzer.getTotalPosts(thread));
        assertEquals(0.0, analyzer.getAverageReplies(thread));
        assertEquals("00:00:00", analyzer.getAverageInterval(thread));
    }

    @Test
    void testSinglePostThread() {
        Post rootPost = new Post(null, null, "Single post", "2024-01-01T10:00:00Z", testAuthor);
        Thread thread = new Thread(rootPost);
        
        assertEquals(1.0, analyzer.getTotalPosts(thread));
        assertEquals(0.0, analyzer.getAverageReplies(thread));
        assertEquals("00:00:00", analyzer.getAverageInterval(thread));
    }

    @Test
    void testSimpleThreadWithReplies() {
        Post rootPost = new Post(null, null, "Root post", "2024-01-01T10:00:00Z", testAuthor);
        Post reply1 = new Post(null, null, "Reply 1", "2024-01-01T10:30:00Z", testAuthor);
        Post reply2 = new Post(null, null, "Reply 2", "2024-01-01T11:00:00Z", testAuthor);
        
        rootPost.addReply(reply1);
        rootPost.addReply(reply2);
        Thread thread = new Thread(rootPost);
        
        assertEquals(3.0, analyzer.getTotalPosts(thread));
        assertEquals(0.6666666666666666, analyzer.getAverageReplies(thread), 0.0001);
        assertEquals("00:30:00", analyzer.getAverageInterval(thread));
    }

    @Test
    void testNestedReplies() {
        Post rootPost = new Post(null, null, "Root", "2024-01-01T10:00:00Z", testAuthor);
        Post reply1 = new Post(null, null, "Reply 1", "2024-01-01T10:30:00Z", testAuthor);
        Post reply2 = new Post(null, null, "Reply 2", "2024-01-01T11:00:00Z", testAuthor);
        Post nestedReply = new Post(null, null, "Nested Reply", "2024-01-01T11:30:00Z", testAuthor);
        
        reply1.addReply(nestedReply);
        rootPost.addReply(reply1);
        rootPost.addReply(reply2);
        Thread thread = new Thread(rootPost);
        
        assertEquals(4.0, analyzer.getTotalPosts(thread));
        assertEquals(0.75, analyzer.getAverageReplies(thread));
        assertEquals("00:30:00", analyzer.getAverageInterval(thread));
    }

    @Test
    void testNullCreatedAt() {
        Post rootPost = new Post(null, null, "Root", null, testAuthor);
        Post reply = new Post(null, null, "Reply", null, testAuthor);
        
        rootPost.addReply(reply);
        Thread thread = new Thread(rootPost);
        
        assertEquals(2.0, analyzer.getTotalPosts(thread));
        assertEquals(0.5, analyzer.getAverageReplies(thread));
        assertEquals("00:00:00", analyzer.getAverageInterval(thread));
    }

    @Test
    void testNullText() {
        Post rootPost = new Post(null, null, null, "2024-01-01T10:00:00Z", testAuthor);
        Post reply = new Post(null, null, null, "2024-01-01T11:00:00Z", testAuthor);
        
        rootPost.addReply(reply);
        Thread thread = new Thread(rootPost);
        
        assertEquals(2.0, analyzer.getTotalPosts(thread));
        assertEquals(0.5, analyzer.getAverageReplies(thread));
        assertEquals("01:00:00", analyzer.getAverageInterval(thread));
    }
}