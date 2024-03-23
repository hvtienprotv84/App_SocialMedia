package com.DoAn_Mobile.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DoAn_Mobile.Activities.EditInfoActivity;
import com.DoAn_Mobile.Activities.FriendActivity;
import com.DoAn_Mobile.Activities.FriendChatActivity;
import com.DoAn_Mobile.Activities.FriendRequestActivity;
import com.DoAn_Mobile.Adapters.MyPostAdapter;
import com.DoAn_Mobile.Authentication.User;
import com.DoAn_Mobile.Models.Post;
import com.DoAn_Mobile.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int EDIT_INFO_REQUEST_CODE = 100;

    private CircleImageView profileImage;
    private TextView tvUsername;
    private TextView tvName;
    private TextView post;
    private TextView tvFollow;
    private EditText edtBio;
    private Button editbutton;
    private FirebaseAuth mAuth;

    private FirebaseFirestore db; // Firestore instance
    private StorageReference storageRef; // Firebase Storage reference
    MyPostAdapter postAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImage = view.findViewById(R.id.profile_image);
        tvName = view.findViewById(R.id.tvName);
        tvUsername = view.findViewById(R.id.tvUsername);
        post = view.findViewById(R.id.post);
        tvFollow = view.findViewById(R.id.follow);
        edtBio = view.findViewById(R.id.edtBio);
        editbutton = view.findViewById(R.id.editbutton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        loadUserDataFromFirestore();

        Button btnFriend = view.findViewById(R.id.btnFriend);
        Button btnFriendRequest = view.findViewById(R.id.btnFriendRequest);

        btnFriendRequest.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FriendRequestActivity.class);
            startActivity(intent);
        });

        btnFriend.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FriendActivity.class);
            startActivity(intent);
        });

        edtBio = view.findViewById(R.id.edtBio);
        Button btnSaveBio = view.findViewById(R.id.btnSaveBio);

        edtBio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnSaveBio.setVisibility(View.VISIBLE);
                } else {
                    btnSaveBio.setVisibility(View.GONE);
                }
            }
        });

        btnSaveBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newBio = edtBio.getText().toString();
                DocumentReference userRef = db.collection("users").document(mAuth.getUid());

                userRef.update("bio", newBio)
                        .addOnSuccessListener(aVoid -> {
                            edtBio.clearFocus();
                            hideKeyboardFrom(getContext(), edtBio);
                        })
                        .addOnFailureListener(e -> {
                        });

                btnSaveBio.setVisibility(View.GONE);
            }
        });

        profileImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openImageChooser();
                return true;
            }
        });

        editbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditInfoActivity.class);
                startActivityForResult(intent, EDIT_INFO_REQUEST_CODE);
            }
        });
        RecyclerView myPostRecyclerView = view.findViewById(R.id.myPost);
        //GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        //myPostRecyclerView.setLayoutManager(gridLayoutManager);
        postAdapter = new MyPostAdapter();
        myPostRecyclerView.setAdapter(postAdapter); // Ensure that you set the adapter to your RecyclerView

        loadUserPosts(); // Call this after initializing the adapter
        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }



    private void loadUserDataFromFirestore() {
        DocumentReference userRef = db.collection("users").document(mAuth.getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String username = documentSnapshot.getString("username");
                // Chuyển đổi giá trị int từ Firestore thành String
                Long statusLong = documentSnapshot.getLong("post");
                String status = statusLong != null ? statusLong.toString() : "0";

                Long followLong = documentSnapshot.getLong("follow");
                String follow = followLong != null ? followLong.toString() : "0";
                String bio = documentSnapshot.getString("bio");
                String imgAvatar = documentSnapshot.getString("profileImageUrl");

                tvName.setText(name);
                tvUsername.setText("@"+username);
                post.setText(status + " Posts");
                tvFollow.setText(follow + " Followers");
                edtBio.setText(bio);

                // Tải và hiển thị ảnh đại diện
                if (imgAvatar != null && !imgAvatar.isEmpty()) {
                    Glide.with(this).load(imgAvatar).into(profileImage);
                }
            }
        }).addOnFailureListener(e -> {
            // Xử lý lỗi nếu có
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri); // Cập nhật UI trước khi tải lên

            uploadImageToFirebase(selectedImageUri); // Gọi hàm tải ảnh lên Firebase
        } else if (requestCode == EDIT_INFO_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            String updatedName = data.getStringExtra("updatedName");
            String updatedGender = data.getStringExtra("updatedGender");
            tvName.setText(updatedName);        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileReference = storageRef.child("uploads/" + mAuth.getUid() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveImageUrlToFirestore(imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        // Handle errors here
                    });
        }
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        DocumentReference userRef = db.collection("users").document(mAuth.getUid());

        userRef.update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    // Thông báo cập nhật thành công
                })
                .addOnFailureListener(e -> {
                    // Handle errors here
                });
    }
    private void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void loadUserPosts() {
        CollectionReference feedCollection = db.collection("users").document(mAuth.getUid()).collection("feed");

        feedCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentReference> postRefs = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                postRefs.add(document.getReference());
            }
            loadPostsFromRefs(postRefs);
        }).addOnFailureListener(e -> {
            // Handle errors here
        });
    }

    private void loadPostsFromRefs(List<DocumentReference> postRefs) {
        List<Post> posts = new ArrayList<>();

        for (DocumentReference postRef : postRefs) {
            String postId = postRef.getId();
            db.collection("Posts").document(postId).get().addOnSuccessListener(postSnapshot -> {
                if (postSnapshot.exists()) {
                    Post post = postSnapshot.toObject(Post.class);
                    // Check if creatorID matches currentID
                    if (post != null && postId.equals(post.getPostid())) {
                        posts.add(post);
                    }
                }

                if (posts.size() == postRefs.size()) {
                    // All posts have been processed, update the adapter
                    postAdapter.setPosts(posts);
                    postAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(e -> {
                // Handle errors here
            });
        }
    }



}
