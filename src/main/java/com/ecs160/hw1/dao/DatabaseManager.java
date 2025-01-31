package com.ecs160.hw1.dao;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class DatabaseManager {
    private final JedisPool jedisPool;

    public DatabaseManager() {
        this.jedisPool = new JedisPool("localhost", 6379);
    }

    public void clearDatabase() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushAll();  // Clear all databases
            System.out.println("Database cleared successfully");
        } catch (Exception e) {
            System.err.println("Error clearing database: " + e.getMessage());
        }
    }

    public void showDatabaseStats() {
        try (Jedis jedis = jedisPool.getResource()) {
            System.out.println("Database Statistics:");
            System.out.println("Total Keys: " + jedis.dbSize());
            System.out.println("Post Counter: " + jedis.get("post:id:counter"));
            System.out.println("Thread Counter: " + jedis.get("thread:id:counter"));
        } catch (Exception e) {
            System.err.println("Error getting database stats: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        DatabaseManager manager = new DatabaseManager();
         manager.clearDatabase();
        
        // if (args.length > 0 && args[0].equals("--clear")) {
        //     manager.clearDatabase();
        // } else {
        //     manager.showDatabaseStats();
        // }
    }
}