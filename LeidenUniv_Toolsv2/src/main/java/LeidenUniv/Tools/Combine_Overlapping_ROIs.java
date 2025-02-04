package LeidenUniv.Tools;

import ij.IJ;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import java.util.ArrayList;

/**
 * Combines rois in a 3D stack based on overlap in consecutive slices
 * @author willemsejj
 */
public class Combine_Overlapping_ROIs implements PlugIn {
   /**
 * to run the plugin in ImageJ/FIJI
 */
public void run(String arg) {
      RoiManager rm = RoiManager.getInstance();
      if (rm == null) {
         rm = RoiManager.getInstance2();
      }

      if (rm == null) {
         IJ.showMessage("No roi manager found");
      } else {
         Roi[] r = rm.getRoisAsArray();
         ArrayList<int[]> roisets = (new FindNeighbours()).FindNeigh(r);
         (new create3DRoiGroups()).to3dobj(r, roisets);
      }

   }
}
