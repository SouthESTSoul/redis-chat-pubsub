package guhao.utils;

import lombok.Data;


@Data
public class R {

    private int code = 200;

    private String msg = "success";

    private Object data;

    public R() {
        super();
    }

    public R(Object data) {
        super();
        this.data = data;
    }

    public R(int code, String msg) {
        super();
        this.code = code;
        this.msg = msg;
    }
}
