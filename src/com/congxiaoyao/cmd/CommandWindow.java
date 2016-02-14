package com.congxiaoyao.cmd;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.StringReader;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;

import com.congxiaoyao.cmd.CodeAssistant.WeightedString;

/**
 * ��JTextAreaд�ɵķ�windows��CMD���ڣ���ΪCMD��ܵ�һ���֣���Ҫ����UI���������
 * �����޲εĹ��캯��ʵ������ʹ��{@code CommandWindow#setVisible()}������ʾ �رմ��������{@code closeWindow}����
 * �û������ÿһ�кϷ����ݶ����Իص��ӿڵ���ʽ֪ͨ���
 * {@code OnSubmitListener#onSubmit(String)}
 * ֧�ִ�����ʾ���ܣ�Ĭ�ϲ����������������빹��{@code CodeAssistant}ʵ��������
 * {@code #setAssistant(CodeAssistant)}
 * ��סctrl+���¼�ͷ�ɲ鿴֮ǰ�����������
 * ����С�����뿴���ڹ��з����ķ���ע��
 *
 * @see OnSubmitListener#onSubmit(String)
 * @see #setAssistant(CodeAssistant)
 *
 * @version 1.1
 * @author congxiaoyao
 * @date 2016.1.24
 */

public class CommandWindow extends JFrame{

