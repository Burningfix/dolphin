package org.dolphin.http;

import org.dolphin.lib.IOUtil;
import org.dolphin.lib.SecurityUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

import org.dolphin.http.HttpRequestBody.EntityHolder;
import org.dolphin.lib.ValueUtil;
import org.dolphin.lib.exception.AbortException;

import static org.dolphin.lib.Preconditions.checkNotNull;
import static org.dolphin.lib.Preconditions.checkNotNulls;

/**
 * Created by hanyanan on 2015/5/27.
 */
public class HttpPostLoader extends HttpUrlLoader {
    public boolean isMultipart(HttpRequest httpRequest) {
        return true;
    }

    @Override
    public void setRequestHeaderProperty(HttpRequest request, HttpURLConnection connection) throws Throwable {
        if (isMultipart(request)) {
            request.getRequestHeader().remove(Headers.CONTENT_LENGTH);
        }
        super.setRequestHeaderProperty(request, connection);
    }

    @Override
    public void sendRequestBody(HttpRequest request, HttpURLConnection connection) throws Throwable {
        Map<String, Object> params = request.getParams();
        List<EntityHolder> entityHolders = request.getRequestBody().getResources();
        if (params.size() <= 0 && entityHolders.size() <= 0) {
            System.out.println("url post request not need upload anything.");
            writeRequestParamUrlEncoded(request, params, connection);
        } else if (isMultipart(request)) {
            writeRequestBodyMultipart(request, params, entityHolders, connection);
        }

        super.sendRequestBody(request, connection);
    }


    /**
     * Send request param, encode with utf-8 charset, the Content-Type is "application/x-www-form-urlencoded;charset=utf-8"
     *
     * @param request    the specify request on running
     * @param params     the param need send to server with request body.
     * @param connection http connection.
     * @throws IOException
     */
    private void writeRequestParamUrlEncoded(HttpRequest request, Map<String, Object> params, URLConnection connection)
            throws IOException {
        if (null == params || params.isEmpty()) return;
        // Content-Type: application/x-www-form-urlencoded;charset=utf-8
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        Map<String, String> encodedParam = encodeParams(params);
        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, String>> entries = encodedParam.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        outputStream.write(sb.substring(0, sb.length() - 1).toString().getBytes());
        outputStream.flush();
        outputStream.close();
    }


    /**
     * Send both request param and body to server, with Content-Type is "multipart/form-data;boundary=......"
     *
     * @param request       the specify request on running
     * @param params        the param need send to server with request body.
     * @param entityHolders
     * @param connection    http connection.
     * @throws IOException
     */
    private void writeRequestBodyMultipart(HttpRequest request, Map<String, Object> params, List<EntityHolder> entityHolders,
                                           URLConnection connection) throws IOException, AbortException {
        String boundary = UUID.randomUUID().toString();
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        Map<String, String> encodedParam = encodeParams(params);
        String boundaryLine = DASHDASH + boundary;
        String boundaryEndLine = DASHDASH + boundary + DASHDASH;
        //            --ZnGpDtePMx0KrHh_G0X99Yef9r8JZsRJSXC
//            Content-Disposition: form-data;name="desc"
//            Content-Type: text/plain; charset=UTF-8
//            Content-Transfer-Encoding: 8bit
//
//                    [......][......][......][......]...........................


        if (null != encodedParam && encodedParam.size() > 0) { //write request param
            Set<Map.Entry<String, String>> entries = encodedParam.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                StringBuilder data = new StringBuilder();
                data.append(boundaryLine)
                        .append(CRLF)
                        .append("Content-Disposition: form-data;name=\"").append(entry.getKey()).append("\"")
                        .append(CRLF)
                        .append(CRLF)
                        .append(entry.getValue())
                        .append(CRLF);
                outputStream.write(data.toString().getBytes());
            }
        }


        if (null != entityHolders && entityHolders.size() > 0) {
//            --ZnGpDtePMx0KrHh_G0X99Yef9r8JZsRJSXC
//            Content-Disposition: form-data;name="pic"; filename="photo.jpg"
//            Content-Type: application/octet-stream
//            Content-Transfer-Encoding: binary
//
//                    [图片二进制数据]
            long count = getTotalSize(entityHolders);
            long cursor = 0;
            for (EntityHolder entityHolder : entityHolders) {
                long size = entityHolder.resource.size();
                InputStream from = entityHolder.resource.openStream();
                StringBuilder data = new StringBuilder();
                data.append(boundaryLine)
                        .append(CRLF)
                        .append("Content-Disposition: form-data;name=\"").append(entityHolder.param).append("\"");
                if (!ValueUtil.isEmpty(entityHolder.fileName)) {
                    data.append(COLONSPACE).append("filename=\"").append(entityHolder.fileName).append("\"");
                }
                data.append(CRLF)
                        .append("Content-Type: application/octet-stream")
                        .append(CRLF)
                        .append(CRLF);
                outputStream.write(data.toString().getBytes());
                outputStream.flush();
                cursor = upload(request, from, outputStream, cursor, count);
                outputStream.write(CRLF.getBytes());
                IOUtil.closeQuietly(entityHolder.resource.openStream());
            }
        }
        outputStream.write(boundaryEndLine.getBytes());
        outputStream.write(CRLF.getBytes());
        outputStream.flush();
        IOUtil.closeQuietly(outputStream);
    }

    private long getTotalSize(List<HttpRequestBody.EntityHolder> entityHolders) {
        if (null == entityHolders) return 0;
        long size = 0;
        for (HttpRequestBody.EntityHolder entityHolder : entityHolders) {
            size += entityHolder.resource.size();
        }
        return size;
    }


    /**
     * Copy data from inputStream to outputStream.
     *
     * @param inputStream  the data come from
     * @param outputStream the data output
     * @return the last cursor after upload some data.
     */
    private long upload(HttpRequest request, InputStream inputStream, OutputStream outputStream,
                        long cursor, long totalSize) throws AbortException, IOException {
        checkNotNulls(request, inputStream, outputStream);
        int buffSize = 1024 * 2;//1k
        byte[] buf = new byte[buffSize];
        do {
            long read = inputStream.read(buf);
            if (read <= 0) break; // 读取完毕
            outputStream.write(buf, 0, (int) read);
            cursor += read;
            onTransportUpProgress(request, cursor, totalSize);
        } while (true);
        return totalSize;
    }

    /**
     * 实际上，到这来的时候，所有的都已经都编码过了，不需要做其他编码，只需要循环遍历，将Map<String,Object>
     * 转换成Map<String,String>
     */
    private Map<String, String> encodeParams(Map<String, Object> params) {
        if (null == params || params.size() <= 0) return null;
        Map<String, String> res = new LinkedHashMap<String, String>();
        Set<Map.Entry<String, Object>> entries = params.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            res.put(entry.getKey(), entry.getValue().toString());

//            try {
//                res.put(URLEncoder.encode(entry.getKey(), "UTF-8"),
//                        URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
        }
        return res;
    }
}
