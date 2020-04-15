package bgu.spl.mics.application.passiveObjects;
import java.util.concurrent.LinkedBlockingQueue;
import bgu.spl.mics.Future;

/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class ResourcesHolder {
	
	private static class ResourcesHolderHold{
		private static ResourcesHolder resourcesHolder = new ResourcesHolder();
	}
	
	private LinkedBlockingQueue<DeliveryVehicle> availableVehicles;
	private LinkedBlockingQueue<DeliveryVehicle> unAvalibleVehicles;
	private LinkedBlockingQueue<Future<DeliveryVehicle>> unResolveFutures;
	Boolean terminate;
	/**
     * Retrieves the single instance of this class.
     */
	public static ResourcesHolder getInstance() { 
		  return ResourcesHolderHold.resourcesHolder; 
	}
	
	private ResourcesHolder() {
		availableVehicles = new LinkedBlockingQueue<>();
		unAvalibleVehicles = new LinkedBlockingQueue<>();
		unResolveFutures = new LinkedBlockingQueue<>();
		terminate = false;
	}
	
	/**
     * Tries to acquire a vehicle and gives a future object which will
     * resolve to a vehicle.
     * <p>
     * @return 	{@link Future<DeliveryVehicle>} object which will resolve to a 
     * 			{@link DeliveryVehicle} when completed.   
     */
	//acquire vehicle if there is one available, remove it from the available vehicle map and connect it with the future
	public Future<DeliveryVehicle> acquireVehicle() {
		Future<DeliveryVehicle> f = new Future<>();
		synchronized (terminate) {
			if(terminate)
				return null;
			unResolveFutures.add(f);
		}
		resolveFutures();
		return f;

	}
	
	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	//call from the vehicle itself after finishing the delivery, return the vehicle to the available vehicle map
	public void releaseVehicle(DeliveryVehicle vehicle) {
		if(vehicle == null) {
			synchronized (terminate) {
				for (Future<DeliveryVehicle> f : unResolveFutures) {
					f.resolve(null);
				}
				terminate = true;
			}
		}
		else {
			makeAvailable(vehicle);
			resolveFutures();
		}
	}
	
	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles) {
		for(DeliveryVehicle i : vehicles) {
				availableVehicles.add(i);
		}
	}
	
	private void makeAvailable (DeliveryVehicle vehicle) {
		synchronized (terminate) {
			unAvalibleVehicles.remove(vehicle);
			availableVehicles.add(vehicle);
		}
		
	}
	
	private void resolveFutures() {
		while(!unResolveFutures.isEmpty() && !availableVehicles.isEmpty()) {
				DeliveryVehicle temp = availableVehicles.poll();
				if (temp != null) {
					synchronized (temp) {
						unAvalibleVehicles.add(temp);
						unResolveFutures.poll().resolve(temp);
					}
				} 
			
					
		}
	}
	
}

