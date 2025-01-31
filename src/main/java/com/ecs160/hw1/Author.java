package com.ecs160.hw1;

public class Author {
    private String did; // Decentralized identifier
    private String handle; // Username/handle
    private String displayName; // Display name
    private String avatar; // URL to avatar image

    // Constructor
    public Author(String did, String handle, String displayName, String avatar) {
        this.did = did;
        this.handle = handle;
        this.displayName = displayName;
        this.avatar = avatar;
    }

    // Getters and Setters
    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
