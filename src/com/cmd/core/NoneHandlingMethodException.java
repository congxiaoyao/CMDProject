package com.cmd.core;

/**
 * 当Command对象中不持有HandlingMaethod的引用又需要获取HandlingMaethod中的信息的时候会抛出此异常
 *
 * @version 2.0
 * Created by congxiaoyao on 2016/2/20.
 */
public class NoneHandlingMethodException extends Exception{

	private static final long serialVersionUID = 6374099046757734005L;
	
	public static final String MSG = "can't find any method to handle this command\n";

    public NoneHandlingMethodException() {
        super(MSG);
    }

    public NoneHandlingMethodException(String message) {
        super(MSG + message);
    }
}
