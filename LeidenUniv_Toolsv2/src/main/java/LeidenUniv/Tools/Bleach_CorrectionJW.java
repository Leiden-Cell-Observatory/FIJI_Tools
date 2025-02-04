package LeidenUniv.Tools;
import ij.*;
import ij.gui.*;
import ij.plugin.*;

/**
 * 
 * Plugin for leveling the total intensities of the imagestack
 * Allows to correct on a ROI as well
 * @author willemsejj
 */
public class Bleach_CorrectionJW implements PlugIn {

	/**
	 * To run in ImageJ/FIJI
	 * starts from the first frame, slice and channel
	 */
	public void run(String arg) {
		ImagePlus imp = IJ.getImage();
		// build in menu for selection
		imp= getCorrected(imp,1,1,1);
	}

	/**
	 * Similar to running it from the menu, however allows the input of another slice as start position
	 * @param imp2 The image to correct
	 * @param c The channel to use as base for the correction
	 * @param s The slice to use as base for the correction
	 * @param f The frame to use as base for the correction
	 * @return The corrected imagestack
	 */
	static public ImagePlus getCorrected (ImagePlus imp2, int c, int s, int f){
		int[] Dim =new int[5];
		Dim=imp2.getDimensions();
		Roi cRoi= imp2.getRoi();
		double StartMean=0;
		if (cRoi!=null && cRoi.isArea()){
			StartMean=imp2.getStatistics(2).mean;
		} else {
			StartMean= imp2.getStatistics(2).mean;
		}
		//IJ.log(""+Dim[2]+",s:"+Dim[3]+","+Dim[4]);
		for (int h=1;h<Dim[2]+1;h++){
			for (int i=1;i<Dim[3]+1;i++){
				for (int j=1;j<Dim[4]+1;j++){
					imp2.setPosition(h,i,j);
					if (cRoi!=null && cRoi.isArea()){
						imp2.setRoi(cRoi);
						double m=imp2.getStatistics(2).mean;
						IJ.run(imp2, "Select None", "");
						IJ.run(imp2, "Multiply...", "value="+StartMean/m+" slice");
					} else {
						double m= imp2.getStatistics(2).mean;
						IJ.run(imp2, "Multiply...", "value="+StartMean/m+" slice");
					}
				}
			}
		}
		return imp2;
	}
}
