package com.DoAn_Mobile.Authentication;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class User {
    String id;
    String name;
    String username;
    String email;
    String bio;
    int post;
    int follow;
    String profileImageUrl;
    String onesignalPlayerId;
    Boolean active;
    String gender;

    List<DocumentReference> posts;
    List<DocumentReference> saved;
    List<DocumentReference> following;
    List<DocumentReference> followers;
    List<DocumentReference> blockedAccounts;

    private Location location;
    String description;
    public User() {

    }
    public User(String id, String email, String profileImageUrl, String gender, Boolean active) {
        this.id = id;
        this.name = "";
        this.username ="";
        this.email = email;
        this.onesignalPlayerId = "";
        this.profileImageUrl = profileImageUrl;
        this.bio = "123";
        this.active = active;

        this.post = 0;
        this.follow = 0;
        this.posts = new ArrayList<>();
        this.saved = new ArrayList<>();
        this.following = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.blockedAccounts = new ArrayList<>();
        this.gender = gender;


    }


    public List<DocumentReference> getFollowing() {
        return following;
    }

    public List<DocumentReference> getFollowers() {
        return followers;
    }

    public List<DocumentReference> getBlockedAccounts() {
        return blockedAccounts;
    }

    public List<DocumentReference> getSaved() {
        return saved;
    }

    public List<DocumentReference> getPosts() {
        return posts;
    }

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public String getBio() {
        return bio;
    }
    public String getEmail() {
        return email;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        active = active;
    }



    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPosts(List<DocumentReference> posts) {
        this.posts = posts;
    }

    public void setSaved(List<DocumentReference> saved) {
        this.saved = saved;
    }

    public void setFollowing(List<DocumentReference> following) {
        this.following = following;
    }

    public void setFollowers(List<DocumentReference> followers) {
        this.followers = followers;
    }

    public void setBlockedAccounts(List<DocumentReference> blockedAccounts) {
        this.blockedAccounts = blockedAccounts;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}