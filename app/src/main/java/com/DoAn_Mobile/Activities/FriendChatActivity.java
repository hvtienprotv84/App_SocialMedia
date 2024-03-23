package com.DoAn_Mobile.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.DoAn_Mobile.Adapters.FriendChatAdapter;
import com.DoAn_Mobile.Authentication.User;
import com.DoAn_Mobile.Models.FriendItem;
import com.DoAn_Mobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FriendChatActivity extends AppCompatActivity {
    FriendChatAdapter adapter;
    FirebaseFirestore db;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_chat);

        RecyclerView recyclerView = findViewById(R.id.rclMatch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendChatAdapter();
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter.setOnItemClickListener(friendItem -> {
            // Here you'll handle the click event and navigate to the chat screen
            Intent chatIntent = new Intent(FriendChatActivity.this, ChatActivity.class);
            chatIntent.putExtra("matchedUserId", friendItem.getUserId()); // Now this method should be resolved
            startActivity(chatIntent);
        });

        fetchFriendsAndMessages();




    }

    private void fetchFriendsAndMessages() {
        db.collection("users").document(currentUserId).collection("friends")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot friendDoc : task.getResult()) {
                            String friendId = friendDoc.getId();
                            fetchLastMessageFromConversation(friendId);
                        }
                    } else {
                        Log.e("Firestore", "Error fetching friends: ", task.getException());
                    }
                });
    }

    private void fetchLastMessageFromConversation(String friendID) {
        String conversationId = createConversationId(currentUserId, friendID);

        db.collection("Messages")
                .document(conversationId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<Map<String, Object>> messages = (List<Map<String, Object>>) document.get("messages");
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

                FriendItem friendItem = new FriendItem();
                friendItem.setUserId(otherUserId);
                friendItem.setUsername(otherUser.getName());
                friendItem.setLastMessage(lastMessage);
                friendItem.setTimestamp(formattedTimestamp);
                friendItem.setAvatarUrl(otherUser.getProfileImageUrl());

                // Add friendItem to the adapter and notify the change
                adapter.addFriendItem(friendItem);
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Error fetching user details: ", e));
    }

    private String createConversationId(String uid1, String uid2) {
        // Sort the UIDs to ensure consistency
        List<String> sortedUids = Arrays.asList(uid1, uid2);
        Collections.sort(sortedUids);

        // Combine sorted UIDs to create the unique ID
        return sortedUids.get(0) +"_"+ sortedUids.get(1);
    }
}