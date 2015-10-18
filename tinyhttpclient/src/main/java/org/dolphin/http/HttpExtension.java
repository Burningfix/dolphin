package org.dolphin.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yananh on 2015/10/16.
 */
public class HttpExtension {
    private HttpExtension(){
        throw new IllegalStateException("");
    }
    private static final String TAG = "HttpExtension";
    private static final List<HttpOptionalParam> EXTENSION_PARAMS_LIST =new ArrayList<HttpOptionalParam>();
    public static interface HttpOptionalParam{
        String getKey();
        String getValue();
    }

    public synchronized static void putExtensionParam(HttpOptionalParam optionalParam){
        if(!EXTENSION_PARAMS_LIST.contains(optionalParam)) {
            EXTENSION_PARAMS_LIST.add(optionalParam);
        }
    }

    public synchronized static Map<String, String> getExtensionParams(){
        Map<String, String> optionalParams = new HashMap<String, String>();
        for(HttpOptionalParam optionalParam : EXTENSION_PARAMS_LIST){
            optionalParams.put(optionalParam.getKey(), optionalParam.getValue());
        }
        return optionalParams;
    }





}
