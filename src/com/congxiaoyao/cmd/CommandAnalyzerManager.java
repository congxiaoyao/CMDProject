package com.congxiaoyao.cmd;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Ϊ�˽���������Ķ���ֲ������⣬ʹ�õ����ķ�ʽͨ��CommandAnalyzerManager���������еĴ����������û�����
 * �����ĺô���ʹ�ô��������Ը��õĸ���ص����ھ�����������������
 * �˷���һ��CommandAnalyzerֻ�ܽ���һ�����еĴ�������ȱ�㣬ʹ�ÿ�ܸ���ǿ������
 * @author congxiaoyao
 * @version 1.0
 * @date 2016.1.21
 */

public class CommandAnalyzerManager implements Analysable{
	
	private static CommandAnalyzerManager commandManager = null;
	private Set<CommandAnalyzer> analyzers = null;

	/**
	 * ����ģʽ����ȡCommandManager��ʵ��
	 * @param handlingObject ���д����������ʵ��
	 * @return CommandManager
	 */
	public static CommandAnalyzerManager handleWith(Object handlingObject) {
		CommandAnalyzerManager manager = getInstance();
		synchronized (manager) {
			manager.addHandlingObject(handlingObject);
		}
		return manager;
	}
	
	/**
	 * @return ����ģʽ����ȡCommandManager��ʵ��
	 */
	public static CommandAnalyzerManager getInstance() {
		if(commandManager == null) {
			synchronized (CommandAnalyzerManager.class) {
				if(commandManager == null)
					commandManager = new CommandAnalyzerManager();
			}
		}
		return commandManager;
	}
	
	public CommandAnalyzerManager() {
		analyzers = new HashSet<>();
	}
	
	/**
	 * ���һ��CommandAnalyzer�����analyzer�Ĵ�����λ��handlingObject��
	 * @param handlingObject
	 */
	public void addHandlingObject(Object handlingObject) {
		List<Command> commands = getCommands();
		CommandAnalyzer analyzer = null;
		if (commands == null) {
			analyzer = new CommandAnalyzer(handlingObject);
		}else {
			analyzer = new CommandAnalyzer(handlingObject, commands,
					analyzers.iterator().next().commandsDirectory);
		}
		analyzers.add(analyzer);
	}

	/**
	 * @return �����
	 */
	@Override
	public List<Command> getCommands() {
		if(analyzers.size() == 0) return null;
		Iterator<CommandAnalyzer> iterator = analyzers.iterator();
		CommandAnalyzer analyzer = iterator.next();
		return analyzer.getCommands();
	}
	
	/**
	 * @return �����ֵ䣬����΢�����һ�²���Ч��
	 */
	public Map<Character, int[]> getCommandsDirectory(){
		if(analyzers.size() == 0) return null;
		Iterator<CommandAnalyzer> iterator = analyzers.iterator();
		CommandAnalyzer analyzer = iterator.next();
		return analyzer.commandsDirectory;
	}
	
	/**
	 * ���һ��������CommandManager��δ����CommandAnalyzer��������ʧЧ
	 * @param command
	 */
	@Override
	public void addCommand(Command command) {
		if(analyzers.size() == 0) return;
		analyzers.iterator().next().addCommand(command);
	}
	
	@Override
	public void removeCommand(Command command) {
		if(analyzers.size() == 0) return;
		analyzers.iterator().next().removeCommand(command);
	}
	
	/**
	 * ����һ���û���������ݣ�������ָ�ɵ�ά�������е�CommandAnalyzer��ȥ���Դ���ֱ�������ܹ�������
	 * @param content ��ʶ���ָ��
	 */
	@Override
	public boolean process(String content) {
		if(analyzers.size() == 0) return false;
		Command command = analyzers.iterator().next().analyze(content);
		if(command != null) {
			for (CommandAnalyzer commandAnalyzer : analyzers) {
				if(commandAnalyzer.handleCommand(command)) return true;
			}
		}
		return false;
	}
	
	/**
	 * @return ���е����������ƴ�ӳ�һ��string����ʽ����
	 */
	@Override
	public String getCommandsDescription() {
		for (CommandAnalyzer commandAnalyzer : analyzers) {
			return commandAnalyzer.getCommandsDescription();
		}
		return null;
	}

	@Override
	public String getCommandInfo(String commandName) {
		StringBuilder builder = new StringBuilder();
		List<Command> selected = getCommandsByName(commandName, getCommands(),getCommandsDirectory());
		for (Command command : selected) {
			builder.append('\n');
			builder.append("commandName-->").append(command.commandName).append('\n');
			builder.append("paramCount-->").append(command.paramCount).append('\n');
			builder.append("delimiter-->").append(command.delimiter).append('\n');
			builder.append("description-->").append(command.description).append('\n');
			for (CommandAnalyzer analyzer : analyzers) {
				Method method = analyzer.methodsMap.get(command.commandName+
						(command.paramCount == -1 ? 
								(command.parameters == null ? 0 : command.parameters.length)
										: command.paramCount));
				if(method == null) method = analyzer.methodsMap.get(command.commandName + "$");
				if(method != null) {
					builder.append("handlingMethod-->").append(method.toGenericString()).append('\n');
					break;
				}
				//�п������command��Ӧ�˺ö��handlingMethod��һ��һ���Ұ�
				else {
					Set<Entry<String,Method>> entrySet = analyzer.methodsMap.entrySet();
					for (Entry<String, Method> entry : entrySet) {
						String key = entry.getKey();
						if(CommandAnalyzer.isBeginWith(commandName, key, null)) {
							builder.append("handlingMethod-->")
							.append(entry.getValue().toGenericString()).append('\n');
						}
					}
				}
			}
		}
		if(builder.length() == 0) {
			if(commandName.equals("-all")) {
				List<Command> commands = getCommands();
				Set<String> commandNames = new HashSet<>(commands.size());
				for (Command command : commands) {
					commandNames.add(command.commandName);
				}
				for (String string : commandNames) {
					builder.append(getCommandInfo(string));
				}
				builder.append("\n");
			}
		}
		return builder.toString();
	}
	
	public Set<CommandAnalyzer> getAnalyzers() {
		return analyzers;
	}
	
	/**
	 * ��������ΪcommandName������ȫ����commands��������
	 * @param commandName
	 * @param commands
	 * @param directory
	 * @return
	 */
	public static List<Command> getCommandsByName(String commandName, List<Command> commands,
			Map<Character, int[]> directory) {
		List<Command> selected = new ArrayList<>(1);
		if (commandName == null || commandName.length() == 0)
			return null;

		char key = commandName.charAt(0);
		int[] info = directory.get(key);
		if (info != null) {
			for (int i = info[0], len = info[0] + info[1]; i < len; i++) {
				Command command = commands.get(i);
				if(command.commandName.equals(commandName))
					selected.add(commands.get(i));
			}
		}
		return selected;
	}
}