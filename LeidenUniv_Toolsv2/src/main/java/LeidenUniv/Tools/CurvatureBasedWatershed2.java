package LeidenUniv.Tools;

import java.awt.Point;
import ij.plugin.filter.EDM;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.Color;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import ij.process.ImageStatistics; //livia

public class CurvatureBasedWatershed2 implements PlugIn {
	private int negativeCurvatureRegionCount = 0;
	private double point0Curvature = Double.NaN;
	private double point2Curvature = Double.NaN;

	public static void main(String[] args) {
		// this part is just for testing
		System.out.println("starting");
		ImageJ ij = new ImageJ();
		ImagePlus imp = IJ.openImage("http://imagej.net/images/blobs.gif");
		
		
		ImagePlus imp2 = new Duplicator().run(imp);
		//#TODO add new image and test
		imp.show();
		//rm.runCommand("open", "E:/Temp/curves/RoiSet_comparison.zip"); //open specific roiset
		CurvatureBasedWatershed2 cbw = new CurvatureBasedWatershed2();
		
		Roi [] rois = cbw.getRois(imp);			
		double [] means = cbw.getMeans(imp2, rois);
		cbw.createResultsTable(means);
		
		cbw.run("15");
	}
	public double [] getMeans(ImagePlus imp2, Roi[] rois) {
		double [] means=new double[rois.length];
		for (int i=0;i<rois.length;i++) {
			double mean=calculateMeanIntensity(imp2, rois[i]); //going into your method
			//System.out.println(mean);
			means[i]=mean;
		}
		//System.out.println(Arrays.toString(means));
		return means;
	}
	
	public Roi [] getRois(ImagePlus imp) {
		RoiManager rm =RoiManager.getInstance2();
		if (rm==null) {
			rm = new RoiManager();
		} else {
			rm.reset();
		}
		IJ.setAutoThreshold(imp, "Default");
		IJ.run(imp, "Convert to Mask", "");
		IJ.run(imp, "Analyze Particles...", "add");
		// ends here
		return rm.getRoisAsArray();
	}
	
