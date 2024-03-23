package com.DoAn_Mobile.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.DoAn_Mobile.Activities.AddvideoActivity;
import com.DoAn_Mobile.Adapters.WatchAdapter;
import com.DoAn_Mobile.Models.VideoInfo;
import com.DoAn_Mobile.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WatchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WatchFragment extends Fragment {
    private List<VideoInfo> videoList;
    private WatchAdapter adapter;
    private ViewPager2 viewPager2;
    private CollectionReference videoRef;
    FirebaseFirestore db;
    Button themvd;
    public WatchFragment() {}
    public static WatchFragment newInstance() {
        WatchFragment fragment = new WatchFragment();
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watch, container, false);
        viewPager2 = view.findViewById(R.id.viewPager2);
        themvd = view.findViewById(R.id.themvd);

        db = FirebaseFirestore.getInstance();
        videoRef = db.collection("videos");
        loadVideoData();

        themvd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddvideoActivity.class);
                startActivity(intent);
            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Tự động phát video khi chuyển sang trang mới
                adapter.playVideo(position, videoList.get(position));
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    // Trạng thái idle tức là ngừng lướt
                    // Kiểm tra nếu đang lướt lên, thì bắt đầu lại video từ đầu
                    if (viewPager2.getCurrentItem() == 0) {
                        adapter.restartVideo();
                    }
                }
            }
        });

        return view;
    }
    private void loadVideoData() {
        videoList = new ArrayList<>();
        videoRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String videoUrl = document.getString("url");
                    String videoDescription = document.getString("description");
                    videoList.add(new VideoInfo(videoUrl, videoDescription));
                }
                adapter = new WatchAdapter(videoList); // Sử dụng constructor chính xác ở đây
                viewPager2.setAdapter(adapter);
            } else {
                Log.e("WatchFragment", "Error fetching videos", task.getException());
            }
        });
    }
}
