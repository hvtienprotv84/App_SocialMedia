package com.DoAn_Mobile.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.DoAn_Mobile.Authentication.LoginActivity;
import com.DoAn_Mobile.Authentication.WelcomeActivity;
import com.DoAn_Mobile.MainActivity;
import com.DoAn_Mobile.R;
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        LottieAnimationView animationView = findViewById(R.id.lottieAnimationView);
        animationView.playAnimation();

        String uid = mAuth.getUid(); // Thay thế với UID thực tế của người dùng
        DocumentReference isActiveRef = db.collection("users").document(uid);

        isActiveRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean isActive = documentSnapshot.getBoolean("active");

                if (isActive != null && !isActive) {
                    Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = getIntent();
                            if (intent != null) {
                                String sourceActivity = intent.getStringExtra("source_activity");
                                if (sourceActivity != null && sourceActivity.equals("toMain")) {
                                    Intent newIntent = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(newIntent);
                                    finish();
                                }
                            }
                        }
                    }, 1000);
                }
            }
        }).addOnFailureListener(e -> {
            // Xử lý lỗi đọc dữ liệu
        });
    }
}