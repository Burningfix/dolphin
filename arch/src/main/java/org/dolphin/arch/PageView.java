package org.dolphin.arch;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.LinkedList;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(null != savedInstanceState) {
            // 尝试恢复pageModel
            pageModel = savedInstanceState.getParcelable(RESTORE_MODEL_KAY);

            //尝试恢复pageViewModel
            ArrayList<PageViewModel> parcelableArrayList = (ArrayList<PageViewModel>) savedInstanceState.getSerializable(RESTORE_MODEL_VIEW_KAY);
            if(null != parcelableArrayList) {
                pageViewModels.addAll(parcelableArrayList);
            }
        }

        if(null == pageModel) {
            pageModel = createModel();
        }
        pageModel.start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        pageModel.stop();
        outState.putParcelable(RESTORE_MODEL_KAY, pageModel);
        outState.putSerializable(RESTORE_MODEL_VIEW_KAY, pageViewModels);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    public abstract <T extends PageModel> T createModel();

    /**
     * android view的data binding， 当viewModel改变是view会自动更新；view更新时viewModel也会自动更新
     * @param view 需要绑定的view
     * @param viewModel 需要被绑定的数据
     * @param <T> ViewModel的数据类型
     */
    public abstract <T extends PageViewModel> void bindViewModel(View view, T viewModel);

    /**
     * 当有新的viewModel创建时调用此方法
     * @param viewModel
     */
    public abstract void  updateViewModel(PageViewModel viewModel);

}
