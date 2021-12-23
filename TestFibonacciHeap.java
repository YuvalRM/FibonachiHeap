import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class HeapPrinter {
    static final String NULL = "(null)";
    final PrintStream stream;

    public HeapPrinter(PrintStream stream) {
        this.stream = stream;
    }

    void printIndentPrefix(ArrayList<Boolean> hasNexts) {
        int size = hasNexts.size();
        for (int i = 0; i < size - 1; ++i) {
            this.stream.format("%c   ", hasNexts.get(i) ? '│' : ' ');
        }
    }

    void printIndent(FibonacciHeap.HeapNode heapNode, ArrayList<Boolean> hasNexts) {
        int size = hasNexts.size();
        printIndentPrefix(hasNexts);

        this.stream.format("%c── %s\n",
                hasNexts.get(size - 1) ? '├' : '╰',
                heapNode == null ? NULL : String.valueOf(heapNode.getKey()));
    }

    static String repeatString(String s, int count) {
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < count; i++) {
            r.append(s);
        }
        return r.toString();
    }

    void printIndentVerbose(FibonacciHeap.HeapNode heapNode, ArrayList<Boolean> hasNexts) {
        int size = hasNexts.size();
        if (heapNode == null) {
            printIndentPrefix(hasNexts);
            this.stream.format("%c── %s\n", hasNexts.get(size - 1) ? '├' : '╰', NULL);
            return;
        }

        Function<Supplier<FibonacciHeap.HeapNode>, String> keyify = (f) -> {
            FibonacciHeap.HeapNode node = f.get();
            return node == null ? NULL : String.valueOf(node.getKey());
        };
        String title = String.format(" Key: %d ", heapNode.getKey());
        List<String> content = Arrays.asList(
                String.format(" Rank: %d ", heapNode.getRank()),
                String.format(" Marked: %b ", heapNode.getMarked()),
                String.format(" Parent: %s ", keyify.apply(heapNode::getParent)),
                String.format(" Next: %s ", keyify.apply(heapNode::getNext)),
                String.format(" Prev: %s ", keyify.apply(heapNode::getPrev)),
                String.format(" Child: %s", keyify.apply(heapNode::getChild)));

        /* Print details in box */
        int length = Math.max(
                title.length(),
                content.stream().map(String::length).max(Integer::compareTo).get());
        String line = repeatString("─", length);
        String padded = String.format("%%-%ds", length);
        boolean hasNext = hasNexts.get(size - 1);

        // print header row
        printIndentPrefix(hasNexts);
        this.stream.format("%c── ╭%s╮\n", hasNext ? '├' : '╰', line);

        // print title row
        printIndentPrefix(hasNexts);
        this.stream.format("%c   │" + padded + "│\n", hasNext ? '│' : ' ', title);

        // print separator
        printIndentPrefix(hasNexts);
        this.stream.format("%c   ├%s┤\n", hasNext ? '│' : ' ', line);

        // print content
        for (String data : content) {
            printIndentPrefix(hasNexts);
            this.stream.format("%c   │" + padded + "│\n", hasNext ? '│' : ' ', data);
        }

        // print footer
        printIndentPrefix(hasNexts);
        this.stream.format("%c   ╰%s╯\n", hasNext ? '│' : ' ', line);
    }

    void printHeapNode(FibonacciHeap.HeapNode heapNode, FibonacciHeap.HeapNode until,
            ArrayList<Boolean> hasNexts, boolean verbose) {
        if (heapNode == null || heapNode == until) {
            return;
        }
        hasNexts.set(
                hasNexts.size() - 1,
                heapNode.getNext() != null && heapNode.getNext() != heapNode && heapNode.getNext() != until);
        if (verbose) {
            printIndentVerbose(heapNode, hasNexts);
        } else {
            printIndent(heapNode, hasNexts);
        }

        hasNexts.add(false);
        printHeapNode(heapNode.getChild(), null, hasNexts, verbose);
        hasNexts.remove(hasNexts.size() - 1);

        until = until == null ? heapNode : until;
        printHeapNode(heapNode.getNext(), until, hasNexts, verbose);
    }

    public void print(FibonacciHeap heap, boolean verbose) {
        if (heap == null) {
            this.stream.println(NULL);
            return;
        } else if (heap.isEmpty()) {
            this.stream.print("(empty)\n");
            return;
        }

        this.stream.print("╮\n");
        ArrayList<Boolean> list = new ArrayList<>();
        list.add(false);
        printHeapNode(heap.getFirst(), null, list, verbose);
    }
}

