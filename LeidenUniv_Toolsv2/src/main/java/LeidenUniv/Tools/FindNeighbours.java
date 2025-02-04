package LeidenUniv.Tools;
import java.util.*;
import ij.gui.*;
/**
 * For combining a Roi array that overlaps in postions over a z-stack into groups of rois
 * @author willemsejj
 *
 */
public class FindNeighbours{
	
	/**
	 * Combines the existing rois in the roi array in a connecting arraylist
	 * @param ConRoi ROIS to combine
	 * @return The combined rois
	 */
	public ArrayList<int[]> FindNeigh(Roi[] ConRoi){
		ArrayList<int []> NeighBours = new ArrayList<int[]>();
		for (int i=0;i<ConRoi.length-1;i++){
			int cSL=ConRoi[i].getPosition();
			for (int j=i+1;j<ConRoi.length;j++){
				int nSL=ConRoi[j].getPosition();
				if (Math.abs(nSL-cSL)==1){
					boolean connected=false;
					//check if one roi contains another roi
					int [] xpa =(((PolygonRoi)ConRoi[i]).getPolygon()).xpoints;
					int [] ypa =(((PolygonRoi)ConRoi[i]).getPolygon()).ypoints;
					int [] xpb =(((PolygonRoi)ConRoi[j]).getPolygon()).xpoints;
					int [] ypb =(((PolygonRoi)ConRoi[j]).getPolygon()).ypoints;
					for (int k=0;k<xpa.length;k++){
						if (((PolygonRoi)ConRoi[j]).contains(xpa[k],ypa[k])){
							int[] NtoAdd={i,j};
							NeighBours.add(NtoAdd);
							connected = true;
							break;
						}
					}
					if (!connected){
						for (int k=0;k<xpb.length;k++){
							if (((PolygonRoi)ConRoi[i]).contains(xpb[k],ypb[k])){
								int[] NtoAdd={i,j};
								NeighBours.add(NtoAdd);
								break;
							}
						}
					}
				}
			}
		}
		return NeighBours;
	}

	/**
	 * Combines the existing rois in the roi array in a connecting arraylist, allows for missing slices
	 * @param ConRoi Roi array to analyse
	 * @param depth slices that can be missed
	 * @return The combined rois
	 */
	public ArrayList<int[]> FindNeigh(Roi[] ConRoi, int depth){
		ArrayList<int []> NeighBours = new ArrayList<int[]>();
		for (int i=0;i<ConRoi.length-depth;i++){// loop through all ROis
			int cSL=ConRoi[i].getPosition(); // get frame position
			for (int j=i+depth;j<ConRoi.length;j++){ //check all subsequent rois
				int nSL=ConRoi[j].getPosition();// get framnumbe 2nd roi
				if (Math.abs(nSL-cSL)<=depth){ // check the frame difference, if it falls below depth go on
					
					boolean connected=false;
					//check if one roi contains another roi
					int [] xpa =(((PolygonRoi)ConRoi[i]).getPolygon()).xpoints;
					int [] ypa =(((PolygonRoi)ConRoi[i]).getPolygon()).ypoints;
					int [] xpb =(((PolygonRoi)ConRoi[j]).getPolygon()).xpoints;
					int [] ypb =(((PolygonRoi)ConRoi[j]).getPolygon()).ypoints;
					for (int k=0;k<xpa.length;k++){
						if (((PolygonRoi)ConRoi[j]).contains(xpa[k],ypa[k])){
							int[] NtoAdd={i,j};
							NeighBours.add(NtoAdd);
							connected = true;
							break;
						}
					}
					if (!connected){
						for (int k=0;k<xpb.length;k++){
							if (((PolygonRoi)ConRoi[i]).contains(xpb[k],ypb[k])){
								int[] NtoAdd={i,j};
								NeighBours.add(NtoAdd);
								break;
							}
						}
					}
				}
			}
		}
		return NeighBours;
	}
}
