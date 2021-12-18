import java.util.Arrays;

public class TestsForHeap {
	public static void main(String[] args) {
		FibonacciHeap a=new FibonacciHeap();
		FibonacciHeap.HeapNode eight=a.insert(8);
		FibonacciHeap.HeapNode six=a.insert(6);
		a.insert(-3);
		a.insert(17);
		a.insert(-8);
		FibonacciHeap.HeapNode node2= a.insert(2);
		a.insert(1);
		a.insert(100);
		a.deleteMin();
		HeapPrinter.print(a, true);
		a.deleteMin();
		FibonacciHeap.HeapNode node=a.insert(32);
		a.decreaseKey(node, 12);
		a.decreaseKey(node, 21);
		a.delete(node2);
		HeapPrinter.print(a, true);
		a.delete(six);
		HeapPrinter.print(a, true);
		a.delete(eight);
		HeapPrinter.print(a, true);
		int [] arr=FibonacciHeap.kMin(a,3);
		System.out.println(Arrays.toString(arr));
		
		FibonacciHeap b=new FibonacciHeap();
		for(int i=0;i<150;i++) {
			b.insert(i);
		}
		for(int i =0; i<15;i++) {
			b.deleteMin();
		}
		HeapPrinter.print(b, true);
	}
}
