package LeidenUniv.Tools;
import ij.*;
import ij.gui.*;
import ij.plugin.*;
import java.util.*;

import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import mcib3d.image3d.legacy.*;
import mcib3d.geom.*;
import ij.measure.*;

/**
 * Counts the number of 3d objects over time
 * @author willemsejj
 *
 */
public class ObjectCounter4D_ implements PlugIn {
	
	/**
	 * To run it from ImageJ/FIJI
	 * If there is no image it will create a test image and run the plugin on that
	 */
	public void run(String arg) {
		if (WindowManager.getCurrentImage()==null){
			ImagePlus image = createTestImage();
			new ObjectCounter4D_ (image); // the constructor also runs the plugin
			image.show();			
		} else {		
			if (IJ.getImage().getProcessor().isBinary()){
				new ObjectCounter4D_ (IJ.getImage());	
			} else {
				if (IJ.getImage().getProcessor().createMask()==null){
					new WaitForUserDialog("Please set a threshold and apply it.\n \nIf a general threshold does not suit your image\nmake sure to try 'Auto Local Threshold'\nthis can be applied by clicking the\n>Image>Adjust>Auto Local Threshold button.\n \nMake sure to select one of the methods and not try all.\n \nAnd make sure the stack button is checked.").show();
				}
				//ImagePlus image = new ImagePlus("Thresholded", IJ.getImage().getProcessor().createMask());
				new ObjectCounter4D_ (IJ.getImage());	
			}
		}
	}

	/**
	 * @return used if there is no image open
	 * creates a 512x512 100 frames, 100 slices image with 10 random dots 
	 * Each dot moves at random 10% in any direction every timeframe
	 */
	public ImagePlus createTestImage(){
		ImagePlus image = IJ.createImage("HyperStack", "8-bit grayscale-mode", 512, 512, 1, 100, 100);
		//imp.show();
		for (int i=0;i<10;i++){ // number of balls
			int sx= (int)(Math.random()*500)+6;
			int sy= (int)(Math.random()*500)+6;
			int sz= (int)(Math.random()*90)+5;
			for (int k=0;k<100;k++){ // for the time frames
				Roi r = new OvalRoi(sx,sy,1,1);
				image.setPositionWithoutUpdate(1,sz-3,k+1);
				image.setRoi(r);
				IJ.run(image, "Fill", "slice");

				r = new OvalRoi(sx,sy,2,2);
				image.setPositionWithoutUpdate(1,sz-2,k+1);
				image.setRoi(r);
				IJ.run(image, "Fill", "slice");

				r = new OvalRoi(sx,sy,3,3);
				image.setPositionWithoutUpdate(1,sz-1,k+1);
				image.setRoi(r);
				IJ.run(image, "Fill", "slice");

				r = new OvalRoi(sx,sy,4,4);
				image.setPositionWithoutUpdate(1,sz,k+1);
				image.setRoi(r);
				IJ.run(image, "Fill", "slice");

				r = new OvalRoi(sx,sy,3,3);
				image.setPositionWithoutUpdate(1,sz+1,k+1);
				image.setRoi(r);
				IJ.run(image, "Fill", "slice");

				r = new OvalRoi(sx,sy,2,2);
				image.setPositionWithoutUpdate(1,sz+2,k+1);
				image.setRoi(r);
				IJ.run(image, "Fill", "slice");

				r = new OvalRoi(sx,sy,1,1);
				image.setPositionWithoutUpdate(1,sz+3,k+1);
				image.setRoi(r);
				IJ.run(image, "Fill", "slice");
				
				sx+=((int)(Math.random()*25))-12;
				sy+=((int)(Math.random()*25))-12;
				sz+=((int)(Math.random()*11))-5;
			}
		}
		IJ.run(image, "Properties...", "unit=pixel pixel_width=1.0000 pixel_height=1.0000 voxel_depth=5.12");
		return image;
	}

	/**
	 * @param imp Runs the counter on the given image
	 */
	public ObjectCounter4D_ (ImagePlus imp){
		ArrayList<Objects3DPopulation> output = getDots(imp);
		ResultsTable rt2 = new ResultsTable();
		ResultsTable rt3 = new ResultsTable();
		for (int i=0;i<output.size();i++){
			rt2.incrementCounter();
			rt2.addValue("Frame" ,i+1);
			rt2.addValue("# Objects",output.get(i).getNbObjects());
			rt3.incrementCounter();
			for (int j=0; j<output.get(i).getNbObjects();j++){
				rt3.addValue("Frame ", i+1);
				rt3.addValue("Object "+j+1+" X",output.get(i).getObject(j).getCenterX());
				rt3.addValue("Object "+j+1+" Y",output.get(i).getObject(j).getCenterY());
				rt3.addValue("Object "+j+1+" Z",output.get(i).getObject(j).getCenterZ());
				rt3.addValue("Object "+j+1+" Volume",output.get(i).getObject(j).getVolumeUnit());
			}
		}
		rt3.show("Details");
		rt2.show("Summary");	
	}

	public ObjectCounter4D_(){
	}

	/**
	 * @param In The image that needs to be analysed
	 * @return The Objects3DPopulation ArrayList (Used from the 3d suite from Thomas Boudier)
	 */
	public ArrayList<Objects3DPopulation> getDots(ImagePlus In){
		ArrayList<Objects3DPopulation> centroids = new ArrayList<Objects3DPopulation>();
		
		for (int l=0;l<In.getNFrames();l++){ // for the time frames
			In.setSlice(l*In.getNSlices()+1);
			In.killRoi();
			ImagePlus temp = new Duplicator().run(In, 1, 1, 1, In.getNSlices()+1, l+1, l+1);
			temp.setTitle("Time Frame "+l);
			ImageInt ii = ImageInt.wrap(temp);
			//ii.threshold(128,false,false);
			ImageLabeller label = new ImageLabeller();
			ImageInt ij = label.getLabels(ii);
			Objects3DPopulation o3d = new Objects3DPopulation(ij);
			centroids.add(o3d);
		}
		return centroids;
	}
	public static void main(String[] arg) {
		new ObjectCounter4D_().run("");
	}
}
