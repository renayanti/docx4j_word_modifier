/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myproject.xwpf.app;


import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.bind.JAXBException;
import org.apache.log4j.BasicConfigurator;
import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.XSLTUtils;
import sun.security.acl.WorldGroupImpl;
/**
 *
 * @author x
 */
public final class Process extends JPanel {

	FileInputStream fc;
	WordprocessingMLPackage document;
//	private static Logger logger = Logger.getLogger(Process.class.getName());
	
	
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

	Process(FileInputStream in) throws FileNotFoundException,IOException, Docx4JException, JAXBException{
//		Logger logger = Logger.getLogger(XSLTUtils.class.getName());
//		logger.setLevel(Level.OFF);
		Docx4jProperties.getProperties().setProperty("docx4j.Log4j.Configurator.disabled", "true");
//		Log4jConfigurator.configure();            
//		org.docx4j.convert.out.pdf.viaXSLFO.Conversion.log.setLevel(Level.OFF);
		BasicConfigurator.configure();
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		fc =in;
		document  = WordprocessingMLPackage.load(in);
		MainDocumentPart mainpart = document.getMainDocumentPart();
		String XPATH_TO_SELECT_TEXT_NODES = "//w:p";
		List<Object> jaxbNodes = mainpart.getJAXBNodesViaXPath(XPATH_TO_SELECT_TEXT_NODES, true);

		for (Object jaxbNode : jaxbNodes) {
			final String paragraphString = jaxbNode.toString();
			System.out.println("[Start]: " + paragraphString);
		}
		
		
//		List <Object> objects = mainpart.getContent();
//		for (Object object  : objects ){
//			String string = object.toString();
//			System.out.println(string);
//		}
//		
//		WordprocessingMLPackage newdoc = copy(document);
//		writeDocxToStream(newdoc,"/home/x/Documents/temp/zz.docx");
		
	}
	
	protected static WordprocessingMLPackage copy(WordprocessingMLPackage doc) throws Docx4JException {
		WordprocessingMLPackage newdoc;
		// Make a copy of it
		Set<String> relationshipTypes = new TreeSet<String>();
			relationshipTypes.add(Namespaces.DOCUMENT);
			relationshipTypes.add(Namespaces.HEADER);
			relationshipTypes.add(Namespaces.FOOTER);
			//those are probably not affected but get visited by the 
			//default TraversalUtil.
			relationshipTypes.add(Namespaces.ENDNOTES);
			relationshipTypes.add(Namespaces.FOOTNOTES);
			relationshipTypes.add(Namespaces.COMMENTS);
			newdoc = (WordprocessingMLPackage) clone.process(doc, relationshipTypes);
			return newdoc;
	}
	
	
	private void writeDocxToStream(WordprocessingMLPackage template, String target) throws IOException, Docx4JException {
		File f = new File(target);
		template.save(f);
	}
	


}
