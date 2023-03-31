package com.example.camera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //static final int REQUEST_IMAGE_CAPTURE = 1;
    //private Bitmap mImageBitmap;
    //private String mCurrentPhotoPath;
    private ImageView mImageView;

    private final int request_code = 100;
    private FloatingActionButton camerabtn;

    private Uri imageuri;

    StorageReference storageReference;

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.imgcamera);
        camerabtn = findViewById(R.id.camera);

        camerabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
               // imageuri = getImageUri();
                startActivityForResult(cameraIntent,request_code);


            }
        });
    }
//
//    private Uri getImageUri() {
//        Uri m_imgUri = null;
//        File m_file;
//        try {
//            SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
//            String m_curentDateandTime = m_sdf.format(new Date());
//            String m_imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + m_curentD ateandTime + ".jpg";
//            m_file = new File(m_imagePath);
//            m_imgUri = Uri.fromFile(m_file);
//        } catch (Exception p_e) {
//        }
//        return m_imgUri
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == request_code){

            Bitmap img = (Bitmap) (data.getExtras()).get("data");
            mImageView.setImageBitmap(img);

            uploadImage(imageuri);

            saveimage(img);
        }
    }


    private void uploadImage(Uri imageuri) {

        storageReference = FirebaseStorage.getInstance().getReference();

        storageReference.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this,"Image added Successfully",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Failed to Upload",Toast.LENGTH_LONG).show();
            }
        });
    }


    private void saveimage(Bitmap img) {

        Uri images;
        ContentResolver contentResolver = getContentResolver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }else {
            images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() +".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
        Uri uri = contentResolver.insert(images, contentValues);
        try {
            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            img.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);

           // Toast.makeText(MainActivity.this,"Store in mobile",Toast.LENGTH_LONG);

            //

        }catch (Exception e){
            //
            e.printStackTrace();

        }


    }
}