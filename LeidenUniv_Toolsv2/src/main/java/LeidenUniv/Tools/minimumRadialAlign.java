package LeidenUniv.Tools;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import ij.plugin.MontageMaker;
import ij.plugin.PlugIn;

public class minimumRadialAlign implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		
	}
	public static ImagePlus verticalAllignImages(ImagePlus wide, ImagePlus small) {
		ImageStack als = new ImageStack(small.getWidth(),small.getHeight());
		float mindif=Float.MAX_VALUE;
		float mindifpos=0;
		for (int i=0;i<360;i++) {
			float dif = getImageDifference(wide,small,i,small.getHeight());
			if (mindif>dif) {
				mindif=dif;
				mindifpos=i;
			}
		}
		IJ.run(wide, "Specify...", "width=360 height="+small.getHeight()+" x="+mindifpos+" y=0");
		Duplicator dp = new Duplicator();
		ImagePlus wideal= dp.run(wide);
		als.addSlice(small.getProcessor());
		als.addSlice(wideal.getProcessor());
		ImagePlus aligned = new ImagePlus("Aligned",als);
		return aligned;
	}
	
	public static float getImageDifference(ImagePlus wide, ImagePlus small, int start, int height) {
		IJ.run(wide, "Specify...", "width=360 height="+height+" x="+start+" y=0");
		Duplicator dp = new Duplicator();
		ImagePlus dup = dp.run(wide);
		float[][] w= dup.getProcessor().getFloatArray();
		float [][] s = small.getProcessor().getFloatArray();
		float imdif=0;
		//IJ.log(""+wide.getWidth());
		for (int i=0;i<360;i++) {
			for (int j=0;j<height;j++) {
				float td = (Math.abs(w[i][j]-s[i][j]));
				imdif=imdif+td;
			}
		}
		//System.err.println(""+imdif);

		return imdif;
		
	}
	
	public static ImagePlus getHorizontalDuplicate(ImagePlus imp) {
		imp.show();
		IJ.run(imp, "Add Slice", "");
		imp.setSlice(0);
		IJ.run(imp, "Select All", "");
		IJ.run(imp, "Copy", "");
		imp.setSlice(1);
		IJ.run(imp, "Paste", "");
		IJ.run(imp, "Make Montage...", "columns=2 rows=1 scale=1");
		ImagePlus res = new MontageMaker().makeMontage2(imp, 2,1,1,1,2,1,0,true);
		res.show();
		return res;
	}
	public static void main (String[] args) {
		ImagePlus small = IJ.openImage("F:/Joost/projects/colony morphology/radialtransform alignment/singletf1.tif");
		ImagePlus prewide = IJ.openImage("F:/Joost/projects/colony morphology/radialtransform alignment/singletf1.tif");
		IJ.run(small, "Stack to RGB", "");
		IJ.run(prewide, "Stack to RGB", "");
		// check if picture is 24-bit/8-bit. Need to make different alignment methods
		prewide.show();
		small.show();
		ImagePlus wide = minimumRadialAlign.getHorizontalDuplicate(prewide);
		ImagePlus aligned =minimumRadialAlign.verticalAllignImages(wide,small);
		aligned.show();
	}
}
