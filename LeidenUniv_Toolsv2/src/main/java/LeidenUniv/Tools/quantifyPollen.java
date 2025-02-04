package LeidenUniv.Tools;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.RoiEnlarger;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;

public class quantifyPollen implements PlugIn{

	@Override
	public void run(String arg0) {
		ImagePlus imp = IJ.getImage(); //start with current image
		int cores = Runtime.getRuntime().availableProcessors(); // get the amount of processors
		String filename = imp.getTitle(); // for data storage later on
		String model = getModelLocation(); // find the classifier for the data
		ImagePlus imp2 = new Duplicator().run(imp, 2, 2, 1, 1, 1, 1); // duplicate the transmitted light channel
		ImagePlus fluo = new Duplicator().run(imp, 1, 1, 1, 1, 1, 1); // duplicate the transmitted light channel
		ImagePlus imp3 = getWekaProbability.getProbability(imp2,model,cores); // get the probability maps
		//imp3.show(); // show the maps (not needed)
		ImagePlus abp = new Duplicator().run(imp3, 1, 1, 1, 1, 1, 1);
		ImagePlus pt = new Duplicator().run(imp3, 2, 2, 1, 1, 1, 1);
		ImagePlus pc = new Duplicator().run(imp3, 3, 3, 1, 1, 1, 1);
		ImagePlus pw = new Duplicator().run(imp3, 4, 4, 1, 1, 1, 1);
		ImagePlus bck = new Duplicator().run(imp3, 5, 5, 1, 1, 1, 1);
		ImagePlus dst = new Duplicator().run(imp3, 6, 6, 1, 1, 1, 1);
		
		// For the abortedPollen
		IJ.run(abp, "Gaussian Blur...", "sigma=1");
		IJ.setThreshold(abp, 0.6000, 1000000000000000000000000000000.0000);
		IJ.run(abp, "Convert to Mask", "");

		
		// For the Pollencenter
		IJ.run(pc, "Gaussian Blur...", "sigma=1");
		IJ.setThreshold(pc, 0.3000, 1000000000000000000000000000000.0000);
		IJ.run(pc, "Convert to Mask", "");
		// now we need to substract the pollenwalls
		
		// For the Pollencenter
		IJ.run(pw, "Gaussian Blur...", "sigma=1");
		IJ.setThreshold(pw, 0.3000, 1000000000000000000000000000000.0000);
		IJ.run(pw, "Convert to Mask", "");
		
		//seperate the pollencenters by substracting the pollenwall		
		ImagePlus pcs = ImageCalculator.run(pc, pw, "Subtract create");
		//IJ.run(pcs, "Analyze Particles...", "display exclude add");		
		
		ResultsTable rt = ResultsTable.getResultsTable();
		if (rt==null){
			rt = new ResultsTable();
		}
		fluo.setTitle("fluo");
		IJ.run("Set Measurements...", "area mean integrated redirect=None decimal=2");
		ResultsTable rt2 = new ResultsTable();
		RoiManager rpc = new RoiManager();
		ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.SHOW_NONE+ParticleAnalyzer.ADD_TO_MANAGER+ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES,Measurements.AREA+Measurements.INTEGRATED_DENSITY,rt2,30.0,Double.MAX_VALUE);
		ParticleAnalyzer.setRoiManager(rpc);
		pa.analyze(pcs);
//		rpc.runCommand(fluo,"Select All");
		//rpc.runCommand(fluo,"Measure");
		Roi[] rois = rpc.getRoisAsArray();
		for (int i =0 ;i<rois.length;i++) {
			Roi cRoi = rois[i];
			RoiEnlarger.enlarge(cRoi, 3);
			IJ.run(fluo, "Measure", "");
		}

		
		//ResultsTable rtf=rpc.multiMeasure(fluo);
		//rtf.show("Intensities");
		rt.show("Results");
		
		// For the Pollentubes
		IJ.run(pt, "Gaussian Blur...", "sigma=1");
		IJ.setThreshold(pt, 0.8000, 1000000000000000000000000000000.0000);
		IJ.run(pt, "Convert to Mask", "");
		IJ.run(pt, "Close-", "");
		
		ResultsTable rt3 = new ResultsTable();
		IJ.run("Set Measurements...", "area mean integrated redirect=None decimal=2");
		ParticleAnalyzer pa2 = new ParticleAnalyzer(ParticleAnalyzer.SHOW_MASKS+ParticleAnalyzer.ADD_TO_MANAGER,Measurements.AREA,rt3,30.0,Double.MAX_VALUE);
		RoiManager rm = new RoiManager();
		pa2.analyze(pt);
		ImagePlus ptmask=pa2.getOutputImage();
		
		//MaximumFinder mf = new MaximumFinder();
		ResultsTable rt8 = new ResultsTable();
		
		/* maximumfinder does not work. try another way with particle analyzer
		MaximumFinder mf = new MaximumFinder();
		mf.findMaxima(abp.getProcessor(),10,MaximumFinder.COUNT,true);
		mf.findMaxima(pcs.getProcessor(),10,MaximumFinder.COUNT,true);
		mf.findMaxima(ptmask.getProcessor(),10,MaximumFinder.COUNT,true);
		//IJ.run(abp, "Find Maxima...", "prominence=10 output=Count");
		//IJ.run(pcs, "Find Maxima...", "prominence=10 output=Count");
		//IJ.run(ptmask, "Find Maxima...", "prominence=10 output=Count");8?

		 */
		
		ResultsTable rt4 = new ResultsTable();
		ParticleAnalyzer pa4 = new ParticleAnalyzer(ParticleAnalyzer.SHOW_NONE,Measurements.AREA,rt4,30.0,Double.MAX_VALUE);
		pa4.analyze(abp);
		
		ResultsTable rt6 = new ResultsTable();
		ParticleAnalyzer pa6 = new ParticleAnalyzer(ParticleAnalyzer.SHOW_NONE,Measurements.AREA,rt6,30.0,Double.MAX_VALUE);
		pa6.analyze(ptmask);
		
		rt8.incrementCounter();
		rt8.addValue("aborted pollen", rt4.getCounter());
		rt8.addValue("normal pollen", rt2.getCounter());
		rt8.addValue("germinated pollen", rt6.getCounter());
		
		rt8.show("Result data");
		
		
	}
	public static void main (String [] args) {
		ImagePlus imp = IJ.openImage("F:/temp/#13-5003.nd2");//channels, z and t
		imp.show();
		quantifyPollen qp = new quantifyPollen();
		qp.run("");
		
	}
	
	public String getModelLocation() {
		JFileChooser chooser = new JFileChooser();
		if (IJ.getDirectory("image")!=null) {
			chooser.setCurrentDirectory(new File(IJ.getDirectory("current")));
		}
		FileNameExtensionFilter filter = new FileNameExtensionFilter("model files", "model");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(chooser);
	    
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       // get the right values
	       String filename = chooser.getSelectedFile().getPath();
	       return filename;
	     } else {
	    	 IJ.showMessage("No file selected");
	    	 return null;
	     }
	}
}
