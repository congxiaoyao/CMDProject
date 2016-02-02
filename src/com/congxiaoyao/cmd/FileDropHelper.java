package com.congxiaoyao.cmd;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * �ļ���ק����������CommandWindow�󶨣������������CommandWindow���ļ�
 * �����ļ�ͨ��{@code onFileDrop}����֪ͨ���
 * 
 * @version 1.0
 * @author congxiaoyao
 * @date 2016.1.24
 */
public class FileDropHelper {
	
	public FileDropHelper(Component c) {
		
		new DropTarget(c, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
					dtde.getTransferable();
					if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
						@SuppressWarnings("unchecked")
						List<File> list = (List<File>) (dtde.getTransferable()
								.getTransferData(DataFlavor.javaFileListFlavor));
						//ֻ֧��һ������һ���ļ�
						File file = (File) list.get(0);
						onFileDrop(file);
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (UnsupportedFlavorException ufe) {
					ufe.printStackTrace();
				}
			}
		});
	}
	
	public void onFileDrop(File file){}
}