	@Override
	public void run(String arg0) {
	    int curveregion =5;

	    ResultsTable rt = new ResultsTable();
	    ImagePlus imp = IJ.getImage();

	    ImagePlus distanceIntensity = createDistanceIntensity(imp);//intensity part
	    distanceIntensity.show();
	    if (imp != null) {
	        ImageProcessor ip = imp.getProcessor();
	        if (!ip.isBinary()) {
	            IJ.error("8-bit binary image (0 and 255) required.");
	            return;
	        }
	        ip.resetRoi();
	        //processing routines assume background=0; image may be otherwise
	    }

	    Roi cRoi=imp.getRoi();
	    RoiManager rm = RoiManager.getInstance2();
	    Local_Curvature lc = new Local_Curvature();
	    //lc.run("5");

	    if (cRoi==null&&(rm==null||rm.getCount()==0)){
	        IJ.showMessage("ROI needed");
	    } else {
	        // we create the output image for displaying the curvatures
	        ImagePlus imp3 = IJ.createImage("Curvatures", "32-bit black", imp.getWidth(), imp.getHeight(), imp.getNSlices());
	        // get the floatprocessor of the new image so we can put values
	        FloatProcessor fp3 =(FloatProcessor) imp3.getProcessor();

	        // create the Roi array to work with
	        Roi [] rs;

	        if (rm!=null && rm.getCount()>0) {
	            rs = rm.getRoisAsArray();
	        } else {
	            rs = new Roi[1];
	            rs[0]=cRoi;
	        }

	        // run through all rois, which is one in length if the roimanager did not contain anything
	        //for (int i=0;i<rs.length;i++){ // this is where the calculations are done for 07/10/26...all rois
	            //System.out.println("Roi "+(i+1));

	     // Process all ROIs
	        for (int i = 0; i < rs.length; i++) {
	        
	        //for (int i=0;i<rs.length;i++){ //we are checking roi 33 only for now

	            //if (i != 32 && i != 26 && i != 30 && i != 15 && i != 37 && i != 22  ) {
	            	//&& i != 59 && i != 52  ....if i wanna check for more
	                //continue;
	            

	            System.out.println("Roi "+(i+1));

	            double meanIntensity = calculateMeanIntensity(imp, rs[i]);//Livia... here we get a variable
	            rt.incrementCounter(); // livia- make the table so that i can see it in fiji
	            rt.addValue("ROI #", i+1);
	            rt.addValue("Mean Intensity", meanIntensity);

	            FloatPolygon fp=rs[i].getInterpolatedPolygon(1,true);
	            // here we need to calculate the curvature
	            double [] curves = lc.calculateLocal3PointCurvature(
	                    fp.xpoints, fp.ypoints, curveregion, fp
	            );

	            //count-ve regions....
	            Point [] ps = getTwoLowestPointsInNegativeRegions(
	                    curves, fp, -0.02, 5, rs[i]
	            );

	            //for (int j = 0; j < ps.length; j++) { //divide by 2
	               // System.out.println("Point " + j + ": x=" + ps[j].x + " y=" + ps[j].y);
	            //}
	            //System.out.println("Number of selected points: " + ps.length);

	            System.out.println("Number of negative-curvature regions: "
	                    + negativeCurvatureRegionCount);

	            if (ps.length >= 2) {
	                System.out.println("Point 0: x=" + ps[0].x
	                        + " y=" + ps[0].y
	                        + " curvature=" + point0Curvature);

	                System.out.println("Point 2: x=" + ps[1].x
	                        + " y=" + ps[1].y
	                        + " curvature=" + point2Curvature);
	            
	            //temp fix for pt 2 =====are original coordinates even inside roi?
	            System.out.println("Point 2 inside ROI: "
	                    + rs[i].contains(ps[1].x, ps[1].y));
	            }
//temp fix ends here
	            if (ps.length >= 2) {

	                // calculate the direction from the lowest point in region 1
	                // to the lowest point in region 2
	                double alpha = calculateAlpha(ps[0], ps[1]);

	                System.out.println("Initial alpha from Point 0 to Point 2: " + alpha);

	                System.out.println("About to call connectPoints");

	                // Connect the two endpoints
	                ArrayList<Point> path = connectPoints(
	                        ps[0], ps[1], alpha, distanceIntensity, rs[i]);

	             // make the graph for ROI 27 only
	               // ArrayList<Point> refinedPath = new ArrayList<Point>();

	              //  if (i == 26) {
	                 //   System.out.println("Building graph for ROI " + (i + 1));
	                    //double[][] graph = makeGraph(path, distanceIntensity);

	                    // Find a lower intensity route using Dijkstra
	                   // refinedPath = dijkstra(graph, path);
	             //   }
	                
	               
	                
	                
	             // Make the graph for the all the rois
	                ArrayList<Point> refinedPath = new ArrayList<Point>();

	                System.out.println("Building graph for ROI " + (i + 1));

	                if (!path.isEmpty()) {
	                    double[][] graph = makeGraph(path, distanceIntensity);

	                    // Find a lower intensity route using Dijkstra
	                    refinedPath = dijkstra(graph, path);
	                }
	                //start of visual diagnostic for points, delete later 18/11
	                
	             // Check if the points in the refined path are connected properly.
	                // We are checking ROI 27 because the path looks clumped in the image,
	                // even though Dijkstra reduces the number of points from 25 to 18.
	                if (i == 26 && !refinedPath.isEmpty()) {

	                    // First check the path in the order returned by Dijkstra.
	                    // The distance between consecutive points should be 1 or 1.414
	                    // if they are connected horizontally, vertically or diagonally.
	                    System.out.println("Path from first point to last:");

	                    for (int j = 0; j < refinedPath.size(); j++) {
	                        Point p = refinedPath.get(j);

	                        System.out.println("Point " + j + ": x=" + p.x + " y=" + p.y);

	                        // Check the distance between the current point and the previous one.
	                        // This helps us see if there are any gaps in the path.
	                        if (j > 0) {
	                            Point previous = refinedPath.get(j - 1);

	                            double distance = Math.sqrt(
	                                    Math.pow(p.x - previous.x, 2)
	                                    + Math.pow(p.y - previous.y, 2)
	                            );

	                            System.out.println("Distance from previous point: " + distance);
	                        }
	                    }

	                    // Now check the same path in the opposite direction.
	                    // This lets us compare the order of the points from both ends.
	                    System.out.println("Path from last point to first:");

	                    for (int j = refinedPath.size() - 1; j >= 0; j--) {
	                        Point p = refinedPath.get(j);

	                        System.out.println("Point " + j + ": x=" + p.x + " y=" + p.y);

	                        // Check the distance between this point and the next one
	                        // in the original path.
	                        if (j < refinedPath.size() - 1) {
	                            Point next = refinedPath.get(j + 1);

	                            double distance = Math.sqrt(
	                                    Math.pow(p.x - next.x, 2)
	                                    + Math.pow(p.y - next.y, 2)
	                            );

	                            System.out.println("Distance from next point: " + distance);
	                        }
	                    }
	                
	                //end of visual diagnostic, delete later 18/11
	                System.out.println("Number of points in path: " + path.size());
	                
	                // Draw the traced path....this is the greedy one
	                for (Point p : path) {
	                    imp.getProcessor().drawDot(p.x, p.y);
	                }
	           
	                // Draw the refined path in blue for comparison....this is dijkstra
	                //if (i == 26) {
	                    imp.getProcessor().setColor(Color.BLUE);

	                    for (Point p : refinedPath) {
	                        imp.getProcessor().drawDot(p.x, p.y);
	                    }
	               // }
	                
	                
	                // from the minima we need to find the shortest distances between them
	                Point[] twoPoints = {ps[0], ps[1]};

	                // go to connect method
	                int[] links = getMinDistanceIndex(twoPoints);
	                ArrayList<Line> lines = getSeperatorLines(twoPoints, links);

	                //drawLines(imp, lines);

	            } else {
	                System.out.println("Not enough points found for ROI " + (i + 1)
	                        + ". Skipping connection.");
	            }

	            // Draw the selected points on the ROI
	            for (int j = 0; j < ps.length; j++) {
	                imp.getProcessor().drawDot(ps[j].x, ps[j].y);
	            }

	            imp.show();
	        }

	    }
	    
	    } 
	    rt.show("Results");
	    }
	
		
	
	
	
