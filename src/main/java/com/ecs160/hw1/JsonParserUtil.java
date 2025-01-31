package com.ecs160.hw1;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonParserUtil {
    public static int num = 0;

    public static Post parsePost(JsonObject postObject) {
        try {
            // Extract post details
            String uri = postObject.has("uri") ? postObject.get("uri").getAsString() : null;
            String cid = postObject.has("cid") ? postObject.get("cid").getAsString() : null;
            
            JsonObject record = postObject.has("record") ? postObject.getAsJsonObject("record") : null;
            String text = record != null && record.has("text") ? record.get("text").getAsString() : null;
            String createdAt = record != null && record.has("createdAt") ? record.get("createdAt").getAsString() : null;

            // Parse author
            Author author = null;
            if (postObject.has("author")) {
                author = parseAuthor(postObject.getAsJsonObject("author"));
            } else {
                author = new Author("unknown", "anonymous", "Anonymous", "no-avatar");
            }

            return new Post(uri, cid, text, createdAt, author);
        } catch (Exception e) {
            System.err.println("Error parsing post: " + e.getMessage());
            return null;
        }
    }

    //  parseReplies to handle nested replies
    //  parseReplies is a helper method that takes in a JsonArray of replies and a Post object
    //  and parses the replies into a list of Post objects
    //  it then adds the replies to the parentPost and recursively parses any nested replies
    private static void parseReplies(JsonArray repliesArray, Post parentPost) {
        for (JsonElement replyElement : repliesArray) {
            try {
                JsonObject replyThreadView = replyElement.getAsJsonObject();
                if (replyThreadView.has("post")) {
                    JsonObject replyPost = replyThreadView.getAsJsonObject("post");
                    Post reply = parsePost(replyPost);
                    if (reply != null) {
                        parentPost.addReply(reply);
                        
                        // Recursively parse nested replies
                        if (replyThreadView.has("replies")) {
                            parseReplies(replyThreadView.getAsJsonArray("replies"), reply);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing reply: " + e.getMessage());
            }
        }
    }

    private static Author parseAuthor(JsonObject authorObject) {
        try {
            if (authorObject == null) {
                return new Author("unknown", "anonymous", "Anonymous", "no-avatar");
            }

            String did = authorObject.has("did") ? authorObject.get("did").getAsString() : "unknown";
            String handle = authorObject.has("handle") ? authorObject.get("handle").getAsString() : "anonymous";
            String displayName = authorObject.has("displayName") ? authorObject.get("displayName").getAsString() : "Anonymous";
            String avatar = authorObject.has("avatar") ? authorObject.get("avatar").getAsString() : "no-avatar";

            return new Author(did, handle, displayName, avatar);
        } catch (Exception e) {
            System.err.println("Error parsing author: " + e.getMessage());
            return new Author("unknown", "anonymous", "Anonymous", "no-avatar");
        }
    }

    public static List<Thread> parseFeed(JsonArray feedArray) {
        List<Thread> threads = new ArrayList<>();
        if (feedArray == null) {
            System.err.println("Error: Feed array is null");
            return threads;
        }

        System.out.println("Processing " + feedArray.size() + " feed items...");

        for (JsonElement feedElement : feedArray) {
            try {
                JsonObject feedObject = feedElement.getAsJsonObject();
                
                //  check if the feed object has a thread
                if (!feedObject.has("thread")) {
                    System.err.println("Skipping feed item: Missing 'thread' key.");
                    continue;
                }
                
                // Each feed item has a thread object 
                // And the thread object has only one root post object
                JsonObject threadObject = feedObject.getAsJsonObject("thread");
                if (!threadObject.has("post")) {
                    System.err.println("Skipping thread: Missing 'post' key.");
                    continue;
                }

                //  parsePost to parse the root post
                JsonObject postObject = threadObject.getAsJsonObject("post");
                Post rootPost = parsePost(postObject);
                
                // In the thread object, there is a replies array that contains all the replies to the root post
                // Parse replies at the thread level
                if (threadObject.has("replies")) {
                    parseReplies(threadObject.getAsJsonArray("replies"), rootPost);
                }

                if (rootPost != null) {
                    threads.add(new Thread(rootPost));
                }
            } catch (Exception e) {
                System.err.println("Error processing feed item: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Successfully parsed " + threads.size() + " threads");
        return threads;
    }
}
