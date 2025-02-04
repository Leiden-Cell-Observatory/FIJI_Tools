package LeidenUniv.Tools;
import ij.*;
import ij.process.*;

/**
 * get the imae histogram as int array
 * @author willemsejj
 *
 */
public class ImageHistogram {

	/**
	 * @param imp The image to get the histogram from
	 * @return the int[] holding the histogram
	 */
	public int [] ImageHist (ImagePlus imp){
		int HistSize=(int)Math.pow(2,imp.getBitDepth()); //create array of possible grey values
		ImageProcessor ip=imp.getProcessor();
		int[] H=new int[HistSize];
		H=ip.getHistogram();
		return H;
	}
}