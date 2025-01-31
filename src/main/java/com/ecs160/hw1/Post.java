package com.ecs160.hw1;
import java.util.ArrayList;
import java.util.List;
import com.ecs160.hw1.Author;

public class Post {
    private String uri; // Unique identifier for the post
    private String cid; // Content ID
    private String text; // Content of the post
    private String createdAt; // Timestamp for when the post was created
    private Author author; // Author details
    private List<Post> replies; // List of replies to this post

    // Constructor
    public Post(String uri, String cid, String text, String createdAt, Author author) {
        this.uri = uri;
        this.cid = cid;
        this.text = text;
        this.createdAt = createdAt;
        this.author = author;
        this.replies = new ArrayList<>();
    }

    // Add a reply
    public void addReply(Post reply) {
        if (reply != null) {
            replies.add(reply);
        }
    }

    // Getters and Setters
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public List<Post> getReplies() {
        return replies;
    }

    public int getTotalReplies() {
        int total = replies.size();
        for (Post reply : replies) {
            total += reply.getTotalReplies();
        }
        return total;
    }
}
