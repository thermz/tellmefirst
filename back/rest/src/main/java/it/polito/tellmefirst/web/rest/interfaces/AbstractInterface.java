package it.polito.tellmefirst.web.rest.interfaces;

import it.polito.tellmefirst.exception.TMFOutputException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.helpers.AttributesImpl;
import javax.xml.transform.sax.TransformerHandler;
import java.io.ByteArrayOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class AbstractInterface extends AbsResponseInterface {

    static Log LOG = LogFactory.getLog(AbstractInterface.class);

    public String getJSON(String uri, String lang) throws TMFOutputException {
        LOG.debug("[getJSON] - BEGIN");
        String result;
        String xml = getXML(uri, lang);
        result = xml2json(xml);
        LOG.debug("[getJSON] - END");
        return result;
    }

    public String getXML(String uri, String lang) throws TMFOutputException {
        LOG.debug("[getXML] - BEGIN");
        String result;
        String abstracto = enhancer.getAbstractFromDBpedia(uri, lang);
        result = produceXML(abstracto);
        LOG.debug("[getXML] - END");
        return result;
    }

    private String produceXML(String abstracto) throws TMFOutputException {
        String xml;
        LOG.debug("[produceXML] - BEGIN");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TransformerHandler hd = initXMLDoc(out);
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("","","abstract","", abstracto);
            hd.startElement("","","Enhancement",null);
            hd.startElement("","","Result",atts);
            hd.endElement("", "", "Result");
            hd.endElement("","","Enhancement");
            hd.endDocument();
            xml = out.toString("utf-8");
            System.out.println(xml);
        } catch (Exception e) {
            throw new TMFOutputException("Error creating XML output.", e);
        }
        LOG.debug("[produceXML] - END");
        return xml;
    }
}
