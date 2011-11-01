package com.glavsoft.viewer;

import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;

@SuppressWarnings("serial")
public class ViewerFrame extends JFrame
						 implements ActionListener, ItemListener
	{

	private JPanel contentPane;
	private JFrame saveFrame;
	final JFileChooser fc = new JFileChooser();
	private Project project = new Project();	
	private JButton btnSelectLocation;
	private JButton btnAddFile;
	private JButton btnCopyToPhone;
	private JButton btnReset;
	private JButton btnLaunchExperiment;
	private JButton btnSave;
	private JButton btnNewProject;
	private JButton btnDelete;
	private JButton btnDeleteButton;
	private JButton btnSaveButton;
	private JComboBox comboBox;
	public JComboBox comboBox_2;
	private List list;	
	private JTextField textField;
	private String selectedItem;
	private ImagePanel panel_3;
	public Viewer caller;

	public void itemStateChanged(ItemEvent e){		
		if (e.getSource() == comboBox){
			if(caller.projects.indexOf(comboBox.getSelectedItem()) != -1){
				this.project = caller.projects.get(caller.projects.indexOf(comboBox.getSelectedItem()));
				if(this.project != null){
					this.list.removeAll();
					this.textField.setText(this.project.name);
					for(String i : this.project.origin_files){
						this.list.add(i);
					}
					this.comboBox_2.removeAll();
					for(Action a : this.project.actions){
						this.comboBox_2.addItem(a);
					}
					this.comboBox_2.setSelectedItem(null);
					this.comboBox_2.updateUI();
				}
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
	    //Handle open button action.
	    if (e.getSource() == btnAddFile ) {
	    	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	        int returnVal = fc.showOpenDialog(ViewerFrame.this);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            String[] items = this.list.getItems();
	            if (!Arrays.asList(items).contains(file.getAbsolutePath())){
	            	this.list.add(file.getAbsolutePath());
	            	Action a = new Action(file.getName());
	            	this.project.actionMap.put(file.getAbsolutePath(), a);
	            	this.project.actions.add(a);
	            	comboBox_2.addItem(a);
	            }else{
	            	JOptionPane.showMessageDialog(this, "This file has already been added.");
	            }	            	            	            
	        }
	   }else if(e.getSource() == btnCopyToPhone){
		   saveFrame = new JFrame();		   
		   JPanel contentPanel = new JPanel();	
		   contentPanel.setLayout(null);
		   saveFrame.setBounds(300,300,600,100);
		   contentPanel.setBounds(50, 50, 600, 70);
		   saveFrame.setContentPane(contentPanel);
		   JLabel instructions = new JLabel();
		   instructions.setText("A directory will be created which you should move or copy to your phone's SD card root");
		   instructions.setBounds(5, 10, 600, 29);
		   contentPanel.add(instructions);
		   
		   btnSelectLocation = new JButton();
		   btnSelectLocation.addActionListener(this);
		   btnSelectLocation.setBounds(5,40,150,29);
		   btnSelectLocation.setText("Save in...");
		   contentPanel.add(btnSelectLocation);
		   
		   saveFrame.setVisible(true);		   
		   
	   }else if(e.getSource() == btnSelectLocation){
		   fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		   int returnVal = fc.showOpenDialog(ViewerFrame.this);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {	        	
	            File file = new File(fc.getSelectedFile().getAbsolutePath()+"/oz-prototyping");
	            file.mkdir();
	            for(Project p:caller.projects){
	            	File projdir = new File(fc.getSelectedFile().getAbsolutePath()+"/oz-prototyping/"+p.name);
	            	projdir.mkdir();
	            	String[] items = p.origin_files;
		            for(String item:items){
		            	File orig = new File(item);
		            	File dest = new File(projdir.getAbsolutePath()+"/"+orig.getName());
		            	try {
							FileUtils.copyFile(orig,dest);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		            }
	            }	            
            	saveFrame.setVisible(false);	            
            	JOptionPane.showMessageDialog(this, "The files have been copied to \n"
            			+fc.getSelectedFile().getAbsolutePath()+"/oz-prototyping");	            
	        }
	   }else if(e.getSource() == btnReset){		   
		   list.removeAll();
		   project.actionMap.clear();
	   }else if(e.getSource() == btnLaunchExperiment){
		   this.setVisible(false);
		   caller.experiment();	   
	   }else if(e.getSource() == btnSave){
		   if(!caller.projects.contains(this.project)){
			   this.project.name = textField.getText();
			   this.project.origin_files = list.getItems();
			   this.caller.projects.add(this.project);
			   comboBox.removeAllItems();
			   for (Project p: caller.projects){
					comboBox.addItem(p);
				}
			   comboBox.setSelectedIndex(this.caller.projects.size()-1);
		   }else{
			   this.project.name = textField.getText();
			   this.project.origin_files = list.getItems();
			   this.caller.projects.remove(this.project);
			   this.caller.projects.add(this.project);
		   }
		   comboBox.updateUI();
		   this.caller.saveProjects();
	   }else if(e.getSource() == btnNewProject){
		   this.project = new Project();
		   textField.setText("");
		   textField.updateUI();		   
		   list.removeAll();
		   list.repaint();
	   }else if(e.getSource() == btnDelete){
		   this.caller.projects.remove(this.project);
		   this.project = new Project();
		   textField.setText("");
		   textField.updateUI();		   
		   list.removeAll();
		   list.repaint();
		   comboBox.removeAllItems();
		   for (Project p: caller.projects){
				comboBox.addItem(p);
		   }
		   caller.saveProjects();
	   }else if(e.getSource() == list){		
		   String i = list.getSelectedItem();
		   this.selectedItem = i;
		   Action a = this.project.actionMap.get(i);   
		   int point = a.filename.lastIndexOf(".");
		   String ext = a.filename.substring(point+1,a.filename.length());
		   if(this.caller.extensionMap.get(ext) == "image"){
			   panel_3.setImage(list.getSelectedItem());
			   panel_3.a = a;
		   }
		   this.comboBox_2.setSelectedItem(null);
	   }else if(e.getSource() == btnDeleteButton){
		   this.panel_3.a.buttons.remove(this.panel_3.pressed);
		   this.panel_3.pressed = null;
		   this.panel_3.updateUI();
	   }else if(e.getSource() == btnSaveButton){
		   if(this.panel_3.pressed != null){
			   this.panel_3.pressed.action = (Action)comboBox_2.getSelectedItem();
		   }
		   this.panel_3.a.buttons.remove(this.panel_3.pressed);
		   this.panel_3.a.buttons.add(this.panel_3.pressed);
		   this.project.actionMap.put(selectedItem, this.panel_3.a);
		   this.project.actions.remove(this.panel_3.a);
		   this.project.actions.add(this.panel_3.a);
		   this.caller.saveProjects();
	   }
	}

	/**
	 * Create the frame.
	 */
	public ViewerFrame(Viewer view) {
		this.caller = view;
		setTitle("Oz Prototyping Client");
		fc.setControlButtonsAreShown(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 813, 696);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(6, 6, 801, 668);
		contentPane.add(tabbedPane);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Preparation", null, panel, null);
		panel.setLayout(null);
		
		btnAddFile = new JButton("Add File...");
		btnAddFile.setBounds(132, 142, 107, 29);
		btnAddFile.addActionListener(this);
		panel.add(btnAddFile);
		
		list = new List();
		list.setMultipleMode(false);
		list.addActionListener(this);
		list.setBounds(27, 177, 315, 385);
		panel.add(list);
		
		btnCopyToPhone = new JButton("Generate folder");
		btnCopyToPhone.setBounds(206, 49, 136, 29);
		btnCopyToPhone.addActionListener(this);
		panel.add(btnCopyToPhone);
		
		btnReset = new JButton("Clear");
		btnReset.setBounds(91, 568, 78, 29);
		btnReset.addActionListener(this);
		panel.add(btnReset);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(6, 76, 359, 12);
		panel.add(separator);
		
		JLabel lblProject = new JLabel("Project:");
		lblProject.setBounds(42, 20, 61, 16);
		panel.add(lblProject);
		
		btnNewProject = new JButton("New Project");
		btnNewProject.setBounds(52, 49, 117, 29);
		btnNewProject.addActionListener(this);
		panel.add(btnNewProject);
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(this);
		btnSave.setBounds(244, 568, 78, 29);
		panel.add(btnSave);		
		
		textField = new JTextField();
		textField.setBounds(89, 100, 198, 28);
		panel.add(textField);
		textField.setColumns(10);
		
		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(42, 106, 61, 16);
		panel.add(lblName);				
		
		btnDelete = new JButton("Delete");
		btnDelete.setBounds(169, 568, 78, 29);
		btnDelete.addActionListener(this);
		panel.add(btnDelete);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBounds(381, 20, 381, 577);
		panel.add(panel_2);
		panel_2.setLayout(null);
		
		JLabel lblAction = new JLabel("Action:");
		lblAction.setBounds(35, 477, 61, 16);
		panel_2.add(lblAction);
		
		comboBox_2 = new JComboBox();
		comboBox_2.setBounds(92, 473, 249, 27);
		comboBox_2.addActionListener(this);
		panel_2.add(comboBox_2);
		
		btnSaveButton = new JButton("Save");
		btnSaveButton.setBounds(60, 522, 117, 29);
		btnSaveButton.addActionListener(this);
		panel_2.add(btnSaveButton);
		
		JLabel lblImageButtons = new JLabel("Image Buttons");
		lblImageButtons.setBounds(19, 16, 90, 16);
		panel_2.add(lblImageButtons);
		
		JLabel lblDragDrop = new JLabel("Drag & Drop on the image to create buttons");
		lblDragDrop.setBounds(19, 36, 276, 16);
		panel_2.add(lblDragDrop);
		
		panel_3 = new ImagePanel();
		panel_3.setBounds(19, 64, 340, 400);
		panel_3.caller = this;
		panel_2.add(panel_3);
		
		btnDeleteButton = new JButton("Delete");
		btnDeleteButton.setBounds(210, 522, 117, 29);
		btnDeleteButton.addActionListener(this);
		panel_2.add(btnDeleteButton);
		
		JPanel panel_1 = new JPanel();
		
		btnLaunchExperiment = new JButton("Launch experiment");
		btnLaunchExperiment.setBounds(131, 6, 107, 29);
		btnLaunchExperiment.addActionListener(this);
		panel_1.add(btnLaunchExperiment);
		
		comboBox = new JComboBox();
		comboBox.setBounds(97, 16, 177, 27);
		comboBox.addItemListener(this);
		for (Project p: this.caller.projects){
			comboBox.addItem(p);
		}
		panel.add(comboBox);
		
		tabbedPane.addTab("Experiment", null, panel_1, null);

	}
}
