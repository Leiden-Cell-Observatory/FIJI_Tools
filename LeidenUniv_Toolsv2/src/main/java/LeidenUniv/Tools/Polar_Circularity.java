package LeidenUniv.Tools;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*;
import ij.measure.*;

/**
 * Plugin for measuring the polar circularity on a ROI
 * Based on Stojmenovic M, Jevremovic A, Nayak A (2013) 
 * Fast iris detection via shape based circularity. 
 * In: 2013 8th IEEE conference on industrial electronics and applications (ICIEA)
 * 19–21 June 2013, pp 747–752
 * @author willemsejj
 *
 */
public class Polar_Circularity implements PlugIn {

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
			IJ.showMessage("ROI needed");	
		} else if (!cRoi.isArea()){
			IJ.showMessage("Area ROI needed");	
		} else {
			rt.incrementCounter();
			rt.addValue("Polar Circularity", getPolarCircularity(cRoi, imp));
			rt.show("Results");
		}
	}

	static public double [] getRadii (Roi cRoi, ImagePlus imp){
		if (cRoi.getType()<5){
			 imp.setRoi(cRoi);
			 ImageStatistics is = imp.getStatistics(32);
			double XM=is.xCentroid;
			double YM=is.yCentroid;
			FloatPolygon fp = cRoi.getInterpolatedPolygon();
			float[] xs= fp.xpoints;
			float[] ys= fp.ypoints;
			// need to get center
			double[] Radii = new double[xs.length];
			for (int k=0;k<xs.length;k++){
				Radii[k]=Math.sqrt(Math.pow((xs[k]-XM),2)+Math.pow((ys[k]-YM),2));
			}
			return Radii;
		} else {
			IJ.showMessage("This type of ROI is not supported: type "+cRoi.getType());
			return null;
		}
	}
	
	/**
	 * Get the polar circularity of a roi, can also be called from a plugin
	 * @param cRoi ROI to work on
	 * @param imp Image to work on
	 * @return The Polar circularity of the ROI
	 */
	public double getPolarCircularity(Roi cRoi, ImagePlus imp){
		double standarddeviation=0;
		double av=0;
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
			 ImageStatistics is = imp.getStatistics(32);
			double XM=is.xCentroid;
			double YM=is.yCentroid;
			FloatPolygon fp = cRoi.getInterpolatedPolygon();
			float[] xs= fp.xpoints;
			float[] ys= fp.ypoints;
			// need to get center
			double[] Radii = new double[xs.length];
			double totaldist=0;
			for (int k=0;k<xs.length;k++){
				Radii[k]=Math.sqrt(Math.pow((xs[k]-XM),2)+Math.pow((ys[k]-YM),2));
				totaldist+=Radii[k];
			}
			// these radii array we can use for the local curvature!
			av=totaldist/xs.length;
			double standarddevationsum=0;
			for (int k=0;k<xs.length;k++){
				standarddevationsum+=Math.pow((Radii[k]-av),2);
			}
			standarddeviation=Math.sqrt(standarddevationsum/xs.length);
			return standarddeviation/av;
		} else {
			IJ.showMessage("This type of ROI is not supported: type "+cRoi.getType());
			return standarddeviation/av;
		}
	}

}
