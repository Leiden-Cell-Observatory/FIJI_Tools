package LeidenUniv.Tools;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
public class readScaleMZ16 implements PlugIn {
	private double resolution;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ImageJ();
		ImagePlus imp =IJ.openImage("F:/Joost/projects/image analysis/image analyse Plant/redouan/Auxin 5 hours CaM4.tif");
		imp.show();
		readScaleMZ16 reader = new readScaleMZ16();
		reader.run("");
	}

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus imp = IJ.getImage();
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(IJ.getDirectory("image")));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("xml files", "xml", "Xml", "XML");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(chooser);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("You chose to open this file: " + chooser.getSelectedFile().getPath());
	       // get the right values
	       readPixelSize(chooser.getSelectedFile().getPath());
	       resolution = resolution*1000000;
	       IJ.run(imp, "Set Scale...", "distance=1 known="+resolution+" unit=micron");
	     } else {
	    	 IJ.showMessage("No file selected");
	     }
	}
	
	public void readPixelSize (String file){
		try {
	    	   SAXParserFactory spf = SAXParserFactory.newInstance();
	    	   SAXParser saxP = spf.newSAXParser();
	    	   DefaultHandler handler = new DefaultHandler() { // this is done for all elements in the xml
	    		   boolean res= false;
	    		   	//parser starts parsing a specific element inside the document
	    		   	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {  
	    			   if(qName.equalsIgnoreCase("XMetresPerPixel")){
	    				   //IJ.log("Reading resolution");
	    				   res=true;
	    			   }  
    			    }  
	    			//reads the text value of the currently parsed element  
	    			public void characters(char ch[], int start, int length) throws SAXException{  
	    				if (res){  
	    					String xRes = new String(ch, start, length);
	    					//IJ.log("Resolution : " +xRes);
	    					setRes(xRes);
	    					res=false;
	    				}  
    			   }  
	    	   };
			   saxP.parse(file, handler);  
		   } catch (Exception e){  
			   e.printStackTrace();  
		   }  
	}
	
	public void setRes(String s) {
		resolution =Double.parseDouble(s);
	}
}

