package bgu.spl.mics.application.passiveObjects;


import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive data-object representing the store inventory.
 * It holds a collection of {@link BookInventoryInfo} for all the
 * books in the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Inventory implements Serializable{
	
	private static class InventoryHolder{  
		private static Inventory inventory = new Inventory();
	}
	
	private ConcurrentHashMap<String, BookInventoryInfo> books;
	
	/**
     * Retrieves the single instance of this class.
     */
	public static Inventory getInstance() {
		return InventoryHolder.inventory;
	}
	
	private Inventory(){
		books = new ConcurrentHashMap<>();
	}
	
	/**
     * Initializes the store inventory. This method adds all the items given to the store
     * inventory.
     * <p>
     * @param inventory 	Data structure containing all data necessary for initialization
     * 						of the inventory.
     */
	//Load inventory, if book dosen't exist add new one and update amountInIventory
	//if exist update amountInIventory
	public void load (BookInventoryInfo[ ] inventory ) {
		for(BookInventoryInfo i : inventory){
			if(books.get(i.getBookTitle())==null)
				books.put(i.getBookTitle(), i);
			else
				books.get(i.getBookTitle()).setAmountInInventory(i.getAmountInInventory());
		}	
	}
	
	/**
     * Attempts to take one book from the store.
     * <p>
     * @param book 		Name of the book to take from the store
     * @return 	an {@link Enum} with options NOT_IN_STOCK and SUCCESSFULLY_TAKEN.
     * 			The first should not change the state of the inventory while the 
     * 			second should reduce by one the number of books of the desired type.
     */
	//if the book exist in inventory and the it's amount bigger then 0, take the book.
	//otherwise return NOT IN STOCK
	public OrderResult take (String book) {
		BookInventoryInfo temp = books.get(book);
		synchronized (temp) {
			if(temp==null || temp.getAmountInInventory()==0)
				return OrderResult.NOT_IN_STOCK;
			else{
				temp.amoundReduceByOne();;
				return OrderResult.SUCCESSFULLY_TAKEN;
			}
		}
		
	}
	
	
	
	/**
     * Checks if a certain book is available in the inventory.
     * <p>
     * @param book 		Name of the book.
     * @return the price of the book if it is available, -1 otherwise.
     */
	//if the book dosen't exist in inventory or it's amount is 0 return -1
	//otherwise return it's price
	public int checkAvailabiltyAndGetPrice(String book) {
		BookInventoryInfo temp = books.get(book);
		if(temp==null || temp.getAmountInInventory()==0)
				return -1;
		else
			return temp.getPrice();
	}
	
	/**
     * 
     * <p>
     * Prints to a file name @filename a serialized object HashMap<String,Integer> which is a Map of all the books in the inventory. The keys of the Map (type {@link String})
     * should be the titles of the books while the values (type {@link Integer}) should be
     * their respective available amount in the inventory. 
     * This method is called by the main method in order to generate the output.
     */
	
	public void printInventoryToFile(String filename){
		HashMap<String, Integer> booksToPrint = new HashMap<>();
		for (String key : books.keySet()) {
			booksToPrint.put(key, books.get(key).getAmountInInventory());
		}
		try {
		    FileOutputStream file = new FileOutputStream(filename);
		    ObjectOutputStream writer = new ObjectOutputStream(file);
		    writer.writeObject(booksToPrint);
		    writer.close();
		    file.close();
		} catch (Exception ex) {
		    System.err.println("failed to write " + filename + ", "+ ex);
		}
		
	}
	
	
}
