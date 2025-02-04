package LeidenUniv.Tools;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.ImageJ;

public class IntensityDirection implements PlugIn {
	
	ImageProcessor ip1 = null;

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus imp = IJ.getImage();
		
		//imp.hide();
		//Get user input for kernel size
		GenericDialog gd = new GenericDialog("Set kernel size");
		gd.addNumericField("Kernel size", 3, 0);
		gd.addCheckbox("Gaussian Blur before?", false);
		gd.showDialog();
		double kernelSize = gd.getNextNumber();
		boolean blur=gd.getNextBoolean();
		ImagePlus output = getIntensityDirectionImage(imp, kernelSize,blur);
		output.show();
		
	}
	
	public ImagePlus getIntensityDirectionImage(ImagePlus imp, double kernSize,boolean blur) {
		if (blur) {
			IJ.run(imp, "Gaussian Blur...", "sigma="+(int)kernSize);
		}
		int height =imp.getHeight();
		int width = imp.getWidth();
		int kSize=(int)kernSize;
		ip1 =imp.getProcessor();
		Duplicator dp = new Duplicator();
		ImagePlus imp2 = dp.run(imp,1,1);
		IJ.run(imp2, "32-bit", "");
//		ImagePlus imp2 = IJ.createImage("Direction Image", "black", imp.getWidth(), imp.getHeight(), 1);
		//IJ.log(""+ imp2.getHeight());
		imp2.show();
		ImageProcessor ip2=imp2.getProcessor();
		for (int i=0;i<width;i++) {
			for (int h=0;h<height;h++) {
				int intensity = ip1.get(i, h);
				double newval = getLocalDirection(i,h,intensity,kSize);
				ip2.putPixelValue(i, h, newval);
			}
		}
		imp2.setProcessor(ip2);
		IJ.resetMinAndMax(imp2);
		IJ.run(imp2, "Spectrum", "");
		//imp2.show();
		return imp2;
	}
	
	public double getLocalDirection(int i, int j, int intensity, int kernSize) {
		int maxX=i;
		int maxY=j;
		//IJ.log("ij " +i+","+j);
		int ks= Math.max(i-kernSize, 0);
		int ke = Math.min(i+kernSize,ip1.getWidth());
		int ls =Math.max(j-kernSize, 0);
		int le = Math.min(j+kernSize, ip1.getHeight());
		/*if (i==10){
			IJ.log(""+ks+","+ls+" i "+intensity);
		}*/
		for (int k=ks;k<ke;k++) {
			for (int l= ls;l<le;l++) {
				
				if (ip1.get(k, l)>intensity) {
					maxX=k;
					maxY=l;
				}
			}
		}
		if (maxX!=i ||maxY!=j) {
			return Math.toDegrees(Math.atan2(maxY-j, maxX-i));
		} else {
			return 0.0;
		}
		//return 0.0;
	}
	
	public static void main(String[] args) {
		ImageJ imageJ = new ImageJ();
		imageJ.getInfo();
    	ImagePlus imp = IJ.openImage("http://imagej.nih.gov/ij/images/AuPbSn40.jpg");
    	//ImagePlus imp = IJ.createImage("Untitled", "8-bit ramp", 500, 150, 1);
    	imp.show();
    	//imp = imp.resize(500, 500, "bilinear");
    	//IJ.run(imp, "Size...", "width=500 height=500 depth=1 constrain average interpolation=Bilinear");
    	IntensityDirection id = new IntensityDirection();
    	id.run("");
    }
}
