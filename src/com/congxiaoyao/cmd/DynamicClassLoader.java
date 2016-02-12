package com.congxiaoyao.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * class�ļ����ȼ����࣬ʹ��cmd��ܶ�̬��Ӻʹ��������Ϊ����
 * ������ͨ���������������������ģ������������������ܹ�ͨ����ӵ�����������µ�����
 * ����{@code DynamicClassLoader}�����ṩ��handle_with������Ĵ�����ʵ����CommandWindowHandler��
 * handle_with������Ҫ����һ������ ��{@code CommandAnalyzerManager#handleWith(Object) }��ͬ����
 * ����Ĳ�����handlingObject���������������ʵ��
 * �ٸ����ӣ�������Ƕ�̬�����һ������sayhello���������½�����com.cmd.Handler�������Ĵ�����д���������
 * ��������ע��<code>@CommandName("sayhello")</code>
 * ��ʱֻҪ����������һ�£�����ͨ������ handle_with com.cmd.Handler ʵ����
 * ����<code>CommandAnalyzerManager.handleWith(new Handler());</code>��ͬ�Ĺ���
 * Ҳ���Ǹ�����sayhello��Ӧ�Ĵ����� �Ӷ���ɶ�̬�����
 *
 * @author congxiaoyao
 * @version 1.0
 * @date 2016.2.12
 */
public class DynamicClassLoader extends ClassLoader {

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    @SuppressWarnings("unchecked")
    public Class loadClass(String classPath, String className)
            throws ClassNotFoundException {
        try {
            className += ".class";
            String url = classPathParser(classPath) + classNameParser(className);
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();
            while (data != -1) {
                buffer.write(data);
                data = input.read();
            }
            input.close();
            byte[] classData = buffer.toByteArray();
            return defineClass(noSuffix(className), classData, 0,
                    classData.length);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String pathParser(String path) {
        return path.replaceAll("\\\\", "/");
    }

    private String classPathParser(String path) {
        String classPath = pathParser(path);
        if (!classPath.startsWith("file:")) {
            classPath = "file:" + classPath;
        }
        if (!classPath.endsWith("/")) {
            classPath = classPath + "/";
        }
        return classPath;
    }

    private String classNameParser(String className) {
        return className.substring(0, className.lastIndexOf(".")).replaceAll(
                "\\.", "/")
                + className.substring(className.lastIndexOf("."));
    }

    private String noSuffix(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }
}