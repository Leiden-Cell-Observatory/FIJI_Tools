package LeidenUniv.Tools;

import fiji.stacks.Hyperstack_rearranger;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.WaitForUserDialog;
import ij.plugin.ChannelSplitter;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.PlugIn;
import ij.plugin.ZProjector;

/**
 * For subaveraging of stacks
 * @author willemsejj
 *
 */
public class Subaveraging_of_stacks implements PlugIn {
   public static int t;
   public int Sizeinframes;
   public int EndT;
   public int StartT;
   public int nZ;
   private String[] ch = new String[]{"frames", "slices", "channels"};
   ImagePlus imp;

   /**
 * runs the plugin with user interface
 */
   public void run(String var1) {
      this.nZ = 3;
      GenericDialog var2 = new GenericDialog("Stack Averaging");
      var2.addNumericField("Number of images to average ", (double)this.nZ, 0);
      var2.addChoice("Average over ", this.ch, this.ch[0]);
      var2.showDialog();
      this.nZ = (int)var2.getNextNumber();
      int var3 = var2.getNextChoiceIndex();
      if (this.nZ <= 1) {
         IJ.showMessage("Cannot average over less than 2 images");
         return;
      }
     this.imp = IJ.getImage();
     ImagePlus output = getAveragedStack(imp,var3);
     output.setTitle("Averaged Stack");
     output.show();
	}

	public ImagePlus getAveragedStack(ImagePlus imp, int dimension){
	int[] var4 = this.imp.getDimensions();
    int nF= var4[4];
    int nS = var4[3];
    int nC= var4[2];
    ImagePlus var12;
    if (dimension == 0) {// average over frames
    	if (nF<=1) {
    		IJ.showMessage("Cannot average over less than 2 images");
    		return null;
    	} else if (nF<nZ) {
    		IJ.showMessage("Cannot average over more images than the selected dimension has, reverting to max amount of frames");
    		nZ=nF;
    	}
       if (var4[3] == 1 && var4[2] == 1) {// in case of only time
          var12 = this.OneDimStackAv(this.imp, nF);
          var12.show();
       } else if (var4[3] == 1 && var4[2] > 1) {// in case of multiple channels
          var12 = this.MultiColorAv(this.imp, nF);
          var12 = Hyperstack_rearranger.reorderHyperstack(var12, "TCZ", false, true);
       } else if (var4[3] > 1 && var4[2] == 1) { // in case of multiple slices
          var12 = this.MultiZAv(this.imp, nF);
          var12.show();
       } else { //in case of multiple slices and channels
          Duplicator var6 = new Duplicator();
          ImagePlus var7 = var6.run(this.imp);
          ImagePlus[] var8 = ChannelSplitter.split(var7);
          ImagePlus[] var9 = new ImagePlus[var8.length];
          for(int var10 = 0; var10 < var8.length; ++var10) {
             var9[var10] = this.MultiZAv(var8[var10], nF);
          }
          Concatenator var13 = new Concatenator();
          ImagePlus var11 = var13.concatenate(var9, false);
          var11 = HyperStackConverter.toHyperStack(var11, var4[2], var4[3], var4[4] - this.nZ + 1, "xyztc", "Color");
          return var11;
       }
    } else if (dimension == 1) { // average over slices
    	if (nS<=1) {
    		IJ.showMessage("Cannot average over less than 2 images");
    		return null;
    	} else if (nS<nZ) {
    		IJ.showMessage("Cannot average over more images than the selected dimension has, reverting to max amount of slices");
    		nZ=nS;
    	}
    	if (var4[4] == 1 && var4[2] == 1) {// in case of only slices
            var12 = this.OneDimStackAv(this.imp, nS);
            return var12;
            
         } else if (var4[4] == 1 && var4[2] > 1) {// in case of multiple channels
            var12 = this.MultiColorAv(this.imp, nS);
            var12 = Hyperstack_rearranger.reorderHyperstack(var12, "TCZ", false, false);
            return var12;
         } else if (var4[4] > 1 && var4[2] == 1) { // in case of multiple frames
        	var12 = Hyperstack_rearranger.reorderHyperstack(imp, "TZC", false, false);
            var12 = this.MultiColorAv(var12, nS);
            var12 = Hyperstack_rearranger.reorderHyperstack(var12, "CZT", false, false);
            return var12;
         } else { //in case of multiple slices and channels
            Duplicator var6 = new Duplicator();
            ImagePlus var7 = var6.run(this.imp);
            ImagePlus[] var8 = ChannelSplitter.split(var7);
            ImagePlus[] var9 = new ImagePlus[var8.length];
            for(int var10 = 0; var10 < var8.length; ++var10) {
            	var12 = Hyperstack_rearranger.reorderHyperstack(var8[var10], "TZC", false, false);
                var12 = this.MultiColorAv(var12, nS);
                var9[var10] = Hyperstack_rearranger.reorderHyperstack(var12, "CZT", false, false);
            }
            Concatenator var13 = new Concatenator();
            ImagePlus var11 = var13.concatenate(var9, false);
            var11 = HyperStackConverter.toHyperStack(var11, var4[2], var4[3]-this.nZ+1, var4[4], "xyztc", "Color");
            return var11;
         }
    } else { //average over channels
    	if (nC<=1) {
    		IJ.showMessage("Cannot average over less than 2 images");
    		return null;
    	}else if (nC<nZ) {
    		IJ.showMessage("Cannot average over more images than the selected dimension has, reverting to max amount of channels");
    		nZ=nC;
    	}
    	if (var4[4] == 1 && var4[3] == 1) {// in case of only channels
    		var12 = Hyperstack_rearranger.reorderHyperstack(this.imp, "ZCT", false, false);
            var12 = this.OneDimStackAv(var12, nC);
            var12 = Hyperstack_rearranger.reorderHyperstack(var12, "ZCT", false, false);
            return var12;
            
         } else if (var4[4] == 1 && var4[3] > 1) {// in case of multiple slices
        	var12 = Hyperstack_rearranger.reorderHyperstack(this.imp, "ZCT", false, false);
            var12 = this.MultiColorAv(var12, nC);
            var12 = Hyperstack_rearranger.reorderHyperstack(var12, "CTZ", false, false);
            return var12;
         } else if (var4[4] > 1 && var4[3] == 1) { // in case of multiple frames
        	var12 = Hyperstack_rearranger.reorderHyperstack(imp, "TCZ", false, false);
            var12 = this.MultiColorAv(var12, nC);
            return var12;
         } else { //in case of multiple slices and channels
            var12 = Hyperstack_rearranger.reorderHyperstack(this.imp, "ZCT", false, false);
            ImagePlus[] var8 = ChannelSplitter.split(var12);
            //each layer of var8 contains a slice with multiple c and t, where c==>z
            ImagePlus[] var9 = new ImagePlus[var8.length];
            for(int var10 = 0; var10 < var8.length; ++var10) {
            	var12 = Hyperstack_rearranger.reorderHyperstack(var8[var10], "TZC", false, false);
                var9[var10] = this.MultiColorAv(var12, nC);
                //var9[var10] = Hyperstack_rearranger.reorderHyperstack(var12, "CZT", false, false);
            }
            Concatenator var13 = new Concatenator();
            ImagePlus var11 = var13.concatenate(var9, false);
            var11 = HyperStackConverter.toHyperStack(var11, var4[2]-this.nZ+1, var4[3], var4[4], "xytzc", "Color");
            return var11;
         }
    }
    return var12;
  }

