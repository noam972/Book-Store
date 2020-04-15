package bgu.spl.mics.application.services;

import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BookAvailabilityEvent;
import bgu.spl.mics.application.messages.TakeBookEvent;
import bgu.spl.mics.application.messages.TimeToTerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.OrderResult;

/**
 * InventoryService is in charge of the book inventory and stock.
 * Holds a reference to the {@link Inventory} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class InventoryService extends MicroService{
	private Inventory inventory;
	private static AtomicInteger i = new AtomicInteger(0);
	private AtomicInteger counterOfInitialization;
	
	public InventoryService(AtomicInteger counterOfInitialization) {
		super("InventoryService"+i.getAndIncrement());
		inventory = Inventory.getInstance();
		this.counterOfInitialization = counterOfInitialization;
	}
	
	//Initialize to all the relevant Messages
	@Override
	protected void initialize() {
		subscribeBroadcast(TimeToTerminateBroadcast.class, b -> {
			this.terminate();
		});
		subscribeEvent(BookAvailabilityEvent.class, ev ->{
			int result = inventory.checkAvailabiltyAndGetPrice(ev.getBookTitle());
			complete(ev,result);
		});
		subscribeEvent(TakeBookEvent.class, ev ->{
			OrderResult result = inventory.take(ev.getBookTitle());
			complete(ev, result);
		});
		counterOfInitialization.getAndIncrement();
	}

}
