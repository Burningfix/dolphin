package org.dolphin.http;


import org.dolphin.lib.ListUtil;
import org.dolphin.lib.Preconditions;
import org.dolphin.lib.binaryresource.BinaryResource;

import java.util.LinkedList;
import java.util.List;

import static org.dolphin.lib.Preconditions.checkNotNull;
import static org.dolphin.lib.Preconditions.checkArgument;
import javax.annotation.Nullable;


/**
 * Created by dolphin on 2015/5/11.
 *
 *  application/x-www-form-urlencoded
 *
 * <ol>
 *     <li>application/x-www-form-urlencoded</li>
 *     <li>The multipart boundary format as follow:<br/><code>
 * Content-Type:multipart/form-data;boundary=ZnGpDtePMx0KrHh_G0X99Yef9r8JZsRJSXC\r\n<br/>
 * --ZnGpDtePMx0KrHh_G0X99Yef9r8JZsRJSXC\r\n<br/>
 * Content-Disposition: form-data;name="desc"\r\n<br/>
 * Content-Type: text/plain; charset=UTF-8\r\n<br/>
 * Content-Transfer-Encoding: 8bit\r\n<br/>
 * \r\n<br/>
 * [......][......][......][......]..........................\r\n<br/>
 * --ZnGpDtePMx0KrHh_G0X99Yef9r8JZsRJSXC\r\n<br/>
 * Content-Disposition: form-data;name="image";filename="file.jpg"\r\n<br/>
 * Content-Type: application/octet-stream\r\n<br/>
 * Content-Transfer-Encoding: binary\r\n<br/>
 * \r\n<br/>
 * [......][......][......][......]..........................\r\n<br/>
 * --ZnGpDtePMx0KrHh_G0X99Yef9r8JZsRJSXC--\r\n</code></li>
 * </ol>
 *
 */
public class HttpRequestBody {
    private final LinkedList<EntityHolder> resources = new LinkedList<EntityHolder>();

    /**
     * post a file to server like {Content-Disposition: form-data;name="image"}
     * @see #add(String, String, BinaryResource)
     * @param param param which indicate current param
     * @param resource resource will send to server
     */
    public HttpRequestBody add(String param, BinaryResource resource) {
        checkNotNull(param);
        checkNotNull(resource);
        checkArgument(resource.size() > 0, "The Body's size must be greater than 0!");
        resources.add(new EntityHolder(param, null, resource));
        return this;
    }

    /**
     * post a file to server like {Content-Disposition: form-data;name="image";filename="file.jpg"}
     * @see #add(String, BinaryResource)
     * @param param param which indicate current param
     * @param fileName supply local file name
     * @param resource resource will send to server
     */
    public HttpRequestBody add(String param, String fileName, BinaryResource resource) {
        checkNotNull(param);
        checkNotNull(fileName);
        checkNotNull(resource);
        checkArgument(resource.size() > 0, "The Body's size must be greater than 0!");
        resources.add(new EntityHolder(param, null, resource));
        return this;
    }

    List<EntityHolder> getResources() {
        return ListUtil.immutableList(resources);
    }

    public boolean hasContent() {
        return !resources.isEmpty();
    }

    public void releaseResource(){
        // TODO
    }

    /**
     * The entry of one file will upload.
     */
    public static final class EntityHolder {
        /** Current entity's param */
        public String param;
        /** local file name */
        public String fileName;
        /** Body content */
        public BinaryResource resource;

        private EntityHolder(String param, String fileName, BinaryResource binaryResource) {
            this.param = param;
            this.fileName = fileName;
            this.resource = binaryResource;
        }
    }


    public static HttpRequestBody create(MimeType mimeType, String content) {
//        Charset charset = Util.UTF_8;
//        if (contentType != null) {
//            charset = contentType.charset();
//            if (charset == null) {
//                charset = Util.UTF_8;
//                contentType = MediaType.parse(contentType + "; charset=utf-8");
//            }
//        }
//        byte[] bytes = content.getBytes(charset);
//        return create(contentType, bytes);
        return null;
    }

//    public static HttpRequestBody create(String content) {
//        //TODO
//        return null;
//    }

//    /** Returns a new request body that transmits {@code content}. */
//    public static RequestBody create(final MediaType contentType, final byte[] content,
//                                     final int offset, final int byteCount) {
//        if (content == null) throw new NullPointerException("content == null");
//        Util.checkOffsetAndCount(content.length, offset, byteCount);
//        return new RequestBody() {
//            @Override public MediaType contentType() {
//                return contentType;
//            }
//
//            @Override public long contentLength() {
//                return byteCount;
//            }
//
//            @Override public void writeTo(BufferedSink sink) throws IOException {
//                sink.write(content, offset, byteCount);
//            }
//        };
}
