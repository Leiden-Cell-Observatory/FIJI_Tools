package LeidenUniv.Tools;

import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import ij.measure.*;

/**
 * Tool for analysing multiple lines
 * @author willemsejj
 *
 */
public class Line_Analysis implements PlugIn {
	
	ImagePlus imp;
	/**
	 * To run the plugin from ImageJ/FIJI
	 */
	public void run(String arg) {
		imp = IJ.getImage();
		IJ.setTool("line");
		IJ.run(imp, "Line Width...", "line=50");
		
		Roi [] rois = addLines();
		double [][] Dat =getLines(rois);
		//Dat=alignMinima(Dat);
		ResultsTable fin = getRes(Dat, rois.length);
		fin.show("Plots");
	
	}

	/**
	 * This aligns the minima from the double arrays
	 * @param ChAr the input double array-array
	 * @return a novel double array-array
	 */
	double [][] alignMinima(double [][] ChAr){
		double[] mins = new double[ChAr.length];
		int [] minpos = new int[ChAr.length];
		int maxsize=0;
		for (int i=0;i<ChAr.length;i++){
			if (ChAr[i].length>maxsize){
				maxsize=ChAr[i].length;
			}
			double min =255.0;
			for (int j=0;j<ChAr[i].length;j++){
				if (ChAr[i][j]<min){
					min=ChAr[i][j];
					minpos[i]=j;
				}
			}
			mins[i]=min;
			//IJ.log("min" +mins[i]+" minpos"+minpos[i]);
		}
		double [][] retAr= new double[ChAr.length][maxsize];
		
		int maxminpos =0;
		for (int k=0;k<5;k++){
			if (minpos[k]>maxminpos){
				maxminpos=minpos[k];
			}
		}
		//IJ.log(""+maxminpos);
		for (int l=0;l<ChAr.length;l++){
			for (int lll=0;lll<maxsize;lll++){
				if ((lll+maxminpos-minpos[l])<ChAr[l].length){
					retAr[l][lll+maxminpos-minpos[l]]=ChAr[l][lll];
				}
			}
		}
		return retAr;
	}

	/**
	 * Gets the results in a resultstable
	 * @param DA The double array to put in the results
	 * @param Num The number of things to analyse, limited to 5 currently
	 * @return the created resultstable
	 */
	ResultsTable getRes(double [][] DA, int Num){
		ResultsTable tot = new ResultsTable();
		String [] cols = {"I","II","III","IV","V"};
		for (int i=0;i<Num;i++){
			for (int j=0;j<DA[i].length;j++){
				tot.setValue(cols[i],j,DA[i][j]);
			}
		}
		return tot;
	}

	/**
	 * Gets the values for each ROI in the ROI array
	 * @param RA the input ROI array
	 * @return the measured double array
	 */
	double [][] getLines(Roi[] RA){
		double [][] TotalData = new double [RA.length][0];
		 
		for (int i=0;i<RA.length;i++){
			imp.setRoi(RA[i]);
			ProfilePlot pp = new ProfilePlot(imp);
			TotalData [i] = pp.getProfile();
		}
		return TotalData;
	}

	/** This is where the ROI array is created interactively
	 * @return the ROI array
	 */
	Roi [] addLines(){
		RoiManager rm = RoiManager.getInstance();
		if (rm==null) rm = new RoiManager();
		if (rm.getCount()>0){
			rm.runCommand("Deselect");
			rm.runCommand("Delete");
		}
		WaitForUserDialog wfd = new WaitForUserDialog("Please draw the lines you want to measure,\n press t after each line.\n Press OK when you are done");
		wfd.show();
		return rm.getRoisAsArray();
	}


}
