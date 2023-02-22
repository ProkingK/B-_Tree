/**
 * A B+ tree generic node
 * Abstract class with common methods and data. Each kind of node implements this class.
 * @param <TKey> the data type of the key
 * @param <TValue> the data type of the value
 */
abstract class BPTreeNode<TKey extends Comparable<TKey>, TValue>
{
	protected int m; //bucketsize
	protected int keyTally; //size
	protected Object[] keys; //key

	protected BPTreeNode<TKey, TValue> parentNode;
	protected BPTreeNode<TKey, TValue> leftSibling;
	protected BPTreeNode<TKey, TValue> rightSibling;

	protected static int level = 0;
	
	protected BPTreeNode() 
	{
		this.keyTally = 0;
		this.parentNode = null;
		this.leftSibling = null;
		this.rightSibling = null;
	}

	public int getKeyCount() 
	{
		return this.keyTally;
	}
	
	@SuppressWarnings("unchecked")
	public TKey getKey(int index) 
	{
		return (TKey)this.keys[index];
	}

	public void setKey(int index, TKey key) 
	{
		this.keys[index] = key;
	}

	public BPTreeNode<TKey, TValue> getParent() 
	{
		return this.parentNode;
	}

	public void setParent(BPTreeNode<TKey, TValue> parent) 
	{
		this.parentNode = parent;
	}	
	
	public abstract boolean isLeaf();
	
	/**
	 * Print all nodes in a subtree rooted with this node
	 */
	@SuppressWarnings("unchecked")
	public void print(BPTreeNode<TKey, TValue> node)
	{
		level++;
		if (node != null)
		{
			System.out.print("Level " + level + " ");
			node.printKeys();
			System.out.println();

			// If this node is not a leaf, then 
			// print all the subtrees rooted with this node.
			if (!node.isLeaf())
			{	
				BPTreeInnerNode<TKey, TValue> inner = (BPTreeInnerNode<TKey, TValue>)node;

				for (int j = 0; j < (node.m); j++)
				{
					this.print((BPTreeNode<TKey, TValue>)inner.references[j]);
				}
			}
		}

		level--;
	}

	/**
	 * Print all the keys in this node
	 */
	protected void printKeys()
	{
		System.out.print("[");

		for (int i = 0; i < this.getKeyCount(); i++)
		{
			System.out.print(" " + this.keys[i]);
		}

 		System.out.print("]");
	}
	
	/**
	 * Search a key on the B+ tree and return its associated value using the index set. If the given key 
	 * is not found, null should be returned.
	 */
	public TValue search(TKey key) 
	{
		BPTreeNode<TKey, TValue> node = find(key);
		
		if (node != null)
		{
			return node.values()[0];
		}
		else
		{
			return  null;
		}
	}

	@SuppressWarnings("unchecked")
	private BPTreeNode<TKey, TValue> find(TKey key)
	{
		BPTreeInnerNode<TKey, TValue> currPtr = (BPTreeInnerNode<TKey, TValue>)this;

		while (currPtr.isLeaf() == false)
		{
			for (int i = 0; i < currPtr.keyTally; i++)
			{
				if (key.compareTo((TKey)currPtr.keys[i]) < 0)
				{
					currPtr = (BPTreeInnerNode<TKey, TValue>)currPtr.references[i];
					break;
				}
				else if (i == currPtr.keyTally - 1)
				{
					currPtr = (BPTreeInnerNode<TKey, TValue>)currPtr.references[i + 1];
					break;
				}
			}
		}

		if (key.compareTo((TKey)currPtr.keys[0]) >= 0 && key.compareTo((TKey)currPtr.keys[currPtr.keyTally - 1]) <= 0);
		{
			for (int i = 0; i < currPtr.keyTally; i++)
			{
				if (currPtr.keys[i] == key)
				{
					return currPtr;
				}
			}
		}

		return null;
	}

