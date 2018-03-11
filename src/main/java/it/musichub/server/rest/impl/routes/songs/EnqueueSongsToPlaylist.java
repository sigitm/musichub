package it.musichub.server.rest.impl.routes.songs;

import java.util.List;

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
import it.musichub.server.library.model.Song;
import it.musichub.server.rest.ListPaginator;
import it.musichub.server.rest.impl.AbstractRoute;
import it.musichub.server.rest.model.RestDtoMapper;
import it.musichub.server.rest.model.SongDto;
import it.musichub.server.rest.model.SongDtoList;
import it.musichub.server.search.model.Query;
import spark.Request;
import spark.Response;

@Api
@Path("/songs")
@Produces("application/json")
public class EnqueueSongsToPlaylist extends AbstractRoute {

	@PATCH
	@ApiOperation(value = "Enqueue songs to Playlist", nickname = "EnqueueSongsToPlaylist", tags = "songs")
	@ApiImplicitParams({ //
//			@ApiImplicitParam(required = true, dataType = "string", name = "auth", paramType = "header"), //
			@ApiImplicitParam(required = false, dataType = "string", name = "title", paramType = "query"), //
			@ApiImplicitParam(required = false, dataType = "string", name = "artist", paramType = "query"), //
			@ApiImplicitParam(required = false, dataType = "string", name = "albumTitle", paramType = "query"), //
			@ApiImplicitParam(required = false, dataType = "integer", name = "year", paramType = "query"), //
			@ApiImplicitParam(required = false, dataType = "string", name = "genre", paramType = "query"), //
			@ApiImplicitParam(required = false, dataType = "integer", name = "rating", paramType = "query"), //
	}) //
	@ApiResponses(value = { //
			@ApiResponse(code = 200, message = "Success", response = SongDtoList.class), //
//			@ApiResponse(code = 400, message = "Invalid input data", response = ApiError.class), //
//			@ApiResponse(code = 401, message = "Unauthorized", response = ApiError.class), //
//			@ApiResponse(code = 404, message = "User not found", response = ApiError.class) //
	})
	public Object handle(@ApiParam(hidden = true) Request request, @ApiParam(hidden = true) Response response) throws Exception {
		Query query = new Query();
		query.addClause(encodeSongParams(request));
		
		List<Song> songs = getSearchService().search(query);
		List<SongDto> songsDto = RestDtoMapper.toSongDto(songs);
		
//		getUpnpControllerService().getRendererState().getPlaylist().clear();
		getUpnpControllerService().getRendererState().getPlaylist().addSongs(songs);
		
//		List<Song> songsFromPlaylist = getUpnpControllerService().getRendererState().getPlaylist().getSongs();
//		List<SongDto> songsFromPlaylistDto = RestDeviceMapper.toSongDto(songsFromPlaylist);
		
		Integer[] paginationParams = getPaginationParams(request);
		return ListPaginator.paginateList(songsDto, getUrl(request), paginationParams[0], paginationParams[1]);
	}
	
}
