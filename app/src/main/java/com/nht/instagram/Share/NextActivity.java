package com.nht.instagram.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nht.instagram.R;
import com.nht.instagram.Utils.FirebaseMethods;
import com.nht.instagram.Utils.UniversalImageLoader;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private ImageView back;
    private TextView sharePost;
    private String mAppend = "file:/";
    private EditText mCaption;
    private String imgURL;
    private Intent intent;
    private Bitmap bitmap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        setupFirebaseAuth();

        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });

        sharePost = (TextView)findViewById(R.id.sharePost);
        sharePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: sharing post");
                Toast.makeText(NextActivity.this, "Uploading...", Toast.LENGTH_SHORT).show();
                //upload image to firebase
                mCaption = (EditText)findViewById(R.id.caption);
                String caption = mCaption.getText().toString();

                intent = getIntent();

                if(intent.hasExtra(getString(R.string.selected_image))){
                    imgURL = intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imgURL,null);
                }
                else if(intent.hasExtra(getString(R.string.selected_bitmap))){
//                    bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    byte[] byteArray = intent.getByteArrayExtra(getString(R.string.selected_bitmap));
                    Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, null, bitmap);
                }
            }
        });
        setImage();
    }

    private void setImage(){
        Intent intent = getIntent();
        ImageView image = (ImageView)findViewById(R.id.imageShare);

        if(intent.hasExtra(getString(R.string.selected_image))){
            imgURL = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new image url: " + imgURL);
            UniversalImageLoader.setImage(imgURL, image, null, mAppend);
        }
        else if(intent.hasExtra(getString(R.string.selected_bitmap))){
//            bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
            byte[] byteArray = intent.getByteArrayExtra(getString(R.string.selected_bitmap));
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            image.setImageBitmap(bitmap);
        }
    }

    /*
    --------------------------------------- Firebase Authentication -------------------------------
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebase: setting up firebase auth");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mFirebaseMethods = new FirebaseMethods(NextActivity.this);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: sign in: " + user.getUid());
                }
                else{
                    Log.d(TAG, "onAuthStateChanged: sign out");
                }
            }
        };

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
