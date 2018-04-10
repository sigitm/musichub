package it.musichub.server.rest.impl.routes.devices;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.jaxrs.PATCH;
import it.musichub.rest.model.ApiError;
import it.musichub.rest.model.DeviceDto;
import it.musichub.server.rest.impl.AbstractRoute;
import it.musichub.server.rest.impl.RestDtoMapper;
import it.musichub.server.upnp.ex.DeviceNotFoundException;
import it.musichub.server.upnp.model.Device;
import spark.Request;
import spark.Response;

@Api
@Path("/devices/selected")
@Produces("application/json")
public class ModifySelectedDevice extends AbstractRoute {

	@PATCH
	@ApiOperation(value = "Modify selected device", nickname = "ModifySelectedDevice", tags = "devices")
	@ApiImplicitParams({ //
//			@ApiImplicitParam(required = true, dataType = "string", name = "auth", paramType = "header"), //
//		    @ApiImplicitParam(required = true, dataType = "string", name = "id", paramType = "path"), //
		    @ApiImplicitParam(required = false, dataType = "string", name = "customName", paramType = "query") //
	}) //
	@ApiResponses(value = { //
			@ApiResponse(code = 200, message = "Success", response = DeviceDto.class), //
			@ApiResponse(code = 400, message = "nvalid input data: custom name not specified", response = ApiError.class), //
//			@ApiResponse(code = 401, message = "Unauthorized", response = ApiError.class), //
			@ApiResponse(code = 404, message = "No device selected", response = ApiError.class) //
	})
	public Object handle(@ApiParam(hidden = true) Request request, @ApiParam(hidden = true) Response response) throws Exception {
		boolean isParamCustomName = request.queryParams().contains("customName");
		if (!isParamCustomName){
			response.status(400);
			return new ApiError(response.status(), "Invalid input data: custom name not specified");
		}
		String paramCustomName = request.queryParams("customName");
		
		Device device = getUpnpControllerService().getSelectedDevice();
		if (device == null){
			response.status(404);
			return new ApiError(response.status(), "No device selected");
		}
		getUpnpControllerService().setDeviceCustomName(device.getUdn(), paramCustomName);
			
		DeviceDto deviceDto = RestDtoMapper.toDeviceDto(device);
		
		return deviceDto;
	}
	
}
