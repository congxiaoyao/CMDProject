package com.congxiaoyao.handler;

import com.congxiaoyao.cmd.*;

import java.io.File;

/**
 * ����Analysable��getCommandsDescription��getCommandInfo�����ṩ��������
 * ����CommandAnalyzer�Ķ�̬������ʵ����һЩ������ʹ��ʹ���߿���������ʱ������䴦��������ɾ����
 * 
 * ֧�ֵ�������
 * 'help'			'����'
 * 'help'	        '1'		'�鿴ÿ���������ϸ��Ϣ'
 * 'remana'         '1'     'ͨ��id�Ƴ�һ��CommandAnalyzer'
 * 'handle_with'    '1'     '����Ϊ���������� ��com.cmd.Command'
 * 'addcmd'   '4'   '`'     '�������`������`��������`�ָ���`����'
 * 'addcmd'   '4'   ' '     '�������:������`��������`�ָ���`����'
 * 'delcmd'   '3'   '`'     'ɾ������`������`��������`�ָ���'
 * 'delcmd'   '3'   ' '     'ɾ������:������`��������`�ָ���'
 * 
 * Created by congxiaoyao on 2016/2/12.
 * @version 1.0
 */
public class CommandManagementHandler extends CommandHandler{

    /**
     * ����������ʱ���һ�����ʹ��Analysable����ά������command
     * @param commandName
     * @param paramCount
     * @param delimiter
     * @param description
     */
    @CommandName("addcmd")
    public void addCommand(String commandName, int paramCount, String delimiter, String description) {
        getAnalysable().addCommand(new Command(commandName, paramCount, delimiter, description));
        System.out.println("ok");
    }

    /**
     * ������ʱͨ����������ɾ��һ������
     * ע�⣬���ͨ����������ɾ�����Լ����ڲ���ȡ��ʩ������½��޷���ɾ������������
     * @param commandName
     * @param paramCount
     * @param delimiter
     */
    @CommandName("delcmd")
    public void deleteCommand(String commandName, int paramCount, String delimiter) {
        getAnalysable().removeCommand(new Command(commandName, paramCount, delimiter, ""));
        System.out.println("ok");
    }

    /**
     * ������ʱ��Ӵ����������ͨ��addcmd�����һ������Ϳ�����ͨ���������������Ĵ�����
     * ��������ǰ�ķ�ʽ��дһ������������󽫴����������������(ȫ��)��Ϊ����������뼴��
     * ����ǽ��µĴ�����д���µ����У�����������в��ȶ�
     * @param className
     * @throws Exception
     */
    @CommandName("handle_with")
    public void addHandlingMethod(String className) throws Exception {
        String classPath = new File("bin").getAbsolutePath();
        DynamicClassLoader classLoader =
                new DynamicClassLoader(DynamicClassLoader.class.getClassLoader());
        Class<?> objectClass = classLoader.loadClass(classPath, className);
        Object handlingObject = objectClass.newInstance();
        CommandAnalyzerManager.handleWith(handlingObject);
        System.out.println("ok");
    }

    /**
     * ������ʱͨ��idɾ��һ��CommandAnalyzer��ʹ������ά�������д�����ʧЧ
     * ����addHandlingMethod���ᵽ��ý��µĴ�����д���µ����У�����������в��ȶ�
     * ���ִ��Ҫ���µĴ�����д��ɵ����л�ı�ɵ����еĴ������ĺ����壬������ͨ��������ɾ���ɵ�analyzer
     * @param id
     */
    @CommandName("remana")
    public void removeAnalyzer(int id) {
        CommandAnalyzerManager.getInstance().removeHandlingObject(id);
        System.out.println("ok");
    }

    /**
     * ���������Ϣ
     */
    @CommandName
    public void handleHelp() {
        System.out.println("\5" + getAnalysable().getCommandsDescription());
    }

    /**
     * ���ĳ���ض�ָ�����ϸ������Ϣ ����-allΪ���ȫ���������ϸ������Ϣ
     * ǿ���¿������ ���������������Ĵ����ֱ������������һ��help���޲�help��
     * �ɵ���������������Ҳ�����ص� ��Ȼ�˴�������һ��Ҳͬʱ����
     * ֻҪCommandNameע���ע�ã����ɸ��ݲ���������λ��Ӧ������
     * @param commandName
     */
    @CommandName
    public void handleHelp(String commandName) {
        System.out.println("\5" + getAnalysable().getCommandInfo(commandName));
    }

    /**
     * �����ͨ���ļ���������Ļ���Ҳ���Ե������������̬�������Щ����
     * ���������ӵ���������Ĵ������Ѿ���CommandHandler�еĴ�����ʵ����
     */
    @Override
    public CommandHandler registerCommands() {
        Analysable analysable = getAnalysable();
        analysable.addCommand(new Command("help",	"����"));
        analysable.addCommand(new Command("help",	     1,	"�鿴ÿ���������ϸ��Ϣ"));
        analysable.addCommand(new Command("remana",      1, "ͨ��id�Ƴ�һ��CommandAnalyzer"));
        analysable.addCommand(new Command("handle_with", 1, "����Ϊ���������� ��com.cmd.Command"));
        analysable.addCommand(new Command("addcmd", 4, "`", "�������`������`��������`�ָ���`����"));
        analysable.addCommand(new Command("addcmd", 4, " ", "�������:������`��������`�ָ���`����"));
        analysable.addCommand(new Command("delcmd", 3, "`", "ɾ������`������`��������`�ָ���"));
        analysable.addCommand(new Command("delcmd", 3, " ", "ɾ������:������`��������`�ָ���"));
        return this;
    }
}