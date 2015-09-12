package org.dolphin.http;


import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import hyn.com.lib.Preconditions;
import hyn.com.lib.binaryresource.BinaryResource;
import hyn.com.lib.binaryresource.ListUtil;

/**
 * Created by hanyanan on 2015/5/11.
 */
public class HttpRequestBody {
    private final LinkedList<EntityHolder> resources = new LinkedList<EntityHolder>();

    public HttpRequestBody add(String param, BinaryResource resource) {
        Preconditions.checkNotNull(param);
        Preconditions.checkNotNull(resource);
        Preconditions.checkArgument(resource.size() > 0, "The Body's size must be greater than 0!");
        resources.add(new EntityHolder(param, null, resource));
        return this;
    }

    public HttpRequestBody add(String param, String fileName, BinaryResource resource) {
        Preconditions.checkNotNull(param);
        Preconditions.checkNotNull(fileName);
        Preconditions.checkNotNull(resource);
        Preconditions.checkArgument(resource.size() > 0, "The Body's size must be greater than 0!");
        resources.add(new EntityHolder(param, null, resource));
        return this;
    }

    public List<EntityHolder> getResources() {
        return ListUtil.immutableList(resources);
    }

    public boolean hasContent() {
        return resources.size() > 0;
    }

    /**
     * The entry of one file will upload.
     */
    public static final class EntityHolder {
        public String param;
        @Nullable public String fileName;
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

    public static HttpRequestBody create(String content) {
        //TODO
        return null;
    }

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
