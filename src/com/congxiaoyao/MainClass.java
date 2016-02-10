package com.congxiaoyao;

import com.congxiaoyao.cmd.*;

public class MainClass {

	private static Analysable analyzer;
	private static CommandWindow window;
	
	public static void main(String[] args) {
		//����һ��CommandWindowʵ������ʾ����
		window = new CommandWindow().setVisible();
		//����ÿһ���û��ύ�����벢����Analysable������
		window.setOnSubmitListener((content -> analyzer.process(content)));

		//������Ĵ���������������ʵ���������Ƕ��
		CommandAnalyzerManager.handleWith(new CommandWindowHandler(window));
		CommandAnalyzerManager.handleWith(new MainClass());

		//��̬����Ӻ�ɾ������
		analyzer = CommandAnalyzerManager.getInstance();
		analyzer.addCommand(new Command("welcome"));
		analyzer.process("welcome");
		analyzer.removeCommand(new Command("welcome"));

//		һ�仰����֧�ִ�����ʾ
//		window.setAssistant(new CodeAssistant(analyzer.getCommands()));
	}

	@CommandName
	public static void handleWelcome() {
		System.out.println("\n\n��demoչʾ�����׿�ܵĻ���ʹ�÷���\n"
				+ "���������һЩ�й�������ڵĲ���������\n"
				+ "�����Ѿ�ʵ������Ӧ���ܣ���������help���в鿴\n"
				+ "���幤��ԭ��ʹ�÷�ʽ��CommandAnalyzer��ͷע��\n"
				+ "\4");
	}
}