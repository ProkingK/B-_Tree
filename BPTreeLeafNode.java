/**
 * A B+ tree leaf node
 * @param <TKey> the data type of the key
 * @param <TValue> the data type of the value
 */
class BPTreeLeafNode<TKey extends Comparable<TKey>, TValue> extends BPTreeNode<TKey, TValue>
{
	protected Object[] values;
	
	public BPTreeLeafNode(int order)
	{
		this.m = order;
		this.keys = new Object[m];
		this.values = new Object[m];
	}

	@SuppressWarnings("unchecked")
	public TValue getValue(int index)
	{
		return (TValue)this.values[index];
	}

	public void setValue(int index, TValue value)
	{
		this.values[index] = value;
	}
	
	@Override
	public boolean isLeaf()
	{
		return true;
	}
}