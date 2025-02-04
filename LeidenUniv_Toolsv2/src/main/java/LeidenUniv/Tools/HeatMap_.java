package LeidenUniv.Tools;

import ij.*;
import ij.plugin.*;

/**
 * 
 * Makes a Z-projection heatmap
 * @author willemsejj
 */
public class HeatMap_ implements PlugIn {

	/**
	 * To run as plugin from ImageJ/FIJI
	 */
	public void run(String arg) {
		ImagePlus imp = IJ.getImage();
		ImagePlus heatmap = getHeatMap(imp, "Triangle");
		heatmap.show();
	}
	
	/**
	 * @param imp The image to make the heatmap of
	 * @param method The threshold method to be used
	 * @return The heatmap image
	 */
	public ImagePlus getHeatMap(ImagePlus imp, String method) {
		IJ.run(imp, "Convert to Mask", "method="+method+" background=Dark calculate black");
		ZProjector zp = new ZProjector(imp);
		zp.setMethod(ZProjector.AVG_METHOD);
		zp.doProjection();
		ImagePlus heatmap= zp.getProjection();
		IJ.run(heatmap, "Fire", "");
		IJ.run(heatmap, "Enhance Contrast", "saturated=0.00");
		IJ.run(heatmap, "RGB Color", "");
		return heatmap;
	}
	
	public static void main(String [] arg) {
		ImagePlus imp = IJ.openImage("http://imagej.nih.gov/ij/images/t1-head.zip");
		imp.show();
		new HeatMap_().run("");
	}

}
