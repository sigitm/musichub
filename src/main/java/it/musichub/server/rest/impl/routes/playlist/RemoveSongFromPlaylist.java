package it.musichub.server.rest.impl.routes.playlist;

import java.util.List;

import javax.ws.rs.DELETE;
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
import it.musichub.rest.model.SongDto;
import it.musichub.rest.model.SongDtoList;
import it.musichub.server.library.model.Song;
import it.musichub.server.rest.ListPaginator;
import it.musichub.server.rest.impl.AbstractRoute;
import it.musichub.server.rest.impl.RestDtoMapper;
import spark.Request;
import spark.Response;

@Api
@Path("/playlist/{id}")
@Produces("application/json")
public class RemoveSongFromPlaylist extends AbstractRoute {

	@DELETE
	@ApiOperation(value = "Remove song from playlist", nickname = "RemoveSongFromPlaylist", tags = "playlist")
	@ApiImplicitParams({ //
//			@ApiImplicitParam(required = true, dataType = "string", name = "auth", paramType = "header"), //
			@ApiImplicitParam(required = true, dataType = "string", name = "id", paramType = "path") //
	}) //
	@ApiResponses(value = { //
			@ApiResponse(code = 200, message = "Success", response = SongDtoList.class), //
//			@ApiResponse(code = 400, message = "Invalid input data", response = ApiError.class), //
//			@ApiResponse(code = 401, message = "Unauthorized", response = ApiError.class), //
//			@ApiResponse(code = 404, message = "User not found", response = ApiError.class) //
	})
	public Object handle(@ApiParam(hidden = true) Request request, @ApiParam(hidden = true) Response response) throws Exception {
		String id = request.params("id");
		
		Song song = getUpnpControllerService().getRendererState().getPlaylist().getSongById(id);
		if (song == null){
			response.status(404);
			return new ApiError(response.status(), "Song not found in playlist");
		}
		
		getUpnpControllerService().getRendererState().getPlaylist().removeSong(song);
		
		List<Song> songs = getUpnpControllerService().getRendererState().getPlaylist().getSongs();
		List<SongDto> songsDto = RestDtoMapper.toSongDto(songs);
		
		Integer[] paginationParams = getPaginationParams(request);
		return ListPaginator.paginateList(songsDto, getUrl(request), paginationParams[0], paginationParams[1]);
	}
	
}
