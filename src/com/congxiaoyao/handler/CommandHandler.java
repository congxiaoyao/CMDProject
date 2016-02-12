package com.congxiaoyao.handler;

import com.congxiaoyao.cmd.Analysable;
import com.congxiaoyao.cmd.CommandAnalyzerManager;

/**
 * ͳһhandler�ı�׼Ϊ������handler�ṩgetAnalysable������Ҫ������ʵ������Ķ�̬��ӷ���
 *
 * @version 1.0
 * Created by congxiaoyao on 2016/2/13.
 */
public abstract class CommandHandler {

    abstract CommandHandler registerCommands();

    protected Analysable getAnalysable() {
        return CommandAnalyzerManager.getInstance();
    }
}
