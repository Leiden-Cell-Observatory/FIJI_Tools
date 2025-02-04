package LeidenUniv.Tools;
import ij.IJ;
//import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class MinMaxContrast implements PlugIn{
	/**
	 * To run the program in ImageJ/FIJI
	 */
	/**
	 *
	 */
	public void run(String arg){
		//IJ.log("\\Clear");
		ImagePlus OR = IJ.getImage();
		boolean isRGB=false;
		if (OR.getBitDepth()==24) {
			IJ.run(OR, "RGB Stack", "");
			isRGB=true;
		}
		int[] Dim =OR.getDimensions();
		GenericDialog gd = new GenericDialog("settings");
		
		
		boolean MC=false, MD=false, MT=false, CC=false,DC=false, TC=false;
		
		if (Dim[2]>1) {
			gd.addCheckbox("Channels", true);
			MC=true;
		} 
		if (Dim[3]>1) {
			gd.addCheckbox("Depth", false);
			MD=true;
		} 
		if (Dim[4]>1) {
			gd.addCheckbox("Time", false);
			MT=true;
		}
		if (MC||MD||MT) {
			gd.addMessage("check the dimensions you want to do the min max optimization for\nleaving the boxes unchecked means it will optimize on min and max of the entire stack");
			gd.showDialog();
		}
		
		if (MC) {
			CC=gd.getNextBoolean();
			if (MD) {
				DC=gd.getNextBoolean();
				if (MT) {
					TC=gd.getNextBoolean();
				}
			}
		} else if (MD) {
			DC=gd.getNextBoolean();
			if(MT) {
				TC=gd.getNextBoolean();
			}
		} else if (MT) {
			TC=gd.getNextBoolean();
		} 
		
		ImagePlus opt = MinMaxContrast.getMinMaxContrast(OR, CC, DC, TC);
		if (isRGB) {
			IJ.run("RGB Color", "");
		}
		opt.show();
	}
		
	public static ImagePlus getMinMaxContrast(ImagePlus OR, boolean CC, boolean DC, boolean TC) {
		int [] Dim=OR.getDimensions();
		Duplicator dp = new Duplicator();
		if (CC&DC&TC) { 
			//optimize for min max of entire stack :111 --check
			ImageStack ios= new ImageStack();
			for (int i=1;i<=Dim[4];i++) {
				for (int k=1;k<=Dim[3];k++) {
					for (int l=1;l<=Dim[2];l++) {
						ImagePlus tmp = dp.run(OR,l,l,k,k,i,i);
						
						ImageStack tis =tmp.getImageStack();
						double minimum=tmp.getDisplayRangeMax();
						double maximum=tmp.getDisplayRangeMin();
						for (int j=1;j<=tis.getSize();j++) {
							ImageProcessor ip = tis.getProcessor(j);
							ImageStatistics tisis=ip.getStatistics();
							if (tisis.min<minimum) {
								minimum =tisis.min;
							}
							if (tisis.max>maximum) {
								maximum=tisis.max;
							}
						}
						tmp.setDisplayRange(minimum, maximum);
						IJ.run(tmp, "Apply LUT", "stack");
						for (int j=0;j<tis.getSize();j++) {
							ios.addSlice(tis.getProcessor(j+1));
						}
					}
				}
			}
			ImagePlus oOR=new ImagePlus("optimized",ios);
			oOR=HyperStackConverter.toHyperStack(oOR, Dim[2], Dim[3], Dim[4],"xyczt", "Color");
			oOR.setDisplayRange(0, Math.pow(2, oOR.getBitDepth()));
			return oOR;
		} else if (CC&!DC&!TC) {
			//optimize for min max of z and t, but split channels (suggested option) 100 --check
			ImageStack ios= new ImageStack();
			for (int i=1;i<=Dim[2];i++) {// run through channels
				ImagePlus tmp = dp.run(OR,i,i,1,Dim[3],1,Dim[4]);
				ImageStack tis =tmp.getImageStack();
				double minimum=tmp.getDisplayRangeMax();
				double maximum=tmp.getDisplayRangeMin();
				for (int j=1;j<=tis.getSize();j++) {
					ImageProcessor ip = tis.getProcessor(j);
					ImageStatistics tisis=ip.getStatistics();
					if (tisis.min<minimum) {
						minimum =tisis.min;
					}
					if (tisis.max>maximum) {
						maximum=tisis.max;
					}
				}
				tmp.setDisplayRange(minimum, maximum);
				IJ.run(tmp, "Apply LUT", "stack");
				
				for (int j=0;j<tis.getSize();j++) {
					ios.addSlice(tis.getProcessor(j+1));
				}
			}
			ImagePlus oOR=new ImagePlus("optimized",ios);
			oOR=HyperStackConverter.toHyperStack(oOR, Dim[2], Dim[3], Dim[4],"xyztc", "Color");
			oOR.setDisplayRange(0, Math.pow(2, oOR.getBitDepth()));
			return oOR;
		} else if (CC&!DC&&TC) {
			//optimze for min max in depth, and optimize each separate timepoint, and separate colour 101--check
			ImageStack ios= new ImageStack();
			for (int i=1;i<=Dim[2];i++) {// run through channels
				for (int k=1;k<=Dim[4];k++) {
					ImagePlus tmp = dp.run(OR,i,i,1,Dim[3],k,k);
					
					ImageStack tis =tmp.getImageStack();
					double minimum=tmp.getDisplayRangeMax();
					double maximum=tmp.getDisplayRangeMin();
					for (int j=1;j<tis.getSize();j++) {
						ImageProcessor ip = tis.getProcessor(j);
						ImageStatistics tisis=ip.getStatistics();
						if (tisis.min<minimum) {
							minimum =tisis.min;
						}
						if (tisis.max>maximum) {
							maximum=tisis.max;
						}
					}
					tmp.setDisplayRange(minimum, maximum);
					IJ.run(tmp, "Apply LUT", "stack");
					for (int j=0;j<tis.getSize();j++) {
						ios.addSlice(tis.getProcessor(j+1));
					}
				}
			}
			ImagePlus oOR=new ImagePlus("optimized",ios);
			oOR=HyperStackConverter.toHyperStack(oOR, Dim[2], Dim[3], Dim[4],"xyztc", "Color");
			oOR.setDisplayRange(0, Math.pow(2, oOR.getBitDepth()));
			return oOR;
		} else if(CC&&DC&!TC) { 
			//optimze for min max in time, and optimize each separate z-point, and separate colour 110 -- check
			ImageStack ios= new ImageStack();
			for (int i=1;i<=Dim[2];i++) {// run through channels
				for (int k=1;k<=Dim[3];k++) {
					ImagePlus tmp = dp.run(OR,i,i,k,k,1,Dim[4]);
				
					ImageStack tis =tmp.getImageStack();
					double minimum=tmp.getDisplayRangeMax();
					double maximum=tmp.getDisplayRangeMin();
					for (int j=1;j<=tis.getSize();j++) {
						ImageProcessor ip = tis.getProcessor(j);
						ImageStatistics tisis=ip.getStatistics();
						if (tisis.min<minimum) {
							minimum =tisis.min;
						}
						if (tisis.max>maximum) {
							maximum=tisis.max;
						}
					}
					tmp.setDisplayRange(minimum, maximum);
					IJ.run(tmp, "Apply LUT", "stack");
					for (int j=0;j<tis.getSize();j++) {
						ios.addSlice(tis.getProcessor(j+1));
					}
				}
			}
			ImagePlus oOR=new ImagePlus("optimized",ios);
			oOR=HyperStackConverter.toHyperStack(oOR, Dim[2], Dim[3], Dim[4],"xytzc", "Color");
			oOR.setDisplayRange(0, Math.pow(2, oOR.getBitDepth()));
			return oOR;
		} else if (!CC&&DC&&TC) {
			//combine colors, but not z and t for optimization --check
			ImageStack ios= new ImageStack();
			for (int i=1;i<=Dim[4];i++) {// run through frames
				for (int k=1;k<=Dim[3];k++) { // run through slices
					ImagePlus tmp = dp.run(OR,1,Dim[2],k,k,i,i);
					
					ImageStack tis =tmp.getImageStack();
					double minimum=tmp.getDisplayRangeMax();
					double maximum=tmp.getDisplayRangeMin();
					for (int j=1;j<=tis.getSize();j++) {
						ImageProcessor ip = tis.getProcessor(j);
						ImageStatistics tisis=ip.getStatistics();
						if (tisis.min<minimum) {
							minimum =tisis.min;
						}
						if (tisis.max>maximum) {
							maximum=tisis.max;
						}
					}
					tmp.setDisplayRange(minimum, maximum);
					IJ.run(tmp, "Apply LUT", "stack");
					for (int j=0;j<tis.getSize();j++) {
						ios.addSlice(tis.getProcessor(j+1));
					}
				}
			}
			ImagePlus oOR=new ImagePlus("optimized",ios);
			oOR=HyperStackConverter.toHyperStack(oOR, Dim[2], Dim[3], Dim[4],"xyczt", "Color");
			oOR.setDisplayRange(0, Math.pow(2, oOR.getBitDepth()));
			return oOR;
		} else if (!CC&!DC&&TC) {
		// optimze for separate t, but combine all colours and z-points 001 -check
			ImageStack ios= new ImageStack();
			for (int i=1;i<=Dim[4];i++) {// run through frames
				ImagePlus tmp = dp.run(OR,1,Dim[2],1,Dim[3],i,i);
				ImageStack tis =tmp.getImageStack();
				double minimum=tmp.getDisplayRangeMax();
				double maximum=tmp.getDisplayRangeMin();
				for (int j=1;j<=tis.getSize();j++) {
					ImageProcessor ip = tis.getProcessor(j);
					ImageStatistics tisis=ip.getStatistics();
					if (tisis.min<minimum) {
						minimum =tisis.min;
					}
					if (tisis.max>maximum) {
						maximum=tisis.max;
					}
				}
				tmp.setDisplayRange(minimum, maximum);
				IJ.run(tmp, "Apply LUT", "stack");
			
				for (int j=0;j<tis.getSize();j++) {
					ios.addSlice(tis.getProcessor(j+1));
				}
			}
			ImagePlus oOR=new ImagePlus("optimized",ios);
			oOR=HyperStackConverter.toHyperStack(oOR, Dim[2], Dim[3], Dim[4],"xyczt", "Color");
			oOR.setDisplayRange(0, Math.pow(2, oOR.getBitDepth()));
			return oOR;
		} else if (!CC&&DC&!TC) {
			// optimze for separate z, but combine all colours and t-points 010 //check
			ImageStack ios= new ImageStack();
			for (int i=1;i<=Dim[3];i++) {// run through z
				ImagePlus tmp = dp.run(OR,1,Dim[2],i,i,1,Dim[4]);
				ImageStack tis =tmp.getImageStack();
				double minimum=tmp.getDisplayRangeMax();
				double maximum=tmp.getDisplayRangeMin();
				for (int j=1;j<=tis.getSize();j++) {
					ImageProcessor ip = tis.getProcessor(j);
					ImageStatistics tisis=ip.getStatistics();
					if (tisis.min<minimum) {
						minimum =tisis.min;
					}
					if (tisis.max>maximum) {
						maximum=tisis.max;
					}
				}
				tmp.setDisplayRange(minimum, maximum);
				IJ.run(tmp, "Apply LUT", "stack");
				
				for (int j=0;j<tis.getSize();j++) {
					ios.addSlice(tis.getProcessor(j+1));
				}
			}
			ImagePlus oOR=new ImagePlus("optimized",ios);
			oOR=HyperStackConverter.toHyperStack(oOR, Dim[2], Dim[3], Dim[4],"xyctz", "Color");
			oOR.setDisplayRange(0, Math.pow(2, oOR.getBitDepth()));
			return oOR;
		} else { // optimize for all seperately //check
			//ImageStack ios= new ImageStack();
			ImagePlus tmp = dp.run(OR,1,Dim[2],1,Dim[3],1,Dim[4]);
			ImageStack tis =tmp.getImageStack();
			double minimum=tmp.getDisplayRangeMax();
			double maximum=tmp.getDisplayRangeMin();
			for (int j=1;j<=tis.getSize();j++) {
				ImageProcessor ip = tis.getProcessor(j);
				ImageStatistics tisis=ip.getStatistics();
				if (tisis.min<minimum) {
					minimum =tisis.min;
				}
				if (tisis.max>maximum) {
					maximum=tisis.max;
				}
			}
			tmp.setDisplayRange(minimum, maximum);
			IJ.run(tmp, "Apply LUT", "stack");
			tmp.setTitle("optimzed");
			tmp.setDisplayRange(0, Math.pow(2, tmp.getBitDepth()));
			return tmp;
		}
	}
	
	
	public static void main(String[] args) {
		//ImageJ IJ2 = new ImageJ();
    	//ImagePlus imp = IJ.openImage("http://wsr.imagej.net/images/FluorescentCells.zip"); //channels rest one dim
		//ImagePlus imp = IJ.openImage("http://wsr.imagej.net/images/confocal-series.zip"); //channels and z-stack
		ImagePlus imp = IJ.openImage("http://wsr.imagej.net/images/Spindly-GFP.zip");//channels, z and t
    	//IJ.run(imp, "RGB Stack", "");
    	imp.show();
    	MinMaxContrast mmc = new MinMaxContrast();
    	mmc.run("");
    }

}