	public void drawLines(ImagePlus imp, ArrayList<Line> lines) {
		//IJ.setForegroundColor(0, 0, 0);
		ImageProcessor ip = imp.getProcessor();
		for (int i=0;i<lines.size();i++) {
			Line l = lines.get(i);
			FloatPolygon fp = l.getInterpolatedPolygon(); 
			PolygonRoi pr = new PolygonRoi(fp,PolygonRoi.POLYLINE);
			Polygon p = pr.getPolygon();// returns roundway trip polygon, only need to run through half of the points
			// the pr2 now still has steps that go both in x and y. so we correct this
			//fill1StepPolygonRoi(p, ip);
		}
	}
	
	public void fill1StepPolygonRoi(Polygon p, ImageProcessor ip) {
		int prevx=p.xpoints[0];
		int prevy=p.ypoints[0];
		ip.putPixelValue(p.xpoints[0], p.ypoints[0], 0);
		for (int i=1;i<p.npoints;i++) {
			/*if (Math.abs(p.xpoints[i]-prevx)==1&& Math.abs(p.ypoints[i]-prevy)==1) { //now we have difference in both
				ip.putPixelValue(p.xpoints[i], prevy, 0);
				ip.putPixelValue(p.xpoints[i], p.ypoints[i], 0);
				prevx=p.xpoints[i];
				prevy=p.ypoints[i];
			} else {*/
				ip.putPixelValue(p.xpoints[i], p.ypoints[i], 128);
				prevx=p.xpoints[i];
				prevy=p.ypoints[i];
			//}
		}
	}
	
	public ArrayList<Line> getSeperatorLines (Point[] ps, int[] links) {
		ArrayList<Line> lines = new ArrayList<Line>();
		boolean [] mapped = new boolean[ps.length];
		for (int i=0;i<ps.length;i++) {
			mapped[i]=false;
		}
		for (int i=0;i<ps.length;i++) {
			if (!mapped[i]&&links[links[i]]==i&&ps.length>1){// need to check if it is aleady mapped
				lines.add(new Line(ps[i].x,ps[i].y,ps[links[i]].x,ps[links[i]].y));
				mapped[i]=true;
				mapped[links[i]]=true;// needed to prevent going back
			}
		}
		for (int i=0;i<ps.length;i++) {
			if(!mapped[i]&&ps.length>1) {
				lines.add(new Line(ps[i].x,ps[i].y,ps[links[i]].x,ps[links[i]].y));
			}
		}
		
		return lines;
	}
	
	public int [] getMinDistanceIndex(Point[] ps){
		int [] minDistIndex = new int[ps.length];
		// we need to see which points are closest
		for (int i=0;i<ps.length;i++) {
			double mindist=Double.MAX_VALUE;
			for (int j=0;j<ps.length;j++) {
				if (i!=j) {
					double dist=Math.sqrt(Math.pow(ps[i].x-ps[j].x,2)+Math.pow(ps[i].y-ps[j].y, 2));
					if (dist<mindist) {
						mindist=dist;
						minDistIndex[i]=j;
					}
				}
			}
		}
		System.out.println(Arrays.toString(minDistIndex));
		return minDistIndex;
	}
	
	public ArrayList<Integer> findMinima(double [] curves, double limit, int curveregion) {
		ArrayList<Integer> mindexes = new ArrayList<Integer>();
		for (int i=0;i<curves.length;i++) {
			// run through all neighbouring points and see if the are bigger. only then record it as a minimum
			boolean smallest=true;
			for (int j=-curveregion/2;j<curveregion/2;j++) {
				if (curves[(i+j+curves.length)%curves.length]<curves[i]) {
					smallest=false;
					break;
				}
			}
			if (smallest&&curves[i]<limit&&curves[i]!=-1) {
				mindexes.add(i);
			}
		}
		return mindexes;
	}
	
