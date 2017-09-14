package it.musichub.server.library.model;

import java.io.File;
import java.io.IOException;

import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import it.musichub.server.library.utils.FileUtils;

public class SongFactory {
	
	public static Song fromFilePath(String path, String baseFolderPath) throws UnsupportedTagException, InvalidDataException, IOException{
		Song song = new Song();
		File file = new File(path);
		Mp3File mp3file = new Mp3File(path);
		song.setPath(mp3file.getFilename());
		song.setRelativePath(FileUtils.getRelativePath(file, new File(baseFolderPath)));
		song.setFilename(FileUtils.extractFilename(mp3file.getFilename()));
		song.setFile(file);
		song.setSize(mp3file.getLength());
		song.setLastModified(file.lastModified());
		song.setLength(mp3file.getLengthInSeconds());
		song.setBitrate(mp3file.getBitrate());
		song.setVbr(mp3file.isVbr());
		

		String title = null;
		String artist = null;
		String album = null;
		Integer year = null;
		String track = null;
		String genre = null;
		Integer rating = null;
		byte[] albumImage = null;
		String albumImageMimeType = null;
		if (mp3file.hasId3v1Tag()){
			song.setId3v1(true);
			ID3v1 tag = mp3file.getId3v1Tag();
			
			if (mp3file.hasId3v2Tag()){
				song.setId3v2(true);
				ID3v2 tagv2 = mp3file.getId3v2Tag();
				tag = tagv2;
				if (tagv2.getWmpRating() > -1)
					rating = tagv2.getWmpRating();
				if (tagv2.getAlbumImage() != null){
					albumImage = tagv2.getAlbumImage();
					albumImageMimeType = tagv2.getAlbumImageMimeType();
				}
			}
			title = tag.getTitle();
			artist = tag.getArtist();
			album = tag.getAlbum();
			year = parseInt(tag.getYear());
			track = tag.getTrack();
			genre = tag.getGenreDescription();
		}else{
			//caso senza id3
			String extension = FileUtils.extractExtension(song.getFilename());
			title = song.getFilename().substring(0, song.getFilename().lastIndexOf(extension));
		}
		song.setTitle(title);
		song.setArtist(artist);
		song.setAlbum(album);
		song.setYear(year);
		song.setTrack(track);
		song.setGenre(genre);
		song.setRating(rating);
		song.setAlbumImage(albumImage);
		song.setAlbumImageMimeType(albumImageMimeType);
		
		return song;
	}
	
	private static Integer parseInt(String yearString){
		Integer year = null;
		try {
			year = Integer.parseInt(yearString);
		} catch (NumberFormatException e){
			//Nothing to do
		}
		return year;
	}
	
	public static MusicTrack toMusicTrack(Song song){
        String album = song.getAlbum();
        String creator = song.getArtist(); // Required
        PersonWithRole artist = new PersonWithRole(creator, "Performer");
        String title = song.getTitle();
        
        MimeType mimeType = new MimeType("audio", "mpeg");

        MusicTrack mt = new MusicTrack(
        		song.getId(), // Item ID,
        		song.getFolder().getId(), // parent Container ID
                title, creator, album, artist,
                new Res(mimeType, song.getSize(), song.getLengthHhMmSs(), song.getBitrate().longValue(), "http://10.0.0.1/files/101.mp3")
        );

		
		return mt;
		
//		return new TrackMetadata(Integer.toString(song.getId()), song.getTitle(), song.getArtist(), song.getGenre(), artURI, res, "object.item.audioItem");
	}
	
}

