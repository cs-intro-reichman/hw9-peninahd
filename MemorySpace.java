/**
 * Represents a managed memory space. The memory space manages a list of allocated 
 * memory blocks, and a list free memory blocks. The methods "malloc" and "free" are 
 * used, respectively, for creating new blocks and recycling existing blocks.
 */
public class MemorySpace {
	
	// A list of the memory blocks that are presently allocated
	private LinkedList allocatedList;

	// A list of memory blocks that are presently free
	private LinkedList freeList;

	/**
	 * Constructs a new managed memory space of a given maximal size.
	 * 
	 * @param maxSize
	 *            the size of the memory space to be managed
	 */
	public MemorySpace(int maxSize) {
		// initiallizes an empty list of allocated blocks.
		allocatedList = new LinkedList();
	    // Initializes a free list containing a single block which represents
	    // the entire memory. The base address of this single initial block is
	    // zero, and its length is the given memory size.
		freeList = new LinkedList();
		freeList.addLast(new MemoryBlock(0, maxSize));
	}

	/**
	 * Allocates a memory block of a requested length (in words). Returns the
	 * base address of the allocated block, or -1 if unable to allocate.
	 * 
	 * This implementation scans the freeList, looking for the first free memory block 
	 * whose length equals at least the given length. If such a block is found, the method 
	 * performs the following operations:
	 * 
	 * (1) A new memory block is constructed. The base address of the new block is set to
	 * the base address of the found free block. The length of the new block is set to the value 
	 * of the method's length parameter.
	 * 
	 * (2) The new memory block is appended to the end of the allocatedList.
	 * 
	 * (3) The base address and the length of the found free block are updated, to reflect the allocation.
	 * For example, suppose that the requested block length is 17, and suppose that the base
	 * address and length of the the found free block are 250 and 20, respectively.
	 * In such a case, the base address and length of of the allocated block
	 * are set to 250 and 17, respectively, and the base address and length
	 * of the found free block are set to 267 and 3, respectively.
	 * 
	 * (4) The new memory block is returned.
	 * 
	 * If the length of the found block is exactly the same as the requested length, 
	 * then the found block is removed from the freeList and appended to the allocatedList.
	 * 
	 * @param length
	 *        the length (in words) of the memory block that has to be allocated
	 * @return the base address of the allocated block, or -1 if unable to allocate
	 */
	public int malloc(int length) {		
		if (freeList == null || freeList.getSize() == 0 || length < 0) {
			return -1; // No blocks available for allocation		
		}

		boolean found = false;
		int acquiredLength = -1;
		int acquiredAddress = -1;

		//first we are going to iterate over the list and find the memory block
		//with the closest difference to our length
		ListIterator iterator = freeList.iterator();
		int foundIndex = -1;
		
		while(iterator.hasNext()) {
			MemoryBlock currentBlock = iterator.next();
			
			if (currentBlock.length >= length) {
				//foundBlock = currentBlock;
				acquiredLength = currentBlock.length;
				acquiredAddress = currentBlock.baseAddress;
				foundIndex = freeList.indexOf(currentBlock)
;				found = true;
				break;
			}
		}

		//if we found one, create a new block and allocate it to the allocated list
		if (found){
		MemoryBlock allocatedBlock = new MemoryBlock(acquiredAddress, length);
		allocatedList.addLast(allocatedBlock);
		} else {
			return -1;
		}

		//remove the block if it was exactly the same length or split the block
		if (acquiredLength == length) {
			freeList.remove(freeList.getBlock(foundIndex));
		} else {
			freeList.getBlock(foundIndex).baseAddress += length;
			freeList.getBlock(foundIndex).length -= length;
			//System.out.println("Free List State: " + freeList);
			//System.out.println("Allocated List State: " + allocatedList);

		}

		return acquiredAddress;
	}

	/**
	 * Frees the memory block whose base address equals the given address.
	 * This implementation deletes the block whose base address equals the given 
	 * address from the allocatedList, and adds it at the end of the free list. 
	 * 
	 * @param baseAddress
	 *            the starting address of the block to freeList
	 */
	public void free(int address) {
		if(allocatedList.getSize() == 0) {
			throw new IllegalArgumentException("index must be between 0 and size");
		}
		
		ListIterator iterator = allocatedList.iterator();
		MemoryBlock freeBlock = null;
		
		//finds the block in the allocated list
		while(iterator.hasNext()) {
			MemoryBlock currentBlock = iterator.next();
			if (currentBlock.baseAddress == address) {
			freeBlock = currentBlock;
			freeList.addLast(freeBlock);
			break;
			}
		}

		if (freeBlock != null) {
			allocatedList.remove(freeBlock);
		}
	
	}
	
	/**
	 * A textual representation of the free list and the allocated list of this memory space, 
	 * for debugging purposes.
	 */
	public String toString() {
		return freeList.toString() + "\n" + allocatedList.toString();		
	}
	
	/**
	 * Performs defragmantation of this memory space.
	 * Normally, called by malloc, when it fails to find a memory block of the requested size.
	 * In this implementation Malloc does not call defrag.
	 */
	public void defrag() {
		//when the list is empty or of size 1 throw an error
		if (freeList.getSize() == 0 || freeList.getSize() == 1){
			return;
		}
		
		boolean merged; //tracking if we merged an element each time

		do {
		merged = false; //reset to false

		for (int i = 0; i < freeList.getSize(); i++){
			for (int j = 0; j < freeList.getSize(); j++) {
				//skip the current element we are comparing
				if (j == i) {
					continue;
				}
				//get the sum of the base address and length of the element in the freeList
				int possibleAddress = freeList.getBlock(i).baseAddress + freeList.getBlock(i).length;
				
				//checks each element to see if they can be merged with the current element
				if (freeList.getBlock(j).baseAddress == possibleAddress) {
					freeList.getBlock(i).length += freeList.getBlock(j).length;
					freeList.remove(j);
					j--;
					merged = true;
					break;
				}
			}
			if (merged) {
				break;
			}
		}

		} while (merged);
	}
}