	public Point[] getMinPoints(ArrayList<Integer> mindexs, FloatPolygon fp) {
		Point[] ps = new Point[mindexs.size()];
		for (int i=0;i<mindexs.size();i++) {
			ps[i]=new Point((int)fp.xpoints[mindexs.get(i)], (int)fp.ypoints[mindexs.get(i)]);
		}
		return ps;
	}
	public double calculateMeanIntensity(ImagePlus originalImp, Roi roi) { //livia- ..here maybe we can see the average brightness of the pixels inside that shape
	    originalImp.setRoi(roi);
	    ImageStatistics stats = originalImp.getStatistics();
	    return stats.mean;
	}
	
	public void createResultsTable(double [] data) {
		ResultsTable rt = new ResultsTable();
		for (int i=0;i<data.length;i++) {
			  rt.incrementCounter(); // livia- make the table so that i can see it in fiji 
			  rt.addValue("ROI #", i+1);
			  rt.addValue("Mean Intensity", data[i]);
		}
		rt.show("Results"); //Livia
	}
	public ImagePlus createDistanceIntensity(ImagePlus imp) {

	    // Create distance transform
	    EDM edm = new EDM();

	    FloatProcessor distance =
	            edm.makeFloatEDM(
	                    imp.getProcessor(),
	                    0,
	                    false
	            );

	    ImagePlus distanceImage =
	            new ImagePlus("Distance Transform", distance);

	    // Multiply distance transform with original intensity
	    ImagePlus multiplied =
	            ij.plugin.ImageCalculator.run(
	                    distanceImage,
	                    imp,
	                    "multiply create 32-bit"
	            );

	    multiplied.getProcessor().blurGaussian(2.0);

	    multiplied.setTitle("Distance x Intensity");

	    return multiplied;
	}
// work on this file and use intensity image and outline

	public Point[] getTwoLowestPointsInNegativeRegions(
	        double[] curves, FloatPolygon fp, double limit, int minRegionSize, Roi roi) {

    ArrayList<Point> points = new ArrayList<Point>();

    int n = curves.length;
    boolean inRegion = false;
    int regionSize = 0;
    int lowestIndex = -1;
    int secondLowestIndex = -1;
    double lowest = Double.MAX_VALUE;
    double secondLowest = Double.MAX_VALUE;

    // Reset the values for this ROI so the console reports only
    // the current ROi s regions and endpoint curvatures.
    negativeCurvatureRegionCount = 0;
    point0Curvature = Double.NaN;
    point2Curvature = Double.NaN;

    for (int i = 0; i <= n; i++) {

       
        boolean negative = i < n
                && curves[i] < limit
                && curves[i] != -1;

        if (negative) {
            if (!inRegion) {
                inRegion = true;
                regionSize = 1;
                lowestIndex = i;
                secondLowestIndex = -1;
                lowest = curves[i];
                secondLowest = Double.MAX_VALUE;
            } else {
                regionSize++;

                if (curves[i] < lowest) {
                    secondLowest = lowest;
                    secondLowestIndex = lowestIndex;
                    lowest = curves[i];
                    lowestIndex = i;
                } else if (curves[i] < secondLowest) {
                    secondLowest = curves[i];
                    secondLowestIndex = i;
                }
            }

        } else if (inRegion) {

            // Ignore short regions that may be caused by isolated
            // contour samples rather than a meaningful concavity.
            if (regionSize >= minRegionSize) {
                negativeCurvatureRegionCount++;

                if (lowestIndex >= 0) {

                    // Save the curvature of each endpoint
                    if (points.size() == 0) {
                        point0Curvature = curves[lowestIndex];
                    } else if (points.size() == 1) {
                        point2Curvature = curves[lowestIndex];
                    }

                   //temp check for that pt 2 ============== prints original contour coordinates
                    System.out.println("Original contour coordinates: x="
                            + fp.xpoints[lowestIndex] + ", y="
                            + fp.ypoints[lowestIndex]);     
         //temp fix ends here
                
                    
                  //  temp fix for pt 2 points, int, int lines replaced for now======
                    // Add only the lowest-curvature point from this region
                   // points.add(new Point(
                          //  (int) fp.xpoints[lowestIndex],
                          //  (int) fp.ypoints[lowestIndex]
                   // ));     ...until here uncomment later
                    //points.add(new Point(
                    	   // Math.round(fp.xpoints[lowestIndex]),
                    	   // Math.round(fp.ypoints[lowestIndex])
                   //));
                    
                 // Get the integer pixel coordinates of the lowest-curvature point
                    int x = (int) Math.round(fp.xpoints[lowestIndex]);
                    int y = (int) Math.round(fp.ypoints[lowestIndex]);

                    // Check if the rounded point is inside the roi
                    if (!roi.contains(x, y)) {

                        // If not search nearby pixels for one that is inside the roi
                        double shortestDistance = Double.MAX_VALUE;
                        Point closestPoint = null;

                        for (int dx = -2; dx <= 2; dx++) {
                            for (int dy = -2; dy <= 2; dy++) {

                                int newX = x + dx;
                                int newY = y + dy;

                                // Only consider pixels that fall inside the roi
                                if (roi.contains(newX, newY)) {

                                    // Calculate the distance from the original point
                                    double distance = Math.sqrt(dx * dx + dy * dy);

                                    // Keep the closest valid pixel
                                    if (distance < shortestDistance) {
                                        shortestDistance = distance;
                                        closestPoint = new Point(newX, newY);
                                    }
                                }
                            }
                        }

                        // Add the closest valid pixel to the selected points
                        if (closestPoint != null) {
                            points.add(closestPoint);
                        }

                    } else {
                        // If the rounded point is already inside the ROI, keep it
                        points.add(new Point(x, y));
                    }               
                    
                }
                
               // if (lowestIndex >= 0 && secondLowestIndex >= 0) {

                    // Save the actual curvature values before converting
                    // the contour coordinates to integer pixel positions.
                   // if (points.size() == 0) {
                   //     point0Curvature = curves[lowestIndex];
                   // } else if (points.size() == 2) {
                   //     point2Curvature = curves[lowestIndex];
                  //  }

                   // points.add(new Point(
                        //    (int) fp.xpoints[lowestIndex],
                        //    (int) fp.ypoints[lowestIndex]
                  //  ));

                 //   points.add(new Point(
                   //         (int) fp.xpoints[secondLowestIndex],
                        //    (int) fp.ypoints[secondLowestIndex]
                  //  ));
               // }
            }

            inRegion = false;
            regionSize = 0;
            lowestIndex = -1;
            secondLowestIndex = -1;
            lowest = Double.MAX_VALUE;
            secondLowest = Double.MAX_VALUE;
        }
    }

    return points.toArray(new Point[points.size()]);
}
	
