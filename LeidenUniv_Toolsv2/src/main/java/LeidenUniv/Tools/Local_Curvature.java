package LeidenUniv.Tools;

import ij.plugin.frame.RoiManager;

import java.awt.Polygon;
import java.awt.Scrollbar;
import java.awt.image.IndexColorModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.Opener;
import ij.plugin.*;
import ij.measure.*;

public class Local_Curvature implements PlugIn {
	//double [] radii;
	// #DONE Make curvature analysis for line roi, this will not have an in or outside.
	/* #DONE Make adjustable size menu
	 * #DONE Make results table with Rois, with min Curvature, Max curvature, Stdev curvature, mean curvature
	 * 
	 */
	
	public static void main(String[] args) {
		// this part is just for testing
		ImageJ ij = new ImageJ();
		ImagePlus imp = IJ.openImage("http://imagej.net/images/blobs.gif");
		imp.show();
		IJ.setAutoThreshold(imp, "Default");
		IJ.run(imp, "Convert to Mask", "");
		IJ.run(imp, "Analyze Particles...", "exclude add");
		// ends here*/
		RoiManager rm = new RoiManager();
		//rm.open("E:/Temp/curves/RoiSet.zip");
		Local_Curvature LC = new Local_Curvature();
		LC.run("15");
		
	}
	
	public double [] calculateLocal3PointCurvature(float []xs, float []ys, int dimension, FloatPolygon fp) {
		double [] curvatures = new double[xs.length] ;
		// if the number of points in the roi is smaller than the distance the curvature needs to be calculated on 0 is returned
		if (xs.length<=dimension) {
			for (int i=0;i<xs.length;i++) {
				curvatures[i]=0;
			}
			return curvatures;
		}

		for (int i=0;i<xs.length;i++) {
			// run through all points
			double [][] vals=get3PointValues(xs,ys,i,dimension); // gets the normal values
			curvatures[i]=getPointKappa(vals,xs,ys, fp);
		}
		return curvatures;
	}
	
	public double getPointKappa(double [][] vals, float []xs, float [] ys, FloatPolygon fp) {
		// this part is based on the beanshell, gives only positives values then all values are positive.
		double a, b, c, d, e, f, g;
		double px = 0;
		double py = 0;
		double pr = 0;
		double kappa;
		double [] centrad = new double [5];
		if ((vals[0][0]==vals[0][1] && vals[0][0]==vals[0][2]) || (vals[1][0]==vals[1][1] && vals[1][0]==vals[1][2])){ //colinear coordinates
			centrad[0]=0; //x
			centrad[1]=0; //y
			centrad[2]=-1; //radius
			kappa=Double.NaN;
			return kappa;
		}

		a = vals[0][1] - vals[0][0];
		b = vals[1][1] - vals[1][0];
		c = vals[0][2] - vals[0][0];
		d = vals[1][2] - vals[1][0];

		e = a*(vals[0][0] + vals[0][1]) + b*(vals[1][0] + vals[1][1]);
		f = c*(vals[0][0] + vals[0][2]) + d*(vals[1][0] + vals[1][2]);

		g = 2.0*(a*(vals[1][2] - vals[1][1])-b*(vals[0][2] - vals[0][1]));
		//  If g is 0 then the three points are colinear and no finite-radius
		//  circle through them exists. Return -1 for the radius. Somehow this does not
		// work as it should (representation of double number?), so it is trapped earlier..

		if (g==0.0){
			centrad[0]=0; //x
			centrad[1]=0; //y
			centrad[2]=Double.NaN; //radius
		}
		else { //return centre and radius of the circle
			centrad[0] = (d * e - b * f) / g;
			centrad[1] = (a * f - c * e) / g;
			centrad[2] = Math.sqrt(Math.pow((vals[0][0] - centrad[0]),2) + Math.pow((vals[1][0] - centrad[1]),2));
		}
		
		px = (px + centrad[0]) / 2;
		py = (py + centrad[1]) / 2;
		pr = (pr + centrad[2]) / 2;
		
		centrad[3]=(vals[0][0]+vals[0][2])/2;
		centrad[4]=(vals[1][0]+vals[1][2])/2;
		
		double sign=0;
		
		// Check if the middle between 1 point before, and one point after is within the ROI
		if (fp.contains(centrad[3], centrad[4])){
			sign=1;
		} else {
			sign=-1;
		}
		kappa =1/centrad[2]*sign;
		return kappa;
	}
	
