package com.example.vegetable;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;

import com.example.vegetable.ml.Model;
import com.example.vegetable.ml.ModelUnquant;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.vegetable.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //declare
    android.widget.Button bupload, bcam;
    FloatingActionButton logoutBtn;
    ImageView rimg;
    TextView ttype, rtype, tcond, rcond, tcolour, rcolour, imgtv;
    int SELECT_PICTURE = 200;
    int CAMERA_PIC_REQUEST = 100;
    FirebaseAuth mAuth;
    private int imageSize = 224;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FIREBASE
        mAuth = FirebaseAuth.getInstance();


        //link to items in page
        bupload = findViewById(R.id.uploadimgbtn);
        bcam = findViewById(R.id.accesscambtn);
        rimg = findViewById(R.id.resultimg);
        ttype = findViewById(R.id.type);
        rtype = findViewById(R.id.typeresult);
        tcond = findViewById(R.id.condi);
        rcond = findViewById(R.id.condiresult);
        tcolour = findViewById(R.id.colour);
        rcolour = findViewById(R.id.colourresult);
        imgtv = findViewById(R.id.imgtv);
        logoutBtn = findViewById(R.id.logoutBtn);

        //upload img
        bupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        //access cam
        bcam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePic();
            }
        });

        if (rimg.getDrawable() == null){
            imgtv.setVisibility(View.VISIBLE);
        } else imgtv.setVisibility(View.GONE);

        /*handle to logout*/
        logoutBtn.setOnClickListener(v ->{
            showDialog();
        });
    }

    //when upload img btn clicked
    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    //dialog signout
    public void showDialog() {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.logoutdialog, null);
        builder.setView(customLayout);
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        MaterialButton yes = customLayout.findViewById(R.id.yesBtn);
        yes.setOnClickListener(v -> {
            if (mAuth != null){
                mAuth.signOut();
                dialog.dismiss();
                finish();
            } else Toast.makeText(MainActivity.this, "Please try again.", Toast.LENGTH_SHORT).show();

        });
        MaterialButton no = customLayout.findViewById(R.id.NObTN);
        no.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //when user choose img from gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    Bitmap image = null;
                    try {
                        image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        rimg.setImageBitmap(image);
                        int dimension = Math.min(image.getWidth(), image.getHeight());
                        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                        classifyImage(image);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
            //display taken pic in imgview
            else if (requestCode == CAMERA_PIC_REQUEST) {
                Bitmap image = (Bitmap) (data.getExtras().get("data"));
                //image = Bitmap.createScaledBitmap(image, rimg.getWidth(),rimg.getHeight(),true);
                rimg.setImageBitmap(image);
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }



            rimg.setVisibility(View.VISIBLE);
            ttype.setVisibility(View.VISIBLE);
            rtype.setVisibility(View.VISIBLE);
            tcond.setVisibility(View.VISIBLE);
            rcond.setVisibility(View.VISIBLE);
            tcolour.setVisibility(View.VISIBLE);
            rcolour.setVisibility(View.VISIBLE);
            if (rimg.getDrawable() == null){
                imgtv.setVisibility(View.VISIBLE);
            } else imgtv.setVisibility(View.GONE);
        }
    }

    //when access cam btn clicked
    void takePic() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    public void classifyImage(Bitmap image){
        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());



            //for uint8

        /*    Bitmap input=Bitmap.createScaledBitmap(image,224,224,true);
            TensorImage imaget=new TensorImage(DataType.UINT8);
            imaget.load(input);
            ByteBuffer byteBuffers = imaget.getBuffer();

            inputFeature0.loadBuffer(byteBuffers);*/
            // get 1D array of 224 * 224 pixels in image
            int [] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
            int pixel = 0;
            for(int i = 0; i < imageSize; i++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.

            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            Log.d("classification", String.valueOf(maxPos)+" -- "+ Arrays.toString(confidences));
            String[] classes = {"Unripe", "Ripe", "Overripe", "No Banana"};

            float percentage = (float)maxConfidence*100.0f;

            //banana ripeness
            rcond.setText(classes[maxPos]);

            String s = "";
            for(int i = 0; i < classes.length; i++){
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
            }
            //percentage
            rcolour.setText(s);


            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }
}