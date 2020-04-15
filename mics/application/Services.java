package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.Customer;

public class Services {

	public Time time;
	public int selling;
	public int inventoryService;
	public int logistics;
	public int resourcesService;
	public Customer[] customers;
	
	public int getSum() {
		return selling+inventoryService+logistics+resourcesService+customers.length+1;
		 
	}
	
	public int getCustomersArrayLength() {
		return customers.length;
	}
}
