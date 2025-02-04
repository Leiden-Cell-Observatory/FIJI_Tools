package LeidenUniv.Tools;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.ZProjector;
import ij.process.ImageStatistics;

public class ExtracellularBacterialBurden2D implements PlugIn{
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
					sum.addValue("Red area", rt.getValue("Red area", 0));
					sum.addValue("Red mean", rt.getValue("Red mean", 0));
					sum.addValue("Red IntDen", rt.getValue("Red IntDen", 0));
					sum.addValue("Green area", rt.getValue("Green area", 0));
					sum.addValue("Green mean", rt.getValue("Green mean", 0));
					sum.addValue("Green IntDen", rt.getValue("Green IntDen", 0));
					sum.addValue("Coloc Red area", rt.getValue("Coloc Red area", 0));
					sum.addValue("Coloc Red mean", rt.getValue("Coloc Red mean", 0));
					sum.addValue("Coloc Red IntDen", rt.getValue("Coloc Red IntDen", 0));
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
		ImagePlus gs = ZProjector.run(green,"sum");
		ImagePlus gm = ZProjector.run(green,"max");
		ImagePlus rs = ZProjector.run(red,"sum");
		ImagePlus rm = ZProjector.run(red,"max");
		IJ.setAutoThreshold(gm, "MaxEntropy dark");
		IJ.run(gm, "Convert to Mask", "");
		IJ.setAutoThreshold(rm, "MaxEntropy dark");
		IJ.run(rm, "Convert to Mask", "");
		IJ.run(rm, "Create Selection", "");
		Roi rRoi =rm.getRoi();
		IJ.run(gm, "Create Selection", "");
		Roi gRoi =gm.getRoi();
		gs.setRoi(gRoi);
		rs.setRoi(rRoi);
		ImageStatistics gis = gs.getStatistics();
		ImageStatistics ris = rs.getStatistics();
		ImageCalculator ic = new ImageCalculator();
		ImagePlus both = ic.run("AND create stack", gm, rm);
		IJ.run(both, "Create Selection", "");
		Roi bRoi =both.getRoi();
		rs.setRoi(bRoi);
		ImageStatistics bis = rs.getStatistics();
		ResultsTable rt = new ResultsTable();
		rt.incrementCounter();
		rt.addValue("Red area", ris.area);
		rt.addValue("Red mean", ris.mean);
		rt.addValue("Red IntDen", ris.mean*ris.area);
		rt.addValue("Green area", gis.area);
		rt.addValue("Green mean", gis.mean);
		rt.addValue("Green IntDen", gis.mean*gis.area);
		rt.addValue("Coloc Red area", bis.area);
		rt.addValue("Coloc Red mean", bis.mean);
		rt.addValue("Coloc Red IntDen", bis.mean*bis.area);
		return rt;
	}
}
