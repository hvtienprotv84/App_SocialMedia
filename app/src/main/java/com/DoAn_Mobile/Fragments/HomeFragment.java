package com.DoAn_Mobile.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.DoAn_Mobile.Activities.FriendChatActivity;
import com.DoAn_Mobile.Activities.SearchActivity;
import com.DoAn_Mobile.Models.Post;
import com.DoAn_Mobile.Activities.PostActivity;
import com.DoAn_Mobile.Adapters.ChangePasswordActivity;
import com.DoAn_Mobile.Adapters.HomeAdapter;
import com.DoAn_Mobile.Adapters.PostAdapter;
import com.DoAn_Mobile.Authentication.LoginActivity;
import com.DoAn_Mobile.Authentication.User;
import com.DoAn_Mobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class HomeFragment extends Fragment {

    RecyclerView recyclerViewPosts;
    PostAdapter postAdapter;
    Toolbar toolbar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerViewPosts = view.findViewById(R.id.recyclerview_posts);

        toolbar = view.findViewById(R.id.top_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.nav_post_image:
                    intent = new Intent(requireActivity(), PostActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("type", "picture");
                    startActivity(intent);
                    break;
                case R.id.nav_post_text:
                    intent = new Intent(requireActivity(), PostActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("type", "text");
                    startActivity(intent);
                    break;
                case R.id.nav_find:
                    intent = new Intent(requireActivity(), SearchActivity.class);
                    startActivity(intent);
                    break;
                case R.id.nav_chat:
                    intent = new Intent(requireActivity(), FriendChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
                case R.id.nav_change_password:
                    intent = new Intent(requireActivity(), ChangePasswordActivity.class);
                    startActivity(intent);
                    break;
                case R.id.action_logout:
                    FirebaseAuth.getInstance().signOut();
                    Intent logoutIntent = new Intent(requireActivity(), LoginActivity.class);
                    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(logoutIntent);
                    break;

            }
            return true;
        });

        postAdapter = new PostAdapter(requireContext());
        recyclerViewPosts.setHasFixedSize(true);
        recyclerViewPosts.setAdapter(postAdapter);
        readPosts();
        return view;
    }


    void readPosts() {
        CollectionReference feedReference = FirebaseFirestore.getInstance().collection("Posts"); // Thay đổi đường dẫn đến bài viết của tất cả người dùng

        feedReference.get().addOnSuccessListener(feedSnapshots -> {
            if (feedSnapshots.isEmpty()) {
                return;
            }
            postAdapter.clearPosts();
            for (DocumentSnapshot feedSnapshot : feedSnapshots) {
                DocumentReference postReference = feedSnapshot.getReference();
                boolean isVisited = Boolean.TRUE.equals(feedSnapshot.getBoolean("visited"));
                postReference.get().addOnSuccessListener(postSnapshot -> postAdapter.addPost(postSnapshot.toObject(Post.class)));
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore Error", e.getMessage());
        });
    }

}