	//public Point[] getTwoLowestPointsInNegativeRegions(
	        //double[] curves, FloatPolygon fp, double limit, int minRegionSize) {

	    //ArrayList<Point> points = new ArrayList<Point>();

	  //  int n = curves.length;
	   // boolean inRegion = false;
	  //  int regionSize = 0;
	    //int lowestIndex = -1;
	   // int secondLowestIndex = -1;
	   // int regionStartIndex = -1;
	   // double lowest = Double.MAX_VALUE;
	    //double secondLowest = Double.MAX_VALUE;

	    //for (int i = 0; i < n; i++) {

	        // We are inside a negative-curvature region
	       // if (curves[i] < limit && curves[i] != -1) {

	            //if (!inRegion) {

	                // Start of a new region
	                //inRegion = true;
	                //regionSize = 1;
	                //regionStartIndex = i;

	                //lowestIndex = i;
	                //secondLowestIndex = -1;

	                //lowest = curves[i];
	                //secondLowest = Double.MAX_VALUE;

	          //  } else {

	                //regionSize++;

	                // Update the lowest and second-lowest values
	                //if (curves[i] < lowest) {

	                    //secondLowest = lowest;
	                   // secondLowestIndex = lowestIndex;

	                   // lowest = curves[i];
	                    //lowestIndex = i;

	                //} else if (curves[i] < secondLowest) {

	                    //secondLowest = curves[i];
	                    //secondLowestIndex = i;
	           //     }
	          //  }

	      //  } else {

	            // End of the negative-curvature region
	          //  if (inRegion && regionSize >= minRegionSize) {

	            	// Print the points with negative curvature
	            	//System.out.println("Negative curvature region:");

	            	//for (int j = 0; j < n; j++) {
	            	    //if (curves[j] < limit && curves[j] != -1) {
	            	        //System.out.println(
	            	            //"x=" + fp.xpoints[j] +
	            	          //  " y=" + fp.ypoints[j] +
	            	           // " curvature=" + curves[j]
	            //	System.out.println("Negative curvature region:");

	            	//for (int j = 0; j < n; j++) {
	            	  //  if (curves[j] < limit && curves[j] != -1) {
	            	 //       System.out.println(
	            	   //         "index=" + j +
	            	      //      " x=" + fp.xpoints[j] +
	            	      //      " y=" + fp.ypoints[j] +
	            	      //      " curvature=" + curves[j]
	            	//System.out.println("Negative curvature region:");

	            	//int regionEndIndex = i - 1;

	            	//System.out.println(
	            	    //"First point: index=" + regionStartIndex +
	            	    //" x=" + fp.xpoints[regionStartIndex] +
	            	   // " y=" + fp.ypoints[regionStartIndex] +
	            	   // " curvature=" + curves[regionStartIndex]
	            	//);

