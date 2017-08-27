package com.himanshuc961gmail.snoredetector;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.Manifest.permission.RECORD_AUDIO;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity {


    int state=1;
    //byte[] data = new byte[16384];
    public static final int RequestPermissionCode = 1;
    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    private LineGraphSeries<DataPoint> series;
    private GetAudio getAudio;
    private final int maxPoints = 100000;
    int count =0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        series = new LineGraphSeries<>();
        series.appendData(new DataPoint(0, 0), true, maxPoints);
        GraphView graph = (GraphView)findViewById(R.id.graph);
        graph.addSeries(series);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(200);
        getAudio = new GetAudio();
        getAudio.execute("");
    }

    protected void onResume() {
        super.onResume();
        mTimer = new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream byteArrayOutputStream = getAudio.getByteArrayOutputStream();
                if (byteArrayOutputStream != null) {
                    int s= byteArrayOutputStream.size();
                    System.out.println(s);
                    int i=0;
                    byte [] arr = byteArrayOutputStream.toByteArray();
                    for(i=0;i<s;i++){
                        count++;
                        System.out.println(i);
                        series.appendData(new DataPoint(count,arr[i]), true, maxPoints);
                    }
                    byteArrayOutputStream.reset();
                }
                mHandler.postDelayed(this, 50);
            }
        };
        mHandler.postDelayed(mTimer, 1000);
    }

    protected void onPause() {
        mHandler.removeCallbacks(mTimer);
        super.onPause();
    }

    public void start(View v){
        state = 1;




    }


    public void stop(View v){

        state = 0;

    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{RECORD_AUDIO}, RequestPermissionCode);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {

                    boolean RecordPermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;

                    if ( RecordPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted",Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }


    }
    class GetAudio extends AsyncTask<String, Void, String> {


        private ByteArrayOutputStream byteArrayOutputStream;
        private InputStream inputStream;
        private MediaRecorder mediaRecorder;

        ByteArrayOutputStream getByteArrayOutputStream() {
            return byteArrayOutputStream;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(checkPermission()) {

                MediaRecorderReady();




                Toast.makeText(MainActivity.this, "Recording started",
                        Toast.LENGTH_LONG).show();
            }
            else {
                requestPermission();
            }

        }

        @Override
        protected String doInBackground(String... params) {
            int read;
            byte[] data = new byte[16384];
            try {
                while ((read = inputStream.read(data, 0, data.length)) != -1) {
                    byteArrayOutputStream.write(data, 0, read);
                }
                System.out.println(byteArrayOutputStream);
                byteArrayOutputStream.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            MediaRecorderStop();
            super.onPostExecute(s);
        }
        public void MediaRecorderReady() {
            try {
                byteArrayOutputStream = new ByteArrayOutputStream();

                ParcelFileDescriptor[] descriptors = ParcelFileDescriptor.createPipe();
                ParcelFileDescriptor parcelRead = new ParcelFileDescriptor(descriptors[0]);
                ParcelFileDescriptor parcelWrite = new ParcelFileDescriptor(descriptors[1]);

                inputStream = new ParcelFileDescriptor.AutoCloseInputStream(parcelRead);

                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.setOutputFile(parcelWrite.getFileDescriptor());
                mediaRecorder.prepare();
                mediaRecorder.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }



        }
        public void MediaRecorderStop(){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder.reset();
        }
        public boolean checkPermission() {

            int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                    RECORD_AUDIO);
            return result1 == PackageManager.PERMISSION_GRANTED;
        }


    }




}




