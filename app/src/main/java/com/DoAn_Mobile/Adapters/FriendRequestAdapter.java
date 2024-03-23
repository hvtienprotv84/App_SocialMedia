package com.DoAn_Mobile.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DoAn_Mobile.Models.FriendRequest;
import com.DoAn_Mobile.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder> {
    private Context context;
    private List<FriendRequest> friendRequests;
    private OnFriendRequestListener listener;
    private FirebaseFirestore db;

    public interface OnFriendRequestListener {
        void onAcceptClicked(FriendRequest request);
        void onDeclineClicked(FriendRequest request);
    }

    public FriendRequestAdapter(Context context, List<FriendRequest> friendRequests, OnFriendRequestListener listener) {
        this.context = context;
        this.friendRequests = friendRequests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_request, parent, false);
        return new FriendRequestViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        FriendRequest request = friendRequests.get(position);

        db.collection("users").document(request.getFromUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                holder.tvUsername.setText(document.getString("username"));
                                holder.tvName.setText(document.getString("name"));
                                holder.tvUsername.setText(document.getString("username"));
                                String url = document.getString("profileImageUrl");
                                Glide.with(holder.imgAvatar.getContext()).load(url).into(holder.imgAvatar);

                                Map<String, Object> userData = document.getData();
                            } else {
                            }
                        } else {
                        }
                    }
                });

        holder.buttonAccept.setOnClickListener(v -> listener.onAcceptClicked(friendRequests.get(position)));
        holder.buttonDecline.setOnClickListener(v -> listener.onDeclineClicked(friendRequests.get(position)));
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }

    public class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        TextView tvName;
        ImageView imgAvatar;
        Button buttonAccept, buttonDecline;

        public FriendRequestViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvName = itemView.findViewById(R.id.tvName);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            buttonAccept = itemView.findViewById(R.id.button_accept);
            buttonDecline = itemView.findViewById(R.id.button_decline);
            db = FirebaseFirestore.getInstance();


        }

    }
}
