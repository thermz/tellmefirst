/**
 * TellMeFirst - A Knowledge Discovery Application
 *
 * Copyright (C) 2012 Federico Cairo, Giuseppe Futia, Federico Benedetto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.polito.tellmefirst.web.rest.interfaces;

import it.polito.tellmefirst.classify.Classifier;
import it.polito.tellmefirst.exception.TMFOutputException;
import it.polito.tellmefirst.exception.TMFVisibleException;
import it.polito.tellmefirst.util.Behaviour;
import it.polito.tellmefirst.util.Ret;
import it.polito.tellmefirst.web.rest.TMFListener;
import it.polito.tellmefirst.web.rest.services.Classify;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.transform.sax.TransformerHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.xml.sax.helpers.AttributesImpl;

import static it.polito.tellmefirst.classify.Classifier.getOptionalFields;
import static it.polito.tellmefirst.util.TMFUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class ClassifyInterface extends AbsResponseInterface {

    static Log LOG = LogFactory.getLog(ClassifyInterface.class);

    public String getJSON(String textStr, int numTopics, String lang, boolean wikihtml, String optionalFieldsComma) throws Exception {
        LOG.debug("[getJSON] - BEGIN");
        
        String xml = getXML(textStr, numTopics, lang, wikihtml);
        String result = xml2json(xml);
        
        //post-process result JSON string in order to add optional fields when required.
        if(hasContent(optionalFieldsComma))
        	result = addOptionalFields(result, optionalFieldsComma);
        
        //no prod
        LOG.info("--------Result from Classify--------");
        LOG.debug("[getJSON] - END");
        return result;
    }

    public String getXML(String textStr, int numTopics, String lang, boolean wikihtml) throws TMFVisibleException, TMFOutputException {
        LOG.debug("[getXML] - BEGIN");
        String result;
        Classifier classifier = (lang.equals("italian")) ? TMFListener.getItalianClassifier() : TMFListener.getEnglishClassifier();  
        ArrayList<String[]> topics = classifier.classify(textStr, numTopics, lang, wikihtml);
        result = produceXML(topics, wikihtml);
        LOG.debug("[getXML] - END");
        return result;
    }

    private String produceXML(ArrayList<String[]> topics, boolean wikihtml) throws TMFOutputException {
        LOG.debug("[produceXML] - BEGIN");
        String xml;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TransformerHandler hd = initXMLDoc(out);
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("","","service","","Classify");
            hd.startElement("","","Classification",atts);
            int i=0;
            for (String[] topic : topics){
            	
                if (i==0){
                    atts.clear();
                    hd.startElement("","","Resources",atts);
                }
                atts.addAttribute("","","uri","",topic[0]);
                atts.addAttribute("","","label","",topic[1]);
                atts.addAttribute("","","title","",topic[2]);
                atts.addAttribute("","","score","",topic[3]);
                atts.addAttribute("", "", "mergedTypes", "", topic[4]);
                atts.addAttribute("", "", "image", "", topic[5]);
                atts.addAttribute("", "", "wikilink", "", topic[6]);
                if(wikihtml)
                	atts.addAttribute("", "", "htmlwiki", "", topic[7]);
                hd.startElement("","","Resource",atts);
                hd.endElement("","","Resource");
                i++;
            }
            if (i>0) hd.endElement("","","Resources");
            hd.endElement("","","Classification");
            hd.endDocument();
            xml = out.toString("utf-8");
            //System.out.println(xml);
        } catch (Exception e) {
        	LOG.error("Error creating XML output.",e);
            throw new TMFOutputException("Error creating XML output.", e);
        }
        LOG.debug("[produceXML] - END");
        return xml;
    }
    
    private String addOptionalFields(final String result, final String optionalFieldsComma){
    	try{
			JSONObject classifyJSONResult = new JSONObject(result);
			JSONArray resourcesArray = classifyJSONResult.getJSONArray("Resources");
			JSONArray resultArray = new JSONArray();
			for (int i=0; i<resourcesArray.length() ;i++){
				LOG.debug("\n\n\nAdding optional fields: "+optionalFieldsComma+" for "+i+" object, where max length is "+resourcesArray.length()+" \n\n\n");
				resultArray.put(addOptionalFieldsInASingleObject(resourcesArray.getJSONObject(i), optionalFieldsComma));
			}
			classifyJSONResult.put("Resources", resultArray);
		    return classifyJSONResult.toString();
    	} catch (Exception e) {
    		LOG.error("Optional fields not added",e);
    		return result;
		}
    }
    
    private JSONObject addOptionalFieldsInASingleObject(JSONObject singleResult, final String optionalFieldsComma){
    	try{
		    String uri 	 = singleResult.getString("@uri");
		    String label = singleResult.getString("@label");
		    String [] optionalFields = optionalFieldsComma.split(",");
		    Map<String, String> optionalFieldsMap = getOptionalFields(uri, label, optionalFields);
		    for (Map.Entry<String, String> entry : optionalFieldsMap.entrySet())
		    	//XXX get rid of those '@'
		    	singleResult.put("@"+entry.getKey(), entry.getValue());
		    
		    return singleResult;
    	} catch (Exception e) {
    		LOG.error("Optional fields not added",e);
    		return singleResult;
		}
    }
    
}
