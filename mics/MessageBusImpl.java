package bgu.spl.mics;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Map.Entry;



/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {


	private ConcurrentHashMap<MicroService,LinkedBlockingQueue<Message>> mapOfToDoLists;
	private ConcurrentHashMap<Class<? extends Message>, LinkedBlockingQueue<MicroService>> mapOfMessageToMicroserviceQueue;
	private ConcurrentHashMap<Event<?>, Future<?>> mapOfEventToFuture;
	
	// Singleton Implementation 
	private static class MessageBusHolder{
		private static MessageBusImpl bus = new MessageBusImpl();
	}
	
	public static MessageBusImpl getInstance() {
		  return MessageBusHolder.bus; 
	}
	
	//private constructor
	private MessageBusImpl() {
		mapOfToDoLists = new ConcurrentHashMap<>();
		mapOfMessageToMicroserviceQueue = new ConcurrentHashMap<>();
		mapOfEventToFuture = new ConcurrentHashMap<>();
	}
		
	//@Pre checks if the type exist in mapOfMessageToMicroserviceQueue, if not put new LinkedBlockingQueue
	//@Post LinkedBlockingQueue.size == @Pre LinkedBlockingQueue.size +1
	//add the MicroService to the event queue
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
			mapOfMessageToMicroserviceQueue.putIfAbsent(type, new LinkedBlockingQueue<MicroService>());
			mapOfMessageToMicroserviceQueue.get(type).add(m);
	}

	//@Pre checks if the type exist in mapOfMessageToMicroserviceQueue, if not put new LinkedBlockingQueue
	//@Post LinkedBlockingQueue.size == @Pre LinkedBlockingQueue.size +1
	//add the MicroService to the event queue
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		mapOfMessageToMicroserviceQueue.putIfAbsent(type, new LinkedBlockingQueue<MicroService>());
		mapOfMessageToMicroserviceQueue.get(type).add(m);
	}

	//calls Future resolve method with the result that return from the MicroService
	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> temp = (Future<T>) mapOfEventToFuture.get(e);
		if(temp != null)
			temp.resolve(result);
	}

	
	//get MicroService from this broadcast queue and add this broadcast to his to do list.
	@Override
	public void sendBroadcast(Broadcast b) {
		if(mapOfMessageToMicroserviceQueue.get(b.getClass())!=null) {
			LinkedBlockingQueue<MicroService> temp = mapOfMessageToMicroserviceQueue.get(b.getClass());
			for(MicroService m : temp) 
				mapOfToDoLists.get(m).add(b);
			synchronized (this) {
				notifyAll();
			}	
		}
	}

	//get MicroService from this event queue and add this event to his to do list in round robin pattern.
	@Override
	public  <T> Future<T> sendEvent(Event<T> e) {
		if(mapOfMessageToMicroserviceQueue.get(e.getClass())!=null) {
			Future<T> future = new Future<>();
			mapOfEventToFuture.put(e, future);
			LinkedBlockingQueue<MicroService> temp = mapOfMessageToMicroserviceQueue.get(e.getClass());
			synchronized (temp) {
				if (!temp.isEmpty()) {
					MicroService micro = temp.poll();
					temp.add(micro);
					mapOfToDoLists.get(micro).add(e);
				}
				else 
					return null;
			}
			synchronized (this) {
				notifyAll();
			}
			return future;
		}
		else
			return null;
	}

	//@Post mapOfToDoLists.size == @Pre mapOfToDoLists.size+1
	//if this MicroService isn't register already, create new LinkedBlockingQueue in mapOfToDoLists with the MicroService as key
	@Override
	public void register(MicroService m) {
		mapOfToDoLists.putIfAbsent(m, new LinkedBlockingQueue<>());
	}

	@Override
	public void unregister(MicroService m) {
		for(Entry<Class<? extends Message>, LinkedBlockingQueue<MicroService>> i : mapOfMessageToMicroserviceQueue.entrySet()){
			synchronized (i.getValue()) {
					i.getValue().remove(m);
			}
		}
		LinkedBlockingQueue<Message> temp = mapOfToDoLists.get(m);
		for(Message i : temp) {
			if(mapOfEventToFuture.get(i)!=null)
			mapOfEventToFuture.get(i).resolve(null);
		}
		mapOfToDoLists.remove(m);
	}

	
	@Override
	public  Message awaitMessage(MicroService m) throws InterruptedException,IllegalStateException {
		LinkedBlockingQueue<Message> mToDoList = mapOfToDoLists.get(m);
		synchronized (this) {
			while(mToDoList.isEmpty()){
				wait();	
			}
			Message tempMessage = mToDoList.poll();
			return tempMessage;
		}
	}

	

}
