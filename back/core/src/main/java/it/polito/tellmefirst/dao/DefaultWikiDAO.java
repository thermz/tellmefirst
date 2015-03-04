package it.polito.tellmefirst.dao;

import it.polito.tellmefirst.apimanager.RestManager;
import static it.polito.tellmefirst.util.TMFUtils.existsLink;
import static it.polito.tellmefirst.util.TMFUtils.unchecked;
import java.net.URLEncoder;
import static java.net.URLEncoder.encode;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class DefaultWikiDAO implements WikiDAO {

	RestManager rm = new RestManager();
	
	@Override
	public List<String> getFileLabels(String searchText) {
		// FIXME predisporre una classe più "open-closed" di RestManager per invocare API tramite HTTP
		String xml = rm.getStringFromAPI(getWikiURL(searchText));
        Document doc = Jsoup.parse(xml);
        Elements elementsFound = doc.getElementsByTag("im");
        List<String> imagesFound = new ArrayList<String>();
		elementsFound.forEach((e) -> {
			imagesFound.add(e.attr("title"));
		});
		return imagesFound;
	}

	@Override
	public String getURLFromFileLabel(String fileLabel) {
		String xml = rm.getStringFromAPI(getWikiImgURL(fileLabel));
        Document doc = Jsoup.parse(xml);
        final Elements elementsFound = doc.getElementsByTag("ii");
        return unchecked(() -> elementsFound.first().attr("url"), "Wikipedia image URL not found");
	}
	
	// TODO FIXME XXX Implementare successivamente una classe di accesso alle property applicative.
    private String getWikiURL(final String label) {
    	return unchecked(() -> "http://en.wikipedia.org/w/api.php?action=query&prop=images&format=xml&titles="+encode(label.trim().replace(" ", "_"), "UTF-8"), "Wikipedia URL not resolved!");
    }
    
    // TODO FIXME XXX Implementare successivamente una classe di accesso alle property applicative.
    private String getWikiImgURL(final String label) {
    	return unchecked(() -> "http://en.wikipedia.org/w/api.php?action=query&iiprop=url&prop=imageinfo&format=xml&titles="+encode(label.trim().replace(" ", "+"), "UTF-8"), "Wikipedia image URL not resolved!");
    }

    /**
     * Send an HTTP HEAD to verify the image actually exists at that URL
     * 
     * @return <tt>true</tt> if the image exists <tt>false</tt> otherwise
     */
	@Override
	public Boolean existsImage(String url) {
		return existsLink(url);
	}

}
