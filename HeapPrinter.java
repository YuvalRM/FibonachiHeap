import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class HeapPrinter {
	static final PrintStream stream = System.out;

	static void printIndentPrefix(ArrayList<Boolean> hasNexts) {
		int size = hasNexts.size();
		for (int i = 0; i < size - 1; ++i) {
			stream.format("%c   ", hasNexts.get(i).booleanValue() ? '│' : ' ');
		}
	}

	static void printIndent(FibonacciHeap.HeapNode heapNode, ArrayList<Boolean> hasNexts) {
		int size = hasNexts.size();
		printIndentPrefix(hasNexts);

		stream.format("%c── %s\n", hasNexts.get(size - 1) ? '├' : '╰',
				heapNode == null ? "(null)" : String.valueOf(heapNode.getKey()));
	}

	static String repeatString(String s, int count) {
		StringBuilder r = new StringBuilder();
		for (int i = 0; i < count; i++) {
			r.append(s);
		}
		return r.toString();
	}

	static void printIndentVerbose(FibonacciHeap.HeapNode heapNode, ArrayList<Boolean> hasNexts) {
		int size = hasNexts.size();
		if (heapNode == null) {
			printIndentPrefix(hasNexts);
			stream.format("%c── %s\n", hasNexts.get(size - 1) ? '├' : '╰', "(null)");
			return;
		}

		Function<Supplier<FibonacciHeap.HeapNode>, String> keyify = (f) -> {
			FibonacciHeap.HeapNode node = f.get();
			return node == null ? "(null)" : String.valueOf(node.getKey());
		};
		String title = String.format(" Key: %d ", heapNode.getKey());
		List<String> content = Arrays.asList(String.format(" Rank: %d ", heapNode.getRank()),
				String.format(" Marked: %b ", heapNode.getMarked()),
				String.format(" Parent: %s ", keyify.apply(heapNode::getParent)),
				String.format(" Next: %s ", keyify.apply(heapNode::getNext)),
				String.format(" Prev: %s ", keyify.apply(heapNode::getPrev)),
				String.format(" Child: %s", keyify.apply(heapNode::getChild)));

		/* Print details in box */
		int length = Math.max(title.length(), content.stream().map(String::length).max(Integer::compareTo).get());
		String line = repeatString("─", length);
		String padded = String.format("%%-%ds", length);
		boolean hasNext = hasNexts.get(size - 1);

		// print header row
		printIndentPrefix(hasNexts);
		stream.format("%c── ╭%s╮%n", hasNext ? '├' : '╰', line);

		// print title row
		printIndentPrefix(hasNexts);
		stream.format("%c   │" + padded + "│%n", hasNext ? '│' : ' ', title);

		// print separator
		printIndentPrefix(hasNexts);
		stream.format("%c   ├%s┤%n", hasNext ? '│' : ' ', line);

		// print content
		for (String data : content) {
			printIndentPrefix(hasNexts);
			stream.format("%c   │" + padded + "│%n", hasNext ? '│' : ' ', data);
		}

		// print footer
		printIndentPrefix(hasNexts);
		stream.format("%c   ╰%s╯%n", hasNext ? '│' : ' ', line);
	}

	static void printHeapNode(FibonacciHeap.HeapNode heapNode, FibonacciHeap.HeapNode until,
			ArrayList<Boolean> hasNexts, boolean verbose) {
		if (heapNode == null || heapNode == until) {
			return;
		}
		hasNexts.set(hasNexts.size() - 1,
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

	public static void print(FibonacciHeap heap, boolean verbose) {
		if (heap == null) {
			stream.println("(null)");
			return;
		} else if (heap.isEmpty()) {
			stream.println("(empty)");
			return;
		}

		stream.println("╮");
		ArrayList<Boolean> list = new ArrayList<>();
		list.add(false);
		printHeapNode(heap.getFirst(), null, list, verbose);
	}

	public static void demo() {
		/* Build an example */
		FibonacciHeap heap = new FibonacciHeap();

		heap.insert(20);
		heap.insert(8);
		heap.insert(3);
		heap.insert(100);
		heap.insert(15);
		heap.insert(18);
		heap.insert(1);
		heap.insert(2);
		heap.insert(7);
		heap.deleteMin();
		heap.insert(500);

		/* Print */
		stream.println("Printing in verbose mode:");
		HeapPrinter.print(heap, true);

		stream.println("Printing in regular mode:");
		HeapPrinter.print(heap, false);
	}

	public static void main(String[] args) {
		demo();
	}
}