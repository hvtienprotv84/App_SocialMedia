    package com.DoAn_Mobile.Adapters;

    import android.media.MediaPlayer;
    import android.net.Uri;
    import android.os.Handler;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.ProgressBar;
    import android.widget.SeekBar;
    import android.widget.TextView;
    import android.widget.VideoView;
    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;
    import com.DoAn_Mobile.Models.VideoInfo;
    import com.DoAn_Mobile.R;
    import java.util.List;

    public class WatchAdapter extends RecyclerView.Adapter<WatchAdapter.WatchViewHolder> {
        private List<VideoInfo> videoList;
        private VideoView videoView;
        private ImageView playPauseIcon;
        ProgressBar loadingProgressBar;
        String videoUrl;
        String videoDescription;
        TextView tvDescription;
        SeekBar seekBar;
        public WatchAdapter(List<VideoInfo> videoList) {
            this.videoList = videoList;
        }
        public void setVideoList(List<VideoInfo> newVideoList) {
            this.videoList = newVideoList;
            notifyDataSetChanged();
        }
        @Override
        public int getItemCount() {
            return videoList.size();
        }
        @NonNull
        @Override
        public WatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watch, parent, false);
            return new WatchViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull WatchViewHolder holder, int position) {
            VideoInfo video = videoList.get(position);
            holder.bind(video);
        }

        public class WatchViewHolder extends RecyclerView.ViewHolder{
            public WatchViewHolder(@NonNull View itemView) {
                super(itemView);
                loadingProgressBar = itemView.findViewById(R.id.loadingProgressBar);
                playPauseIcon = itemView.findViewById(R.id.playPauseIcon);
                videoView = itemView.findViewById(R.id.videoView);
                seekBar = itemView.findViewById(R.id.seekBar);
                tvDescription = itemView.findViewById(R.id.tvDescription);

                setupVideoView();
            }
            public void bind(VideoInfo video) {
                tvDescription.setText(video.getDescription());
                videoView.setVideoURI(Uri.parse(video.getUrl()));
            }
        }
        public void setupVideoView() {
            videoView.setOnPreparedListener(mp -> {
                loadingProgressBar.setVisibility(View.GONE);
                // Set max duration of the seek bar to video duration
                seekBar.setMax(videoView.getDuration());
                // Update seek bar progress periodically
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSeekBar();
                        handler.postDelayed(this, 1000); // Update every 1 second
                    }
                }, 1000); // Initial delay of 1 second
            });
            videoView.setOnCompletionListener(mp -> {
                // Xử lý sự kiện khi video phát xong
            });
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        // Nếu sự kiện được kích hoạt bởi người dùng, cập nhật vị trí của video
                        videoView.seekTo(progress);
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Không cần thực hiện gì khi bắt đầu chạm vào seek bar
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Không cần thực hiện gì khi kết thúc chạm vào seek bar
                }
            });
        }
        private void updateSeekBar() {
            seekBar.setProgress(videoView.getCurrentPosition());
        }
        public void playVideo(int position, VideoInfo video) {
            if (videoView != null) {
                videoView.setVideoURI(Uri.parse(video.getUrl()));
                videoView.start();
            }
        }
        public void restartVideo() {
            if (videoView != null) {
                videoView.seekTo(0);
                videoView.start();
            }
        }
    }
