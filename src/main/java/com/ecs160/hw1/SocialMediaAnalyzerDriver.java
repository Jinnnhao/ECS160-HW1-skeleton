package com.ecs160.hw1;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.ecs160.hw1.analyzer.NonWeightedAnalyzer;
import com.ecs160.hw1.analyzer.WeightedAnalyzer;
import com.ecs160.hw1.dao.DatabaseManager;
import com.ecs160.hw1.dao.RedisDAO;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;  

public class SocialMediaAnalyzerDriver {

    public static void main(String[] args) {
        boolean weighted = args.length > 0 && args[0].equalsIgnoreCase("weighted=true");
        RedisDAO redisDAO = null;
        
        try {
            // Try to connect to Redis
            try {
                redisDAO = new RedisDAO();
            } catch (Exception e) {
                System.err.println("Warning: Redis connection failed. Running in memory-only mode.");
                System.err.println("Redis error: " + e.getMessage());
            }

            // Load and parse JSON
            URL resource = SocialMediaAnalyzerDriver.class.getClassLoader().getResource("input.json");
            if (resource == null) {
                throw new FileNotFoundException("input.json not found in resources directory.");
            }

            // Read and parse JSON
            JsonObject root = JsonParser.parseReader(new FileReader(resource.toURI().getPath())).getAsJsonObject();

            if (!root.has("feed")) {
                throw new IllegalStateException("JSON does not contain 'feed' array");
            }

            //  get the feed array
            JsonArray feedArray = root.getAsJsonArray("feed");
            System.out.println("Found " + feedArray.size() + " items in feed");

            //Parse Threadsll
           List<Thread> threads = JsonParserUtil.parseFeed(feedArray);
           System.out.println("Found " + threads.size() + " items in threads");

            // Create appropriate analyzer
            Analyzer analyzer = weighted ? new WeightedAnalyzer() : new NonWeightedAnalyzer();

            // Process threads
            if (redisDAO != null) {
                // Save to Redis and analyze from Redis
                processWithRedis(threads, redisDAO, analyzer);
            } else {
                // Analyze directly from memory
                processInMemory(threads, analyzer);
            }
            //  show the database stats
            DatabaseManager databaseManager = new DatabaseManager();
            databaseManager.showDatabaseStats();
            
            //  clear the database
            // databaseManager.clearDatabase();

        } catch (Exception e) {
            System.err.println("Error processing data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (redisDAO != null) {
                try {
                    redisDAO.close();
                } catch (Exception e) {
                    System.err.println("Error closing Redis connection: " + e.getMessage());
                }
            }
        }
    }

    private static void processWithRedis(List<Thread> threads, RedisDAO redisDAO, Analyzer analyzer) {
        List<String> threadKeys = new ArrayList<>();
        //  save the thread to redis    
        //  create a hash map to store the thread data 
        //  put the thread data into the hash map
        //  return the thread key
        for (Thread thread : threads) {
            try {
                // process the thread and save it to redis with the class method
                String threadKey = redisDAO.saveThread(thread);
                // add the thread key to the list
                threadKeys.add(threadKey);
                System.out.println("Saved thread with key: " + threadKey);
            } catch (Exception e) {
                System.err.println("Failed to save thread to Redis: " + e.getMessage());
            }
        }

        //  get the thread from redis 
        // for (String threadKey : threadKeys) {
        //     try {
        //         //  get the thread from redis with the thread key
        //         Thread thread = redisDAO.getThread(threadKey);
        //         if (thread != null) {
        //             analyzeAndPrintThread(thread, analyzer);
        //         }
        //     } catch (Exception e) {
        //         System.err.println("Failed to analyze thread from Redis: " + e.getMessage());
        //     }
        // }
    }

    private static void processInMemory(List<Thread> threads, Analyzer analyzer) {
        for (Thread thread : threads) {
            analyzeAndPrintThread(thread, analyzer);
        }
    }

    private static void analyzeAndPrintThread(Thread thread, Analyzer analyzer) {
        System.out.println("\nAnalyzing thread:");
        System.out.println("Total Posts: " + analyzer.getTotalPosts(thread));
        System.out.println("Average Replies: " + analyzer.getAverageReplies(thread));
        System.out.println("Average Interval: " + analyzer.getAverageInterval(thread));
    }

   


}
