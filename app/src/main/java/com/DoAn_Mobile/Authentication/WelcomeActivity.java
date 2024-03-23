package com.DoAn_Mobile.Authentication;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.DoAn_Mobile.MainActivity;
import com.DoAn_Mobile.R;
import com.DoAn_Mobile.databinding.ActivityWelcomeBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Firestore instance
    private ActivityWelcomeBinding binding;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        int imageResourceId = R.drawable.img_default; // ID của tài nguyên drawable
        String packageName = getApplicationContext().getPackageName(); // Tên package của ứng dụng
        imageUri = Uri.parse("android.resource://" + packageName + "/" + imageResourceId);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.genderSpinner.setAdapter(adapter);


        loadUserDataFromFirestore();

        binding.btnChangeAVT.setOnClickListener(v -> openFileChooser());

        binding.btnNext.setOnClickListener(v -> {

            if (isUserDataValid()) {
                updateUserInfo();
            }
        });
    }
    private void loadUserDataFromFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String username = documentSnapshot.getString("name");
                String gender = documentSnapshot.getString("gender");

                binding.username.setText(username);

                if (gender != null) {
                    int genderPosition = getGenderPosition(gender);
                    if (genderPosition != -1) {
                        binding.genderSpinner.setSelection(genderPosition);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            // Handle errors
        });
    }
    private int getGenderPosition(String gender) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_array,
                android.R.layout.simple_spinner_item
        );
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            if (adapter.getItem(i).toString().equals(gender)) {
                return i;
            }
        }
        return -1;
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            binding.imgAvatar.setImageURI(imageUri);
        }
    }

    private void updateUserInfo() {
        String uid = mAuth.getCurrentUser().getUid();
        DocumentReference usersRef = db.collection("users").document(uid);

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", binding.name.getText().toString());
        userUpdates.put("username", binding.username.getText().toString());
        userUpdates.put("gender", binding.genderSpinner.getSelectedItem().toString());
        userUpdates.put("active", true);

        usersRef.update(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    if (imageUri != null) {
                        uploadImageToFirebase(imageUri);
                    } else {
                        startMainActivity();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                });
    }
    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileReference = FirebaseStorage.getInstance().getReference("uploads").child(System.currentTimeMillis() + getFileExtension(imageUri));

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveImageUrlToDatabase(imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {

                    });
        }
    }

    private void saveImageUrlToDatabase(String imageUrl) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImageUrl", imageUrl);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    startMainActivity();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                });
    }


    private void startMainActivity() {
        Intent newIntent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(newIntent);
        finish();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private boolean isUserDataValid() {
        String username = binding.username.getText().toString().trim();
        String name = binding.name.getText().toString().trim();
        String gender = binding.genderSpinner.getSelectedItem().toString();

        if (username.isEmpty()) {
            binding.username.setError("Vui lòng nhập tên của bạn");
            return false;
        } else if (!username.matches(USERNAME_PATTERN)) {
            binding.username.setError("Username phải gồm 4 ký tự trở lên, không chứa khoảng trống và Ký tự đặc biệt.");
            return false;
        } else if (gender.equals("Chọn giới tính")) {
            // Hiển thị lỗi hoặc thông báo liên quan đến giới tính
            return false;
        } else if (name.isEmpty()) {
            // Hiển thị lỗi hoặc thông báo liên quan đến tên
            return false;
        }

        return true;
    }


    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9]{4,}$";

}
