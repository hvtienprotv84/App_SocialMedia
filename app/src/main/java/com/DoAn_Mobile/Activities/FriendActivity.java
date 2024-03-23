package com.DoAn_Mobile.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.DoAn_Mobile.Adapters.FriendAdapter;
import com.DoAn_Mobile.Authentication.User;
import com.DoAn_Mobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class FriendActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFriends;
    private List<User> friendList;
    private FirebaseFirestore firestore;
    private FriendAdapter friendAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        recyclerViewFriends = findViewById(R.id.rclMatch);
        friendList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        friendAdapter = new FriendAdapter(this, friendList, new FriendAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, User user) {
                // Handle item click, navigate to ActivityProfile with user details
                openUserProfile(user.getId());
            }
        });
        recyclerViewFriends.setAdapter(friendAdapter);

        // Load friends from Firestore
        loadFriends();
    }

    private void loadFriends() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String currentUserId = firebaseAuth.getCurrentUser().getUid();

        // Assuming your Firestore collection is named "users"
        // and each user has a subcollection named "friends"
        firestore.collection("users")
                .document(currentUserId)
                .collection("friends")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String friendUid = document.getId();
                            // Load user details using friendUid from Firestore
                            // For example, you can create a method to load user details based on UID
                            loadUserDetails(friendUid);
                        }
                    }
                });
    }

    private void loadUserDetails(String friendUid) {
        // Load user details from Firestore based on friendUid
        firestore.collection("users")
                .document(friendUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User friend = document.toObject(User.class);
                            friendList.add(friend);

                            // Notify the adapter that the data has changed
                            friendAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void openUserProfile(String friendUid) {
        // Navigate to ActivityProfile with the selected friend's UID
        Intent intent = new Intent(this, OtherUserActivity.class);
        intent.putExtra("user_id", friendUid);
        intent.putExtra("type", "friend");
        startActivity(intent);
    }
}
