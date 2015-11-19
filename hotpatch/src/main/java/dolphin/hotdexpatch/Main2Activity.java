package dolphin.hotdexpatch;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import org.dolphin.dexhotpatch.R;

public class Main2Activity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        try {
            Class<Fragment> clz = (Class<Fragment>) Class.forName("dolphin.hotdexpatch.plugin.Fragment1");
            Fragment fragment = clz.newInstance();
            getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
