package org.dolphin.http;


/**
 * Created by hanyanan on 2015/5/27.
 */
public class HttpGetLoader extends HttpUrlLoader {
    public static final HttpGetLoader INSTANCE = new HttpGetLoader();
    /**
     * Return the url will be request.
     */
    @Override
    protected String getUrl(HttpRequest request) {
        String url = super.getUrl(request);
        url = HttpUtil.generateUrl(url, request.getParams(), false);
        HttpLog.d("TinyHttpClient", "getUrl " + url);
        return url;
    }
}
