package com.congxiaoyao.cmd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ���в�������
 * ����ĳЩ��������������ʱֻ�������������һЩ�ض�ֵ�������ô���״̬�������ֻ����ȫ�������ر����������������
 * ���������Ĳ���������ϣ�����ε�����ע��������ڹ��˵����ض�ֵ��ͬʱ��Ҫ���{@code CommandName}һͬʹ��
 * ����ٳ�����ʹ�÷���
 * <pre>
 *
 * �����˺�����������������foo����������ֻϣ������ڶ�������Ϊsensitiveʱ���������ô���Ĵ��������Զ�������
 * <code>@CommandName("foo")</code>
 * public void foo(String arg1, @OnlyCare("sensitive")String arg2){
 *     System.out.println("foo XXX sensitive");
 * }
 * ֻҪ�������ĵ�����ͨ��OnlyCareע���ע�ڷ����Ĳ���ǰ����ʵ�����в�������
 *
 * ����һ������foo������һ������Ķ��巽ʽ�Լ���д(��д����������)
 * <code>@CommandName("foo")</code>
 * <code>@OnlyCare("sensitive")</code>
 * public void foo(){
 *     System.out.println("foo sensitive");
 * }
 *
 * ����һ�������ϲ���������foo�����԰Ѻ����Ĳ����б���Ĳ���������OnlyCare�Ĳ����Ӷ�ʡ��OnlyCare�Ĳ���
 * <code>@OnlyCare("sensitive")</code>
 * public void foo( @OnlyCare String hello, @OnlyCare String world){
 *     System.out.println("foo hello world");
 * }
 * ע��Ҫ��ʹ������������ڱ���ʱʹ��javac��-parameters����
 *
 * </pre>
 *
 * @author congxiaoyao
 * @version 1.4
 * @date 2016.1.24
 * @see CommandName
 */

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnlyCare {

	String value() default "";

}