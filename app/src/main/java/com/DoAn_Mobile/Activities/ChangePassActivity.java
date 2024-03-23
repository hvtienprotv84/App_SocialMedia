package com.DoAn_Mobile.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.DoAn_Mobile.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ChangePassActivity extends AppCompatActivity {

    private EditText edtEmail;
    private Button btnSend;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtEmail = findViewById(R.id.edtemail);
        btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResetPasswordEmail();
            }
        });
    }

    private void sendResetPasswordEmail() {
        String email = edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Địa chỉ email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Nếu email reset mật khẩu được gửi thành công, có thể cập nhật thông tin trong Firestore
                            updateFirestore(email);
                            Toast.makeText(ChangePassActivity.this, "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư đến của bạn.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChangePassActivity.this, "Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateFirestore(String email) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("resetPasswordRequested", true);

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String userId = task.getResult().getDocuments().get(0).getId();
                            db.collection("users").document(userId).update(updateData);
                        }
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
}
