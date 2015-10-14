package org.dolphin.job;

import java.util.Map;

/**
 * Created by hanyanan on 2015/10/14.
 */
public class Jobs {
    /**
     * 创建一个pending的Job，每次
     * @return
     */
    public static Job pending(){

        return null;
    }

    public static <T> Job create(T input){

        return null;
    }

    public static Job httpGet(String url){

        return null;
    }

    public static Job httpGet(String url, Map<String, String> params){

        return null;
    }

    public static Job httpPost(String url){

        return null;
    }
}
