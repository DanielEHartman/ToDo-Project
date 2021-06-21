package com.example.todo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.model.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class List extends AppCompatActivity {

    private static final String TAG = "List";
    CustomAdapter adapter;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    FloatingActionButton floatingActionButton;
    Context context;
    ImageView imgCloud, imgSettings;
    String userID;
    RecyclerView recyclerView;
    ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        context = this;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        imgCloud = findViewById(R.id.imgCloud);
        imgSettings = findViewById(R.id.imgSettings);
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        userID = settings.getString("userID", mAuth.getCurrentUser().getUid());
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//"n9OEVkRsmuViCAQjn05T6hpPIPv2"
//        mAuth.getCurrentUser().getUid()

        CollectionReference collectionReference = db.collection(userID).document("To do list").collection("Items");
        RegisterSnapshotListener(collectionReference);


//        adapter = new CustomAdapter(this, todoitems);

        floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Create new");

                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
//qwe
                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputString = input.getText().toString();
                        Map<String, Object> data = new HashMap<>();
                        data.put("description", inputString);
                        data.put("completed", false);
                        data.put("timestamp added", new Date());
//                        db.collection().document("To do list").collection("Items").add(data)
                        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
                        userID = settings.getString("userID", mAuth.getCurrentUser().getUid());
                        db.collection(userID).document("To do list").collection("Items").add(data)
                                .addOnSuccessListener(new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error adding document", e);
                                    }
                                });

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        imgSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("User settings");

                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                if(!userID.equals(mAuth.getCurrentUser().getUid()))
                input.setText(userID);
                builder.setView(input);

                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputString = input.getText().toString();
                        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("userID",inputString);
                        editor.commit();
                        registration.remove();
                        CollectionReference collectionReference = db.collection(inputString).document("To do list").collection("Items");
                        RegisterSnapshotListener(collectionReference);

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.setNeutralButton("Copy mine", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedID = input.getText().toString().matches("") ? mAuth.getCurrentUser().getUid(): input.getText().toString() ;
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("userID", selectedID);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getApplicationContext(),"Copied",Toast.LENGTH_SHORT).show();
                    }
                });


                builder.show();
            }

        });
    }

    private void RegisterSnapshotListener(CollectionReference collectionReference) {
        Query query = collectionReference.orderBy("timestamp added", Query.Direction.ASCENDING);
        registration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                if (value != null && !value.isEmpty()) {
                    java.util.List<DocumentSnapshot> dc = value.getDocuments();
                    ArrayList<TodoItems> todoitems = new ArrayList<>();
                    for (DocumentSnapshot doc : dc) {
                        Log.d(TAG, "Current data: " + doc.get("description"));
                        todoitems.add(new TodoItems((String) doc.getId(), (String) doc.get("description"), (Boolean) doc.get("completed"), (Timestamp) doc.get("timestamp added")));
                    }
                    setAdapter(todoitems);
//                    recyclerView.setAdapter(adapter);
                    imgCloud.setImageResource(value.getMetadata().isFromCache() ? R.drawable.ic_cloud_off_outline_grey600_36dp : R.drawable.ic_cloud_check_outline_grey600_36dp);

                } else {
                    if (adapter != null)
                        recyclerView.setAdapter(null);

//                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Current data: null");
                }
            }
        });

    }

    private void setAdapter(ArrayList<TodoItems> todoitems) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new CustomAdapter(this, todoitems);
        adapter = new CustomAdapter(this, todoitems, userID);
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }
}

