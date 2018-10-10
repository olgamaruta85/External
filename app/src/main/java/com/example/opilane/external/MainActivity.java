package com.example.opilane.external;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText txtFileName, txtMessage;
    private TextView txtViewMessages;
    private ListView lstFiles;
    ArrayList<String> filepath = new ArrayList<>();
    ArrayAdapter <String> listAdapter;
    String line = null;

    private static final int REQUEST_CODE_PERMISSION =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtFileName = findViewById(R.id.etFileName);
        txtMessage = findViewById(R.id.etMessage);
        txtViewMessages = findViewById(R.id.txtViewMessage);
        lstFiles = findViewById(R.id.lstFiles);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filepath);
        lstFiles.setAdapter(listAdapter);

        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String []{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_PERMISSION );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_CODE_PERMISSION){
            int grantResultsLength=grantResults.length;
            if (grantResultsLength > 0 && grantResults [0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "You granted write external storage permission",
                        Toast.LENGTH_LONG).show();
            } else Toast.makeText(getApplicationContext(), "You denied write external storage permission",
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean isExternalStorageWritable(){
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.i("State", "Writable");
            return true;
        }else return false;
    }

    private boolean isExternalStorageReadable (){
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())){
            Log.i("State", "Readable");
            return true;
        }else return false;
    }

    public void onSave(View view) {
        if(isExternalStorageWritable()){
            File textFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    txtFileName.getText().toString());
            try{
                FileOutputStream fileOutputStream = new FileOutputStream(textFile);
                fileOutputStream.write(txtMessage.getText().toString().getBytes());
                fileOutputStream.close();
                txtFileName.setText("");
                txtMessage.setText("");
                Toast.makeText(this, "File saved", Toast.LENGTH_LONG).show();
            }catch (IOException ex){ex.printStackTrace(); }
        }else Toast.makeText(this, "External storage is not mounted", Toast.LENGTH_LONG).show();
    }


    public void onShow(View view) {
        if (isExternalStorageReadable()){
            StringBuilder stringBuilder =new StringBuilder();
            try {
                File textFile= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        txtFileName.getText().toString());
                FileInputStream fileInputStream = new FileInputStream(textFile);
                if (fileInputStream != null){
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    while ((line=bufferedReader.readLine()) != null){
                        stringBuilder.append(line + "\n");
                    }
                    fileInputStream.close();
                    txtFileName.setText("");
                    txtViewMessages.setText(stringBuilder);
                }
            }catch (IOException ex){ex.printStackTrace();}
        }else Toast.makeText(this, "Cannot read from external storage", Toast.LENGTH_SHORT).show();
    }

    public  void ShowDirectoryFilesInList(File externalDirectory){
        File listFile []= externalDirectory.listFiles();
        if (listFile != null){
            for (int i=0; i<listFile.length; i++){
                if (listFile[i].isDirectory()){
                    ShowDirectoryFilesInList(listFile[i]);
                }else filepath.add(listFile[i].getAbsolutePath());
            }
        }
    }
}
