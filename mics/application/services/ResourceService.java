package bgu.spl.mics.application.services;


import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CarAvaliabilityEvent;
import bgu.spl.mics.application.messages.ReleaseVehicle;
import bgu.spl.mics.application.messages.TimeToTerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService{
	
	private static AtomicInteger ResourceServiceNum = new AtomicInteger(0);
	private ResourcesHolder resourceHolder;
	private AtomicInteger counterOfInitialization;
	
	public ResourceService(AtomicInteger counterOfInitialization) {
		super("ResourceService " +ResourceServiceNum.getAndIncrement());
		this.counterOfInitialization = counterOfInitialization;
		resourceHolder = ResourcesHolder.getInstance();
	}

	//Initialize to all the relevant Messages
	@Override
	protected void initialize() {
		subscribeBroadcast(TimeToTerminateBroadcast.class, b -> {
			resourceHolder.releaseVehicle(null);
			this.terminate();
			
		});
		subscribeEvent(CarAvaliabilityEvent.class, ev->{
			Future<DeliveryVehicle> f1 =resourceHolder.acquireVehicle();
			complete(ev, f1);
		});
		subscribeEvent(ReleaseVehicle.class, ev ->{
			resourceHolder.releaseVehicle(ev.getVehicle());
		});
		counterOfInitialization.incrementAndGet();
	}

}
