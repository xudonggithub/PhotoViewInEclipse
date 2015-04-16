/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 *******************************************************************************/
package uk.co.senab.photoview.sample;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnMatrixChangedListener;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Add a custom view which can be a  complicated view group defined by yourself above the photo view. 
 * This view is scaled and moved  in the same way as the photo view. 
 * 
 * 
 * @author xdchen chen
 *
 */
public class SimpleSampleActivity extends Activity {

    static final String PHOTO_TAP_TOAST_STRING = "Photo Tap! X: %.2f %% Y:%.2f %% ID: %d";
    static final String SCALE_TOAST_STRING = "Scaled to: %.2ff";

    private TextView mCurrMatrixTv;

    private PhotoViewAttacher mAttacher;

    private Toast mCurrentToast;

    private Matrix mCurrentDisplayMatrix = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView mImageView = (ImageView) findViewById(R.id.iv_photo);
        mCurrMatrixTv = (TextView) findViewById(R.id.tv_current_matrix);

        Drawable bitmap = getResources().getDrawable(R.drawable.wallpaper);
//        mImageView.setImageDrawable(bitmap);
        String path = "file://"+Environment.getExternalStorageDirectory().getPath().concat("/DCIM/HDR/src_img/jpg/IMG_20150122154915_1_1280x720.jpg");
        mImageView.setImageURI(Uri.parse(path));

        // The MAGIC happens here!
        mAttacher = new PhotoViewAttacher(mImageView);

        // Lets attach some listeners, not required though!
        mAttacher.setOnMatrixChangeListener(new MatrixChangeListener());
        mAttacher.setOnPhotoTapListener(new PhotoTapListener());
        initCustomView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Need to call clean-up
        mAttacher.cleanup();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem zoomToggle = menu.findItem(R.id.menu_zoom_toggle);
        assert null != zoomToggle;
        zoomToggle.setTitle(mAttacher.canZoom() ? R.string.menu_zoom_disable : R.string.menu_zoom_enable);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_zoom_toggle:
                mAttacher.setZoomable(!mAttacher.canZoom());
                return true;

            case R.id.menu_scale_fit_center:
                mAttacher.setScaleType(ScaleType.FIT_CENTER);
                return true;

            case R.id.menu_scale_fit_start:
                mAttacher.setScaleType(ScaleType.FIT_START);
                return true;

            case R.id.menu_scale_fit_end:
                mAttacher.setScaleType(ScaleType.FIT_END);
                return true;

            case R.id.menu_scale_fit_xy:
                mAttacher.setScaleType(ScaleType.FIT_XY);
                return true;

            case R.id.menu_scale_scale_center:
                mAttacher.setScaleType(ScaleType.CENTER);
                return true;

            case R.id.menu_scale_scale_center_crop:
                mAttacher.setScaleType(ScaleType.CENTER_CROP);
                return true;

            case R.id.menu_scale_scale_center_inside:
                mAttacher.setScaleType(ScaleType.CENTER_INSIDE);
                return true;

            case R.id.menu_scale_random_animate:
            case R.id.menu_scale_random:
                Random r = new Random();

                float minScale = mAttacher.getMinimumScale();
                float maxScale = mAttacher.getMaximumScale();
                float randomScale = minScale + (r.nextFloat() * (maxScale - minScale));
                mAttacher.setScale(randomScale, item.getItemId() == R.id.menu_scale_random_animate);

                showToast(String.format(SCALE_TOAST_STRING, randomScale));

