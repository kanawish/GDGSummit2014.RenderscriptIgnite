package com.gdg.montreal.android.renderscript.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.android.rs.image.ScriptC_vignette_full;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @InjectView(R.id.leftHandImageView)
    ImageView leftHandImageView;

    @InjectView(R.id.rightHandImageView)
    ImageView rightHandImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // see http://jakewharton.github.io/butterknife/
        ButterKnife.inject(this);

        ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Bitmap rhSourceBitmap = decodeScaledBitmapResource(rightHandImageView,R.drawable.vespa_right);
                // This function will blur the source bitmap.
                Bitmap rhOutputBitmap = blurBitmap(rhSourceBitmap);
                // Assign the blurred bitmap to the image.
                rightHandImageView.setImageBitmap(rhOutputBitmap);

                // Only run this once.
                rightHandImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        };

        // Assumption is both left/right images have been layed-out when this will trigger.
        rightHandImageView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function decodes the bitmap to fit the target image size, thus saving memory.
     *
     * @param target we need an ImageView that has been "layed-out".
     * @param resourceId the bitmap resource to decode
     * @return the decoded resource, pixel-scaled to fit the target image.
     */
    private Bitmap decodeScaledBitmapResource( ImageView target, int resourceId ) {
        // Get the dimensions of the target View
        int targetW = target.getWidth();
        int targetH = target.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(this.getResources(), resourceId, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image.
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), resourceId);
        return bitmap ;
    }

    private Bitmap blurBitmap(Bitmap sourceBitmap) {
        // First, we need a renderscript context.
        RenderScript rs = RenderScript.create(MainActivity.this);

        // We create an empty bitmap matching the source bitmap size and configuration.
        Bitmap outputBitmap = Bitmap.createBitmap(
                sourceBitmap.getWidth(),
                sourceBitmap.getHeight(),
                sourceBitmap.getConfig());

        // Allocations are the primary method through which we pass data to and from a Renderscript 'kernel'.
        // (More on this later)
        Allocation tmpIn = Allocation.createFromBitmap(rs, sourceBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);


        // We have a pick of 9 different renderscript "intrinsics", here we pick a blur.
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        // Sets the desired blur radius.
        theIntrinsic.setRadius(24.f);
        // Sets the input of the blur.
        theIntrinsic.setInput(tmpIn);
        // Applies the filter to the input, and saves it to the tmpOut Allocation.
        theIntrinsic.forEach(tmpOut);


        // Small safety measure against memory issues that can creep up when manipulating bitmaps.
        sourceBitmap.recycle();

        // Copy from allocation into the output bitmap.
        tmpOut.copyTo(outputBitmap);

        // And we're done!
        rs.destroy();

        return outputBitmap;
    }

    private Bitmap vignetteBitmap(Bitmap sourceBitmap) {
        // First, we need a renderscript context.
        RenderScript rs = RenderScript.create(MainActivity.this);

        // We create an empty bitmap matching the source bitmap size and configuration.
        Bitmap outputBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), sourceBitmap.getConfig());

        // Allocations are the primary method through which we pass data to and from a Renderscript 'kernel'.
        // (More on this later)
        Allocation tmpIn = Allocation.createFromBitmap(rs, sourceBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        // This instantiates the generated script. Naming convention is ScriptC_xyz
        ScriptC_vignette_full scriptVignette = new ScriptC_vignette_full(rs,getResources(),R.raw.vignette_full);

        // Vignette parameter assignments
        float center_x = 1f; float center_y = 0.5f;
        float scale = 0.8f;
        float shade = 0.8f; // 0.0f light -> 1.0f dark
        float slope = 5.0f; // Rate at which we swing from dark to light

        // Invoke the vignette script with parameters.
        scriptVignette.invoke_init_vignette(
                tmpIn.getType().getX(), tmpIn.getType().getY(),
                center_x, center_y, scale, shade, slope);

        // Execute
        scriptVignette.forEach_root(tmpIn, tmpOut);


        // Small safety measure against memory issues that can creep up when manipulating bitmaps.
        sourceBitmap.recycle();

        // Copy from allocation into the output bitmap.
        tmpOut.copyTo(outputBitmap);

        // And we're done!
        rs.destroy();

        return outputBitmap;
    }

}
