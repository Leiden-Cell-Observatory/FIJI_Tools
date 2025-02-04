package LeidenUniv.Tools;
import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.plugin.filter.MaximumFinder;
import ij.process.ByteProcessor;
import ij.plugin.ImageCalculator;

/**
 * Does adaptive watershedding based on the find maxima threshold
 * @author willemsejj
 */
public class Adaptive_Watershed implements PlugIn {
	
	/*
	 * @param string Not used in this instance
	 */
	public void run(String arg) {
		ImagePlus imp =IJ.getImage();
		GenericDialog gd = new GenericDialog("Set noise level for watershed");
		gd.addNumericField("Noise level",10,0);
		gd.showDialog();

		int th= (int)(gd.getNextNumber());
		ImagePlus newImp = getWatershedImage(imp,th);
		newImp.show();
		newImp.setTitle("watershedded @ noise "+th);
	}

	/**
	 * @param or The input image to be watershedded
	 * @param thresh The noise level for adaptive watershedding
	 * @return The watershedded image
	 */
	public ImagePlus getWatershedImage(ImagePlus or, int thresh){
		ImagePlus or2 =or.duplicate();
		IJ.run(or2, "Distance Map", "stack");
		ImagePlus seg=null;
		if (or2.isStack()){
			MaximumFinder mf = new MaximumFinder();
			for (int i=0; i<or2.getNSlices();i++){
				or2.setSlice(i+1);
				ByteProcessor bp = mf.findMaxima(or2.getProcessor(),thresh,MaximumFinder.SEGMENTED,false);				
				or2.setProcessor(bp);
			}
			seg = or2;
		} else {
			MaximumFinder mf = new MaximumFinder();
			ByteProcessor bp = mf.findMaxima(or2.getProcessor(),thresh,MaximumFinder.SEGMENTED,false);
			seg = new ImagePlus("Segmented",bp);
		}
		//seg.show();
		ImageCalculator ic = new ImageCalculator();
		ImagePlus i4 =ic.run("AND stack create",or,seg);
		//i4.show();
		return i4;
	}
}