public class TestFibonacciHeap {
    static HeapPrinter heapPrinter = new HeapPrinter(System.out);

    static void print(FibonacciHeap heap) {
        boolean verbose = true;
        heapPrinter.print(heap, verbose);
    }

    int assertValidHeapRoots(FibonacciHeap heap) {
        int numberOfTrees = 0;
        Map<Integer, Integer> actualRanks = new HashMap<>();
        FibonacciHeap.HeapNode node = heap.getFirst();
        FibonacciHeap.HeapNode actualMin = node;
        int[] ranks = heap.countersRep();
        FibonacciHeap.HeapNode min = heap.findMin();
        int potential = heap.potential();

        /* Check roots */
        do {
            numberOfTrees++;
            assertNull(node.getParent());
            assertFalse(node.getMarked());
            if (node.getKey() < actualMin.getKey()) {
                actualMin = node;
            }

            if (node.getNext() != null) {
                assertSame(node, node.getNext().getPrev());
            }
            if (node.getPrev() != null) {
                assertSame(node, node.getPrev().getNext());
            }

            actualRanks.merge(node.rank, 1, Integer::sum); // increase value by 1 or put 1 if absent

            node = node.getNext();
        } while (node != null && node != heap.getFirst());

        for (int i = 0; i < ranks.length; ++i) {
            assertEquals(
                    ranks[i], actualRanks.getOrDefault(i, 0),
                    String.format(
                            "Expected %d roots of rank %d but found %d",
                            ranks[i], i, actualRanks.getOrDefault(i, 0)));
        }

        String details = min.getKey() < actualMin.getKey() ? "findMin() node is NOT a sibling of getFirst() node" : "";
        assertSame(
                min, actualMin,
                String.format(
                        "received key %d from findMin but found min root key %d in heap. %s",
                        min.getKey(), actualMin.getKey(), details));

        assertTrue(potential >= numberOfTrees);
        assertTrue((potential - numberOfTrees) % 2 == 0);

        return numberOfTrees;
    }

    int assertValidHeapNodes(FibonacciHeap heap) {
        /* Check all nodes */
        FibonacciHeap.HeapNode node = heap.getFirst();
        Stack<FibonacciHeap.HeapNode> stack = new Stack<>();
        Set<FibonacciHeap.HeapNode> visited = new HashSet<>();
        int actualSize = 0;
        int numberOfMarked = 0;

        visited.add(null);
        stack.add(node);

        /* Traverse the heap using 'pre-order' DFS */
        while (!visited.contains(node) || !stack.empty()) {
            if (visited.contains(node)) {
                node = stack.pop();
                continue;
            }
            visited.add(node);

            // Check current node
            actualSize++;
            stack.push(node.getNext());
            numberOfMarked += node.getMarked() ? 1 : 0;

            // Check its relations to its children
            if (node.getChild() == null) {
                assertEquals(0, node.getRank());
            } else {
                int childrenCount = 0;
                FibonacciHeap.HeapNode currentChild = node.getChild();
                do {
                    childrenCount++;
                    assertSame(node, currentChild.getParent());

                    // Check heap property
                    if(currentChild.getKey()<=node.getKey()) {
                    	System.out.println(currentChild.getKey());
                    	System.out.println(node.getKey());
                    }
                    assertTrue(currentChild.getKey() > node.getKey());

                    currentChild = currentChild.getNext();

                } while (currentChild != null && currentChild != node.getChild());
                assertEquals(
                        childrenCount, node.getRank(),
                        String.format(
                                "Node with key %d has rank %d but only %d %s",
                                node.getKey(), node.getRank(), childrenCount,
                                childrenCount == 1 ? "child" : "children"));
            }

            node = node.getChild();
        }

        assertEquals(heap.size(), actualSize);
        assertTrue(heap.potential() >= numberOfMarked * 2);

        return numberOfMarked;
    }

