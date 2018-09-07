/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package myproject.docx4j.app;

import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.relationships.Relationships;
import org.docx4j.vml.CTLine;
import org.docx4j.vml.CTShapetype;
import org.docx4j.vml.CTStroke;
import org.w3c.dom.Document;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.CTShapeDefaults;
import org.docx4j.wml.Pict;


/** Create a partial deep copy of the document. All the parts are copied, 
 * as they may have some references to other parts. The data in the parts is 
 * only copied if the relationship type of the part is contained in the passed
 * relationshipTypes otherwise the new part contains a reference to the data 
 * of the old part.<br>
 * If the passed relationship types is null, then it will do a complete deep copy.
 * This is probably faster than storing and reading the document but it is restricted 
 * to Parts of the types: BinaryPart, JaxbXmlPart, CustomXmlDataStoragePart, XmlPart.<br>
 * If the passed relationship types is empty, then the passed Package is returned.  
 * 
 */
public class clone {
	
	protected static Logger log = LoggerFactory.getLogger(clone.class);
	
	
	public static OpcPackage process(OpcPackage opcPackage, Set<String> relationshipTypes) throws Docx4JException {
		
		OpcPackage ret = null;
		RelationshipsPart relPart = null;
		if (opcPackage != null) {
			if ((relationshipTypes != null) && (relationshipTypes.isEmpty())) {
				ret = opcPackage;
			}
			else {
				ret = createPackage(opcPackage);
				
				if (ret==null) {
					log.error("createPackage returned null!");
				}
				
				
				deepCopyRelationships(ret, opcPackage, ret, relationshipTypes);
				
				// Copy the font mappings
				if (opcPackage instanceof WordprocessingMLPackage) {
					
//					// First need shortcut to MDP
//					// .. get its name
//					PartName mdpName = ((WordprocessingMLPackage)opcPackage).getMainDocumentPart().getPartName();
//					// .. get the part
//					Part mdp = ((WordprocessingMLPackage)ret).getParts().get(mdpName);
//					// .. set the shortcut
//					ret.setPartShortcut(mdp, mdp.getRelationshipType());					
					
					try {
						((WordprocessingMLPackage)ret).setFontMapper(
								((WordprocessingMLPackage)opcPackage).getFontMapper(), false); //don't repopulate, since we want to preserve existing mappings
					} catch (Exception e) {
						// shouldn't happen
						log.error(e.getMessage(),e);
						throw new Docx4JException("Error setting font mapper on copy", e);
					}
				}
				
			}
		}
		return ret;
	}

	protected static OpcPackage createPackage(OpcPackage opcPackage) throws Docx4JException {
		
		OpcPackage ret = null;
		try {
			ret = opcPackage.getClass().newInstance();
		} catch (InstantiationException e) {
			throw new Docx4JException("InstantiationException duplicating package", e);
		} catch (IllegalAccessException e) {
			throw new Docx4JException("IllegalAccessException duplicating package", e);
		}
		
//		contentType
		ret.setContentType(new ContentType(opcPackage.getContentType()));
//		partName
		ret.setPartName(opcPackage.getPartName());
//		relationships
		//is done in an another method
//		userData
//		ret.setUserData(opcPackage.getUserData());
//		contentTypeManager
		ret.setContentTypeManager(opcPackage.getContentTypeManager());
//		customXmlDataStorageParts
		ret.getCustomXmlDataStorageParts().putAll(opcPackage.getCustomXmlDataStorageParts());
//		docPropsCorePart
		ret.setPartShortcut(opcPackage.getDocPropsCorePart(), Namespaces.PROPERTIES_CORE);
//		docPropsCustomPart
		ret.setPartShortcut(opcPackage.getDocPropsCustomPart(), Namespaces.PROPERTIES_CUSTOM);
//		docPropsExtendedPart
		ret.setPartShortcut(opcPackage.getDocPropsExtendedPart(), Namespaces.PROPERTIES_EXTENDED);
		
		
//		externalResources
		ret.getExternalResources().putAll(opcPackage.getExternalResources());
//		handled
		//isn't needed as it is already loaded
//		parts
		//is done in an another method
//		partStore
		ret.setSourcePartStore(opcPackage.getSourcePartStore());
				
		return ret;
	}

