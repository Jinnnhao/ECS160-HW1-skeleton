package com.ecs160.hw1;

public class Thread {

    private Post rootPost;

    public Thread(Post rootPost) {
        this.rootPost = rootPost;
    }

    public Post getRootPost() {
        return rootPost;
    }

    public int getTotalPosts() {
        return 1 + rootPost.getTotalReplies(); // Root post + all replies
    }

    public double getAverageReplies() {
        if (rootPost.getReplies().isEmpty()) { // No replies
            return 0.0;
        }
        int totalPosts = getTotalPosts();
        return calculateTotalRepliesPerPost(rootPost) / (double) totalPosts;
    }

    private double calculateTotalRepliesPerPost(Post post) {
        double total = post.getReplies().size();
        for (Post reply : post.getReplies()) {
            total += calculateTotalRepliesPerPost(reply);
        }
        return total;
    }
}
