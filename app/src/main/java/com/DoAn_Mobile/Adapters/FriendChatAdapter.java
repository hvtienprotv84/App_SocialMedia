package com.DoAn_Mobile.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DoAn_Mobile.Models.FriendItem;
import com.DoAn_Mobile.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class FriendChatAdapter extends RecyclerView.Adapter<FriendChatAdapter.FriendViewHolder> {
    private List<FriendItem> FriendItemList = new ArrayList<>(); // Initialize the list to avoid null pointer exception

    // Removed userList as it is not used
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(FriendItem FriendItem);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    public FriendChatAdapter() {
    }

    public void addFriendItem(FriendItem FriendItem) {
        FriendItemList.add(FriendItem);
        notifyItemInserted(FriendItemList.size() - 1); // Better to notify only for the new item
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_chat, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendItem FriendItem = FriendItemList.get(position);
        holder.textViewUserName.setText(FriendItem.getUsername());
        holder.tvMess.setText(FriendItem.getLastMessage());
        holder.tvTime.setText(FriendItem.getTimestamp());
        // Using Glide or a similar library to load the avatar
        Glide.with(holder.imageViewUser.getContext()).load(FriendItem.getAvatarUrl()).into(holder.imageViewUser);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(FriendItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return FriendItemList.size(); // Return the size of FriendItemList instead of userList
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserName, tvMess, tvTime; // Correctly declare all TextViews
        ImageView imageViewUser;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.tvUsername);
            tvMess = itemView.findViewById(R.id.tvMess); // Assign tvMess
            tvTime = itemView.findViewById(R.id.tvTime); // Assign tvTime
            imageViewUser = itemView.findViewById(R.id.imgAvatar);
        }
    }


}

