package com.lqjai.common.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.List;

/**
 * @author lqj
 * @version 1.0 2020/8/11
 */
@Named("ConvertMapper")
public class ConvertMapper {

    /**
     * JSON对象转JSON字符串
     *
     * @param jsonObject
     * @return
     */
    @Named("json2StrTransfer")
    public String json2StrTransfer(JSONObject jsonObject) {
        return JSONUtil.toJsonStr(jsonObject);
    }

    /**
     * JSON字符串转JSON对象
     *
     * @param jsonStr
     * @return
     */
    @Named("str2JsonTransfer")
    public JSONObject str2JsonTransfer(String jsonStr) {
        return JSONUtil.parseObj(jsonStr);
    }

    @Named("str2JsonArrayTransfer")
    public JSONArray str2JsonArrayTransfer(String jsonStr) {
        return JSONUtil.parseArray(jsonStr);
    }

    @Named("jsonArray2StrTransfer")
    public String jsonArray2StrTransfer(JSONArray jsonArray) {
        return JSONUtil.toJsonStr(jsonArray);
    }

    @Named("str2ListTransfer")
    public List<String> str2ListTransfer(String str) {
        return Arrays.asList(str);
    }

}
