//package paging;

/**
 * In this lab we simulate demand paging to see how the number of page faults depends on the page size, program size, replacement alg, and job mix.
 * Novemeber 23rd 2019
 * @author Sonam Tailor 
 * 
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class paging {
	public static void main(String[] args) throws FileNotFoundException {
		
		//checks for command line argument 
		if (args.length == 0) {
			System.err.println("Usage Error: the program expects an argument.");
			System.exit(1);
		}
		Scanner scanRand;
		
		//java command line arguments are stored as strings - so we must convert to int
		int M = Integer.parseInt(args[0]); //machine size 
		int P = Integer.parseInt(args[1]); //page size
		int S = Integer.parseInt(args[2]); //size of each process
		int J = Integer.parseInt(args[3]); //job mix
		int N = Integer.parseInt(args[4]); //number of references
		String repAlg = args[5]; //replacement alg
		
		int numFrames = M/P;
		
		//Frame Table of size numFrames
		ArrayList<page> frameTable = new ArrayList<>(numFrames);
		//initialize with all negative values so we know it is empty
		for (int i = 0; i < numFrames; i++) {
			frameTable.add(i,null);
		}
		//System.out.println(numFrames);
		
		//to store processes and process number
		HashMap<Integer, process> processes = new HashMap<>();
		
		//to store the order of our processes - helps with round robin
		Queue<Integer> run = new LinkedList<>();
		
		
		//output information to console 
		System.out.println("The machine size is "+ M +".");
		System.out.println("The page size is "+ P +".");
		System.out.println("The process size is "+ S +".");
		System.out.println("The job mix number is "+ J +".");
		System.out.println("The number of references per process is "+ N +".");
		System.out.println("The replacement algorithm is "+ repAlg +".");
		System.out.println("");
		
		//open the random file 
		File rand = new File("random-numbers.txt");
		//read the random file 

		 scanRand = new Scanner(new FileReader(rand));

		//System.out.println(scanRand.next());
		 int numberProcs;  //stores number of processes we have
		
		//now we must create our hashmap of processes according to the value of J
		 //(111*1 + S)%S sets the first word
		if(J == 1) {
			int currRef = (111*1 + S)%S; 
			process temp = new process(1, N, 1, 0, 0, currRef);
			processes.put(1,temp); 
			run.add(1);
			numberProcs =1;
		}
		
		else if(J == 2) {
			for (int i = 1; i <= 4; i++) {
				int currRef = (111*i + S)%S; 
				process temp = new process(i, N, 1, 0, 0, currRef);
				processes.put(i, temp);
				run.add(i);
			}
			numberProcs =4;
		}
		
		else if (J == 3) {
			for (int i = 1; i <= 4; i++) {
				int currRef = (111*i + S)%S; 
				process temp = new process(i, N, 0, 0, 0, currRef); //fully random references 
				processes.put(i, temp);
				run.add(i);
			}
			numberProcs =4;
		}
		//Case of J = 4
		else {
			for (int i = 1; i <= 4; i++) {
				int currRef = (111*i + S)%S; 
				process temp;
				if (i ==1) {
					temp = new process(1, N, .75, .25, 0, currRef); //first process
				}
				
				else if (i == 2) {
					temp = new process(2, N, .75, 0, .25, currRef); //second process
				}
				
				else if(i == 3) {
					temp = new process(3, N, .75, .125, .125, currRef); //third process
				}
				
				else {
					temp = new process(4, N, .5, .125, .125, currRef); //fourth process
				}
			
				processes.put(i, temp);
				run.add(i);
			}
			numberProcs =4;
		}
		//this hashmap stores the pages as the keys and their indices in the frame as the Value
		//Used a LinkedHashMap because order matters - this hashmap will be used to determine what gets evicted in lru 
		//move pages recently referenced to the end of the linkedhashmap
		LinkedHashMap<page, Integer> frame = new LinkedHashMap<>(numberProcs);

		
		//total time to run this will be number of processes * the number of references for each process
		int time = 1;
		if(repAlg.equals("lru")) {
			while(numberProcs*N>=time) { //all our processes will have completed when we exceed numberProcs*N
				int atHead = run.peek(); //processNum to run 
				process curr = processes.get(atHead);
				//System.out.println("The one at head is "+atHead + " and the curr is "+ curr.processNum);
				for(int q = 0; q < 3 && curr.N>0; q++) { //want to make sure we do not exceed the number of references for the process
					//simulate the reference for this process
					
					int pageNum = curr.currRef/P; //word/P 
					//Debug line
					//System.out.print(curr.processNum + " references word "+ curr.currRef + "(page " + pageNum + ") at time " + time+":");
					if(!pageContained(curr.processNum, pageNum, frameTable)) { //not contained
						curr.pageFaults+=1;
						
						//Debug line
						//System.out.print(" Fault");
						
						lru(frameTable,pageNum, frame, numFrames,curr ,processes, time);
					}
					else { //contained
						page toRemove = null;
						int index = 0; 
						//Debug line
						//System.out.println(" Hit");
						for(page pg : frame.keySet()) {
							if(pg.pageNum == pageNum && pg.processNum == curr.processNum) {
								toRemove = pg;
								index = frame.get(toRemove);
								break;
							}
						}
						
						frame.remove(toRemove); //remove
						frame.put(toRemove, index); //adds to end 
					}
					
					curr.N--;
					
					//calculate the next reference for this process
					int randomNum = scanRand.nextInt();
					
					//Debug line
					//System.out.println("Random Num being used is: " + randomNum);
					double y = randomNum/(Integer.MAX_VALUE + 1d);
					if(y < curr.A) {
						curr.nextRef = (curr.currRef+1 + S) % S;
					}
					else if(y<(curr.A+curr.B)) {
						curr.nextRef = (curr.currRef-5 + S) % S;
					}
					else if(y<(curr.A+curr.B+curr.C)){
						curr.nextRef = (curr.currRef+4 +S) % S;	
					}
					//case we have y>= A+B+C
					else {
						curr.nextRef = (scanRand.nextInt())%S;
					}
					curr.currRef = curr.nextRef; 
					
					time++;
					
				}
				int done = run.poll();
				run.add(done);
			
			}
		}
		
		else if(repAlg.equals("random")) {
			while(numberProcs*N>=time) { //all our processes will have completed when we exceed numberProcs*N
				int atHead = run.peek(); //processNum to run 
				process curr = processes.get(atHead);
				//System.out.println("The one at head is "+atHead + " and the curr is "+ curr.processNum);
				for(int q = 0; q < 3 && curr.N>0; q++) { //want to make sure we do not exceed the number of references for the process
					//simulate the reference for this process
					
					int pageNum = curr.currRef/P; 
					//Debug line
					//System.out.print(curr.processNum + " references word "+ curr.currRef + " (page " + pageNum + ") at time " + time+":");
					if(!pageContained(curr.processNum, pageNum, frameTable)) { //not contained
						curr.pageFaults+=1;
						
						//Debug line
						//System.out.print(" Fault");
						
						//check if there is empty spot - otherwise we need a random number to evict
						if(frameTable.contains(null)) {
							for(int i = frameTable.size()-1; i>=0; i--) {
								if (frameTable.get(i)==null) {
									page temp = new page(pageNum, curr.processNum);
									temp.loadTime = time;
									frameTable.set(i, temp);
									//Debug line
									//System.out.println(" using free frame "+i);
									break;
								}
							}
						}
						else {
							int random = scanRand.nextInt(); //get random integer to calculate the frame to Evict
							int FrameToEvict = (random + numFrames)%numFrames; //calculates which frame to evict
							random(frameTable,pageNum, numFrames, curr, processes, time, FrameToEvict);
						}
					}
					else { //contained
						//Debug line
						//System.out.println(" Hit");
					}
					
					//decrement number of references for the process
					curr.N--;
					
					//calculate the next reference for this process
					int randomNum = scanRand.nextInt();
					
					//Debug line
					//System.out.println("Random Num being used is: " + randomNum);
					double y = randomNum/(Integer.MAX_VALUE + 1d);
					if(y < curr.A) {
						curr.nextRef = (curr.currRef+1 + S) % S;
					}
					else if(y<(curr.A+curr.B)) {
						curr.nextRef = (curr.currRef-5 + S) % S;
					}
					else if(y<(curr.A+curr.B+curr.C)){
						curr.nextRef = (curr.currRef+4 +S) % S;	
					}
					//case we have y>= A+B+C
					else {
						curr.nextRef = (scanRand.nextInt())%S;
					}
					curr.currRef = curr.nextRef; 
					
					time++;
					
				}
				int done = run.poll();
				run.add(done);
				
				}
			
		}
		else { //case that alg is LIFO
			Stack<Integer> order = new Stack<Integer>(); //stack
			while(numberProcs*N>=time) { //all our processes will have completed when we exceed numberProcs*N
				int atHead = run.peek(); //processNum to run 
				process curr = processes.get(atHead);
				
				//Debug line
				//System.out.println("The one at head is "+atHead + " and the curr is "+ curr.processNum);
				for(int q = 0; q < 3 && curr.N>0; q++) { //want to make sure we do not exceed the number of references for the process
					//simulate the reference for this process
					
					int pageNum = curr.currRef/P; 
					
					//Debug line
					//System.out.print(curr.processNum + " references word "+ curr.currRef + " (page " + pageNum + ") at time " + time+":");
					if(!pageContained(curr.processNum, pageNum, frameTable)) { //not contained
						curr.pageFaults+=1;
						
						//Debug line
						//System.out.print(" Fault");
						if(frameTable.contains(null)) {
							for(int i = frameTable.size()-1; i>=0; i--) {
								if (frameTable.get(i)==null) {
									page temp = new page(pageNum, curr.processNum);
									temp.loadTime = time;
									frameTable.set(i, temp);
									order.push(i);
									//Debug line
									//System.out.println(" using free frame "+i);
									break;
								}
							}
						}
						else {
							LIFO(frameTable,pageNum, numFrames, curr, processes, time, order);
						}
					}
					else { //contained
						//Debug line
						//System.out.println(" Hit");
					}
					
					//decrements number of references 
					curr.N--;
					
					//calculate the next reference for this process
					int randomNum = scanRand.nextInt();
					
					//Debug line
					//System.out.println("Random Num being used is: " + randomNum);
					double y = randomNum/(Integer.MAX_VALUE + 1d);
					if(y < curr.A) {
						curr.nextRef = (curr.currRef+1 + S) % S;
					}
					else if(y<(curr.A+curr.B)) {
						curr.nextRef = (curr.currRef-5 + S) % S;
					}
					else if(y<(curr.A+curr.B+curr.C)){
						curr.nextRef = (curr.currRef+4 +S) % S;	
					}
					//case we have y>= A+B+C
					else {
						curr.nextRef = (scanRand.nextInt())%S;
					}
					curr.currRef = curr.nextRef; 
					
					time++;
					
				}
				int done = run.poll();
				run.add(done);
				
				}
		}
		
		int totalFaults = 0;
		float totalNumEvict = 0;
		float totalResTime = 0;
		
		//output faults & residency 
		for (int i: processes.keySet()) {
			process curr = processes.get(i);

			System.out.print("\nProcess "+ i+ " had "+ curr.pageFaults + " faults");
			totalFaults+=curr.pageFaults;
			
			process current = processes.get(i);
			if(current.numEvictions==0) {
				System.out.println(".\n\t With no evictions, the average residence is undefined. ");
			}
			else {
				totalNumEvict+=curr.numEvictions;
				totalResTime+=curr.residencyTime;
				double avg = (curr.residencyTime)/(curr.numEvictions);
				System.out.println(" and " + avg + " average residency.");
			}
			//System.out.println("PROCESS "+curr.processNum+ " HAS "+ curr.numEvictions + " EVICTIONS");
		}
		
		System.out.println("");
		System.out.print("The total number of faults is "+totalFaults);
		
		if(totalNumEvict==0) {
			System.out.println(".\n\t With no evictions, the overall average residence is undefined. ");
		}
		else {
			double avg = (totalResTime)/(totalNumEvict);
			System.out.printf(" and the overall average residence is %f.",avg);
		}
		
		scanRand.close();
	
	}
	
	/**
	 * This function output edits the frameTable by first checking if there is a free space to put the page or by evicting the least recently used frame.
	 * frameTrack is used to keep track of indices of the page in the frameTable
	 * @param frameTable - stores the current pages in frames
	 * @param pageNum - the pageNum to add
	 * @param frameTrack - keys are the pages stored in frames and values are the indices of the pages in the frameTable
	 * frameTrack is also ordered and keeps track of what has been used the least by moving recently used pages to the end 
	 * @param numFrames - how many frames we have 
	 * @param Proc - the process which the new page is associated with 
	 * @param processes - hashmap of the processes with keys as the number and values as the processes themselves
	 * @param time - current time 
	 */
	static void lru(ArrayList<page> frameTable, int pageNum, LinkedHashMap<page, Integer> frameTrack, int numFrames, process Proc, HashMap<Integer, process> processes, int time) {
		int val = 0;
		int pgEvicted = 0;
		int proNum = 0;
		//empty frame available 
		if(frameTable.contains(null)) {
			
			for (int i = numFrames-1; i >= 0; i--) {
				//found empty frame 
				if (frameTable.get(i)==null) { 
					page temp = new page(pageNum, Proc.processNum);
					temp.loadTime = time;
					frameTable.set(i, temp);
					frameTrack.put(temp, i); //store index of where the page is stored as the value in frame
					//debug line
					//System.out.println(" Using frame: " +i);
					return;
				}
			}
		}
		else { //we need to remove the lru 
			for(page i: frameTrack.keySet()) {
				val = frameTrack.get(i); //index
				pgEvicted = i.pageNum;
				proNum = i.processNum;
				i.evictedTime = time;
				int ProcToEdit = i.processNum;
				process ProcEd = processes.get(ProcToEdit);
				ProcEd.residencyTime+= (i.evictedTime-i.loadTime);
				ProcEd.numEvictions++;
				frameTrack.remove(i); //removes first one
				frameTable.remove(val);
				break; //after the first iteration stop because we just need the first page in the frame 
			}
			page temp = new page(pageNum, Proc.processNum);
			temp.loadTime=time;
			frameTrack.put(temp, val); //gets the index of what was removed
			frameTable.add(val, temp);
			//debug line 
			//System.out.println(" Evicted page "+pgEvicted + " of "+proNum +" Using frame: " +val);
			 
		}
		
	}
	
	/**
	 * This function randomly evicts a page from the frameTable.
	 * @param frameTable - stores the current pages in frames
	 * @param pageNum - the pageNum to add
	 * @param numFrames - how many frames we have 
	 * @param Proc - the process which the new page is associated with 
	 * @param processes - hashmap of the processes with keys as the number and values as the processes themselves
	 * @param time - current time 
	 * @param FrameToEvict - the random number frame to evict 
	 */
	static void random(ArrayList<page> frameTable, int pageNum, int numFrames, process Proc, HashMap<Integer, process> processes, int time, int FrameToEvict) {
		//System.out.println("IN THE RANDOM FUNCTION");
		
		//store info about page to evict
		page pgEvicted = frameTable.get(FrameToEvict);
		//int pgEvictedNum = pgEvicted.pageNum;
		int proNum = pgEvicted.processNum;
		pgEvicted.evictedTime = time;
		int ProcToEdit = pgEvicted.processNum;
		process ProcEd = processes.get(ProcToEdit);
		ProcEd.residencyTime+= (pgEvicted.evictedTime-pgEvicted.loadTime);
		ProcEd.numEvictions++;
		frameTable.remove(FrameToEvict);
		
		//add new page to table
		page temp = new page(pageNum, Proc.processNum);
		temp.loadTime=time;
		frameTable.add(FrameToEvict, temp);
		//debug line 
		//System.out.println(", evicting page "+pgEvicted.pageNum + " of "+proNum +" Using frame: " +FrameToEvict);
	}
	
	/**
	 * This function evicts the frame that has been last used.
	 * @param frameTable - stores the current pages in frames
	 * @param pageNum - the pageNum to add
	 * @param numFrames - how many frames we have 
	 * @param Proc - the process which the new page is associated with
	 * @param processes - hashmap of the processes with keys as the number and values as the processes themselves
	 * @param time - current time 
	 * @param order - stores the order of the index of the frameTable to evict
	 */
	static void LIFO(ArrayList<page> frameTable, int pageNum, int numFrames, process Proc, HashMap<Integer, process> processes, int time, Stack<Integer> order) {
		int toEvictIndex = order.pop(); //pop the last index that was added to the frame 
		page pgEvicted = null;
		
		for(int i = 0; i < frameTable.size(); i++) {
			if(i==toEvictIndex) { //find page to evict (that which has the index to evict 
				pgEvicted = frameTable.get(i);
			}
		}
		int proNum = pgEvicted.processNum;
		pgEvicted.evictedTime = time;
		process ProcEd = processes.get(proNum);
		ProcEd.residencyTime+= (pgEvicted.evictedTime-pgEvicted.loadTime);
		ProcEd.numEvictions++;	
		page temp = new page(pageNum, Proc.processNum);
		temp.loadTime=time;
		frameTable.remove(toEvictIndex);
		frameTable.add(toEvictIndex, temp);
		//debug line 
		//System.out.println(", evicting page "+pgEvicted.pageNum + " of "+proNum +" Using frame: " +toEvictIndex);
		order.push(toEvictIndex); //add to the top of the stack 
		
	}
	
	/**
	 * Contain function to check if the page is already stored in the frameTable
	 * @param processNum - the process of the page we are checking to see if contained 
	 * @param pageNum - the pageNum to add
	 * @param frameTable - stores the current pages in frames
	 * @return true or false depending on whether it is contained 
	 */
	static boolean pageContained(int processNum, int pageNum, ArrayList<page> frameTable) {
		
		for (page pg : frameTable) {
			if(pg!=null) { //avoid null pointer exception
				if(pg.processNum == processNum && pg.pageNum == pageNum) { 
					
					return true;
				}
			}
		}
		return false;
	}
	

	
}
