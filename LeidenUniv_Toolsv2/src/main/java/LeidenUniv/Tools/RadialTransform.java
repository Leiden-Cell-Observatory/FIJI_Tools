package LeidenUniv.Tools;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.ProfilePlot;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class RadialTransform implements PlugIn {
	
	public static ImagePlus getRadialTransform(ImagePlus imp3, int w, int h){

		ImagePlus[] channels = null;
		int [] dim = imp3.getDimensions();
		if (imp3.getBitDepth()==16){
			channels = new ImagePlus[3];
			Duplicator dc = new Duplicator();
			channels[0]=dc.run(imp3,1,1,1,dim[3],1,1);
			channels[1]=dc.run(imp3,2,2,1,dim[3],1,1);
			channels[2]=dc.run(imp3,3,3,1,dim[3],1,1);
			
		} else if (imp3.getBitDepth()==24){
			channels = ChannelSplitter.split(imp3);
		}	

		float [][] red = getRadialKymogram(channels[0],w,h);	
		float [][] green = getRadialKymogram(channels[1],w,h);	
		float [][] blue = getRadialKymogram(channels[2],w,h);	

		ImageStack out = new ImageStack(red.length,red[0].length);
		//IJ.log(""+out.isRGB());
		//IJ.log(""+red.length+" "+red[0].length);
		ImageProcessor ro = new FloatProcessor(red.length,red[0].length);
		ro.setFloatArray(red);
		ImageProcessor go = new FloatProcessor(green);
		ImageProcessor bo = new FloatProcessor(blue);
		out.addSlice("r",ro);
		out.addSlice("g",go);
		out.addSlice("b",bo);
		
		ImagePlus Iout = new ImagePlus("output",out);
		IJ.run(Iout, "8-bit", "");
		
		//Iout.hide();
		return Iout;
	}
	
	public static float[][] getRadialKymogram(ImagePlus imp2,int w,int h){
		int min = Math.min(w, h);
		
		float [][] vals = new float [360][0];
		int LineL =min/2;
		double [] tmp = new double[min/2];
		for (int i=0;i<360;i++){
			imp2.setRoi(new Line(w/2,h/2,w/2+min/2,h/2));
			IJ.run(imp2, "Rotate...", "rotate angle="+i);
			ProfilePlot profile = new ProfilePlot(imp2);
			tmp = profile.getProfile();	
			float[] tf = new float[LineL];
			for (int j=0;j<Math.min(LineL,tmp.length);j++){
				tf[j]=(float)tmp[j];
			}
			vals[i]=tf;
			//IJ.log(""+tf.length);
		}
		return vals;
	}
	
	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus imp = IJ.getImage();
		int w = imp.getWidth();
		int h = imp.getHeight();
		ImagePlus mintf = RadialTransform.getRadialTransform(imp, w,h);
		//ImagePlus tf = RadialTransform.getRadialTransform(imp, w,h);
		mintf.show();
	}
}
