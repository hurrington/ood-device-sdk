package com.ood.core.entity;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;

import java.util.HashMap;

/**
 * 操作消息提醒
 *
 * @author ruoyi
 */
public class ResultData extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    public static final String CODE_TAG = "code";

    /**
     * 返回内容
     */
    public static final String MSG_TAG = "msg";

    /**
     * 数据对象
     */
    public static final String DATA_TAG = "data";

    /**
     * 初始化一个新创建的 AjaxResult 对象，使其表示一个空消息。
     */
    public ResultData() {
    }

    public ResultData(HashMap<String, Object> map) {
        this.putAll(map);
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     */
    public ResultData(int code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     * @param data 数据对象
     */
    public ResultData(int code, String msg, Object data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (ObjectUtil.isNotNull(data)) {
            super.put(DATA_TAG, data);
        }
    }

    /**
     * 方便链式调用
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public ResultData put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static ResultData success() {
        return ResultData.success("操作成功");
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static ResultData success(Object data) {
        return ResultData.success("操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static ResultData success(String msg) {
        return ResultData.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static ResultData success(String msg, Object data) {
        return new ResultData(HttpStatus.HTTP_OK, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @return
     */
    public static ResultData error() {
        return ResultData.error("操作失败");
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static ResultData error(String msg) {
        return ResultData.error(msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static ResultData error(String msg, Object data) {
        return new ResultData(HttpStatus.HTTP_OK, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @param code 状态码
     * @param msg  返回内容
     * @return 警告消息
     */
    public static ResultData error(int code, String msg) {
        return new ResultData(code, msg, null);
    }

    public static ResultData newInstance() {

        return new ResultData();
    }

    public ResultData setCode(int code) {

        super.put(CODE_TAG, code);
        return this;
    }

    public ResultData setMsg(String msg) {

        super.put(MSG_TAG, msg);
        return this;
    }

    public ResultData setData(Object data) {

        super.put(DATA_TAG, data);
        return this;
    }

    public String getMsg() {
        return Convert.toStr(this.get("msg"));
    }

    public int getCode() {
        return Convert.toInt(this.get("code"));
    }

    public Object getData() {
        return this.get("data");
    }

    public boolean isSuccess() {
        return HttpStatus.HTTP_OK == getCode();
    }

}
