package guhao.handler;

import guhao.exception.GlobalException;
import guhao.utils.R;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//统一异常处理器
@RestControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

	//表示统一处理exception异常
    @ExceptionHandler(value = Exception.class)
    public R exception(Exception e) {
        e.printStackTrace();
        return new R(500, "系统异常");
    }
    //表示统一处理自定义异常
    @ExceptionHandler(value = GlobalException.class)
    public R globalException(GlobalException e) {
        e.printStackTrace();
        return new R(500, e.getMsg());
    }

}