	public static final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0);
	public static final KeyStroke BACK = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,0);
	public static final KeyStroke PASTE = KeyStroke.getKeyStroke(KeyEvent.VK_V,KeyEvent.CTRL_MASK);
	public static final KeyStroke CUT = KeyStroke.getKeyStroke(KeyEvent.VK_X,KeyEvent.CTRL_MASK);
	public static final KeyStroke ARROW_UP = KeyStroke.getKeyStroke(KeyEvent.VK_UP,KeyEvent.CTRL_MASK);
	public static final KeyStroke ARROW_DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,KeyEvent.CTRL_MASK);
	
	public String HINT = "������>";
	public String LFHINT = "\n������>";
	public int    LEN_HINT = HINT.length();

	private JTextArea textArea;
	private JScrollPane scrollPane;
	private Font font = new Font("����", Font.BOLD, 15);

	private PrintStream printStream;
	
	private RoundList<String> inputs = new RoundList<>(10);
	private int inputsPointer = 0;
	
	private CodeAssistant assistant;
	
	private OnSubmitListener onSubmitListener;

	public CommandWindow(int width , int height) {
		super("����������");

		initPrintStream();

		textArea = new CMDTextArea(HINT);
		textArea.setFont(font);
		textArea.enableInputMethods(false);
		textArea.setLineWrap(true);
		moveCaretToBottom();
		scrollPane = new JScrollPane(textArea);
		add(scrollPane);

		setBounds(width , height);
		setVisible(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	public CommandWindow(boolean fullScreen) {
		this();
		if(fullScreen) {
			Toolkit kit = Toolkit.getDefaultToolkit();
	        Dimension dimension = kit.getScreenSize();
	        setBounds(dimension.width, dimension.height);
		}
	}

	public CommandWindow() {
		this(480,400);
	}

	/**
	 * ��������ض����CommandWindow�ֻ���ĸ�print����
	 */
	private void initPrintStream() {
		printStream = new PrintStream(System.out){
			@Override
			public void println(String x) {
				CommandWindow.this.println(x);
			}
			@Override
			public void println(Object x) {
				if(x == null) 
					println("null");
				else println(x.toString());
			}
			@Override
			public void print(String s) {
				textArea.append(s);
				moveCaretToBottom();
			}
			@Override
			public void print(Object object) {
				print(object.toString());
			}
		};
		System.setOut(printStream);
	}
	
	/**
	 * @return ���λ��
	 */
	private int getCaretPosition() {
		return textArea.getCaretPosition();
	}
	
	private int getHintPosition() {
		int position = 0;
		try {
			position = textArea.getLineStartOffset(textArea.getLineCount()-1);
			position += HINT.length();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return position;
	}
	
	/**
	 * �����������Ͷ�
	 */
	private void moveCaretToBottom() {
		textArea.setCaretPosition(getTextLength());
	}
	
	/**
	 * @return ��ǰ���ݵĳ���
	 */
	private int getTextLength() {
		return textArea.getDocument().getLength();
	}
	
	/**
	 * @return ��������һ���ַ����淵��true ����false
	 */
	private boolean isCaretAtBottom() {
		return getTextLength() == getCaretPosition();
	}
	
	/**
	 * @return ���ѡ��Ĳ���λ�ڿɱ༭������true
	 */
	private boolean isSelectLegal() {
		Caret caret = textArea.getCaret();
        int hintPos = getHintPosition();
        return (caret.getDot() - hintPos >= 0 && caret.getMark() - hintPos >= 0);
	}
	
	/**
	 * @return �������ѡ��״̬����true
	 */
	private boolean isSelecting() {
		Caret caret = textArea.getCaret();
		return caret.getDot() != caret.getMark();
	}
	
	/**
	 * @return ��굽��ʾ�������һ���ַ��ľ��룬�磺
	 * ������>abc|de
	 * |�����꣬��ʱ����3
	 */
	private int distanceBetweenCaretAndHint() {
		return getCaretPosition() - getHintPosition();
	}
	
	/**
	 * @return textArea�����һ�е�����
	 */
	private String getLastLine() {
		try {
			int offset = textArea.getLineStartOffset(textArea.getLineCount()-1);
			return textArea.getDocument().getText(offset, getTextLength() - offset);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @return �û������һ�е�����
	 */
	private String getLastInput(String lastLine) {
		if(lastLine == null) {
			lastLine = getLastLine();
		}
		if(lastLine.length() == 0) return "";
		return lastLine.substring(LEN_HINT, lastLine.length());
	}
	
	/**
	 * �滻���û�����Ĳ���Ϊcontent��Ҳ�������һ�е����ݣ�������hint��
	 * @param content
	 */
	private void replaceInputing(String content) {
		try {
			int offset = textArea.getLineStartOffset(textArea.getLineCount()-1);
			textArea.getDocument().remove(offset, getTextLength() - offset);
			textArea.append(HINT);
			textArea.append(content);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ���ÿ���̨�߶�
	 * @param height �߶ȵ�����ֵ
	 */
	public void setCommandHeight(int height) {
		setBounds(getWidth(), height);
	}
	
	/**
	 * �رմ���
	 */
	public void closeWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dispose();
	}
	
	/**
	 * ��������е�����
	 */
	public void clearCommandWindow() {
		textArea.setText("");
	}
	
	/**
	 * ���������С
	 * @param size
	 */
	public void setFontSize(int size) {
		this.font = new Font("����", Font.TYPE1_FONT, size);
		textArea.setFont(font);
	}
	
	/**
	 * ������ʾ����ʾ������
	 * @param hint
	 */
	public void setHint(String hint) {
		HINT = hint;
		LFHINT = "\n"+hint;
		LEN_HINT = hint.length();
	}
	
	/**
	 * ��ʾ����
	 * @return
	 */
	public CommandWindow setVisible() {
		setVisible(true);
		return this;
	}
	
	/**
	 * ����һ�����ڵĴ�С�����������ʾ
	 * @param width frame���
	 * @param height frame�߶�
	 */
	public void setBounds(int width ,int height)
	{
		Dimension scrSize=Toolkit.getDefaultToolkit().getScreenSize();   
		setBounds((int) (scrSize.getWidth()-width)/2, (int) (scrSize.getHeight()-height)/2  
				,width, height);
	}
	
	/**
	 * ���ض������������sysout�ػؿ���̨
	 */
	public void resetPrintStream() {
		printStream = new PrintStream(System.out);
		System.setOut(printStream);
	}
	
	/**
	 * �ڴ��������string��ֵ,�����string�Ľ�β����\4������Ϊ��Ҫ����һ�����HINT
	 * @param string
	 */
	public void println(String string){
		int len = string.length();
		if (len != 0 && string.charAt(0) == '\5') {
			printlnSmoothly(string.substring(1, len));
		}
		else if(len != 0 && string.charAt(len-1) == '\4') {
			textArea.append(string.substring(0, len-1));
			textArea.append(LFHINT);
		}else {
			textArea.append(string);
			textArea.append("\n");
		}
		moveCaretToBottom();
	}
	
	/**
	 * �ڴ��������string��ֵ,������һ���ӱĳ������ǻ����ĳ����ģ�ǣ�������߳����⣬������
	 * �뾡���ڴ�������ʹ�ô˺���������������������ʾ������
	 * @param string
	 */
	private boolean can = true;
	public void printlnSmoothly(String string) {
		if(string == null) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					textArea.getDocument().remove(getTextLength()-HINT.length(), HINT.length());
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				BufferedReader reader = new BufferedReader(new StringReader(string));
				String line = null;
				try {
					can =false;
					while((line = reader.readLine()) != null) {
						System.out.println(line);
						Thread.sleep(5);
					}
					can = true;
					reader.close();
					textArea.append(HINT);
					moveCaretToBottom();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public JTextArea getTextArea() {
		return textArea;
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	public int getFontSize() {
		return font.getSize();
	}
	
	public void setAssistant(CodeAssistant assistant) {
		this.assistant = assistant;
	}

	public void setOnSubmitListener(OnSubmitListener onSubmitListener) {
		this.onSubmitListener = onSubmitListener;
	}
	
	private class CMDTextArea extends JTextArea {

		private static final long serialVersionUID = 1L;

		public CMDTextArea(String HINT) {
			super(HINT);
		}

		/**
		 * ��д��processKeyBinding�����������˻س����˸�ճ�����ɼ���ĸ���Ų����¼��Դ���
		 * �����˶�textArea��һ���ֲ�����ʹ�������ֵ��������CMDһ��
		 * return����Ҳ�����return�ģ�true��false����ν��
		 */
		@Override
		protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
			if(condition != 0) return false;
			//�ȼ�����һ�е���ʾ���Ƿ����
			String lastLine = getLastLine();
			if(lastLine.length() < HINT.length() && can) {
				textArea.append(HINT);
				moveCaretToBottom();
				if(ks.equals(ENTER)) return false;
				return processKeyBinding(ks, e, condition, pressed);
			}
			//����ctrl+�ϼ�ͷ���¼�ͷ
			if(ks.equals(ARROW_UP)) {
				if(inputs.size() > 0) {
					replaceInputing(inputs.getAllowsNegativeIndex(--inputsPointer));
					moveCaretToBottom();
				}
			}else if(ks.equals(ARROW_DOWN)) {
				if(inputs.size() > 0) {
					replaceInputing(inputs.getAllowsNegativeIndex(++inputsPointer));
					moveCaretToBottom();
				}
			}
			//���ػس�
			if(ks.equals(ENTER)) {
				//������ڴ�����ʾ״̬
				if(isSelecting() && assistant !=null) {
					int len = getTextLength();
					select(len, len);
					moveCaretToBottom();
					return true;
				}
				if(!isCaretAtBottom()) {
					moveCaretToBottom();
					return processKeyBinding(ks, e, condition, pressed);
				}
				//��ȡ�û�����
				String content = getLastInput(lastLine);
				if(!content.equals("") && onSubmitListener != null){
					textArea.append("\n");
					inputs.add(content);
					inputsPointer = inputs.size();
					onSubmitListener.onSubmit(content);
					lastLine = getLastLine();
					if (lastLine.isEmpty()) {
						textArea.append(HINT);
					}else if(!lastLine.equals(HINT)){
						textArea.append(LFHINT);
					}
				}else {
					textArea.append(LFHINT);
				}
				moveCaretToBottom();
				return false;
			}
			//�����˸�
			else if (ks.equals(BACK)) {
				if(isSelecting()) {
					if(isSelectLegal()) 
						return super.processKeyBinding(ks, e, condition, pressed);
				}else {
					int dis = distanceBetweenCaretAndHint();
					if(dis > 0) 
						return super.processKeyBinding(ks, e, condition, pressed);
				}
				moveCaretToBottom();
				return false;
			}
			//����ճ��
			else if(ks.equals(PASTE)){
				if(!isSelectLegal()) {
					moveCaretToBottom();
					return false;
				}
				return super.processKeyBinding(ks, e, condition, pressed);
			}
			//���ؼ���
			else if(ks.equals(CUT)) {
				if(isSelectLegal())
					return super.processKeyBinding(ks, e, condition, pressed);
		        return false;
			}
			//��ֹ�����ڲ���ȷ�ĵط�����
			else if(ks.getKeyChar() > 31 && ks.getKeyChar() < 127){
				if(!isSelectLegal()) {
					moveCaretToBottom();
					return super.processKeyBinding(KeyStroke.getKeyStroke(ks.getKeyChar()), e, condition, pressed);
				}
			}
			return handleCodeCompletion(ks, e) ? true : super.processKeyBinding(ks, e, condition, pressed);
		}

		/**
		 * ���������ʾ��ͨ�����ڵ�CodeAssistant��������Ӧ���벢��ʾ��commandWindow��
		 * @param ks
		 * @param e
         * @return ��������˿ո����ʾ�˴���Ļ�����true�����򷵻�false
         */
		public boolean handleCodeCompletion(KeyStroke ks , KeyEvent e) {
			if (assistant == null) return false;
			if(ks.getKeyEventType() == 400 &&ks.getKeyChar() == ' ') {
				if(isSelecting()) {
					int pos = getTextLength();
					textArea.select(pos, pos);
					
					return true;
				}else {
					return false;
				}
			}
			if(ks.getKeyEventType() == 402 &&e.getKeyChar() > 31 && e.getKeyChar() < 127) {
				int start = getCaretPosition();
				String lastInput = getLastInput(null);
				SelectableArray<WeightedString> find = assistant.find(lastInput);
				int select = -1, min = Integer.MAX_VALUE;
				for (int i = 0; i < find.size(); i++) {
					WeightedString weightedString = find.get(i);
					if (weightedString.weight == 0) {
						int dis = weightedString.string.length() - lastInput.length();
						if (dis < min) {
							min = dis;
							select = i;
						}
					}
				}
				if (select != -1) {
					replaceInputing(find.get(select).string);
					select(getTextLength(), start);
					moveCaretPosition(start);
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * �û���������ݻ�ͨ���˽ӿڻص�����ÿһ���û���һ��������Ϊ�����Ĳ���֪ͨ���
	 * @author congxiaoyao
	 *
	 */
	public interface OnSubmitListener{
		void onSubmit(String content);
	}
}