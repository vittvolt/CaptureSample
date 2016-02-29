package wu.th.camerapreview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Policy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.jar.Manifest;

import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnTouchListener, SurfaceHolder.Callback{
    static String TAG = "Main";
    String mCurrentPhotoPath;

    SurfaceView view, rect;
    Button button01;
    private Camera mCamera;

    int left, top, right, bottom;
    int pic_object_x, pic_object_y, pic_object_width, pic_object_height;
    int canvas_width, canvas_height;

    Paint paint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3);

        button01 = (Button) findViewById(R.id.button01);
        button01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    public void run() {
                        TakePhoto_And_Generate_Info();
                    }
                }.start();
            }
        });

        view = (SurfaceView) findViewById(R.id.surface_view);
        view.getHolder().addCallback(this);

        rect = (SurfaceView) findViewById(R.id.rectangle);
        rect.setOnTouchListener(this);
        rect.getHolder().setFormat(PixelFormat.TRANSPARENT);
        rect.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Canvas canvas = rect.getHolder().lockCanvas();

                canvas_width = canvas.getWidth();
                canvas_height = canvas.getHeight();

                canvas.drawRect(canvas.getWidth()/4, canvas.getHeight()/4, canvas.getWidth()*3/4, canvas.getHeight()*3/4, paint);
                rect.getHolder().unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = getCameraInstance();
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mCamera != null)
            mCamera.release();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (view.getHolder().getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(view.getHolder());
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
            c.setDisplayOrientation(90);
            Camera.Parameters p = c.getParameters();
            p.setRotation(90);
            p.setPictureSize(640, 480);
            c.setParameters(p);
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    /*private static int findFrontFacingCamera() {
        int cameraId = 0;

        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    } */

    public boolean onTouch(View v, MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if (event.getAction() == MotionEvent.ACTION_DOWN){
            left = (int) event.getX();
            top = (int) event.getY();
        }
        else if(event.getAction() == MotionEvent.ACTION_UP){
            right = (int) event.getX();
            bottom = (int) event.getY();

            Canvas canvas = rect.getHolder().lockCanvas();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);

            canvas.drawRect(left, top, right, bottom, paint);
            rect.getHolder().unlockCanvasAndPost(canvas);

            pic_object_x = left * 480 / view.getWidth();
            pic_object_y = top * 640 / view.getHeight();
            pic_object_width = (right - left) * 480 / view.getWidth();
            pic_object_height = (bottom - top) * 640 / view.getHeight();
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE){
            right = (int) event.getX();
            bottom = (int) event.getY();

            Canvas canvas = rect.getHolder().lockCanvas();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);

            canvas.drawRect(left, top, right, bottom, paint);
            rect.getHolder().unlockCanvasAndPost(canvas);
        }
        return true;
    }

    /*private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory(), "Positive Samples");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, 1);
            }
        }
    } */

    public void TakePhoto_And_Generate_Info(){
        Camera.PictureCallback pictureCB = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera cam) {
                File picFile = Get_Output_Directory();
                if (picFile == null) {
                    Log.e(TAG, "Couldn't create media file; check storage permissions?");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(picFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found: " + e.getMessage());
                    e.getStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "I/O error writing file: " + e.getMessage());
                    e.getStackTrace();
                }

                //Avoid freezing
                mCamera.startPreview();
            }
        };
        mCamera.takePicture(null, null, pictureCB);
    }

    public File Get_Output_Directory(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory(), "Positive Samples/pos_img");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = null;

        try {
            image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //Write to the data file
        generateInfoOnSD("pos_img.txt ", "img\\" + imageFileName + " 1 " + String.valueOf(pic_object_x) + " " + String.valueOf(pic_object_y) + " " + String.valueOf(pic_object_width) + " " + String.valueOf(pic_object_height) + "\n");

        return image;
    }

    public void generateInfoOnSD(String sFileName, String content){
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), "Positive Samples");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile, true);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