	public double [][] get3PointValues(float []xs, float []ys, int i, int dimension){
		double[][] vals = new double[2][3];
		vals[0][0]=xs[(i-dimension+xs.length)%xs.length];
		vals[0][1]=xs[i];
		vals[0][2]=xs[(i+dimension)%xs.length];
		vals[1][0]=ys[(i-dimension+xs.length)%xs.length];
		vals[1][1]=ys[i];
		vals[1][2]=ys[(i+dimension)%xs.length];
		return vals;
	}
	
	public double [] summarizeCurvatures(double [] curvatures) {
		double [] summary = new double [4];
		double sum=0, avg=0, min=Double.MAX_VALUE, max=Double.MIN_VALUE, stdev=0;
		
		for (int i=0;i<curvatures.length;i++) {
			if (!Double.isNaN(curvatures[i])) {
				sum+=curvatures[i];
			}
			if (curvatures[i]<min) min=curvatures[i];
			if (curvatures[i]>max) max=curvatures[i];
		}
		avg=sum/curvatures.length;
		sum=0;
		for (int i=0;i<curvatures.length;i++) {
			if (!Double.isNaN(curvatures[i])) {
				sum+=(curvatures[i]-avg)*(curvatures[i]-avg);
			}
			
		}
		stdev=Math.sqrt(sum/curvatures.length);
		
		summary[0]=avg;
		summary[1]=min;
		summary[2]=max;
		summary[3]=stdev;
		return summary;
	}
	
	
	public Object [] calculateRoiManagerCurves(ImagePlus imp, RoiManager rm, int dimension) {
		ImagePlus imp3 = IJ.createImage("Curvatures", "32-bit black", imp.getWidth(), imp.getHeight(), imp.getNSlices());
		FloatProcessor fp3 =(FloatProcessor) imp3.getProcessor();
		ResultsTable rt = ResultsTable.getResultsTable();
		rt.reset();
		Roi [] rs;
		rs = rm.getRoisAsArray();
		Object[] retvals = new Object[2];
		double [][] ROICurveVals=new double [rs.length][4];
		// run through all rois, which is one in length if the roimanager did not contain anything
		for (int i=0;i<rs.length;i++){
			rt.incrementCounter();
			//IJ.log("Calculating roi " +(i+1)+" out of "+(rs.length));
			PolygonRoi r=(PolygonRoi)rs[i];
			// get the interpolated version for calculations
			FloatPolygon fp=r.getInterpolatedPolygon(1,true);
			// here we need to calculate the curvature
			double [] curves = calculateLocal3PointCurvature(fp.xpoints,fp.ypoints, dimension,fp);
			ROICurveVals[i]=summarizeCurvatures(curves);
			for (int j=0;j<fp.npoints;j++){
				int x = (int)fp.xpoints[j];
				int y = (int)fp.ypoints[j];
				if (fp3.getPixelValue(x,y)!=0){
					if (Math.abs(curves[j])>Math.abs(fp3.getPixelValue(x, y))) {
						fp3.putPixelValue(x,y,curves[j]);
					} 
				} else {
					if (!Double.isNaN(curves[j])) {
						fp3.putPixelValue(x,y,curves[j]);
					}
				}
			}
		}
		IJ.resetMinAndMax(imp3);
		IJ.run(imp3, "Red/Green", "");
		imp3.show();
		LUT lut=null;
		try {
			InputStream in = getClass().getResourceAsStream("/Curvatures.lut");
		    IndexColorModel icm = LutLoader.open(in);
		    lut = new LUT(icm, -0.5, 0.5);
		} catch (Exception e) {
				
		}
		imp3.setLut(lut);
		imp3.setDisplayRange(-0.50, 0.50);
		imp3.updateAndDraw();
		retvals[0]=imp3;
		retvals[1]=ROICurveVals;
		return retvals;
	}
	
