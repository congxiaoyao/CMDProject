package com.congxiaoyao.cmd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ��ע�����ڰ󶨴������뱻��������ʹ��CommandAnalyzer����ͨ����ע���ҵ������봦��������ϵ
 * ������ֻ��Ҫд�����������ɣ������д����ֵ��CommandAnalyzer�᳢�ԴӺ��������н�����������
 * ���ԣ�������������淶��д���������ǿ��Բ��Ӳ�����
 * �������� help ������ handleHelp
 * Ҫ������ֻ��Сд��ĸ��������ɣ���ĸ��ͷ����������������ǰ���handle����������������ĸ��д
 * @author congxiaoyao
 * @version 1.0
 * @date 2016.1.20
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface CommandName {
	
	/**
	 * ֻҪ����������Ϊע��Ĳ���д�뼴����ɴ�����������İ�
	 * @return 
	 */
	public String value() default "";
	
}