package org.dolphin.arch;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanyanan on 2015/12/4.
 */
public abstract class PageView extends Fragment {
    public static final String RESTORE_KAY = "!@@!_restore_!##!";
    public static final String RESTORE_MODEL_KAY = RESTORE_KAY + "_model";
    public static final String RESTORE_MODEL_VIEW_KAY = RESTORE_KAY + "_model_view";
    /**
     * 与该page绑定的model，需要能存储和恢复,只保存状态，不保存数据
     */
    private PageModel pageModel = null;
    /**
     * 与该page绑定的所有的pageViewModel，需要能存储和恢复
     */
    private LinkedList<PageViewModel> pageViewModels = new LinkedList<PageViewModel>();

    private WeakReference<View> rootViewRef = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModels.addAll(getPageViewModels(savedInstanceState));
        pageModel = getPageModel(savedInstanceState);
        pageModel.start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        pageModel.stop();
        outState.putParcelable(RESTORE_MODEL_KAY, pageModel);
        outState.putSerializable(RESTORE_MODEL_VIEW_KAY, pageViewModels);
    }

    public LinkedList<PageViewModel> getPageViewModels() {
        return new LinkedList<PageViewModel>(pageViewModels);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = createView(inflater, container, savedInstanceState);
        rootViewRef = new WeakReference<View>(rootView);
        LinkedList<PageViewModel> currentViewPages = getPageViewModels();
        for (PageViewModel pageViewModel : currentViewPages) {
            bindViewModel(pageViewModel);
        }
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        pageModel.stop();
        super.onDestroy();
    }

    /**
     * 尝试恢复已经存在的pageViewModel
     */
    private ArrayList<PageViewModel> getPageViewModels(Bundle savedInstanceState) {
        ArrayList<PageViewModel> res = new ArrayList<PageViewModel>();
        if (null != savedInstanceState) {
            List<PageViewModel> parcelableArrayList = (List<PageViewModel>)savedInstanceState.getSerializable(RESTORE_MODEL_VIEW_KAY);
            if (null != parcelableArrayList) {
                res.addAll(parcelableArrayList);
            }
        }
        return res;
    }

    private PageModel getPageModel(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            // 尝试恢复pageModel
            pageModel = savedInstanceState.getParcelable(RESTORE_MODEL_KAY);
        }

        if (pageModel == null) {
            // 创建新的pageModel
            pageModel = createPageModel();
        }
        pageModel.setBindPageView(this);
        return pageModel;
    }

    /**
     * 当有新的viewModel创建时调用此方法
     *
     * @param viewModel
     */
    public void foundViewModel(PageViewModel viewModel) {
        pageViewModels.add(viewModel);
        bindViewModel(viewModel);
    }

    public abstract <T extends PageModel> T createPageModel();

    /**
     * android view的data binding， 当viewModel改变是view会自动更新；view更新时viewModel也会自动更新
     *
     * @param view      需要绑定的view
     * @param viewModel 需要被绑定的数据
     * @param <T>       ViewModel的数据类型
     */
    public abstract <T extends PageViewModel> void bindViewModel(View view, T viewModel);


    @Nullable
    public abstract View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);


    /**
     * 将指定的ViewModel与当前的绑定
     */
    public abstract void bindViewModel(PageViewModel viewModel);
}
