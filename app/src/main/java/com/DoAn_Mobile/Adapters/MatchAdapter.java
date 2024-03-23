package com.DoAn_Mobile.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DoAn_Mobile.Models.MatchItem;
import com.DoAn_Mobile.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
    private List<MatchItem> matchItemList = new ArrayList<>(); // Initialize the list to avoid null pointer exception

    // Removed userList as it is not used
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(MatchItem matchItem);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    public MatchAdapter() {
    }

    public void addMatchItem(MatchItem matchItem) {
        matchItemList.add(matchItem);
        notifyItemInserted(matchItemList.size() - 1); // Better to notify only for the new item
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchItem matchItem = matchItemList.get(position);
        holder.textViewUserName.setText(matchItem.getUsername());
        holder.tvMess.setText(matchItem.getLastMessage());
        holder.tvTime.setText(matchItem.getTimestamp());
        // Using Glide or a similar library to load the avatar
        Glide.with(holder.imageViewUser.getContext()).load(matchItem.getAvatarUrl()).into(holder.imageViewUser);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(matchItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return matchItemList.size(); // Return the size of matchItemList instead of userList
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserName, tvMess, tvTime; // Correctly declare all TextViews
        ImageView imageViewUser;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.tvUsername);
            tvMess = itemView.findViewById(R.id.tvMess); // Assign tvMess
            tvTime = itemView.findViewById(R.id.tvTime); // Assign tvTime
            imageViewUser = itemView.findViewById(R.id.imgAvatar);
        }
    }


}
