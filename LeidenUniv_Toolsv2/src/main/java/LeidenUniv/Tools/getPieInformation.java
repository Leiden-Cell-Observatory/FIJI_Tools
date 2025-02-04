package LeidenUniv.Tools;

import java.awt.Point;
import java.util.Arrays;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

public class getPieInformation implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus toAnalyze= IJ.getImage();
		// add menu to get nPiePieces
		int [] pieces = new int[] {4,5,6,8,9,10,12,15,18,20,24,30,36,40,45,60,72,90,120,180};
		pieAnalysis(toAnalyze, 12);
	}
	
	public void pieAnalysis (ImagePlus imp, int nPieces) {
		int h = imp.getHeight();
		int w= imp.getWidth();
		int minDim = Math.min(h,w);
		int angle = 360/nPieces;
		int [] xp = new int[] {w/2, w/2+minDim/2,w/2+(int)(Math.cos((double)angle*Math.PI/180)*(minDim/2))};
		int [] yp = new int[] {h/2, h/2,h/2+(int)(Math.sin((double)angle*Math.PI/180)*(minDim/2))}; 
		for (int i=0;i<nPieces;i++) {
			Roi nRoi = new PolygonRoi(xp,yp,3,Roi.POLYGON);
			imp.setRoi(nRoi);
			IJ.run(imp, "Rotate...", "rotate angle="+angle*i);
			IJ.run(imp, "Draw", "slice");
		}
		imp.show();
	}
	
	public double [][] pieAnalysis (ImagePlus imp, int nPieces, int minDim) {
		int h = imp.getHeight();
		int w= imp.getWidth();
		int angle = 360/nPieces;
		int [] xp = new int[] {w/2, w/2+minDim/2,w/2+(int)(Math.cos((double)angle*Math.PI/180)*(minDim/2))};
		int [] yp = new int[] {h/2, h/2,h/2+(int)(Math.sin((double)angle*Math.PI/180)*(minDim/2))};
		Roi nRoi = new PolygonRoi(xp,yp,3,Roi.POLYGON);
		Point [] ps= nRoi.getContainedPoints();
		//IJ.log(""+ps.length);
		double[][] data=new double[nPieces][];
		for (int i=0;i<nPieces;i++) {
			ImagePlus toTurn = imp.duplicate();
			toTurn.setTitle("imag "+i);
			//toTurn.show();
			// instead of rotating the ROI, rotating the imagePlus
			IJ.run(toTurn, "Rotate... ", "angle="+angle*i+" grid=1 interpolation=Bilinear");
			toTurn.setRoi(nRoi);
			IJ.run(toTurn, "Draw", "slice");
			//IJ.run(imp, "Rotate...", "rotate angle="+angle*i);
			double[] cvals = new double[ps.length];
			for (int j=0;j<ps.length;j++) {
				cvals[j]=toTurn.getProcessor().getValue(ps[j].x, ps[j].y);
			}
			data[i]=cvals;
			//IJ.log(Arrays.toString(data[i]));
		}
		//imp.show();
		return data;
	}
	
	public pearsonTable getPearsons(double [][] data) {
		// we need to start from i and compare to all subsequent
		double [][] allcps =new double [data.length][data.length];
		for (int i=0;i<data.length;i++){  
			//this means that there will be n-i-1 results to calculate
			double [] cps = new double [data.length];
			//starting at i+1, till the end get all comparisons
			for (int j=0;j<data.length;j++) {
				
				double cP = getPearson(data[i],data[j]);
				cps[j]=cP;
			}
			allcps[i]=cps;
		}
		pearsonTable ps = new pearsonTable(allcps);
		return ps;
	}
	
	public double getPearson(double[] a, double[] b) {
		double sx=0,sy=0,sxy=0,xs=0,ys=0;
		for (int i=0;i<a.length;i++) {
			sx+=a[i];
			sy+=b[i];
			sxy+=a[i]*b[i];
			xs+=a[i]*a[i];
			ys+=b[i]*b[i];
		}
		//IJ.log(""+sx+", "+sy+","+xs+", "+ys+","+sxy);
		return ((a.length*sxy-sx*sy)/Math.sqrt((a.length*xs-sx*sx)*(a.length*ys-sy*sy)));
	}
	
	public ResultsTable makeTable (double [][] data) {
		ResultsTable rt = new ResultsTable();
		for (int i=0;i<data.length;i++) {
			rt.incrementCounter();
			for (int j=0;j<data[i].length;j++) {
				rt.addValue("compare "+(i+j), data[i][j]);
			}
		}
		return rt;
	}
	
	public pearsonTable[] getAllPearsonData(ImagePlus imp, int minDim) {
		int [] pieces = new int[] {4,5,6,8,9,10,12};//,15,18,20,24,30,36,40,45,60,72,90,120,180};
		pearsonTable[] pearsonTables = new pearsonTable[pieces.length];
		for (int i=0;i<pieces.length;i++) {
			double[][] data = pieAnalysis(imp, pieces[i],minDim);
			pearsonTable ttable = getPearsons(data);
			pearsonTables[i]=ttable;
		}
		return pearsonTables;
	}
	
	public int getOptimalRotationSymmetry(pearsonTable[] ps) {
		double oP=-1;
		pearsonTable best=null;
		for (int i=0;i<ps.length;i++) {
			IJ.log("pieces "+ps[i].table.length +", "+ps[i].avPearsonMinOne+", avP "+ps[i].avPearson+", minP "+ps[i].minPearson+", maxP "+ps[i].maxPearson+", pdev" +ps[i].totalPearsonDeviation);
			if (ps[i].avPearsonMinOne>oP) {
				best=ps[i];
				oP=ps[i].avPearsonMinOne;
			}
		}
		return best.table.length;
	}

	public static void main (String [] args) {
		ImageJ IJ2 = new ImageJ();
		ImagePlus imp = IJ.openImage("F:/OneDrive - Universiteit Leiden/projects/colony morphology/T4Livi-5.tif");
		getPieInformation p = new getPieInformation();
		pearsonTable[] ps = p.getAllPearsonData(imp, 160);
		IJ.log("optimal symmetry"+ p.getOptimalRotationSymmetry(ps));
		//ResultsTable rt2 = p.makeTable(pearsons);
		//rt2.show("Outcome");
	}
	
	
}