	protected static void deepCopyRelationships(OpcPackage opcPackage,
			Base sourcePart,
			Base targetPart,
			Set<String> relationshipTypes) throws Docx4JException {
		
		RelationshipsPart sourceRelationshipsPart = sourcePart.getRelationshipsPart(false);
		Relationships sourceRelationships = (sourceRelationshipsPart != null ? 
									   		 sourceRelationshipsPart.getRelationships() : 
									   null);
		List<Relationship> sourceRelationshipList = (sourceRelationships != null ? 
									   				 sourceRelationships.getRelationship() : 
													 null);
		
		RelationshipsPart targetRelationshipsPart = null;
		Relationships targetRelationships = null;
		
		Relationship sourceRelationship = null;
		Relationship targetRelationship = null;
		
		Part sourceChild = null;
		Part targetChild = null;
		
		if ((sourceRelationshipList != null) && 
			(!sourceRelationshipList.isEmpty())) {
			
			targetRelationshipsPart = targetPart.getRelationshipsPart(); //create if needed
			targetRelationships = targetRelationshipsPart.getRelationships();
			
			for (int i=0; i<sourceRelationshipList.size(); i++) {
				
				sourceRelationship = sourceRelationshipList.get(i);
				//the Relationship doesn't have any references to parts, therefore it can be reused
				targetRelationships.getRelationship().add(sourceRelationship);
				
				if (sourceRelationship.getTargetMode()==null
						// per ECMA 376 4ed Part 2, capitalisation should be thus: "External"
						// but we can relax this..
						|| !"external".equals(sourceRelationship.getTargetMode().toLowerCase())) {
					sourceChild = sourceRelationshipsPart.getPart(sourceRelationship);
					targetChild = deepCopyPart(opcPackage, targetPart, sourceChild, relationshipTypes);
					if (sourceChild != targetChild) {
						deepCopyRelationships(opcPackage, sourceChild, targetChild, relationshipTypes);
					}
				}
			}
		}
	}

	protected static Part deepCopyPart(OpcPackage opcPackage, Base targetParent, Part sourcePart, Set<String> relationshipTypes) throws Docx4JException {

		//check if already handled
		Part ret = opcPackage.getParts().get(sourcePart.getPartName());
		if (ret == null) {
			//
			ret = copyPart(sourcePart, 
						   opcPackage, ((relationshipTypes == null) || 
								        relationshipTypes.contains(sourcePart.getRelationshipType()))
						   );
			opcPackage.getParts().put(ret);
			targetParent.setPartShortcut(ret, ret.getRelationshipType());
		}
		return ret;
	}


	protected static Part copyPart(Part part, OpcPackage targetPackage, boolean deepCopy) throws Docx4JException {
	Part ret = null;
		String zz;
		zz = part.getPartName().getName();
//		System.out.println("=======>"+zz);
//		if(zz == "")
		
		try {
			ret = part.getClass().getConstructor(PartName.class).newInstance(part.getPartName());
		} catch (Exception e) {
			throw new Docx4JException("Error cloning part of class " + part.getClass().getName(), e);
		}
		ret.setRelationshipType(part.getRelationshipType());
		ret.setContentType(new ContentType(part.getContentType()));
		
		if (targetPackage != null) {
			ret.setPackage(targetPackage);
		}
		if (deepCopy) {
			deepCopyContent(part, ret);
		}
		else {
			shallowCopyContent(part, ret);
		}
		return ret;
	}


	protected static void deepCopyContent(Part source, Part destination) throws Docx4JException {
		if (source instanceof BinaryPart) {
			byte[] byteData = new byte[((BinaryPart)source).getBuffer().limit()]; // = remaining() when current pos = 0
			((BinaryPart)source).getBuffer().get(byteData);
			((BinaryPart)destination).setBinaryData(ByteBuffer.wrap(byteData));
		}
		else if (source instanceof JaxbXmlPart) {
			((JaxbXmlPart)destination).setJaxbElement(XmlUtils.deepCopy(((JaxbXmlPart)source).getJaxbElement(), 
							((JaxbXmlPart)source).getJAXBContext()));
			((JaxbXmlPart)destination).setJAXBContext(((JaxbXmlPart)source).getJAXBContext());
//			System.out.println("((JaxbXmlPart)source).getJAXBContext()).toString() =======>"+(((JaxbXmlPart)source).getXML()));
//			StringWriter sb = new StringWriter();
			
//			try {
//				 JAXBContext context = JAXBContext.newInstance("com.integra.xml");
//				Marshaller masrhaller = ((JaxbXmlPart)source).getJAXBContext().createMarshaller();
//				masrhaller.marshal(masrhaller, sb);
//				System.out.println(sb.toString());
//			} catch (JAXBException e) {
//				e.printStackTrace();
//			}
			if (log.isDebugEnabled()
					&& (source instanceof MainDocumentPart)) {
//				log.debug("source: " + ((JaxbXmlPart)source).getXML());
//				log.debug("destination: " + ((JaxbXmlPart)destination).getXML());
			}
			
			
		}
		else if (source instanceof CustomXmlDataStoragePart) {
			CustomXmlDataStorage dataStorage = ((CustomXmlDataStoragePart)source).getData().factory();
			dataStorage.setDocument(
					(Document)((CustomXmlDataStoragePart)source).getData().getDocument().cloneNode(true));
			((CustomXmlDataStoragePart)destination).setData(dataStorage);
		
		}
		else if (source instanceof XmlPart) {
			((XmlPart)destination).setDocument((Document)((XmlPart)source).getDocument().cloneNode(true));
//			Node node = ((XmlPart)source).getDocument().getDocumentElement();
//			try {
				// test the method
//				System.out.println(node2String(node));
//			System.out.println("=======>"+(((XmlPart)source).getDocument().getTextContent()));
//			System.out.println("=======>"+(((XmlPart)source).getDocument().getNodeValue()));
//			for (int y = 0; y < ((XmlPart)source).getDocument().getLength(); y++) {
//				Node info = ClinicInfo.item(y);
//				if (info.hasChildNodes()) {
//					Log.e(info.getNodeName(), info.getFirstChild().getNodeValue());
//				}
//			}
//			} catch (TransformerFactoryConfigurationError ex) {
//				java.util.logging.Logger.getLogger(clone.class.getName()).log(Level.SEVERE, null, ex);
//			} catch (TransformerException ex) {
//				java.util.logging.Logger.getLogger(clone.class.getName()).log(Level.SEVERE, null, ex);
//			}
			
		}
		else {
			throw new IllegalArgumentException("Dont know how to handle a part of type " + source.getClass().getName());
		}
	}

