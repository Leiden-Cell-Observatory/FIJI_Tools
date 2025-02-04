package LeidenUniv.Tools;
import ij.*;
import ij.process.*;
import ij.plugin.*;

public class RGB_cube implements PlugIn {

	public void run(String arg) {
		ImagePlus imp = IJ.createImage("Untitled", "RGB black", 256, 256, 256);
		ImageProcessor ip = imp.getProcessor();
		imp.setTitle("RGB color cube");
		for (int i=0;i<256;i++){
			imp.setSlice(i+1);
			for (int j=0;j<256;j++){
				for (int k=0; k<256;k++){
					int [] val = {k,j,i};
					ip.putPixel(k,j,val);
				}
			}
		}
		imp.show();
	}
	
	public static void main(String[] args) {
		ImageJ ij = new ImageJ();
		ij.getInfo();
		new RGB_cube().run("");
	}
}
