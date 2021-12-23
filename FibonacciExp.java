
public class FibonacciExp {
	public static void main(String[] args) {
		double times[]=new double[5];
		for(int i=0;i<4;i++) {
			times[i]=System.currentTimeMillis();
			Exp(10+5*i);
		}
		times[4]=System.currentTimeMillis();
		for(int i=0;i<4;i++) {
			System.out.println("The "+ String.valueOf(i)+"th iteration took");
			System.out.println(times[i+1]-times[i]);
			System.out.println("time");
		}

	}
	public static void Exp(int k) {
		FibonacciHeap expKtree = new FibonacciHeap();
		int m = (int) Math.pow(2, k);
		FibonacciHeap.HeapNode[] nodes = new FibonacciHeap.HeapNode[m];
		for (int i = m - 1; i >= -1; i--) {
			if (i != -1) {
				nodes[i]=expKtree.insert(i);
			}
			else {
				expKtree.insert(-1);
			}
		}
		//expKtree.deleteMin();
		
		for(int i=k;i>0;i--) {
			expKtree.decreaseKey(nodes[m-(int)Math.pow(2, i)+1], m+1);
		}
		System.out.println(FibonacciHeap.totalCuts());
		System.out.println(FibonacciHeap.totalLinks());
		System.out.println(expKtree.potential());
		
	}
}
