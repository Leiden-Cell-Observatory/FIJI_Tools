package LeidenUniv.Tools;
import fiji.stacks.Hyperstack_rearranger;
import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.*;

/**
 * To create a one by one max projection build up, useful for making movies of your stack
 * @author willemsejj
 *
 */
public class ZProject_BuildUp implements PlugIn {

	public void run(String arg) {
		ImagePlus imp = IJ.getImage();
		int [] dim = imp.getDimensions();
		
		GenericDialog gd = new GenericDialog("settings");
		String [] methods = new String[] {"avg","min","max","sum","sd", "median"};
		gd.addChoice("Project method",methods , "Depth");
		
		
		
		if (dim[3]>1 && dim[4]>1) {
			gd.addChoice("Dimension to project", new String[] {"Time","Depth"}, "Depth");
		} else if (dim[4]>1) {
			// re-order stack
			Hyperstack_rearranger.reorderHyperstack(imp, 0,2, 1, true, true);
			imp = IJ.getImage();
		}
		
		gd.showDialog();
		int meth = gd.getNextChoiceIndex();
		
		if (dim[3]>1 && dim[4]>1) {
			if (gd.getNextChoiceIndex()==0) {
				// re-order stack
				Hyperstack_rearranger.reorderHyperstack(imp, 0,2, 1, true, true);
				imp = IJ.getImage();
			}
		}
		dim = imp.getDimensions();
		
		ImageStack is = new ImageStack(imp.getWidth(),imp.getHeight());
		for (int i=1;i<=dim[3];i++){
			ImagePlus tmp = ZProjector.run(imp,methods[meth],1,i);
			//tmp.show();
			is.addSlice(tmp.getProcessor());
		}
		ImagePlus impout=new ImagePlus("Projection", is);
		impout.show();
	}

}