	/**
	 * Insert a new key and its associated value into the B+ tree. The root node of the
	 * changed tree should be returned.
	 */
	@SuppressWarnings("unchecked")
	public BPTreeNode<TKey, TValue> insert(TKey key, TValue value) 
	{	
		if (this.isLeaf() == true)
		{			
			BPTreeLeafNode<TKey, TValue> currPtr = (BPTreeLeafNode<TKey, TValue>)this;
			
			if (currPtr.keyTally < m-1)
			{
				int i = 0;

				while (key.compareTo((TKey)currPtr.keys[i]) > 0 && i < currPtr.keyTally)
				{
					i++;
				}

				for (int j = currPtr.keyTally; j > i; j--)
				{
					currPtr.keys[j] = currPtr.keys[j - 1];
					currPtr.values[j] = currPtr.values[j - 1];
				}

				currPtr.keyTally++;
				currPtr.keys[i] = key;
				currPtr.values[i] = value;

				return currPtr;
			}
			else
			{
				Object[] tempKeys = new Object[m];
				Object[] tempValues = new Object[m];
				BPTreeLeafNode<TKey, TValue> newLeafNode = new BPTreeLeafNode<TKey, TValue>(m);

				for (int i = 0; i < m-1; i++)
				{
					tempKeys[i] = currPtr.keys[i];
					tempValues[i] = currPtr.values[i];
				}

				int index = 0;

				while (key.compareTo((TKey)tempKeys[index]) > 0 && index < m-1)
				{
					index++;
				}

				for (int j = m-1; j > index; j--)
				{
					tempKeys[j] = tempKeys[j - 1];
					tempValues[j] = tempValues[j - 1];
				}

				tempKeys[index] = key;
				tempValues[index] = value;
				currPtr.keyTally = m / 2;
				newLeafNode.keyTally = m - (m / 2);

				for (int i = 0; i < currPtr.keyTally; i++)
				{
					currPtr.keys[i] = tempKeys[i];
					currPtr.values[i] = tempValues[i];
				}

				for (int i = 0, j = currPtr.keyTally; i < newLeafNode.keyTally; i++, j++)
				{
					newLeafNode.keys[i] = tempKeys[j];
					newLeafNode.values[i] = tempValues[j];
				}

				BPTreeInnerNode<TKey, TValue> newRoot = new BPTreeInnerNode<TKey, TValue>(m);
				newRoot.keys[0] = newLeafNode.keys[0];
				newRoot.references[0] = currPtr;
				newRoot.references[1] = newLeafNode;
				newRoot.keyTally = 1;

				return newRoot;
			}
		}
		else
		{
			
		}
		
		return null;
	}




