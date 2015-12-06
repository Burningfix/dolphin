package dolphin.hotdexpatch;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.dolphin.dexhotpatch.R;
import org.dolphin.hotpatch.apk.ApkLoadEngine;
import org.dolphin.hotpatch.apk.ApkPlugin;
import org.dolphin.hotpatch.apk.ApkPluginDataChangeObserver;
import org.dolphin.hotpatch.apk.ApkPluginInterface;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.lib.ReflectUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements ApkPluginDataChangeObserver {
    public static final String TAG = "Plugin";
    private ApkLoadEngine apkLoadEngine;
    private File privateFile;
    private File optimizedFile;
    ListView listView;
    //    AssetManager mAssetManager = null;
    Resources mResources;
    String extraPath = null;


    public MainActivity() {
        super();
        MyPathClassLoader.printCurrClassLoader("MainActivity.MainActivity");
    }

//    @Override
//    public ClassLoader getClassLoader() {
//        Log.d(TAG, "MainActivity getClassLoader");
//        if(null == pluginClassLoader) return super.getClassLoader();
//        return pluginClassLoader;
//    }



//    @Override
//    protected void attachBaseContext(Context newBase) {
//        HostContentWrapper hostContentWrapper = new HostContentWrapper(newBase);
//        super.attachBaseContext(hostContentWrapper);
////        new Handler(getMainLooper()).post(new Runnable() {
////            @Override
////            public void run() {
////                displayLoadedApk();
////            }
////        });
//    }

    private void displayLoadedApk(){
        try {
            Field activityThreadField = ReflectUtil.findField(this, "mMainThread");
            Object activityThread = activityThreadField.get(this);
            Field mPackages = ReflectUtil.findField(activityThread, "mPackages");
            WeakReference<?> ref;
            Map<String, ?> map = (Map<String, ?>) mPackages.get(activityThread);
            ref = (WeakReference<? >) map.get(getPackageName()) ;
            Object apk = ref.get();
//            Class apkClass = apk.getClass();
//            Field  mClassLoader = ReflectUtil.findField( apkClass , "mClassLoader");
//            mClassLoader. set (apk , classLoader ) ;

            Log.d("ClassLoader", "MainActivity loadedApk " + apk);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Resources getResources() {
        if (null == mResources) return super.getResources();
        return mResources;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(null!= extraPath){
            outState.putString("extraPath", extraPath);
        }
    }



    //    @Override
//    public AssetManager getAssets() {
//        if(null == mAssetManager) return super.getAssets();
//        return mAssetManager;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(null != savedInstanceState) {
            extraPath = savedInstanceState.getString("extraPath");
        }
        MyPathClassLoader.printCurrClassLoader("MainActivity.onCreate");
        if(null != extraPath) {
            try {
                AssetManager assetManager = AssetManager.class.newInstance();
                Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
                addAssetPath.invoke(assetManager, extraPath);
                String localApkDir = getApplicationInfo().publicSourceDir;
                addAssetPath.invoke(assetManager, localApkDir);
                Resources superRes = getResources();
                mResources = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
//                        mTheme = mResources.newTheme();
//                        mTheme.setTo(super.getTheme());
                Toast.makeText(this, R.string.test, 3000).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        listView = (ListView) findViewById(R.id.list);
        privateFile = new File(this.getFilesDir(), "_private");
        optimizedFile = new File(privateFile, "_opt");
        apkLoadEngine = ApkLoadEngine.instance(this, privateFile, optimizedFile);
        apkLoadEngine.setApkPluginDataChangeObserver(this);
        String a = "ssss";
        ClassLoader normalClassLoader = a.getClass().getClassLoader();
        Log.d("Plugin","normalClassLoader " + normalClassLoader);
        Log.d("Plugin","SystemClassLoader " + ClassLoader.getSystemClassLoader());










        Log.d("Context", "Activity's getBaseContext  =  " + getBaseContext());
        Log.d("Context", "Activity's getApplicationContext  =  " + getApplicationContext());
        Log.d("Context", "Activity's ClassLoader =  " + getClassLoader());


        DelegateApplication.instance();
    }

    public void next(View view) {
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
    }

    public void onResume(){
        super.onResume();
    }

    @Override
    public void onApkPluginDataChanged(List<ApkPlugin> apkPluginList) {
        final PluginAdapter adapter = new PluginAdapter(build(apkPluginList));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TwoTuple<ApkPlugin, ApkPluginInterface.PageSpec> item = (TwoTuple<ApkPlugin, ApkPluginInterface.PageSpec>) adapter.getItem(position);

                try {
                    PackageInfo plocalObject = getPackageManager().getPackageArchiveInfo(item.value1.getPath(), 1);
                    MainActivity activity = MainActivity.this;

                    Log.d(TAG, "plugin " + plocalObject.packageName);

                    try {
                        AssetManager assetManager = AssetManager.class.newInstance();
                        Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
                        addAssetPath.invoke(assetManager, item.value1.getPath());
                        String localApkDir = activity.getApplicationInfo().publicSourceDir;
                        addAssetPath.invoke(assetManager, localApkDir);

//                        Method localMethod = ((AssetManager)AssetManager.class.newInstance()).getClass().getMethod("addAssetPath", new Class[] { String.class });
//                        localMethod.invoke(assetManager, new String[]{localApkDir, item.value1.getPath()});
//                        mAssetManager = assetManager;
                        Resources superRes = activity.getResources();
                        mResources = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
//                        mTheme = mResources.newTheme();
//                        mTheme.setTo(super.getTheme());
                        Toast.makeText(activity, R.string.test, 3000).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ClassLoader pluginClassLoader = null;
                    pluginClassLoader = item.value1.getClassLoader();
                    Class<Fragment> fragmentClass = (Class<Fragment>) pluginClassLoader.loadClass(item.value2.path());
                    Fragment fragment = fragmentClass.newInstance();
                    getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                    activity.extraPath = item.value1.getPath();



                    ClassLoader classLoader = activity.getClassLoader();
                    if(ClassLoaderWrapper.class.isInstance(classLoader)) {
                        ClassLoaderWrapper classLoaderWrapper = (ClassLoaderWrapper)classLoader;
                        classLoaderWrapper.pushExtraClassLoader(pluginClassLoader);
                    }


//                    if(plocalObject.activities!=null &&plocalObject.activities.length>0){
//                        String activityname = plocalObject.activities[0].name;
//                        Log.d(TAG, "activityname = "+ activityname);
//
//                        Class localClass = classLoader.loadClass(activityname);
//                        Constructor localConstructor = localClass
//                                .getConstructor(newClass[]{});
//                        Object instance = localConstructor.newInstance(newObject[]{});
//                        Log.d(TAG, "instance = "+ instance);
//
//                        Method localMethodSetActivity = localClass.getDeclaredMethod(
//                                "setActivity", newClass[]{ Activity.class});
//                        localMethodSetActivity.setAccessible(true);
//                        localMethodSetActivity.invoke(instance, newObject[]{this});
//
//                        Method methodonCreate = localClass.getDeclaredMethod(
//                                "onCreate", newClass[]{ Bundle.class});
//                        methodonCreate.setAccessible(true);
//                        methodonCreate.invoke(instance, newObject[]{ paramBundle });
//                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private List<TwoTuple<ApkPlugin, ApkPluginInterface.PageSpec>> build(List<ApkPlugin> apkPluginList) {
        if (null == apkPluginList || apkPluginList.isEmpty()) return null;
        List<TwoTuple<ApkPlugin, ApkPluginInterface.PageSpec>> res = new ArrayList<TwoTuple<ApkPlugin, ApkPluginInterface.PageSpec>>();
        for (ApkPlugin plugin : apkPluginList) {
            if (null == plugin.getPageList()) continue;
            for (ApkPluginInterface.PageSpec pageSpec : plugin.getPageList()) {
                res.add(new TwoTuple<ApkPlugin, ApkPluginInterface.PageSpec>(plugin, pageSpec));
            }
        }
        return res;
    }

    private class PluginAdapter extends BaseAdapter {
        private List<TwoTuple<ApkPlugin, ApkPluginInterface.PageSpec>> apkPlugins;

        PluginAdapter(List<TwoTuple<ApkPlugin, ApkPluginInterface.PageSpec>> apkPluginList) {
            apkPlugins = apkPluginList;
        }

        @Override
        public int getCount() {
            if (null == apkPlugins) return 0;
            return apkPlugins.size();
        }

        @Override
        public Object getItem(int position) {
            return apkPlugins.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TwoTuple<ApkPlugin, ApkPluginInterface.PageSpec> pluginPageSpecTwoTuple = (TwoTuple<ApkPlugin, ApkPluginInterface.PageSpec>) getItem(position);
            TextView tv = new TextView(MainActivity.this);
            tv.setText(pluginPageSpecTwoTuple.value1.getName() + "\n"
                    + pluginPageSpecTwoTuple.value1.getPath() + "\n"
                    + pluginPageSpecTwoTuple.value2.path());
            return tv;
        }
    }


}
