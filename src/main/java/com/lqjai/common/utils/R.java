package com.lqjai.common.utils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用于微服务返回结果给前端
 *
 * @param <T>
 */
@ApiModel(description = "Result", value = "Result")
@Data
@Accessors(chain = true)
public class R<T> {

    @ApiModelProperty(value = "执行是否成功,true:成功,false:失败", required = true)
    private boolean flag;//是否成功
    @ApiModelProperty(value = "返回状态码,20000:成功,20001:失败,20002:用户名或密码错误,20003:权限不足,20004:远程调用失败,20005:重复操作,20006:没有对应的数据", required = true)
    private Integer code;//返回码

    @ApiModelProperty(value = "提示信息", required = true)
    private String message;//返回消息
    @ApiModelProperty(value = "逻辑数据", required = true)
    private T data;//返回数据

    public R(boolean flag, Integer code, String message, Object data) {
        this.flag = flag;
        this.code = code;
        this.message = message;
        this.data = (T) data;
    }

    public R(boolean flag, Integer code, String message) {
        this.flag = flag;
        this.code = code;
        this.message = message;
    }

    public R() {
        this.flag = true;
        this.code = StatusCode.OK;
        this.message = "请求成功!";
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setFlag(true).setCode(StatusCode.OK)
        .setMessage("success").setData(data);
        return r;
    }

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setFlag(true).setCode(StatusCode.OK)
                .setMessage("success");
        return r;
    }

    public static <T> R<T> error(T message) {
        R<T> r = new R<>();
        r.setFlag(false).setCode(StatusCode.ERROR)
                .setMessage(message.toString());
        return r;
    }
}
