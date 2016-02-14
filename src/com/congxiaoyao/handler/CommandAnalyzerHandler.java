package com.congxiaoyao.handler;

import com.congxiaoyao.cmd.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * �ṩ��CommandAnalyzerManager��ά����CommandAnalyzer����Ϣ�Ķ�ȡ����
 * ͨ��������ⲿ�õ���CommandAnalyzer�е�methodsMap��invoker��commandDirectory�������Ϣ��չʾ����
 * ���ڵ��Կ���ڳ���ʮ���а���
 *
 * ֧�ֵ�������
 * 'ca_selc_id'     '1'     'ͨ��idѡ��һ��CommandAnalyzer�Ӷ�����ִ��ca_show'
 * 'ca_selc_name'   '1'     'ͨ������(�ɼ�д)ѡ��һ��CommandAnalyzer�Ӷ�����ִ��ca_show'
 * 'ca_show'        '1'     '������ѡ methodsMap��invoker��directory��commands'
 * 'ca_reload'              '���¼���ѡ�е�CommandAnalyzer'
 * 'ca_ids'                 '���е�CommandAnalyzer��id'
 * 'ca_names'               '���е�CommandAnalyzer��invoker��ClassName'
 * 'ca_infos'               'ids+names'
 *
 * Created by congxiaoyao on 2016/2/12.
 * @version 1.4
 */
public class CommandAnalyzerHandler extends BaseHandler {

    private Set<CommandAnalyzer> set;
    private CommandAnalyzer nowAnalyzer;

    public CommandAnalyzerHandler() {
        set = CommandAnalyzerManager.getInstance().getAnalyzers();
    }

    /**
     * @return ��ȡCommandAnalyzer��set�е�����CommandAnalyzer��id
     */
    public static int[] getCommandAnalyzerIds() {
        Set<CommandAnalyzer> set = CommandAnalyzerManager.getInstance().getAnalyzers();
        int[] ids = new int[set.size()];
        Iterator<CommandAnalyzer> iterator = null;
        for (int i = 0; i < ids.length; i++) {
            ids[i] = iterator.next().getId();
        }
        return ids;
    }

    /**
     * @return ��ȡCommandAnalyzer��set�е�����CommandAnalyzer��name
     */
    public static String[] getCommandAnalyzerNames() {
        Set<CommandAnalyzer> set = CommandAnalyzerManager.getInstance().getAnalyzers();
        int index = 0;
        String[] names = new String[set.size()];
        for (CommandAnalyzer analyzer : set) {
            names[index++] = getInvoker(analyzer).getClass().getName();
        }
        return names;
    }

