package com.ecs160.hw1.analyzer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.ecs160.hw1.Analyzer;
import com.ecs160.hw1.Post;
import com.ecs160.hw1.Thread;

public class NonWeightedAnalyzer implements Analyzer {
    @Override
    public double getTotalPosts(Thread thread) {
        return countAllPosts(thread.getRootPost());
    }

    private int countAllPosts(Post post) {
        int count = 1; // Count current post
        for (Post reply : post.getReplies()) {
            count += countAllPosts(reply);
        }
        return count;
    }

    @Override
    public double getAverageReplies(Thread thread) {
        int totalPosts = (int) getTotalPosts(thread);
        if (totalPosts <= 1) {
            return 0.0;
        }
        return calculateTotalReplies(thread.getRootPost()) / (double) totalPosts;
    }

    private int calculateTotalReplies(Post post) {
        int replies = post.getReplies().size();
        for (Post reply : post.getReplies()) {
            replies += calculateTotalReplies(reply);
        }
        return replies;
    }

    @Override
    public String getAverageInterval(Thread thread) {
        List<Instant> timestamps = new ArrayList<>();
        collectTimestamps(thread.getRootPost(), timestamps);
        
        if (timestamps.size() <= 1) {
            return "00:00:00";
        }

        timestamps.sort(Instant::compareTo);
        long totalSeconds = 0;
        for (int i = 1; i < timestamps.size(); i++) {
            Duration duration = Duration.between(timestamps.get(i-1), timestamps.get(i));
            totalSeconds += duration.getSeconds();
        }

        long averageSeconds = totalSeconds / (timestamps.size() - 1);
        Duration averageDuration = Duration.ofSeconds(averageSeconds);
        
        return String.format("%02d:%02d:%02d",
            averageDuration.toHours(),
            averageDuration.toMinutesPart(),
            averageDuration.toSecondsPart());
    }

    private void collectTimestamps(Post post, List<Instant> timestamps) {
        if (post.getCreatedAt() != null) {
            timestamps.add(Instant.parse(post.getCreatedAt()));
        }
        for (Post reply : post.getReplies()) {
            collectTimestamps(reply, timestamps);
        }
    }
}