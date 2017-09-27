package it.musichub.server.upnp.model;

import java.util.EnumMap;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.dlna.DLNAAttribute;
import org.fourthline.cling.support.model.dlna.DLNAAttribute.Type;
import org.fourthline.cling.support.model.dlna.DLNAFlags;
import org.fourthline.cling.support.model.dlna.DLNAFlagsAttribute;
import org.fourthline.cling.support.model.dlna.DLNAOperations;
import org.fourthline.cling.support.model.dlna.DLNAOperationsAttribute;
import org.fourthline.cling.support.model.dlna.DLNAProfiles;
import org.fourthline.cling.support.model.dlna.DLNAProtocolInfo;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;

import it.musichub.server.library.model.Song;
import it.musichub.server.upnp.MediaServer;
import it.musichub.server.upnp.renderer.IRendererCommand;
import it.musichub.server.upnp.renderer.IRendererState;
import it.musichub.server.upnp.renderer.RendererCommand;
import it.musichub.server.upnp.renderer.RendererState;

public class UpnpFactory {

	// @Override
	// public IUpnpServiceController createUpnpServiceController(Context ctx)
	// {
	// return new ServiceController(ctx);
	// }

	public static IRendererState createRendererState() {
		return new RendererState();
	}

	public static IRendererCommand createRendererCommand(ControlPoint cp, IRendererState rs) {
		return new RendererCommand(cp, (RendererState) rs);
	}

	@Deprecated
	public static MusicTrack songToMusicTrack(MediaServer httpServer, Song song) {
		String album = song.getAlbum();
		String creator = song.getArtist(); // Required
		PersonWithRole artist = new PersonWithRole(creator, "Performer");
		String title = song.getTitle();

		MimeType mimeType = MimeType.valueOf(DLNAProfiles.MP3.getContentFormat());

		EnumMap<DLNAAttribute.Type, DLNAAttribute> attributes = new EnumMap<>(DLNAAttribute.Type.class);
		attributes.put(Type.DLNA_ORG_OP, new DLNAOperationsAttribute(/*DLNAOperations.TIMESEEK,*/ DLNAOperations.RANGE ));
	    attributes.put(Type.DLNA_ORG_FLAGS, new DLNAFlagsAttribute(
	                        DLNAFlags.DLNA_V15, 
	                        DLNAFlags.CONNECTION_STALL, 
	                        DLNAFlags.STREAMING_TRANSFER_MODE,
	                        DLNAFlags.BACKGROUND_TRANSFERT_MODE)
	    );
	    DLNAProtocolInfo protocolInfo = new DLNAProtocolInfo(DLNAProfiles.MP3, attributes);
	    
		String URI = httpServer.getSongFileUrl(song);

		MusicTrack mt = new MusicTrack(song.getId(), // Item ID,
				song.getFolder().getId(), // parent Container ID
				title, creator, album, artist, new Res(protocolInfo, song.getSize(), song.getLengthHhMmSs(),
						song.getBitrate().longValue(), URI));

		return mt;
	}

	public static TrackMetadata songToTrackMetadata(MediaServer httpServer, Song song){
		String URI = httpServer.getSongFileUrl(song);
		String artURI = httpServer.getSongAlbumArtUrl(song);
		
		MimeType mimeType = MimeType.valueOf(DLNAProfiles.MP3.getContentFormat());
		
		EnumMap<DLNAAttribute.Type, DLNAAttribute> attributes = new EnumMap<>(DLNAAttribute.Type.class);
		attributes.put(Type.DLNA_ORG_OP, new DLNAOperationsAttribute(/*DLNAOperations.TIMESEEK,*/ DLNAOperations.RANGE ));
	    attributes.put(Type.DLNA_ORG_FLAGS, new DLNAFlagsAttribute(
	                        DLNAFlags.DLNA_V15, 
	                        DLNAFlags.CONNECTION_STALL, 
	                        DLNAFlags.STREAMING_TRANSFER_MODE,
	                        DLNAFlags.BACKGROUND_TRANSFERT_MODE)
	    );
	    DLNAProtocolInfo protocolInfo = new DLNAProtocolInfo(DLNAProfiles.MP3, attributes);
	      
		Res res = new Res(protocolInfo, song.getSize(), song.getLengthHhMmSs(), song.getBitrate().longValue(), URI);
		return new TrackMetadata(song.getId(), song.getFolder().getId(), song.getTitle(), song.getArtist(), song.getGenre(), artURI, res, MusicTrack.CLASS.getValue());
	}

}
