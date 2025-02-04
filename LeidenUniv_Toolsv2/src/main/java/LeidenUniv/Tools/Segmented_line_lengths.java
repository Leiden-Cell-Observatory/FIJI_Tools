package LeidenUniv.Tools;
import ij.*;
import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.gui.Roi;
import java.awt.Polygon;
import ij.measure.Calibration;
import ij.measure.ResultsTable;

/**
 * Measures subsections of a polygon line
 * @author willemsejj
 *
 */
public class Segmented_line_lengths  implements PlugIn {

	public void run(String arg) {
		ImagePlus imp = IJ.getImage();
		Calibration cal =imp.getCalibration();
		Roi cr = imp.getRoi();
		if (cr==null) {
			IJ.showMessage("Roi required");
			return;
		}
		Polygon p = cr.getPolygon();
		if (p==null) {
			IJ.showMessage("Polygon Roi required");
			return;
		}
		int[] xs = p.xpoints;
		int[] ys = p.ypoints;
		double [] dist = new double[p.npoints-1];
		for (int i=0; i<p.npoints-1;i++){
			dist[i]=Math.sqrt(((double)xs[i]-(double)xs[i+1])*((double)xs[i]-(double)xs[i+1])+((double)ys[i]-(double)ys[i+1])*((double)ys[i]-(double)ys[i+1]))*cal.pixelWidth;
		}
		ResultsTable rt = new ResultsTable();
		for (int i =0; i<dist.length;i++){
			rt.incrementCounter();
			rt.addValue("Distance", dist[i]);
		}
		rt.show("Measured lengths");
	}
}
