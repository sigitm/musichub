package it.musichub.server.rest.impl.routes.device;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.musichub.server.rest.ListPaginator;
import it.musichub.server.rest.model.DeviceDto;
import it.musichub.server.rest.model.DeviceDtoList;
import it.musichub.server.rest.model.RestDeviceMapper;
import it.musichub.server.runner.ServiceFactory;
import it.musichub.server.runner.ServiceRegistry.Service;
import it.musichub.server.upnp.UpnpControllerService;
import it.musichub.server.upnp.model.Device;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.StringUtils;

@Api
@Path("/devices")
@Produces("application/json")
public class GetDevicesRoute implements Route {

//	private IndexerService getIndexerService(){
//		return (IndexerService) ServiceFactory.getServiceInstance(Service.indexer);
//	}
	
	private UpnpControllerService getUpnpControllerService(){
		return (UpnpControllerService) ServiceFactory.getServiceInstance(Service.upnpcontroller);
	}
	
	@GET
	@ApiOperation(value = "Gets all devices", nickname = "GetDevicesRoute", tags = "devices")
	@ApiImplicitParams({ //
//			@ApiImplicitParam(required = true, dataType = "string", name = "auth", paramType = "header"), //
			@ApiImplicitParam(required = false, dataType = "string", name = "customName", paramType = "query"), //
			@ApiImplicitParam(required = false, dataType = "boolean", name = "online", paramType = "query"), //
			@ApiImplicitParam(required = false, dataType = "integer", name = "limit", paramType = "query"), //
			@ApiImplicitParam(required = false, dataType = "integer", name = "start", paramType = "query"), //
	}) //
	@ApiResponses(value = { //
			@ApiResponse(code = 200, message = "Success", response = DeviceDtoList.class), //
//			@ApiResponse(code = 400, message = "Invalid input data", response = ApiError.class), //
//			@ApiResponse(code = 401, message = "Unauthorized", response = ApiError.class), //
//			@ApiResponse(code = 404, message = "User not found", response = ApiError.class) //
	})
	public DeviceDtoList handle(@ApiParam(hidden = true) Request request, @ApiParam(hidden = true) Response response) throws Exception {
		String paramCustomName = request.queryParams("customName");
		String paramOnline = request.queryParams("online");
		
		List<Device> devices = getUpnpControllerService().getDevices();
		List<DeviceDto> devicesDto = RestDeviceMapper.toDto(devices);
		
		List<DeviceDto> filteredDevicesDto = new ArrayList<>();
		for (DeviceDto device : devicesDto){
			if (!StringUtils.isEmpty(paramCustomName) && !paramCustomName.equals(device.getCustomName()))
				break;
			if (!StringUtils.isEmpty(paramOnline) && !Boolean.valueOf(paramOnline).equals(device.isOnline()))
				break;

			filteredDevicesDto.add(device);
		}
		
		//TODO XXX gestione dei fields custom (v. mongodb)
		
		//TODO XXX gestione dei filtri e dell'ordinamento
		
		String url = request.url()+"?"+request.queryString();
		Integer[] paginationParams = getPaginationParams(request);
		
		return (DeviceDtoList)ListPaginator.paginateList(filteredDevicesDto, url, paginationParams[0], paginationParams[1]);
	}
	
	protected static Integer[] getPaginationParams(Request request){ //DA METTERE SU CLASSE ASTRATTA
		String paramLimit = request.queryParams("limit");
		Integer limit = null;
		if (paramLimit != null){
			try {
				limit = Integer.valueOf(paramLimit);
			} catch (NumberFormatException e){
				//Nothing to do
			}
		}
		String paramStart = request.queryParams("start");
		Integer start = null;
		if (paramStart != null){
			try {
				start = Integer.valueOf(paramStart);
			} catch (NumberFormatException e){
				//Nothing to do
			}
		}
		return new Integer[]{limit, start};
	}

}
