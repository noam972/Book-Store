package bgu.spl.mics.application.services;

import java.util.concurrent.TimeUnit;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TimeToTerminateBroadcast;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link Tick Broadcast}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{
	
	private int speed;
	private int duration;
	private int tick;

	public TimeService(int speed,int duration) {
		super("Time Service");
		this.speed = speed;
		this.duration = duration;
		tick = 1;
	}

	@Override
	protected void initialize() {
		sendBroadcast(new TickBroadcast(tick));
		TimeUnit unit = TimeUnit.MILLISECONDS;
		while(duration>1) {
			try {
				unit.sleep(speed);
				tick++; duration--;
				sendBroadcast(new TickBroadcast(tick));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		sendBroadcast(new TimeToTerminateBroadcast());
		this.terminate();
	}

}
