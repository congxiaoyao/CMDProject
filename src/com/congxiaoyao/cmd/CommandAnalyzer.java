package com.congxiaoyao.cmd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.Map.Entry;

/**
 * ������������һ����һ������ν�Ŀ�ܵĹ���ԭ��ʹ�÷���
 * ��ϣ�����׿���ܹ�Ϊʵ��һ��������ʾ��ϵͳ�ṩһЩ��Ҫ��֧�֣�ʹ�ÿ�ܵ�ʹ���߿���רע�ڴ�������
 * �Ӷ�����������塢���������ҡ�ִ�е�ϸ���ϻ���̫��ʱ��
 * 1����cmd/values.txt�ڶ���һ��������������ĸ�ʽ���ļ���ע��
 * 2�����������ж�������Ĵ���������ͨ��ע���������
 * 3�������CommandAnalyzer��ʵ��������{@code process(String)}�������ɴ���һ���û���������ݣ�����ʹ�ü�����ע��
 * ������С���ӣ�
 * ��values.txt�ж������������
 * <hr><pre>  'help' '�鿴������Ϣ'
 * ͨ��ע��<code>@CommandName</code>�󶨴�����������
 * public class Case{
 * 		<code>@CommandName("help")
 * 		public void foo(){
 * 			//something to do when user input help
 * 		}
 * 
 * 		public static void main(String[] args){
 * 			new CommandAnalyzer(new Case()).process("help");
 * 		}
 * }
 * </pre><hr>
 * <p>������������</p>
 * ����֧�ֶ�����أ��ص���������ʱ�򣬻���ݴ����������ĸ���������ѡ����ʵĴ��������лص�
 * �����ʶ������󣬻���ȥѰ�Ҳ���������������Я���Ĳ���������ȵĴ������ص�֮
 * ����Ҳ��������ٴ�Ѱ�Ҳ���ΪCommand��String...���͵Ĵ���������Ϊ���������Ϳ������������������
 * �����Ȼ�Ҳ�������Ϊ����ʧ��
 * �����ͬһ�������ص�������˶���������������һ������������˶��������ͬ���������Ĵ�����
 * �ᵼ�������Ĵ�����ʧЧ���뾡�������ظ�
 * 
 * <p>���ڿɱ��������</p>
 * �ɱ����������ָ�ڲ��������ʱ�򲢲�����׼ȷ�Ĳ���������paramCountҪ��Ϊ-1�����û��伸���Ҿʹ������������Ļ�
 * ��Ҫ���ؿɱ��������������Ĳ���������Command���͡�����Ҷ���String���ͣ�Ҫ����������û�����Ĳ�������ƥ�䣩
 * ��String...���͡������Command��Ϊ�������ͣ�������е�paramCountΪ-1������parameters��String���飩�ĳ���Ϊ׼
 *
 * <p>���ڶ�̬����</p>
 * ֧������Ķ�̬���룬����ͨ�������������������һ������ ��{@code CommandAnalyzer#addCommand(Command)}
 * ֧������Ķ�̬ɾ��������ͨ����������������ɾ��һ�������{@code CommandAnalyzer#removeCommand(Command)}
 * ֧�ִ������Ķ�̬��ӣ�ͨ��{@code DynamicClassLoader}ʵ����class�ļ����ȼ���
 * ��̬����ʹ�ó�����������֮����Ȼ���Զ�̬����ӡ�ɾ�������������Ըı�����Ĵ���ʽ����Ĳ���
 *
 * <p>���ڴ������Ķ���ֲ����⣬��{@code CommandAnalyzerManager}��ͷע��</p>
 * 
 * <p>�����Զ���������ת��</p>
 * ��������еĲ����ǻ����������͵�һ�� �����ô��ڳߴ�ʱ������Ĳ���ʵ����int�͵�
 * ��ôֻҪ���������Ĳ������Ͷ���Ϊint��Integer���ͣ�CommandAnalyzer���Զ���String���͵Ĳ���תΪint/Integer��
 * 
 * <p>�������в�������</p>
 * ����һ������,�䴦��������ͨ��OnlyCareע����˵������Ĳ�����ֻ���û�����ע����ĵĲ���ʱ�Ż�ص������������
 * 	
 * <hr><pre>
 * <code>@CommandName("screen")
 * <code>@OnlyCare("max")
 * public void maxSizeWindow(String arg) {
 * 		//ֻ���û����� ��screen max�� ��ʱ��˺����Żᱻ�ص�
 * 		//����argҲ���Բ�д
 * }
 * </pre><hr>
 * 
 * @version 1.4
 * @date 2016.1.19
 * @author congxiaoyao
 */

