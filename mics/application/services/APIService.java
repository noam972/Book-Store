package bgu.spl.mics.application.services;


import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.OrderBookEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TimeToTerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.OrderSchedule;

/**
 * APIService is in charge of the connection between a client and the store.
 * It informs the store about desired purchases using {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class APIService extends MicroService{
	
	private static AtomicInteger APIServiceNum = new AtomicInteger(0);
	private Customer customer;
	private OrderSchedule[] orderSchedule;
	private int currentTick;
	private AtomicInteger bookToBuy;
	private AtomicInteger counterOfInitialization;

	public APIService(Customer customer,AtomicInteger counterOfInitialization) {
		super("API Service " + APIServiceNum.getAndIncrement());
		this.customer = customer;
		orderSchedule = customer.getOrderSchedule();
		sort();
		bookToBuy = new AtomicInteger(0);
		this.counterOfInitialization = counterOfInitialization;
	}

	//Initialize to all the relevant Messages
	@Override
	protected void initialize() {
		//Update the currentTick and check if it need to order book. If do, send orderBook event
		subscribeBroadcast(TickBroadcast.class, b -> {
			currentTick = b.getTick();
			while(bookToBuy.get() < orderSchedule.length && currentTick==orderSchedule[bookToBuy.get()].tick) {
					String bookTitle = orderSchedule[bookToBuy.get()].bookTitle;
					sendEvent(new OrderBookEvent(customer,bookTitle,currentTick));
					bookToBuy.incrementAndGet();
			}
		});
		subscribeBroadcast(TimeToTerminateBroadcast.class, b -> {
			this.terminate();
		});
		counterOfInitialization.getAndIncrement();
	}
	
	//Sort OrderSchedule array
	private void sort() {
		int i, j; 
		OrderSchedule key;
		int n = orderSchedule.length;
		   for (i = 1; i < n; i++) { 
		       key = orderSchedule[i]; 
		       j = i-1; 

		       while (j >= 0 && orderSchedule[j].tick > key.tick) 
		       { 
		    	   orderSchedule[j+1] = orderSchedule[j]; 
		           j = j-1; 
		       } 
		       orderSchedule[j+1] = key; 
		   } 
	}

}