	            	//System.out.println(
	            	    //"Lowest point: index=" + lowestIndex +
	            	   // " x=" + fp.xpoints[lowestIndex] +
	            	    //" y=" + fp.ypoints[lowestIndex] +
	            	   // " curvature=" + curves[lowestIndex]
	            	//);

	            	//System.out.println(
	            	   // "Last point: index=" + regionEndIndex +
	            	   // " x=" + fp.xpoints[regionEndIndex] +
	            	   // " y=" + fp.ypoints[regionEndIndex] +
	            	   // " curvature=" + curves[regionEndIndex]
	            	    //    );
	            	    
	            
	                // Add the lowest-curvature point
	               // if (lowestIndex >= 0) {
	                   // points.add(new Point(
	                      //  (int) fp.xpoints[lowestIndex],
	                       // (int) fp.ypoints[lowestIndex]
	                 //   ));
	              //  }

	                // Add the second-lowest-curvature point
	               // if (secondLowestIndex >= 0) {
	                 //  points.add(new Point(
	                      //  (int) fp.xpoints[secondLowestIndex],
	                      //  (int) fp.ypoints[secondLowestIndex]
	                   // ));
	             // Check if the two lowest-curvature points are actually far enough apart.
	             // They might just be neighbouring points from the same curvature region.
	           //  if (lowestIndex >= 0 && secondLowestIndex >= 0) {

	                 // Keep the original coordinates before converting them to integers
	                // double x1 = fp.xpoints[lowestIndex];
	                // double y1 = fp.ypoints[lowestIndex];

	                // double x2 = fp.xpoints[secondLowestIndex];
	               //  double y2 = fp.ypoints[secondLowestIndex];

	                // double dx = x2 - x1;
	                // double dy = y2 - y1;

	                 // Calculate how far apart the two points are.
	                // double distance = Math.sqrt(dx * dx + dy * dy);

	                 // Print the points and their curvature values.
	                // System.out.println("Lowest point: x=" + x1 +
	                        // " y=" + y1 +
	                        // " curvature=" + curves[lowestIndex]);

	               //  System.out.println("Second-lowest point: x=" + x2 +
	                       //  " y=" + y2 +
	                       //  " curvature=" + curves[secondLowestIndex]);

	                 // Check how far apart they are along the outline.
	                // System.out.println("Index difference: " +
	                        // Math.abs(lowestIndex - secondLowestIndex));

	                // System.out.println("Distance between points: " + distance);

	                 // Keep using the integer points for now.
	                // points.add(new Point((int) x1, (int) y1));
	               //  points.add(new Point((int) x2, (int) y2));
	               // }

	              //  inRegion = false;
	          //  }
	//}
	  //  }

	   // return points.toArray(new Point[points.size()]);
	//}
	public double calculateAlpha(Point p1, Point p2) {

	    double dx = p2.x - p1.x;
	    double dy = p2.y - p1.y;

	    double alpha = Math.toDegrees(Math.atan2(dy, dx));

	    return alpha;
	}
	public Point findNextPoint(Point current, Point previousPoint, Point end,
	        double alpha, ImagePlus distanceIntensity, Roi roi, int cpNumber,
	        ArrayList<Point> path) {
		
	    ImageProcessor ip = distanceIntensity.getProcessor();
	   //better to read in console
	    System.out.println();
	    System.out.println("========== CP" + cpNumber + " ==========");
	    System.out.println("Current point: (" + current.x + ", " + current.y + ")");
	    System.out.println("Target Point 2: (" + end.x + ", " + end.y + ")");
	  //temp fix pt 2====check if target is inside roi
	    System.out.println("Target inside ROI: " + roi.contains(end.x, end.y));
	    //temp fix ends here
	    
	    //trying to move towards pt 2
	    double targetAlpha = calculateAlpha(current, end);
	    System.out.println("Direction towards target: " + targetAlpha);

	    System.out.println("Checking three candidate pixels:");

	    Point bestPoint = null;
	    double lowestValue = Double.MAX_VALUE;

	 // Check three candidate pixels in the direction of the target
	    double angleDifference = 45.0;
	 

	    for (int i = -1; i <= 1; i++) {
	        double direction = targetAlpha + (i * angleDifference);

	        double radians = Math.toRadians(direction);
//calculates three candidate directions (α − 45°, α, α + 45°) and convert the angles from degrees to radians
	        int dx = (int) Math.round(Math.cos(radians));
	        int dy = (int) Math.round(Math.sin(radians));

	        int x = current.x + dx;
	        int y = current.y + dy;
	        
	        System.out.println("Candidate " + (i + 2) + ": (" + x + ", " + y + ")" +
	        	    " | direction = " + direction);
	        	;//656-659....just to check 3 candidates

	        // Skip pixels outside the image or roi
	        if (x < 0 || y < 0 ||
	                x >= ip.getWidth() || y >= ip.getHeight()) {
	            continue;
	        }

	        if (!roi.contains(x, y)) {
	        	//below line checking if pt 2 is the problem or the ones reaching before
	        	System.out.println("    Skipped: outside ROI");
	        	continue;
	        }

	     // Skip only the previous pixel
	        if (previousPoint != null &&
	                x == previousPoint.x && y == previousPoint.y) {
	            continue;
	        }
	     // Choose the endpoint if it is one of the eligible pixels
	       // if (x == end.x && y == end.y) {
	         //   return new Point(end);
	       // }

	       // double value = ip.getPixelValue(x, y);
	        
	        if (x == end.x && y == end.y) {
	            return new Point(end);
	        }

	        if (path.contains(new Point(x, y))) {
	            System.out.println("    Skipped: already visited");
	            continue;
	        }

	        double value = ip.getPixelValue(x, y);
	        
	        System.out.println("    Pixel value: " + value);
	        //	);//683-686....just to check 3 candidates

	        if (value < lowestValue) {
	            lowestValue = value;
	            bestPoint = new Point(x, y);
	      }
	    }
	    return bestPoint;
	    }
	
