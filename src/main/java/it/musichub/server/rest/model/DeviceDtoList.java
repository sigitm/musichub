package it.musichub.server.rest.model;

import java.util.List;

import it.musichub.server.rest.model.utils.PaginatedList;
import it.musichub.server.rest.model.utils.PagingLinks;

public class DeviceDtoList extends PaginatedList<DeviceDto> {

	public DeviceDtoList(int total, List<DeviceDto> results, PagingLinks _links) {
		super(total, results, _links);
	}

}
