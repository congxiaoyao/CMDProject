package com.congxiaoyao.cmd;

import com.congxiaoyao.cmd.CommandWindow.OnSubmitListener;

import javax.swing.*;

/**
 * ��ҪΪ�˴����CommandWindow�Ĳ�����������������˳������ô��ڴ�С��ʾ���
 * �����Ļ���Щ������Ӧ�������CommandWindow���ڵ����У�������Ҫ�ǵ�����cmd���ʹ�õ�һ����demo
 * ֧�ֶ�̬��Ӳ���CommandWindow��������û������Ӧ�ļ�����������ɵ��ô˺������
 * @see #registerCommands(Analysable)
 * 
 * @author congxiaoyao
 * @date 2016.2.2
 * @version 1.1
 */
public class CommandWindowHandler {
	
	private CommandWindow window;

	public CommandWindowHandler(CommandWindow window) {
		this.window = window;
	}

	private static Analysable getAnalyzer() {
		return CommandAnalyzerManager.getInstance();
	}
	
	/**
	 * CommandNameע��û������Ҳ�ǿ��Ե� ����Ҫ��handle��ͷ
	 */
	@CommandName
	public void handleExit() {
		window.closeWindow();
	}
	
	@CommandName("restart")
	public void restartWindow() {
		window.closeWindow();
		window = new CommandWindow().setVisible();
		window.setOnSubmitListener(new OnSubmitListener() {
			@Override
			public void onSubmit(String content) {
				getAnalyzer().process(content);
			}
		});
	}
	
	@CommandName
	public void handleVersion() {
		System.out.println("1.0");
	}
	
	@CommandName
	public void handle720P() {
		CommandAnalyzerManager.getInstance().process("bound 1280 720");
		getAnalyzer().process("font 20");
	}
	
	/**
	 * �������ν�Ĳ����������ԣ����ص���window�����max����
	 * @param arg
	 */
	@CommandName("window")
	@OnlyCare("max")
	public void maxSizeWindow(String arg) {
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	@CommandName("window")
	@OnlyCare("full")
	public void handleFull() {
		int size = window.getFontSize();
		window.closeWindow();
		window = new CommandWindow(true);
		window.setFontSize(size);
		window.setUndecorated(true);
		window.setVisible();
		window.setOnSubmitListener(new OnSubmitListener() {
			@Override
			public void onSubmit(String content) {
				getAnalyzer().process(content);
			}
		});
	}
	
	@CommandName("window")
	@OnlyCare("nobar")
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
				getAnalyzer().process(content);
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
	
	@CommandName
	public void handleHint(String arg) {
		window.setHint(arg);
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
	public void handleHelp() {
		window.printlnSmoothly(getAnalyzer().getCommandsDescription());
	}
	
	/**
	 * ���������������Ĵ�������Ϊ�˴����������һ��help���޲�help���ģ��ɵ���������������Ҳ�����ص�
	 * ��Ȼ�˴�������һ��Ҳͬʱ���أ�ֻҪCommandNameע���ע�ã����ɸ��ݲ���������λ��Ӧ������
	 * @param commandName
	 */
	@CommandName
	public void handleHelp(String commandName) {
		String result = getAnalyzer().getCommandInfo(commandName);
		window.printlnSmoothly(result);
	}

	public void registerCommands(Analysable analysable) {

		analysable.addCommand(new Command("720p",	"���ô��ڳߴ�Ϊ720P"));
		analysable.addCommand(new Command("cls",	"����"));
		analysable.addCommand(new Command("exit",	"�˳�"));
		analysable.addCommand(new Command("help",	"����"));
		analysable.addCommand(new Command("version","�汾��"));

		analysable.addCommand(new Command("help",	1,	"�鿴ÿ���������ϸ��Ϣ"));
		analysable.addCommand(new Command("height",	1,	"���ô��ڸ߶�"));
		analysable.addCommand(new Command("font",	1,	"���������С"));
		analysable.addCommand(new Command("close",	1,	"�����ܷ������رմ���"));
		analysable.addCommand(new Command("window",	1,	"max��full��nobar"));
		
		analysable.addCommand(new Command("bound",	2,	"���ô��ڿ��"));
		
		analysable.addCommand(new Command("hint",	1,		"`",	"����hint����"));
	}
}