package guhao.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 全局Runtime异常捕获
 */
public class GlobalException extends RuntimeException {

    @Getter
    @Setter
    private String msg;

    public GlobalException(String message) {
        this.msg = message;
    }
}
