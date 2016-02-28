package wu.th.camerapreview;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.jar.Manifest;

import android.util.Log;
import android.view.View;

public class MainActivity extends Activity implements View.OnTouchListener, SurfaceHolder.Callback{
    static String TAG = "Main";

    SurfaceView view, rect;
    private Camera mCamera;

    int left, top, right, bottom;
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
}
