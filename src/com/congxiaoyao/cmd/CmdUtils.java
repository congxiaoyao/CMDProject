package com.congxiaoyao.cmd;

import java.util.ArrayList;

/**
 * ���������õĹ�����
 *
 * @version 1.0
 * Created by congxiaoyao on 2016/2/13.
 */
public class CmdUtils {

    private static ArrayList<String> params = new ArrayList<>(5);

    /**
     * @param command
     * @return ͨ��һ�������������һ���������ļ��ж��������ַ���
     */
    public static String parseCommandIntoString(Command command) {
        return command.toDefinitionString();
    }

    /**
     * @param cmd
     * @return ���������ļ��е�һ���������ΪCommand����
     */
    public static Command getCommand(String cmd) {
        //��������������������һ��һ���ļ���params��,��һ�����������һ�ο���������params�е�����
        params.clear();
        int start = cmd.indexOf('\'');
        while(start != -1) {
            String param = cmd.substring(start + 1 , start = cmd.indexOf('\'',start + 1));
            start = cmd.indexOf('\'',start + 1);
            params.add(param);
        }
        //ѭ����ȡ��������ʼ���ݲ�������������Ӧ��Command����
        int size = params.size();
        Command command = null;
        switch (size) {
            case 1: command = new Command(params.get(0)); break;
            case 2: command = new Command(params.get(0),params.get(1)); break;
            case 3: command = new Command(params.get(0), Integer.parseInt(params.get(1)),
                    params.get(2)); break;
            case 4: String delimiter = characterEscape(params.get(2));
                command = new Command(params.get(0),
                        Integer.parseInt(params.get(1)),
                        delimiter,params.get(3));
                break;
            default: break;
        }
        return command;
    }

    //��Ҫת����ַ�
    private static String[] ec = {".","$","^","(",")","[","|","{","?","+","*",};
    /**
     * �������String�Ƿ�Ϊec�������������Ĵ�ת����ַ�������Ǿ͸���ת���
     * @param ch �����String
     * @return ת����ch
     */
    static String characterEscape(String ch) {
        for (String string : ec) {
            if(string.equals(ch)) {
                ch = "\\"+string;
                break;
            }
        }
        return ch;
    }

    /**
     * �ж������Ƿ��Ը����ĵ��ʿ�ʼ
     * @param beginWord �����Ŀ�ʼ����
     * @param command �������
     * @param delimiter �ָ����������Ϊnull���ж�command�д�0���ָ���֮ǰ�������ǲ���beginWord������ֱ���ж�
     * @return �Ը����ĵ��ʿ�ʼ����true
     */
    static boolean isBeginWith(String beginWord, String command, String delimiter) {
        if (beginWord.length() > command.length()) {
            return false;
        }
        if (command.equals(beginWord)) {
            return true;
        }
        return delimiter == null ? command.substring(0, beginWord.length()).equals(beginWord)
                : command.split(delimiter)[0].equals(beginWord);
    }

    /**
     * �õ��򻯰�ķ���ǩ���紫��static String com.congxiaoyao.cmd.CmdUtils.getSimpleMethodSignature(String)
     * ���᷵��static String getSimpleMethodSignature(String)
     * @param completely
     * @return
     */
    static String getSimpleMethodSignature(String completely) {
        String[] methodInfo= completely.split(" ");
        String methodName = methodInfo[methodInfo.length - 1];
        int end = methodName.indexOf('(');
        methodName = methodName.substring(0, end);
        String[] split = methodName.split("\\.");
        String simple = split[split.length - 1];
        simple = completely.replace(methodName,simple);
        return simple;
    }

    /**
     * ��command�е��ĸ��ֶε�����׷�ӵ�StringBuilder��
     * @param command
     * @param builder
     */
    static void appendCommandAttribute(Command command, StringBuilder builder) {
        builder.append("commandName-->").append(command.commandName).append('\n');
        builder.append("paramCount-->").append(command.paramCount).append('\n');
        builder.append("delimiter-->").append(command.delimiter).append('\n');
        builder.append("description-->").append(command.description).append('\n');
    }
}