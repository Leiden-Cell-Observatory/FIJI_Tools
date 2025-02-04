package LeidenUniv.Tools;
import ij.*;
import ij.gui.*;
import ij.plugin.*;

/**
 * 
 * Plugin for drawing in an overlay element from current frame till the last one
 * Useful for making movies with arrows
 * @author willemsejj
 */
public class drawTillEnd_ implements PlugIn {
	ImagePlus imp;
	int[] Dim = new int[5];
	int Ch,Sl,Fr;
	int cCh,cSl,cFr;
	int[] Pos=new int[3];

	/**
	 * not used in this plugin
	 */
	public void run(String arg){
	}
	
	/**
	 * this can be called from a plugin
	 * @param im the image on which this plugin is run
	 * 
	 */
	public drawTillEnd_(ImagePlus im){
		imp=im;
		Dim=imp.getDimensions();
		Ch=Dim[2];
		Sl=Dim[3];
		Fr=Dim[4];
	}

	/**
	 * Normal constructor used when the plugin is activated
	 */
	public drawTillEnd_(){
		imp = IJ.getImage();
		Dim=imp.getDimensions();
		Ch=Dim[2];
		Sl=Dim[3];
		Fr=Dim[4];
		GenericDialog gd = new GenericDialog("Draw Till End");
		String [] Choices={"Channels","Slices","Frames"};
		gd.addChoice("Draw on subsequent:",Choices,Choices[2]);
		gd.showDialog();
		String Chosen=gd.getNextChoice();
		if (Chosen=="Channels"){
			Draw_Channels();
		} else if (Chosen=="Slices"){
			Draw_Slices();
		} else {	
			Draw_Frames();
		}
	}

	/**
	 * Draws the current overlay on all subsequent channels
	 */
	public void Draw_Channels() {
		int temp= imp.getCurrentSlice();
		Pos=imp.convertIndexToPosition(temp);
		for (int t=Pos[0];t<=Ch;t++){
			imp.setPosition(t,Pos[1],Pos[2]);
			IJ.run(imp, "Draw", "slice");
		}	
	}

	/**
	 * Draws the current overlay on all subsequent frames
	 */
	public void Draw_Frames() {
		int temp= imp.getCurrentSlice();
		Pos=imp.convertIndexToPosition(temp);
		for (int t=Pos[2];t<=Fr;t++){
			imp.setPosition(Pos[0],Pos[1],t);
			IJ.run(imp, "Draw", "slice");
		}	
	}

	/**
	 * Draws the current overlay on all subsequent slices in the Z-stack
	 */
	public void Draw_Slices() {
		int temp= imp.getCurrentSlice();
		Pos=imp.convertIndexToPosition(temp);
		for (int t=Pos[1];t<=Sl;t++){
			imp.setPosition(Pos[0],t,Pos[2]);
			IJ.run(imp, "Draw", "slice");
		}	
	}
}
