package com.DoAn_Mobile.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DoAn_Mobile.Models.Model;
import com.DoAn_Mobile.R;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {

    ArrayList<Model> homeList = new ArrayList<>();

    public HomeAdapter(ArrayList<Model> homeList) {
        this.homeList = homeList;
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_layout,parent,false);
        return new HomeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, int position) {

        holder.image.setImageResource(homeList.get(position).image);
        holder.textView1.setText(homeList.get(position).content);
        holder.textView2.setText(homeList.get(position).desc);
    }

    @Override
    public int getItemCount() {
        return homeList.size();
    }

    public class HomeHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView textView1,textView2;

        public HomeHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.circularImage);
            textView1 = itemView.findViewById(R.id.textView1);
            textView2 = itemView.findViewById(R.id.textView2);
        }
    }
}
