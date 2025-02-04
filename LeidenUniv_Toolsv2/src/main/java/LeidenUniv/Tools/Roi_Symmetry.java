package LeidenUniv.Tools;
import java.awt.Rectangle;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*;
import ij.plugin.frame.RoiManager;
import ij.measure.*;

/**
 * Plugin for measuring the symmetry of a ROI
 * Based on Willemse et al 2020 
 * Mathematical Mirroring 
 * @author willemsejj
 *
 */
public class Roi_Symmetry implements PlugIn {

	/**
	 * For running the plugin in ImageJ/FIJI
	 */
	public void run(String arg) {
		ResultsTable rt = ResultsTable.getResultsTable();
		rt.reset();
		//IJ.log("\\Clear");
		ImagePlus imp = IJ.getImage();
		Roi cRoi=imp.getRoi();
		if (cRoi==null){
			RoiManager rm = RoiManager.getInstance2();
			if (rm==null) {
				IJ.showMessage("ROI needed");
			} else {
				if (rm.getCount()==0) {
					IJ.showMessage("ROI needed");
				} else {
					Roi [] rois = rm.getRoisAsArray();
					for (int i=0;i<rois.length;i++) {
						rt.incrementCounter();
						cRoi=rois[i];
						double trbl=getRoiTRBLSymmetry(cRoi, imp);
						double tlbr =getRoiTLBRSymmetry(cRoi, imp);
						double lr =getRoiLRSymmetry(cRoi, imp);
						double tb =getRoiTBSymmetry(cRoi, imp);
						rt.addValue("TRBL Symmetry", trbl);
						rt.addValue("TLBR Symmetry", tlbr);
						rt.addValue("LR Symmetry", lr);
						rt.addValue("TB Symmetry", tb);
						rt.addValue("Average Symmetry", (trbl+tlbr+lr+tb)/4);
					}
					rt.show("Results");
				}
			}
		} else if (!cRoi.isArea()){
			IJ.showMessage("Area ROI needed");	
		} else {
			rt.incrementCounter();
			double trbl=getRoiTRBLSymmetry(cRoi, imp);
			double tlbr =getRoiTLBRSymmetry(cRoi, imp);
			double lr =getRoiLRSymmetry(cRoi, imp);
			double tb =getRoiTBSymmetry(cRoi, imp);
			rt.addValue("TRBL Symmetry", trbl);
			rt.addValue("TLBR Symmetry", tlbr);
			rt.addValue("LR Symmetry", lr);
			rt.addValue("TB Symmetry", tb);
			rt.addValue("Average Symmetry", (trbl+tlbr+lr+tb)/4);
			rt.show("Results");
		}
	}

	/**
	 * Get the polar circularity of a roi, can also be called from a plugin
	 * @param cRoi ROI to work on
	 * @param imp Image to work on
	 * @return The TLBR symmetryof the ROI
	 */
	public double getRoiTRBLSymmetry(Roi cRoi, ImagePlus imp){
		if (cRoi.getType()<5){
			/*
			* rect = 0
			* rounded rect =0
			* oval =1
			* ellipse =3
			* changed by selection brush becomes type 9
			* polygon =2
			* freehand =3
			* magic wand =4
			*/
			imp.setRoi(cRoi);
			ImageProcessor ip = imp.getProcessor();
			int bitMax=(int)Math.pow(2,imp.getBitDepth());
			// here we need to do the symmetry calculation
			// so we need to get the bounding rectangle and run through all those pixels
			// running through half of the bounding box, we need to check multiple things
			// are the rectangle pixel and the mirror pixel in the roi?
			// if so then we need to calculate, else we need to add the max difference value
			Rectangle rct = cRoi.getBounds();
			if (rct.width==1 || rct.height==1){ // for only one pixel symmetry is always 1
				return 1;
			} else { //multiple rows, need to be checked, rct.width wide, depends on even/uneven
				double diff=0;
				double sum=0;
				
				for (int j=0;j<rct.width-1;j++){	
					for (int i=0;i<rct.height-1-j;i++){
						if(cRoi.contains(rct.x+rct.width-1-j,rct.y+i)&&cRoi.contains(rct.x+i,rct.y+rct.height-1-j)) {
							diff+=Math.abs(ip.getPixel(rct.x+rct.width-1-j,rct.y+i)-ip.getPixel(rct.x+i,rct.y+rct.height-1-j));
							sum+=ip.getPixel(rct.x+rct.width-1-j, rct.y+i)+ip.getPixel(rct.x+i,rct.y+rct.height-1-j);
						} else if (!cRoi.contains(rct.x+rct.width-1-j,rct.y+i)&&!cRoi.contains(rct.x+i,rct.y+rct.height-1-j)){
							// do nothing since they are both not in the roi
						} else {
							diff+=bitMax;
							sum+=bitMax;
						}
					}
				}
				//IJ.log("TRBL "+(1-(diff/sum)));
				return (1-(diff/sum));
			}
				
		} else {
			IJ.showMessage("This type of ROI is not supported: type "+cRoi.getType());
			return 0;
		}
	}
	
