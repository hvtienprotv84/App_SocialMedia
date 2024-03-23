package com.DoAn_Mobile.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.DoAn_Mobile.Activities.ChangePassActivity;
import com.DoAn_Mobile.MainActivity;
import com.DoAn_Mobile.R;
import com.DoAn_Mobile.UI.SplashActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference databaseReferences;
    TextView btnSignup;
    private FirebaseFirestore db;

    EditText edtEmail, edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        Button btnLogin;
        edtEmail = findViewById(R.id.edtEmail2);
        edtPassword = findViewById(R.id.edtPassword2);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            if(isValid()){
                signInEmailPassword(email,password);
            }


        });
        TextView textForgot = findViewById(R.id.textforgot);
        textForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở ChangePassActivity khi người dùng bấm vào "Forgot password"
                Intent intent = new Intent(LoginActivity.this, ChangePassActivity.class);
                startActivity(intent);
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
                } else {

                    edtPassword.setError("Mật khẩu không hợp lệ");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(this,SignupActivity.class);
            startActivity(intent);
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        ImageView button = findViewById(R.id.btnButton);
        button.setOnClickListener(v -> {
            signIn();
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            updateUI(currentUser);
        }
    }
    public void updateUI(FirebaseUser user) {
        databaseReferences = FirebaseDatabase.getInstance().getReference("users");

        if(user!=null){
            createSession();
            Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
            intent.putExtra("source_activity", "toMain");
            startActivity(intent);
        }
    }

    private void createSession() {
        String sessionId = UUID.randomUUID().toString();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("session", sessionId);
            docRef.update(sessionInfo);
            storeSessionId(sessionId);
        }
    }

    private void storeSessionId(String sessionId) {
        SharedPreferences sharedPref = getSharedPreferences("PreSession2", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("sessionID2", sessionId);
        editor.apply();
    }



    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private static final int RC_SIGN_IN = 9008;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            checkIfEmailExists(user.getEmail());

                        } else {
                            updateUI(null);
                            Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Sign in Email/Password
    private void signInEmailPassword(String loginEmail, String loginPassword) {
        mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (!mAuth.getCurrentUser().isEmailVerified()) {
                        Toast.makeText(LoginActivity.this, "Vui lòng xác thực Email trước khi đăng nhập!", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    } else {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkIfEmailExists(user.getEmail());
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Wrong Email or Password!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // end Sign in Email/Password
    private void checkIfEmailExists(String email) {
        if (email != null) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Người dùng đã đăng nhập
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference usersRef = db.collection("users");

                usersRef.whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                // Email đã tồn tại trong cơ sở dữ liệu
                                for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                    // Xử lý mỗi tài liệu ở đây
                                    Map<String, Object> userUpdate = new HashMap<>();
                                    userUpdate.put("id", currentUser.getUid());
                                    userUpdate.put("email", documentSnapshot.getString("email"));
                                    userUpdate.put("bio", documentSnapshot.getString("bio"));
                                    userUpdate.put("gender", documentSnapshot.getString("gender"));
                                    userUpdate.put("active", documentSnapshot.getBoolean("active"));
                                    userUpdate.put("profileImageUrl", documentSnapshot.getString("profileImageUrl"));
                                    userUpdate.put("name", documentSnapshot.getString("name"));
                                    userUpdate.put("username", documentSnapshot.getString("username"));

                                    usersRef.document(currentUser.getUid()).update(userUpdate);
                                    updateUI(currentUser);
                                    finish();
                                }
                            } else {
                                // Email chưa tồn tại trong cơ sở dữ liệu
                                User users = new User(currentUser.getUid(), currentUser.getEmail(), "", "Male", false);
                                usersRef.document(currentUser.getUid()).set(users)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(LoginActivity.this, "Welcome, " + currentUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                                                    updateUI(currentUser);
                                                    finish();
                                                } else {
                                                    updateUI(null);
                                                    Toast.makeText(LoginActivity.this, "Failed to create user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Xử lý lỗi truy vấn
                            Toast.makeText(LoginActivity.this, "Query failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(LoginActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
            }
        }
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


    private boolean isValid(){
        if(!isValidEmail(edtEmail.getText().toString())){
            return false;
        } else if (!isValidPassword(edtPassword.getText().toString())) {
            return false;
        }else
            return true;
    }
}