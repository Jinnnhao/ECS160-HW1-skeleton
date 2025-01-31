package com.ecs160.hw1.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecs160.hw1.Author;
import com.ecs160.hw1.Post;
import com.ecs160.hw1.Thread;

class WeightedAnalyzerTest {
    private WeightedAnalyzer analyzer;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        analyzer = new WeightedAnalyzer();
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
        Post rootPost = new Post(null, null, "This is a test post", "2024-01-01T10:00:00Z", testAuthor);
        Thread thread = new Thread(rootPost);
        
        assertEquals(2.0, analyzer.getTotalPosts(thread)); // Weight = 1 + (4/4) = 2
        assertEquals(0.0, analyzer.getAverageReplies(thread));
    }

    @Test
    void testThreadWithVaryingLengths() {
        Post rootPost = new Post(null, null, "Short post", "2024-01-01T10:00:00Z", testAuthor);
        Post reply1 = new Post(null, null, "This is a longer reply post", "2024-01-01T10:30:00Z", testAuthor);
        Post reply2 = new Post(null, null, "This is the longest reply post with more words", "2024-01-01T11:00:00Z", testAuthor);
        
        rootPost.addReply(reply1);
        rootPost.addReply(reply2);
        Thread thread = new Thread(rootPost);
        
        // Max words = 9 (reply2)
        // rootPost weight = 1 + (2/9) ≈ 1.22
        // reply1 weight = 1 + (6/9) ≈ 1.67
        // reply2 weight = 1 + (9/9) = 2.0
        double expectedTotal = 1.22 + 1.67 + 2.0;
        assertEquals(expectedTotal, analyzer.getTotalPosts(thread), 0.01);
    }

    @Test
    void testNestedRepliesWithWeights() {
        Post rootPost = new Post(null, null, "Root post", "2024-01-01T10:00:00Z", testAuthor);
        Post reply1 = new Post(null, null, "First level reply", "2024-01-01T10:30:00Z", testAuthor);
        Post nestedReply = new Post(null, null, "This is a nested reply with more words", "2024-01-01T11:00:00Z", testAuthor);
        
        reply1.addReply(nestedReply);
        rootPost.addReply(reply1);
        Thread thread = new Thread(rootPost);
        
        // Max words = 8 (nestedReply)
        // rootPost weight = 1 + (2/8) = 1.25
        // reply1 weight = 1 + (3/8) = 1.375
        // nestedReply weight = 1 + (8/8) = 2.0
        double expectedTotal = 1.25 + 1.375 + 2.0;
        assertEquals(expectedTotal, analyzer.getTotalPosts(thread), 0.01);
    }

    @Test
    void testNullText() {
        Post rootPost = new Post(null, null, null, "2024-01-01T10:00:00Z", testAuthor);
        Post reply = new Post(null, null, "This is a reply", "2024-01-01T11:00:00Z", testAuthor);
        
        rootPost.addReply(reply);
        Thread thread = new Thread(rootPost);
        
        // Max words = 4 (reply)
        // rootPost weight = 1 + (0/4) = 1.0
        // reply weight = 1 + (4/4) = 2.0
        assertEquals(3.0, analyzer.getTotalPosts(thread));
    }

    @Test
    void testVeryLongPost() {
        String longText = "This is a very long post with many words repeated many times. " +
                         "This is a very long post with many words repeated many times. " +
                         "This is a very long post with many words repeated many times.";
        Post rootPost = new Post(null, null, longText, "2024-01-01T10:00:00Z", testAuthor);
        Post shortReply = new Post(null, null, "Short reply", "2024-01-01T11:00:00Z", testAuthor);
        
        rootPost.addReply(shortReply);
        Thread thread = new Thread(rootPost);
        
        assertTrue(analyzer.getTotalPosts(thread) > 2.0); // Should be weighted more than unweighted
        assertTrue(analyzer.getAverageReplies(thread) > 0.0);
    }
} 