	/**
	 * Get the polar circularity of a roi, can also be called from a plugin
	 * @param cRoi ROI to work on
	 * @param imp Image to work on
	 * @return The TLBR symmetryof the ROI
	 */
	public double getRoiTLBRSymmetry(Roi cRoi, ImagePlus imp){
		if (cRoi.getType()<5){
			/*
			* rect = 0
			* rounded rect =0
			* oval =1
			* ellipse =3
			* changed by selection brush becomes type 9
			* polygon =2
			* freehand =3
			* magic wand =4
			*/
			imp.setRoi(cRoi);
			ImageProcessor ip = imp.getProcessor();
			int bitMax=(int)Math.pow(2,imp.getBitDepth());
			// here we need to do the symmetry calculation
			// so we need to get the bounding rectangle and run through all those pixels
			// running through half of the bounding box, we need to check multiple things
			// are the rectangle pixel and the mirror pixel in the roi?
			// if so then we need to calculate, else we need to add the max difference value
			Rectangle rct = cRoi.getBounds();
			if (rct.width==1 || rct.height==1){ // for only one pixel symmetry is always 1
				return 1;
			} else { //multiple rows, need to be checked, rct.width wide, depends on even/uneven
				double diff=0;
				double sum=0;
				
				for (int j=0;j<rct.width-1;j++){	
					for (int i=0;i<rct.height-1-j;i++){
						if(cRoi.contains(rct.x+rct.width-1-j,rct.y+rct.height-1-i)&&cRoi.contains(rct.x+i,rct.y+j)) {
							diff+=Math.abs(ip.getPixel(rct.x+rct.width-1-j,rct.y+rct.height-1-i)-ip.getPixel(rct.x+i,rct.y+j));
							sum+=ip.getPixel(rct.x+rct.width-1-j,rct.y+rct.height-1-i)+ip.getPixel(rct.x+i,rct.y+j);
						} else if (!cRoi.contains(rct.x+rct.width-1-j,rct.y+rct.height-1-i)&&!cRoi.contains(rct.x+i,rct.y+j)){
							// do nothing since they are both not in the roi
						} else {
							diff+=bitMax;
							sum+=bitMax;
						}
					}
				}
				//IJ.log("TLBR "+(1-(diff/sum)));
				return (1-(diff/sum));
			}
				
		} else {
			IJ.showMessage("This type of ROI is not supported: type "+cRoi.getType());
			return 0;
		}
	}
	
