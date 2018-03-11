package it.musichub.server.rest.impl.routes.playlist;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.musichub.server.library.model.Song;
import it.musichub.server.rest.ListPaginator;
import it.musichub.server.rest.impl.AbstractRoute;
import it.musichub.server.rest.model.RestDtoMapper;
import it.musichub.server.rest.model.SongDto;
import it.musichub.server.rest.model.SongDtoList;
import spark.Request;
import spark.Response;

@Api
@Path("/playlist")
@Produces("application/json")
public class ClearPlaylist extends AbstractRoute {

	@DELETE
	@ApiOperation(value = "Clear playlist", nickname = "ClearPlaylist", tags = "playlist")
	@ApiImplicitParams({ //
//			@ApiImplicitParam(required = true, dataType = "string", name = "auth", paramType = "header"), //
	}) //
	@ApiResponses(value = { //
			@ApiResponse(code = 200, message = "Success", response = SongDtoList.class), //
//			@ApiResponse(code = 400, message = "Invalid input data", response = ApiError.class), //
//			@ApiResponse(code = 401, message = "Unauthorized", response = ApiError.class), //
//			@ApiResponse(code = 404, message = "User not found", response = ApiError.class) //
	})
	public Object handle(@ApiParam(hidden = true) Request request, @ApiParam(hidden = true) Response response) throws Exception {
		
		getUpnpControllerService().getRendererState().getPlaylist().clear();
		
		List<Song> songs = getUpnpControllerService().getRendererState().getPlaylist().getSongs();
		List<SongDto> songsDto = RestDtoMapper.toSongDto(songs);
		
		Integer[] paginationParams = getPaginationParams(request);
		return ListPaginator.paginateList(songsDto, getUrl(request), paginationParams[0], paginationParams[1]);
	}
	
}