    void assertValidHeap(FibonacciHeap heap) {

        int size = heap.size();

        FibonacciHeap.HeapNode node = heap.getFirst();
        FibonacciHeap.HeapNode min = heap.findMin();

        /* Handle empty heap */
        assertTrue(heap.isEmpty() == (size == 0));
        if (heap.isEmpty()) {
            assertNull(min);
            assertNull(node);
            assertEquals(0, heap.countersRep().length);
            assertEquals(0, heap.potential());
            return;
        }

        assertNotNull(node);
        assertNotNull(min);
        assertNull(min.parent);

        int numberOfTrees = assertValidHeapRoots(heap);
        int numberOfMarked = assertValidHeapNodes(heap);

        assertEquals(numberOfTrees + numberOfMarked * 2, heap.potential());
    }

    Map<Integer, FibonacciHeap.HeapNode> testInsertion(FibonacciHeap heap, int... keys) {
        Map<Integer, FibonacciHeap.HeapNode> nodes = new HashMap<>();
        FibonacciHeap.HeapNode minNode = heap.findMin();
        int startPotential = heap.potential();
        int startSize = heap.size();

        for (int key : keys) {
            FibonacciHeap.HeapNode current = heap.insert(key);
            assertValidHeap(heap);
            minNode = minNode == null || key < minNode.getKey() ? current : minNode;
            nodes.put(key, current);
        }

        assertSame(minNode, heap.findMin());
        assertEquals(keys.length, heap.potential() - startPotential);
        assertEquals(keys.length, heap.size() - startSize);

        return nodes;
    }

    void testDeletion(FibonacciHeap heap, FibonacciHeap.HeapNode... nodes) {
        int startSize = heap.size();

        for (FibonacciHeap.HeapNode node : nodes) {
            heap.delete(node);
            assertValidHeap(heap);
        }

        assertEquals(nodes.length, startSize - heap.size());
    }

    FibonacciHeap heap = new FibonacciHeap();

    @BeforeEach
    void beforeEachTest(TestInfo testInfo) {
        heap = new FibonacciHeap();
    }

    @AfterEach
    void afterEachTest(TestInfo testInfo) throws IOException {
        File dir = new File("result");
        dir.mkdirs();
        File file = new File(dir, testInfo.getDisplayName() + ".txt");
        file.createNewFile();
        try (PrintStream stream = new PrintStream(file)) {
            HeapPrinter printer = new HeapPrinter(stream);
            printer.print(heap, true);
        }

        String expected = new String(
                Files.readAllBytes(Paths.get("./expected/" + testInfo.getDisplayName() + ".txt")),
                StandardCharsets.UTF_8);
        String result = new String(
                Files.readAllBytes(Paths.get("./result/" + testInfo.getDisplayName() + ".txt")),
                StandardCharsets.UTF_8);

        assertEquals(expected, result);
    }

    @Test
    public void testConstructorSanity() {
        assertValidHeap(heap);
        assertTrue(heap.isEmpty());
    }

    @Test
    public void testInsertDeleteSanity() {
        // case 2
        Map<Integer, FibonacciHeap.HeapNode> nodes = testInsertion(heap, 2);
        testDeletion(heap, nodes.get(2));
        assertTrue(heap.isEmpty());
    }

    @Test
    public void testkMinSanity() {
        // case 14
        testInsertion(heap, 1);
        int[] keys = FibonacciHeap.kMin(heap, 1);
        assertEquals(1, keys.length);
        assertEquals(1, keys[0]);
        assertEquals(1, heap.size());
        assertEquals(1, heap.findMin().getKey());
        assertSame(heap.findMin(), heap.getFirst());
        assertEquals(1, heap.potential());
    }

