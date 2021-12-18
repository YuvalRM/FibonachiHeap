/**
 * FibonacciHeap
 *
 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap {
	protected HeapNode first;
	protected HeapNode min;
	protected int size;
	protected static int links;
	protected static int cuts;
	protected int trees;
	protected int marked;

	// empty constructor

	/**
	 * public boolean isEmpty()
	 *
	 * Returns true if and only if the heap is empty.
	 * 
	 */
	public boolean isEmpty() {
		return first == null;
	}

	protected void resetHeap() {
		trees = 0;
		marked = 0;
		size = 0;
		first = null;
		min = null;
	}

	protected void connect2Nodes(HeapNode left, HeapNode right) {
		left.setNext(right);
		right.setPrev(left);
	}

	/**
	 * public HeapNode insert(int key)
	 *
	 * Creates a node (of type HeapNode) which contains the given key, and inserts
	 * it into the heap. The added key is assumed not to already belong to the heap.
	 * 
	 * Returns the newly created node.
	 */
	public HeapNode insert(int key) {
		HeapNode node = new HeapNode(key);
		size++;
		insertNode(node);
		return node;

	}

	protected void insertNode(HeapNode node) {
		trees++;
		if (this.isEmpty()) {
			min = node;
			connect2Nodes(node, node);
		} else {
			if (min.getKey() > node.getKey()) {
				min = node;
			}
			HeapNode last = first.getPrev();
			connect2Nodes(node, first);
			connect2Nodes(last, node);
		}
		first = node;
	}

	/**
	 * public void deleteMin()
	 *
	 * Deletes the node containing the minimum key.
	 *
	 */
	public void deleteMin() {
		size--;
		if (size == 0 || size == -1) {
			resetHeap();
			return;
		} else {
			if (first == min) {
				if (min.getChild() != null) {
					first = min.getChild();
				} else {
					first = min.getNext();
				}
			}
			HeapNode child = min.getChild();
			if (child == null) {
				HeapNode nextMin = min.getNext();
				HeapNode prevMin = min.getPrev();
				connect2Nodes(prevMin, nextMin);
			} else {
				HeapNode node = child;
				do {
					if (node.isMark()) {
						marked--;
					}
					node.setMark(false);
					node.setParent(null);
					node = node.getNext();
				} while (node != child);
				if (min.getNext() == min) {
					first = child;
				} else {
					node = child.getPrev();
					HeapNode nextMin = min.getNext();
					HeapNode prevMin = min.getPrev();
					connect2Nodes(node, nextMin);
					connect2Nodes(prevMin, child);
				}
			}

		}
		this.consolidate();

	}

	protected void consolidate() {
		HeapNode[] ranks = new HeapNode[(int) Math.ceil(Math.log((double) size))*2 + 2];
		HeapNode node = first;
		first.getPrev().setNext(null); // so we know where to stop
		while (node != null) {
			HeapNode next = node.getNext();
			int k = node.getRank();
			while (ranks[k] != null) {
				node = link(node, ranks[k]);
				ranks[k] = null;
				k++;
				assert (k <= (int) Math.ceil(Math.log((double) size)) * 2);
			}

			ranks[k] = node;
			node = next;
		}
		first = null;
		this.trees = 0;
		for (HeapNode tree : ranks) {
			if (tree != null) {
				insertNode(tree);
			}
		}
	}

	protected HeapNode link(HeapNode parent, HeapNode child) {
		links++;
		if (child.getKey() < parent.getKey()) {
			HeapNode temp = parent;
			parent = child;
			child = temp;
		}
		child.setParent(parent);
		child.setMark(false);
		if (parent.getChild() == null) {
			parent.setChild(child);
			connect2Nodes(child, child);

		} else {
			HeapNode thisChild = parent.getChild();
			HeapNode lastChild = thisChild.getPrev();
			connect2Nodes(lastChild, child);
			connect2Nodes(child, thisChild);
			parent.setChild(child);
		}
		parent.setRank(parent.getRank() + 1);
		return parent;
	}

	/**
	 * public HeapNode findMin()
	 *
	 * Returns the node of the heap whose key is minimal, or null if the heap is
	 * empty.
	 *
	 */
	public HeapNode findMin() {

		return min;// should be replaced by student code
	}

	/**
	 * public void meld (FibonacciHeap heap2)
	 *
	 * Melds heap2 with the current heap.
	 *
	 */
	public void meld(FibonacciHeap heap2) {
		HeapNode thisLast = this.first.getPrev();
		HeapNode thatLast = heap2.first.getPrev();
		HeapNode thisFirst = this.first;
		HeapNode thatFirst = heap2.first;
		connect2Nodes(thisLast, thatFirst);
		connect2Nodes(thatLast, thisFirst);
		this.size += heap2.size;
		this.trees += heap2.trees;
		this.marked += heap2.marked;
		min = this.min.getKey() < heap2.min.getKey() ? min : heap2.min;
	}

	/**
	 * public int size()
	 *
	 * Returns the number of elements in the heap.
	 * 
	 */
	public int size() {
		return this.size; // should be replaced by student code
	}

	/**
	 * public int[] countersRep()
	 *
	 * Return an array of counters. The i-th entry contains the number of trees of
	 * order i in the heap. Note: The size of of the array depends on the maximum
	 * order of a tree, and an empty heap returns an empty array.
	 * 
	 */
	public int[] countersRep() {
		if (this.isEmpty()) {
			return new int[0];
		}
		HeapNode node = this.first;
		int max_rank = 0;

		do {
			if (node.getRank() >= max_rank) {
				max_rank = node.getRank();
			}
			node = node.getNext();
		} while (node != this.first);

		int[] res = new int[max_rank + 1];

		node = this.first;

		do {
			int rank = node.getRank();
			assert (rank <= max_rank);
			res[rank]++;
		} while (node != this.first);

		return res;
	}

	/**
	 * public void delete(HeapNode x)
	 *
	 * Deletes the node x from the heap. It is assumed that x indeed belongs to the
	 * heap.
	 *
	 */
	public void delete(HeapNode x) {
		assert (x.getKey() >= min.getKey());
		if (x != min) {
			decreaseKey(x, x.getKey() - this.min.getKey() + 2);
		}
		this.deleteMin();
	}

	/**
	 * public void decreaseKey(HeapNode x, int delta)
	 *
	 * Decreases the key of the node x by a non-negative value delta. The structure
	 * of the heap should be updated to reflect this change (for example, the
	 * cascading cuts procedure should be applied if needed).
	 */
	public void decreaseKey(HeapNode x, int delta) {
		x.setKey(x.getKey() - delta);
		if (x.getKey() < this.min.getKey()) {
			this.min = x;
		}
		if (x.getParent() == null || x.getParent().getKey() < x.getKey()) {
			return;
		}
		cascadingCuts(x, x.getParent());

	}

	protected void cascadingCuts(HeapNode child, HeapNode parent) {
		assert (parent != null);
		assert (child.getParent() == parent);
		if (child.isMark()) {
			this.marked--;
		}
		cut(child, parent);
		insertNode(child);
		if (parent.getParent() != null) {
			if (parent.isMark()) {
				cascadingCuts(parent, parent.getParent());

			} else {
				parent.setMark(true);
				this.marked++;
			}

		}
	}

	protected void cut(HeapNode child, HeapNode parent) {
		cuts++;
		assert (child.getParent() == parent);
		child.setParent(null);
		if (child.getNext() == child) {
			parent.setChild(null);
		} else {
			parent.setChild(child.getNext());
			HeapNode next = child.getNext();
			HeapNode prev = child.getPrev();
			connect2Nodes(prev, next);
		}
		parent.setRank(parent.getRank() - 1);
	}

	/**
	 * public int potential()
	 *
	 * This function returns the current potential of the heap, which is: Potential
	 * = #trees + 2*#marked
	 * 
	 * In words: The potential equals to the number of trees in the heap plus twice
	 * the number of marked nodes in the heap.
	 */
	public int potential() {
		return trees + 2 * marked; // should be replaced by student code
	}

	/**
	 * public static int totalLinks()
	 *
	 * This static function returns the total number of link operations made during
	 * the run-time of the program. A link operation is the operation which gets as
	 * input two trees of the same rank, and generates a tree of rank bigger by one,
	 * by hanging the tree which has larger value in its root under the other tree.
	 */
	public static int totalLinks() {
		return links; // should be replaced by student code
	}

	/**
	 * public static int totalCuts()
	 *
	 * This static function returns the total number of cut operations made during
	 * the run-time of the program. A cut operation is the operation which
	 * disconnects a subtree from its parent (during decreaseKey/delete methods).
	 */
	public static int totalCuts() {
		return cuts; // should be replaced by student code
	}

	/**
	 * public static int[] kMin(FibonacciHeap H, int k)
	 *
	 * This static function returns the k smallest elements in a Fibonacci heap that
	 * contains a single tree. The function should run in O(k*deg(H)). (deg(H) is
	 * the degree of the only tree in H.)
	 * 
	 * ###CRITICAL### : you are NOT allowed to change H.
	 */
	public static int[] kMin(FibonacciHeap H, int k) {
		int[] res = new int[k];
		FibonacciHeap help = new FibonacciHeap();
		help.insert(H.findMin().getKey()).kmin_help = H.findMin();

		for (int i = 0; i < k; i++) {
			HeapNode node = help.findMin();
			res[i] = node.getKey();
			help.deleteMin();
			HeapNode child = node.kmin_help.getChild();
			if (child != null) {
				do {
					help.insert(child.getKey()).kmin_help = child;
					child = child.getNext();
				} while (child != node.kmin_help.child);
			}
		}

		return res;
	}

	public HeapNode getFirst() {
		return this.first;
	}

	/**
	 * public class HeapNode
	 * 
	 * If you wish to implement classes other than FibonacciHeap (for example
	 * HeapNode), do it in this file, not in another file.
	 * 
	 */
	public static class HeapNode {

		public int key;
		protected int rank;
		protected boolean mark;
		protected HeapNode kmin_help;
		protected HeapNode child;
		protected HeapNode prev;
		protected HeapNode parent;
		protected HeapNode next;

		public HeapNode(int key) {
			this.key = key;
			mark = false;
			rank = 0;
		}

		public int getKey() {
			return this.key;
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}

		public boolean isMark() {
			return mark;
		}

		public void setMark(boolean mark) {
			this.mark = mark;
		}

		public HeapNode getChild() {
			return child;
		}

		public void setChild(HeapNode child) {
			this.child = child;
		}

		public HeapNode getPrev() {
			return prev;
		}

		public void setPrev(HeapNode prev) {
			this.prev = prev;
		}

		public HeapNode getParent() {
			return parent;
		}

		public void setParent(HeapNode parent) {
			this.parent = parent;
		}

		public HeapNode getNext() {
			return next;
		}

		public void setNext(HeapNode next) {
			this.next = next;
		}

		public void setKey(int key) {
			this.key = key;
		}

		public boolean getMarked() {
			return mark;
		}

	}
}
