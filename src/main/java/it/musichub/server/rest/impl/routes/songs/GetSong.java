package it.musichub.server.rest.impl.routes.songs;

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
import it.musichub.rest.model.ApiError;
import it.musichub.rest.model.SongDto;
import it.musichub.server.library.model.Song;
import it.musichub.server.rest.impl.AbstractRoute;
import it.musichub.server.rest.impl.RestDtoMapper;
import it.musichub.server.search.model.Query;
import it.musichub.server.search.model.SimpleClause;
import it.musichub.server.search.model.SimpleClause.Operator;
import spark.Request;
import spark.Response;

@Api
@Path("/songs/{id}")
@Produces("application/json")
public class GetSong extends AbstractRoute {

	@GET
	@ApiOperation(value = "Get a song", nickname = "GetSong", tags = "songs")
	@ApiImplicitParams({ //
//			@ApiImplicitParam(required = true, dataType = "string", name = "auth", paramType = "header"), //
		    @ApiImplicitParam(required = true, dataType = "string", name = "id", paramType = "path") //
	}) //
	@ApiResponses(value = { //
			@ApiResponse(code = 200, message = "Success", response = SongDto.class), //
//			@ApiResponse(code = 400, message = "Invalid input data", response = ApiError.class), //
//			@ApiResponse(code = 401, message = "Unauthorized", response = ApiError.class), //
			@ApiResponse(code = 404, message = "Device not found", response = ApiError.class) //
	})
	public Object handle(@ApiParam(hidden = true) Request request, @ApiParam(hidden = true) Response response) throws Exception {
		String id = request.params("id");
		
		Query query = new Query();
		query.addClause(new SimpleClause("id", Operator.EQUALS, id));
		
		List<Song> songs = getSearchService().search(query);
		if (songs.isEmpty()){
			response.status(404);
			return new ApiError(response.status(), "Song not found");
		}
		
		Song song = songs.get(0);
		SongDto songDto = RestDtoMapper.toSongDto(song);
		
		return songDto;
	}
	
}
