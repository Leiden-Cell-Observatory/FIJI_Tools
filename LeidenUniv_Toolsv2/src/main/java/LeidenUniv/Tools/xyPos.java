package LeidenUniv.Tools;

/**
 * pixel position
 * @author willemsejj
 *
 */
public class xyPos {
	
		public int xp;
		public int yp;
		
		public xyPos(int x, int y){
			xp=x;
			yp=y;
		}

		public int getX(){
			return xp;
		}

		public int getY(){
			return yp;
		}
		@Override
		public boolean equals(Object p){
			if (p instanceof xyPos){
				xyPos pp = (xyPos)p;
				if (this.xp==pp.xp && this.yp==pp.yp) return true;
				
			} else {
				return false;
			}
			return false;
		}

	}
