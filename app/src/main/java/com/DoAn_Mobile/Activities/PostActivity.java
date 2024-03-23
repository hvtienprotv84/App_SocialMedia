package com.DoAn_Mobile.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.DoAn_Mobile.MainActivity;
import com.DoAn_Mobile.Models.Post;
import com.bumptech.glide.Glide;
import com.example.filterbadwords.FilterBadwords;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.DoAn_Mobile.R;
import com.DoAn_Mobile.Authentication.User;
import com.DoAn_Mobile.Fragments.SelectSourceDialogFragment;
import com.DoAn_Mobile.DateFormatter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    ImageView imageView;
    TextView caption;
    Uri filePath = null;
    StorageReference storageReference;
    FirebaseFirestore db;
    FirebaseUser user;
    String currentPhotoPath;

    FilterBadwords filterWords;

    ActivityResultLauncher<Intent> fromGalleryResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                filePath = data.getData();
                showActivity(true);
                Glide.with(this).load(filePath).into(imageView);
            }
        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
            startMainActivity();
        }
    });
    ActivityResultLauncher<Intent> fromCameraResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            showActivity(true);
            Glide.with(this).load(filePath).into(imageView);
        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
            startMainActivity();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        imageView = findViewById(R.id.post_img);
        caption = findViewById(R.id.et_caption);

        ImageView closeButton = findViewById(R.id.btn_close);
        ImageView postButton = findViewById(R.id.btn_post);

        user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        closeButton.setOnClickListener(view -> {
            startMainActivity();
        });
        postButton.setOnClickListener(view -> {
            closeKeyboard();
            publishPost();
        });

        String type = getIntent().getStringExtra("type");

        if (type.equals("picture")) {
            showDialog();
            showActivity(false);
        } else {
            TextInputLayout captionLayout = findViewById(R.id.layout_caption);
            captionLayout.setHint("Enter text content");
            captionLayout.setHint("Nhập nội dung");
        }
    }

    void showDialog() {
        SelectSourceDialogFragment newFragment = SelectSourceDialogFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    private File createImageFile() throws IOException {
        String timeStamp = DateFormatter.getCurrentTime();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                filePath = FileProvider.getUriForFile(this,
                        "com.DoAn_Mobile.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
                fromCameraResultLauncher.launch(Intent.createChooser(takePictureIntent, "Select Picture"));
            }
        }
    }
    public void initializeFilterWords() {
        try {
            filterWords = new FilterBadwords(); // Initialize the FilterWords object
        } catch (IOException e) {
            e.printStackTrace(); // Handle the IOException appropriately
        }
    }
    private void publishPost() {
        String postCaption = caption.getText().toString();
        if (filterWords == null) {
            initializeFilterWords();
        }
        boolean isBad = filterWords.containsBadWords(postCaption);
        if (isBad) {
            Toast.makeText(this, "Từ ngữ không phù hợp", Toast.LENGTH_SHORT).show();
        }else {
            if (filePath != null) {
                ProgressDialog progressDialog = new ProgressDialog(this);
                //progressDialog.setTitle("Uploading...");
                progressDialog.setTitle("Đang tải lên...");
                progressDialog.show();

                String postId = DateFormatter.getCurrentTime();
                StorageReference ref = storageReference.child("Posts/" + postId);

                ref.putFile(filePath).addOnSuccessListener(snapshot -> {
                    //Toast.makeText(this, "Image Uploaded!", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Tải lên thành công!", Toast.LENGTH_SHORT).show();

                    storageReference.child("Posts").child(postId).getDownloadUrl().addOnSuccessListener(uri -> {
                        Post post = new Post(user.getUid(), postId, uri.toString(), caption.getText().toString());
                        DocumentReference postRef = db.collection("Posts").document(postId);

                        postRef.set(post).addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            startMainActivity();
                        });
                        updateUserPostsAndFeed(postRef, false); // Gửi tham số false để chỉ định rằng đây là một bài đăng ảnh
                    });
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    //progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    progressDialog.setMessage("Đang tải lên " + (int) progress + "%");
                });
            } else {

                String postId = DateFormatter.getCurrentTime();
                Post post = new Post(user.getUid(), postId, "", "");
                post.setKitt(postCaption);
                DocumentReference postRef = db.collection("Posts").document(postId);
                postRef.set(post).addOnCompleteListener(task -> {
                    updateUserPostsAndFeed(postRef, true);
                    startMainActivity();
                });
            }
        }


    }

    void updateUserPostsAndFeed(DocumentReference postReference, boolean isTextPost) {
        DocumentReference userReference = db.collection("users").document(user.getUid());
        userReference.update("posts", FieldValue.arrayUnion(postReference));

        Map<String, Object> map = new HashMap<>();
        map.put("postReference", postReference);
        map.put("visited", false);
        if (!isTextPost) {
            map.put("isTextPost", false); // Thêm thuộc tính "isTextPost" với giá trị false để xác định đây là một bài đăng ảnh
        }
        userReference.collection("feed").document(postReference.getId()).set(map);
        userReference.get().addOnSuccessListener(userSnapshot -> {
            User me = userSnapshot.toObject(User.class);
            assert me != null;
            for (DocumentReference follower : me.getFollowers()) {
                follower.collection("feed").document(postReference.getId()).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        follower.collection("feed").document(postReference.getId()).set(map);
                    }
                });
            }
        });
    }

    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void selectPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fromGalleryResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void showActivity(boolean set) {
        if (set) findViewById(R.id.post_parent).setVisibility(View.VISIBLE);
        else findViewById(R.id.post_parent).setVisibility(View.INVISIBLE);
    }

    public void selectFromCamera() {
        takePicture();
    }

    public void selectFromGallery() {
        selectPicture();
    }
}