	@Override
	public void run(String arg0) {
		int curveregion =5;
		
		ResultsTable rt = new ResultsTable();
		ImagePlus imp = IJ.getImage();
		Roi cRoi=imp.getRoi();
		RoiManager rm = RoiManager.getInstance2();
		
		if (arg0==null || arg0.equals("")) {
			// no arguments given, start up menu
			GenericDialog var2 = new GenericDialog("Distance to calculate curvature over");
			var2.addSlider("distance in pixels to calculate curvature ", 1, Math.max(imp.getWidth(),imp.getHeight()),5);
			var2.showDialog();
			if (var2.wasCanceled()) {
				IJ.log("User cancelled calculation");
				return;
			} else {
				@SuppressWarnings("unchecked")
				Vector<Scrollbar> v = var2.getSliders();
				Scrollbar s = v.firstElement();
				curveregion=s.getValue();
			}
		} else {
			// we read the parameters given
			try {
				curveregion = Integer.parseInt(arg0);
			} catch (NumberFormatException e) {
				IJ.log("Numerical parameter needed, continuing with default parameter 5");
			}
		}
		
		if (cRoi==null&&(rm==null||rm.getCount()==0)){
			IJ.showMessage("ROI needed");
		} else {
			// we create the output image for displaying the curvatures
			ImagePlus imp3 = IJ.createImage("Curvatures", "32-bit black", imp.getWidth(), imp.getHeight(), imp.getNSlices());
			// get the floatprocessor of the new image so we can put values
			FloatProcessor fp3 =(FloatProcessor) imp3.getProcessor();

			// create the Roi array to work with
			Roi [] rs;
			
			if (rm!=null && rm.getCount()>0) {
				rs = rm.getRoisAsArray();
			} else {
				rs = new Roi[1];
				rs[0]=cRoi;
			}
			double [][] ROICurveVals=new double [rs.length][4];
			// run through all rois, which is one in length if the roimanager did not contain anything
			for (int i=0;i<rs.length;i++){ // this is where the calculations are done
				rt.incrementCounter();
				// to see what roi type it is
				//PolygonRoi r=(PolygonRoi)rs[i];
				// get the interpolated version for calculations
				FloatPolygon fp=rs[i].getInterpolatedPolygon(1,true);
				// here we need to calculate the curvature
				double [] curves = calculateLocal3PointCurvature(fp.xpoints,fp.ypoints, curveregion,fp);
				
				// here we can calculate the averages
				ROICurveVals[i]=summarizeCurvatures(curves);
				rt.addValue("Roi #", (i+1));
				rt.addValue("Avg curvature", ROICurveVals[i][0]);
				rt.addValue("Min curvature", ROICurveVals[i][1]);
				rt.addValue("Max curvature", ROICurveVals[i][2]);
				rt.addValue("StDev curvature", ROICurveVals[i][3]);
				
				for (int j=0;j<fp.npoints;j++){
					int x = (int)fp.xpoints[j];
					int y = (int)fp.ypoints[j];
					if (fp3.getPixelValue(x,y)!=0){
						if (Math.abs(curves[j])>Math.abs(fp3.getPixelValue(x, y))) {
							fp3.putPixelValue(x,y,curves[j]);
						} 
					} else {
						if (!Double.isNaN(curves[j])) {
							fp3.putPixelValue(x,y,curves[j]);
						}
					}
				}
			}
			rt.show("Roi Curvature data");
			IJ.resetMinAndMax(imp3);
			IJ.run(imp3, "Red/Green", "");
			imp3.show();
			LUT lut=null;
			try {
				InputStream in = getClass().getResourceAsStream("/Curvatures.lut");
			    IndexColorModel icm = LutLoader.open(in);
			    lut = new LUT(icm, -0.2, 0.2);
			} catch (Exception e) {
					
			}
			imp3.setLut(lut);
			imp3.setDisplayRange(-0.50, 0.50);
			imp3.updateAndDraw();
		}
	}
	
}
