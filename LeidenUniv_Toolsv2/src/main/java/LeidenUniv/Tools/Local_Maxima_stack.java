package LeidenUniv.Tools;
import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.plugin.filter.MaximumFinder;
import ij.measure.*;

public class Local_Maxima_stack implements PlugIn {
	static ImagePlus imp;

	public void run(String arg) {
		imp=IJ.getImage();
		Calibration cal = imp.getCalibration();
		ImageStack is = new ImageStack(imp.getWidth(),imp.getHeight());
		int lth=5;
		int sps=3;
		GenericDialog gd = new GenericDialog("Settings");

		gd.addNumericField("Prominence/Threshold for local maxima:", lth, 0);
		gd.addNumericField("Enter number of dilations:", sps, 0);
		gd.addCheckbox("exclude edges", false);
		gd.showDialog();
		lth=(int)gd.getNextNumber();
		sps=(int)gd.getNextNumber();
		boolean edges=gd.getNextBoolean();
		
		if (!gd.wasCanceled()) {
	
			for (int i=0; i<Math.max(imp.getDimensions()[3],imp.getDimensions()[4]);i++){
				imp.setSlice(i+1);
				MaximumFinder mf = new MaximumFinder();
				is.addSlice(mf.findMaxima(imp.getProcessor(),lth,MaximumFinder.SINGLE_POINTS,edges));
			}
	
			ImagePlus imp3= new ImagePlus ("points",is);
			IJ.run(imp3, "Make Binary", "method=Default background=Default calculate black");
			IJ.run(imp3, "Options...", "iterations="+ (sps-1)+" count=3 black edm=Overwrite do=Nothing");
			IJ.run(imp3, "Dilate", "stack");
			IJ.run(imp3, "Options...", "iterations=1 count=1 black edm=Overwrite do=Nothing");
			imp3.show();
			IJ.run(imp3, "Properties...", "channels=1 slices=1 frames="+Math.max(imp.getDimensions()[3],imp.getDimensions()[4])+" unit=pixel pixel_width=1.0000 pixel_height=1.0000 voxel_depth=1.0000");
			imp3.setCalibration(cal);
		}
	}

}
