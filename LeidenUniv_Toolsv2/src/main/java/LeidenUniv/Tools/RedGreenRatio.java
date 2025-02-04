package LeidenUniv.Tools;

import java.awt.Rectangle;
import java.io.File;
import ij.io.*;
import ij.measure.ResultsTable;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.ChannelSplitter;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageStatistics;

public class RedGreenRatio implements PlugIn{
	private String fe;
	private Double bck, max, objSize;
	
	public static void main(String[] args) {

			RedGreenRatio rgr = new RedGreenRatio();
			rgr.run("");
	}

	@Override
	public void run(String arg0) {
     		//select folder
			DirectoryChooser DC = new DirectoryChooser("Select the folders you want to analyze");
    		String tDir=DC.getDirectory();
    		
    		File tempFol= new File(tDir); // get list of mosaics
			File[] LOF=tempFol.listFiles();
			
			GenericDialog gd = new GenericDialog("Plugin settings");
			gd.addStringField("File extension","jpg");
			gd.addNumericField("Background value (8-bit)",12,0);
			gd.addNumericField("Max intensity value (8-bit)",150,0);
			gd.addNumericField("Minimum object size",20000,0);
			gd.showDialog();
			Prefs.blackBackground = true;

			fe = gd.getNextString();
			bck = gd.getNextNumber();
			max = gd.getNextNumber();
			objSize = gd.getNextNumber();
			ResultsTable rt= MeasureData (LOF);
			rt.show("Data");
			rt.save(tempFol+"//GreenRedResults.csv");
	}
	
	public ResultsTable MeasureData (File [] LOF) {
		ResultsTable rt = new ResultsTable();
		for (int j=0;j<LOF.length;j++) {
			File cFile =LOF[j];
			String tFileName=cFile.toString();
			if ((tFileName.toLowerCase()).endsWith("."+fe)){
				RoiManager rm = RoiManager.getInstance();
				if (rm!=null) {
					rm.close();
					rm = new RoiManager();
				} else {
					rm = new RoiManager();
				}
				
				rt.incrementCounter();
				ImagePlus Original = IJ.openImage(tFileName);
				String title = Original.getTitle();
				ImagePlus [] imps = ChannelSplitter.split(Original);
				IJ.setRawThreshold(imps[2], bck, max,"");
				IJ.run(imps[2], "Convert to Mask", "");
				IJ.run(imps[2], "Fill Holes", "");
				IJ.run(imps[2], "Analyze Particles...", "size="+ objSize+"-Infinity add");
				
				
				
				//RoiManager rm = RoiManager.getInstance();
				Roi [] rois =rm.getRoisAsArray();
				for (int i=0;i<rm.getCount();i++) {
					rt.addValue("Image name", title);
					rt.addValue("Roi",""+(i+1));
					imps[0].setRoi(rois[i]);
					ImageStatistics is0 = imps[0].getStatistics(ImageStatistics.MEAN+ImageStatistics.AREA+ImageStatistics.MIN_MAX+ImageStatistics.MEDIAN);
					imps[1].setRoi(rois[i]);
					ImageStatistics is1 = imps[1].getStatistics(ImageStatistics.MEAN+ImageStatistics.MIN_MAX+ImageStatistics.MEDIAN);
					rt.addValue("Area", is0.area);
					rt.addValue("Red_mean", is0.mean);
					rt.addValue("Red_min", is0.min);
					rt.addValue("Red_max", is0.max);
					rt.addValue("Red_median", is0.median);
					rt.addValue("Green_mean", is1.mean);
					rt.addValue("Green_min", is1.min);
					rt.addValue("Green_max", is1.max);
					rt.addValue("Green_median", is1.median);
					rt.addValue("Green_red_ratio", is1.mean/is0.mean);
					rt.addValue("Red_Green_ratio", is0.mean/is1.mean);
					Rectangle r = rois[i].getBounds();
					rt.addValue("Top left x", r.x);
					rt.addValue("Top left y", r.y);
					rt.addValue("Bottom right x", r.x+r.width);
					rt.addValue("Bottom right y", r.y+r.height);
					if (i<rm.getCount()-1) {
						rt.incrementCounter();
					}
				}
			}
		}
		return rt;
	}
}
