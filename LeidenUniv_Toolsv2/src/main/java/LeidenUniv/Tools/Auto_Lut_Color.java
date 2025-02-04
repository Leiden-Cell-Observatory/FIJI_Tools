package LeidenUniv.Tools;
import ij.*;
import ij.process.*;
import ij.plugin.*;

/**
 * Automatically adapts LUT based on the image histogram
 * @author willemsejj
 *
 */
public class Auto_Lut_Color implements PlugIn{
	
		/**
		 * To run the program in ImageJ/FIJI
		 */
		public void run(String arg){
			//IJ.log("\\Clear");
			ImagePlus OR = IJ.getImage();
			IJ.run(OR, "8-bit", "");
			int aa=OR.getWidth()*OR.getHeight();
			int [] H = new ImageHistogram().ImageHist(OR);
		
			byte [] r=new byte[256];
			byte [] b=new byte[256];

			byte [] g = new byte[256];
			int index=0;
			int sumInt=0;
			int prevI=0;
			for (int i=0;i<H.length;i++){
				//IJ.log("sumInt"+sumInt+", index: "+index+", i:"+i);
				if (sumInt >(aa/255)){
					//IJ.log("sumInt"+sumInt+", index: "+index+", i:"+ i+", floor:"+(Math.floor((sumInt*255)/aa)-1));
					for (int j=prevI;j<i+Math.floor((sumInt*255)/aa)-1;j++){

						r[j]=(byte)(index);
						g[j]=(byte)(255-index);
						b[j]=(byte)(255-Math.abs((255-index*2)));
					}
					prevI=i;
					index+=(Math.floor((sumInt*255)/aa))-1;
					index+=1;
					//IJ.log("index"+index+", i:"+i);
					sumInt=sumInt%(aa/255);
				} 
				sumInt+=H[i];
				
				//IJ.log(""+sumInt+", "+index);
			}
			//IJ.log(Arrays.toString(r));
			for (int j=prevI;j<256;j++){
				r[j]=(byte)(index);
				g[j]=(byte)(255-index);
				b[j]=(byte)(255-Math.abs((255-index*2)));
			}
			LUT AutoL=new LUT(r, g,b);
			ImageProcessor ip = OR.getProcessor();
			ip.setLut(AutoL);
			OR.updateAndDraw();
	}
}
