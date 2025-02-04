package LeidenUniv.Tools;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.ZProjector;
import ij.process.ImageStatistics;

public class ExtracellularBacterialBurden implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		if (WindowManager.getImageCount()>0) {
			
			if (WindowManager.getImageCount()==1){
				ImagePlus imp = IJ.getImage(); //gets image
				if (imp!=null) {
					ResultsTable rt = Analyse(imp); // starts analysis
					rt.show("Summary");
				}
			} else {
				int [] ims = WindowManager.getIDList();
				ResultsTable sum= new ResultsTable();
				for (int i=0;i<ims.length;i++) {
					sum.incrementCounter();
					ImagePlus imp = WindowManager.getImage(ims[i]);
					ResultsTable rt = Analyse(imp); // starts analysis"
					sum.addValue("Image name", imp.getTitle());
					sum.addValue("Red pixels", rt.getValue("Red pixels", 0));
					sum.addValue("Green pixels", rt.getValue("Green pixels", 0));
					sum.addValue("Co-localizing pixels", rt.getValue("Co-localizing pixels", 0));
					sum.addValue("Actual bacterial burden", rt.getValue("Actual bacterial burden", 0));
					imp.hide();
					imp.close();
				}
				sum.show("Combined data");
			}
		} else {
			IJ.showMessage("Please open image(s) to analyse");
		}
	}
	
	public ResultsTable Analyse(ImagePlus imp) {
		int[] dim = imp.getDimensions();
		IJ.run(imp, "Set Scale...", "distance=0 known=0 unit=pixel");
		ImagePlus green = new Duplicator().run(imp, 1, 1, 1, dim[3], 1, 1);
		ImagePlus red = new Duplicator().run(imp, 2, 2, 1, dim[3], 1, 1);
		IJ.run(green, "Gaussian Blur...", "sigma=1 stack");
		IJ.run(red, "Gaussian Blur...", "sigma=1 stack");
		green.setSlice(dim[3]/2);
		red.setSlice(dim[3]/2);
		IJ.setAutoThreshold(green, "MaxEntropy dark");
		IJ.run(green, "Convert to Mask", "method=MaxEntropy background=Dark black");
		IJ.setAutoThreshold(red, "MaxEntropy dark");
		IJ.run(red, "Convert to Mask", "method=MaxEntropy background=Dark black");
		ImageCalculator ic = new ImageCalculator();
		ImagePlus both = ic.run("AND create stack", green, red);
		ImagePlus both2 = ZProjector.run(both,"sum both");
		ImagePlus green2 = ZProjector.run(green,"sum green");
		ImagePlus red2 = ZProjector.run(red,"sum red");
		ImageStatistics isb = both2.getStatistics(Measurements.AREA+Measurements.MEAN);
		ImageStatistics isg = green2.getStatistics(Measurements.AREA+Measurements.MEAN);
		ImageStatistics isr = red2.getStatistics(Measurements.AREA+Measurements.MEAN);
		double bval = isb.mean*isb.area;
		double gval =isg.mean*isg.area;
		double rval =isr.mean*isr.area;
		ResultsTable rt = new ResultsTable();
		rt.incrementCounter();
		rt.addValue("Red pixels", rval/255);
		rt.addValue("Green pixels", gval/255);
		rt.addValue("Co-localizing pixels", bval/255);
		rt.addValue("Actual bacterial burden", (rval-bval)/255);
		return rt;
	}

}
