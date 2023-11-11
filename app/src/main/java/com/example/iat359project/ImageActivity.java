package com.example.iat359project;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView entry;
    Button cameraBtn, galleryBtn, nextBtn;
    String imagePath; //file location of the saved image from gallery or image to save if from camera
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    Context c = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //Initialization
        entry = findViewById(R.id.entryImg);
        cameraBtn = findViewById(R.id.buttonCamera);
        galleryBtn = findViewById(R.id.buttonGallery);
        nextBtn = findViewById(R.id.buttonNext);

        cameraBtn.setOnClickListener(this);
        galleryBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch(view.getId()){
            case R.id.buttonCamera:
                    if(allPermissionsGranted()){ //If all necessary permissions are given, take photo
                        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (i.resolveActivity(getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                Log.d("ex", "cannot create file");
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                //Create photo and save it to the file path
                                Uri photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()), BuildConfig.APPLICATION_ID + ".provider", photoFile);
                                i.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                resultLauncher.launch(i);
                            }
                        }
                    }
                    else{ //If all necessary permissions are not given, request them
                        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                    }
                break;
            case R.id.buttonGallery: //Get the image from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryResultLauncher.launch(intent);
                break;
            case R.id.buttonNext:
                //Saves file path to SharedPreferences
                SharedPreferences sp = getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor e = sp.edit();
                e.putString(Constants.IMAGE, imagePath);
                e.commit();
                //Advances to next activity
                Intent in = new Intent(ImageActivity.this, MoodActivity.class);
                startActivity(in);
                break;
        }
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult( //For camera
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    File f = new File(imagePath);
                    Bitmap pic = BitmapFactory.decodeFile(f.getAbsolutePath());
                    try { //Rotates the photo is it is not oriented correctly
                        pic = rotatePhoto(f.getAbsolutePath(), pic);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    entry.setImageBitmap(pic); //Sets the image view as the taken photo
                    Toast.makeText(c, "FILE PATH: " + imagePath, Toast.LENGTH_SHORT).show();
                }
            }
    );

    ActivityResultLauncher<Intent> galleryResultLauncher = registerForActivityResult( //For gallery
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        //Gets file path from the gallery and then displays the image in the activity
                        Intent data = result.getData();
                        Uri selectedImage = data.getData();
                        imagePath = getPath(getApplicationContext(), selectedImage ); //gets the location of the gallery activity
                        entry.setImageURI(selectedImage); //Sets the image view as the chosen photo
                        Log.d("Picture Path", imagePath);
                        Toast.makeText(getApplicationContext(), "Gallery Image Path: " + imagePath, Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean allPermissionsGranted(){ //Checks for necessary permissions
        for(String permission: REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
    private File createImageFile() throws IOException {
        // Creates a File object for image storage
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //Name is created based on the time, i.e. JPEG_20221204_053130_.jpg
        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        // Set the imagePath String to the File's location
        imagePath = image.getAbsolutePath();
        return image;
    }

    public static String getPath(Context context, Uri uri ) {//Returns a String of the given image's path as saved on the phone
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }
    public static Bitmap rotatePhoto(String photoPath, Bitmap bitmap) throws IOException { //Rotates the photo depending on the orientation it was taken in
        ExifInterface ei = new ExifInterface(photoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        //Checks the metadata for the orientation and then rotates it accordingly
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

    public static Bitmap rotateImage(Bitmap source, float angle) { //Function for image rotation
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

}