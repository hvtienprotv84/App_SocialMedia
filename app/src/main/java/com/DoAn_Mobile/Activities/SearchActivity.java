package com.DoAn_Mobile.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.DoAn_Mobile.Adapters.UserAdapter;
import com.DoAn_Mobile.Authentication.User;
import com.DoAn_Mobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText etSearch;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearch = findViewById(R.id.et_search);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(adapter);
        getCurrentUserUsername();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void searchUsers(String text) {
        Query query = FirebaseFirestore.getInstance().collection("users")
                .orderBy("username")
                .startAt(text)
                .endAt(text + "\uf8ff");

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    if (!user.getUsername().equals(currentUsername)) {
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                // Xử lý lỗi
            }
        });
    }
    private void getCurrentUserUsername() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            FirebaseFirestore.getInstance().collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Lấy thông tin người dùng từ Firestore
                            User user = documentSnapshot.toObject(User.class);

                            // Kiểm tra nếu username không rỗng thì sử dụng
                            if (user != null && user.getUsername() != null && !user.getUsername().isEmpty()) {
                                String username = user.getUsername();
                                currentUsername = username;
                                // Sử dụng biến username ở đây
                            } else {
                                // Handle trường hợp username không tồn tại
                            }
                        } else {
                            // Handle trường hợp không tìm thấy tài khoản người dùng trong Firestore
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý lỗi khi thực hiện truy vấn Firestore
                    });
        } else {
            // Người dùng chưa đăng nhập
        }
    }


}
