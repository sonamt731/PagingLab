# PagingLab
This project simulates demand paging and how the number of page faults depends on page size, program size, replacement algorithm, and the job mx.

The program is invoked with 6 command line arguments:

- M, the machine size in words.
- P, the page size in words.
- S, the size of each process, i.e., the references are to virtual addresses 0..S-1.
- J, the “job mix”, which determines A, B, and C, as described below.
- N, the number of references for each process.
- R, the replacement algorithm, LIFO (NOT FIFO), RANDOM, or LRU.

Driver reads all input and simulates the N memory references per program. 
Based on the Job Mix there are 4 possible sets of processes which my program accounts for. 

Note that amongst all processes and algorithms the first word referenced is (111*k mod S) where k is the process number


Running the Code on Crackle:

The random-numbers.txt file must be within the same directory.
1) cd PagingLab
2) javac paging.java page.java process.java
3) Now run the code with java paging (*insert command line arguments here)

Based on the replacement algorithm entered, the code will either call LRU, Random, or FIFO. 


