package org.dolphin.http.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.dolphin.http.Headers;
import org.dolphin.lib.KeepAttr;
import org.dolphin.lib.SecurityUtil;
import org.dolphin.lib.binaryresource.BinaryResource;
import org.dolphin.lib.binaryresource.ByteArrayBinaryResource;
import org.dolphin.lib.binaryresource.FileBinaryResource;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2015/11/4.
 */
public class QueryFilesRequestHandler extends HttpGetRequestHandler {
    private final String rootPath;
    private final Map<String, FileBean> fileTreeMaps = new LinkedHashMap<String, FileBean>();

    QueryFilesRequestHandler(String localPath) {
        this.rootPath = localPath;
        File root = new File(localPath);
        if (root.isDirectory()) {
            for (File file : root.listFiles()) {
                FileBean fileBean = new FileBean();
                fileBean.path = file.getAbsolutePath();
                fileBean.modifyTime = file.lastModified();
                fileBean.size = file.length();
                fileBean.name = file.getName();
                String id = SecurityUtil.md5_16(fileBean.path);
                fileBean.url = "http://127.0.0.1:8877" + Main.REQUEST_FILE_PATH + "?" + Main.QUERY_FILE_PARAM_KEY + "=" + id;
                fileTreeMaps.put(id, fileBean);
            }
        }
    }

    public class FileTreeBean implements KeepAttr {
        @Expose
        public long error;
        @Expose
        public String msg;
        @Expose
        public FileBean[] files;
    }

    private BinaryResource queryRequest(String path, Map<String, String> params,
                                        Map<String, String> responseHeaders) {
        FileTreeBean fileTreeBean = new FileTreeBean();
        fileTreeBean.error = 0;
        fileTreeBean.msg = "success";
        fileTreeBean.files = fileTreeMaps.values().toArray(new FileBean[]{});
        responseHeaders.put("Content-Type", "application/json");
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation() //不导出实体中没有用@Expose注解的属性
                .create();
        return new ByteArrayBinaryResource(gson.toJson(fileTreeBean).getBytes());
    }

    private BinaryResource transportFileRequest(String path, Map<String, String> params,
                                                Map<String, String> responseHeaders){
        String id = params.get(Main.QUERY_FILE_PARAM_KEY);
        FileBean fileBean = fileTreeMaps.get(id);
        responseHeaders.put(Headers.CONTENT_DISPOSITION.value(), "attachment; filename=\""+fileBean.name+"\"");
        return new FileBinaryResource(new File(fileBean.path));
    }

    @Override
    protected BinaryResource getResource(String path, Map<String, String> params,
                                         Map<String, String> responseHeaders) {
        if (path.equalsIgnoreCase(Main.QUERY_FILE_LIST_PATH)) {
            return queryRequest(path, params, responseHeaders);
        } else if (path.equalsIgnoreCase(Main.REQUEST_FILE_PATH)) {
            return transportFileRequest(path, params, responseHeaders);
        }
        return new ByteArrayBinaryResource(new byte[0]);
    }
}
