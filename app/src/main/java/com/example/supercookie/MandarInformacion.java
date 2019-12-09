package com.example.supercookie;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MandarInformacion {
    private static final String TAG = "POKEDEX";

    public void mandarIfo(View view ){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("Nombre","Carlos");
        user.put("Direccion","col. la joya sector 3, casa 3322");

        db.collection("Usuario").document("2").set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(BulbasaurActivity.this,"add new contact",Toast.LENGTH_SHORT).show();
            }


        })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }


                });


        // Access a Cloud Firestore instance from your Activity




    }
}
