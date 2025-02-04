package LeidenUniv.Tools;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

public class readXMLAttribute implements PlugIn{
	private String data;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ImageJ();
		readXMLAttribute rat = new readXMLAttribute();
		IJ.showMessage(rat.readXMLattribute("F:\\Joost\\projects\\image analysis\\image analysis bac\\Test_pourscope.xml", "Factor_0"));
	}

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		JFileChooser chooser = new JFileChooser();
		if (IJ.getDirectory("image")!=null) {
			chooser.setCurrentDirectory(new File(IJ.getDirectory("image")));
		}
		FileNameExtensionFilter filter = new FileNameExtensionFilter("xml files", "xml", "Xml", "XML");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(chooser);
	    
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       // get the right values
	       String resolution = readXMLattribute(chooser.getSelectedFile().getPath(), "XMetresPerPixel");
	       Double res = Double.parseDouble(resolution)*1000000;
	       IJ.showMessage(resolution);
	     } else {
	    	 IJ.showMessage("No file selected");
	     }
	}
	
	public String readXMLattribute (String file, String dataToRead){
		try {
	    	   SAXParserFactory spf = SAXParserFactory.newInstance();
	    	   SAXParser saxP = spf.newSAXParser();
	    	   DefaultHandler handler = new DefaultHandler() { // this is done for all elements in the xml
	    		   boolean res= false;
	    		   String Res="";
	    		   	//parser starts parsing a specific element inside the document
	    		   	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {  
	    			   if(qName.equalsIgnoreCase(dataToRead)){
	    				   //IJ.log("Reading resolution");
	    				   res=true;
	    			   }  
    			    }  
	    			//reads the text value of the currently parsed element  
	    			public void characters(char ch[], int start, int length) throws SAXException{  
	    				if (res){  
	    					setString(new String(ch, start, length));
	    					//IJ.log("Resolution : " +xRes);
	    					res=false;
	    				}  
    			   }
	    	   };
			   saxP.parse(file, handler);
		   } catch (Exception e){  
			   e.printStackTrace();  
		   }
			return this.data;
	}
	
	public void setString(String data) {
		this.data=data;
	}
}
