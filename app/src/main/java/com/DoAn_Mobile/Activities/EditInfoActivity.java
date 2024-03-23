package com.DoAn_Mobile.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.DoAn_Mobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditInfoActivity extends AppCompatActivity {

    private EditText editName, editNickName;
    private Spinner spinner;
    private Button saveButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        editNickName = findViewById(R.id.editNickName);
        editName = findViewById(R.id.editName);
        spinner = findViewById(R.id.spinner);
        saveButton = findViewById(R.id.button2);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        DocumentReference userRef = db.collection("users").document(mAuth.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String name = document.getString("name");
                    String nickname = document.getString("nickname");
                    String gender = document.getString("gender");

                    editName.setText(name);
                    editNickName.setText(nickname);

                    if (gender != null) {
                        int genderPosition = getGenderPosition(gender);
                        if (genderPosition != -1) {
                            spinner.setSelection(genderPosition);
                        }
                    }
                }
            } else {

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString();
                String nickname = editNickName.getText().toString();
                String gender = spinner.getSelectedItem().toString();

                updateUserInfo(name, nickname, gender);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedName", name);
                resultIntent.putExtra("updatedNickname", nickname);
                resultIntent.putExtra("updatedGender", gender);
                setResult(RESULT_OK, resultIntent);

                finish();
            }
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
    private void updateUserInfo(String name, String nickname, String gender) {
        DocumentReference userRef = db.collection("users").document(mAuth.getUid());
        userRef.update("name", name, "nickname", nickname, "gender", gender);
    }
}
