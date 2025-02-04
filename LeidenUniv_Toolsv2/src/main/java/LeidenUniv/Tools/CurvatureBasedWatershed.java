package LeidenUniv.Tools;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Scrollbar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class CurvatureBasedWatershed implements PlugIn {

	public static void main(String[] args) {
		// this part is just for testing
		ImageJ ij = new ImageJ();
		ImagePlus imp = IJ.openImage("E:/Temp/curves/ComparisonImage.tif");
		imp.show();
		RoiManager rm =RoiManager.getInstance2();
		if (rm==null) {
			rm = new RoiManager();
		} else {
			rm.reset();
		}
		//IJ.setAutoThreshold(imp, "Default");
		//IJ.run(imp, "Convert to Mask", "");
		//IJ.run(imp, "Analyze Particles...", "add");
		// ends here
		
		rm.runCommand("open", "E:/Temp/curves/RoiSet_comparison.zip");
		CurvatureBasedWatershed cbw = new CurvatureBasedWatershed();
		cbw.run("15");

	}
	
	@Override
	public void run(String arg0) {
		int curveregion =5;
		
		ResultsTable rt = new ResultsTable();
		ImagePlus imp = IJ.getImage();
		 if (imp != null) {
	            ImageProcessor ip = imp.getProcessor();
	            if (!ip.isBinary()) {
	                IJ.error("8-bit binary image (0 and 255) required.");
	                return;
	            }
	            ip.resetRoi();
	            //processing routines assume background=0; image may be otherwise
	        }
		Roi cRoi=imp.getRoi();
		RoiManager rm = RoiManager.getInstance2();
		Local_Curvature lc = new Local_Curvature();
		//lc.run("5");
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
			// run through all rois, which is one in length if the roimanager did not contain anything
			for (int i=0;i<rs.length;i++){ // this is where the calculations are done
				System.out.println("Roi "+(i+1));
				FloatPolygon fp=rs[i].getInterpolatedPolygon(1,true);
				// here we need to calculate the curvature
				double [] curves = lc.calculateLocal3PointCurvature(fp.xpoints,fp.ypoints, curveregion,fp);
				// now the need to find the minima, based on threshold
				ArrayList<Integer> mindexs=findMinima(curves,-0.02, curveregion);
				// now we need to get the corresponding points
				Point [] ps = getMinPoints(mindexs,fp);
				// from the minima we need to find the shortest distances between them, so create an arraylist and sort it on points
				int [] links =getMinDistanceIndex(ps);
				// Now we need to see if the connection from one to the other is also the minimum
				ArrayList<Line> lines = getSeperatorLines(ps,links);
				
				drawLines(imp,lines);
				imp.show();
				//imp3.show();
			}
		}
	}
	
	public void drawLines(ImagePlus imp, ArrayList<Line> lines) {
		//IJ.setForegroundColor(0, 0, 0);
		ImageProcessor ip = imp.getProcessor();
		for (int i=0;i<lines.size();i++) {
			Line l = lines.get(i);
			FloatPolygon fp = l.getInterpolatedPolygon(); 
			PolygonRoi pr = new PolygonRoi(fp,PolygonRoi.POLYLINE);
			Polygon p = pr.getPolygon();// returns roundway trip polygon, only need to run through half of the points
			// the pr2 now still has steps that go both in x and y. so we correct this
			fill1StepPolygonRoi(p, ip);
		}
	}
	
	public void fill1StepPolygonRoi(Polygon p, ImageProcessor ip) {
		int prevx=p.xpoints[0];
		int prevy=p.ypoints[0];
		ip.putPixelValue(p.xpoints[0], p.ypoints[0], 0);
		for (int i=1;i<p.npoints;i++) {
			if (Math.abs(p.xpoints[i]-prevx)==1&& Math.abs(p.ypoints[i]-prevy)==1) { //now we have difference in both
				ip.putPixelValue(p.xpoints[i], prevy, 0);
				ip.putPixelValue(p.xpoints[i], p.ypoints[i], 0);
				prevx=p.xpoints[i];
				prevy=p.ypoints[i];
			} else {
				ip.putPixelValue(p.xpoints[i], p.ypoints[i], 0);
				prevx=p.xpoints[i];
				prevy=p.ypoints[i];
			}
		}
	}
	
	public ArrayList<Line> getSeperatorLines (Point[] ps, int[] links) {
		ArrayList<Line> lines = new ArrayList<Line>();
		boolean [] mapped = new boolean[ps.length];
		for (int i=0;i<ps.length;i++) {
			mapped[i]=false;
		}
		for (int i=0;i<ps.length;i++) {
			if (!mapped[i]&&links[links[i]]==i&&ps.length>1){// need to check if it is aleady mapped
				lines.add(new Line(ps[i].x,ps[i].y,ps[links[i]].x,ps[links[i]].y));
				mapped[i]=true;
				mapped[links[i]]=true;// needed to prevent going back
			}
		}
		for (int i=0;i<ps.length;i++) {
			if(!mapped[i]&&ps.length>1) {
				lines.add(new Line(ps[i].x,ps[i].y,ps[links[i]].x,ps[links[i]].y));
			}
		}
		
		return lines;
	}
	
	public int [] getMinDistanceIndex(Point[] ps){
		int [] minDistIndex = new int[ps.length];
		// we need to see which points are closest
		for (int i=0;i<ps.length;i++) {
			double mindist=Double.MAX_VALUE;
			for (int j=0;j<ps.length;j++) {
				if (i!=j) {
					double dist=Math.sqrt(Math.pow(ps[i].x-ps[j].x,2)+Math.pow(ps[i].y-ps[j].y, 2));
					if (dist<mindist) {
						mindist=dist;
						minDistIndex[i]=j;
					}
				}
			}
		}
		System.out.println(Arrays.toString(minDistIndex));
		return minDistIndex;
	}
	
	public ArrayList<Integer> findMinima(double [] curves, double limit, int curveregion) {
		ArrayList<Integer> mindexes = new ArrayList<Integer>();
		for (int i=0;i<curves.length;i++) {
			// run through all neighbouring points and see if the are bigger. only then record it as a minimum
			boolean smallest=true;
			for (int j=-curveregion/2;j<curveregion/2;j++) {
				if (curves[(i+j+curves.length)%curves.length]<curves[i]) {
					smallest=false;
					break;
				}
			}
			if (smallest&&curves[i]<limit&&curves[i]!=-1) {
				mindexes.add(i);
			}
		}
		return mindexes;
	}
	
	public Point[] getMinPoints(ArrayList<Integer> mindexs, FloatPolygon fp) {
		Point[] ps = new Point[mindexs.size()];
		for (int i=0;i<mindexs.size();i++) {
			ps[i]=new Point((int)fp.xpoints[mindexs.get(i)], (int)fp.ypoints[mindexs.get(i)]);
		}
		return ps;
	}
}