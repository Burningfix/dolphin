package org.dolphin.http.server;

import org.dolphin.lib.util.ValueUtil;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TinyHttpHelper {
    private static final HashMap<String, String> sHttpStatusCodeMap = new HashMap<String, String>();

    static {
        sHttpStatusCodeMap.put("100", "Continue");
        sHttpStatusCodeMap.put("101", "Switching Protocols");
        sHttpStatusCodeMap.put("200", "OK");
        sHttpStatusCodeMap.put("201", "Created");
        sHttpStatusCodeMap.put("202", "Accepted");
        sHttpStatusCodeMap.put("203", "Non-Authoritative Information (for DNS)");
        sHttpStatusCodeMap.put("204", "No Content");
        sHttpStatusCodeMap.put("205", "Reset Content");
        sHttpStatusCodeMap.put("206", "Partial Content");
        sHttpStatusCodeMap.put("300", "Multiple Choices");
        sHttpStatusCodeMap.put("301", "Moved Permanently");
        sHttpStatusCodeMap.put("302", "Moved Temporarily");
        sHttpStatusCodeMap.put("303", "See Other");
        sHttpStatusCodeMap.put("304", "Not Modified");
        sHttpStatusCodeMap.put("305", "Use Proxy");
        sHttpStatusCodeMap.put("307", "Redirect Keep Verb");
        sHttpStatusCodeMap.put("400", "Bad Request");
        sHttpStatusCodeMap.put("401", "Unauthorized");
        sHttpStatusCodeMap.put("402", "Payment Required");
        sHttpStatusCodeMap.put("403", "Forbidden");
        sHttpStatusCodeMap.put("404", "Not Found");
        sHttpStatusCodeMap.put("405", "Bad Request");
        sHttpStatusCodeMap.put("406", "Not Acceptable");
        sHttpStatusCodeMap.put("407", "Proxy Authentication Required");
        sHttpStatusCodeMap.put("408", "Request Timed-Out");
        sHttpStatusCodeMap.put("409", "Conflict");
        sHttpStatusCodeMap.put("410", "Gone");
        sHttpStatusCodeMap.put("411", "Length Required");
        sHttpStatusCodeMap.put("412", "Precondition Failed");
        sHttpStatusCodeMap.put("413", "Request Entity Too Large");
        sHttpStatusCodeMap.put("414", "Request, URI Too Large");
        sHttpStatusCodeMap.put("415", "Unsupported Media Type");
        sHttpStatusCodeMap.put("500", "Internal Server Error");
        sHttpStatusCodeMap.put("501", "Not Implemented");
        sHttpStatusCodeMap.put("502", "Bad Gateway");
        sHttpStatusCodeMap.put("503", "Server Unavailable");
        sHttpStatusCodeMap.put("504", "Gateway Timed-Out");
        sHttpStatusCodeMap.put("505", "HTTP Version not supported");
        sHttpStatusCodeMap.put("12001", "Out of Handles");
        sHttpStatusCodeMap.put("12002", "Timeout");
        sHttpStatusCodeMap.put("12003", "Extended Error");
        sHttpStatusCodeMap.put("12004", "Internal Error");
        sHttpStatusCodeMap.put("12005", "Invalid URL");
        sHttpStatusCodeMap.put("12006", "Unrecognized Scheme");
        sHttpStatusCodeMap.put("12007", "Name Not Resolved");
        sHttpStatusCodeMap.put("12008", "Protocol Not Found");
        sHttpStatusCodeMap.put("12009", "Invalid Option");
        sHttpStatusCodeMap.put("12010", "Bad Option Length");
        sHttpStatusCodeMap.put("12011", "Option not Settable");
        sHttpStatusCodeMap.put("12012", "Shutdown");
        sHttpStatusCodeMap.put("12013", "Incorrect User Name");
        sHttpStatusCodeMap.put("12014", "Incorrect Password");
        sHttpStatusCodeMap.put("12015", "Login Failure");
        sHttpStatusCodeMap.put("12016", "Invalid Option");
        sHttpStatusCodeMap.put("12017", "Operation Cancelled");
        sHttpStatusCodeMap.put("12018", "Incorrect Handle Type");
        sHttpStatusCodeMap.put("12019", "Inccorect Handle State");
        sHttpStatusCodeMap.put("12020", "Not Proxy Request");
        sHttpStatusCodeMap.put("12021", "Registry Value Not Found");
        sHttpStatusCodeMap.put("12022", "Bad Registry Parameter");
        sHttpStatusCodeMap.put("12023", "No Direct Access");
        sHttpStatusCodeMap.put("12024", "No Content");
        sHttpStatusCodeMap.put("12025", "No Callback");
        sHttpStatusCodeMap.put("12026", "Request Pending");
        sHttpStatusCodeMap.put("12027", "Incorrect Format");
        sHttpStatusCodeMap.put("12028", "Item Not Found");
        sHttpStatusCodeMap.put("12029", "Cannot Connect");
        sHttpStatusCodeMap.put("12030", "Connection Aborted");
        sHttpStatusCodeMap.put("12031", "Connection Reset");
        sHttpStatusCodeMap.put("12032", "Force Retry");
        sHttpStatusCodeMap.put("12033", "Invalid Proxy Request");
        sHttpStatusCodeMap.put("12034", "Need UI");
        sHttpStatusCodeMap.put("12035", "Not Defined in WinInet");
        sHttpStatusCodeMap.put("12036", "Handle Exists");
        sHttpStatusCodeMap.put("12037", "See Cert Date Invalid");
        sHttpStatusCodeMap.put("12038", "See Cert CN Invalid");
        sHttpStatusCodeMap.put("12039", "HTTP to HTTPS on Redir");
        sHttpStatusCodeMap.put("12040", "HTTPs to HTTP on Redir");
        sHttpStatusCodeMap.put("12041", "Mixed Security");
        sHttpStatusCodeMap.put("12042", "Chg Post is Non Secure");
        sHttpStatusCodeMap.put("12043", "Post is Non Secure");
        sHttpStatusCodeMap.put("12044", "Client Auth Cert Needed");
        sHttpStatusCodeMap.put("12045", "Invalid CA (Cert)");
        sHttpStatusCodeMap.put("12046", "Client Auth Not Setup");
        sHttpStatusCodeMap.put("12047", "Async Thread Failed");
        sHttpStatusCodeMap.put("12048", "Redirect Scheme Changed");
        sHttpStatusCodeMap.put("12049", "Dialog Pending");
        sHttpStatusCodeMap.put("12050", "Retry Dialog");
        sHttpStatusCodeMap.put("12052", "Https Http Submit Redir");
        sHttpStatusCodeMap.put("12053", "Insert Cdrom");
        sHttpStatusCodeMap.put("12171", "Failed DueToSecurityCheck");
        sHttpStatusCodeMap.put("12110", "Transfer in Progress");
        sHttpStatusCodeMap.put("12111", "FTP Dropped");
        sHttpStatusCodeMap.put("12130", "Protocol Error");
        sHttpStatusCodeMap.put("12131", "Not File");
        sHttpStatusCodeMap.put("12132", "Data Error");
        sHttpStatusCodeMap.put("12133", "End of Data");
        sHttpStatusCodeMap.put("12134", "Invalid Locator");
        sHttpStatusCodeMap.put("12135", "Invalid Locator Type");
        sHttpStatusCodeMap.put("12136", "Not Gopher Plus");
        sHttpStatusCodeMap.put("12137", "Attribute Not Found");
        sHttpStatusCodeMap.put("12138", "Unknown Locator");
        sHttpStatusCodeMap.put("12150", "Header Not Found");
        sHttpStatusCodeMap.put("12151", "Downlevel Server");
        sHttpStatusCodeMap.put("12152", "Invalid Server Response");
        sHttpStatusCodeMap.put("12153", "Invalid Header");
        sHttpStatusCodeMap.put("12154", "Invalid Query Request");
        sHttpStatusCodeMap.put("12155", "Header Already Exists");
        sHttpStatusCodeMap.put("12156", "Redirect Failed");
        sHttpStatusCodeMap.put("12157", "Security Channel Error");
        sHttpStatusCodeMap.put("12158", "Unable to Cache File");
        sHttpStatusCodeMap.put("12159", "TCP/IP not installed");
        sHttpStatusCodeMap.put("12160", "Not Redirected");
        sHttpStatusCodeMap.put("12161", "Cookie Needs Confirmation");
        sHttpStatusCodeMap.put("12162", "Cookie Declined");
        sHttpStatusCodeMap.put("12168", "Redirect Needs Confirmation");
        sHttpStatusCodeMap.put("12157", "Security Channel Error");
        sHttpStatusCodeMap.put("12158", "Unable To Cache File");
        sHttpStatusCodeMap.put("12159", "Tcpip Not Installed");
        sHttpStatusCodeMap.put("12163", "Disconnected");
        sHttpStatusCodeMap.put("12164", "Server Unreachable");
        sHttpStatusCodeMap.put("12165", "Proxy Server Unreachable");
        sHttpStatusCodeMap.put("12166", "Bad Auto Proxy script");
        sHttpStatusCodeMap.put("12167", "Unable To Download script");
        sHttpStatusCodeMap.put("12169", "Sec Invalid Cert");
        sHttpStatusCodeMap.put("12170", "Sec Cert Revoked");
    }

    public static String getHttpDesc(int code) {
        String res = sHttpStatusCodeMap.get(String.valueOf(code));
        if (null != res) return res;
        return "Error";
    }

    /**
     * 将bytes=5275648- 转化成需要的结果
     *
     * @param range
     * @return
     */
    public static long[] getRange(String range) {
        long[] res = new long[]{0, Long.MAX_VALUE};
        if (ValueUtil.isEmpty(range)) return res;
        Pattern pattern = Pattern.compile("(\\d+)-(\\d*)");
        Matcher m = pattern.matcher(range);
        if (m.find()) {
            String start = m.group(1);
            String end = m.group(2);
            res[0] = ValueUtil.parseLong(start, 0);
            res[1] = ValueUtil.parseLong(end, Long.MAX_VALUE);
        }

        return res;
    }
}

