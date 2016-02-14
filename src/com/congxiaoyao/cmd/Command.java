package com.congxiaoyao.cmd;

import java.util.Arrays;

/**
 * ����cmd�ļ���������values.cmd�ļ������úõ����е�����������������ʽ�����й���
 * @author congxiaoyao
 * @date 2016.1.19
 * @version 1.4
 */

public class Command
{
	public String commandName;
	public int paramCount;
	public String delimiter;
	public String description;
	public String[] parameters;

	/**
	 * ����ǿ�ƹ涨��һ������ĸ�ʽ ���磺[commandName][delimiter][parameter][delimiter][parameter]...
	 * @param commandName ����ͷ
	 * @param paramCount ��������
	 * @param delimiter �ָ���
	 * @param description �������������ע�ͣ�Ҳ���ǰ�����Ϣ
	 */
	public Command(String commandName, int paramCount, String delimiter, String description) {
		this.commandName = commandName;
		this.paramCount = paramCount;
		this.delimiter = delimiter;
		this.description = description;
	}

	public Command(String commandName , int paramCount , String description){
		this(commandName,paramCount," ",description);
	}

	public Command(String commandName ,  String description){
		this(commandName, 0, description);
	}
	
	public Command(String commandName) {
		this(commandName, ".");
	}

	@Override
	public String toString() {
		return "Command{" +
				"commandName='" + commandName + '\'' +
				", paramCount=" + paramCount +
				", delimiter='" + delimiter + '\'' +
				", description='" + description + '\'' +
				", parameters=" + Arrays.toString(parameters) +
				'}';
	}

	/**
	 * ���ɿ����������ļ��ж������������ַ���
	 */
	public String toDefinitionString() {
		StringBuilder builder = new StringBuilder("'");
		builder.append(commandName).append("'\t'");
		builder.append(paramCount).append("'\t'");
		builder.append(delimiter).append("'\t'");
		builder.append(description).append("'");
		return builder.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Command) {
			Command command = (Command) obj;
			if(command.commandName.equals(commandName) 
					&&command.paramCount == paramCount && command.delimiter.equals(delimiter)) {
				return true;
			}
		}
		return false;
	}
}