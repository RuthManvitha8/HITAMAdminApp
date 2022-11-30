package com.example.hitamadminapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.Reference;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UploadAssignment extends AppCompatActivity {
    private CardView addAssignments;
    private final int req=1;
    private Bitmap bitmap;
    private ImageView notice;
    private EditText assigntitle;
    private Button button;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String downloadurl="";
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_assignment);
        addAssignments=findViewById(R.id.addAssignments);
        notice=findViewById(R.id.assignimage);
        assigntitle=findViewById(R.id.assignmenttitle);
        button=findViewById(R.id.uploadassignbutton);
        databaseReference= FirebaseDatabase.getInstance().getReference();
        storageReference= FirebaseStorage.getInstance().getReference();
        pd=new ProgressDialog(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(assigntitle.getText().toString().isEmpty())
                {
                    assigntitle.setError("Empty, Please Enter a title");
                    assigntitle.requestFocus();
                }
                else if(bitmap==null)
                {
                    uploaddata();
                }
                else
                {
                    uploadimage();
                }
            }
        });
        addAssignments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
    }

    private void uploadimage() {
        pd.setMessage("Uploading");
        pd.show();
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        int i = 50;
        bitmap.compress(Bitmap.CompressFormat.JPEG,i,baos);
        byte[] finalimage= baos.toByteArray();
        final StorageReference filePath;
        filePath= storageReference.child("Assigntitle").child(finalimage+".jpg");
        final UploadTask uploadTask=filePath.putBytes(finalimage);
        uploadTask.addOnCompleteListener(UploadAssignment.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadurl= String.valueOf(uri);
                                    uploaddata();
                                }
                            });

                        }
                    });
                }
                else
                {
                 pd.dismiss();
                 Toast.makeText(UploadAssignment.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void uploaddata() {
        databaseReference = databaseReference.child("Assignment");
        //for unique key
        final String uniquekey = databaseReference.push().getKey();
        Assigndata assigndata = null;
        String title = assigntitle.getText().toString();
        //for time
        Calendar caldate = Calendar.getInstance();
        SimpleDateFormat curdate = new SimpleDateFormat("dd-MM-yy");
        String date = curdate.format(caldate.getTime());
        //For date
        Calendar caltime = Calendar.getInstance();
        SimpleDateFormat curtime = new SimpleDateFormat("hh:mm a");
        String time = curtime.format(caltime.getTime());

        assigndata = new Assigndata(title, downloadurl, date, time, uniquekey);
        databaseReference.child(uniquekey).setValue(assigndata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(UploadAssignment.this, "Assignment uploaded", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(UploadAssignment.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent pick= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pick,req);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==req && resultCode==RESULT_OK ){
            Uri uri = data.getData();
            try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            notice.setImageBitmap(bitmap);
        }
    }
}