package org.dolphin.arch;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by hanyanan on 2015/12/4.
 */
public abstract class PageView extends Fragment {
    public static final String RESTORE_KAY = "!@@!_restore_!##!";
    private final


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container);
    }

    /**
     * 创建整个页面的view
     * @param inflater
     * @param container
     * @return
     */
    public abstract View createView(LayoutInflater inflater, ViewGroup container);

    /**
     * android view的data binding， 当viewModel改变是view会自动更新；view更新时viewModel也会自动更新
     * @param view 需要绑定的view
     * @param viewModel 需要被绑定的数据
     * @param <T> ViewModel的数据类型
     */
    public abstract  void bindViewModel(View view, PageViewModel viewModel);




}
