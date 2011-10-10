package com.glavsoft.viewer;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import javax.swing.JButton;

import org.apache.commons.io.FileUtils;

import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JSeparator;
import java.awt.Choice;
import javax.swing.JComboBox;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ViewerFrame extends JFrame
						 implements ActionListener, ItemListener
	{

	private JPanel contentPane;
	private JFrame saveFrame;
	final JFileChooser fc = new JFileChooser();
	private Project project;
	private JButton btnSelectLocation;
	private JButton btnAddFile;
	private JButton btnCopyToPhone;
	private JButton btnReset;
	private JButton btnLaunchExperiment;
	private JButton btnSave;
	private JButton btnNewProject;
	private JComboBox comboBox;
	private List list;	
	private JTextField textField;
	public Viewer caller;

	public void itemStateChanged(ItemEvent e){		
		if (e.getSource() == comboBox){
			this.project = caller.projects.get(caller.projects.indexOf(comboBox.getSelectedItem()));
			if(this.project != null){
				this.textField.setText(this.project.name);
				for(String i : this.project.origin_files){
					this.list.add(i);
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
	            String[] items = list.getItems();
	            if (!Arrays.asList(items).contains(file.getAbsolutePath())){
	            	list.add(file.getAbsolutePath());		          
	            }else{
	            	JOptionPane.showMessageDialog(this, "This file has already been added.");
	            }	            
	            //This is where a real application would open the file.
	            
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
	            	Vector<String> dest_files = new Vector<String>();
		            for(String item:items){
		            	File orig = new File(item);
		            	File dest = new File(projdir.getAbsolutePath()+"/"+orig.getName());
		            	dest_files.add(dest.getAbsolutePath());
		            	try {
							FileUtils.copyFile(orig,dest);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		            	p.files = (String[]) dest_files.toArray();
		            }
	            }
	            
            	saveFrame.setVisible(false);	            
            	JOptionPane.showMessageDialog(this, "The files have been copied to \n"
            			+fc.getSelectedFile().getAbsolutePath()+"/oz-prototyping");	            
	        }
	   }else if(e.getSource() == btnReset){		   
		   list.removeAll();
	   }else if(e.getSource() == btnLaunchExperiment){
		   this.setVisible(false);
		   caller.experiment();	   
	   }else if(e.getSource() == btnSave){
		   if(this.project == null){
			   this.project = new Project();
			   this.project.name = textField.getText();
			   this.project.origin_files = list.getItems();
			   this.caller.projects.add(this.project);
			   comboBox.removeAll();
			   for (Project p: caller.projects){
					comboBox.addItem(p);
				}
			   comboBox.updateUI();
			   this.caller.saveProjects();
		   }else{
			   this.project.name = textField.getText();
			   this.project.origin_files = list.getItems();
		   }
	   }else if(e.getSource() == btnNewProject){
		   textField.setText("");
		   list.removeAll();
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
		setBounds(100, 100, 404, 696);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(6, 6, 392, 662);
		contentPane.add(tabbedPane);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Preparation", null, panel, null);
		panel.setLayout(null);
		
		btnAddFile = new JButton("Add File...");
		btnAddFile.setBounds(132, 142, 107, 29);
		btnAddFile.addActionListener(this);
		panel.add(btnAddFile);
		
		list = new List();
		list.setBounds(27, 177, 315, 385);
		panel.add(list);
		
		btnCopyToPhone = new JButton("Generate folder");
		btnCopyToPhone.setBounds(206, 49, 136, 29);
		btnCopyToPhone.addActionListener(this);
		panel.add(btnCopyToPhone);
		
		btnReset = new JButton("Clear");
		btnReset.setBounds(161, 568, 78, 29);
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
		
		comboBox = new JComboBox();
		comboBox.setBounds(97, 16, 177, 27);
		comboBox.addItemListener(this);
		for (Project p: this.caller.projects){
			comboBox.addItem(p);
		}
		panel.add(comboBox);
		
		JPanel panel_1 = new JPanel();
		
		btnLaunchExperiment = new JButton("Launch experiment");
		btnLaunchExperiment.setBounds(131, 6, 107, 29);
		btnLaunchExperiment.addActionListener(this);
		panel_1.add(btnLaunchExperiment);
		
		tabbedPane.addTab("Experiment", null, panel_1, null);

	}
}
