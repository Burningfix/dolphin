package org.dolphin.secret.play;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import org.dolphin.secret.R;
import org.dolphin.secret.browser.BrowserManager;
import org.dolphin.secret.core.FileInfo;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

public class ImagePlayerActivity extends Activity {
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_browser);
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        setContentView(mViewPager);

        List<FileInfo> images = new ArrayList<FileInfo>(BrowserManager.getInstance().getImageFileList());
        int position = getIntent().getIntExtra("position", 0);
        mViewPager.setAdapter(new SamplePagerAdapter(images));
        mViewPager.setCurrentItem(position);
        mViewPager.setOffscreenPageLimit(3);
    }

    static class SamplePagerAdapter extends PagerAdapter {
        List<FileInfo> images;

        SamplePagerAdapter(List<FileInfo> images) {
            this.images = images;
        }

        @Override
        public int getCount() {
            return this.images.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            ZoomPhotoView photoView = new ZoomPhotoView(container.getContext());
            photoView.setFile();
            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
