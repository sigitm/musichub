package it.musichub.server.rest.model.utils;

public class PagingCoordinates {
	public int start;
	public int limit;

	public PagingCoordinates(int start, int limit) {
		super();
		this.start = start;
		this.limit = limit;
	}

	@Override
	public String toString() {
		return "start="+start+"&limit="+limit;
	}
	
	
}