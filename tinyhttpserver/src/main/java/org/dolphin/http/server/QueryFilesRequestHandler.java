package org.dolphin.http.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.dolphin.http.Headers;
import org.dolphin.http.MimeType;
import org.dolphin.lib.KeepAttr;
import org.dolphin.lib.SecurityUtil;
import org.dolphin.lib.binaryresource.BinaryResource;
import org.dolphin.lib.binaryresource.ByteArrayBinaryResource;
import org.dolphin.lib.binaryresource.FileBinaryResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2015/11/4.
 */
public class QueryFilesRequestHandler extends HttpGetRequestHandler {
    private final Map<String, FileBean> fileTreeMaps = new LinkedHashMap<String, FileBean>();

    QueryFilesRequestHandler(String path, String ... localPath) {
        try {
            Files.walkFileTree(Paths.get(path, localPath), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                                                         BasicFileAttributes attrs) throws IOException {
                    //访问文件夹之前调用
                    System.out.println("preVisitDirectory\t"+dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs) throws IOException {
                    // 访问文件调用
                    System.out.println("visitFile\t"+file);
                    File f = file.toFile();
                    String name = f.getName();
                    String mime = MimeType.getMimeTypeFromExtension(MimeType.getFileExtension(name));
                    if(null != mime && mime.startsWith("video")) {
                        FileBean fileBean = new FileBean();
                        fileBean.path = f.getAbsolutePath();
                        fileBean.modifyTime = f.lastModified();
                        fileBean.size = f.length();
                        fileBean.name = name;
                        fileBean.type = mime;
                        String id = SecurityUtil.md5_16(fileBean.path);
                        fileBean.url = Main.REQUEST_FILE_PATH + "?" + Main.QUERY_FILE_PARAM_KEY + "=" + id;
                        fileTreeMaps.put(id, fileBean);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc)
                        throws IOException {
                    // 访问文件失败时调用
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                                                          IOException exc) throws IOException {
                    System.out.println("postVisitDirectory\t"+dir);
                    // 访问文件夹之后调用
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class FileTreeBean implements KeepAttr {
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
