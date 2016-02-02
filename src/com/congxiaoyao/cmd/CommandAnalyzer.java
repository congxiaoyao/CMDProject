package com.congxiaoyao.cmd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * ������������һ����һ������ν�Ŀ�ܵĹ���ԭ����ʹ�÷���
 * ��ϣ�����׿���ܹ�Ϊʵ��һ��������ʾ��ϵͳ�ṩһЩ��Ҫ��֧�֣�ʹ�ÿ�ܵ�ʹ���߿���רע�ڴ�������
 * �Ӷ�����������塢���������ҡ�ִ�е�ϸ���ϻ���̫��ʱ��
 * 1����cmd/values.txt�ڶ���һ��������������ĸ�ʽ���ļ���ע��
 * 2�����������ж�������Ĵ�����������ͨ��ע���������
 * 3�������CommandAnalyzer��ʵ��������process�������ɴ���һ���û���������ݣ�����ʹ�ü�����ע��
 * ������С����
 * �������������������		'help' '�鿴������Ϣ'
 * ͨ��ע�����󶨴�������������������ǵĴ�������д������Case��
 * <code>
 * public class Case{
 * 
 * 		<code>@CommandName("help")</code>
 * 		public void foo(){
 * 			//something to do when user input help
 * 		}
 * 
 * 		public static void main(String[] args){
 * 			new CommandAnalyzer(new Case()).process("help");
 * 		}
 * }
 * </code>
 * ������������
 * ����֧�ֶ�����أ��ص�����������ʱ�򣬻���ݴ������������ĸ���������ѡ����ʵĴ����������лص�
 * �����ʶ������󣬻���ȥѰ�Ҳ���������������Я���Ĳ���������ȵĴ��������ص�֮
 * ����Ҳ��������ٴ�Ѱ�Ҳ���ΪCommand��String...���͵Ĵ�����������Ϊ���������Ϳ������������������
 * �����Ȼ�Ҳ�������Ϊ����ʧ��
 * �����ͬһ�������ص�������˶�����������������һ������������˶��������ͬ���������Ĵ�������
 * �ᵼ�������Ĵ�������ʧЧ���뾡�������ظ�
 * 
 * ���ڿɱ��������
 * ��Ҫ���ؿɱ��������Ļص������������Ĳ���������Command���͡���ε�String���ͣ�Ҫ����������û�����Ĳ�������ƥ�䣩��
 * String...���͡������Command��Ϊ�������ͣ�������е�paramCountΪ-1������parameters��String���飩�ĳ���Ϊ׼
 * 
 * ��������Ķ�̬���룬��{@code CommandAnalyzer#addCommand(Command)}����
 * ���ڴ��������Ķ���ֲ����⣬��{@code CommandAnalyzerManager}��ͷע��
 * 
 * �����Զ���������ת��
 * ��������еĲ����ǻ����������͵�һ�� �����ô��ڳߴ�ʱ������Ĳ���ʵ����int�͵�
 * ��ôֻҪ�����������Ĳ������Ͷ���Ϊint��Integer���ͣ�CommandAnalyzer���Զ���String���͵Ĳ���תΪint/Integer��
 * 
 * �������в�������
 * ����һ������,�䴦����������ͨ��OnlyCareע����˵������Ĳ�����ֻ���û���������Ҫ�Ĳ���ʱ�Ż�ص������������
 * 	
 * <code>@CommandName("screen")</code>
 * <code>@OnlyCare("max")</code>
 * <code>
 * public void maxSizeWindow(String arg) {
 * 		//ֻ���û����� ��screen max�� ��ʱ��˺����Żᱻ�ص�
 * 		//����argҲ���Բ�д
 * }
 * <code>
 * @version 1.0
 * @date 2016.1.19
 * @author congxiaoyao
 */

public class CommandAnalyzer implements Analysable
{
	private Object invoker;
	
	private List<Command> commands = new ArrayList<>();
	//����ͨ�����map��������ĸ��commands�в��ң����Ч�ʣ����int[]��¼��startIndex��length����ֵ
	Map<Character, int[]> commandsDirectory = new HashMap<>();	
	
	Map<String, Method> methodsMap = new HashMap<>();

	/**
	 * @param invoker Ҫִ�еķ������ڵ����ʵ��
	 */
	public CommandAnalyzer(Object invoker) {
		this.invoker = invoker;
		initCommandList();
		initMethodsMap();
	}
	
	CommandAnalyzer(Object invoker , List<Command> commands , Map<Character, int[]> commandsDirectory) {
		this.commands = commands;
		this.commandsDirectory = commandsDirectory;
		this.invoker = invoker;
		initMethodsMap();
	}
	
	/**
	 * ��һ���û����붪��������������᳢�Խ�������ַ������ҽ����ɹ���᳢��ִ���������
	 * Ҳ����ֱ��ȥ�����invoker�Ǹ�����ȥ�ҵ���Ӧ�ķ�������
	 * @param content ������������
	 * @return ����������ճɹ�ִ�У��򷵻�true�������������false
	 */
	@Override
	public boolean process(String content) {
		Command command = analyze(content);
		if(command != null) {
			return handleCommand(command);
		}
		return false;
	}
	
	/**
	 * ��cmd/values.txt�ļ��ж�ȡ���õĽű���Ϣ��������Ӧ��command�����������ϲ�������Ŀ¼�����ѯ
	 */
	private void initCommandList() {
		List<String> params = new ArrayList<String>(5);
		new TextReader("cmd/values.txt") {
			@Override
			public void onReadLine(String line) {
				//ȥע�ͼ�����
				if(line.length() == 0 || line.charAt(0) == '%') return;
				
				//��������������������һ��һ���ļ���params�У�����ÿһ�ж����������ݣ���һ�����������һ��������params�е�����
				params.clear();
				int start = line.indexOf('\'');
				while(start != -1) {
					String param = line.substring(start + 1 , start = line.indexOf('\'',start + 1));
					start = line.indexOf('\'',start + 1);
					params.add(param);
				}
				//ѭ����ȡ��������ʼ���ݲ�������������Ӧ��Command����
				int size = params.size();
				Command command = null;
				switch (size) {
				case 1: command = new Command(params.get(0)); break;
				case 2: command = new Command(params.get(0),params.get(1)); break;
				case 3: command = new Command(params.get(0),
						Integer.parseInt(params.get(1)),params.get(2)); break;
				case 4: String delimiter = characterEscape(params.get(2));
				command = new Command(params.get(0),
						Integer.parseInt(params.get(1)),
						delimiter,params.get(3));
				break;
				default: return;
				}
				insertCommand(command);
			}
			
			public void onError(Exception e) {
				
			};
		}.read();
		//����Ŀ¼�Է������
		if(commands.size() != 0) {
			updateCommandsDirectory();
		}
	}
	
	/**
	 * ������commandName���������ĺ�����������������HashMap,������������
	 * ����invoker�����е�method������һ������������method����map������һ��entry�����method��Ϊentry��value
	 * ���ڶ�Ӧ��key������������ɣ�commandName + X��,commandName�����method��ע�ͻ�������ã�X��ָ���
	 * method�Ĳ����������������String...��Command���͵Ĳ�����������ΪXΪ$
	 * ���ڴ���OnlyCareע��Ĵ���������key�����ɹ����ΪcommandName+OnlyCare
	 * ���ڱ�׼�������������������������Ҫ��CommandNameע�����Ӳ���������ͨ�������������������������������
	 * ��׼�������Ҫ������ֻ��Сд��ĸ��������ɣ���ĸ��ͷ������������������ǰ���handle���ɣ�handle�����������������ת����Сд��
	 * �� ������ help ���������� handleHelp
	 */
	private void initMethodsMap() {
		Method[] methods = invoker.getClass().getDeclaredMethods();
		for (Method method : methods) {
			//���˵�û��ע��ķ���
			if(!method.isAnnotationPresent(CommandName.class)) continue;
			//��ȡ�����ע��
			CommandName commandName = method.getAnnotation(CommandName.class);
			String value = commandName.value();
			//���ע����û�в���������ͨ����������������ͷ
			if(value.length() == 0 && isBeginWith("handle", method.getName(), null)) {
				value = method.getName().replaceFirst("handle", "").toLowerCase();
			}
			String key = null;
			//������������Ĵ��ڣ���ͬ�ĺ������ܻᴦ����ͬ����������
			int paramCount = method.getParameterCount();
			//��OnlyCareע�����Ҫ�������������key
			if(method.isAnnotationPresent(OnlyCare.class)) {
				if(paramCount > 1) continue;
				key = value + method.getAnnotation(OnlyCare.class).value();
			}
			//�������������ֻ��һ�������Ҳ���ΪCommand���ͣ��ڹ���mapʱ������ͷ��$��Ϊmap��key
			else if(paramCount == 1) {
				Class<?> type = method.getParameterTypes()[0];
				if(type == Command.class || type == String[].class)
					key = value + "$";
				else key = value + paramCount;
			}
			//����������ͷ�Ӵ��������Ĳ�������Ϊmap��key
			else key = value + paramCount;
			methodsMap.put(key , method);
		}
	}

	/**
	 * ����һ���ַ����Ƿ���һ������������,Ҫ��commandName��ͬ���ܽ�������ͬ�����Ĳ���
	 * @param content ����̨�ύ���ַ���
	 * @return ����ܹ��ɹ�ƥ�䣬����һ��command���󣬷��򷵻�null
	 */
	public Command analyze(String content)
	{
		if(content.length() == 0) return null;
		int[] info = commandsDirectory.get(content.charAt(0));
		if(info == null) return null;
		for(int i = info[0],len = info[1]+i;i<len;i++) {
			Command command = commands.get(i);
			//�ȿ��ǲ��Ǹ����ַ����ǲ��ǵ�ǰ�����������
			if (!isBeginWith(command.commandName, content, command.delimiter)) continue;
			//Ȼ����ȥ�����ͷȥ��������
			String contentNew = content.replaceFirst(command.commandName, "");
			//��ʼ����������
			int paramCount = 0;
			String[] parameters = null;
			//����в�����������ַ����а����Ĳ�������
			if (contentNew.length() > 1) {
				contentNew = contentNew.replaceFirst(command.delimiter, "");
				parameters = contentNew.split(command.delimiter);
				paramCount = parameters.length;
			}
			//�������ĸ����������������涨����ͬ
			if (paramCount == command.paramCount || command.paramCount == -1) {
				command.parameters = parameters;
				return command;
			}
		}
		return null;
	}
	
	/**
	 * ȥinvoker������ȥѰ���command��Ӧ�Ĵ������ķ���������CommandNameע�⡢���������Ĳ������ͼ�������
	 * @param command
	 * @return ��������command�����ɹ�����true���򷵻�false
	 */
	public boolean handleCommand(Command command) {
		Method method = null;
		//����һ�ε�����������в�������
		if(command.paramCount == 1) {
			method = methodsMap.get(command.commandName+command.parameters[0]);
		}
		//û�еĻ�����û�й̶������Ĵ�����������ķ���
		if(method == null) {
			method = methodsMap.get(command.commandName+
					(command.paramCount == -1 ? 
							(command.parameters == null ? 0 : command.parameters.length)
									: command.paramCount));
		}
		//��û�еĻ������Ҳ���ΪCommand�Ĵ�����������ķ���
		if(method == null) {
			method = methodsMap.get(command.commandName + "$");
			if(method == null) return false;
		}
		try {
			int paramCount = method.getParameterCount();
			//ƥ�䵽�ķ���ֻ��һ����ΪCommand���ͻ�String...��
			if(paramCount == 1) {
				Class<?> type = method.getParameterTypes()[0];
				if(type == Command.class) 
					method.invoke(invoker, command);
				else if(type == String[].class) {
					if(command.parameters == null) 
						command.parameters = new String[0];
					method.invoke(invoker, (Object)command.parameters);
				}
				else method.invoke(invoker, toType(command.parameters[0], type));
			}
			//�޲�
			else if (paramCount == 0) {
				method.invoke(invoker);
			}
			//��Σ�����һ�Σ�
			else if(paramCount == command.parameters.length){
				//��String�����ֻ����������͵�ת�������մ������������б���˳��һһת��
				Class<?>[] types = method.getParameterTypes();
				Object[] objects = new Object[types.length];
				for(int i=0,len = objects.length;i<len;i++) {
					objects[i] = toType(command.parameters[i], types[i]);
				}
				method.invoke(invoker, (Object[])objects);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * ��һ��command���뵽commands�����У�����������Ŀ¼map
	 * һ����Ҫ���Ǹ���Ŀ¼������������
	 * @param command
	 */
	private int insertCommand(Command command) {
		if(command == null) return -1;
		Character key = command.commandName.charAt(0);
		int[] info = commandsDirectory.get(key);
		if(info == null) {
			int start = commands.size();
			commands.add(command);
			commandsDirectory.put(key, new int[]{start,1});
			return start;
		}else {
			//��λ�ò��
			for(int i=0,len = commands.size();i<len;i++) {
				if(commands.get(i).commandName.charAt(0) == key) {
					commands.add(i,command);
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * ��������Ŀ¼��ʹ��Ŀ¼map�ܹ���������
	 */
	private void updateCommandsDirectory() {
		if(commands.size() == 1) return;
		//�����źõ�˳����Ŀ¼
		Iterator<Command> iterator = commands.iterator();
		Command temp = iterator.next();
		char lastchar = temp.commandName.charAt(0) , nowchar;
		int len = 1;
		for(int i=1;iterator.hasNext();i++) {
			temp = iterator.next();
			nowchar = temp.commandName.charAt(0);
			if(nowchar != lastchar) {
				int[] info = commandsDirectory.get(lastchar);
				info[0] = i - len;
				info[1] = len;
				len = 0;
			}
			lastchar = nowchar;
			len++;
		}
		int[] info = commandsDirectory.get(lastchar);
		info[0] = commands.size() - len;
		info[1] = len;
	}
	
	/**
	 * ��һ��String���͵Ĳ���arg����Ϊtype���Ͳ���Object���͵���ʽ����
	 * @param arg
	 * @param type
	 * @return
	 */
	private Object toType(String arg, Class<?> type) {
		Object object = arg;
		if (type == byte.class || type == Byte.class) {
			object = Byte.parseByte(arg);
		} else if (type == short.class || type == Short.class) {
			object = Short.parseShort(arg);
		} else if (type == int.class || type == Integer.class) {
			object = Integer.parseInt(arg);
		} else if (type == long.class || type == Long.class) {
			object = Long.parseLong(arg);
		} else if (type == float.class || type == Float.class) {
			object = Float.parseFloat(arg);
		} else if (type == double.class || type == Double.class) {
			object = Double.parseDouble(arg);
		} else if (type == boolean.class || type == Boolean.class) {
			object = Boolean.parseBoolean(arg);
		}
		return object;
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
	
	//��Ҫת����ַ�
	private static String[] ec = {".","$","^","(",")","[","|","{","?","+","*",};
	/**
	 * �������String�Ƿ�Ϊec�������������Ĵ�ת����ַ�������Ǿ͸���ת���
	 * @param ch �����String
	 * @return ת����ch
	 */
	private static String characterEscape(String ch) {
		for (String string : ec) {
			if(string.equals(ch)) {
				ch = "\\"+string;
				break;
			}
		}
		return ch;
	}

	/**
	 * @return ��ʼ�����������
	 */
	@Override
	public List<Command> getCommands() {
		return commands;
	}
	
	/**
	 * ����һ������,�����
	 * @param command
	 */
	@Override
	public void addCommand(Command command) {
		int i = insertCommand(command);
		if(i == -1) return;
		int index = i + 1;
		for(int size = commands.size();index < size;index++) {
			if(command.equals(commands.get(index))) {
				commands.remove(i);
				break;
			}
		}
		updateCommandsDirectory();
	}

	/**
	 * @return ���е����������ƴ�ӳ�һ��string����ʽ����
	 */
	@Override
	public String getCommandsDescription() {
		StringBuilder builder = new StringBuilder();
		for (Command command : commands) {
			builder.append(command.commandName);
			builder.append("\t\t");
			builder.append(command.description);
			builder.append("\n");
		}
		builder.delete(builder.length()-1, builder.length());
		return builder.toString();
	}

	@Override
	public String getCommandInfo(String commandName) {
		List<Command> selected = CommandAnalyzerManager.getCommandsByName
				(commandName, commands, commandsDirectory);
		StringBuilder builder = new StringBuilder();
		for (Command command : selected) {
			builder.append('\n');
			builder.append("commandName-->").append(command.commandName).append('\n');
			builder.append("paramCount-->").append(command.paramCount).append('\n');
			builder.append("delimiter-->").append(command.delimiter).append('\n');
			builder.append("description-->").append(command.description).append('\n');
			Method method = methodsMap.get(command.commandName+
					(command.paramCount == -1 ? 
							(command.parameters == null ? 0 : command.parameters.length)
							: command.paramCount));
			if(method == null) method = methodsMap.get(command.commandName + "$");
			//��������ҵ���
			if(method != null) {
				builder.append("handlingMethod-->").append(method.toGenericString()).append('\n');
			}
			//�п������command��Ӧ�˺ö��handlingMethod��һ��һ���Ұ�
			else {
				Set<Entry<String,Method>> entrySet = methodsMap.entrySet();
				for (Entry<String, Method> entry : entrySet) {
					String key = entry.getKey();
					if(isBeginWith(commandName, key, null)) {
						builder.append("handlingMethod-->")
						.append(entry.getValue().toGenericString()).append('\n');
					}
				}
			}
		}
		return builder.append('\n').toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CommandAnalyzer) {
			return ((CommandAnalyzer)obj).invoker.equals(invoker);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return invoker.hashCode();
	}
	
	@Override
	public String toString() {
		return methodsMap.size()+"";
	}
}