package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

public class ReleaseVehicle implements Event<Boolean> {
	private DeliveryVehicle v;

	public ReleaseVehicle(DeliveryVehicle v) {
		this.v = v;
	}
	
	public DeliveryVehicle getVehicle() {
		return v;
	}

}
