package com.ecs160.hw1.dao;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import com.ecs160.hw1.Post;
import com.ecs160.hw1.Thread;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.ecs160.hw1.Author;

public class RedisDAO implements AutoCloseable {
    private final JedisPool jedisPool;
    private final Gson gson;
    private static final String POST_KEY_PREFIX = "post ID:";
    private static final String POST_ID_COUNTER = "post:id:counter";
    private static final String THREAD_KEY_PREFIX = "thread ID:";
    private static final String THREAD_ID_COUNTER = "thread:id:counter";

    public RedisDAO() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // Increase pool size and timeout settings
        poolConfig.setMaxTotal(50);  // Increased from 10
        poolConfig.setMaxIdle(10);   // Increased from 5
        poolConfig.setMinIdle(5);    // Increased from 1
        poolConfig.setTestOnBorrow(true);
        
        try {
            this.jedisPool = new JedisPool(poolConfig, "localhost", 6379, 30000); // Increased timeout to 30 seconds
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to Redis. Make sure Redis server is running on localhost:6379", e);
        }
        
        this.gson = new Gson();
    }

    private long getNextId(String counterKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(counterKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get next ID from Redis", e);
        }
    }
    
    //  save the post to redis 
    //  create a hash map to store the post data 
    //  put the post data into the hash map
    //  return the post key
    public String savePost(Post post) {
        if (post == null) {
            return null;
        }

        long postId = getNextId(POST_ID_COUNTER);
        String postKey = POST_KEY_PREFIX + postId;

        try (Jedis jedis = jedisPool.getResource()) {
            // Create a HashMap for batch operation
            Map<String, String> postData = new HashMap<>();
            postData.put("uri", post.getUri() != null ? post.getUri() : "");
            postData.put("cid", post.getCid() != null ? post.getCid() : "");
            postData.put("text", post.getText() != null ? post.getText() : "");
            postData.put("createdAt", post.getCreatedAt() != null ? post.getCreatedAt() : "");
            postData.put("author", gson.toJson(post.getAuthor()));

            // Process replies first
            List<String> replyIds = new ArrayList<>();
            if (post.getReplies() != null) {
                for (Post reply : post.getReplies()) {
                    try {
                        String replyId = savePost(reply);
                        if (replyId != null) {
                            replyIds.add(replyId);
                        }
                    } catch (Exception e) {
                        System.err.println("Error saving reply: " + e.getMessage());
                    }
                }
            }
            postData.put("replies", gson.toJson(replyIds));

            // Save all post data in one operation
            jedis.hmset(postKey, postData);
            System.out.println("Saved post: " + postKey);
            return postKey;
        } catch (Exception e) {
            System.err.println("Error saving post: " + e.getMessage());
            return null;
        }
    }
    
    //  save the thread to redis 
    //  create a hash map to store the thread data 
    //  put the thread data into the hash map
    //  return the thread key
    public String saveThread(Thread thread) {
        if (thread == null || thread.getRootPost() == null) {
            return null;
        }

        try {
            long threadId = getNextId(THREAD_ID_COUNTER);
            String threadKey = THREAD_KEY_PREFIX + threadId;
            
            // Save the root post first
            String rootPostKey = savePost(thread.getRootPost());
            if (rootPostKey == null) {
                return null;
            }

            try (Jedis jedis = jedisPool.getResource()) {
                Map<String, String> threadData = new HashMap<>();
                threadData.put("rootPostKey", rootPostKey);
                threadData.put("createdAt", thread.getRootPost().getCreatedAt() != null ? 
                             thread.getRootPost().getCreatedAt() : "");

                // Save thread data in one operation
                jedis.hmset(threadKey, threadData);
                System.out.println("Saved thread: " + threadKey);
                return threadKey;
            }
        } catch (Exception e) {
            System.err.println("Error saving thread: " + e.getMessage());
            return null;
        }
    }
    //  get the post from redis 
    //  create a hash map to store the post data 
    //  put the post data into the hash map
    //  return the post
    public Post getPost(String postKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> postData = jedis.hgetAll(postKey);
            if (postData.isEmpty()) {
                return null;
            }

            Author author = gson.fromJson(postData.get("author"), Author.class);
            Post post = new Post(
                postData.get("uri"),
                postData.get("cid"),
                postData.get("text"),
                postData.get("createdAt"),
                author
            );

            // Get and process replies
            String repliesJson = postData.get("replies");
            if (repliesJson != null) {
                List<String> replyIds = gson.fromJson(repliesJson, List.class);
                for (String replyId : replyIds) {
                    try {
                        Post reply = getPost(replyId);
                        if (reply != null) {
                            post.addReply(reply);
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading reply: " + e.getMessage());
                    }
                }
            }

            return post;
        } catch (Exception e) {
            System.err.println("Error loading post: " + e.getMessage());
            return null;
        }
    }

    public Thread getThread(String threadKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            
            //  get the thread from redis with the thread key
            Map<String, String> threadData = jedis.hgetAll(threadKey);
            if (threadData.isEmpty()) {
                return null;
            }
            
            //  get the root post key from the thread data
            String rootPostKey = threadData.get("rootPostKey");
            if (rootPostKey == null) {
                return null;
            }

            //  get the root post from the root post key
            Post rootPost = getPost(rootPostKey);

            //  return the thread with the root post
            return rootPost != null ? new Thread(rootPost) : null;

        } catch (Exception e) {
            System.err.println("Error loading thread: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }
}