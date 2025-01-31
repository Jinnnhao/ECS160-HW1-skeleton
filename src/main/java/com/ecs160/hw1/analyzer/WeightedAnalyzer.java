package com.ecs160.hw1.analyzer;

import com.ecs160.hw1.Analyzer;
import com.ecs160.hw1.Post;
import com.ecs160.hw1.Thread;

public class WeightedAnalyzer implements Analyzer {
    @Override
    public double getTotalPosts(Thread thread) {
        int maxWords = findMaxWords(thread.getRootPost());
        return calculateWeightedTotal(thread.getRootPost(), maxWords);
    }

    @Override
    public double getAverageReplies(Thread thread) {
        int maxWords = findMaxWords(thread.getRootPost());
        double totalWeightedReplies = calculateWeightedReplies(thread.getRootPost(), maxWords);
        int totalPosts = countAllPosts(thread.getRootPost());
        return totalPosts > 0 ? totalWeightedReplies / totalPosts : 0.0;
    }

    @Override
    public String getAverageInterval(Thread thread) {
        // Time interval calculation remains the same as NonWeightedAnalyzer
        return new NonWeightedAnalyzer().getAverageInterval(thread);
    }

    private int findMaxWords(Post post) {
        int maxWords = countWords(post.getText());
        for (Post reply : post.getReplies()) {
            maxWords = Math.max(maxWords, findMaxWords(reply));
        }
        return maxWords;
    }

    private double calculateWeightedTotal(Post post, int maxWords) {
        double weight = calculateWeight(post.getText(), maxWords);
        double total = weight;
        for (Post reply : post.getReplies()) {
            total += calculateWeightedTotal(reply, maxWords);
        }
        return total;
    }

    private double calculateWeightedReplies(Post post, int maxWords) {
        double weightedReplies = 0;
        for (Post reply : post.getReplies()) {
            weightedReplies += calculateWeight(reply.getText(), maxWords);
            weightedReplies += calculateWeightedReplies(reply, maxWords);
        }
        return weightedReplies;
    }

    //  calculate the weight of the post
    private double calculateWeight(String text, int maxWords) {
        if (text == null || maxWords == 0) {
            return 1.0;
        }
        int words = countWords(text);
        return 1.0 + ((double) words / maxWords);
    }

    //  count the words in the text
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    //  count the total number of posts in the thread
    private int countAllPosts(Post post) {
        int count = 1;
        for (Post reply : post.getReplies()) {
            count += countAllPosts(reply);
        }
        return count;
    }
} 