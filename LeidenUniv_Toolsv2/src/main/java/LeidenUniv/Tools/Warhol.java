package LeidenUniv.Tools;

import java.io.File;
import java.util.ArrayList;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.Duplicator;
import ij.plugin.LutLoader;
import ij.plugin.MontageMaker;
import ij.plugin.PlugIn;

public class Warhol implements PlugIn{

	@Override
	public void run(String arg0) {
		// This gets all available luts
		ImagePlus imp = IJ.getImage();
		if (imp==null) {
			IJ.showMessage("Image needed");
			return;
		}
		ArrayList<String> lutlist = getLuts(arg0);
		int[] choices = new int[] {3,4,6,9,16,25,36};
		int nImages = choices[getChoice()];
		
		if (nImages>lutlist.size()) {
			if (lutlist.size()<36) {
				nImages=25;
			}
			if (lutlist.size()<25) {
				nImages=16;
			}
			if (lutlist.size()<16) {
				nImages=9;
			}
			if (lutlist.size()<9) {
				nImages=6;
			}
			if (lutlist.size()<6) {
				nImages=4;
			}

			IJ.showMessage("Not so many luts available, continuing with " +nImages + " images");
		}
		//IJ.log(""+nImages);
		ImagePlus wh = makeWarhol(imp,lutlist,nImages);
		wh.show();
	}
	
	public ImagePlus makeWarhol (ImagePlus imp, ArrayList<String> luts, int n) {
		IJ.run(imp, "8-bit", "");
		LutLoader ll = new LutLoader();
		ImagePlus [] imps = new ImagePlus[n];
		imps[0]=imp;
		int lutchoice = (int)Math.random()*luts.size();
		ll.run(luts.get(lutchoice));
		imp.hide();
		IJ.run(imp, "RGB Color", "");
		luts.remove(lutchoice);
		Duplicator dp = new Duplicator();
		for (int i=0;i<n;i++) {
			ImagePlus temp =dp.run(imp,1,1,1,1,1,1);
			temp.show();
			IJ.run(temp, "8-bit", "");
			lutchoice = (int)Math.random()*luts.size();
			ll.run(luts.get(lutchoice));
			temp.hide();
			IJ.run(temp, "RGB Color", "");
			luts.remove(lutchoice);
			imps[i]=temp;
		}
		ImageStack is= ImageStack.create(imps);
		ImagePlus array = new ImagePlus("array", is);
		//array.show();
		MontageMaker mm = new MontageMaker();
		ImagePlus wh;
		if (n==3) {
			if (array.getWidth()>array.getHeight()) {
				wh =mm.makeMontage2(array,1,3,1,1,n,1,0,false);
			} else {
				wh =mm.makeMontage2(array,3,1,1,1,n,1,0,false);
			}
		} else if (n==4) {
			wh =mm.makeMontage2(array,2,2,1,1,n,1,0,false);
		} else if (n==6) {
			if (array.getWidth()>array.getHeight()) {
				wh =mm.makeMontage2(array,2,3,1,1,n,1,0,false);
			} else {
				wh =mm.makeMontage2(array,3,2,1,1,n,1,0,false);
			}
		} else if (n==9){
			wh =mm.makeMontage2(array,3,3,1,1,n,1,0,false);
		} else if (n==16) {
			wh =mm.makeMontage2(array,4,4,1,1,n,1,0,false);
		} else if (n==25) {
			wh =mm.makeMontage2(array,5,5,1,1,n,1,0,false);
		} else {
			wh =mm.makeMontage2(array,6,6,1,1,n,1,0,false);
		}
		
		return wh;
		// we now have an imageplus array, we need to make a composite out of this
	}
	
	public int getChoice() {
		GenericDialog gd = new GenericDialog("Warhol settings");
		gd.addChoice ("Number of images", new String[] {"3","4","6","9","16","25","36"}, "4");
		gd.showDialog();
		return gd.getNextChoiceIndex();
	}
	
	public ArrayList<String> getLuts(String arg0){
		String lutdir = IJ.getDirectory("luts");
		//IJ.log(lutdir);
		ArrayList<String> lutlist = new ArrayList<String>();
		if (lutdir!=null) {
			File lutd = new File(lutdir);
			//IJ.log(lutd.toString());
			String [] luts= lutd.list();
			if (luts!=null) {
				for (int i=0;i<luts.length;i++) {
					if (luts[i].endsWith("lut")) {
						String temp = lutdir+luts[i].toString();
						lutlist.add(temp);
					}
				}
			}
		}
		lutlist.add("fire");
		lutlist.add("ice");
		lutlist.add("spectrum");
		lutlist.add("3-3-2 RGB");
		lutlist.add("redgreen");
		return lutlist;
	}
	
	public static void main (String[] args) {
		ImageJ ij = new ImageJ();
		ImagePlus imp = IJ.openImage("http://imagej.nih.gov/ij/images/boats.gif");
		imp.show();
		Warhol wh = new Warhol();
		wh.run("F:\\fiji-win64\\Fiji.app Programming LeidenUniv\\luts");
	}
}
