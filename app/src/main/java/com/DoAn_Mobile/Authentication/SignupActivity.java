package com.DoAn_Mobile.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.DoAn_Mobile.MainActivity;
import com.DoAn_Mobile.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfPassword;
    TextView btnLogin, tvNotePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfPassword = findViewById(R.id.edtConfirmPassword);
        tvNotePass = findViewById(R.id.txtNotePass);
        btnLogin = findViewById(R.id.btnLogin);
        Button btnSignup = findViewById(R.id.btnSignup);
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
        });

        btnSignup.setOnClickListener(v -> {
            if(isValid()) {
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Tạo document người dùng mới trong Firestore
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("email", email);
                                    // thêm các trường khác nếu cần
                                    if (firebaseUser != null) {
                                        db.collection("users").document(firebaseUser.getUid()).set(user);
                                    }

                                    mAuth.getCurrentUser().sendEmailVerification();
                                    mAuth.signOut();
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    Toast.makeText(SignupActivity.this, "Account Created! Vui lòng xác thực Email trước khi đăng nhập!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(SignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });




        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isValidEmail(s.toString())) {
                    edtEmail.setError(null);


                } else {
                    // Nếu mật khẩu không hợp lệ, có thể hiển thị thông báo lỗi
                    edtEmail.setError("Email không hợp lệ");
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isValidPassword(s.toString())) {
                    edtPassword.setError(null);
                    if(!arePasswordsMatching(edtConfPassword.getText().toString(),s.toString())){
                        edtConfPassword.setError("Password không trùng khớp!");
                    }else{
                        edtConfPassword.setError(null);
                    }
                } else {
                    // Nếu mật khẩu không hợp lệ, có thể hiển thị thông báo lỗi
                    edtPassword.setError("Mật khẩu không hợp lệ");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(tvNotePass, "alpha", 0f, 1f);
                    fadeIn.setDuration(500); // Thời gian animation là 1000 milliseconds

                    fadeIn.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            tvNotePass.setVisibility(View.VISIBLE);
                        }
                    });
                    fadeIn.start();
                } else {
                    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(tvNotePass, "alpha", 1f, 0f);
                    fadeOut.setDuration(500); // Thời gian animation là 1000 milliseconds

// Ẩn TextView sau khi hoàn thành animation
                    fadeOut.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            tvNotePass.setVisibility(View.GONE);
                        }
                    });

                    fadeOut.start();
                }
            }
        });
        edtConfPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(!arePasswordsMatching(edtPassword.getText().toString(),s.toString())){
                        edtConfPassword.setError("Password không trùng khớp!");
                    }else{
                        edtConfPassword.setError(null);
                    }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        if (email == null){
            return false;
        }

        return pattern.matcher(email).matches();
    }
    public static boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-zA-Z]).{6,20}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        if (password == null){
            return false;
        }

        return pattern.matcher(password).matches();
    }

    public static boolean arePasswordsMatching(String password, String confirmPassword) {
        return password != null && confirmPassword != null && password.equals(confirmPassword);
    }

    private boolean isValid(){
        if(!isValidEmail(edtEmail.getText().toString())){
            return false;
        } else if (!isValidPassword(edtPassword.getText().toString())) {
            return false;
        } else if (!arePasswordsMatching(edtPassword.getText().toString(),edtConfPassword.getText().toString())) {
            return false;
        }else
            return true;
    }
}