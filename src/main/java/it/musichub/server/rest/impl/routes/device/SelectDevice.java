package it.musichub.server.rest.impl.routes.device;

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.musichub.server.rest.impl.AbstractRoute;
import it.musichub.server.rest.model.ApiError;
import it.musichub.server.rest.model.DeviceDto;
import it.musichub.server.rest.model.RestDeviceMapper;
import it.musichub.server.upnp.ex.DeviceNotFoundException;
import it.musichub.server.upnp.model.Device;
import spark.Request;
import spark.Response;

@Api
@Path("/devices/{id}")
@Produces("application/json")
public class SelectDevice extends AbstractRoute {

	@PUT
	@ApiOperation(value = "Set a device selected", nickname = "SelectDevice", tags = "devices")
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
			
		getUpnpControllerService().setSelectedDevice(device);
		
		DeviceDto deviceDto = RestDeviceMapper.toDto(device);
		
		return deviceDto;
	}
	
}
