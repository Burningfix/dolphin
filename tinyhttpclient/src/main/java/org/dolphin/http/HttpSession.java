package org.dolphin.http;

import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dolphin on 2015/9/13.
 *
 * Recorde a complete
 */
public class HttpSession {
    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;
    private final TimeStatus timeStatus;
    private final List<RedirectedResponse> redirectedResponses = Lists.newLinkedList();
    private final TrafficStatus trafficStatus;

    /**
     *
     */
    private final String destUrl;










    public List<RedirectedResponse> getRedirectedResponses(){
        return Lists.newCopyOnWriteArrayList(redirectedResponses);
    }

    void addRedirectedResponse(RedirectedResponse redirectedResponse) {
        redirectedResponses.add(redirectedResponse);
    }
}
