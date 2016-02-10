package com.congxiaoyao.cmd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

/**
 * 在这个类里解释一下这一整套所谓的框架的工作原理及使用方法
 * 我希望这套框架能够为实现一个命令提示符系统提供一些必要的支持，使得框架的使用者可以专注于处理事务
 * 从而不必在命令定义、分析、查找、执行等细节上花费太多时间
 * 1、在cmd/values.txt内定义一个具体的命令，定义的格式见文件内注释
 * 2、在任意类中定义命令的处理方法，并通过注解与命令绑定
 * 3、构造出CommandAnalyzer的实例，调用process方法即可处理一条用户输入的内容，具体使用见方法注释
 * 下面是小例子
 * 假设我们有命令定义如下		'help' '查看帮助信息'
 * 通过注解来绑定处理函数与命令，假设我们的处理方法写在了类Case中
 * <hr><pre>
 * public class Case{
 * 
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
 * 关于重载命令
 * 命令支持多参重载，回调处理函数的时候，会根据处理函数参数的个数及类型选择合适的处理函数进行回调
 * 当命令被识别出来后，会先去寻找参数个数与命令所携带的参数个数相等的处理函数回调之
 * 如果找不到，会再次寻找参数为Command或String...类型的处理函数，因为这两种类型可以拦截任意参数个数
 * 如果依然找不到则认为处理失败
 * 如果对同一个非重载的命令定义了多个处理函数，或对于一个重载命令定义了多个处理相同参数个数的处理函数
 * 会导致其多余的处理函数失效，请尽量避免重复
 * 
 * 关于可变参数命令
 * 可变参数命令是指在参数定义的时候并不关心准确的参数个数（paramCount要标为-1），用户输几个我就处理几个，这样的话
 * 想要拦截可变参数命令，处理函数的参数可以是Command类型、多参且都是String类型（要正好与这次用户输入的参数个数匹配）、
 * String...类型。如果以Command作为参数类型，其对象中的paramCount为-1，请以parameters（String数组）的长度为准
 * 
 * 关于命令的动态申请，见{@code CommandAnalyzer#addCommand(Command)}方法
 * 关于处理函数的多类分布问题，见{@code CommandAnalyzerManager}类头注释
 * 
 * 关于自动参数类型转换
 * 如果命令中的参数是基本数据类型的一种 如设置窗口尺寸时，命令的参数实际是int型的
 * 那么只要将处理函数的参数类型定义为int或Integer类型，CommandAnalyzer会自动将String类型的参数转为int/Integer型
 * 
 * 关于敏感参数拦截
 * 对于一参命令,其处理函数可以通过OnlyCare注解过滤掉其他的参数，只在用户输入她想要的参数时才会回调这个函数例如
 * 	
 * <hr><pre>
 * <code>@CommandName("screen")
 * <code>@OnlyCare("max")
 * public void maxSizeWindow(String arg) {
 * 		//只有用户输入 ‘screen max’ 的时候此函数才会被回调
 * 		//参数arg也可以不写
 * }
 * </pre><hr>
 * 
 * @version 1.1
 * @date 2016.1.19
 * @author congxiaoyao
 */

public class CommandAnalyzer implements Analysable
{
	private Object invoker;
	
	private List<Command> commands = new ArrayList<>();
	//可以通过这个map按照首字母在commands中查找，提高效率，这个int[]记录了startIndex跟length两个值
	Map<Character, int[]> commandsDirectory = new HashMap<>();	
	
	Map<String, Method> methodsMap = new HashMap<>();