	public ArrayList<Point> connectPoints(
	        Point start, Point end, double alpha,
	        ImagePlus distanceIntensity, Roi roi) {

	    ArrayList<Point> path = new ArrayList<Point>();
	 // Get the image dimensions
	    int width = distanceIntensity.getWidth();
	    int height = distanceIntensity.getHeight();

	    Point previousPoint = null;

	 // Check that the starting point is inside the image
	    if (start.x < 0 || start.y < 0 ||
	            start.x >= width || start.y >= height) {
	        System.out.println("Starting point is outside the image.");
	        return path;
	    }

	    Point currentPoint = new Point(start);
	    path.add(currentPoint);
//temp check to see if pt 2 pixles=======
	 // Check the three pixels around Point 2
	    System.out.println();
	    System.out.println("Checking the three pixels around Point 2:");

	    int[][] neighbours = { // those 8 pixels
	            {end.x - 1, end.y - 1},
	            {end.x, end.y - 1},
	            {end.x + 1, end.y - 1},
	            {end.x - 1, end.y},
	            {end.x + 1, end.y},
	            {end.x - 1, end.y + 1},
	            {end.x, end.y + 1},
	            {end.x + 1, end.y + 1}
	    };
	    
	    for (int i = 0; i < neighbours.length; i++) { //check neighbouring pixels
	        int x = neighbours[i][0]; //get the x and y coordinates of the current pixel
	        int y = neighbours[i][1];
	        // Print the pixel coordinates and check if they are inside the ROI
	        System.out.println("Point 2 candidate " + (i + 1) +
	                ": (" + x + ", " + y + ")" +
	                " | inside ROI: " + roi.contains(x, y));
	    }
	    
	//tmp fix for pt 2 ends here
	    
	    // Mark the starting pixel as visited
	  //  visited[currentPoint.x][currentPoint.y] = true;

	    // Keep on tracing until we reach the endpoint
	    while (!currentPoint.equals(end)) {

	    	Point nextPoint = findNextPoint(
	    	        currentPoint, previousPoint, end, alpha,
	    	        distanceIntensity, roi, path.size(), path);
	        if (nextPoint == null) {
	            System.out.println("No suitable next point found.");
	            break;
	        }
	      //3 lines below checks for cp1
	       // System.out.println("Selected CP1: (" + nextPoint.x + ", " + nextPoint.y + ")");
	       // path.add(nextPoint);
	       // return path;
	      
	        System.out.println("Selected CP" + path.size() + ": (" + nextPoint.x + ", " + nextPoint.y + ")");
	        path.add(nextPoint);

	     // Move to the next point
	        previousPoint = currentPoint;
	        alpha = calculateAlpha(currentPoint, nextPoint);
	        currentPoint = nextPoint;

	        // Stop after CP2 for now
	     // if (path.size() >=19 ) {
	           // return path;
	       // }
	    }

	    // Check whether the endpoint was reached
	    if (currentPoint.equals(end)) {
	        System.out.println("Reached Point 2.");
	    } else {
	        System.out.println("Tracing stopped before reaching Point 2.");
	    }

	    return path;
	}
	

