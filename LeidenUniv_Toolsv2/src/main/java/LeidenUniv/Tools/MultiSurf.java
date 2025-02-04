package LeidenUniv.Tools;

import java.awt.Scrollbar;
import java.io.File;
import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.DirectoryChooser;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.plugin.RGBStackMerge;

public class MultiSurf implements PlugIn{

	public static void main(String[] args) {
		MultiSurf ms = new MultiSurf();
		ms.run(null);
		}
	

	@Override
	public void run(String arg0) { // this works fine
		// TODO getdirectory
		// TODO run through the files and SRRF them.
		if (IJ.showMessageWithCancel("Warning","This tool has been designed to run on an OMERO dataset. Preferably use it from OMERO\n Click Ok to continue this way, cancel to stop")) {
			DirectoryChooser dc = new DirectoryChooser("Please select the folder holding your local images");
			
			GenericDialog gd = new GenericDialog("SRRF settings");
			gd.addSlider("Ring radius (smaller = better resolution)", 0.1, 3.0, 0.5);
			gd.addSlider("Radiality Magnification (bigger = better resolution)",1,10,5);
			gd.addSlider("Axes in ring (bigger = better resolution)",2,8,6);
			
			gd.showDialog();
			if (gd.wasCanceled()) {
				return;
			}
			Vector<Scrollbar> v = gd.getSliders();
			Scrollbar b1 = (Scrollbar) v.get(0);
			Scrollbar b2 = (Scrollbar) v.get(1);
			Scrollbar b3 = (Scrollbar) v.get(2);
			double v1 =b1.getValue(); //returns from 5 to ? 150
			double v2 =b2.getValue(); //returns from 1 to ? 10
			double v3 =b3.getValue(); //returns from 2 to ? 8
			
			String dir = dc.getDirectory();
			if (dir!=null) {
				File f = new File(dir);
				File [] files = f.listFiles();
				for (File fi :files) {
					try {
						ImagePlus imp = IJ.openImage(fi.toString());
						imp.show();
						SRRFImage(imp,v1/50,(int)v2,(int)v3);
					} catch (Exception e) {
						IJ.log(("Could not open file: " +fi.toString()));
						IJ.log(e.getMessage());
						continue;
					}
				}
			}
		}
	}
	
	public ImagePlus SRRFImage(ImagePlus imp, double ring, int mag, int axes) {
		IJ.run(imp, "Subtract Background...", "rolling=50 stack");
		Calibration cal = imp.getCalibration();
		IJ.run("SRRF Analysis", "ring="+ring+" radiality_magnification="+mag+" axes="+axes+" frames_per_time-point=0 start=0 end=0 max=100 preferred=0");
		ImagePlus n= IJ.getImage();
		IJ.run(n, "Fire", "");
		IJ.resetMinAndMax(n);
		IJ.run(n, "Enhance Contrast", "saturated=0.001");
        IJ.run(n, "16-bit", "");
        //IJ.saveAs(n, "Tiff", "e:/temp/srrf/"+n.getTitle());
        Duplicator dc = new Duplicator();
        ImagePlus imp2= dc.run(imp,1,1,1,1,1,1);
        imp2.setTitle("temp");
        imp2 = imp2.resize(imp2.getWidth()*mag, imp2.getHeight()*mag, "bilinear");
        ImagePlus [] imps = new ImagePlus[] {n,imp2};
        ImagePlus imp3 = RGBStackMerge.mergeChannels(imps, false);
        Calibration cal2 = cal.copy();
        cal2.pixelDepth=cal.pixelDepth/mag;
        cal2.pixelHeight=cal.pixelHeight/mag;
        cal2.pixelWidth=cal.pixelWidth/mag;
        imp3.setCalibration(cal2);
        imp.hide();
        imp.close();
        n.hide();
        n.close();
		return imp3;
	}

}
