package com.DoAn_Mobile.Adapters;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.DoAn_Mobile.Authentication.User;
import com.DoAn_Mobile.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class FindAdapter extends RecyclerView.Adapter<FindAdapter.FindViewHolder> {

    List<User> userList;
    private double currentUserLatitude;
    private double currentUserLongitude;
    private UserInteractionListener listener;
    public interface UserInteractionListener {
        void onLikeClicked(User user);
        void onDislikeClicked(User user);
    }

    public FindAdapter(List<User> userList, double currentUserLatitude, double currentUserLongitude, UserInteractionListener listener) {
        this.userList = userList;
        this.currentUserLatitude = currentUserLatitude;
        this.currentUserLongitude = currentUserLongitude;
        this.listener = listener;
    }
    public List<User> getUserList() {
        return userList;
    }
    @NonNull
    @Override
    public FindViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_find, parent,false);
        return new FindViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindViewHolder holder, int position) {
        User user = userList.get(position);
        // Tính toán và hiển thị khoảng cách
        if (user.getLocation() != null) {
            int distance = calculateDistance(
                    currentUserLatitude,
                    currentUserLongitude,
                    user.getLocation().getLatitude(),
                    user.getLocation().getLongitude()
            );
            holder.tvUsername.setText(user.getName());
            holder.tvDescription.setText(user.getDescription());
            holder.tvLocation.setText(distance + " km");
            Glide.with(holder.image)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .fitCenter()
                    .into(holder.image);

        }

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

    public class FindViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView tvUsername,tvDescription, tvLocation;
        Button btnLike, btnDislike;

        public FindViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_user);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnDislike = itemView.findViewById(R.id.btnDislike);

            btnLike.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onLikeClicked(userList.get(position));
                }
            });

            btnDislike.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDislikeClicked(userList.get(position));
                }
            });

        }

    }

    private int calculateDistance(double lat1, double long1, double lat2, double long2) {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(long1);

        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(long2);

        float distanceInMeters = location1.distanceTo(location2);
        int distanceInKilometers = Math.round(distanceInMeters / 1000);
        return distanceInKilometers;
    }


}