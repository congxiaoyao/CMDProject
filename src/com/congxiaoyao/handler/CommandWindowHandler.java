package com.congxiaoyao.handler;

import com.congxiaoyao.cmd.*;
import com.congxiaoyao.cmd.CommandWindow.OnSubmitListener;

import javax.swing.*;

/**
 * ��ҪΪ�˴����CommandWindow�Ĳ�����������������˳������ô��ڴ�С��ʾ���
 * �����Ļ���Щ������Ӧ�������CommandWindow���ڵ����У�������Ҫ�ǵ�����cmd���ʹ�õ�һ����demo
 *
 * ֧�ֵ�������
 * '720p'			'���ô��ڳߴ�Ϊ720P'
 * 'cls'			'����'
 * 'exit'			'�˳�'
 * 'version'		'�汾��'
 * 'height'	'1'		'���ô��ڸ߶�'
 * 'font'	'1'		'���������С'
 * 'close'	'1'		'�����ܷ������رմ���'
 * 'window'	'1'		'max��full��nobar'
 * 'bound'	'2'		'���ô��ڿ��'
 * 'hint'	'1'	'`'	'����hint����'
 * 
 * @author congxiaoyao
 * @date 2016.2.2
 * @version 1.2
 */
public class CommandWindowHandler extends BaseHandler {
	
	private CommandWindow window;

	public CommandWindowHandler(CommandWindow window) {
		this.window = window;
	}

	@CommandName("restart")
	public void restartWindow() {
		window.closeWindow();
		window = new CommandWindow().setVisible();
		window.setOnSubmitListener(content -> getAnalysable().process(content));
	}

	/**
	 * CommandNameע��û������Ҳ�ǿ��Ե� ����Ҫ��handle��ͷ
	 */
	@CommandName
	public void handleExit() {
		window.closeWindow();
	}

	@CommandName
	public void handleVersion() {
		System.out.println("v1.2.1");
	}

	@CommandName
	public void handle720P() {
		CommandAnalyzerManager.getInstance().process("bound 1280 720");
		getAnalysable().process("font 20");
	}

	/**
	 * �������ν�Ĳ����������ԣ����ص���window�����max����
	 */
    @OnlyCare("max")
	@CommandName("window")
	public void maxSizeWindow() {
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

    @OnlyCare("full")
	@CommandName("window")
	public void handleFull() {
		int size = window.getFontSize();
		window.closeWindow();
		window = new CommandWindow(true);
		window.setFontSize(size);
		window.setUndecorated(true);
		window.setVisible();
		window.setOnSubmitListener(content -> getAnalysable().process(content));
	}

	@OnlyCare("nobar")
	@CommandName("window")
	public void handleNoBar() {
		int size = window.getFontSize();
		window.closeWindow();
		window = new CommandWindow(window.getWidth(),window.getHeight());
		window.setFontSize(size);
		window.setUndecorated(true);
		window.setVisible();
		window.setOnSubmitListener(new OnSubmitListener() {
			@Override
			public void onSubmit(String content) {
				getAnalysable().process(content);
			}
		});
	}

	@CommandName("cls")
	public void clearCommandWindow() {
		window.clearCommandWindow();
	}
	
	/**
	 * �������ν���Զ���������ת�����ɽ��û��ĺϷ������Զ�ת��Ϊint�ͷ���ʹ��
	 * @param height
	 */
	@CommandName("height")
	public void setWindowHeight(int height) {
		window.setCommandHeight(height);
	}
	
	@CommandName("font")
	public void setFontSize(int size) {
		window.setFontSize(size);
	}
	
	/**
	 * ����ֵ���͵Ĳ���Ҳ���Զ�����ת�� ���û�����true/falseʱ���Զ�ת��Ϊ��������
	 * @param can
	 */
	@CommandName("close")
	public void setWindowDefaultCloseOperation(boolean can) {
		if(can) {
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			System.out.println("���óɹ� ��ͨ�����رմ���");
		}else {
			window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			System.out.println("���óɹ� �ѽ�ֹ���رմ���");
		}
	}

	@CommandName("bound")
	public void setWindowBounds(int w , int h) {
		window.setBounds(w, h);
	}

	@CommandName
	public void handleHint(String arg) {
		window.setHint(arg);
	}

	@CommandName("ecc")
	public void enableCodeCompletion(boolean enable) {
		if (enable) {
			window.setAssistant(new CodeAssistant(getAnalysable().getCommands()));
		}else {
			window.setAssistant(null);
		}
	}

	/**
	 * �����ͨ���ļ���������Ļ���Ҳ���Ե������������̬�������Щ����
	 * ���������ӵ���������Ĵ������Ѿ���CommandWindowHandler�еĴ�����ʵ����
     */
	@Override
	public BaseHandler registerCommands() {
		Analysable analysable = getAnalysable();
		analysable.addCommand(new Command("720p",	"���ô��ڳߴ�Ϊ720P"));
		analysable.addCommand(new Command("cls",	"����"));
		analysable.addCommand(new Command("exit",	"�˳�"));
		analysable.addCommand(new Command("version","�汾��"));

		analysable.addCommand(new Command("height",	1,	"���ô��ڸ߶�"));
		analysable.addCommand(new Command("font",	1,	"���������С"));
		analysable.addCommand(new Command("close",	1,	"�����ܷ������رմ���"));
		analysable.addCommand(new Command("window",	1,	"max��full��nobar"));

		analysable.addCommand(new Command("bound",	2,	"���ô��ڿ��"));

		analysable.addCommand(new Command("hint",	1,	"`",	"����hint����"));
		return this;
	}
}