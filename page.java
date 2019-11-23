//package paging;

/**
 * Page class to store info about each page. 
 * @author Sonam Tailor
 *
 */

public class page {
	
	//data fields
	int pageNum;
	int processNum;
	int loadTime; //for use of the residency time 
	int evictedTime;
	
	//constructor
	page(int pageNum, int processNum){
		this.pageNum = pageNum;
		this.processNum = processNum;
	}
	
}
