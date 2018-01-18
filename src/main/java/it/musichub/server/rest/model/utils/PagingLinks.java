package it.musichub.server.rest.model.utils;

public class PagingLinks{
	public String self;
	public String prev;
	public String next;
	
	public PagingLinks(String self, String prev, String next) {
		super();
		this.self = self;
		this.prev = prev;
		this.next = next;
	}
}