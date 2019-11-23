//package paging;

/**
 * Process class to store data about each process. 
 * @author Sonam Tailor
 *
 */

public class process {
	
	//data fields 
	int pageFaults = 0;
	float residencyTime = 0; //running sum
	float numEvictions = 0;
	int processNum;
	double A;
	double B;
	double C;
	int N; 
	int nextRef = -1; 
	int currRef;
	
	//constructor 
	process(int processNum, int N, double A, double B, double C, int currRef){
		this.processNum = processNum;
		this.N = N;
		this.A = A;
		this.B = B;
		this.C = C;
		this.currRef = currRef;
		
	}
	

	
}
