package com.congxiaoyao.cmd;

/**
 * ���������㷨�ķ�װ��(������?) ����ֻ���һ�������������
 * �������Լ�ʵ����һ��Ԫ������������ArrayList��Vector)����û��ʵ�ֱ�׼��List�ӿ�ʱ
 * ϵͳ�޷�Ϊ�����ṩ�����㷨,��������þ������������������ṩ���������㷨
 * ���з���{@code Container}Ϊ���������ͣ�����{@code Element}Ϊ�����ڰ�����Ԫ�ص�����
 * ����ʵ������abstract�����Ա�������ʱ���Խ���Ԫ��λ�ü�ȷ��������С
 * ����������Ҫ�����Ч��Ԫ�ؽ����������Ը�д{@code QuickSort#swap(int, int, Object)}����
 *
 * Created by congxiaoyao on 2016/2/8.
 * @version 1.0
 */
public abstract class QuickSort<Container , Element> {

    /**
     * ������ں�����ͨ������ʵ������
     * @param container �������������Ϊ����������
     */
    public void sort(Container container) {
        if (container != null) {
            quickSort(0, size(container) - 1, container);
        }
    }

    /**
     * �ݹ����ţ���container�е�Ԫ�ؽ�������
     * @param start ��ʼ����ķ�Χ
     * @param end   ��������ķ�Χ
     * @param container �������������Ϊ����������
     */
    private void quickSort(int start, int end, Container container) {
        if (start >= end) return;

        int startOrg = start;
        int endOrg = end;
        Element refer = get(container, start);

        while (start < end) {

            while (compare(get(container, end), refer) >= 0 && start < end) {
                end--;
            }
            while (compare(get(container, start), refer) <= 0 && start < end) {
                start++;
            }

            swap(start, end, container);
        }
        swap(startOrg, end, container);

        quickSort(startOrg, end - 1, container);
        quickSort(end + 1, endOrg, container);
    }

    /**
     * ����container�е�λ��index0��index1������Ԫ��
     * @param index0
     * @param index1
     * @param container
     */
    public void swap(int index0, int index1, Container container){
        Element temp = get(container, index0);
        set(container, get(container, index1), index0);
        set(container, temp, index1);
    }

    /**
     * ���ϣ��QuickSort��С������������ element0-element1�������� element1-element0
     * @param element0
     * @param element1
     * @return element0 ���� element1 Ҫ�������� element0 С�� element1 Ҫ���ظ��� ��ȷ���0
     */
    public abstract int compare(Element element0, Element element1);

    /**
     * @param container
     * @param index
     * @return container��λ��index��Ԫ��
     */
    public abstract Element get(Container container, int index);

    /**
     * ��container��indexλ�ø�ֵ
     * @param container
     * @param element
     * @param index
     */
    public abstract void set(Container container, Element element, int index);

    /**
     * @param container
     * @return container�Ĵ�С��Ҳ�������Ϊ��Ҫ����Ĳ��ֵĳ���
     */
    public abstract int size(Container container);
}