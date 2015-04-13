package uk.co.senab.photoview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import uk.co.senab.photoview.PhotoView;

public class AUILSampleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        PhotoView photoView = (PhotoView) findViewById(R.id.iv_photo);

        if (!ImageLoader.getInstance().isInited()) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
            ImageLoader.getInstance().init(config);
        }

//        ImageLoader.getInstance().displayImage("http://pbs.twimg.com/media/Bist9mvIYAAeAyQ.jpg", photoView);
        String path = "file://"+Environment.getExternalStorageDirectory().getPath().concat("/DCIM/HDR/src_img/jpg/IMG_20150122154915_1_1280x720.jpg");
        ImageLoader.getInstance().displayImage(path, photoView);
    }
}
