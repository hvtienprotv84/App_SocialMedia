package com.DoAn_Mobile.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.DoAn_Mobile.Adapters.ChatAdapter;
import com.DoAn_Mobile.Models.Conversation;
import com.DoAn_Mobile.Models.Message;
import com.DoAn_Mobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private Button buttonSend;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private FirebaseFirestore db;

    private String currentUserId, matchedUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        matchedUserId = getIntent().getStringExtra("matchedUserId");

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, currentUserId);
        recyclerViewChat.setAdapter(chatAdapter);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));


        buttonSend.setOnClickListener(v -> {
            String messageText = editTextMessage.getText().toString();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
                editTextMessage.setText("");
            }
        });

        loadMessages();
    }

    private void loadMessages() {
        String conversationId = getConversationId(currentUserId, matchedUserId);

        db.collection("Messages").document(conversationId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("ChatActivity", "Listen failed.", e);
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            Conversation conversation = snapshot.toObject(Conversation.class);
                            List<Message> newMessages = conversation.getMessages();
                            messageList.clear();
                            for (Message message : newMessages) {
                                if (!message.getMessage().equals("Chúc mừng! Bạn đã được ghép đôi.") || !message.getMessage().equals("Hãy bắt đầu cuộc trò chuyện mới!.")) {
                                    messageList.add(message);
                                }
                            }
                            chatAdapter.notifyDataSetChanged();
                            if (!messageList.isEmpty()) {
                                recyclerViewChat.scrollToPosition(messageList.size() - 1);
                            }
                        }
                    }
                });
    }






    private void sendMessage(String messageText) {
        String conversationId = getConversationId(currentUserId, matchedUserId);
        Message newMessage = new Message(currentUserId, matchedUserId, messageText, new Date());

        // Đối tượng để tạo hoặc cập nhật
        Map<String, Object> conversationUpdate = new HashMap<>();
        conversationUpdate.put("messages", FieldValue.arrayUnion(newMessage));

        // Lấy hoặc tạo document mới cho cuộc hội thoại và cập nhật tin nhắn
        db.collection("Messages").document(conversationId)
                .set(conversationUpdate, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("ChatActivity", "Message sent successfully");
                })
                .addOnFailureListener(e -> Log.w("ChatActivity", "Error sending message", e));
    }



    private String getConversationId(String userId1, String userId2) {
        // Sắp xếp userId1 và userId2 theo thứ tự bảng chữ cái và nối chúng lại với nhau
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

}