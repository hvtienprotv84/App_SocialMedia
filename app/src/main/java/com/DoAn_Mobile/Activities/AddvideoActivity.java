package com.DoAn_Mobile.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.DoAn_Mobile.Models.VideoInfo;
import com.DoAn_Mobile.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AddvideoActivity extends AppCompatActivity {
    EditText url, noidung;
    Button them, huy;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    CollectionReference videoRef;
    private static final int PICK_VIDEO_REQUEST = 1;
    private Button btnPickVideo;
    private PlayerView videoPreview;
    private SimpleExoPlayer exoPlayer;
    private StorageReference storageRef;
    private boolean isUsingUrl = false;
    private Uri videoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addvideo);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        initView();
        firebaseAuth = FirebaseAuth.getInstance();
        initControll();
        db = FirebaseFirestore.getInstance();
        videoRef = db.collection("videos");

        videoPreview.setVisibility(View.GONE);
        btnPickVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideoPicker();
            }
        });
    }
    private void showVideoPreview() {
        videoPreview.setVisibility(View.VISIBLE);
    }
    private void openVideoPicker() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn video"), PICK_VIDEO_REQUEST);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();// Giải phóng nguồn lực của ExoPlayer khi Activity bị hủy
    }
    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private void initializePlayer(Uri videoUri) {
        // Tạo một đối tượng SimpleExoPlayer
        exoPlayer = new SimpleExoPlayer.Builder(this).build();
        // Liên kết ExoPlayer với PlayerView để hiển thị video
        videoPreview.setPlayer(exoPlayer);
        // Tạo MediaItem từ Uri
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        // Đặt MediaItem cho ExoPlayer
        exoPlayer.setMediaItem(mediaItem);
        // Chuẩn bị ExoPlayer
        exoPlayer.prepare();
        // Bắt đầu phát video
        exoPlayer.setPlayWhenReady(true);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Xử lý video đã chọn
            videoUri = data.getData();
            // Hiển thị videoPreview bằng ExoPlayer
            initializePlayer(videoUri);
            Toast.makeText(this, "Selected Video: " + getFileName(videoUri), Toast.LENGTH_SHORT).show();
            showVideoPreview();
            url.setVisibility(View.GONE);
        }
    }
    private String getFileName(Uri uri) {
        String result = null;
        Cursor cursor = null;
        try {
            if (uri.getScheme().equals("content")) {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex >= 0) {
                        result = cursor.getString(displayNameIndex);
                    } else {
                        Log.e("Error", "Column DISPLAY_NAME not found in the cursor");
                    }
                }
            }
            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        } catch (Exception e) {
            Log.e("Error", "Failed to get file name: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }
    private void initControll() {
        huy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        them.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth != null) {
                    currentUser = firebaseAuth.getCurrentUser();
                }if (currentUser != null) {
                    String userId = currentUser.getUid();
                        String videoUrl = url.getText().toString();
                        if (!videoUrl.isEmpty()) {
                            isUsingUrl = true;
                        } else {
                            isUsingUrl = false;
                        }
                        checkUserChoice(userId);
                }
            }
        });
    }

    private void checkUserChoice(String userId) {
        String videoDescription = noidung.getText().toString();
        if (isUsingUrl) {
            String videoUrl = url.getText().toString();
            addVideoToFirestore(userId, videoUrl, videoDescription, null);
        } else if (videoUri != null) {
            addVideoToFirestore(userId, "", videoDescription, videoUri);
        } else {
            Toast.makeText(this, "Vui lòng chọn một video hoặc nhập URL trước khi thêm", Toast.LENGTH_SHORT).show();
        }
    }
    private void addVideoToFirestore(String userId, String videoUrl, String videoDescription, Uri videoUri) {
        VideoInfo videoInfo = new VideoInfo(userId, videoUrl, videoDescription);
        if (videoUri != null) {
            uploadVideoToStorage(userId, videoInfo, videoUri);
        } else {
            saveVideoToFirestore(userId, videoInfo);
        }
    }
    private void saveVideoToFirestore(String userId, VideoInfo videoInfo) {
        videoRef.add(videoInfo)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddvideoActivity.this, "Thêm video thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddvideoActivity.this, "Thêm video thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void uploadVideoToStorage(String userId, VideoInfo videoInfo, Uri videoUri) {
        String fileName = getFileName(videoUri);
        StorageReference videoStorageRef = storageRef.child("videos/" + fileName);
        // Upload video lên Firebase Storage
        videoStorageRef.putFile(videoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Lấy link tải về của video
                    videoStorageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Lưu link vào đối tượng VideoInfo
                        videoInfo.setUrl(uri.toString());
                        // Thêm dữ liệu lên FirebaseFirestore
                        saveVideoToFirestore(userId, videoInfo);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddvideoActivity.this, "Upload video thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void initView() {
        url = findViewById(R.id.add_url);
        noidung = findViewById(R.id.add_nd);
        them = findViewById(R.id.add_them);
        huy = findViewById(R.id.add_huy);
        btnPickVideo = findViewById(R.id.btn_pick_video);
        videoPreview = findViewById(R.id.video_preview);
        EditText url = findViewById(R.id.add_url);
        Button btnPickVideo = findViewById(R.id.btn_pick_video);
        url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    btnPickVideo.setEnabled(false);
                } else {
                    btnPickVideo.setEnabled(true);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
}