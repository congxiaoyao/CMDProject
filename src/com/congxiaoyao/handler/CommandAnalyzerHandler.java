package com.congxiaoyao.handler;

import com.congxiaoyao.cmd.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * @version 1.0
 */
public class CommandAnalyzerHandler extends CommandHandler{

    private Set<CommandAnalyzer> set;
    private CommandAnalyzer nowAnalyzer;

    public CommandAnalyzerHandler() {
        set = CommandAnalyzerManager.getInstance().getAnalyzers();
    }
    public CommandAnalyzerHandler(CommandAnalyzer analyzer) {
        set = new HashSet<>(1);
        set.add(analyzer);
    }

    @OnlyCare("methodsMap")
    @CommandName("ca_show")
    public void showMethodsMap() {
        if (!checkNowAnalyzer()) return;
        try {
            Field field = CommandAnalyzer.class.getDeclaredField("methodsMap");
            field.setAccessible(true);
            Map<String, Method> methodsMap = (Map<String, Method>) field.get(nowAnalyzer);
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
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    @OnlyCare("directory")
    @CommandName("ca_show")
    public void showCommandsDirectory() {
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

    @OnlyCare("invoker")
    @CommandName("ca_show")
    public void showInvoker() {
        if (!checkNowAnalyzer()) return;
        try {
            System.out.println(getInvoker(nowAnalyzer).toString());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @OnlyCare("commands")
    @CommandName("ca_show")
    public void showCommands() {
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
            try {
                Object invoker = getInvoker(analyzer);
                if (invoker.getClass().getName().contains(className)) {
                    nowAnalyzer = analyzer;
                    System.out.println("select " + invoker.getClass().getName()+" successed!");
                    return;
                }
            } catch (NoSuchFieldException e) {
                System.out.println("error NoSuchFieldException");
            } catch (IllegalAccessException e) {
                System.out.println("error IllegalAccessException");
            }
        }
        System.out.println("failed");
    }

    @CommandName("ca_ids")
    public void getCommandAnalyzerIds() {
        for (CommandAnalyzer analyzer : set) {
            System.out.println(analyzer.getId()+"");
        }
    }

    @CommandName("ca_names")
    public void getCommandAnalyzerNames() {
        for (CommandAnalyzer analyzer : set) {
            try {
                System.out.println(getInvoker(analyzer).getClass().getName());
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @CommandName("ca_infos")
    public void getCommandAnalyzerInfos() {
        System.out.println("ids\t\tnames");
        for (CommandAnalyzer analyzer : set) {
            try {
                System.out.print(analyzer.getId()+"\t\t");
                System.out.println(getInvoker(analyzer).getClass().getSimpleName());
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
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
     * ͨ�������ȡCommandAnalyzer�ڵ�invoker����
     * @param analyzer
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static Object getInvoker(CommandAnalyzer analyzer) throws NoSuchFieldException, IllegalAccessException {
        Field field = CommandAnalyzer.class.getDeclaredField("invoker");
        field.setAccessible(true);
        return field.get(analyzer);
    }

    /**
     * �����ͨ���ļ���������Ļ���Ҳ���Ե������������̬�������Щ����
     * ���������ӵ���������Ĵ������Ѿ���CommandAnalyzerHandler�еĴ�����ʵ����
     */
    @Override
    public CommandHandler registerCommands() {
        Analysable analysable = getAnalysable();
        analysable.addCommand(new Command("ca_selc_id",
                1, "ͨ��idѡ��һ��CommandAnalyzer�Ӷ�����ִ��ca_show"));
        analysable.addCommand(new Command("ca_selc_name",
                1, "ͨ������(�ɼ�д)ѡ��һ��CommandAnalyzer�Ӷ�����ִ��ca_show"));
        //���ѡ����CommandAnalyzer��methodsMap/invoker/directory/commands����Ϣ
        analysable.addCommand(new Command("ca_show", 1, "������ѡ methodsMap��invoker��directory��commands"));
        analysable.addCommand(new Command("ca_reload" , "���¼���ѡ�е�CommandAnalyzer"));
        analysable.addCommand(new Command("ca_ids" , "���е�CommandAnalyzer��id"));
        analysable.addCommand(new Command("ca_names" , "���е�CommandAnalyzer��invoker��ClassName"));
        analysable.addCommand(new Command("ca_infos", "ids+names"));
        return this;
    }
}