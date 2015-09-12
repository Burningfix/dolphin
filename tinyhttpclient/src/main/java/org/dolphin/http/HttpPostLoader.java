package org.dolphin.http;

import com.hanyanan.http.HttpRequestBody.EntityHolder;
import com.hanyanan.http.TransportProgress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import hyn.com.lib.IOUtil;
import hyn.com.lib.Preconditions;
import hyn.com.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/5/27.
 */
public class HttpPostLoader extends HttpUrlLoader {
    public boolean isMultipart(HttpRequest httpRequest) {
        return true;
    }

    @Override
    protected void writeRequestBody(HttpRequest request, URLConnection connection) throws IOException {
        Map<String, Object> params = request.getParams();
        List<EntityHolder> entityHolders = request.getRequestBody().getResources();
        if(params.size() <= 0 && entityHolders.size() <= 0) {
            System.out.println("url post request not need upload anything.");
            return ;
        }
        if(isMultipart(request)) {
            writeRequestBodyMultipart(request, params, entityHolders, connection);
            return ;
        }

        writeRequestParam(request, params, connection);
    }

    /**
     * Send request param, encode with utf-8 charset, the Content-Type is "application/x-www-form-urlencoded;charset=utf-8"
     *
     * @param request the specify request on running
     * @param params the param need send to server with request body.
     * @param connection http connection.
     * @throws IOException
     */
    private void writeRequestParam(HttpRequest request, Map<String, Object> params, URLConnection connection)
                                                                throws IOException{
        if(null == params || params.isEmpty()) return ;
//        Content-Type: application/x-www-form-urlencoded;charset=utf-8
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        Map<String, String> encodedParam = encodeParams(params);
        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, String>> entries = encodedParam.entrySet();
        for(Map.Entry<String, String> entry : entries) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        outputStream.write(sb.substring(0, sb.length() - 1).toString().getBytes());
        outputStream.flush();
        outputStream.close();
    }


    /**
     * Send both request param and body to server, with Content-Type is "multipart/form-data;boundary=......"
     * @param request the specify request on running
     * @param params the param need send to server with request body.
     * @param entityHolders
     * @param connection http connection.
     * @throws IOException
     */
    private void writeRequestBodyMultipart(HttpRequest request, Map<String, Object> params, List<EntityHolder> entityHolders,
                                           URLConnection connection) throws IOException {
        String boundary = UUID.randomUUID().toString();
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        Map<String, String> encodedParam = encodeParams(params);
        String boundaryLine =DASHDASH +  boundary;
        String boundaryEndLine =DASHDASH +  boundary + DASHDASH;
        //            --ZnGpDtePMx0KrHh_G0X99Yef9r8JZsRJSXC
//            Content-Disposition: form-data;name="desc"
//            Content-Type: text/plain; charset=UTF-8
//            Content-Transfer-Encoding: 8bit
//
//                    [......][......][......][......]...........................


        if(null != encodedParam && encodedParam.size() > 0){ //write request param
            Set<Map.Entry<String, String>> entries = encodedParam.entrySet();
            for(Map.Entry<String, String> entry : entries){
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


        if(null != entityHolders && entityHolders.size() > 0) {
            TransportProgress transportProgress = request.getTransportProgress();
//            --ZnGpDtePMx0KrHh_G0X99Yef9r8JZsRJSXC
//            Content-Disposition: form-data;name="pic"; filename="photo.jpg"
//            Content-Type: application/octet-stream
//            Content-Transfer-Encoding: binary
//
//                    [图片二进制数据]
            long count = getTotle(entityHolders);
            long reads = 0;
            for(EntityHolder entityHolder : entityHolders){
                long size = entityHolder.resource.size();
                StringBuilder data = new StringBuilder();
                data.append(boundaryLine)
                        .append(CRLF)
                        .append("Content-Disposition: form-data;name=\"").append(entityHolder.param).append("\"");
                if(!ValueUtil.isEmpty(entityHolder.fileName)){
                    data.append(COLONSPACE).append("filename=\"").append(entityHolder.fileName).append("\"");
                }
                data.append(CRLF)
                        .append("Content-Type: application/octet-stream")
                        .append(CRLF)
                        .append(CRLF);
                outputStream.write(data.toString().getBytes());
                outputStream.flush();
                reads = upload(request, entityHolder.resource.openStream(),
                        outputStream, transportProgress, reads, count);
                outputStream.write(CRLF.getBytes());
                IOUtil.closeQuietly(entityHolder.resource.openStream());
            }
        }
        outputStream.write(boundaryEndLine.getBytes());
        outputStream.write(CRLF.getBytes());
        outputStream.flush();
        IOUtil.closeQuietly(outputStream);
    }

    private long getTotle(List<EntityHolder> entityHolders){
        if(null == entityHolders) return 0;
        long size = 0;
        for(EntityHolder entityHolder : entityHolders){
            size += entityHolder.resource.size();
        }
        return size;
    }


    /**
     * Copy data from inputStream to outputStream.
     * @param inputStream the data come from
     * @param outputStream the data output
     * @param transportProgress the callback
     * @param maxSize the max size of the data transport
     */
    private long upload(HttpRequest request, InputStream inputStream, OutputStream outputStream,
                        TransportProgress transportProgress, long reads, long maxSize) throws IOException{
        Preconditions.checkNotNull(inputStream);
        Preconditions.checkNotNull(outputStream);
        int buffSize = 1024;//1k
        byte[] buf = new byte[buffSize];
        do {
            long read = inputStream.read(buf);
            if (read <= 0) break; // 读取完毕
            outputStream.write(buf, 0, (int) read);
            reads += read;
            onTransportUpProgress(request, reads, maxSize);
        } while (true);
        return reads;
    }

    private Map<String, String> encodeParams(Map<String, Object> params){
        if(null == params || params.size() <= 0) return null;
        Map<String, String> res = new HashMap<String, String>();
        Set<Map.Entry<String, Object>> entries = params.entrySet();
        for(Map.Entry<String, Object> entry : entries){
            try {
                res.put(URLEncoder.encode(entry.getKey(), "UTF-8"),
                        URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return res;
    }
}