                return true;
            case R.id.menu_matrix_restore:
                if (mCurrentDisplayMatrix == null)
                    showToast("You need to capture display matrix first");
                else
                    mAttacher.setDisplayMatrix(mCurrentDisplayMatrix);
                return true;
            case R.id.menu_matrix_capture:
                mCurrentDisplayMatrix = mAttacher.getDisplayMatrix();
                return true;
            case R.id.extract_visible_bitmap:
                try {
                    Bitmap bmp = mAttacher.getVisibleRectangleBitmap();
                    File tmpFile = File.createTempFile("photoview", ".png",
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                    FileOutputStream out = new FileOutputStream(tmpFile);
                    bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.close();
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/png");
                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tmpFile));
                    startActivity(share);
                    Toast.makeText(this, String.format("Extracted into: %s", tmpFile.getAbsolutePath()), Toast.LENGTH_SHORT).show();
                } catch (Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(this, "Error occured while extracting bitmap", Toast.LENGTH_SHORT).show();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initCustomView() {
    	mPaint.setColor(Color.RED);
    	mPaint.setStrokeWidth(5);
    	mPaint.setStyle(Style.STROKE);
    	mCustomeLayout = (RelativeLayout) findViewById(R.id.customLayout);
    	mView = addCustomView();  
    	mCustomeLayout.addView(mView);
    	DisplayMetrics dm = getResources().getDisplayMetrics();
    	if(dm.widthPixels > dm.heightPixels)
    		mScreenRect.set(0, 0, dm.widthPixels, dm.heightPixels);
    	else
    		mScreenRect.set(0, 0, dm.heightPixels, dm.widthPixels);
    }
    View mView;
    RelativeLayout mCustomeLayout;
    Paint mPaint = new Paint();
    RectF mRect = new RectF(340,136,615,614);
    protected View addCustomView() {
    	View view = new View(this) {
    		@Override
    		protected void onDraw(Canvas canvas) {
//    			canvas.rotate(30, canvas.getWidth()/2, canvas.getWidth()/2);
//    			canvas.translate(0, 200);
    			RectF dstRect = new RectF();
//    			mMatrix.mapRect(dstRect, mRect);
//    			canvas.setMatrix(mMatrix);
    			canvas.drawRect(mRect, mPaint);
    		}
    	};
    	return view;
    }
    private class PhotoTapListener implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(View view, float x, float y) {
            float xPercentage = x * 100f;
            float yPercentage = y * 100f;

            showToast(String.format(PHOTO_TAP_TOAST_STRING, xPercentage, yPercentage, view == null ? 0 : view.getId()));
        }
    }

    private void showToast(CharSequence text) {
        if (null != mCurrentToast) {
            mCurrentToast.cancel();
        }

        mCurrentToast = Toast.makeText(SimpleSampleActivity.this, text, Toast.LENGTH_SHORT);
        mCurrentToast.show();
    }

    private  Matrix mMatrix = new Matrix();
    private  RectF mScreenRect = new RectF();
    private float fromX = 1;
    private float fromY = 1;
    private class MatrixChangeListener implements OnMatrixChangedListener {

        @Override
        public void onMatrixChanged(RectF rect, Matrix matrix) {
            mCurrMatrixTv.setText(rect.toString());
            mMatrix.reset();
            mMatrix.setRectToRect(mScreenRect, rect, ScaleToFit.START);
            
            float[] values = new float[9];
            mMatrix.getValues(values);
            System.out.println("cxd transX="+values[Matrix.MTRANS_X]+", transY="+values[Matrix.MTRANS_Y]
            		+",scaleX="+values[Matrix.MSCALE_X]+", scaleY="+values[Matrix.MSCALE_Y]);

            if(mView != null) {
	        	MatrixScaleAnimation scaleAnim = new MatrixScaleAnimation(fromX, values[Matrix.MSCALE_X], fromY, values[Matrix.MSCALE_Y],
	        			Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
	        	fromX = values[Matrix.MSCALE_X];
	        	fromY =  values[Matrix.MSCALE_Y];
	        	scaleAnim.setMatrix(mMatrix);
	        	scaleAnim.setFillAfter(true);
	        	mView.startAnimation(scaleAnim);
	            mView.invalidate();
            }
        }
    }
    
    static class MatrixScaleAnimation extends ScaleAnimation {

    	public MatrixScaleAnimation(float fromX, float toX, float fromY,
				float toY, int pivotXType, float pivotXValue, int pivotYType,
				float pivotYValue) {
			super(fromX, toX, fromY, toY, pivotXType, pivotXValue, pivotYType, pivotYValue);
			// TODO Auto-generated constructor stub
		}

		private Matrix mMatrix = null;
		
		public void setMatrix(Matrix matrix) {
			mMatrix = matrix;
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			if(mMatrix != null)
				t.getMatrix().set(mMatrix);
			else
				super.applyTransformation(interpolatedTime, t);
		}
    	
    }

}
