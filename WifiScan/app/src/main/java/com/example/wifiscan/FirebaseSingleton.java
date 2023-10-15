package com.example.wifiscan;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirebaseSingleton {
    private static FirebaseSingleton firebaseSingleton;
    private final FirebaseFirestore firebaseFirestore;
    private boolean isEmulator = false;

    private FirebaseSingleton() {
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public static FirebaseSingleton getInstance() {
        if(firebaseSingleton == null) {
            synchronized (FirebaseSingleton.class) {
                firebaseSingleton = new FirebaseSingleton();
            }
        }

        return firebaseSingleton;
    }

    public FirebaseFirestore getFirestore() {
        return firebaseFirestore;
    }

    public void setFirestoreSettings(FirebaseFirestoreSettings firestoreSettings) {
        firebaseFirestore.setFirestoreSettings(firestoreSettings);
    }

    public void useEmulator() {
        setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
        );

        firebaseFirestore.useEmulator("10.0.2.2", 8080);
        isEmulator = true;
    }

    public void useEmulator(String host) {
        setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
        );

        firebaseFirestore.useEmulator(host, 8080);
        isEmulator = true;
    }

    public boolean isEmulator() {
        return isEmulator;
    }
}
