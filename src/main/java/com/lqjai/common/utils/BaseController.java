package com.lqjai.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author jfpan
 * @version 1.0 2021/1/11
 */
@Slf4j
public class BaseController {

    protected static final Map<Integer, String> RESULT_PAGE_MAP = new HashMap<>();

    static{
        RESULT_PAGE_MAP.put(1, "checkout/pending?orderId=");
        RESULT_PAGE_MAP.put(2, "checkout/thank_you?orderId=");
        RESULT_PAGE_MAP.put(4, "checkout/fail?orderId=");
    }

    protected Map getParams(HttpServletRequest request) {
        Map map = new HashMap();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    map.put(paramName, paramValue);
                }
            }
        }
        Set<Map.Entry> set = map.entrySet();
        log.info("-------------【showParams】-----------------");
        for (Map.Entry entry : set) {
            log.info("--> key = "+ entry.getKey() + ", value = " + entry.getValue());
        }
        log.info("-------------【showParams】-----------------");

        return map;
    }

    protected Map<String, String> setParams(HttpServletRequest request) {
        Map<String, String> map = new HashMap();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    map.put(paramName, paramValue);
                }
            }
        }
//        Set<Map.Entry> set = map.entrySet();
        log.info("-------------【showParams】-----------------");
        for (Map.Entry entry : map.entrySet()) {
            log.info("--> key = "+ entry.getKey() + ", value = " + entry.getValue());
        }
        log.info("-------------【showParams】-----------------");

        return map;
    }

    // Simple helper method to fetch request data as a string from HttpServletRequest object.
    protected static String getBody(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("getBody Exception!", ex);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    log.error("getBody Exception!!", ex);
                }
            }
        }
        return stringBuilder.toString();
    }

}
