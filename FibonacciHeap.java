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

	// empty constructor O(1)

	/**
	 * public boolean isEmpty()
	 *
	 * Returns true if and only if the heap is empty.
	 * 
	 * Time complexity:
	 * 
	 * O(1)
	 * 
	 */
	public boolean isEmpty() {
		return first == null;
	}

	/**
	 * protected void resetHeap()
	 * 
	 * reset the heap set all the variables as if the heap was just initialized
	 * 
	 * Time complexity:
	 * 
	 * O(1)
	 * 
	 */
	protected void resetHeap() {
		trees = 0;
		marked = 0;
		size = 0;
		first = null;
		min = null;
	}

	/**
	 * protected void connect2Nodes(HeapNode left, HeapNode right)
	 * 
	 * Receives 2 nodes and connects them setting one to be the next of the other.
	 * 
	 * Time complexity:
	 * 
	 * O(1)
	 * 
	 */
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
	 * 
	 * Time complexity:
	 * 
	 * O(1)
	 */
	public HeapNode insert(int key) {
		HeapNode node = new HeapNode(key); // create a new Node
		size++;// Increase the size of the heap
		insertNode(node); // insert the newly created node
		return node;

	}
	/**
	 * protected void insertNode(HeapNode node)
	 * 
	 * the method receives a root of a tree and insert it as is to the heap as the first heap.
	 * 
	 * Time complexity:
	 * 
	 * O(1)
	 * 
	 */
	protected void insertNode(HeapNode node) {
		trees++; //Increase the amount of trees
		if (this.isEmpty()) {// if the new root is the only one it is also the minimum and the next of itself
			min = node; 
			connect2Nodes(node, node);
		} else { 
			if (min.getKey() > node.getKey()) {//checking if it's supposed to be the new min
				min = node;
			}
			HeapNode last = first.getPrev();
			connect2Nodes(node, first);//we connect it to the relevant nodes
			connect2Nodes(last, node);
		}
		// we put it as the first
		first = node;
	}

	/**
	 * public void deleteMin()
	 *
	 * Deletes the node containing the minimum key.
	 * 
	 * Time complexity:
	 * 
	 * WC O(n)
	 * 
	 * Amort O(log(n))
	 *
	 */
	public void deleteMin() {
		size--; //we delete a node so the size goes down
		if (size == 0 || size == -1) {// if we stated with an empty heap or size one, it's an empty one so we will reset O(1)
			resetHeap();
			return;
		} else {
			if (first == min) {// if the deleted node is the root of the first tree O(1)
				if (min.getChild() != null) {// if it has a child, it is going to be the new first otherwise it's next is going to be the first
					first = min.getChild();
				} else {
					first = min.getNext();
				}
			}
			HeapNode child = min.getChild(); //O(1)
			if (child == null) {// if the min doesn't have children  we will simply disconnect it from the tree O(1)
				HeapNode nextMin = min.getNext();
				HeapNode prevMin = min.getPrev();
				connect2Nodes(prevMin, nextMin);
			} else { // if it has children they are going to be roots so we will set their parent to be null and unmark them all
				HeapNode node = child;
				do {//iterating over all children
					if (node.isMark()) {
						marked--;
						node.setMark(false);//unmarking them
					}
					node.setParent(null);//making them roots
					node = node.getNext();
				} while (node != child);
				if (min.getNext() == min) {
					first = child;
				} else {// setting the first and last children as next and prev of the prev and next of min.
					node = child.getPrev();
					HeapNode nextMin = min.getNext();
					HeapNode prevMin = min.getPrev();
					connect2Nodes(node, nextMin);
					connect2Nodes(prevMin, child);
				}
			}

		}
		this.consolidate();// consolidating amort O(logn), WC O(n)

	}
	
	/**
	 * protected void consolidate()
	 * 
	 * optimizes the amount of trees in the heap by linking them accordingly
	 * 
	 * Time complexity:
	 * 
	 * WC O(n)
	 * 
	 * Amort O(logn)
	 * 
	 */

	protected void consolidate() {
		HeapNode[] ranks = new HeapNode[(int) Math.ceil(Math.log((double) size))*2 + 2];//making array for the possible ranks (it might be much less but it's still O(logn)
		HeapNode node = first;
		first.getPrev().setNext(null); // so we know where to stop
		while (node != null) {//iterating over the trees, O(n), Amort O(logn)
			HeapNode next = node.getNext();//the next node
			int k = node.getRank();
			while (ranks[k] != null) {// go until there is no  tree in our rank
				node = link(node, ranks[k]);// if there is we will link them and empty the spot at the array O(1)
				ranks[k] = null;
				k++;
			}

			ranks[k] = node;// we will put the joint tree in the correct spot
			node = next;
		}
		first = null;// "reseting" the tree
		this.trees = 0;
		for (int i=ranks.length-1;i>=0;i--) {// inserting the trees to the heap from the bigger to the smaller so the smallest will be our first
			HeapNode tree=ranks[i];
			if (tree != null) {
				insertNode(tree);//inserting O(1)
			}
		}
	}
	/**
	 * protected HeapNode link(HeapNode parent, HeapNode child)
	 * 
	 * Receives 2 roots of trees and sets the smaller to be the parent of the other
	 * 
	 * Time complexity:
	 * 
	 * O(1)
	 */
	protected HeapNode link(HeapNode parent, HeapNode child) {
		links++;// we link so we increase the amount of links that have been done
		if (child.getKey() < parent.getKey()) {//swap to the correct parent-child relation
			HeapNode temp = parent;
			parent = child;
			child = temp;
		}
		child.setParent(parent);
		child.setMark(false);// child is no longer a root so we need to make sure that it's mark is false
		if (parent.getChild() == null) {// if the parent has no children we will put the child as the only child
			parent.setChild(child);
			connect2Nodes(child, child);

		} else {// we will put the child as the first one O(1)
			HeapNode thisChild = parent.getChild();
			HeapNode lastChild = thisChild.getPrev();
			connect2Nodes(lastChild, child);
			connect2Nodes(child, thisChild);
			parent.setChild(child);
		}
		parent.setRank(parent.getRank() + 1);// change the rank of the parent to the correct one
		return parent;
	}

	/**
	 * public HeapNode findMin()
	 *
	 * Returns the node of the heap whose key is minimal, or null if the heap is
	 * empty.
	 * 
	 * Time complexity:
	 * 
	 * O(1)
	 */
	public HeapNode findMin() {

		return min;
	}

	/**
	 * public void meld (FibonacciHeap heap2)
	 *
	 * Melds heap2 with the current heap.
	 *
	 * Time complexity:
	 *
	 * O(1)
	 */
	public void meld(FibonacciHeap heap2) {
		if(heap2.isEmpty()) {
			//if the second one is empty we do nothing
			return;
		}
		if(this.isEmpty()) {
			//if the first is empty then it just becomes the second one
			this.first=heap2.first;
			this.marked=heap2.marked;
			this.size=heap2.size;
			this.trees=heap2.trees;
			this.min=heap2.min;
			return;
		}
		//we will set the first of the added heap as the next of the last of the original heap and vice versa
		HeapNode thisLast = this.first.getPrev();
		HeapNode thatLast = heap2.first.getPrev();
		HeapNode thisFirst = this.first;
		HeapNode thatFirst = heap2.first;
		connect2Nodes(thisLast, thatFirst);
		connect2Nodes(thatLast, thisFirst);
		this.size += heap2.size;// add the attributes
		this.trees += heap2.trees;
		this.marked += heap2.marked;
		min = this.min.getKey() < heap2.min.getKey() ? min : heap2.min; // check wich of the minimuns is the new minimum
	}

	/**
	 * public int size()
	 *
	 * Returns the number of elements in the heap.
	 * 
	 * Time complexity:
	 * 
	 * O(1)
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
	 * Time complexity:
	 * 
	 * WC O(n)
	 * 
	 */
	public int[] countersRep() {
		if (this.isEmpty()) {//special case
			return new int[0];
		}
		HeapNode node = this.first;
		int max_rank = 0;

		do {// iterating over the nodes to check what is the highest rank we have (we go over all the trees so it can be O(n))
			if (node.getRank() >= max_rank) {
				max_rank = node.getRank();
			}
			node = node.getNext();
		} while (node != this.first);

		int[] res = new int[max_rank + 1];// make an array with a slot for each rank

		node = this.first;

		do {//iterating over the roots and counting the ranks in the array (we go over all the trees so it can be O(n))
			int rank = node.getRank();
			res[rank]++;
			node=node.getNext();
		} while (node != this.first);

		return res;
	}

	/**
	 * public void delete(HeapNode x)
	 *
	 * Deletes the node x from the heap. It is assumed that x indeed belongs to the
	 * heap.
	 * 
	 * Time complexity:
	 * 
	 * WC O(n)
	 * 
	 * Amort O(logn)
	 *
	 */
	public void delete(HeapNode x) {

		if (x != min) {//if is not the minimum we force it to be the minimum
			decreaseKey(x, x.getKey() - this.min.getKey() + 1);//WC O(n), Amort O(logn)
		}
		//now it's the minimum and we delete it
		this.deleteMin();//WC O(n), Amort O(logn)
	}

	/**
	 * public void decreaseKey(HeapNode x, int delta)
	 *eases the key of the node x by a non-negative value delta. The structure
	 * of the heap should be updated to reflect this change (for example, the
	 * cascading cuts procedure should be applied if needed).
	 * 
	 * Time complexity:
	 * 
	 * WC O(n)
	 * 
	 * Amort O(logn)
	 */
	public void decreaseKey(HeapNode x, int delta) {		
		x.setKey(x.getKey() - delta);//change the key
		if (x.getKey() < this.min.getKey()) {// if its's the new minimum we set it as the minimum
			this.min = x;
		}
		if (x.getParent() == null || x.getParent().getKey() < x.getKey()) {// if we didn't break any rule? GREAT!
			return;
		}
		// if we broke a rule we'll use cascading cuts to cut the node
		cascadingCuts(x, x.getParent());//WC O(n), Amort O(logn)

	}
	
	/**
	 * protected void cascadingCuts(HeapNode child, HeapNode parent) 
	 * 
	 * cuts the current node from the tree and checking if the parent is marked, and if it is continue until the parent is marked or it's a root
	 * 
	 * Time complexity:
	 * 
	 * WC O(n)
	 * 
	 * Amort O(logn)
	 * 
	 */
	protected void cascadingCuts(HeapNode child, HeapNode parent) {
		if (child.isMark()) {//if it is marked it's going to be a root so unmark
			this.marked--;
			child.setMark(false);
		}
		cut(child, parent);// cutting O(1)
		insertNode(child);//insert it as a tree
		if (parent.getParent() != null) {
			if (parent.isMark()) {
				cascadingCuts(parent, parent.getParent());// recursion as we saw in lecture WC O(n)

			} else {
				parent.setMark(true);//if we don't need to continue just mark the parent and finish
				this.marked++;
			}

		}
	}
	
	/**
	 * protected void cut(HeapNode child, HeapNode parent)
	 * 
	 * Time complexity:
	 * 
	 * O(1)
	 * 
	 */
	protected void cut(HeapNode child, HeapNode parent) {
		cuts++;// add to the overall count of cuts
		child.setParent(null);// Setting it's parent to be null
		if (child.getNext() == child) {// if it's the only child, parent is going to have no children
			parent.setChild(null);
		} else {
			if(parent.child==child) {// if it's the first child the paren't direct child changes
				parent.setChild(child.getNext());
			}
			HeapNode next = child.getNext();
			HeapNode prev = child.getPrev();
			connect2Nodes(prev, next);// connect the relevant children
		}
		parent.setRank(parent.getRank() - 1);// decrease the rank
	}

	/**
	 * public int potential()
	 *
	 * This function returns the current potential of the heap, which is: Potential
	 * = #trees + 2*#marked
	 * 
	 * In words: The potential equals to the number of trees in the heap plus twice
	 * the number of marked nodes in the heap.
	 * 
	 * Time complexity:
	 * 
	 * O(1) calculation
	 */
	public int potential() {
		return trees + 2 * marked; //calculation
	}

	/**
	 * public static int totalLinks()
	 *
	 * This static function returns the total number of link operations made during
	 * the run-time of the program. A link operation is the operation which gets as
	 * input two trees of the same rank, and generates a tree of rank bigger by one,
	 * by hanging the tree which has larger value in its root under the other tree.
	 * 
	 * Time complexity:
	 * 
	 * O(1)
	 */
	public static int totalLinks() {
		return links;
	}

	/**
	 * public static int totalCuts()
	 *
	 * This static function returns the total number of cut operations made during
	 * the run-time of the program. A cut operation is the operation which
	 * disconnects a subtree from its parent (during decreaseKey/delete methods).
	 * 
	 * Time complexity:
	 * 
	 * O(1)
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
	 * 
	 * Time complexity:
	 * 
	 * O(k*deg(H)) 
	 * 
	 */
	public static int[] kMin(FibonacciHeap H, int k) {
		if(H.isEmpty()) {// special case
			return new int[0];
		}
		int[] res = new int[k];//initializing the result 
		FibonacciHeap help = new FibonacciHeap(); // creating an auxiliary heap
		help.insert(H.findMin().getKey()).kmin_help = H.findMin();
		//K iterations for O(H) work
		for (int i = 0; i < k; i++) {
			HeapNode node = help.findMin();
			res[i] = node.getKey();// inserting the value of the i th minimum while removing it from the auxiliary heap
			help.deleteMin();//O(log(k))<O(2^deg(H))=O(H)
			HeapNode child = node.kmin_help.getChild();
			if (child != null) {//iterating over it's children < deg(H)
				do {
					help.insert(child.getKey()).kmin_help = child;//inserting a copy of the child while keeping a pointer to the original
					child = child.getNext();
				} while (child != node.kmin_help.child);
			}
		}

		return res;
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
		
		
		/**
		 * public HeapNode(int key)
		 *
		 * Constructs a new HeapNode with the key
		 * 
		 * Time complexity: 
		 * O(1)
		 */
		public HeapNode(int key) {
			this.key = key;
			mark = false;
			rank = 0;
		}
		/**
		 * public int getKey()
		 * 
		 * returns the key of the node
		 * 
		 * Time Complexity:
		 * 
		 * O(1)
		 */
		public int getKey() {
			return this.key;
		}
		/**
		 * public int getRank()
		 * 
		 * returns the rank of the node
		 * 
		 * Time Complexity:
		 * 
		 * O(1)
		 */
		public int getRank() {
			return rank;
		}
		/**
		 * public setRank(int rank)
		 * 
		 * sets the ranks of the Node
		 * 
		 * Time complexity:
		 * 
		 * O(1)
		 */
		public void setRank(int rank) {
			this.rank = rank;
		}
		
		/**
		 * public boolean isMark()
		 * 
		 * returns weather the node is marked or not
		 * 
		 * Time Complexity:
		 * 
		 * O(1)
		 */
		public boolean isMark() {
			return mark;
		}
		
		/**
		 * public void setMark(boolean mark)
		 * 
		 * sets weather the node is marked or not
		 * 
		 * Time complexity:
		 * O(1)
		 */
		public void setMark(boolean mark) {
			this.mark = mark;
		}
		/**
		 * public HeapNode getChild()
		 * 
		 * returns the first child of the node
		 * 
		 * Time complexity:
		 * 
		 * O(1)
		 */
		public HeapNode getChild() {
			return child;
		}
		/**
		 * public void setChild(HeapNode child)
		 * 
		 * sets the child of the node
		 * 
		 * Time complexity:
		 * O(1)
		 */

		public void setChild(HeapNode child) {
			this.child = child;
		}
		/**
		 * public HeapNode getPrev()
		 * 
		 * returns the previous node of the current node
		 * 
		 * Time complexity:
		 * 
		 * O(1)
		 */

		public HeapNode getPrev() {
			return prev;
		}
		/**
		 * public void setPrev(HeapNode prev)
		 * sets the previous node of the current node
		 * 
		 * Time complexity:
		 * O(1)
		 */
		public void setPrev(HeapNode prev) {
			this.prev = prev;
		}
		/**
		 * public HeapNode getParent()
		 * 
		 * returns the parent node of the current node
		 * 
		 * Time complexity:
		 * 
		 * O(1)
		 */
		public HeapNode getParent() {
			return parent;
		}
		
		/**
		 * public void setParent(HeapNode parent)
		 * sets the parent node of the current node
		 * 
		 * Time complexity:
		 * O(1)
		 */
		public void setParent(HeapNode parent) {
			this.parent = parent;
		}
		/**
		 * public HeapNode getNext()
		 * 
		 * returns the next node of the current node
		 * 
		 * Time complexity:
		 * 
		 * O(1)
		 */
		public HeapNode getNext() {
			return next;
		}
		/**
		 * public void setNext(HeapNode next)
		 * sets the next node of the current node
		 * 
		 * Time complexity:
		 * O(1)
		 */

		public void setNext(HeapNode next) {
			this.next = next;
		}
		/**
		 * public void setKey(int key)
		 * sets the key of the current node
		 * 
		 * Time complexity:
		 * O(1)
		 */
		public void setKey(int key) {
			this.key = key;
		}
		/**
		 * public boolean getMarked()
		 * 
		 * returns weather the node is marked or not
		 * 
		 * Time Complexity:
		 * 
		 * O(1)
		 */
		public boolean getMarked() {
			return mark;
		}

	}
}
