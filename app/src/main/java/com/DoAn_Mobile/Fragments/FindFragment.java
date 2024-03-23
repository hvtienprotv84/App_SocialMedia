package com.DoAn_Mobile.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import com.DoAn_Mobile.Activities.MatchActivity;
import com.DoAn_Mobile.Adapters.FindAdapter;
import com.DoAn_Mobile.Authentication.User;
import com.DoAn_Mobile.Models.Message;
import com.DoAn_Mobile.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FindFragment extends Fragment implements FindAdapter.UserInteractionListener {


    ViewPager2 viewPager;
    double currentUserLatitude, currentUserLongitude;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 mét
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 phút
    private static final int PAGE_SIZE = 10; // Ví dụ: Tải 10 người dùng mỗi lần
    private int currentPageStart = 0;
    FindAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Firestore instance


    public FindFragment() {
        // Required empty public constructor
    }


    public static FindFragment newInstance(String param1, String param2) {
        FindFragment fragment = new FindFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find, container, false);
        viewPager = view.findViewById(R.id.pagerFind);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        Button btnMatch = view.findViewById(R.id.btnMatch);
        btnMatch.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MatchActivity.class);
            startActivity(intent);
        });

        getDefaultLocationFromFirebase();
        setupLocationManagerAndListener();
        requestLocationPermission();

        return view;
    }
    private void setupLocationManagerAndListener() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentUserLatitude = location.getLatitude();
                currentUserLongitude = location.getLongitude();
                Log.d("LocationUpdate", "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
                updateLocationInFirebase(currentUserLatitude, currentUserLongitude);
                fetchUsersAndUpdateViewPager();
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Implement as needed or leave it empty
            }
            @Override
            public void onProviderEnabled(String provider) {
                Log.d("LocationUpdate", "Provider enabled: " + provider);
                // You can implement your logic here if needed when provider is enabled
            }
            @Override
            public void onProviderDisabled(String provider) {
                // This method must be overridden
                Log.d("LocationUpdate", "Provider disabled: " + provider);
            }
            // ... (xử lý các phương thức khác của LocationListener)
        };
    }
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettingsAndStartLocationUpdates();
            } else {
                // Xử lý trường hợp quyền bị từ chối
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                getCurrentLocation();
            } else {
                // Xử lý trường hợp người dùng từ chối bật vị trí
            }
        }
    }



    private void getCurrentLocation() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        } catch (SecurityException e) {
            Log.e("LocationError", "SecurityException: " + e.getMessage());
        }
    }

    //Phân trang 10 người 1 lần
    private void fetchUsersAndUpdateViewPager() {
        // Lấy ID của người dùng hiện tại
        String currentUserId = mAuth.getUid();

        // Truy xuất danh sách likes và dislikes
        Task<DocumentSnapshot> likesTask = db.collection("Likes").document(currentUserId).get();
        Task<DocumentSnapshot> dislikesTask = db.collection("Dislikes").document(currentUserId).get();

        // Chờ cả hai truy vấn hoàn thành
        Tasks.whenAllComplete(likesTask, dislikesTask).addOnCompleteListener(tasks -> {
            Set<String> excludedUserIds = new HashSet<>();
            if (likesTask.isSuccessful() && likesTask.getResult().exists()) {
                excludedUserIds.addAll(likesTask.getResult().getData().keySet());
            }
            if (dislikesTask.isSuccessful() && dislikesTask.getResult().exists()) {
                excludedUserIds.addAll(dislikesTask.getResult().getData().keySet());
            }

            // Bây giờ truy xuất và lọc người dùng từ Firestore
            Query query = db.collection("users")
                    .orderBy("id")
                    .startAfter(currentPageStart)
                    .limit(PAGE_SIZE);

            db.collection("users")
                    .document(currentUserId)
                    .collection("friends")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<String> friendIds = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                String friendUid = document.getId();
                                friendIds.add(friendUid);
                            }

                            // After getting friendIds, execute the query to get users
                            query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                                List<User> newUserList = new ArrayList<>();
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    User user = documentSnapshot.toObject(User.class);
                                    if (user != null && user.getLocation() != null
                                            && !excludedUserIds.contains(user.getId())
                                            && !friendIds.contains(user.getId())) {
                                        newUserList.add(user);
                                    }
                                }
                                updateAdapterData(newUserList);

                            }).addOnFailureListener(e -> {
                                Log.e("Firestore", "Error getting user data: ", e);
                            });
                        }
                    });