	/**
	 * @param invoker 要执行的方法所在的类的实例
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
	 * 将一条用户输入丢入这个方法，他会尝试解析这个字符串并且解析成功后会尝试执行这个命令
	 * 也就是直接去传入的invoker那个类中去找到对应的方法调用
	 * @param content 待分析的命令
	 * @return 如果命令最终成功执行，则返回true，其他情况返回false
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
	 * 从cmd/values.txt文件中读取配置的脚本信息并创建相应的command对象加入命令集合并建立起目录方便查询
	 */
	private void initCommandList() {
		List<String> params = new ArrayList<String>(5);
		new TextReader("cmd/values.txt") {
			@Override
			public void onReadLine(String line) {
				//去注释及空行
				if(line.length() == 0 || line.charAt(0) == '%') return;
				
				//将单引号引起来的内容一个一个的加入params中，对于每一行读出来的内容，第一步就是清掉上一次遗留在params中的内容
				params.clear();
				int start = line.indexOf('\'');
				while(start != -1) {
					String param = line.substring(start + 1 , start = line.indexOf('\'',start + 1));
					start = line.indexOf('\'',start + 1);
					params.add(param);
				}
				//循环读取结束，开始根据参数个数创建对应的Command对象
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
		//更新目录以方便查找
		if(commands.size() != 0) {
			updateCommandsDirectory();
		}
	}
	
	/**
	 * 建立起commandName跟处理它的函数（处理函数）的HashMap,具体做法如下
	 * 遍历invoker中所有的method，对于一个符合条件的method，向map中添加一个entry，这个method作为entry的value
	 * 关于对应的key，有两部分组成（commandName + X）,commandName由这个method的注释或函数名获得，X是指这个
	 * method的参数个数，如果遇到String...或Command类型的参数，我们认为X为$
	 * 对于带有OnlyCare注解的处理函数，key的生成规则变为commandName+OnlyCare
	 * 对于标准命名风格的命令及处理函数，不需要给CommandName注解添加参数，即可通过函数名将其所代表的命令解析出来
	 * 标准命名风格要求命令只由小写字母或数字组成，字母开头，处理函数在命令名前面加handle即可（handle后面的命令名可随意转换大小写）
	 * 如 命令名 help 处理函数名 handleHelp
	 */
	private void initMethodsMap() {
		Method[] methods = invoker.getClass().getDeclaredMethods();
		for (Method method : methods) {
			//只处理public方法
			int modifiers = method.getModifiers();
			if (modifiers != 1 && modifiers != 9) continue;
			//过滤掉没有注解的方法
			if(!method.isAnnotationPresent(CommandName.class)) continue;
			//获取上面的注解
			CommandName commandName = method.getAnnotation(CommandName.class);
			String value = commandName.value();
			//如果注解中没有参数，尝试通过函数名解析命令头
			if(value.length() == 0 && isBeginWith("handle", method.getName(), null)) {
				value = method.getName().replaceFirst("handle", "").toLowerCase();
			}
			String key = null;
			//由于重载命令的存在，不同的函数可能会处理不同的重载命令
			int paramCount = method.getParameterCount();
			//带OnlyCare注解的需要按特殊规则生成key
			if(method.isAnnotationPresent(OnlyCare.class)) {
				if(paramCount > 1) continue;
				key = value + method.getAnnotation(OnlyCare.class).value();
			}
			//如果处理函数中只有一个参数且参数为Command类型，在构造map时以命令头加$作为map的key
			else if(paramCount == 1) {
				Class<?> type = method.getParameterTypes()[0];
				if(type == Command.class || type == String[].class)
					key = value + "$";
				else key = value + paramCount;
			}
			//否则以命令头加处理函数的参数个数为map的key
			else key = value + paramCount;
			methodsMap.put(key , method);
		}
	}

	/**
	 * 分析一条字符串是否是一条给定的命令,要求commandName相同且能解析出相同个数的参数
	 * @param content 控制台提交的字符串
	 * @return 如果能够成功匹配，返回一个command对象，否则返回null
	 */
	public Command analyze(String content)
	{
		if(content.length() == 0) return null;
		int[] info = commandsDirectory.get(content.charAt(0));
		if(info == null) return null;
		for(int i = info[0],len = info[1]+i;i<len;i++) {
			Command command = commands.get(i);
			//先看是不是给定字符串是不是当前这个命令类型
			if (!isBeginWith(command.commandName, content, command.delimiter)) continue;
			//然后在去掉命令开头去分析参数
			String contentNew = content.replaceFirst(command.commandName, "");
			//初始化参数个数
			int paramCount = 0;
			String[] parameters = null;
			//如果有参数，求给定字符串中包含的参数个数
			if (contentNew.length() > 1) {
				contentNew = contentNew.replaceFirst(command.delimiter, "");
				parameters = contentNew.split(command.delimiter);
				paramCount = parameters.length;
			}
			//如果求出的个数与这条命令所规定的相同
			if (paramCount == command.paramCount || command.paramCount == -1) {
				command.parameters = parameters;
				return command;
			}
		}
		return null;
	}
	
	/**
	 * 去invoker对象中去寻这个command对应的处理它的方法（依据CommandName注解、处理函数的参数类型及个数）
	 * @param command
	 * @return 如果传入的command处理成功返回true否则返回false
	 */
	public boolean handleCommand(Command command) {
		Method method = null;
		//对于一参的命令，尝试敏感参数拦截
		if(command.paramCount == 1) {
			method = methodsMap.get(command.commandName+command.parameters[0]);
		}
		//没有的话找有没有固定参数的处理这条命令的方法
		if(method == null) {
			method = methodsMap.get(command.commandName+
					(command.paramCount == -1 ? 
							(command.parameters == null ? 0 : command.parameters.length)
									: command.paramCount));
		}
		//再没有的话，再找参数为Command的处理这条命令的方法
		if(method == null) {
			method = methodsMap.get(command.commandName + "$");
			if(method == null) return false;
		}
		try {
			int paramCount = method.getParameterCount();
			//匹配到的方法只有一参且为Command类型或String...型
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
			//无参
			else if (paramCount == 0) {
				method.invoke(invoker);
			}
			//多参（包含一参）
			else if(paramCount == command.parameters.length){
				//做String到各种基本参数类型的转换，按照处理函数参数列表的顺序一一转换
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
	 * 将一条command插入到commands集合中，但并不更新目录map
	 * 一定不要忘记更新目录，否则会出问题
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
			//找位置插队
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
	 * 更新命令目录，使得目录map能够正常工作
	 */
	private void updateCommandsDirectory() {
		if(commands.size() == 1) return;
		//根据排好的顺序建立目录
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
	 * 将一个String类型的参数arg解析为type类型并以Object类型的形式返回
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
	 * 判断命令是否以给定的单词开始
	 * @param beginWord 给定的开始单词
	 * @param command 命令语句
	 * @param delimiter 分隔符，如果不为null则判断command中从0到分隔符之前的内容是不是beginWord，否则直接判断
     * @return 以给定的单词开始返回true
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
	
	//需要转义的字符
	private static String[] ec = {".","$","^","(",")","[","|","{","?","+","*",};
	/**
	 * 分析这个String是否为ec数组中所包含的待转义的字符，如果是就给他转义喽
	 * @param ch 待检查String
	 * @return 转义后的ch
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
	 * @return 初始化过的命令集合
	 */
	@Override
	public List<Command> getCommands() {
		return commands;
	}
	
	/**
	 * 添加一条命令,会查重
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
	 * @return 所有的命令的描述拼接成一个string的形式返回
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
			//如果最终找到了
			if(method != null) {
				builder.append("handlingMethod-->").append(method.toGenericString()).append('\n');
			}
			//有可能这个command对应了好多个handlingMethod，一点一点找吧
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