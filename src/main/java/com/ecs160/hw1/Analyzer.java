package com.ecs160.hw1;

public interface Analyzer {
    double getTotalPosts(Thread thread);
    double getAverageReplies(Thread thread);
    String getAverageInterval(Thread thread);
}
