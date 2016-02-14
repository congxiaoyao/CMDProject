package com.congxiaoyao.handler;

import com.congxiaoyao.cmd.Analysable;
import com.congxiaoyao.cmd.Command;
import com.congxiaoyao.cmd.CommandName;

/**
 * ����Analysable��getCommandsDescription��getCommandInfo�����ṩ��������
 * 'help'			'����'
 * 'help'	        '1'		'�鿴ÿ���������ϸ��Ϣ'
 *
 * @version 1.0
 * Created by congxiaoyao on 2016/2/13.
 */
public class HelpHandler extends BaseHandler {

    private Analysable analysable;

    public HelpHandler(Analysable analysable) {
        this.analysable = analysable;
    }

    public HelpHandler() {
    }

    @Override
    public Analysable getAnalysable() {
        if (analysable != null) {
            return analysable;
        }
        return super.getAnalysable();
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

    @Override
    BaseHandler registerCommands() {
        Analysable analysable = getAnalysable();
        analysable.addCommand(new Command("help",	"����"));
        analysable.addCommand(new Command("help",	     1,	"�鿴ÿ���������ϸ��Ϣ"));
        return this;
    }
}