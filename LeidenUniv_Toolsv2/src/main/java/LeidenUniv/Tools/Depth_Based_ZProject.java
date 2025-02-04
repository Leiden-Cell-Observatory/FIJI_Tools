package LeidenUniv.Tools;
import ij.*;
import ij.gui.GenericDialog;
import ij.measure.Measurements;
import ij.plugin.*;
import ij.process.ImageStatistics;

/**
 * 
 * Plugin for creating a depth encoded z-projection where the intensity relates to the depth in the image
 * @author willemsejj
 */
public class Depth_Based_ZProject implements PlugIn {

	/**
	 * To run the plugin in ImageJ/FIJI
	 * An image needs to be open to run it
	 */
	public void run(String arg) {
		ImagePlus imp = IJ.getImage();
		GenericDialog gd = new GenericDialog("Settings");
    	gd.addChoice("Mode", new String[] {"Depth","Intensity"},"Depth");
    	gd.showDialog();
    	int MC=gd.getNextChoiceIndex();
		imp.hide();
		if (imp.getBitDepth()==24){
			IJ.showMessage("This tool is not made for RGB images");
			return;
		}
		if (imp.getBitDepth()==16){
			IJ.showMessage("This tool is made for 8-bit images");
			return;
		}
		int[] dim = imp.getDimensions();
		if (dim[3]<2){
			IJ.showMessage("This tool made for z-stacks");
			return;
		}
		if (dim[2]>1){
			IJ.showMessage("This tool is not made for multi channel images");
			return;
		}
		
		
		
		ImagePlus result = IJ.createHyperStack("Color projection",dim[0],dim[1],3,1,dim[4],imp.getBitDepth());
		int [][] slices =new int[dim[0]][dim[1]];
		int [][] vals = new int[dim[0]][dim[1]];
		int [][] valsR = new int[dim[0]][dim[1]];
		int [][] valsG = new int[dim[0]][dim[1]];
		int [][] valsB = new int[dim[0]][dim[1]];

		
		double [][] colDist=new double [3][dim[3]];// these are for setting how much color each slice gets, this is based on linear distribution over the entire stack
		//coldist linear
		if (MC==0) {
			for (int i=0;i<dim[3]/2;i++){ // first half of the coldist values
				colDist[0][i]=255-(i/(((double)dim[3]-1)/2))*255; //red starts at max and decreases till 0 halfway
				colDist[1][i]=i/(((double)dim[3]-1)/2)*255; // green increased from 0 to 255
				colDist[2][i]=0; //blue is 0 in the top half
			}
			for (int i=dim[3]/2;i<dim[3];i++){// is 2nd half of coldist values
				colDist[0][i]=0; //red is 0 in the bottom half
				colDist[1][i]=255-Math.max((i-(((double)dim[3]-1)/2))/((((double)dim[3]-1)-((double)dim[3]-1)/2))*255,0); // green decreases to 0 in the 2nd half
				colDist[2][i]=Math.max((i-((double)dim[3]-1)/2)/((((double)dim[3]-1)-((double)dim[3]-1)/2))*255,0); // blue increases in the 2nd half
			}
		} else {
			//coldist intensity linear
			
			//first get intensity sum of all layers
			double [] densities = new double[dim[3]];
			double totDens=0;
			for (int i=0; i<dim[3];i++) {
				imp.setPositionWithoutUpdate(1,i+1,1); // only based on first time frame
				IJ.run(imp, "Select All", "");
				ImageStatistics is = imp.getStatistics(Measurements.AREA+Measurements.INTEGRATED_DENSITY);
				densities[i]=totDens+is.mean*is.area;
				totDens+=is.mean*is.area;
			}
			// get the frame where to switch
			int switchint=0;
			for (int i=0;i<dim[3];i++) {
				if (densities[i]>totDens/2) {
					switchint=i;
					break;
				}
			}
			
			for (int i=0;i<switchint;i++){ // first half of the coldist values
				colDist[0][i]=((totDens-(densities[i]*2))/totDens)*255;
				colDist[1][i]=(densities[i]/totDens*2)*255;
				colDist[2][i]=0; //blue is 0 in the top half
			}
			for (int i=switchint;i<dim[3];i++){// is 2nd half of coldist values
				colDist[0][i]=0; //red is 0 in the bottom half
				colDist[1][i]=(2-densities[i]*2/totDens)*255;// green decreases to 0 in the 2nd half
				colDist[2][i]=((densities[i]*2-totDens)/totDens)*255;// blue increases in the 2nd half
			}
		}
		//IJ.log(Arrays.toString(colDist[0]));
		for (int l=0;l<dim[4];l++){ //run through time
			for (int i=0;i<dim[0];i++){ // set initial values to 0
				for (int j=0;j<dim[1];j++){
					slices[i][j]=0;
					vals[i][j]=0;
				}
			}
			for (int m=0;m<dim[3];m++){// run through z-stack
				imp.setPositionWithoutUpdate(1,m+1,l+1);

				for (int i=0;i<dim[0];i++){
					for (int j=0;j<dim[1];j++){
						int cVal=imp.getPixel(i,j)[0];
						
						if (cVal>vals[i][j]){ // this happens if a non zero value is found, or if the value is higher than a previous one. Basically a max projection
							
							vals[i][j]=cVal;
							slices[i][j]=m;
							valsR[i][j]=(int)(colDist[0][m]/255.0*(double)cVal);
							valsG[i][j]=(int)(colDist[1][m]/255.0*(double)cVal);
							valsB[i][j]=(int)(colDist[2][m]/255.0*(double)cVal);
							if (m==3&&l==1&&i==100){
								IJ.log("m"+m);
								IJ.log("cval"+cVal);
								IJ.log("colDistG"+colDist[1][m]);
								IJ.log("colDistB"+colDist[2][m]);
								IJ.log(""+colDist[1][m]/255);
								IJ.log(""+colDist[2][m]/255);
								IJ.log(""+(int)(colDist[0][m]/255.0*(double)cVal));
								IJ.log(""+(int)(colDist[1][m]/255.0*(double)cVal));
								IJ.log(""+(int)(colDist[2][m]/255.0*(double)cVal));
								IJ.log("j "+j);
							}
						}
					}
				}
			}
			// here we put the maxSlice number in the new image
			result.setPositionWithoutUpdate(1,1,l+1);
			result.getProcessor().setIntArray(valsR);
			result.setPositionWithoutUpdate(2,1,l+1);
			result.getProcessor().setIntArray(valsG);
			result.setPositionWithoutUpdate(3,1,l+1);
			result.getProcessor().setIntArray(valsB);
		}
		result.setPosition(1,1,1);
		IJ.resetMinAndMax(result);
		result.setPosition(2,1,1);
		IJ.resetMinAndMax(result);
		result.setPosition(3,1,1);
		IJ.resetMinAndMax(result);
		IJ.run(result, "RGB Color", "");
		result.show();
		
		//imp.show();
	}
}