	/**
	 * Insert a new key and its associated value into the B+ tree. The root node of the
	 * changed tree should be returned.
	 */
	/*@SuppressWarnings("unchecked")
	public BPTreeNode<TKey, TValue> insert(TKey key, TValue value) 
	{
		BPTreeInnerNode<TKey, TValue> parent = null;
		BPTreeInnerNode<TKey, TValue> currPtr = (BPTreeInnerNode<TKey, TValue>)this;

		if (this.isLeaf() == false)
		{
			while (currPtr.isLeaf() == false)
			{
				parent = currPtr;

				for (int i = 0; i < currPtr.keyTally; i++)
				{
					if (key.compareTo((TKey)currPtr.keys[i]) < 0)
					{
						currPtr = (BPTreeInnerNode<TKey, TValue>)currPtr.references[i];
						break;
					}
					else if(i == currPtr.keyTally - 1)
					{
						currPtr = (BPTreeInnerNode<TKey, TValue>)currPtr.references[i + 1];
						break;
					}
				}
			}
		}
		
		if (currPtr.keyTally < m)
		{
			int i = 0;

			while (key.compareTo((TKey)currPtr.keys[i]) > 0 && i < currPtr.keyTally)
			{
				i++;
			}

			for (int j = currPtr.keyTally; j > i; j--)
			{
                currPtr.keys[j] = currPtr.keys[j - 1];
			}

			currPtr.keys[i] = key;
			currPtr.keyTally++;
			currPtr.references[currPtr.keyTally] = currPtr.references[currPtr.keyTally - 1];
            currPtr.references[currPtr.keyTally - 1] = null;
		}
		else
		{
			Object[] tempNode = new Object[m];
			BPTreeInnerNode<TKey, TValue> newLeafNode = new BPTreeInnerNode<TKey, TValue>(m);

			for (int i = 0; i < m-1; i++)
			{
				tempNode[i] = currPtr.keys[i];
			}

			int index = 0;

			while (key.compareTo((TKey)tempNode[index]) > 0 && index < m-1)
			{
				index++;
			}

			for (int j = m-1; j > index; j--)
			{
                tempNode[j] = tempNode[j - 1];
			}

			tempNode[index] = key;
			currPtr.keyTally = (m - 1) / 2;
			newLeafNode.keyTally = (m) - (m / 2);

			currPtr.references[currPtr.keyTally] = newLeafNode;
            newLeafNode.references[newLeafNode.keyTally] = currPtr.references[m];
 
            currPtr.references[newLeafNode.keyTally] = currPtr.references[m];
			currPtr.references[m-1] = null;

			for (int i = 0; i < currPtr.keyTally; i++)
			{
				currPtr.keys[i] = tempNode[i];
			}

			for (int i = 0, j = currPtr.keyTally; i < newLeafNode.keyTally; i++, j++)
			{
				newLeafNode.keys[i] = tempNode[j];
			}

			if (currPtr == this)
			{
				BPTreeInnerNode<TKey, TValue> newRoot = new BPTreeInnerNode<TKey, TValue>(m);
				newRoot.keys[0] = newLeafNode.keys[0];
				newRoot.references[0] = currPtr;
				newRoot.references[1] = newLeafNode;
				newRoot.keyTally = 1;
				
				return newRoot;
			}
			else
			{
				shiftLevel((TKey)newLeafNode.keys[0], (BPTreeInnerNode<TKey, TValue>)parent, newLeafNode);
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private BPTreeNode<TKey, TValue> shiftLevel(TKey key, BPTreeInnerNode<TKey, TValue> currPtr, BPTreeNode<TKey, TValue> child)
	{
		if (currPtr.keyTally < m)
		{
			int i = 0;

			while (key.compareTo((TKey)currPtr.keys[i]) > 0 && i < currPtr.keyTally)
			{
				i++;
			}

			for (int j = currPtr.keyTally; j < i; j--)
			{
				currPtr.keys[j] = currPtr.keys[j - 1];
			}

			for (int j = currPtr.keyTally + 1; j > i + 1; j--)
			{
				currPtr.references[j] = currPtr.references[j - 1];
			}

			currPtr.keys[i] = key;
        	currPtr.keyTally++;
        	currPtr.references[i + 1] = child;
		} 
		else
		{
			Object[] tempKeys = new Object[m + 1];
			Object[] tempReferences = new Object[m + 2];
			BPTreeInnerNode<TKey, TValue> newInnerNode = new BPTreeInnerNode<TKey, TValue>(m);
	
			for (int i = 0; i < m; i++)
			{
				tempKeys[i] = currPtr.keys[i];
			}
	
			for (int i = 0; i < m + 1; i++)
			{
				tempReferences[i] = currPtr.references[i];
			}
	
			int i = 0;
			int j = 0;

			while (key.compareTo((TKey)tempKeys[i]) > 0 && i < m)
			{
				i++;
			}
	
			for (j = m + 1; j > i; j--)
			{
				tempKeys[j] = tempKeys[j - 1];
			}
	
			tempKeys[i] = key;

			for (j = m + 2; j > i + 1; j--)
			{
				tempReferences[j] = tempReferences[j - 1];
			}
	
			tempReferences[i + 1] = child;
			currPtr.keyTally = (m + 1) / 2;
	
			newInnerNode.keyTally = m - (m + 1) / 2;
	
			for (i = 0, j = currPtr.keyTally + 1; i < newInnerNode.keyTally; i++, j++)
			{
				newInnerNode.keys[i] = tempKeys[j];
			}
	
			for (i = 0, j = currPtr.keyTally + 1; i < newInnerNode.keyTally + 1; i++, j++)
			{
				newInnerNode.references[i] = tempReferences[j];
			}
	
			if (currPtr == this)
			{
				BPTreeInnerNode<TKey, TValue> newRoot = new BPTreeInnerNode<>(m) ;
				newRoot.keys[0] = currPtr.keys[currPtr.keyTally];
				newRoot.references[0] = currPtr;
				newRoot.references[1] = newInnerNode;
				newRoot.keyTally = 1;
				
				return currPtr;
			}
			else
			{
				return shiftLevel((TKey)currPtr.keys[currPtr.keyTally], (BPTreeInnerNode<TKey, TValue>)parentNode, newInnerNode);
			}
		}

		return null;
	}*/




























	/**
	 * Delete a key and its associated value from the B+ tree. The root node of the
	 * changed tree should be returned.
	 */
	public BPTreeNode<TKey, TValue> delete(TKey key) 
	{
		// Your code goes here
		return  null;
	}



	/**
	 * Return all associated key values on the B+ tree in ascending key order using the sequence set. An array
	 * of the key values should be returned.
	 */
	public TValue[] values() 
	{
		// Your code goes here
		return  null;
	}

}