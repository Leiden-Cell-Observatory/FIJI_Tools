package LeidenUniv.Tools;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.measure.Calibration;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImageJ ij = new ImageJ();
		ImagePlus imp = IJ.openImage("E:/Temp/srrf/4test.tif");
		imp.show();
		Calibration cal = imp.getCalibration();
		// this is a null object float [] ct = cal.getCTable();
		System.out.println(""+cal.pixelHeight);
		System.out.println(""+cal.pixelWidth);
		System.out.println(""+cal.pixelDepth);
		cal.pixelDepth=cal.pixelDepth/10;
		cal.pixelHeight=cal.pixelHeight/10;
		cal.pixelWidth=cal.pixelWidth*2;
		//imp.setCalibration(cal);
		System.out.println(""+cal.pixelHeight);
		System.out.println(""+cal.pixelWidth);
		System.out.println(""+cal.pixelDepth);
		
	}

}
