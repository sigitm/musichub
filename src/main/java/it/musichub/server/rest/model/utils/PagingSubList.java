package it.musichub.server.rest.model.utils;

import java.util.List;

public class PagingSubList<T> {
	public List<T> subList;
	public PagingCoordinates prev;
	public PagingCoordinates next;

	public PagingSubList(List<T> subList, PagingCoordinates prev, PagingCoordinates next) {
		super();
		this.subList = subList;
		this.prev = prev;
		this.next = next;
	}

	@Override
	public String toString() {
		return "PagingSubList [subList=" + subList + ", prev=" + prev + ", next=" + next + "]";
	}
}