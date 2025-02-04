package LeidenUniv.Tools;

import java.awt.Point;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.PlugIn;
import ij.plugin.ZProjector;
import ij.plugin.frame.RoiManager;
import ij.process.ImageStatistics;

public class PI_Hoechst_Measurements implements PlugIn{

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus imp = IJ.getImage();
		GenericDialog gd = new GenericDialog("Settings");
    	gd.addNumericField("Region size", 5.0, 0);
    	gd.addNumericField("Prominence for maxima finding", 10.0, 0);
    	gd.addNumericField("Channel number of Hoechst channel", 1, 0);
    	gd.addNumericField("Channel number of PI channel", 2, 0);
    	gd.addNumericField("Cut off 1st channel", 45, 0);
    	gd.addNumericField("Cut off last channel", 33, 0);
    	gd.showDialog();
    	
    	int rsize = (int)gd.getNextNumber();
    	int prom = (int)gd.getNextNumber();
    	int hch= (int)gd.getNextNumber();
    	int pch= (int)gd.getNextNumber();
    	int hLimit = (int)gd.getNextNumber();
    	int pLimit = (int)gd.getNextNumber();
		ResultsTable [] rts = getOmeroData(imp,hch,pch,rsize,prom,hLimit,pLimit);
		rts[0].show("Measurments");
		rts[1].show("% Cell Death");
	}
	
	public static ResultsTable[] getOmeroData(ImagePlus imp, int hch, int pich, int roiSize, int prom, int PILimit, int HLimit) {
		IJ.run(imp, "Set Scale...", "distance=0 known=0 unit=pixel");
		ImagePlus[] imps = ChannelSplitter.split(imp);
		IJ.run("Set Measurements...", "mean redirect=None decimal=3");
		RoiManager rm = RoiManager.getInstance();
		if (rm==null){
			rm = new RoiManager();
		} else {
			rm.close();
			rm= new RoiManager();
		}
		ImagePlus H1 = imps[hch-1].duplicate();
		
		ImagePlus P1 = imps[pich-1].duplicate();
		if (H1.getDimensions()[3]>1||imp.getDimensions()[4]>1) {
			H1= ZProjector.run(H1,"max");
			P1= ZProjector.run(P1,"max");
		} 
		ImagePlus H2 = H1.duplicate();
		IJ.run(H1, "Gaussian Blur...", "sigma=1");
		IJ.run(H1, "Subtract Background...", "rolling=8");
		IJ.run(H1, "8-bit", "");
		IJ.run(H1, "Find Maxima...", "prominence="+prom+" output=[Point Selection]");
		IJ.run(P1, "8-bit", "");
		IJ.run(H2, "8-bit", "");
		Point[] pts=null;
		try { 
			Roi cRoi = H1.getRoi();
			pts = cRoi.getContainedPoints();
		} catch (NullPointerException e){
			IJ.log("no rois found");
		}
		ResultsTable rt2 = new ResultsTable();
		double pCounter=0;
		double hCounter=0;
		for (int i =0;i<pts.length;i++){
			rt2.incrementCounter();
			Roi tRoi = new OvalRoi(pts[i].x-((double)roiSize/2),pts[i].y-((double)roiSize/2),roiSize,roiSize);
			H2.setRoi(tRoi);
			rm.add(tRoi, 1);
			ImageStatistics is =tRoi.getStatistics(); 
			rt2.addValue("X", pts[i].x);
			rt2.addValue("Y", pts[i].y);
			rt2.addValue("Hoechst", is.mean);
			if (is.mean>HLimit) {
				hCounter++;
			}
			P1.setRoi(tRoi);
			ImageStatistics is2 =P1.getStatistics(); 
			rt2.addValue("PI", is2.mean);
			if (is2.mean>PILimit) {
				pCounter++;
			}
			rt2.addValue("Ratio",is2.mean/is.mean);
		}
		ResultsTable rt3= new ResultsTable();
		rt3.incrementCounter();
		rt3.addValue("Image Name", imp.getTitle());
		rt3.addValue("% Cell Death", pCounter/hCounter);
		ResultsTable[] rts = new ResultsTable[2];
		rts[0]=rt2;
		rts[1]=rt3;
		//rts[0].show(imp.getTitle());
		return rts;
	}

}