	public double[][] makeGraph(ArrayList<Point> path, ImagePlus distanceIntensity) {

	    ImageProcessor ip = distanceIntensity.getProcessor();

	    int nodeCount = path.size();
	    int edgeCount = 0;

	    // Create a matrix to store all the pixel connections
	    double[][] graph = new double[nodeCount][nodeCount];

	    // set everything to infinity first
	    // We only change this when we find two connected pixels
	    for (int i = 0; i < nodeCount; i++) {
	        for (int j = 0; j < nodeCount; j++) {
	            graph[i][j] = Double.POSITIVE_INFINITY;
	        }
	    }

	    System.out.println();
	    System.out.println("Graph construction");
	    System.out.println("Number of nodes: " + nodeCount);

	    // Check which pixels in the path are next to each other
	    // This helps to find all the possible connections
	    for (int i = 0; i < nodeCount; i++) {
	        Point p1 = path.get(i);

	        for (int j = i + 1; j < nodeCount; j++) {
	            Point p2 = path.get(j);

	            int dx = Math.abs(p1.x - p2.x);
	            int dy = Math.abs(p1.y - p2.y);

	            // Check horizontal vertical and diagonal neighbours
	            if (dx <= 1 && dy <= 1 && (dx + dy > 0)) {

	                // Get the intensity values of both pixels
	                double intensity1 = ip.getPixelValue(p1.x, p1.y);
	                double intensity2 = ip.getPixelValue(p2.x, p2.y);

	                // Use the average intensity to represent this connection
	                double connectionIntensity =
	                        (intensity1 + intensity2) / 2.0;

	                // Store the connection in both directions
	                graph[i][j] = connectionIntensity;
	                graph[j][i] = connectionIntensity;

	                edgeCount++;

	                System.out.println("Connection: node " + i + " - node " + j
	                        + " | Connection intensity: " + connectionIntensity);
	            }
	        }
	    }

	    System.out.println("Number of connections: " + edgeCount);

	    return graph;
	}
	
	public ArrayList<Point> dijkstra(double[][] graph, ArrayList<Point> path) {

	    int nodeCount = graph.length;

	    // store the lowest total intensity found for each node
	    double[] distances = new double[nodeCount];

	    // Keep track of which node we came from
	    int[] previous = new int[nodeCount];

	    //check which nodes we have already visited
	    boolean[] visited = new boolean[nodeCount];

	    // Set all nodes to infinity at the beginning
	    for (int i = 0; i < nodeCount; i++) {
	        distances[i] = Double.POSITIVE_INFINITY;
	        previous[i] = -1;
	    }

	    // Start from the first node
	    distances[0] = 0;

	    // keep checking nodes until there are no more reachable nodes
	    for (int i = 0; i < nodeCount; i++) {

	        int currentNode = -1;
	        double lowestIntensity = Double.POSITIVE_INFINITY;

	        // find the unchecked node with the lowest total intensity
	        for (int j = 0; j < nodeCount; j++) {
	            if (!visited[j] && distances[j] < lowestIntensity) {
	                lowestIntensity = distances[j];
	                currentNode = j;
	            }
	        }

	        // Stopping if there are no more reachable nodes
	        if (currentNode == -1) {
	            break;
	        }

	        visited[currentNode] = true;

	        // check allthe possible connections from this node
	        for (int j = 0; j < nodeCount; j++) {

	            if (graph[currentNode][j] == Double.POSITIVE_INFINITY) {
	                continue;
	            }

	            if (visited[j]) {
	                continue;
	            }

	            // calculate the total intensity if we go through this node
	            double newIntensity =
	                    distances[currentNode] + graph[currentNode][j];

	            // keep this connection if it gives us a lower total intensity
	            if (newIntensity < distances[j]) {
	                distances[j] = newIntensity;
	                previous[j] = currentNode;
	            }
	        }
	    }

	    // check if we were able to reach the destination
	    int endNode = nodeCount - 1;

	    if (distances[endNode] == Double.POSITIVE_INFINITY) {
	        System.out.println("No route found between Point 0 and Point 2.");
	        return new ArrayList<Point>();
	    }

	    // work backwards from the destination to find the selected route
	    ArrayList<Point> refinedPath = new ArrayList<Point>();

	    int currentNode = endNode;

	    while (currentNode != -1) {
	        refinedPath.add(0, path.get(currentNode));
	        currentNode = previous[currentNode];
	    }

	    System.out.println("Original path points: " + path.size());
	    System.out.println("Refined path points: " + refinedPath.size());
	    System.out.println("Total connection intensity: " + distances[endNode]);
	    
	 // Print the points selected by Dijkstra
	    System.out.println("Refined path coordinates:");

	    for (int i = 0; i < refinedPath.size(); i++) {
	        Point p = refinedPath.get(i);

	        System.out.println("Point " + i + ": (" + p.x + ", " + p.y + ")");
	    }

	    return refinedPath;
	}
	
	}// get variable angle for each point...
//probably rewrite alpha, next oint conncet  points
// each point in console maybe the alpha too and draw it step by step 