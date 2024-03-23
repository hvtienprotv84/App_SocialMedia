package com.DoAn_Mobile.Models;

public class FriendRequest {
    private String requestId;
    private String fromUserId;
    private String toUserId;
    private String status; // Có thể là "sent", "accepted", hoặc "declined"

    // Constructor, getters và setters

    public FriendRequest() {
    }

    public FriendRequest(String requestId, String fromUserId, String toUserId, String status) {
        this.requestId = requestId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.status = status;
    }

    public FriendRequest(String requestId, String fromUserId) {
        this.requestId = requestId;
        this.fromUserId = fromUserId;

    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
