package com.DoAn_Mobile.Models;

import java.util.List;

public class Conversation {
    private List<Message> messages;

    public Conversation() {

    }

    public Conversation(List<Message> messages) {
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
