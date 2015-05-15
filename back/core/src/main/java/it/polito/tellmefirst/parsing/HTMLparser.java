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

package it.polito.tellmefirst.parsing;

import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;
import it.polito.tellmefirst.apimanager.RestManager;
import it.polito.tellmefirst.exception.TMFVisibleException;
import static it.polito.tellmefirst.util.TMFUtils.unchecked;
import java.net.URLEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class HTMLparser {

    static Log LOG = LogFactory.getLog(HTMLparser.class);

    // old one
    public String htmlToText(String url) throws TMFVisibleException {
        LOG.debug("[htmlToText] - BEGIN");
        String result;
        try{
        HtmlFetcher fetcher = new HtmlFetcher();
        JResult res = fetcher.fetchAndExtract(url, 77777, true);
        result = res.getText();
        }catch (Exception e){
            LOG.error("[htmlToText] - EXCEPTION: ", e);
            throw new TMFVisibleException("Unable to extract text from specified URL.");
        }
        LOG.debug("[htmlToText] - END");
        return result;
    }


    public String htmlToTextGoose(String url) throws TMFVisibleException {
        LOG.debug("[htmlToTextAE] - BEGIN");
		String aeUrl = "http://xaas.teamlife.it/tmf/ae/extraction?url="+unchecked(()->URLEncoder.encode(url, "UTF-8"));
        String result = new RestManager().getStringFromAPI(aeUrl);
        LOG.debug("[htmlToTextAE] - END");
        return result;
    }

    // just for testing
    public static void main(String[] args) throws Exception {
        HTMLparser parser = new HTMLparser();
        String result = parser.htmlToTextGoose("http://www.nytimes.com/2013/01/13/technology/aaron-swartz-internet-activist-dies-at-26.html?hp");
        //String result = parser.getImage("http://it.wikipedia.org/wiki/Sistemi_informatici");
        System.out.println(result);
        //JResult res = fetcher.fetchAndExtract("http://www.repubblica.it/economia/2012/08/23/news/borse_mercati_piazza_affari_spread_wall_street_gioved-41342741/?ref=HREA-1", 77777, true);
        //String title = res.getTitle();
        //String imageUrl = res.getImageUrl();
    }
}