	   /** T-averaging of multiple Z's, Slices get transformed to channels averaged and converted back
	 * @param var1 Image to average
	 * @param var2 number of slices to average
	 * @return new imageplus with the averaged images
	 */
	public ImagePlus MultiZAv(ImagePlus var1, int var2) {
      new ImageStack(var1.getWidth(), var1.getHeight());
      Duplicator var4 = new Duplicator();
      ImagePlus var5 = var4.run(var1);
      var5 = Hyperstack_rearranger.reorderHyperstack(var5, "ZCT", false, false);
      var5 = this.MultiColorAv(var5, var2);
      var5 = Hyperstack_rearranger.reorderHyperstack(var5, "CTZ", false, false);
      return var5;
   }

	   /** Channel-averaging one channel at a time
	 * @param var1 Image to average
	 * @param var2 number of channels to average
	 * @return new imageplus with the averaged images
	 */
	public ImagePlus MultiColorAv(ImagePlus var1, int var2) {
      Duplicator var3 = new Duplicator();
      ImagePlus var4 = var3.run(var1);
      ImagePlus[] var5 = ChannelSplitter.split(var4);
      ImagePlus[] var6 = new ImagePlus[var5.length];

      for(int var7 = 0; var7 < var5.length; ++var7) {
         var6[var7] = this.OneDimStackAv(var5[var7], var2);
      }

      Concatenator var9 = new Concatenator();
      ImagePlus var8 = var9.concatenate(var6, false);
      return var8;
   }

	   /** Simple z-stack averaging
	 * @param var1 Image to average
	 * @param var2 number of slices to average
	 * @return new imageplus with the averaged images
	 */
	public ImagePlus OneDimStackAv(ImagePlus var1, int var2) {
      ImageStack var3 = new ImageStack(var1.getWidth(), var1.getHeight());
      ImagePlus var4;
      for(t = 0; t <= var2 - this.nZ; t++) {
         this.StartT = t;
         this.EndT = t + this.nZ;
         var4 = ZProjector.run(var1, "avg", this.StartT, this.EndT);
         var3.addSlice(var4.getProcessor());
      }

      var4 = new ImagePlus("Averaged stack", var3);
      return var4;
	}

	/** For testing purposes
	 * @param args String of arguments, not used in this case
	 */
	public static void main(String[] args) {
		//ImageJ ij = new ImageJ();
		ImagePlus imp = IJ.openImage("http://imagej.nih.gov/ij/images/Spindly-GFP.zip");
		imp.show();
		WaitForUserDialog wfd = new WaitForUserDialog("please duplicate the correct dimension to test");
		wfd.show();
		imp = IJ.getImage();
		new Subaveraging_of_stacks().run("");
	}
}
