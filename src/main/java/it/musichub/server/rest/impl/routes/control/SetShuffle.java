package it.musichub.server.rest.impl.routes.control;

import javax.ws.rs.POST;
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
import it.musichub.rest.model.ControlStatusDto;
import it.musichub.server.rest.impl.AbstractRoute;
import it.musichub.server.rest.impl.RestDtoMapper;
import it.musichub.server.upnp.renderer.IRendererState;
import spark.Request;
import spark.Response;

@Api
@Path("/control/shuffle")
@Produces("application/json")
public class SetShuffle extends AbstractRoute {

	@POST
	@ApiOperation(value = "Set shuffle", nickname = "SetShuffle", tags = "control")
	@ApiImplicitParams({ //
//			@ApiImplicitParam(required = true, dataType = "string", name = "auth", paramType = "header"), //
			@ApiImplicitParam(required = false, dataType = "string", name = "value", paramType = "query") //
	}) //
	@ApiResponses(value = { //
			@ApiResponse(code = 200, message = "Success", response = ControlStatusDto.class), //
			@ApiResponse(code = 400, message = "Invalid input data", response = ApiError.class), //
//			@ApiResponse(code = 401, message = "Unauthorized", response = ApiError.class), //
	})
	public Object handle(@ApiParam(hidden = true) Request request, @ApiParam(hidden = true) Response response) throws Exception {
		
		boolean isParamValue = request.queryParams().contains("value");
		if (!isParamValue){
			response.status(400);
			return new ApiError(response.status(), "Value not specified");
		}
		String paramValue = request.queryParams("value");

		Boolean shuffle = null;
		if ("true".equalsIgnoreCase(paramValue) || "1".equals(paramValue))
			shuffle = true;
		else if ("false".equalsIgnoreCase(paramValue) || "0".equals(paramValue))
			shuffle = false;
		
		if (shuffle == null){
			response.status(400);
			return new ApiError(response.status(), "Value "+paramValue+" not valid");
		}
		
		getUpnpControllerService().getRendererState().getPlaylist().setShuffle(shuffle);
		
		
		IRendererState rs = getUpnpControllerService().getRendererState();
		ControlStatusDto controlStatusDto = RestDtoMapper.toControlStatusDto(rs);
		
		return controlStatusDto;
	}
	
}