	/**
	 * @param cRoi the Roi to calculate on
	 * @param imp ImagePlus to calculte this on
	 * @return the new double value that goes into the overlay image
	 */
	public double getRoiLRSymmetry(Roi cRoi, ImagePlus imp){ // Imageprocessor needs to be added for general functionality
		if (cRoi.getType()<5){
			/*
			* rect = 0
			* rounded rect =0
			* oval =1
			* ellipse =3
			* changed by selection brush becomes type 9
			* polygon =2
			* freehand =3
			* magic wand =4
			*/
			imp.setRoi(cRoi);
			ImageProcessor ip = imp.getProcessor();
			int bitMax=(int)Math.pow(2,imp.getBitDepth());
			// here we need to do the symmetry calculation
			// so we need to get the bounding rectangle and run through all those pixels
			// running through half of the bounding box, we need to check multiple things
			// are the rectangle pixel and the mirror pixel in the roi?
			// if so then we need to calculate, else we need to add the max difference value
			Rectangle rct = cRoi.getBounds();
			if (rct.width==1 && rct.height==1){ // for only one pixel srct.ymetry is always 1
				return 1;
			} else if (rct.width==1){ // one pixel wide always 1
				return 1;
			} else if (rct.height==1){ // one pixel high, rct.width wide, depends on even/uneven by using the fact that odd int/2 are always floored we can use 1 formula
				double diff=0;
				double sum=0;
				for (int j=0;j<rct.width/2;j++){	
					if(cRoi.contains(rct.x+rct.width-1-j,rct.y)&&cRoi.contains(rct.x+j,rct.y)) {
						diff+=Math.abs(ip.getPixel(rct.x+rct.width-1-j,rct.y)-ip.getPixel(rct.x+j,rct.y));
						sum+=ip.getPixel(rct.x+j,rct.y)+ip.getPixel(rct.x+rct.width-1-j,rct.y);
					} else if (!cRoi.contains(rct.x+j,rct.y)&&!cRoi.contains(rct.x+rct.width-1-j,rct.y)){
						// do nothing since they are both not in the roi
					} else {
						diff+=bitMax;
						sum+=bitMax;
					}
				}
				return (1-(diff/sum));
			} else { //multiple rows, need to be checked, rct.width wide, depends on even/uneven
				double diff=0;
				double sum=0;
				for (int j=0;j<rct.width;j++){	
					for (int i=0;i<rct.height/2;i++){
						if(cRoi.contains(rct.x+rct.width-1-j,rct.y+i)&&cRoi.contains(rct.x+j,rct.y+i)) {
							diff+=Math.abs(ip.getPixel(rct.x+rct.width-1-j,rct.y+i)-ip.getPixel(rct.x+j,rct.y+i));
							sum+=ip.getPixel(rct.x+j,rct.y+i)+ip.getPixel(rct.x+rct.width-1-j,rct.y+i);
						} else if (!cRoi.contains(rct.x+j,rct.y+i)&&!cRoi.contains(rct.x+rct.width-1-j,rct.y+i)){
							// do nothing since they are both not in the roi
						} else {
							diff+=bitMax;
							sum+=bitMax;
						}
					}
				}
				//IJ.log("LR "+(1-(diff/sum)));
				return (1-(diff/sum));
			}
		}else {
			IJ.showMessage("This type of ROI is not supported: type "+cRoi.getType());
			return 0;
		}
	}

	
	/**
	 * @param cRoi the Roi to calculate on
	 * @param imp ImagePlus to calculte this on
	 * @return the new double value that goes into the overlay image
	 */
	public double getRoiTBSymmetry(Roi cRoi, ImagePlus imp){ // Imageprocessor needs to be added for general functionality
		if (cRoi.getType()<5){
			/*
			* rect = 0
			* rounded rect =0
			* oval =1
			* ellipse =3
			* changed by selection brush becomes type 9
			* polygon =2
			* freehand =3
			* magic wand =4
			*/
			imp.setRoi(cRoi);
			ImageProcessor ip = imp.getProcessor();
			int bitMax=(int)Math.pow(2,imp.getBitDepth());
			// here we need to do the symmetry calculation
			// so we need to get the bounding rectangle and run through all those pixels
			// running through half of the bounding box, we need to check multiple things
			// are the rectangle pixel and the mirror pixel in the roi?
			// if so then we need to calculate, else we need to add the max difference value
			Rectangle rct = cRoi.getBounds();
			if (rct.width==1 && rct.height==1){ // for only one pixel srct.ymetry is always 1
				return 1;
			} else if (rct.height==1){ // one pixel wide always 1
				return 1;
			} else if (rct.width==1){ // one pixel high, rct.width wide, depends on even/uneven by using the fact that odd int/2 are always floored we can use 1 formula
				double diff=0;
				double sum=0;
				for (int j=0;j<rct.height/2;j++){	
					if(cRoi.contains(rct.x,rct.y+j)&&cRoi.contains(rct.x,rct.y+rct.height-1-j)) {
						diff+=Math.abs(ip.getPixel(rct.x,rct.y+j)-ip.getPixel(rct.x,rct.y+rct.height-1-j));
						sum+=ip.getPixel(rct.x,rct.y+j)+ip.getPixel(rct.x,rct.y+rct.height-1-j);
					} else if (!cRoi.contains(rct.x,rct.y+j)&&!cRoi.contains(rct.x,rct.y+rct.height-1-j)){
						// do nothing since they are both not in the roi
					} else {
						diff+=bitMax;
						sum+=bitMax;
					}
				}
				return (1-(diff/sum));
			} else { //multiple rows, need to be checked, rct.width wide, depends on even/uneven
				double diff=0;
				double sum=0;
				for (int j=0;j<rct.height;j++){	
					for (int i=0;i<rct.width/2;i++){
						if(cRoi.contains(rct.x+i,rct.y+j)&&cRoi.contains(rct.x+j,rct.y+rct.height-1-i)) {
							diff+=Math.abs(ip.getPixel(rct.x+i,rct.y+j)-ip.getPixel(rct.x+j,rct.y+rct.height-1-i));
							sum+=ip.getPixel(rct.x+i,rct.y+j)+ip.getPixel(rct.x+j,rct.y+rct.height-1-i);
						} else if (!cRoi.contains(rct.x+i,rct.y+j)&&!cRoi.contains(rct.x+j,rct.y+rct.height-1-i)){
							// do nothing since they are both not in the roi
						} else {
							diff+=bitMax;
							sum+=bitMax;
						}
					}
				}
				//IJ.log("TB "+(1-(diff/sum)));
				return (1-(diff/sum));
			}
		}else {
			IJ.showMessage("This type of ROI is not supported: type "+cRoi.getType());
			return 0;
		}
	}
    /**
     * Purely for testing purposes
     * @param args are not used
     */
    public static void main(String[] args) {
    	ImagePlus imp = IJ.openImage("http://imagej.nih.gov/ij/images/blobs.gif");
    	imp.show();
    	int[] xpoints = {90,119,135,112,79,76,100,109};
    	int[] ypoints = {172,198,157,146,149,161,171,163};
    	imp.setRoi(new PolygonRoi(xpoints,ypoints,8,Roi.POLYGON));
    	Roi_Symmetry rs = new Roi_Symmetry();
    	rs.run("");
    	imp.setRoi(new OvalRoi(173,174,14,17));
    	rs.run("");
    	imp.setRoi(new OvalRoi(115,124,13,10));
    	rs.run("");
    }

}
