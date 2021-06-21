package com.example.todo;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private static final String TAG = "CustomAdapter";
    private ArrayList<TodoItems> localDataSet;
    private Context localcontext;
    private String localdocumentID;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkbox;
        private final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

//        textView = (TextView) view.findViewById(R.id.checkBox);
            checkbox = (CheckBox) view.findViewById(R.id.checkBox);
            imageView = (ImageView) view.findViewById(R.id.imageView);
        }

        public TextView getCheckBox() {
            return checkbox;
        }

        public ImageView getImageView() {
            return imageView;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public CustomAdapter(Context context, ArrayList<TodoItems> dataSet, String documentID) {
        localDataSet = dataSet;
        localcontext = context;
        localdocumentID = documentID;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.my_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getCheckBox().setText(localDataSet.get(position).getDescription());
        ImageView imgView = viewHolder.getImageView();
//        DocumentReference documentReference = db.collection(mAuth.getCurrentUser().getUid()).document("To do list").collection("Items").document(localDataSet.get(position).getId());
        DocumentReference documentReference = db.collection(localdocumentID).document("To do list").collection("Items").document(localDataSet.get(position).getId());
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                documentReference.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });
            }
        });
        CheckBox cb = viewHolder.checkbox;
        cb.setChecked(localDataSet.get(position).getCheck());

        if (cb.isChecked()) {
            cb.setTextColor(localcontext.getColor(R.color.green));
        } else {
            cb.setTextColor(localcontext.getColor(R.color.red));
        }
        cb.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (cb.isChecked()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("description", localDataSet.get(position).getDescription());
                    data.put("completed", true);
                    data.put("timestamp added", localDataSet.get(position).getDate());
                    documentReference.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });
                    cb.setTextColor(localcontext.getColor(R.color.green));
                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("description", localDataSet.get(position).getDescription());
                    data.put("completed", false);
                    data.put("timestamp added", localDataSet.get(position).getDate());
                    documentReference.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });
                    cb.setTextColor(localcontext.getColor(R.color.red));
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}