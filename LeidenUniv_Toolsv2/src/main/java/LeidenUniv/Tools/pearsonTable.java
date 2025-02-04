package LeidenUniv.Tools;

public class pearsonTable  {
	double [][] table;
	double totalPearson;
	double totalPearsonMinOne;
	double avPearson;
	double avPearsonMinOne;
	double minPearson;
	double maxPearson;
	double totalPearsonDeviation;
	
	pearsonTable(double [][] t){
		table=t;
		double sum=0;
		minPearson = Double.MAX_VALUE;
		maxPearson = Double.MIN_VALUE;
		for (int i=0;i<table.length;i++) {
			for (int j=0;j<table[i].length;j++) {
				sum+=table[i][j];
				if (table[i][j]<minPearson) {
					minPearson=table[i][j];
				}
				if (table[i][j]>maxPearson&& table[i][j]<1) {
					maxPearson=table[i][j];
				}
			}
		}
		avPearson=sum/(table.length*table[0].length);
		avPearsonMinOne=(sum-table.length)/(table.length*(table[0].length-1));
		totalPearson =sum;
		totalPearsonMinOne=sum-table.length;
		totalPearsonDeviation=table.length*table[0].length-sum;
	}
}