//            query.get().addOnSuccessListener(queryDocumentSnapshots -> {
//                List<User> newUserList = new ArrayList<>();
//                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                    User user = documentSnapshot.toObject(User.class);
//                    if (user != null && user.getLocation() != null && !excludedUserIds.contains(user.getId())) {
//                        newUserList.add(user);
//                    }
//                }
//                updateAdapterData(newUserList);
//
//            }).addOnFailureListener(e -> {
//                Log.e("Firestore", "Error getting user data: ", e);
//            });
        });
    }



    private void updateAdapterData(List<User> newUserList) {
        FindAdapter adapter = new FindAdapter(new ArrayList<>(), currentUserLatitude, currentUserLongitude, this);
        viewPager.setAdapter(adapter);
        if (adapter != null) {
            List<User> existingUsers = adapter.getUserList();

            for (User user : newUserList) {
                if (user.getLocation() != null && !Objects.equals(user.getId(), mAuth.getUid())) {
                    existingUsers.add(user);
                }
            }

            adapter.notifyDataSetChanged();
        } else {
            List<User> usersWithLocationAndDistance = new ArrayList<>();
            for (User user : newUserList) {
                if (user.getLocation() != null && !Objects.equals(user.getId(), mAuth.getUid())) {
                    usersWithLocationAndDistance.add(user);
                }
            }

            adapter = new FindAdapter(usersWithLocationAndDistance, currentUserLatitude, currentUserLongitude,this);
            viewPager.setAdapter(adapter);
        }
    }


    private void getDefaultLocationFromFirebase() {
        String userId = FirebaseAuth.getInstance().getUid();
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Giả sử bạn có một lớp Location có thể phân tích tài liệu Firestore
                com.DoAn_Mobile.Authentication.Location location = documentSnapshot.get("location", com.DoAn_Mobile.Authentication.Location.class);
                if (location != null) {
                    currentUserLatitude = location.getLatitude();
                    currentUserLongitude = location.getLongitude();
                    fetchUsersAndUpdateViewPager();
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Error getting data", e);
        });
    }



    private void updateLocationInFirebase(double latitude, double longitude) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> location = new HashMap<>();
        location.put("latitude", latitude);
        location.put("longitude", longitude);

        userRef.update("location", location);
    }
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private int calculateDistance(double lat1, double long1, double lat2, double long2) {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(long1);

        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(long2);

        float distanceInMeters = location1.distanceTo(location2);
        int distanceInKilometers = Math.round(distanceInMeters / 1000);
        return distanceInKilometers;
    }


    @Override
    public void onLikeClicked(User user) {
        // Xử lý khi người dùng nhấn 'like'
        FindAdapter adapter = (FindAdapter) viewPager.getAdapter();
        if (adapter != null) {
            int currentItem = viewPager.getCurrentItem();
            adapter.removeUserAt(currentItem); // Xóa user khỏi adapter
            adapter.notifyItemRemoved(currentItem); // Thông báo cho adapter biết một item đã được xóa

            // Di chuyển đến item tiếp theo nếu còn item trong danh sách
            if (currentItem < adapter.getItemCount()) {
                viewPager.setCurrentItem(currentItem, true);
            }
        } else {
            Log.e("FindFragment", "Adapter is null");
        }
        sendLike(mAuth.getUid(),user.getId());
    }

    @Override
    public void onDislikeClicked(User user) {
        // Xử lý khi người dùng nhấn 'dislike'
        FindAdapter adapter = (FindAdapter) viewPager.getAdapter();
        if (adapter != null) {
            int currentItem = viewPager.getCurrentItem();
            adapter.removeUserAt(currentItem); // Xóa user khỏi adapter
            adapter.notifyItemRemoved(currentItem); // Thông báo cho adapter biết một item đã được xóa

            // Di chuyển đến item tiếp theo nếu còn item trong danh sách
            if (currentItem < adapter.getItemCount()) {
                viewPager.setCurrentItem(currentItem, true);
            }
        } else {
            Log.e("FindFragment", "Adapter is null");
        }
        sendDisLike(mAuth.getUid(),user.getId());
    }


    private static final int REQUEST_CHECK_SETTINGS = 1001; // Mã yêu cầu định nghĩa bởi bạn

    private void checkLocationSettingsAndStartLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), locationSettingsResponse -> {
            getCurrentLocation();
        });

        task.addOnFailureListener(getActivity(), e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Xử lý lỗi
                }
            }
        });
    }
    private void sendLike(String currentUserId, String likedUserId) {
        DocumentReference likesRef = db.collection("Likes").document(currentUserId);

        Map<String, Object> like = new HashMap<>();
        like.put(likedUserId, true);

        likesRef.set(like, SetOptions.merge()).addOnSuccessListener(aVoid -> {
            checkForMatch(currentUserId, likedUserId);
        }).addOnFailureListener(e -> {
            // Handle errors
            Log.e("Firestore", "Error sending like: ", e);
        });
    }
    private void sendDisLike(String currentUserId, String disLikedUserId) {
        DocumentReference likesRef = db.collection("Dislikes").document(currentUserId);

        Map<String, Object> like = new HashMap<>();
        like.put(disLikedUserId, true);

        likesRef.set(like, SetOptions.merge()).addOnSuccessListener(aVoid -> {
            checkForMatch(currentUserId, disLikedUserId);
        }).addOnFailureListener(e -> {
            // Handle errors
            Log.e("Firestore", "Error sending like: ", e);
        });
    }

    private void checkForMatch(String currentUserId, String likedUserId) {
        DocumentReference likedUserRef = db.collection("Likes").document(likedUserId);

        likedUserRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.getBoolean(currentUserId) != null) {


                String conversationId = getConversationId(currentUserId, likedUserId);
                Message welcomeMessage = new Message(currentUserId, likedUserId, "Chúc mừng! Bạn đã được ghép đôi.", new Date());

// Match found
                Map<String, Object> match = new HashMap<>();
                match.put("conversationId", conversationId);

                // Tạo một document mới cho cặp đôi này trong collection 'Matches'
                DocumentReference matchRef = db.collection("Matches").document();
                matchRef.set(match).addOnSuccessListener(aVoid -> {
                    db.collection("Messages").document(conversationId)
                            .update("messages", FieldValue.arrayUnion(welcomeMessage))

                            .addOnFailureListener(e -> {
                                // Xử lý trường hợp document cuộc hội thoại chưa tồn tại
                                if (e.getMessage().contains("No document to update")) {
                                    // Tạo document mới cho cuộc hội thoại
                                    Map<String, Object> conversationData = new HashMap<>();
                                    conversationData.put("messages", Arrays.asList(welcomeMessage));
                                    db.collection("Messages").document(conversationId)
                                            .set(conversationData);
                                } else {
                                    Log.e("Firestore", "Error adding welcome message: ", e);
                                }
                            });


                    // openChat(currentUserId, likedUserId); // Gọi phương thức để mở chat nếu cần
                }).addOnFailureListener(e -> {
                    // Handle errors khi tạo match
                    Log.e("Firestore", "Error setting match: ", e);
                });
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error checking for match: ", e);
        });
    }


    private String getConversationId(String userId1, String userId2) {
        // Sắp xếp userId1 và userId2 theo thứ tự bảng chữ cái và nối chúng lại với nhau
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }



}