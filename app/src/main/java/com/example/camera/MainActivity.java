package com.example.camera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //static final int REQUEST_IMAGE_CAPTURE = 1;
    //private Bitmap mImageBitmap;
    //private String mCurrentPhotoPath;
    DatabaseReference databaseReference;
    RecyclerView recyclerView;
    private ImageView mImageView;
    private final int request_code = 100;
    private FloatingActionButton camerabtn;
    StorageReference storageReference;
    String imageFileName;

    String mDownloadUrl;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // mImageView = findViewById(R.id.imgcamera);
        camerabtn = findViewById(R.id.camera);

        storageReference = FirebaseStorage.getInstance().getReference();

        RecyclerView recyclerView1 = findViewById(R.id.recycleviewcam);

        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
       // recyclerView1.setAdapter(new Adapter(getApplicationContext()));

        camerabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //imageuri = getImageUri();
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

        Uri contenturi = data.getData();
        if (requestCode == request_code){

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            imageFileName = "JPG_"+timeStamp;
            Bitmap img = (Bitmap) (data.getExtras()).get("data");
           // mImageView.setImageBitmap(img);

            uploadImage(imageFileName,data);

            saveimage(img);

            //mongodpConnection(data);
        }
    }

    private void getImage(String imageFileName) {

        System.out.println(imageFileName);
        //String getfilename = bb.toString();
        storageReference = FirebaseStorage.getInstance().getReference().child("image/"+imageFileName);

        try {
            File localfile = File.createTempFile("temp",".jpg");
            storageReference.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this,"Successfully get the image",Toast.LENGTH_LONG).show();
                    Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                    mImageView.setImageBitmap(bitmap);
                    System.out.println(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
               Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_LONG).show();
                }
            });


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    private void mongodpConnection(Intent data) {
//
//
//        ConnectionString connectionString = new ConnectionString("mongodb+srv://Aravinthan:<password>@cluster0.zfdils2.mongodb.net/?retryWrites=true&w=majority");
//        MongoClientSettings settings = MongoClientSettings.builder()
//                .applyConnectionString(connectionString)
//                .serverApi(ServerApi.builder()
//                        .version(ServerApiVersion.V1)
//                        .build())
//                .build();
//        MongoClient mongoClient = MongoClients.create(settings);
//        MongoDatabase database = mongoClient.getDatabase("test");
//
//    }


    private void uploadImage(String imageFileName, Intent data) {
        Bitmap map = (Bitmap) (data.getExtras()).get("data");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        map.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte bb[] = byteArrayOutputStream.toByteArray();


        uploadToFirebase(imageFileName,bb);

    }

    private void uploadToFirebase(String imageFileName, byte[] bb) {

        //String filename = bb.toString();
        System.out.println(imageFileName);

            StorageReference sr = storageReference.child("image/"+imageFileName);
            sr.putBytes(bb).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this, "Successfully upload", Toast.LENGTH_LONG).show();
                    final Task<Uri> firebaseuri = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseuri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mDownloadUrl = uri.toString();
                            System.out.println("Uri"+mDownloadUrl);
                        }
                    });

                    getImage(imageFileName);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Failed to upload", Toast.LENGTH_LONG).show();
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


class Adapter extends RecyclerView.Adapter<MyViewHolder> {

    Context context;
    List<Pictures> pictures;

    public Adapter(Context context, List<Pictures> pictures) {
        this.context = context;
        this.pictures = pictures;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //holder.image.setImageBitmap(pictures.get(position).getImage_url());
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder {
    public ImageView image;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.imagecard);
    }
}