    @Test
    public void testCountersRepSanity() {
        // case 12
        testInsertion(heap, 1);
        int[] arr = heap.countersRep();
        assertValidHeap(heap);
        assertEquals(1, heap.size());
        assertEquals(1, heap.findMin().getKey());
        assertTrue(Arrays.equals(new int[] { 1 }, arr));
    }

    @Test
    public void testInsertionDeletion1() {
        // case 1
        Map<Integer, FibonacciHeap.HeapNode> nodes = testInsertion(
                heap, 2, 1, 3, 7, 4, 8, 6, 5, 9, 10, 11);
        heap.deleteMin();
        assertValidHeap(heap);
        testDeletion(heap, nodes.get(9));
    }

    @Test
    public void testInsertionDeletion2() {
        // case 3
        Map<Integer, FibonacciHeap.HeapNode> nodes = testInsertion(
                heap, 2, 1, 3);
        testDeletion(heap, nodes.get(2));
    }

    @Test
    public void testInsertionDeletion3() {
        // case 4
        testInsertion(heap, 20, 8, 3, 100, 15, 18, 1);
        heap.deleteMin();
        assertValidHeap(heap);
        assertEquals(3, heap.findMin().getKey());
    }

    @Test
    public void testInsertionDeletion4() {
        // case 5
        testInsertion(heap, 7, 2, 1, 18, 15, 100, 3, 8, 20);
        heap.deleteMin();
        assertValidHeap(heap);
        assertEquals(2, heap.findMin().getKey());
        testInsertion(heap, 500);
    }

    @Test
    public void testCut() {
        // case 8
        Map<Integer, FibonacciHeap.HeapNode> nodes = testInsertion(
                heap, 2, 1, 3, 7, 4, 8, 6, 5, 9, 10, 11);
        heap.deleteMin();
        assertValidHeap(heap);
        testDeletion(heap, nodes.get(5));
    }

