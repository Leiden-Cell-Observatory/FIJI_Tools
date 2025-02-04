package LeidenUniv.Tools;
import ij.*;
import ij.plugin.*;
import ij3d.ImageJ3DViewer;

/**
 * Instead of doing a z-projection this makes a max intensity t-projection of your images
 * @author willemsejj
 * 
 */
public class TimeProjector3D_ implements PlugIn {
	
	/**
	 * To run it as plugin from ImageJ/FIJI
	 */
	public void run(String arg) {
		if (WindowManager.getCurrentImage()==null){
			IJ.showMessage("An open image is required");
		} else {
			ImagePlus imp = IJ.getImage();			
			IJ.run(imp, "Re-order Hyperstack ...", "channels=[Channels (c)] slices=[Frames (t)] frames=[Slices (z)]");
			imp = IJ.getImage();			
			ImagePlus imp2 = ZProjector.run(imp,"max all");
			//imp2 = IJ.getImage();			
			IJ.run(imp2, "Re-order Hyperstack ...", "channels=[Channels (c)] slices=[Frames (t)] frames=[Slices (z)]");
			imp2 = IJ.getImage();			
			imp2.setTitle("time projected");
			IJ.run("3D Viewer", "");
			ImageJ3DViewer.add("time projected", "None", "time projected", "0", "true", "true", "true", "1", "0");
			//call("ij3d.ImageJ3DViewer.add", "time projected", "White", "MAX_HyperStack", "0", "true", "true", "true", "1", "0");
		}
	}
}