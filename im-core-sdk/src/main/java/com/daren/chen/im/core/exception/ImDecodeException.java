package com.daren.chen.im.core.exception;

/**
 * @ClassName ImDecodeException
 * @Description
 * @Author WChao
 * @Date 2020/1/11 6:02
 * @Version 1.0
 **/
public class ImDecodeException extends ImException {

    public ImDecodeException(Throwable e) {
        super(e);
    }

    public ImDecodeException(String message) {
        super(message);

    }
}