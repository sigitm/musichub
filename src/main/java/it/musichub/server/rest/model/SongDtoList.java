package it.musichub.server.rest.model;

import java.util.List;

import it.musichub.server.rest.model.utils.PaginatedList;
import it.musichub.server.rest.model.utils.PagingLinks;

public class SongDtoList extends PaginatedList<SongDto> {

	public SongDtoList(int total, List<SongDto> results, PagingLinks _links) {
		super(total, results, _links);
	}

}
