package com.congxiaoyao.cmd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ���в�������
 * ����һ�δ�������������ʱֻ�������������һЩ�ض�ֵ�������ô���״̬�������ֻ����ȫ�������ر����������������
 * ���������Ĳ���������ϣ�����ε�����ע��������ڹ��˵����ض�ֵ��ͬʱ��Ҫ���{@code CommandName}һͬʹ��
 * @see CommandName
 * 
 * @version 1.0
 * @author congxiaoyao
 * @date 2016.1.24
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnlyCare {
	
	public String value() default "";

}