import java.util.*;
import java.util.Map.Entry;

public class StressTest {
	public void StressTest() {
		final int changes = 35;
		final int meldings = 5;
		final int iterations = 10000;
		FibonacciHeap heap = new FibonacciHeap();
		FibonacciHeap heap2 = new FibonacciHeap();
		Map<Integer, FibonacciHeap.HeapNode> nodes = new HashMap<>();
		Map<Integer, FibonacciHeap.HeapNode> nodes2 = new HashMap<>();
		for (int j = 0; j < iterations; j++) {
			heap = new FibonacciHeap();
			heap2 = new FibonacciHeap();
			nodes = new HashMap<>();
			nodes2 = new HashMap<>();
			for (int i = 0; i < meldings; i++) {
				nodes2 = new HashMap<>();
				heap2 = new FibonacciHeap();
				insertKRandomKeys(1000, nodes, heap, 0, i * 1000);
				insertKRandomKeys(1000, nodes2, heap2, (i + 1) * 1000 + 1, (i + 2) * 1000);
				for (int k = 0; k < changes; k++) {
					deleteOrDecreaseOrMin(nodes, heap);
					deleteOrDecreaseOrMin(nodes2, heap2);
				}
				meld(nodes, heap, nodes2, heap2);
			}
		}

	}

	public void insertKRandomKeys(int k, Map<Integer, FibonacciHeap.HeapNode> nodes, FibonacciHeap heap) {
		insertKRandomKeys(k, nodes, heap, 0, 1000);
	}

	public void insertKRandomKeys(int k, Map<Integer, FibonacciHeap.HeapNode> nodes, FibonacciHeap heap, int lowBound,
			int upperBound) {
		if (lowBound > upperBound) {
			return;
		}
		Random rand = new Random();
		for (int i = 0; i < k; i++) {
			int toIns = rand.nextInt(upperBound - lowBound) + lowBound;
			inserNode(nodes, heap, toIns);
		}
	}

	public void inserNode(Map<Integer, FibonacciHeap.HeapNode> nodes, FibonacciHeap heap, int num) {
		if (!nodes.containsKey(num)) {
			nodes.put(num, heap.insert(num));
		}
	}

	public void deleteOrDecreaseOrMin(Map<Integer, FibonacciHeap.HeapNode> nodes, FibonacciHeap heap) {

		if (heap.isEmpty()) {
			return;
		}
		Random rand = new Random();
		int indicator = rand.nextInt(3);
		List<Entry<Integer, FibonacciHeap.HeapNode>> keys = null;
		if (indicator < 2) {
			keys = new LinkedList<>(nodes.entrySet());
			Collections.shuffle(keys);
		}
		if (indicator == 0) {
			heap.delete(keys.get(0).getValue());
			nodes.remove(keys.get(0).getKey());
		} else if (indicator == 1) {
			int decreaseVal = rand.nextInt(2000);
			int key = keys.get(0).getKey();
			if (!(nodes.containsKey(key - decreaseVal))) {
				heap.decreaseKey(keys.get(0).getValue(), decreaseVal);
				nodes.remove(keys.get(0).getKey());
				nodes.put(keys.get(0).getKey() - decreaseVal, keys.get(0).getValue());
			}
		} else {
			nodes.remove(heap.findMin().getKey());
			heap.deleteMin();
		}
	}

	public void meld(Map<Integer, FibonacciHeap.HeapNode> nodes, FibonacciHeap heap,
			Map<Integer, FibonacciHeap.HeapNode> nodes2, FibonacciHeap heap2) {
		heap.meld(heap2);
		nodes.putAll(nodes2);
	}
}
