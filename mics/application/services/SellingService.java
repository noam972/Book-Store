package bgu.spl.mics.application.services;

import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BookAvailabilityEvent;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.messages.OrderBookEvent;
import bgu.spl.mics.application.messages.TakeBookEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TimeToTerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;
import bgu.spl.mics.application.passiveObjects.OrderResult;

/**
 * Selling service in charge of taking orders from customers.
 * Holds a reference to the {@link MoneyRegister} singleton of the store.
 * Handles {@link OrderBookEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class SellingService extends MicroService{

	private static AtomicInteger SellingServiceNum = new AtomicInteger(0);
	private AtomicInteger currentTick;
	private MoneyRegister money;
	private AtomicInteger counterOfInitialization;
	
	public SellingService(AtomicInteger counterOfInitialization) {
		super("Selling Service " + SellingServiceNum.getAndIncrement());
		currentTick = new AtomicInteger(0);
		money = MoneyRegister.getInstance();
		this.counterOfInitialization = counterOfInitialization;
	}

	//Initialize to all the relevant Messages
	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, b -> {
			currentTick.set(b.getTick());
		});
		subscribeBroadcast(TimeToTerminateBroadcast.class, b -> {
			this.terminate();
		});
		//First try to buy the book, success if the customer have enough money and the book available in inventory
		//If the buying succeed, charge the customer creditCard and give him receipt
		//Second try to deliver the book to the customer by sending DeliveryEvent
		subscribeEvent(OrderBookEvent.class, ev ->{
			Customer c = ev.getCustomer();
			int orderTick = ev.getTick();
			int issuedTick = currentTick.get();
			Future<Integer> f1 = sendEvent(new BookAvailabilityEvent(ev.getBookTitle()));
			if(f1 !=null) {
				Integer isBookAvalible = f1.get();
				if(isBookAvalible !=null && isBookAvalible > -1) {
					synchronized(c){
						int creditCartAmount = c.getAvailableCreditAmount();
						if(creditCartAmount >= isBookAvalible){
							Future<OrderResult> f2 = sendEvent(new TakeBookEvent(ev.getBookTitle()));
							if(f2 != null) {
								OrderResult retult = f2.get();
								if(retult != null && retult.equals(OrderResult.SUCCESSFULLY_TAKEN)) {
									c.setAvailableCreditAmount(creditCartAmount-isBookAvalible);
									sendEvent(new DeliveryEvent(c.getAddress(), c.getDistance()));
									OrderReceipt r = new OrderReceipt(0, getName(), c.getId(), ev.getBookTitle(), isBookAvalible, issuedTick, orderTick, currentTick.get());
									c.addRecipt(r);
									money.file(r);
									complete(ev, r);
								}
							}
							else complete(ev,null);
						}
						else complete(ev,null);
					}
				}
			}
		});
		counterOfInitialization.incrementAndGet();
	}

}
