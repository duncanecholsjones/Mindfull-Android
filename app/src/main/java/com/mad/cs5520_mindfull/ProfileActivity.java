package com.mad.cs5520_mindfull;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ProfileActivity extends DrawerClass {

    protected String username = null;
    protected String moodChartUsername;
    private TextView profileName;
    private ImageView profilePic;
    private ImageView profileDrawer;
    private ImageButton cameraBtn;
    private Button galleryBtn;
    private DatabaseReference realtimeDatabase;

    private StorageReference storageReference;
    private DatabaseReference mDatabase;

    private String currentImagePath = null;
    private Uri outputFileUri;
    private File file;

    /**
     * onCreate() initializes profile and adds extras for database.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_profile, frameLayout);

        username = getIntent().getStringExtra("USERNAME");
        moodChartUsername = getIntent().getStringExtra("MOODCHARTUSER");
        profileName = findViewById(R.id.username);
        profileName.setText(username);

        View view;
        LayoutInflater inflater = getLayoutInflater();
        View myView = inflater.inflate(R.layout.nav_header, null);

        profileDrawer = (ImageView) myView.findViewById(R.id.drawer_user_image_iv);


        profilePic = findViewById(R.id.profile_pic);
        cameraBtn = findViewById(R.id.btn_camera);
        galleryBtn = findViewById(R.id.gallery);

        storageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Init();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if (ContextCompat.checkSelfPermission(ProfileActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(ProfileActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(ProfileActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = timeStamp + ".jpg";
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                currentImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
                file = new File(currentImagePath);
                outputFileUri = FileProvider.getUriForFile(view.getContext(),
                        BuildConfig.APPLICATION_ID + ".provider", file);
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                System.out.println(outputFileUri == null);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(cameraIntent, 100);
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 101);
            }
        });
    }

    /**
     * init() initialize data. Works by clearing local itemList then repopulating
     * This is necessary to work properly
     */
    protected void Init() {
        mDatabase.child("users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot snapshot = task.getResult();
                String picName = snapshot.child(username).child("picture").getValue().toString();
                String picUrl = snapshot.child(username).child("pictureUrl").getValue().toString();
                getProfilePicHandler(picName);
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.child("friends").hasChild(username)) {
                        child.child("friends").child(username).getRef().setValue(picUrl);
                    }
                }
            }

        });
    }


    @SuppressLint("MissingSuperCall")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Bitmap captureImage2 = null;
        if (requestCode == 100) {
            File imgFile = new File(currentImagePath);
            if (imgFile.exists()) {
                Uri outputFileUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider", imgFile);

                Bitmap captureImage = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                try {
                    captureImage2 = rotateImage1(currentImagePath, captureImage);
                    uploadHandler(captureImage2);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                profilePic.setImageBitmap(captureImage2);
                profileDrawer.setImageBitmap(captureImage2);
            }
        } else {
            Uri selected = data.getData();
            profilePic.setImageURI(selected);
            try {
                Bitmap captureImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selected);
                captureImage2 = loadedBitmap(selected);
                uploadHandler(captureImage2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * getProfilePicHandler() gets profile picture from database
     * @param username
     */
    private void getProfilePicHandler(String username) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                getProfilePic(username);
            }
        });
    }

    /**
     * loadedBitmap() helper to load in picture as bitmap
     * @param selectedPicture
     * @return
     */
    private Bitmap loadedBitmap(Uri selectedPicture) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(selectedPicture, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        Bitmap loadedBitmap = BitmapFactory.decodeFile(picturePath);
        ExifInterface exif = null;
        try {
            File pictureFile = new File(picturePath);
            exif = new ExifInterface(pictureFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = ExifInterface.ORIENTATION_NORMAL;

        if (exif != null)
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                loadedBitmap = rotateImage(loadedBitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                loadedBitmap = rotateImage(loadedBitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                loadedBitmap = rotateImage(loadedBitmap, 270);
                break;
        }

        return loadedBitmap;
    }

    /**
     * rotateImage1() helper to orient picture correctly.
     * @param photoPath
     * @param bitmap
     * @return
     * @throws IOException
     */
    // https://www.samieltamawy.com/how-to-fix-the-camera-intent-rotated-image-in-android/
    private static Bitmap rotateImage1(String photoPath, Bitmap bitmap) throws IOException {
        ExifInterface ei = new ExifInterface(photoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:

                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

    /**
     * rotateImage() helper to rotate image correctly
     * @param img
     * @param degree
     * @return
     */
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    /**
     * rotateImage() helper to upload picture
     * @param img2
     */
    private void uploadHandler(Bitmap img2) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                uploadProfilePic(img2);
            }
        });
    }

    /**
     * uploadProfilePic() adds bitmap to user and database
     * @param img2
     */
    private void uploadProfilePic(Bitmap img2) {
        final String key = UUID.randomUUID().toString();
        Bitmap bitmap = img2;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        StorageReference imageRef = storageReference.child("images/" + key);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mDatabase.child("users").child(username).child("picture").setValue(key);
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                while (!uri.isComplete()) ;
                String url = uri.getResult().toString();
                mDatabase.child("users").child(username).child("pictureUrl").setValue(url);

            }
        });

    }

    /**
     * getProfilePic() gets profile pic from database
     * @param pictureName
     */
    private void getProfilePic(String pictureName) {
        StorageReference imgRef = storageReference.child("images").child(pictureName);
        imgRef.getBytes(1024 * 1024 * 5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profilePic.setImageBitmap(bm);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}