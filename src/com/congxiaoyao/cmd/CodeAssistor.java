package com.congxiaoyao.cmd;

import java.util.Arrays;
import java.util.List;

/**
 * ������ʾ��
 * ���п��޵�һ���� ����Ϊ���׿�ܵ�һ���֣���Ϊʹ���߶����ṩ������ʾ�Ĺ���
 * ���Ǽ��豾�������CommandAnalyzerһͬʹ�ã���ֻ��Ҫ��CommandAnalyzer��ά����Command��List���빹�캯�����ɹ������
 * ֮�����{@code CodeAssistor#find(String)}���������û��ĵ�ǰ���뼴�ɵõ�������ʾ�Ľ��
 * �������{@code SelectableArray<WeightedString>}����ʽ���أ�SelectableArray֧��foreach����
 * WeightedString�������ֶζ���public����Ȩ�޵ģ���string�ֶδ�������ν��CommandName
 * Created by congxiaoyao on 2015/12/20.
 * @version 1.0
 */
public class CodeAssistor {

    private String lastContent = null;
    private WeightedString[] codes;
    private SelectableArray<WeightedString> seletableArray;

    public CodeAssistor(String[] codes) {
        int len = codes.length;
        this.codes = new WeightedString[len];
        for (int i = 0; i < len; i++) {
            this.codes[i] = new WeightedString(codes[i], 0);
        }
        seletableArray = new SelectableArray<>(this.codes);
    }
    
    public CodeAssistor(List<Command> commands) {
    	this(commandsToCodes(commands));
    }
    

    /**
     * ���û����봫�뼴�ɵõ����ϲ��ұ�׼��ƥ��ȴӸߵ��͵Ĳ��ҽ��
     * @param content 
     * @return ���SelectableArray��ͷע�ͼ�WeightedString��ͷע��
     */
    public SelectableArray<WeightedString> find(String content) {
        //û��ʵ������
        if(content.length() == 0) return seletableArray;
        //ֻ������һ�εĻ����϶������һЩ���ݣ���һ�ε��������������һ�������а���
        if (lastContent != null && content.length() > lastContent.length()
                && content.indexOf(lastContent) == 0) {
            seletableArray.refactor();
        }else {
            seletableArray.reset();
        }
        lastContent = content;
        //��������������code
        char[] chars = content.toCharArray();
        SelectableArray<WeightedString>.ElementPool<WeightedString> pool = seletableArray.getElementPool();
        for (int i = 0, len = pool.size(); i < len; i++) {
            WeightedString weightedString = pool.get(i);
            if (isMatch(weightedString.string, chars)) {
                seletableArray.select(i);
            }
        }
        //������ض�����
        char[] contentChars = content.toCharArray();
        for (WeightedString  ws: seletableArray) {
            ws.weight = 0;
            for (int i = 0; i < contentChars.length; i++) {
                ws.weight += getDistance(ws.chars, contentChars[i], i);
            }
        }
        sort(seletableArray);
        return seletableArray;
    }

    /**
     * ��ȡchar��ָ��λ�õ���̾����㷨
     *
     * @param src    ���������ַ���
     * @param target Ŀ���ַ�
     * @param index  ָ��λ��
     * @return src������target�ַ���indexλ������ľ���
     */
    public static int getDistance(char[] src, char target, int index) {
        int distance = 0;
        int min = Integer.MAX_VALUE;
        for (int i = 0, len = src.length; i < len; i++) {
            if (target == src[i]) {
                distance = Math.abs(i - index);
                if (distance < min) min = distance;
            }
        }
        return min;
    }

    /**
     * �ж�target�����е��ַ��Ƿ�����src���ҵ�������������src�г��ֵ�˳�������target�е�˳��
     * @param src
     * @param target
     * @return ������������true
     */
    public static boolean isMatch(String src, char[] target) {
        int len = target.length, fromIndex = -1;
        for (int i = 0; i < len; i++) {
            if ((fromIndex = src.indexOf(target[i], fromIndex+1)) == -1) return false;
        }
        return true;
    }

    /**
     * ϣ�����򣬰�WeightedString��weight�ֶδ�С��������
     * @param array SelectableArray
     */
    public static void sort(SelectableArray<WeightedString> array){
        int d=array.size();
        while((d/=2)>0){
            for(int i=d;i<array.size();i++){
                WeightedString weightedString = array.get(i);
                int temp=weightedString.weight,position=i;
                for(int j=i-d;j>=0;j-=d){
                	if(array.get(j).weight>=temp) break;
                	array.set(j+d,array.get(j));
                	position-=d;
                }
                array.set(position, weightedString);
            }
        }
    }
    
    /**
     * ��commands�ļ����е�CommandNameת��ΪString�����飬ͬʱ�ظ���CommandName�������ز����������޳�
     * @param commands
     * @return ������CodeReminder�����String����
     */
    public static String[] commandsToCodes(List<Command> commands) {
    	String[] codes = new String[commands.size()];
		int newLength = 0;
		for(int i=0;i<codes.length;i++) {
			String code = commands.get(i).commandName;
			boolean add = true;
			for(int j=0;j<i;j++)
				if(code.equals(codes[j])) add = false;
			if(add) codes[newLength++] = commands.get(i).commandName;
		}
		return Arrays.copyOf(codes, newLength);
    }

    /**
     * ��Ȩֵ��String����������һ��code�������û������ƥ��Ƚ���¼�������Ķ����weight�ֶΣ�code��string�ֶ�
     * @author congxiaoyao
     */
    public class WeightedString {
        public String string;
        public int weight;
        public char[] chars;

        public WeightedString(String string, int weight) {
            this.string = string;
            this.weight = weight;
            chars = string.toCharArray();
        }

        @Override
        public String toString() {
            return "WeightedString{" +
                    "string='" + string + '\'' +
                    ", weight=" + weight +
                    '}';
        }
    }
}