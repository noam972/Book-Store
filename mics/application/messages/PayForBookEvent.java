package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;

public class PayForBookEvent implements Event<Boolean> {
	
	private BookInventoryInfo book;
	
	public PayForBookEvent(BookInventoryInfo book) {
		this.book = book;
	}

	public BookInventoryInfo getBook() {
		return book;
	}
	
	
}
