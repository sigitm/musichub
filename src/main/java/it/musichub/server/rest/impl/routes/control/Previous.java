package it.musichub.server.rest.impl.routes.control;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.musichub.rest.model.ControlStatusDto;
import it.musichub.server.rest.impl.AbstractRoute;
import it.musichub.server.rest.impl.RestDtoMapper;
import it.musichub.server.upnp.renderer.IRendererState;
import spark.Request;
import spark.Response;

@Api
@Path("/control/previous")
@Produces("application/json")
public class Previous extends AbstractRoute {

	@PUT
	@ApiOperation(value = "Previous", nickname = "Previous", tags = "control")
	@ApiImplicitParams({ //
//			@ApiImplicitParam(required = true, dataType = "string", name = "auth", paramType = "header"), //
	}) //
	@ApiResponses(value = { //
			@ApiResponse(code = 200, message = "Success", response = ControlStatusDto.class), //
//			@ApiResponse(code = 401, message = "Unauthorized", response = ApiError.class), //
	})
	public Object handle(@ApiParam(hidden = true) Request request, @ApiParam(hidden = true) Response response) throws Exception {
		
		getUpnpControllerService().getRendererCommand().previous();
		
		IRendererState rs = getUpnpControllerService().getRendererState();
		ControlStatusDto controlStatusDto = RestDtoMapper.toControlStatusDto(rs);
		
		return controlStatusDto;
	}
	
}
