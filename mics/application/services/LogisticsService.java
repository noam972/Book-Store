package bgu.spl.mics.application.services;
import java.util.concurrent.atomic.AtomicInteger;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.messages.ReleaseVehicle;
import bgu.spl.mics.application.messages.TimeToTerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.messages.CarAvaliabilityEvent;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LogisticsService extends MicroService {

	private static AtomicInteger LogisticsServiceNum = new AtomicInteger(0);
	private AtomicInteger counterOfInitialization;

	
	public LogisticsService(AtomicInteger counterOfInitialization) {
		super("LogisticsService "+LogisticsServiceNum.incrementAndGet() );
		this.counterOfInitialization = counterOfInitialization;

		
	}

	//Initialize to all the relevant Messages
	@Override
	protected void initialize() {
		subscribeBroadcast(TimeToTerminateBroadcast.class, b -> {
			this.terminate();
		});
		subscribeEvent(DeliveryEvent.class, ev->{
			int distance = ev.getDistance();
			String address = ev.getAddress();
			Future<Future<DeliveryVehicle>> f1 = sendEvent(new CarAvaliabilityEvent());
			if(f1 !=null) {
				Future<DeliveryVehicle> f2 = f1.get();
				if(f2 != null) {
					DeliveryVehicle v = f2.get();
					if(v !=null) {
						v.deliver(address, distance);
						sendEvent(new ReleaseVehicle(v));
						complete(ev,true);
					}
					else
						complete(ev,null);
				}
				else 
					complete(ev, null);
			}
			else 
				complete(ev,null);	
		});
		counterOfInitialization.getAndIncrement();
	}

}