	protected static void shallowCopyContent(Part source, Part destination) throws Docx4JException {
		if (source instanceof BinaryPart) {
			((BinaryPart)destination).setBinaryData(((BinaryPart)source).getBuffer());
		}
		else if (source instanceof JaxbXmlPart) {
			((JaxbXmlPart)destination).setJaxbElement(((JaxbXmlPart)source).getJaxbElement());
			((JaxbXmlPart)destination).setJAXBContext(((JaxbXmlPart)source).getJAXBContext());
//			System.out.println("JaxbXmlPart =======>"+(((JaxbXmlPart)source).getXML()));
		}
		else if (source instanceof CustomXmlDataStoragePart) {
			((CustomXmlDataStoragePart)destination).setData(((CustomXmlDataStoragePart)source).getData());
			System.out.println("=======>"+(((CustomXmlDataStoragePart)source).getXML()));
		}
		else if (source instanceof XmlPart) {
			((XmlPart)destination).setDocument(((XmlPart)source).getDocument());
//			System.out.println("XmlPart =======>"+(((XmlPart)source).getDocument().getDocumentElement().getNodeName()));
//			System.out.println("XmlPart =======>"+(((XmlPart)source).getDocument().getDocumentElement().getNodeValue()));
		}
		else {
			throw new IllegalArgumentException("Dont know how to handle a part of type " + source.getClass().getName());
		}
	}

	private static String WordprocessingMLPackage(OpcPackage aPackage) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	
//	static void ShowText(JAXBContext source) throws Docx4JException,Exception{
////			 Unmarshaller u = source.createUnmarshaller();
//			StringWriter sb = new StringWriter();
////			Object map = ((JaxbXmlPart)source).getJaxbElement();
////			JAXBContext.newInstance(((Base)source).getClass()).createMarshaller().marshal(map, sb);
//			Marshaller masrhaller = source.createMarshaller();
//			masrhaller.marshal(masrhaller, sb);
//			System.out.println(sb.toString());
//	}
	
//	static String node2String(Node node) throws TransformerFactoryConfigurationError, TransformerException {
//			// you may prefer to use single instances of Transformer, and
//			// StringWriter rather than create each time. That would be up to your
//			// judgement and whether your app is single threaded etc
//			StreamResult xmlOutput = new StreamResult(new StringWriter());
//			Transformer transformer = TransformerFactory.newInstance().newTransformer();
//			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//			transformer.transform(new DOMSource(node), xmlOutput);
//			return xmlOutput.getWriter().toString();
//	}
	
//	static CTShape createShape(source){
//		Chart chartPart = new ObjectFactory(); 
//	}
	
	static void test(){
		
		org.docx4j.vml.ObjectFactory wmlObjectFactory = new org.docx4j.vml.ObjectFactory();
//
//		CTLine line = wmlObjectFactory.createCTLine();
//		 pictWrapped = wmlObjectFactory.createRPict(pict);
//		org.docx4j.vml.ObjectFactory vmlObjectFactory = new org.docx4j.vml.ObjectFactory();
//   
//		// Create object for shapetype (wrapped in JAXBElement)
//		CTShapetype shapetype = vmlObjectFactory.createCTShapetype();
//		JAXBElement<org.docx4j.vml.CTShapetype> shapetypeWrapped = vmlObjectFactory.createShapetype(shapetype);
//		pict.getAnyAndAny().add( shapetypeWrapped);
//		
//		// used to write jaxbElment XML to string
//			StringWriter writer = new StringWriter();
//
//			// create JAXBContext which will be used to update writer
////			JAXBContext context = JAXBContext.newInstance(StoreOperationInput.class);
//			JAXBElement<org.docx4j.vml.CTShape> shapeWrapped = objectFactory.createShape(shape);
//			
//			// marshall or convert jaxbElement containing student to xml format
//			context.createMarshaller().marshal(jaxbElement, writer);
//
//			// print XML string representation of Student object
//			System.out.println(writer.toString());
	}
}
