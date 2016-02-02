package com.congxiaoyao.cmd;

import java.util.List;

/**
 * ʵ�ִ˽ӿڵ��������ӵ��ͨ��������̴���һ���û����������
 * �������ܹ��ṩ��Ϊ��������һЩ��������
 * ��ν������̣���ָ�Ƚ�һ���û�����ʶ��Ϊһ��ָ�����Command������ά����Щ��Ϣ����ͨ������������ȥѰ�������Ĵ��������д���
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
	public abstract boolean process(String content);
	
	/**
	 * @return ����ļ���
	 */
	public abstract List<Command> getCommands();

	/**
	 * ������������һ������
	 * @param command
	 */
	public abstract void addCommand(Command command);

	/**
	 * @return ���������������Ϣ�����String����ʽ����
	 */
	public abstract String getCommandsDescription();

	/**
	 * ͨ����������ȡ�����������Ϣ��String����ʽ����
	 * @param commandName ���е�������ΪcommandName������ᱻ�ҵ�
	 * @return ÿһ�зֱ��� commandName��paramCount��delimiter��handlingMethod
	 */
	public abstract String getCommandInfo(String commandName);
}