/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myproject.xwpf.app;


import java.awt.EventQueue;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.BasicConfigurator;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
/**
 *
 * @author x
 */
public final class Process extends JPanel {

	FileInputStream fc;
	WordprocessingMLPackage document;
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedOperationException ex) {
					ex.printStackTrace();
				} catch (UnsupportedLookAndFeelException ex) {
					Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
				}

				JFrame frame = new JFrame("test");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(new MainPanel());
				frame.pack();
				frame.setLocationRelativeTo(null);

				frame.setVisible(true);
				frame.getComponents();
			}

		});
	}

	Process(FileInputStream in) throws FileNotFoundException,IOException, Docx4JException{
		BasicConfigurator.configure();
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		fc =in;
		document  = WordprocessingMLPackage.load(in);
		MainDocumentPart mainpart = document.getMainDocumentPart();
		List <Object> objects = mainpart.getContent();
		for (Object object  : objects ){
			String string = object.toString();
			System.out.println(string);
		}
		
		
		
		
	}
	
	
	
	


}
