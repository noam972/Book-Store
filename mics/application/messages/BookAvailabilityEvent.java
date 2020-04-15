package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class BookAvailabilityEvent implements Event<Integer> {
	
	private String bookTitle;
	
	public BookAvailabilityEvent(String bookTitle) {
		this.bookTitle = bookTitle;
	}

	public String getBookTitle() {
		return bookTitle;
	}
	
	
}
