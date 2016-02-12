package com.congxiaoyao.cmd;

import java.util.List;

/**
 * ʵ�ִ˽ӿڵ��������ӵ��ͨ��������̴���һ���û����������
 * �������ܹ��ṩ��Ϊ��������һЩ��������
 * ��νͨ��������̴�����ָ�Ƚ�һ���û�����ʶ��Ϊһ��ָ�����Command������ά����Щ��Ϣ����ͨ������������ȥѰ����Ӧ�Ĵ��������д���
 * ����ֻҪ���û����봫��{@code Analysable#process(String)}�����У����ɷ��������Ӧ�Ĵ�������ɴ���
 * ����������;������ע��
 * 
 * @see CommandAnalyzer
 * @see CommandAnalyzerManager
 * 
 * @version��1.0
 * @author congxiaoyao
 * @date 2016.1.24
 */
public interface Analysable {
	
	/**
	 * ����һ���û����벢������Ӧ����������֮
	 * @param content �û�����
	 * @return ����ɹ�true ����false
	 */
	boolean process(String content);
	
	/**
	 * @return ����ļ���
	 */
	List<Command> getCommands();

	/**
	 * ������������һ������
	 * @param command
	 */
	void addCommand(Command command);
	
	/**
	 * ����������Ƴ�һ������
	 * @param command
	 */
	void removeCommand(Command command);

	/**
	 * @return ���������������Ϣ�����String����ʽ����
	 */
	String getCommandsDescription();

	/**
	 * ͨ����������ȡ�����������Ϣ��String����ʽ����
	 * @param commandName ���е�������ΪcommandName������ᱻ�ҵ�
	 * @return ÿһ�зֱ��� commandName��paramCount��delimiter��handlingMethod
	 */
	String getCommandInfo(String commandName);

	/**
	 * @return �������ĸ���
     */
	int getHandlingMethodSize();

	/**
	 * @return id
     */
	int getId();
}