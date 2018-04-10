package it.musichub.server.rest.impl.routes.devices;

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
import it.musichub.rest.model.ApiError;
import it.musichub.rest.model.DeviceDto;
import it.musichub.server.rest.impl.AbstractRoute;
import it.musichub.server.rest.impl.RestDtoMapper;
import it.musichub.server.upnp.ex.DeviceNotFoundException;
import it.musichub.server.upnp.model.Device;
import spark.Request;
import spark.Response;

@Api
@Path("/devices/{id}")
@Produces("application/json")
public class GetDevice extends AbstractRoute {

	@GET
	@ApiOperation(value = "Get a device", nickname = "GetDevice", tags = "devices")
	@ApiImplicitParams({ //
//			@ApiImplicitParam(required = true, dataType = "string", name = "auth", paramType = "header"), //
		    @ApiImplicitParam(required = true, dataType = "string", name = "id", paramType = "path") //
	}) //
	@ApiResponses(value = { //
			@ApiResponse(code = 200, message = "Success", response = DeviceDto.class), //
//			@ApiResponse(code = 400, message = "Invalid input data", response = ApiError.class), //
//			@ApiResponse(code = 401, message = "Unauthorized", response = ApiError.class), //
			@ApiResponse(code = 404, message = "Device not found", response = ApiError.class) //
	})
	public Object handle(@ApiParam(hidden = true) Request request, @ApiParam(hidden = true) Response response) throws Exception {
		String id = request.params("id");
		
		Device device = null;
		try {
			device = getUpnpControllerService().getDevice(id);
		} catch (DeviceNotFoundException e){
			//Nothing to do
		}
		if (device == null){
			response.status(404);
			return new ApiError(response.status(), "Device not found");
		}
			
		DeviceDto deviceDto = RestDtoMapper.toDeviceDto(device);
		
		return deviceDto;
	}
	
}