public class CommandAnalyzer implements Analysable
{
	private int id = -1;
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
		List<String> params = new ArrayList<>(5);
		new TextReader("cmd/values.txt") {
			@Override
			public void onReadLine(String line) {
				//ȥע�ͼ�����
				if(line.length() == 0 || line.charAt(0) == '%') return;
				Command command = CmdUtils.getCommand(line);
				insertCommand(command);
			}
			public void onError(Exception e) {
				e.printStackTrace();
			};
		}.read();
		//����Ŀ¼�Է������
		if(commands.size() != 0) {
			updateCommandsDirectory();
		}
	}
	
	/**
	 * ������commandName���������ĺ���������������HashMap,������������
	 * ����invoker�����е�method������һ������������method����map�����һ��entry�����method��Ϊentry��value
	 * ���ڶ�Ӧ��key������������ɣ�commandName + X��,commandName�����method��ע�ͻ�������ã�X��ָ���
	 * method�Ĳ����������������String...��Command���͵Ĳ�����������ΪXΪ$
	 * ���ڴ���OnlyCareע��Ĵ�������key�����ɹ����ΪcommandName+OnlyCare
	 * ���ڱ�׼�����������������������Ҫ��CommandNameע����Ӳ���������ͨ������������������������������
	 * ��׼�������Ҫ������ֻ��Сд��ĸ��������ɣ���ĸ��ͷ����������������ǰ���handle���ɣ�handle�����������������ת����Сд��
	 * �� ������ help �������� handleHelp
	 */
	private void initMethodsMap() {
		Method[] methods = invoker.getClass().getDeclaredMethods();
		for (Method method : methods) {
			//ֻ����public����
			int modifiers = method.getModifiers();
			if (modifiers != 1 && modifiers != 9 && modifiers != 25) continue;
			//���˵�û��ע��ķ���
			if(!method.isAnnotationPresent(CommandName.class)) continue;
			//��ȡ�����ע��
			CommandName commandName = method.getAnnotation(CommandName.class);
			String value = commandName.value();
			//���ע����û�в���������ͨ����������������ͷ
			if(value.length() == 0 && CmdUtils.isBeginWith("handle", method.getName(), null)) {
				value = method.getName().replaceFirst("handle", "").toLowerCase();
			}
			String key = null;
			//������������Ĵ��ڣ���ͬ�ĺ������ܻᴦ��ͬ����������
			int paramCount = method.getParameterCount();
			//�����������ֻ��һ�������Ҳ���ΪCommand��String[]���ͣ��ڹ���mapʱ������ͷ��$��Ϊmap��key
			if(paramCount == 1) {
				Class<?> type = method.getParameterTypes()[0];
				if(type == Command.class || type == String[].class)
					key = value + "$";
				else key = value + paramCount;
			}
			//����������ͷ�Ӵ������Ĳ�������Ϊmap��key
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
			if (!CmdUtils.isBeginWith(command.commandName, content, command.delimiter)) continue;
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
	 * ȥinvoker������ȥѰ���command��Ӧ�Ĵ������ķ���������CommandNameע�⡢�������Ĳ������ͼ�������
	 * @param command
	 * @return ��������command����ɹ�����true���򷵻�false
	 */
	public boolean handleCommand(Command command) {
		Method method = null;
		//����һ������Ĵ����������Զ���Ϊ�޲κ�����OnlyCareע�����ʽ������һ������ȳ���Ѱ���޲δ�����
		if(command.paramCount == 1) {
			method = methodsMap.get(command.commandName+"0");
			if (method != null) {
				if (!method.isAnnotationPresent(OnlyCare.class)) {
					method = null;
				}
			}
		}
		//û�еĻ�����û�й̶������Ĵ�����������ķ���
		if(method == null) {
			method = methodsMap.get(command.commandName+
					(command.paramCount == -1 ?
							(command.parameters == null ? 0 : command.parameters.length)
							: command.paramCount));
		}
		//��û�еĻ������Ҳ���ΪCommand��String...�Ĵ�����������ķ���
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
				else {
					if (!checkIfOnlyCareCanPass(method, command)) return false;
					method.invoke(invoker, toType(command.parameters[0], type));
				}
			}
			//�޲�
			else if (paramCount == 0) {
				//�����޲κ���һ�����������������������в�������
				if (method.isAnnotationPresent(OnlyCare.class)) {
					OnlyCare onlyCare = method.getAnnotation(OnlyCare.class);
					if (command.parameters.length == 1 &&
							onlyCare.value().equals(command.parameters[0])) {
						method.invoke(invoker);
					}
				}
				else method.invoke(invoker);
			}
			//��Σ�����һ�Σ���
			else if(paramCount == command.parameters.length){
				//��String�����ֻ����������͵�ת�������մ����������б��˳��һһת��
				if (!checkIfOnlyCareCanPass(method, command)) return false;
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
	 * �ж����method�Ƿ����OnlyCareע�⣬����������ж��Ƿ����OnlyCare��Ҫ��
	 * @param command
	 * @param method
	 * @return ����Ҫ��ͨ���˿��Ա�������ã�����true
	 */
	private static boolean checkIfOnlyCareCanPass(Method method, Command command) {
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			if (parameter.isAnnotationPresent(OnlyCare.class)) {
				OnlyCare onlyCare = parameter.getAnnotation(OnlyCare.class);
				String careWhat = onlyCare.value();
				if (careWhat.equals("")) {
					careWhat = parameter.getName();
					System.out.println(careWhat);
				}
				if (!careWhat.equals(command.parameters[i])) {
					return false;
				}
			}
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
	 * @return ��ʼ�����������
	 */
	@Override
	public List<Command> getCommands() {
		return commands;
	}
	
	/**
	 * ���һ������,�����
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
	
	@Override
	public void removeCommand(Command command) {
		if(command == null) return;
		char key = command.commandName.charAt(0);
		int[] sl = commandsDirectory.get(key);
		int start = sl[0] , end = sl[1] + start;
		for(int i=start;i<end;i++) {
			if(command.equals(commands.get(i))) {
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
			builder.append(command.commandName.length() > 7 ? "\t" : "\t\t");
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
			CmdUtils.appendCommandAttribute(command, builder);
			Method method = methodsMap.get(command.commandName+
					(command.paramCount == -1 ? 
							(command.parameters == null ? 0 : command.parameters.length)
							: command.paramCount));
			if(method == null) method = methodsMap.get(command.commandName + "$");
			//��������ҵ���
			if(method != null) {
				builder .append("handlingMethod-->")
						.append(CmdUtils.getSimpleMethodSignature(method.toGenericString())).append('\n');
				builder .append("analyzer_id-->").append(getId()).append('\n');
			}
			//�п������command��Ӧ�˺ö��handlingMethod��һ��һ���Ұ�
			else {
				Set<Entry<String,Method>> entrySet = methodsMap.entrySet();
				for (Entry<String, Method> entry : entrySet) {
					String key = entry.getKey();
					if(CmdUtils.isBeginWith(commandName, key, null)) {
						builder .append("handlingMethod-->")
								.append(CmdUtils.getSimpleMethodSignature(entry.getValue().toGenericString()))
								.append('\n');
						builder .append("analyzer_id-->").append(getId()).append('\n');
					}
				}
			}
		}
		return builder.append('\n').toString();
	}

	@Override
	public int getHandlingMethodSize() {
		return methodsMap.size();
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
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