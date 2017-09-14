package it.musichub.server.upnp.model;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;
import it.musichub.server.upnp.model.x.TrackMetadata;
import it.musichub.server.library.model.Song;
import it.musichub.server.upnp.WebServer;
import it.musichub.server.upnp.model.x.IRendererCommand;
import it.musichub.server.upnp.model.x.IRendererState;
import it.musichub.server.upnp.model.x.RendererCommand;
import it.musichub.server.upnp.model.x.RendererState;

public class UpnpFactory {

//	@Override
//	public IUpnpServiceController createUpnpServiceController(Context ctx)
//	{
//		return new ServiceController(ctx);
//	}

	public static IRendererState createRendererState() {
		return new RendererState();
	}

	public static IRendererCommand createRendererCommand(ControlPoint cp, IRendererState rs) {
		return new RendererCommand(cp, (RendererState) rs);
	}
	
	
	public static MusicTrack songToMusicTrack(WebServer httpServer, Song song){
        String album = song.getAlbum();
        String creator = song.getArtist(); // Required
        PersonWithRole artist = new PersonWithRole(creator, "Performer");
        String title = song.getTitle();
        
        MimeType mimeType = new MimeType("audio", "mpeg");

        String URI = httpServer.getSongFileUrl(song);

        MusicTrack mt = new MusicTrack(
        		song.getId(), // Item ID,
        		song.getFolder().getId(), // parent Container ID
                title, creator, album, artist,
                new Res(mimeType, song.getSize(), song.getLengthHhMmSs(), song.getBitrate().longValue(), URI)
        );

		
		return mt;
	}
	
	public static TrackMetadata songToTrackMetadata(WebServer httpServer, MusicTrack mt, Song song){
		String URI = httpServer.getSongFileUrl(song);
		String artURI = httpServer.getSongAlbumArtUrl(song);
		return new TrackMetadata(song.getId(), song.getTitle(), song.getArtist(), song.getGenre(), artURI, URI, MusicTrack.CLASS.getValue());
	}

}