    /**
     * @param analyzer
     * @return CommandAnalyzer�е�methodsMap����
     */
    public static Map<String, Method> getMethodsMap(CommandAnalyzer analyzer) {
        Map<String, Method> methodsMap = null;
        try {
            Field field = CommandAnalyzer.class.getDeclaredField("methodsMap");
            field.setAccessible(true);
            methodsMap = (Map<String, Method>) field.get(analyzer);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return methodsMap;
    }

    /**
     * @param analyzer
     * @return CommandAnalyzer�е�commandsDirectory����
     */
    public static Map<Character, int[]> getCommandsDirectory(CommandAnalyzer analyzer) {
        Map<Character, int[]> directory = null;
        try {
            Field field = CommandAnalyzer.class.getDeclaredField("commandsDirectory");
            field.setAccessible(true);
            directory = (Map<Character, int[]>) field.get(analyzer);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return directory;
    }

    /**
     * @param analyzer
     * @return ͨ�������ȡCommandAnalyzer�ڵ�invoker����
     */
    public static Object getInvoker(CommandAnalyzer analyzer){
        Object invoker = null;
        try {
            Field field = CommandAnalyzer.class.getDeclaredField("invoker");
            field.setAccessible(true);
            invoker = field.get(analyzer);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return invoker;
    }

    /**
     * @param analyzer
     * @return ����analyzer���ܴ��������ļ���
     */
    public static List<Command> getCommandsHandleBy(CommandAnalyzer analyzer) {
        List<Command> result = new ArrayList<>();
        List<Command> commands = analyzer.getCommands();
        for (Command command : commands) {
            String info = analyzer.getCommandInfo(command.commandName);
            if(info.contains("handlingMethod")) {
                result.add(command);
            }
        }
        return result;
    }

    /**
     * @param className ���� ȫ����д�Կ�
     * @return ͨ��invoker��������Ѱ�Ҷ�Ӧ��CommandAnalyzer
     */
    public static CommandAnalyzer getCommandAnalyzerByName(String className) {
        Set<CommandAnalyzer> set = CommandAnalyzerManager.getInstance().getAnalyzers();
        for (CommandAnalyzer analyzer : set) {
            Object invoker = getInvoker(analyzer);
            if (invoker.getClass().getName().contains(className)) {
                return analyzer;
            }
        }
        return null;
    }

    /**
     * @param id CommandAnalyzer��id
     * @return ͨ��CommandAnalyzer��id��Ѱ�Ҷ�Ӧ��CommandAnalyzer
     */
    public static CommandAnalyzer getCommandAnalyzerById(int id) {
        Set<CommandAnalyzer> set = CommandAnalyzerManager.getInstance().getAnalyzers();
        for (CommandAnalyzer analyzer : set) {
            if (analyzer.getId() == id) {
                return analyzer;
            }
        }
        return null;
    }

    @CommandName("ca_show")
    public void showMethodsMap(@OnlyCare("methodsMap")String arg) {
        if (!checkNowAnalyzer()) return;
        Map<String, Method> methodsMap = getMethodsMap(nowAnalyzer);
        methodsMap.forEach((String s, Method method) ->{
            String[] methodInfo= method.toString().split(" ");
            String methodName = methodInfo[methodInfo.length - 1];
            int end = methodName.indexOf('(');
            methodName = methodName.substring(0, end);
            String[] split = methodName.split("\\.");
            String methodNameNew = split[split.length - 1];
            methodNameNew = method.toString().replace(methodName,methodNameNew);
            System.out.println("<" + s + " , " + methodNameNew + ">");
        });
    }


    @CommandName("ca_show")
    public void showCommandsDirectory(@OnlyCare("directory")String arg) {
        if (!checkNowAnalyzer()) return;
        try {
            Field field = CommandAnalyzer.class.getDeclaredField("commandsDirectory");
            field.setAccessible(true);
            Map<Character, int[]> directory = (Map<Character, int[]>) field.get(nowAnalyzer);
            directory.forEach((character, ints) -> {
                System.out.println("<" + character + " , " + "start=" + ints[0] + "\tlen=" + ints[1] + ">");
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    @CommandName("ca_show")
    public void showInvoker(@OnlyCare("invoker")String invoker) {
        if (!checkNowAnalyzer()) return;
        System.out.println(getInvoker(nowAnalyzer).toString());
    }


    @CommandName("ca")
    public void showCommands(@OnlyCare("commands")String arg) {
        if (!checkNowAnalyzer()) return;
        List<Command> commands = nowAnalyzer.getCommands();
        for (Command command : commands) {
            String info = nowAnalyzer.getCommandInfo(command.commandName);
            if(info.contains("handlingMethod")) {
                System.out.print(nowAnalyzer.getCommandInfo(command.commandName));
            }
        }
    }

    @CommandName("ca_selc_id")
    public void selectCommandAnalyzerById(int id) {
        for (CommandAnalyzer analyzer : set) {
            if (analyzer.getId() == id) {
                nowAnalyzer = analyzer;
                System.out.println("successed!");
                return;
            }
        }
        System.out.println("error");
    }

    @CommandName("ca_selc_name")
    public void selectCommandAnalyzerByName(String className) {
        for (CommandAnalyzer analyzer : set) {
            Object invoker = getInvoker(analyzer);
            if (invoker.getClass().getName().contains(className)) {
                nowAnalyzer = analyzer;
                System.out.println("select " + invoker.getClass().getName()+" successed!");
                return;
            }
        }
        System.out.println("failed");
    }

    @CommandName("ca_ids")
    public void showCommandAnalyzerIds() {
        int[] ids = getCommandAnalyzerIds();
        for (int id : ids) {
            System.out.println(id + "");
        }
    }

    @CommandName("ca_names")
    public void showCommandAnalyzerNames() {
        String[] names = getCommandAnalyzerNames();
        for (String name : names) {
            System.out.println(name);
        }
    }

    @CommandName("ca_infos")
    public void showCommandAnalyzerInfos() {
        System.out.println("ids\t\tsize\t\tnames");
        for (CommandAnalyzer analyzer : set) {
            System.out.print(analyzer.getId()+"\t\t");
            System.out.print(analyzer.getHandlingMethodSize()+"\t\t");
            System.out.println(getInvoker(analyzer).getClass().getSimpleName());
        }
    }

    @CommandName("ca_reload")
    public void reload() {
        if (nowAnalyzer == null) {
            System.out.println("���ȵ���ca_selc_id��ca_selc_id����ͨ��ѡ��һ��CommandAnalyzer��");
            return;
        }
        try {
            Object invoker = getInvoker(nowAnalyzer);
            CommandManagementHandler handler = new CommandManagementHandler();
            handler.removeAnalyzer(nowAnalyzer.getId());
            handler.addHandlingMethod(invoker.getClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return ͨ����鷵��true��û�з���false
     */
    private boolean checkNowAnalyzer() {
        if (nowAnalyzer == null) {
            System.out.println("���ȵ���ca_selc_id��ca_selc_name����ͨ��ѡ��һ��CommandAnalyzer��");
            return false;
        }
        return true;
    }

    /**
     * �����ͨ���ļ���������Ļ���Ҳ���Ե������������̬�������Щ����
     * ���������ӵ���������Ĵ������Ѿ���CommandAnalyzerHandler�еĴ�����ʵ����
     */
    @Override
    public BaseHandler registerCommands() {
        Analysable analysable = getAnalysable();
        analysable.addCommand(new Command("ca_selc_id",
                1, "ͨ��idѡ��һ��CommandAnalyzer�Ӷ�����ִ��ca_show"));
        analysable.addCommand(new Command("ca_selc_name",
                1, "ͨ������(�ɼ�д)ѡ��һ��CommandAnalyzer�Ӷ�����ִ��ca_show"));
        //���ѡ����CommandAnalyzer��methodsMap/invoker/directory/commands����Ϣ
        analysable.addCommand(new Command("ca", 1, "������ѡ methodsMap��invoker��directory��commands"));
        analysable.addCommand(new Command("ca_reload" , "���¼���ѡ�е�CommandAnalyzer"));
        analysable.addCommand(new Command("ca_ids" , "���е�CommandAnalyzer��id"));
        analysable.addCommand(new Command("ca_names" , "���е�CommandAnalyzer��invoker��ClassName"));
        analysable.addCommand(new Command("ca_infos", "ids+names"));
        return this;
    }
}