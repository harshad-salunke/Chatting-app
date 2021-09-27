package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
//    https://console.firebase.google.com/project/friendlychat-e3b57/overview
    private static final String Tag="MainActivity";
    public static final String ANONYMOUS="anonymus";
    public static final int DEFAULT_MSG_LENGTH_LIMIT=1000;

    private ListView mMessageListw;
    private MessageAdapter mMessageAdapter;
    private ProgressBar progressBar;
    private ImageView mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;
    private static final int RC_PHOTO_PICKER=2;
    private String mUsername;
    boolean check=true;
    ArrayList<FriendlyMessage> data=new ArrayList();

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDatabaseReference;
    private ChildEventListener mchildEventListener;

    //suthotiaction
    private FirebaseAuth mfirebaseAuth;
    private FirebaseAuth.AuthStateListener mauthStateListener;
    //storing data
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReferencechatPhoto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseDatabase = FirebaseDatabase.getInstance("https://friendlychat-e3b57-default-rtdb.asia-southeast1.firebasedatabase.app/");
        mMessageDatabaseReference = mFirebaseDatabase.getReference().child("parisha");
        mfirebaseAuth=FirebaseAuth.getInstance();

        storageReferencechatPhoto=FirebaseStorage.getInstance().getReference().child("chat_photo");

        mUsername = ANONYMOUS;
        progressBar = findViewById(R.id.progressBar);
        mMessageListw = findViewById(R.id.messageListView);
        mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        mSendButton = findViewById(R.id.sendButton);
        mMessageEditText = findViewById(R.id.messageEditText);
        List<FriendlyMessage> friendlyMessageList = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, friendlyMessageList);
        mMessageListw.setAdapter(mMessageAdapter);

        progressBar.setVisibility(View.INVISIBLE);

        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check=false;
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityIfNeeded(intent,RC_PHOTO_PICKER);
            }
        });

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String msg = mMessageEditText.getText().toString();
                FriendlyMessage friendlyMessage = new FriendlyMessage(msg, mUsername, null);
                mMessageDatabaseReference.push().setValue(friendlyMessage);
                mMessageEditText.setText("");
            }
        });

                //user Login page

        mauthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                String UserChild=firebaseAuth.getCurrentUser().toString();
                if(user!=null){
                    // user is signed in
                    Toast.makeText(MainActivity.this,"You are signed in!",Toast.LENGTH_SHORT).show();
                    if(check){
                        onSignInInitalized(user.getDisplayName());
                    }

                }
                else {
                    onSingOutCleanUp();
                    Intent signInIntent =
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build();
                    startActivity(signInIntent);
                }
            }
        };

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
//        Toast.makeText(this, "On resume", Toast.LENGTH_SHORT).show();
        super.onResume();
        mfirebaseAuth.addAuthStateListener(mauthStateListener);
        check=true;
    }

    @Override
    protected void onPause() {
//        Toast.makeText(this, "On pause", Toast.LENGTH_SHORT).show();
        super.onPause();
        DeatachDatabasedRead();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.sign_out_menu){
            AuthUI.getInstance().signOut(this);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSignInInitalized(String username){
        mUsername=username;
        AtachDatabasedListenerReaddata();
    }

    public void onSingOutCleanUp(){
        mUsername=ANONYMOUS;
        mMessageAdapter.clear();
    }

    private void AtachDatabasedListenerReaddata(){
        if(mchildEventListener==null){
            mchildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                    FriendlyMessage friendlyMessage = snapshot.getValue(FriendlyMessage.class);
                    mMessageAdapter.add(friendlyMessage);
                }

                @Override
                public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                }

                @Override
                public void onChildRemoved(DataSnapshot snapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            };
            mMessageDatabaseReference.addChildEventListener(mchildEventListener);
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_PHOTO_PICKER && resultCode==RESULT_OK){
            Uri selectedImageUrl=data.getData();
            progressBar.setVisibility(View.VISIBLE);
            StorageReference photoRef=storageReferencechatPhoto.child(selectedImageUrl.getLastPathSegment());
            //upload photo to firebase storage
            photoRef.putFile(selectedImageUrl).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   if(taskSnapshot.getMetadata()!=null){
                       if(taskSnapshot.getMetadata().getReference()!=null){
                           Task<Uri> result=taskSnapshot.getStorage().getDownloadUrl();
                           result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                               @Override
                               public void onSuccess(Uri uri) {
                                   String imageUrl=uri.toString();
                                   FriendlyMessage friendlyMessage=new FriendlyMessage(null,mUsername,imageUrl);
                                   mMessageDatabaseReference.push().setValue(friendlyMessage);
                                   progressBar.setVisibility(View.GONE);
                               }
                           });
                       }
                   }
                }
            });
        }
    }

    private void DeatachDatabasedRead(){
        if(mchildEventListener!=null){
            mMessageDatabaseReference.removeEventListener(mchildEventListener);
            mchildEventListener=null;
            mMessageAdapter.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteData();
    }

    public void deleteData(){
        mFirebaseDatabase.getReference().child("parisha").removeValue();
    }

}