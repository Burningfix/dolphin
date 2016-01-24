package org.dolphin.secret.browser;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yananh on 2016/1/23.
 */
public class FilePage extends Fragment {
    public enum State {
        Normal,
        Selcteable,
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private State state = State.Normal;

    public void setState(State state) {
        this.state = state;
        notifyStateChange();
    }

    public void notifyStateChange() {

    }
}
