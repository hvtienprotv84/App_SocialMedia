package com.DoAn_Mobile.Activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.DoAn_Mobile.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherUserActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView tvUsername, tvName, tvPosts, tvFollow;
    private TextView tvBio;
    private Button btnAdd;

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String otherUserId, type;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        profileImage = findViewById(R.id.profile_image);
        tvName = findViewById(R.id.tvName);
        tvUsername = findViewById(R.id.tvUsername);
        tvPosts = findViewById(R.id.post);
        tvFollow = findViewById(R.id.follow);
        tvBio = findViewById(R.id.tvBio);
        btnAdd = findViewById(R.id.btnAdd);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        otherUserId = getIntent().getStringExtra("user_id");
        type = getIntent().getStringExtra("type");
        if (otherUserId != null) {
            loadUserDataFromFirestore();
        }

        if (type.equals("strange")) {
            Button btnAddFriend = findViewById(R.id.btnAdd);

            // Kiểm tra xem người dùng hiện tại đã gửi lời mời kết bạn chưa
            checkSentFriendRequest();

            // Thiết lập sự kiện click cho nút btnAdd
            btnAddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequest();
                }
            });
        } else if (type.equals("friend")) {
            Button btnAddFriend = findViewById(R.id.btnAdd);
            btnAddFriend.setText("Huỷ kết bạn");
            btnAddFriend.setTextColor(Color.RED);

            // Thiết lập sự kiện click cho nút "Huỷ kết bạn"
            btnAddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelFriendRequest();
                }
            });
        }
    }

    private void checkSentFriendRequest() {
        db.collection("friend_requests")
                .document(currentUserId)
                .collection("to") // Collection chứa các lời mời được gửi đi
                .document(otherUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String status = document.getString("status");
                                if (status != null) {
                                    if (status.equals("pending")) {
                                        // Người dùng đã gửi lời mời kết bạn và đang chờ phản hồi
                                        btnAdd.setText("Request Sent");
                                        btnAdd.setEnabled(false);
                                    } else if (status.equals("denied")) {
                                        // Người dùng đã từ chối lời mời kết bạn trước đó
                                        btnAdd.setText("Add");
                                        btnAdd.setEnabled(true);
                                    }
                                }
                            } else {
                                // Người dùng chưa gửi lời mời kết bạn
                                // Đặt nút "Add" làm mặc định
                                btnAdd.setText("Add");
                                btnAdd.setEnabled(true);
                            }
                        } else {
                            // Xử lý lỗi nếu có
                        }
                    }
                });
    }

    private void loadUserDataFromFirestore() {
        DocumentReference userRef = db.collection("users").document(otherUserId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String username = documentSnapshot.getString("username");
                Long postsLong = documentSnapshot.getLong("post");
                String posts = postsLong != null ? postsLong.toString() : "0";
                Long followLong = documentSnapshot.getLong("follow");
                String follow = followLong != null ? followLong.toString() : "0";
                String bio = documentSnapshot.getString("bio");
                String imgAvatar = documentSnapshot.getString("profileImageUrl");

                tvName.setText(name);
                tvUsername.setText("@" + username);
                tvPosts.setText(posts + " Posts");
                tvFollow.setText(follow + " Followers");
                tvBio.setText(bio);

                if (imgAvatar != null && !imgAvatar.isEmpty()) {
                    Glide.with(this).load(imgAvatar).into(profileImage);
                }

                if (type.equals("strange")) {
                    Button btnAddFriend = findViewById(R.id.btnAdd);
                    btnAddFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendFriendRequest();
                        }
                    });
                }
            }
        }).addOnFailureListener(e -> {
            // Xử lý lỗi nếu có
        });
    }

    private void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void sendFriendRequest() {
        // Lấy uid của người dùng hiện tại
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> friendRequest = new HashMap<>();
        friendRequest.put("from", currentUserId);
        friendRequest.put("to", otherUserId);
        friendRequest.put("status", "pending"); // Mặc định là yêu cầu đang chờ

        // Lưu yêu cầu kết bạn vào Firestore
        db.collection("friend_requests")
                .document(currentUserId + "_" + otherUserId) // Tạo một key duy nhất cho mỗi yêu cầu
                .set(friendRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(OtherUserActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                        // Cập nhật giao diện, ví dụ đổi nút 'Thêm bạn' thành 'Yêu cầu đã gửi'
                        btnAdd.setText("Request Sent");
                        btnAdd.setEnabled(false);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OtherUserActivity.this, "Failed to send friend request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cancelFriendRequest() {
        // Lấy uid của người dùng hiện tại
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Xóa document trong collection 'friend_requests'
        db.collection("friend_requests")
                .document(currentUserId)
                .collection("to") // Collection chứa các lời mời được gửi đi
                .document(otherUserId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(OtherUserActivity.this, "Friend request canceled", Toast.LENGTH_SHORT).show();
                        // Cập nhật giao diện, ví dụ đổi nút 'Huỷ kết bạn' thành 'Thêm bạn'
                        btnAdd.setText("Add");
                        btnAdd.setTextColor(Color.BLACK);
                        btnAdd.setEnabled(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OtherUserActivity.this, "Failed to cancel friend request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
