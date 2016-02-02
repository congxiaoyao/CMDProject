package com.congxiaoyao.cmd;

import java.util.Iterator;

/**
 * ��ѡ�������
 * �߼�����Ԫ�سغͱ�ѡ������鹹�ɣ���ʵ����ά����һ��T���͵�����
 * ���캯���У�����һ��T���͵����飬�����SelectableArray�ĳ�ʼԪ�س�
 * SelectableArray�ĳ�ʼ����Ϊ0��ͨ��select������Ԫ�س���ѡ��Ԫ�أ����ṩget��set����������ѡ������Ԫ��
 * ����Ԫ�س�ElementPool����ElementPool��Ҳ�ṩ��get��set�����Ա����Ԫ�س��е�Ԫ��
 * ͨ��SelectableArray�ڵ�getElementPool�������ɻ�ȡ��ǰά����Ԫ�سض���
 * ��SelectableArray�Ѿ�ѡ����һЩԪ�أ�����ͨ��refactor�������Ѿ�ѡ���Ԫ����Ϊһ���µ�Ԫ�س��滻��ԭ����Ԫ�س� 
 * ͬʱ��ʱSelectableArray�ĳ��ȱ�Ϊ0�������ѡ��Ԫ�س������е�Ԫ�أ����Ե���selectAll����
 * ���۾������ٴ�refactor��������ͨ��reset�����ع������ѡ��״̬��Ԫ�س�����
 * ע�⣬������Ҫ�ظ�ѡ��Ԫ�أ���ѡ���Ԫ����������Ԫ�سص�Ԫ�ظ���ʱ���ᷢ������Խ�磬������ѡ��������辶
 * ������ʹ�����������ע��
 * @author congxiaoyao
 * @date 2015.12.22
 * @version 1.0
 * @param <T>
 */
public class SelectableArray<T> implements Iterable<T> {
	
	private T[] data;
	private ElementPool<T> pool;
	private int[] selected;				//���ڼ�¼��ѡ���Ԫ����data�е�λ��
	private int pointer;				//��һ��select����Ԫ��Ҫ����select�����е�λ��

	public SelectableArray(T[] elementPool) {
		this.data = elementPool;
		pool = new ElementPool<T>(elementPool.length);
		selected = new int[elementPool.length];
		pointer = 0;
	}
	
	/**
	 * ��Ԫ�س���ѡ������Ϊindex��Ԫ��
	 * @param index ��Ԫ�س��ع��������ع����Ԫ�س�����ɵ�Ԫ������Ϊ��׼��index
	 */
    public void select(int index) {
        if (index >= pool.size) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        selected[pointer++] = pool.elements[index];
    }
    
    /**
     * ѡ��ȫ����ѡ������˳���Ǳ�ѡ��Ԫ�ص�ԭʼ˳��
     * ���֮ǰѡ���һЩ�������ô˷�����֮ǰѡ���˳�򽫻ᱻ����
     */
    public void selectAll() {
        pointer = 0;
        for (int i = 0; i < pool.size; i++) {
            selected[pointer++] = pool.elements[i];
        }
    }
    
    /**
     * ����ѡ����������ȡ�±�Ϊindex��Ԫ��
     * @param index ��ѡ�������Ԫ����ɵ�������Ϊ��׼��index
     */
    public T get(int index) throws ArrayIndexOutOfBoundsException{
        if (index >= pointer) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return (T) data[selected[index]];
    }
    
    /**
     * ��ѡ����Ԫ����Ϊ�����������飬��λ��index��ֵelement,ͬʱelementPoolҲ������ܵ�Ӱ��
     * ע�⸳ֵ�����ǲ�����ģ�����ͨ��reset�����ָ���ʼ״̬
     * @param index ��ѡ�������Ԫ����ɵ�������Ϊ��׼��index
     * @param element T����Ԫ��
     */
    public void set(int index, T element) throws ArrayIndexOutOfBoundsException {
        if (index >= pointer) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        data[selected[index]] = element;
    }
    
    /**
     * @return ѡ����������ĳ���
     */
    public int size() {
        return pointer;
    }
    
    /**
     * �ع�����
     * ���ѱ�ѡ���������Ϊ�µ�Ԫ�س��滻��֮ǰ�ģ�ͬʱ��������Ѿ�ѡ���Ԫ��
     */
    public void refactor() {
        System.arraycopy(selected, 0, pool.elements, 0, pointer);
        pool.size = pointer;
        pointer = 0;
    }
    
    /**
     * ���ú������������е��ع��������ص����캯�����ʱ���״̬
     * ע�⣬set�����Ĳ����ǲ�����ģ�ͨ��set�����޸ĵ�ֵ�����ܱ�reset����
     */
    public void reset() {
    	pool.reset();
        pointer = 0;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<T> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < pointer;
        }

        @Override
        public T next() {
            return get(index++);
        }
    }
	
    /**
     * @return ����ά����Ԫ�سض���
     */
	public ElementPool<T> getElementPool() {
		return pool;
	}
	
	/**
	 * ������ͨ��select�γɱ�ѡ������黹���ع�Ԫ�سأ�����ͨ��ָ�����������в���������set����
	 * data�ֶ��е�Ԫ�ز����ܱ��κ�һ���������޸�
	 * ElementPoolά����һ��ָ�����飬���ڼ�¼��ǰԪ�س���ÿһ��λ������Ӧ��data�е�Ԫ��
	 * @author congxiaoyao
	 * 
	 * @param <E> E����Ҫ��SelectableArray��T������ͬ������ᷢ������ת������
	 */
	public class ElementPool<E> implements Iterable<E>{
		private int[] elements;
		private int size;
		
		private ElementPool(int size) {
			this.size = size;
			this.elements = new int[size];
			for(int i=0;i<size;i++) elements[i] = i;
		}
		
		/**
		 * ����Ԫ�س��е�ĳ��Ԫ�صĸ�ֵ�����Ӱ�쵽�Ѿ�ѡ��������е�ĳ��Ԫ�ص�ֵ
		 * @param index ��Ԫ�س���Ԫ������ɵ�Ԫ������Ϊ��׼��index
		 * @param element ����ҪΪT����
		 * @throws ArrayIndexOutOfBoundsException
		 */
		@SuppressWarnings("unchecked")
		public void set(int index , E element) throws ArrayIndexOutOfBoundsException{
			if(index >= size) throw new ArrayIndexOutOfBoundsException(index);
			data[elements[index]] = (T) element;
		}
		
		@SuppressWarnings("unchecked")
		public E get(int index) throws ArrayIndexOutOfBoundsException{
			if(index >= size) throw new ArrayIndexOutOfBoundsException(index);
			return (E) data[elements[index]];
		}
		
		public int size() {
			return size;
		}
		
		protected void reset(){
			size = elements.length;
			for(int i=0;i<size;i++) elements[i] = i;
		}

		@Override
		public Iterator<E> iterator() {
			return new ElementPoolItr();
		}
		
		class ElementPoolItr implements Iterator<E>{
			int index = 0;
			
			@Override
			public boolean hasNext() {
				return index < size;
			}

			@SuppressWarnings("unchecked")
			@Override
			public E next() {
				return (E) data[elements[index++]];
			}
			
		}
	}
}