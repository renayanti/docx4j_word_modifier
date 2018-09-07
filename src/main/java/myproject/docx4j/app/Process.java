/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myproject.docx4j.app;


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
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.XSLTUtils;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.PPr;
import sun.security.acl.WorldGroupImpl;
/**
 *
 * @author x
 */
public final class Process extends JPanel {

	FileInputStream fc;
	WordprocessingMLPackage document;
//	private static Logger logger = Logger.getLogger(Process.class.getName());
	static class vmlFactory extends org.docx4j.vml.ObjectFactory  {
	}
	
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
		Logger logger = Logger.getLogger(XSLTUtils.class.getName());
		ObjectFactory wfactory	 = Context.getWmlObjectFactory();
//		vmlFactory vfactory		=  Context.getVmlObjectFactory();
//		logger.setLevel(Level.OFF);
		Docx4jProperties.getProperties().setProperty("docx4j.Log4j.Configurator.disabled", "true");
//		Log4jConfigurator.configure();            
//		org.docx4j.convert.out.pdf.viaXSLFO.Conversion.log.setLevel(Level.OFF);
		BasicConfigurator.configure();
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		fc =in;
		document  = WordprocessingMLPackage.load(in);
		MainDocumentPart mainpart = document.getMainDocumentPart();
		List<Object> jaxbNodes = mainpart.getContent();
//		Body body =((Document)(mainpart.getContents())).getBody();
//		List<Object> jaxbNodes = body.getContent();
//		String XPATH_TO_SELECT_TEXT_NODES = "//w:p";
//		List<Object> jaxbNodes = mainpart.getJAXBNodesViaXPath(XPATH_TO_SELECT_TEXT_NODES, true);

		for (Object jaxbNode : jaxbNodes) {
//			final String paragraphString = jaxbNode.toString();
//			System.out.println("[Start]: " + paragraphString);
//			PPr pPr = ((P)XmlUtils.unwrap(jaxbNode) ).getPPr();
//			if (pPr != null && pPr.getPStyle() != null) {
//				String style = pPr.getPStyle().getVal();
//				logger.info("style==>"+style+"/n"+"string==>"+paragraphString);
//			}

//			System.out.println(XmlUtils.marshaltoString(jaxbNode, true, true));
			String p_xml = XmlUtils.marshaltoString(jaxbNode, true, true);
			logger.info("p__xml==>"+p_xml);
			P p  = (P)XmlUtils.unmarshalString(p_xml);
			if (p != null && p.getContent() != null) {
//				String style = pPr.getPStyle().getVal();
				List<Object> p_items = p.getContent();
				for(Object p_item : p_items){
					String r_xml = XmlUtils.marshaltoString(p_item, true, true);
//					logger.info("r_xml==>"+r_xml);
//					R r  = (R)XmlUtils.unmarshalString(r_xml);
					Object o_p = XmlUtils.unwrap(p_item); 
					if (o_p instanceof org.docx4j.wml.R) {
						
//						String r_xml = XmlUtils.marshaltoString(p_item, true, true);
						R r  = (R)XmlUtils.unmarshalString(r_xml);
						if (r != null && r.getContent() != null) {
							List<Object> r_items = r.getContent();
							for(Object r_item : r_items){
//								String r_item_xml = XmlUtils.marshaltoString(r_item, true, true);
//								logger.info("r_item__xml==>"+r_item_xml);
								Object o_r = XmlUtils.unwrap(r_item); 
								if (o_r instanceof org.docx4j.wml.R.LastRenderedPageBreak) {
									String pb_xml = XmlUtils.marshaltoString(p_item, true, true);
//									logger.info("pb__xml==>"+pb_xml);
								}
							}
						}
//						R tbl = (R) run;
//						Tr t = (Tr) tbl.getContent().get(0);

//						System.out.println(t.getContent());
//						System.out.println(t.toString());
//						System.out.println(XmlUtils.unwrap(t.getContent().get(0)));
					}
					
//					
				}
				
			}
//			if (jaxbNodes instanceof JaxbXmlPart) {
//				System.out.println("((JaxbXmlPart)source).getJAXBContext()).toString() =======>"+(((JaxbXmlPart)jaxbNodes).getXML()));
//			}
//			else if (jaxbNode instanceof XmlPart) {
////				((XmlPart)destination).setDocument((Document)((XmlPart)source).getDocument().cloneNode(true));
////			Node node = ((XmlPart)source).getDocument().getDocumentElement();
//				System.out.println("=======>"+(((XmlPart)jaxbNode).getDocument().getTextContent()));
//				System.out.println("=======>"+(((XmlPart)jaxbNode).getDocument().getNodeValue()));
//			}
//			else if(jaxbNode instanceof MainDocumentPart) {
//				logger.info("source: " + ((JaxbXmlPart)jaxbNode).getXML());
//			}
		}
		
//		RelationshipsPart rp = mainpart.getRelationshipsPart();
//		for ( Relationship r : rp.getRelationships().getRelationship()) {
//			logger.info("\nFor Relationship Id=" + r.getId()
//					+ " Source is " + rp.getSourceP().getPartName()
//					+ ", Target is " + r.getTarget()
//					+ " type " + r.getType() + "\n");
//			Part part = rp.getPart(r);
//		}
		
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
