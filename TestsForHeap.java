import FibonacciHeap.HeapNode;

public class TestsForHeap {
	public static void main(String[] args) {
		FibonacciHeap a=new FibonacciHeap();
		a.insert(8);
		a.insert(6);
		a.insert(-3);
		System.out.println(a.findMin().getKey());
		a.insert(17);
		a.insert(-8);
		System.out.println(a.findMin().getKey());
		FibonacciHeap.HeapNode node2= a.insert(2);
		a.insert(1);
		a.insert(100);
		System.out.println(a.findMin().getKey());
		a.deleteMin();
		System.out.println(a.findMin().getKey());
		a.deleteMin();
		System.out.println(a.findMin().getKey());
		FibonacciHeap.HeapNode node=a.insert(32);
		a.decreaseKey(node, 12);
		System.out.println(a.findMin().getKey());
		a.decreaseKey(node, 21);
		System.out.println(a.findMin().getKey());
		a.delete(node2);
		System.out.println(a.findMin().getKey());
	}
}
