package com.test;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;



import javax.swing.event.*;

public class photoView extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTree tree;
	private JButton nButton;
	private JButton pButton;
	private JPanel iconPanel;
	private JScrollPane iconScrollPane;
	private ImageIcon icon;
	private Image img;
	private int imgIndex;
	private JDesktopPane desktopPane;
	private int imgCount;
	private boolean stop;
	
	public photoView(){
		Container c = getContentPane();
		setResizable(false);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setExtendedState(photoView.MAXIMIZED_BOTH);
		setTitle("图片与GIF浏览器");
		setBounds(0,0,1024,768);
		JSplitPane splitPane = new JSplitPane();
		c.add(splitPane,"Center");
		splitPane.setDividerLocation(200);
		JScrollPane treeScrollPane = new JScrollPane();
		splitPane.setLeftComponent(treeScrollPane);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("");//建立根目录
		
		File[] files = File.listRoots();
		for(File file1:files){
			File file = file1;
			try{
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getCanonicalFile());
				root.add(node);
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
		tree = new JTree(root,true);
		tree.setRootVisible(false);
		tree.setCellRenderer(new BookCellRenderer());//使用新的渲染器
		treeScrollPane.setViewportView(tree);
		TreePath path1 = null;
		tree.addTreeWillExpandListener(new TreeWillExpandListener(){

			@Override
			public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
				
				iconPanel.removeAll();
				SwingUtilities.updateComponentTreeUI(iconPanel);
				stop=true;
			
				
			}
			

			@Override
			public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
				TreePath path = e.getPath();
				loadTreeNode(path);//加载子目录
				iconPanel.removeAll();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
				
				String Dir = getDir(path);
				if(node.getAllowsChildren()){
					if(path.equals(path1)){
						node=null;
					}
					new loadImgThread(Dir,node).start();//预览图片
				}else{
					if(!nButton.isEnabled()){
						nButton.setEnabled(false);
					}
					if(!pButton.isEnabled()){
						pButton.setEnabled(false);
					}
				}
				SwingUtilities.updateComponentTreeUI(iconPanel);
				path = path1;
			}
			
		});
		
		tree.addTreeSelectionListener(new TreeSelectionListener(){

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath path = tree.getSelectionPath();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
		
				String Dir = getDir(path);
				if(node.getAllowsChildren()){
					
					stop=true;
					
				}else{
					showImgInFrame(Dir);//窗口展示图片
				}
				
			}
			
		});
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		splitPane.setRightComponent(panel);
		desktopPane = new JDesktopPane();
		panel.add(desktopPane, "Center");
		JPanel operationPanel = new JPanel();
		operationPanel.setLayout(new BorderLayout());
		operationPanel.setPreferredSize(new Dimension(0,150));
		panel.add(operationPanel,"South");
		iconScrollPane = new JScrollPane();
		iconScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		iconPanel = new JPanel();
		iconScrollPane.setViewportView(iconPanel);
		operationPanel.add(iconScrollPane, "Center");
		
		nButton = new JButton();
		nButton.setText(">>");
		nButton.setEnabled(false);
		nButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				imgIndex++;
				JButton button = (JButton)iconPanel.getComponent(imgIndex);
				showImgInFrame(button.getName());
				if(imgIndex>0){
					if(!pButton.isEnabled()){
						pButton.setEnabled(true);
					}
				}
				if(imgIndex==imgCount-1){
					nButton.setEnabled(false);
				}
			}
			
		});
		operationPanel.add(nButton, "East");
		
		pButton = new JButton();
		pButton.setText("<<");
		pButton.setEnabled(false);
		pButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				imgIndex--;
				JButton button = (JButton)iconPanel.getComponent(imgIndex);
				showImgInFrame(button.getName());
				if(imgIndex<imgCount-1){
					if(!nButton.isEnabled()){
						nButton.setEnabled(true);
					}
				}
				if(imgIndex==0){
					pButton.setEnabled(false);
				}
			}
			
		});
		operationPanel.add(pButton, "West");
		
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowOpened(WindowEvent e){
				long beginTime = System.currentTimeMillis();
				String title = getTitle();
				
				Timer timer = new Timer(1000,new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e1) {
						long nowTime = System.currentTimeMillis();
						
						long time = nowTime - beginTime;
						setTitle(title+"  【温馨提示】    程序已运行："+time/1000+"秒");
						
					}
					
				});
				timer.start();
			}
			
			@Override
			public void windowClosing(WindowEvent e){
				closeFrame();
			}
			
			@Override
			public void windowClosed(WindowEvent e){
				Desktop desktop=Desktop.getDesktop();// 获取桌面程序管理器
			    try {
			        desktop.browse(new URI("http://www.baidu.com"));// 浏览指定网址
			    } catch (IOException e1) {
			        e1.printStackTrace();
			    } catch (URISyntaxException e1) {
			        e1.printStackTrace();
			    }
			}
		});
		addMouseMotionListener(new MouseMotionListener(){

			@Override
			public void mouseDragged(MouseEvent arg0) {
			    System.out.println("dfg");
				
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
	}
	public class BookCellRenderer implements TreeCellRenderer{
		JLabel label = new JLabel();
		
		public BookCellRenderer(){
			label.setForeground(Color.blue);
			label.setBackground(Color.GREEN);
			label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
			label.setOpaque(true);
			label.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null,
			        null));// 设置标签的边框
			
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			label.setText(value.toString());
			while(label.getText().length()<42){
				label.setText(label.getText()+" ");
			}
			
			return label;
		}
		
	}
	
	private void closeFrame(){
		int i =JOptionPane.showConfirmDialog(null, 
				"你确认要关闭窗口吗", "关闭窗口", JOptionPane.YES_NO_OPTION);
		
		if(i==JOptionPane.YES_OPTION){
			this.dispose();
		}
	}
	
	
	FileFilter fileFilter = new FileFilter(){

		
		public boolean accept(File file) {
			if(file.isDirectory()){
				return true;
			}else if(file.getName().toUpperCase().endsWith(".GIF")){
				return true;
			}else if(file.getName().toUpperCase().endsWith(".JPG")){
				return true;
			}else{
				return false;
			}
			
		}
		
	};
	
	
	private String getDir(TreePath path){
	
		StringBuffer buffer = new StringBuffer();
		Object[] nodes = path.getPath();
		for(int i =0;i<nodes.length;i++){
			buffer.append(nodes[i]);
			buffer.append("\\");
			
		}
		String Dir = buffer.toString();
		
		return Dir;
	}
	
	JProgressBar progressBar;
	public class MProgress extends JDialog{
		private static final long serialVersionUID =1L;
		JProgressBar progressBar;
		public MProgress(){
			progressBar = new JProgressBar();
			getContentPane().add(progressBar);
			setUndecorated(true);
			setAlwaysOnTop(true);
		}
		public JProgressBar getProgressBar(){
			return progressBar;
		}
	}
	MProgress cc;
	public class Progress {
		
		
		
		public Progress(){
			cc = new MProgress();
			if(imgCount>0){
				cc.setVisible(true);
			}
			progressBar =cc.getProgressBar();
			
			
			progressBar.setStringPainted(true);
			progressBar.setIndeterminate(true);
			progressBar.setString("图片导入中");
		    
			
			Point locationOnScreen = desktopPane.getLocationOnScreen();
			int x = (int) (locationOnScreen.getX() + 270);
			int y = (int) (locationOnScreen.getY() + 300);
			cc.setBounds(x,y,300,30);
		}
	}
	Boolean doing;
	private class loadImgThread extends Thread{
		private String selectedDir;
		private DefaultMutableTreeNode node;
		
		public loadImgThread(String selectedDir,DefaultMutableTreeNode node){
			this.selectedDir = selectedDir;
			this.node = node;
		}
		
		@Override
		public void run(){
			imgIndex = -1;
			imgCount = 0;
			stop = false;
			doing = true;
			int childCount = node.getChildCount();
			
			for(int i =0;i<childCount;i++){
				
				if(stop){
					cc.dispose();
					break;
				}
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(i);
				if(imgCount==1&&doing){
					new Progress();
					doing = false;
				}
				if(imgCount>0){
					Point locationOnScreen = desktopPane.getLocationOnScreen();
					int x = (int) (locationOnScreen.getX() + 270);
					int y = (int) (locationOnScreen.getY() + 300);
					cc.setBounds(x,y,300,30);
				}
				
				if(!childNode.getAllowsChildren()){
					imgCount++;
					String name = childNode.toString();
					MButton button = new MButton();
					button.setText(name);
					button.setName(selectedDir+name);
					button.setPreferredSize(new Dimension(120,120));
					button.addActionListener(new preview());
					iconPanel.add(button);
					iconScrollPane.validate();
					if(!nButton.isEnabled()){
						if(imgCount>0){
							nButton.setEnabled(true);
						}
					}
					try{
						Thread.sleep(100);
					}catch(Exception e){
						e.printStackTrace();
					}
					
				}
			}
			if(imgCount>0){
				progressBar.setIndeterminate(false);
				progressBar.setValue(100);
				progressBar.setString("全部图片导入完成");
				try{
					Thread.sleep(1000);
				}catch(Exception e){
					e.printStackTrace();
				}
				cc.dispose();
			}
			
		}
	}
	
	private class preview implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			showImgInFrame(((JButton)e.getSource()).getName());
			
		}
		
	}
	private class CanvasPanel extends Canvas{
		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(Graphics g){
			int height = getHeight();
			int w = icon.getIconWidth();
			int h = icon.getIconHeight();
			
			super.paint(g); 
			Graphics2D g2 = (Graphics2D)g;
			g2.drawImage(img,0,0,w*height/h,height,this);
			
		}
	}
	private void showImgInFrame(String Dir){
		JInternalFrame frame =desktopPane.getSelectedFrame();
		icon = new ImageIcon(Dir);
		img = Toolkit.getDefaultToolkit().getImage(Dir);
		String imgName =new File(Dir).getName();
		
		if(frame==null){
			frame = new JInternalFrame();
			frame.setIconifiable(true);
			frame.setMaximizable(true);
			frame.setResizable(true);
			frame.setBounds(-5, -5, 815, 600);
			desktopPane.add(frame);
			frame.setVisible(true);
			
		}
		
		if(!frame.getTitle().equals(imgName)){
			frame.setTitle(imgName);
			String upperName = Dir.toUpperCase();
			frame.getContentPane().removeAll();
			
			if(upperName.endsWith(".GIF")||upperName.endsWith(".GIF\\")){
				JLabel label= new JLabel();
				frame.getContentPane().add(label);
				label.setIcon(icon);
				
			}else{
				CanvasPanel cp = new CanvasPanel();
				frame.getContentPane().add(cp);
			}
		}
		
	}
	private class MButton extends JButton{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public MButton(){
			super();
			addMouseListener(new MouseAdapter(){
				@Override
				public void mouseClicked(MouseEvent e){
					if(e.getClickCount()==2&&e.getButton()==MouseEvent.BUTTON1){
						if(Desktop.isDesktopSupported()){
							Desktop desktop = Desktop.getDesktop();
							File file = new File(getName());
							try{
								desktop.open(file);
							}catch(Exception e1){
								e1.printStackTrace();
							}
						}
					}
				}
			});
		}
		@Override
		protected void paintComponent(Graphics g){
			int width = getWidth();
			int Height = getHeight();
		    ImageIcon icon = new ImageIcon(getName());
		    Graphics2D g2 = (Graphics2D)g;
		    g2.drawImage(icon.getImage(), 0, 0, width, Height-25,this);
		    g2.drawString(getText(), 5, Height-10);
			
		}
	}
	private void loadTreeNode(TreePath path){
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path
				.getLastPathComponent();
		File file = new File(getDir(path));
		File[] files = file.listFiles(fileFilter);
		for(File file1 :files){
			File file2 = file1;
			if(file2.isDirectory()){
				node.add(new DefaultMutableTreeNode(file2.getName()));
			}else{
				node.add(new DefaultMutableTreeNode(file2.getName(),false));
			}
		}
	}

	public static void main(String[] args) {
		photoView gg = new photoView();
		gg.setVisible(true);
		

	}

}
