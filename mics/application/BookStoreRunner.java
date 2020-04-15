package bgu.spl.mics.application;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;

import bgu.spl.mics.application.inputGson;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;
import bgu.spl.mics.application.services.APIService;
import bgu.spl.mics.application.services.InventoryService;
import bgu.spl.mics.application.services.LogisticsService;
import bgu.spl.mics.application.services.ResourceService;
import bgu.spl.mics.application.services.SellingService;
import bgu.spl.mics.application.services.TimeService;


/** This is the Main class of the application. You should parse the input file, 
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner {
    public static void main(String[] args) {
    	if(args.length >= 5) {
	    	String jsonName = args[0];
	    	String customersHashMapFileName = args[1];
	    	String booksHashMapFileName = args[2];
	    	String orderReceiptsFileName = args[3];
	    	String moneyRegisterFileName = args[4];
	    	
	    	//Read from Json file
	    	Gson gson = new Gson();
	    	inputGson input = null;
	    	try {
				BufferedReader buff = new BufferedReader(new FileReader(jsonName));
				input = gson.fromJson(buff,inputGson.class);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	    	
	    	//Initialized the passive objects
	    	AtomicInteger counterOfInitialization = new AtomicInteger(0);
	    	AtomicInteger index = new AtomicInteger(0);
	    	ResourcesHolder resourcesHolder = ResourcesHolder.getInstance();
	    	Inventory inventory = Inventory.getInstance();
	    	resourcesHolder.load(input.initialResources[0].vehicles);
	    	inventory.load(input.initialInventory);
	    	MoneyRegister moneyRegister = MoneyRegister.getInstance();
	    	
	    	//Apply Thread per service pattern and start all the Threads
	    	//After all the Threads are started, join them to main
	    	Thread[] threadsArray = new Thread[input.services.getSum()];
	    	for(int i = 0; i < input.services.getCustomersArrayLength();i++) {
	    		threadsArray[index.get()] = new Thread(new APIService(input.services.customers[i], counterOfInitialization));
	    		threadsArray[index.get()].start();
	    		index.incrementAndGet();
	    	}
	    	for(int i = 0; i < input.services.selling; i++) {
	    		threadsArray[index.get()] = new Thread(new SellingService(counterOfInitialization));
	    		threadsArray[index.get()].start();
	    		index.incrementAndGet();
	    	}
	    	for(int i = 0; i < input.services.inventoryService; i++) {
	    		threadsArray[index.get()] = new Thread(new InventoryService(counterOfInitialization));
	    		threadsArray[index.get()].start();
	    		index.incrementAndGet();
	    	}
	    	for(int i = 0; i < input.services.logistics; i++) {
	    		threadsArray[index.get()] = new Thread(new LogisticsService(counterOfInitialization));
	    		threadsArray[index.get()].start();
	    		index.incrementAndGet();
	    	}
	    	for(int i = 0; i < input.services.resourcesService; i++) {
	    		threadsArray[index.get()] = new Thread(new ResourceService(counterOfInitialization));
	    		threadsArray[index.get()].start();
	    		index.incrementAndGet();
	    	}
	    	while(counterOfInitialization.get() < threadsArray.length-1) {
	    		}
	    	threadsArray[index.get()] = new Thread(new TimeService(input.services.time.getSpeed(), input.services.time.getDuration()));
	    	threadsArray[index.get()].start();
	    	
	    	
	    	for(int i = 0; i < threadsArray.length; i++)
				try {
					threadsArray[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	
	    	//Apply all the printing
	    	moneyRegister.printOrderReceipts(orderReceiptsFileName);
	    	inventory.printInventoryToFile(booksHashMapFileName);
	    	
	    	HashMap<Integer, Customer> customerMap = new HashMap<>();
	    	Customer[] customersArray = input.services.customers;
	    	for(int i =0; i < customersArray.length;i++) {
	    		customerMap.put(customersArray[i].getId(), customersArray[i]);
	    	}
	    	
	    	printToFile(customersHashMapFileName,customerMap);
	    	printToFile(moneyRegisterFileName,MoneyRegister.getInstance());
    	}
    	else
    		throw new IllegalArgumentException("you should enter 5 arguments");
    	
    }
    
    private static void printToFile(String filename1,Object o) {
    	try {
		    FileOutputStream file1 = new FileOutputStream(filename1);
		    ObjectOutputStream writer1 = new ObjectOutputStream(file1);
		    writer1.writeObject(o);
		    writer1.close();
		    file1.close();
		  
		} catch (Exception ex) {
		    System.err.println("failed to write " + filename1  + ", "+ ex);
		}
    }
    
    
    
   }
    
    

    

