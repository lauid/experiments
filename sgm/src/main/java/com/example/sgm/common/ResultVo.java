package com.example.sgm.common;

public class ResultVo<T> {
    private int code;
    private String message;
    private T data;

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    // 省略构造方法和Getter/Setter

    // 静态方法：成功返回结果
    public static <T> ResultVo<T> success(T data) {
        ResultVo<T> resultVo = new ResultVo<>();
        resultVo.setCode(200);
        resultVo.setMessage("Success");
        resultVo.setData(data);
        return resultVo;
    }

    // 静态方法：失败返回结果
    public static <T> ResultVo<T> error(int code, String message) {
        ResultVo<T> resultVo = new ResultVo<>();
        resultVo.setCode(code);
        resultVo.setMessage(message);
        return resultVo;
    }

    // 静态方法：失败返回结果
    public static <T> ResultVo<T> fail() {
        ResultVo<T> resultVo = new ResultVo<>();
        resultVo.setCode(500);
        resultVo.setMessage("fail");
        return resultVo;
    }
}

