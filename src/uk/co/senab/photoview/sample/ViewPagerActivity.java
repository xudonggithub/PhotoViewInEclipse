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
 *******************************************************************************/
package uk.co.senab.photoview.sample;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnMatrixChangedListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

/** 
 * Lock/Unlock button is added to the ActionBar.
 * Use it to temporarily disable ViewPager navigation in order to correctly interact with ImageView by gestures.
 * Lock/Unlock state of ViewPager is saved and restored on configuration changes.
 * 
 * Julia Zudikova
 */

/**
 * Add a custom view which can be a  complicated view group defined by yourself above every page view. 
 * This view is scaled and moved  in the same way as the page view. 
 * 
 * 
 * @author xdchen chen
 *
 */
public class ViewPagerActivity extends Activity {

	private static final String ISLOCKED_ARG = "isLocked";
	
	private ViewPager mViewPager;
	private MenuItem menuLockItem;
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
		setContentView(mViewPager);

		mViewPager.setAdapter(new SamplePagerAdapter(this));
		
		if (savedInstanceState != null) {
			boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
			((HackyViewPager) mViewPager).setLocked(isLocked);
		}
		initCustomView();
	}
    
    private Paint mPaint = new Paint();
    private  RectF mScreenRect = new RectF();
    private void initCustomView() {
    	
    	mPaint.setColor(Color.RED);
    	mPaint.setStrokeWidth(5);
    	mPaint.setStyle(Style.STROKE);
    	DisplayMetrics dm = getResources().getDisplayMetrics();
    	if(dm.widthPixels > dm.heightPixels)
    		mScreenRect.set(0, 0, dm.widthPixels, dm.heightPixels);
    	else
    		mScreenRect.set(0, 0, dm.heightPixels, dm.widthPixels);
    }
  
    
    protected View addCustomView(final int index) {
		View view = new View(this) {
			@Override
			protected void onDraw(Canvas canvas) {
				if(index % 2 == 1)
					mPaint.setColor(Color.BLUE);
				else
					mPaint.setColor(Color.RED);
				canvas.drawRect(mRectArr[index], mPaint);
			}
		};
		return view;
		
	}
    private RectF mRect = new RectF(340,136,615,614);
    private static final RectF[] mRectArr = {
    	new RectF(40,36,615,614), new RectF(340,136,615,614),
    	new RectF(40,36,615,614), new RectF(340,136,615,614),
    	new RectF(40,36,615,614), new RectF(340,136,615,614),
    };
	private static final int[] sDrawables = { R.drawable.wallpaper, R.drawable.wallpaper, R.drawable.wallpaper,
		R.drawable.wallpaper, R.drawable.wallpaper, R.drawable.wallpaper };
	class SamplePagerAdapter extends PagerAdapter {

		private Context mContext ;
//		private View mCurrentView;
		  
		@Override
		public int getCount() {
			return sDrawables.length;
		}
		public SamplePagerAdapter(Context context) {
			mContext = context;
			
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			View view = inflater.inflate(R.layout.viewpagecontent, null);

			PhotoView photoView = (PhotoView) view.findViewById(R.id.iv_photo);
			photoView.setImageResource(sDrawables[position]);
			container.addView(view,  LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);

			RelativeLayout mCustomeLayout = (RelativeLayout) view
					.findViewById(R.id.customLayout);
			View customView = addCustomView(position);
			mCustomeLayout.addView(customView);
			
			PhotoViewAttacher mAttacher = (PhotoViewAttacher) photoView
					.getIPhotoViewImplementation();
			mAttacher.setOnMatrixChangeListener(new MatrixChangeListener(customView));
			return view;

		}
		
//		@Override
//		public void setPrimaryItem(ViewGroup container, int position,
//				Object object) {
//			super.setPrimaryItem(container, position, object);
//			System.out.println("......cxd, setPrimaryItem postion:"+position);
//			mCurrentView = (View) object;
//		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

//		public View getCurrentView() {
//			return mCurrentView;
//		}
		

		private class MatrixChangeListener implements OnMatrixChangedListener {
			private View mCustomView;
			private Matrix mMatrix = new Matrix();
			private float fromX = 1;
			private float fromY = 1;
			
			public MatrixChangeListener(View view) {
				mCustomView = view;
			}
			@Override
			public void onMatrixChanged(RectF rect, Matrix matrix) {
				if(mCustomView == null)
					return;
				mMatrix.reset();
				mMatrix.setRectToRect(mScreenRect, rect, ScaleToFit.START);

				float[] values = new float[9];
				mMatrix.getValues(values);
				System.out.println("cxd transX=" + values[Matrix.MTRANS_X]
						+ ", transY=" + values[Matrix.MTRANS_Y] + ",scaleX="
						+ values[Matrix.MSCALE_X] + ", scaleY="
						+ values[Matrix.MSCALE_Y]);

				MatrixScaleAnimation scaleAnim = new MatrixScaleAnimation(
						fromX, values[Matrix.MSCALE_X], fromY,
						values[Matrix.MSCALE_Y], Animation.RELATIVE_TO_SELF,
						0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				fromX = values[Matrix.MSCALE_X];
				fromY = values[Matrix.MSCALE_Y];
				scaleAnim.setMatrix(mMatrix);
				scaleAnim.setFillAfter(true);
				
				mCustomView.startAnimation(scaleAnim);
				mCustomView.invalidate();
			}
		}
	}
	
	

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewpager_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuLockItem = menu.findItem(R.id.menu_lock);
        toggleLockBtnTitle();
        menuLockItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				toggleViewPagerScrolling();
				toggleLockBtnTitle();
				return true;
			}
		});

        return super.onPrepareOptionsMenu(menu);
    }
    
    private void toggleViewPagerScrolling() {
    	if (isViewPagerActive()) {
    		((HackyViewPager) mViewPager).toggleLock();
    	}
    }
    
    private void toggleLockBtnTitle() {
    	boolean isLocked = false;
    	if (isViewPagerActive()) {
    		isLocked = ((HackyViewPager) mViewPager).isLocked();
    	}
    	String title = (isLocked) ? getString(R.string.menu_unlock) : getString(R.string.menu_lock);
    	if (menuLockItem != null) {
    		menuLockItem.setTitle(title);
    	}
    }

    private boolean isViewPagerActive() {
    	return (mViewPager != null && mViewPager instanceof HackyViewPager);
    }
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (isViewPagerActive()) {
			outState.putBoolean(ISLOCKED_ARG,
					((HackyViewPager) mViewPager).isLocked());
		}
		super.onSaveInstanceState(outState);
	}

	class MatrixScaleAnimation extends ScaleAnimation {

		public MatrixScaleAnimation(float fromX, float toX, float fromY,
				float toY, int pivotXType, float pivotXValue, int pivotYType,
				float pivotYValue) {
			super(fromX, toX, fromY, toY, pivotXType, pivotXValue, pivotYType,
					pivotYValue);
			// TODO Auto-generated constructor stub
		}

		private Matrix mMatrix = null;

		public void setMatrix(Matrix matrix) {
			mMatrix = matrix;
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			if (mMatrix != null)
				t.getMatrix().set(mMatrix);
			else
				super.applyTransformation(interpolatedTime, t);
		}

	} 
}
