package com.DoAn_Mobile;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.DoAn_Mobile.Adapters.ChangePasswordActivity;
import com.DoAn_Mobile.Adapters.WatchAdapter;
import com.DoAn_Mobile.Authentication.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.DoAn_Mobile.Adapters.VpagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {
    BottomNavigationView navigationView;
    ViewPager2 pager2;
    //Button button;
    TextView button;
    private FirebaseAuth mAuth;

    private Handler sessionHandler = new Handler();
    private Runnable sessionCheckerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        });

        navigationView = findViewById(R.id.BottomNav);
        pager2 = findViewById(R.id.pager2);

        VpagerAdapter adapter = new VpagerAdapter(this);
        pager2.setAdapter(adapter);

        checkSession();
        sessionCheckerRunnable = new Runnable() {
            @Override
            public void run() {
                checkSession();
                sessionHandler.postDelayed(this, 2000); //2s
            }
        };

        // Bắt đầu lặp lại kiểm tra session
        sessionHandler.post(sessionCheckerRunnable);

        navigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.Home:
                    pager2.setCurrentItem(0, true);
                    return true;
                case R.id.Watch:
                    pager2.setCurrentItem(1, true);
                    return true;
                case R.id.Find:
                    pager2.setCurrentItem(2, true);
                    return true;
                case R.id.Profile:
                    pager2.setCurrentItem(3, true);
                    return true;
            }
            return false;
        });

        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    navigationView.setSelectedItemId(R.id.Home);
                } else if (position == 1) {
                    navigationView.setSelectedItemId(R.id.Watch);
                } else if (position == 2) {
                    navigationView.setSelectedItemId(R.id.Find);
                } else if (position == 3) {
                    navigationView.setSelectedItemId(R.id.Profile);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //updateUI(currentUser);
            checkSession();
        }
    }
    private String getSessionId() {
        SharedPreferences sharedPref = getSharedPreferences("PreSession2", Context.MODE_PRIVATE);
        return sharedPref.getString("sessionID2", null);
    }


    private void checkSession() {
        String sessionId = getSessionId();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Thay vì sử dụng Realtime Database, chuyển sang Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(user.getUid());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Lấy giá trị của session từ Firestore
                            String currentSessionID = document.getString("session");
                            if (sessionId != null && !sessionId.equals(currentSessionID)) {
                                showConfirmationDialog();
                            }
                        } else {
                            // Nếu không tìm thấy tài liệu, có thể xử lý theo ý của bạn
                        }
                    } else {
                        // Xử lý lỗi truy vấn
                    }
                }
            });
        }
    }



    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông báo!");
        builder.setMessage("Tài khoản này đang được đăng nhập ở thiết bị khác, vui lòng đăng nhập lại!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                signOutAndStartSignInActivity();
                finish();
            }
        });
        builder.show();
    }
    private void signOutAndStartSignInActivity() {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dừng lặp lại khi activity bị hủy
        sessionHandler.removeCallbacks(sessionCheckerRunnable);
    }
}