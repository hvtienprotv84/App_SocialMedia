package com.DoAn_Mobile.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.DoAn_Mobile.Adapters.MatchAdapter;
import com.DoAn_Mobile.Authentication.User;
import com.DoAn_Mobile.Models.MatchItem;
import com.DoAn_Mobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MatchActivity extends AppCompatActivity {
    MatchAdapter adapter;
    FirebaseFirestore db;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        RecyclerView recyclerView = findViewById(R.id.rclMatch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MatchAdapter();
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter.setOnItemClickListener(matchItem -> {
            // Here you'll handle the click event and navigate to the chat screen
            Intent chatIntent = new Intent(MatchActivity.this, ChatActivity.class);
            chatIntent.putExtra("matchedUserId", matchItem.getUserId()); // Now this method should be resolved
            startActivity(chatIntent);
        });


        db.collection("Matches")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot matchDocument : task.getResult()) {
                            // Lấy giá trị của conversationId từ document
                            String conversationId = matchDocument.getString("conversationId");
                            if (conversationId != null) {
                                // Bây giờ bạn có conversationId, tiếp tục truy vấn tin nhắn cuối cùng
                                fetchLastMessageFromConversation(conversationId);
                            } else {
                                // Không tìm thấy conversationId trong document này
                                Log.d("Firestore", "No conversationId found in Match document");
                            }
                        }
                    } else {
                        Log.e("Firestore", "Error getting matches: ", task.getException());
                    }
                });



    }
    private void fetchLastMessageFromConversation(String conversationId) {
        db.collection("Messages").document(conversationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> messages = (List<Map<String, Object>>) documentSnapshot.get("messages");
                        if (messages != null && !messages.isEmpty()) {
                            // Giả sử tin nhắn cuối cùng là tin nhắn mới nhất trong mảng
                            Map<String, Object> lastMessageData = messages.get(messages.size() - 1);
                            String lastMessage = (String) lastMessageData.get("message");
                            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) lastMessageData.get("timestamp");
                            Date date = timestamp.toDate(); // Chuyển đổi Timestamp thành Date
                            String senderId = (String) lastMessageData.get("senderId");
                            String receiverId = (String) lastMessageData.get("receiverId");

                            // Xác định người dùng khác không phải là người dùng hiện tại
                            String otherUserId = !currentUserId.equals(senderId) ? senderId : receiverId;

                            // Tiếp tục để lấy thông tin người dùng và cập nhật UI
                            displayConversationInfo(otherUserId, lastMessage, date);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching messages: ", e));
    }



    private void displayConversationInfo(String otherUserId, String lastMessage, Date date) {
        // Lấy thông tin người dùng từ 'users' collection và cập nhật UI
        db.collection("users").document(otherUserId).get().addOnSuccessListener(userDocument -> {
            if (userDocument.exists()) {
                User otherUser = userDocument.toObject(User.class);
                String formattedTimestamp = DateFormat.getDateTimeInstance().format(date);

                MatchItem matchItem = new MatchItem();
                matchItem.setUserId(otherUserId);
                matchItem.setUsername(otherUser.getName());
                matchItem.setLastMessage(lastMessage);
                matchItem.setTimestamp(formattedTimestamp);
                matchItem.setAvatarUrl(otherUser.getProfileImageUrl());

                // Add matchItem to the adapter and notify the change
                adapter.addMatchItem(matchItem);
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Error fetching user details: ", e));
    }



}