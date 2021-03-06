package it.polito.tellmefirst.web.rest.images;

import static it.polito.tellmefirst.util.TMFUtils.*;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import it.polito.tellmefirst.apimanager.RestManager;
import it.polito.tellmefirst.util.Ret;
import it.polito.tellmefirst.util.TMFUtils;
import it.polito.tellmefirst.web.rest.interfaces.ImageInterface.ImgResponse;

import org.codehaus.jettison.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static it.polito.tellmefirst.web.rest.interfaces.ImageInterface.*;

public class ImagePolicyDAOImpl implements ImagePolicyDAO {
	
	static Log LOG = LogFactory.getLog(ImagePolicyDAOImpl.class);
	
	RestManager rm = new RestManager();

	@Override
	public Boolean existImage(String url) {
		return existsLink(url);
	}

	@Override
	public Double getAspectRatio(String title) {
		
		LOG.debug("[getAspectRatio] - BEGIN");
		
		String result = rm.getStringFromAPI(getAspectRatioWikiURL(title));
		LOG.debug("[getAspectRatio] - result = "+result);
		
		double ratio = 0.0;
		try{
				
			JSONObject getRatioJSONResult = new JSONObject(result);
			JSONObject queryObj = getRatioJSONResult.getJSONObject("query");
		    Map<String,String> out = new HashMap<String, String>();

		    TMFUtils.jsonObjToMap(queryObj,out);

		    if(sizeNotAvailable(out))
		    	return ratio;
		    
			int width = Integer.parseInt(out.get("width"));
			int height = Integer.parseInt(out.get("height"));
			
			ratio = (double) width / height ;
			LOG.debug("[getAspectRatio] - ratio = "+ratio);
			
    	} catch (Exception e) {
    		LOG.error(" width/height not found in response from wikipedia ",e);
    		
		}
     
		LOG.debug("[getAspectRatio] - END");
		return Double.valueOf(ratio);
	}
	
	private boolean sizeNotAvailable(Map<String, String> map){
		return  hasNoContent( map.get("width") ) || hasNoContent( map.get("height") );
	}
	
	// TODO FIXME XXX Implementare successivamente una classe di accesso alle property applicative.
    private String getAspectRatioWikiURL(final String title) {
    	return unchecked(()->{
				String encodedTitle = title;
				encodedTitle = URLEncoder.encode(title, "UTF-8");
				return "http://en.wikipedia.org/w/api.php?action=query&titles="+encodedTitle+"&prop=pageimages&format=json";
		}, "Wikipedia Ratio URL not resolved!");
    }

	@Override
	public ImgResponse getMobileWikiImg(String title) {
		String label = unchecked( ()-> URLEncoder.encode(title, "UTF-8") );
		ImgResponse link = getFilteredImgFrom("http://it.m.wikipedia.org/wiki/", label);
		if(!link.getSrc().isEmpty()){
			LOG.debug("img found from it : "+link);
			return link;
		} else {
			//TODO label to be Url encoded
	    	link 	= getFilteredImgFrom("http://en.m.wikipedia.org/wiki/", label);
	    	LOG.debug("img found from en : "+link);
			return link;
		}
	}

}
