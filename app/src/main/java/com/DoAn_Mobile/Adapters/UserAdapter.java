package com.DoAn_Mobile.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DoAn_Mobile.Activities.OtherUserActivity;
import com.DoAn_Mobile.Authentication.User;
import com.DoAn_Mobile.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private Context context;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvUsername.setText(user.getUsername());
        holder.tvName.setText(user.getName());
        Glide.with(holder.imgAvatar.getContext()).load(user.getProfileImageUrl()).into(holder.imgAvatar);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OtherUserActivity.class);
            intent.putExtra("user_id", user.getId());
            intent.putExtra("type", "strange");
            context.startActivity(intent);
        });

    }
    public void removeUserAt(int position) {
        if (position >= 0 && position < userList.size()) {
            userList.remove(position);
        }
    }
    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        ImageView imgAvatar;
        TextView tvName;

        public UserViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvName = itemView.findViewById(R.id.tvName);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}
