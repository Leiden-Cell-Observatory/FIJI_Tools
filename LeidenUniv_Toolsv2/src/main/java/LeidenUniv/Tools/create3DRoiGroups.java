package LeidenUniv.Tools;
import java.util.*;
import ij.gui.*;
import ij.plugin.frame.*;

/**
 * Used by the combining of overlapping rois, creates an arraylist of int arrays, where these int are the indices of connected rois 
 * @author willemsejj
 *
 */
public class create3DRoiGroups{
	/**
	 * @param ConRoi Current roi[] that will be grouped
	 * @param NB The arraylist coming from find neighbours
	 * @return The groups as arraylist of arraylist of integers
	 */
	public ArrayList<ArrayList<Integer>> to3dobj(Roi[] ConRoi, ArrayList<int[]> NB){
		RoiManager rm = RoiManager.getInstance2();
		ArrayList<Integer> Visited = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> Grouped = new ArrayList<ArrayList<Integer>>();
		int groups=0;
		for (int i=0;i<ConRoi.length;i++){ //itterate over rois
			ArrayList<Integer> CurGr=new ArrayList<Integer>();// create a new group for the current roi
			if (!Visited.contains(i)){ //if the roi has not been visited before
				groups++;// count another group
				//add this node to the que
				Queue<Integer> Connecting = new LinkedList<Integer>(); // create a new list
				Connecting.add(i); // and add the current roi
				while (!Connecting.isEmpty()){ // if the roilist is not empty (runs at least once based on the previous line
					int tempNode= Connecting.poll(); // take the roinumber from the list
					Visited.add(tempNode); // add it to the visited list
					CurGr.add(tempNode); // add it to the current group or rois
					for (int j=0;j<NB.size();j++){ // run through all neighbour lists
						int[] CheckN=NB.get(j); //get the current neighbour set
						
						if (CheckN[0]==tempNode &&!Visited.contains(CheckN[1])){ // if the first neighbour is the current roi and the 2nd is not visited yet
							Connecting.add(CheckN[1]); //add the new node to the connecting list and continue with the connecting list
							Visited.add(CheckN[1]);
						} else if (CheckN[1]==tempNode&&!Visited.contains(CheckN[0])){ // else if the second neighbour is the current roi and the 1st is not visited yet
							Connecting.add(CheckN[0]);//add the new node to the connecting list and continue with the connecting list
							Visited.add(CheckN[0]);
						}
					}
				}
				//check all neighbours and add them to que
				
				Grouped.add(CurGr);
				for (int ii=0;ii<CurGr.size();ii++){
					rm.select(CurGr.get(ii));
					rm.runCommand("Rename","Object "+groups+ ", Part "+ii);
				}
				
			}
		}
		rm.runCommand("Sort");
		return Grouped;
	}
	
	public Roi[][] to3dRois(Roi[] ConRoi, ArrayList<int[]> NB){
		RoiManager rm = RoiManager.getInstance2();
		ArrayList<Integer> Visited = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> Grouped = new ArrayList<ArrayList<Integer>>();
		int groups=0;
		for (int i=0;i<ConRoi.length;i++){ //itterate over rois
			ArrayList<Integer> CurGr=new ArrayList<Integer>();// create a new group for the current roi
			if (!Visited.contains(i)){ //if the roi has not been visited before
				groups++;// count another group
				//add this node to the que
				Queue<Integer> Connecting = new LinkedList<Integer>(); // create a new list
				Connecting.add(i); // and add the current roi
				while (!Connecting.isEmpty()){ // if the roilist is not empty (runs at least once based on the previous line
					int tempNode= Connecting.poll(); // take the roinumber from the list
					Visited.add(tempNode); // add it to the visited list
					CurGr.add(tempNode); // add it to the current group or rois
					for (int j=0;j<NB.size();j++){ // run through all neighbour lists
						int[] CheckN=NB.get(j); //get the current neighbour set
						
						if (CheckN[0]==tempNode &&!Visited.contains(CheckN[1])){ // if the first neighbour is the current roi and the 2nd is not visited yet
							Connecting.add(CheckN[1]); //add the new node to the connecting list and continue with the connecting list
							Visited.add(CheckN[1]);
						} else if (CheckN[1]==tempNode&&!Visited.contains(CheckN[0])){ // else if the second neighbour is the current roi and the 1st is not visited yet
							Connecting.add(CheckN[0]);//add the new node to the connecting list and continue with the connecting list
							Visited.add(CheckN[0]);
						}
					}
				}
				//check all neighbours and add them to que
				
				Grouped.add(CurGr);
				for (int ii=0;ii<CurGr.size();ii++){
					rm.select(CurGr.get(ii));
					rm.runCommand("Rename","Object "+groups+ ", Part "+ii);
				}
				
			}
		}
		Roi[][] GroupedRois = new Roi[Grouped.size()][];
		for (int i=0;i<Grouped.size();i++) {
			ArrayList<Integer> CurGr=Grouped.get(i);
			Roi [] temp = new Roi[Grouped.get(i).size()];
			for (int j=0;j<temp.length;j++) {
				temp[j]=rm.getRoi(CurGr.get(j));
			}
			GroupedRois[i]=temp;
		}
		rm.runCommand("Sort");
		return GroupedRois;
	}
}
