package bgu.spl.mics.application.passiveObjects;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


/**
 * Passive data-object representing a customer of the store.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Customer implements Serializable{
	private int id;
	private String name;
	private String address;
	private int distance;
	private CreditCard creditCard;
	private OrderSchedule[] orderSchedule;
	private List<OrderReceipt> customerReceiptList = new LinkedList<>();


	/**
     * Retrieves the name of the customer.
     */
	public String getName() {
		return name;
	}

	/**
     * Retrieves the ID of the customer  . 
     */
	public int getId() {
		return id;
	}
	
	/**
     * Retrieves the address of the customer.  
     */
	public String getAddress() {
		return address;
	}
	
	/**
     * Retrieves the distance of the customer from the store.  
     */
	public int getDistance() {
		return distance;
	}

	
	/**
     * Retrieves a list of receipts for the purchases this customer has made.
     * <p>
     * @return A list of receipts.
     */
	public List<OrderReceipt> getCustomerReceiptList() {
			return customerReceiptList;
	}
	
	/**
     * Retrieves the amount of money left on this customers credit card.
     * <p>
     * @return Amount of money left.   
     */
	public int getAvailableCreditAmount() {
		synchronized (creditCard) {
			return creditCard.amount;
		}
	}
	
	/**
     * Retrieves this customers credit card serial number.    
     */
	public int getCreditNumber() {
		return creditCard.number;
	}
	
	public void setAvailableCreditAmount(int amount) {
		synchronized (creditCard) {
			creditCard.amount = amount;
		}
		
	}
	
	public OrderSchedule[] getOrderSchedule() {
		return orderSchedule;
	}
	
	public void addRecipt(OrderReceipt r) {
		synchronized (customerReceiptList) {
			customerReceiptList.add(r);
			
		}
	}
}