    @Test
    public void testCutDirectIndirectChild() {
        // case 13
        Map<Integer, FibonacciHeap.HeapNode> nodes = testInsertion(
                heap, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        heap.deleteMin();
        assertValidHeap(heap);
        assertSame(nodes.get(1), heap.findMin());
        heap.decreaseKey(nodes.get(5), 9);
        assertValidHeap(heap);
        heap.decreaseKey(nodes.get(6), 20);
        assertValidHeap(heap);
    }

    @Test
    public void cascadingCuts() {
        // case 9
        Map<Integer, FibonacciHeap.HeapNode> nodes = testInsertion(
                heap, 2, 1, 3, 7, 4, 8, 6, 5, 9, 10, 11, 12, 13, 14, 15, 16, 17);
        assertEquals(17, heap.potential());
        heap.deleteMin();
        assertValidHeap(heap);
        assertEquals(1, heap.potential());
        heap.decreaseKey(nodes.get(16), 17);
        assertValidHeap(heap);
        assertEquals(4, heap.potential());
        heap.decreaseKey(nodes.get(12), 14);
        assertValidHeap(heap);
        assertEquals(7, heap.potential());
        testDeletion(heap, nodes.get(15));
        assertEquals(4, heap.potential());
    }

    @Test
    public void testkMinEmpty() {
        int[] keys = FibonacciHeap.kMin(heap, 0);
        assertEquals(0, keys.length);
        assertTrue(heap.isEmpty());
        assertValidHeap(heap);
    }

    @Test
    public void testkMinBinomial() {
        // case 6
        testInsertion(heap, 7, 2, 1, 18, 15, 100, 3, 8, 20);
        heap.deleteMin();
        assertValidHeap(heap);
        int[] arr = FibonacciHeap.kMin(heap, 8);
        assertValidHeap(heap);
        assertEquals(8, heap.size());
        assertTrue(Arrays.equals(new int[] { 2, 3, 7, 8, 15, 18, 20, 100 }, arr));
    }

    @Test
    public void testkMinSingle() {
        // case 7
        testInsertion(heap, 7, 6);
        heap.deleteMin();
        assertValidHeap(heap);
        int[] arr = FibonacciHeap.kMin(heap, 1);
        assertTrue(Arrays.equals(new int[] { 7 }, arr));
    }
    
    @Test
	public void StressTest() {
		final int changes = 35;
		final int meldings = 5;
		final int iterations = 10000;
		FibonacciHeap heap = new FibonacciHeap();
		FibonacciHeap heap2 = new FibonacciHeap();
		Map<Integer, FibonacciHeap.HeapNode> nodes = new HashMap<>();
		Map<Integer, FibonacciHeap.HeapNode> nodes2 = new HashMap<>();
		for (int j = 0; j < iterations; j++) {
			System.out.println("Iteration #"+String.valueOf(j));
			heap = new FibonacciHeap();
			heap2 = new FibonacciHeap();
			nodes = new HashMap<>();
			nodes2 = new HashMap<>();
			for (int i = 0; i < meldings; i++) {
				nodes2 = new HashMap<>();
				heap2 = new FibonacciHeap();
				insertKRandomKeys(1000, nodes, heap, 0, (i+1) * 1000);
				insertKRandomKeys(1000, nodes2, heap2, (i + 1) * 1000 + 1, (i + 2) * 1000);
				assertValidHeap(heap);
				assertValidHeap(heap2);
				for (int k = 0; k < changes; k++) {
					Random rand = new Random();
					int indicator=rand.nextInt(3);
					deleteOrDecreaseOrMin(nodes, heap,indicator);
					deleteOrDecreaseOrMin(nodes2, heap2,indicator);
					assertValidHeap(heap);
					assertValidHeap(heap2);
					
				}
				meld(nodes, heap, nodes2, heap2);
				assertValidHeap(heap);
			}
		}

	}

	private void insertKRandomKeys(int k, Map<Integer, FibonacciHeap.HeapNode> nodes, FibonacciHeap heap, int lowBound,
			int upperBound) {
		if (lowBound >= upperBound) {
			return;
		}
		Random rand = new Random();
		for (int i = 0; i < k; i++) {
			int toIns = rand.nextInt(upperBound - lowBound) + lowBound;
			inserNode(nodes, heap, toIns);
		}
	}

	private void inserNode(Map<Integer, FibonacciHeap.HeapNode> nodes, FibonacciHeap heap, int num) {
		if (!(nodes.containsKey(num))) {
			nodes.put(num, heap.insert(num));
		}
	}

	private void deleteOrDecreaseOrMin(Map<Integer, FibonacciHeap.HeapNode> nodes, FibonacciHeap heap,int indicator) {
		Random rand = new Random();
		if (heap.isEmpty()) {
			return;
		}
		
		List<Entry<Integer, FibonacciHeap.HeapNode>> keys = null;
		if (indicator < 2) {
			keys = new LinkedList<>(nodes.entrySet());
			Collections.shuffle(keys);
		}
		if (indicator == 0) {
			heap.delete(keys.get(0).getValue());
			nodes.remove(keys.get(0).getKey());
		} else if (indicator == 1) {
			int decreaseVal = rand.nextInt(2000)+1;
			int key = keys.get(0).getKey();
			if (!(nodes.containsKey(key - decreaseVal))) {
				heap.decreaseKey(keys.get(0).getValue(), decreaseVal);
				nodes.remove(keys.get(0).getKey());
				nodes.put(keys.get(0).getKey() - decreaseVal, keys.get(0).getValue());
			}
		} else {
			heap.deleteMin();
			nodes.remove(heap.findMin().getKey());
		}
	}

	private void meld(Map<Integer, FibonacciHeap.HeapNode> nodes, FibonacciHeap heap,
			Map<Integer, FibonacciHeap.HeapNode> nodes2, FibonacciHeap heap2) {
		heap.meld(heap2);
		nodes.putAll(nodes2);
	}
}
