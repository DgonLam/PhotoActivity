package com.dgonlam.photoactivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dgonlam.library.DgonlamCameraActivity;

public class MainActivity extends AppCompatActivity {

    private static String filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)+"/"+System.currentTimeMillis()+".jpeg";
    }

    public void takePhoto(View view){
        Intent intent = new Intent(this, DgonlamCameraActivity.class);
        intent.putExtra(DgonlamCameraActivity.strFilePath,filePath);
        intent.putExtra(DgonlamCameraActivity.mode,DgonlamCameraActivity.mode_other);
        intent.putExtra(DgonlamCameraActivity.btn_pic,R.drawable.shape_r50_black);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int reqCode,int resultCode,Intent data){
        if (reqCode == 100 && resultCode == RESULT_OK){
            Log.d("long","success:"+filePath);
            ((ImageView)findViewById(R.id.main_iv_photo)).setImageBitmap(BitmapFactory.decodeFile(filePath));
        }else  if (reqCode == 100 && resultCode == DgonlamCameraActivity.RESULT_ERROR) {
            Toast.makeText(this, data.getExtras().getString(DgonlamCameraActivity.result), Toast.LENGTH_SHORT).show();
        }
    }
}
