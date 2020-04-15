package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;

public class OrderBookEvent implements Event<OrderReceipt> {
	
	private Customer customer;
	private String bookTitle;
	private int Tick;


	public OrderBookEvent(Customer customer,String bookTitle,int Tick) {
		this.customer = customer;
		this.bookTitle = bookTitle;
		this.Tick = Tick;
	}

	public Customer getCustomer() {
		return customer;
	}

	public String getBookTitle() {
		return bookTitle;
	}
	
	public int getTick() {
		return Tick;
	}
	
	
}
