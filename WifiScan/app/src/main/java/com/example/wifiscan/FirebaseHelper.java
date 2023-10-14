package com.example.wifiscan;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class FirebaseHelper {
    FirebaseFirestore firebaseFirestore;
    public FirebaseHelper(FirebaseFirestore firebaseFirestore) {
        this.firebaseFirestore = firebaseFirestore;
    }

    public FirebaseHelper() {
        firebaseFirestore = FirebaseSingleton.getInstance().getFirestore();
    }

    public void createEntry(String document, Map<String, Object> entries) {
        entries.putIfAbsent("created_at", FieldValue.serverTimestamp());
        firebaseFirestore.collection(document)
                .add(entries)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("FirestoreHelper", "created document");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FirestoreHelper", "error creating document", e);
                    }
                });
    }
}
