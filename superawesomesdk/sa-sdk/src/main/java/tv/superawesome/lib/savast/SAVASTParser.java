package tv.superawesome.lib.savast;

import android.util.Log;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import tv.superawesome.lib.sautils.SAAsyncTask;
import tv.superawesome.lib.sautils.SAApplication;
import tv.superawesome.lib.sautils.SAFileDownloader;
import tv.superawesome.lib.sautils.SANetInterface;
import tv.superawesome.lib.sautils.SANetwork;
import tv.superawesome.lib.sautils.SAUtils;
import tv.superawesome.lib.savast.models.SAVASTAd;
import tv.superawesome.lib.savast.models.SAVASTAdType;
import tv.superawesome.lib.savast.models.SAVASTCreative;
import tv.superawesome.lib.savast.models.SAVASTCreativeType;
import tv.superawesome.lib.savast.models.SAVASTMediaFile;
import tv.superawesome.lib.savast.models.SAVASTTracking;

/**
 * Created by gabriel.coman on 22/12/15.
 */
public class SAVASTParser {

    private SAVASTParserInterface listener;
    private List<SAVASTAd> ads;

    public void parseVASTAds(final String url, final SAVASTParserInterface listener) {
        this.listener = listener;

        this.parseVAST(url, new SAVASTParserInterface() {
            @Override
            public void didParseVAST(SAVASTAd ad) {
                // download files
                listener.didParseVAST(ad);
            }
        });
    }

    /**
     * The main parseing function
     * @param url - URL to the VAST
     * @return an array of VAST ads
     */
    private void parseVAST(String url, final SAVASTParserInterface listener) {
        /** step 1: get the XML */
        final SANetwork network = new SANetwork();
        network.asyncGet(url, new JSONObject(), new SANetInterface() {
            @Override
            public void success(Object data) {
                String VAST = (String)data;

                Document doc = null;
                try {
                    // get the XML doc and the root element
                    doc = SAXML.parseXML(VAST);
                    final Element root = (Element) doc.getElementsByTagName("VAST").item(0);

                    // do a check
                    if (! SAXML.checkSiblingsAndChildrenOf(root, "Ad")) {
                        listener.didParseVAST(null);
                        return;
                    }

                    // get the proper vast ad
                    Element adXML = SAXML.findFirstInstanceInSiblingsAndChildrenOf(root, "Ad");
                    final SAVASTAd ad = parseAdXML(adXML);

                    // inline case
                    if (ad.type == SAVASTAdType.InLine) {
                        listener.didParseVAST(ad);
                        return;
                    }
                    // wrapper case
                    else if (ad.type == SAVASTAdType.Wrapper) {
                        parseVAST(ad.redirectUri, new SAVASTParserInterface() {
                            @Override
                            public void didParseVAST(SAVASTAd wrapper) {
                                if (wrapper != null) {
                                    wrapper.sumAd(ad);
                                }

                                listener.didParseVAST(wrapper);
                                return;
                            }
                        });
                    }
                    // some other invalid case
                    else {
                        listener.didParseVAST(null);
                    }
                } catch (ParserConfigurationException | IOException | SAXException | NullPointerException e) {
                    listener.didParseVAST(null);
                }
            }

            @Override
            public void failure() {
                listener.didParseVAST(null);
            }
        });
    }

    /**
     * Function that parses a VAST XML "Ad" element into a SAVASTAd model
     * @param adElement XML elemend
     * @return a SAVAST ad object
     */
    public static SAVASTAd parseAdXML(Element adElement) {
        final SAVASTAd ad = new SAVASTAd();

        /** get ID and sequence */
        ad.id = adElement.getAttribute("id");
        ad.sequence = adElement.getAttribute("sequence");

        /** get type */
        ad.type = SAVASTAdType.Invalid;
        boolean isInLine = SAXML.checkSiblingsAndChildrenOf(adElement, "InLine");
        boolean isWrapper = SAXML.checkSiblingsAndChildrenOf(adElement, "Wrapper");

        if (isInLine) ad.type = SAVASTAdType.InLine;
        if (isWrapper) ad.type = SAVASTAdType.Wrapper;

        /** init ad arrays */
        ad.errors = new ArrayList<>();
        ad.impressions = new ArrayList<>();

        Element vastUri = SAXML.findFirstInstanceInSiblingsAndChildrenOf(adElement, "VASTAdTagURI");
        if (vastUri != null) {
            ad.redirectUri = vastUri.getTextContent();
        }

        /** get errors */
        SAXML.searchSiblingsAndChildrenOf(adElement, "Error", new SAXML.SAXMLIterator() {
            @Override
            public void foundElement(Element e) {
                String error = e.getTextContent();
                ad.errors.add(error);
            }
        });

        /** get impressions */
        ad.isImpressionSent = false;
        SAXML.searchSiblingsAndChildrenOf(adElement, "Impression", new SAXML.SAXMLIterator() {
            @Override
            public void foundElement(Element e) {
               ad.impressions.add(e.getTextContent());
            }
        });

        Element creativeXML = SAXML.findFirstInstanceInSiblingsAndChildrenOf(adElement, "Creative");
        ad.creative = parseCreativeXML(creativeXML);

        return ad;
    }

    /**
     * Function that parses a XML "Linear" VAST element and returns a SAVASTLinearCreative model
     * @param element a XML element
     * @return a valid SAVASTLinearCreative model
     */
    public static SAVASTCreative parseCreativeXML(Element element){
        /**
         * first find out what kind of content this creative has
         * is it Linear, NonLinear or CompanionAds?
         */
        boolean isLinear = SAXML.checkSiblingsAndChildrenOf(element, "Linear");

        /** init as a linear Creative */
        if (isLinear) {
            final SAVASTCreative creative = new SAVASTCreative();

            /** get attributes */
            creative.type = SAVASTCreativeType.Linear;
            creative.id = element.getAttribute("id");
            creative.sequence = element.getAttribute("sequence");

            /** create arrays */
            creative.mediaFiles = new ArrayList<>();
            creative.trackingEvents = new ArrayList<>();
            creative.clickTracking = new ArrayList<>();
            creative.customClicks = new ArrayList<>();

            /** populate duration */
            SAXML.searchSiblingsAndChildrenOf(element, "duration", new SAXML.SAXMLIterator() {
                @Override
                public void foundElement(Element e) {
                    creative.duration = e.getTextContent();
                }
            });

            /** populate click through */
            SAXML.searchSiblingsAndChildrenOf(element, "clickThrough", new SAXML.SAXMLIterator() {
                @Override
                public void foundElement(Element e) {
                    creative.clickThrough = e.getTextContent().replace("&amp;","&").replace("%3A",":").replace("%2F", "/");
                }
            });

            /** populate Click Tracking */
            SAXML.searchSiblingsAndChildrenOf(element, "clickTracking", new SAXML.SAXMLIterator() {
                @Override
                public void foundElement(Element e) {
                    creative.clickTracking.add(e.getTextContent());
                }
            });

            /** populate Custom Clicks */
            SAXML.searchSiblingsAndChildrenOf(element, "customClicks", new SAXML.SAXMLIterator() {
                @Override
                public void foundElement(Element e) {
                    creative.customClicks.add(e.getTextContent());
                }
            });

            /** populate Tracking */
            SAXML.searchSiblingsAndChildrenOf(element, "Tracking", new SAXML.SAXMLIterator() {
                @Override
                public void foundElement(Element e) {
                    SAVASTTracking tracking = new SAVASTTracking();
                    tracking.event = e.getAttribute("event");
                    tracking.url = e.getTextContent();
                    creative.trackingEvents.add(tracking);
                }
            });

            /** populate Media Files */
            SAXML.searchSiblingsAndChildrenOf(element, "MediaFile", new SAXML.SAXMLIterator() {
                @Override
                public void foundElement(Element e) {
                    SAVASTMediaFile mediaFile = new SAVASTMediaFile();
                    mediaFile.width = e.getAttribute("width");
                    mediaFile.height = e.getAttribute("height");
                    mediaFile.type = e.getAttribute("type");
                    mediaFile.url = e.getTextContent();

                    if (mediaFile.type.contains("mp4") || mediaFile.type.contains(".mp4")) {
                        creative.mediaFiles.add(mediaFile);
                    }
                }
            });

            /** add the playable media file */
            if (creative.mediaFiles.size() > 0) {
                creative.playableMediaUrl = creative.mediaFiles.get(0).url;
                if (creative.playableMediaUrl != null) {
                    creative.playableDiskUrl = SAFileDownloader.getInstance().downloadFileSync(creative.playableMediaUrl);
                    creative.isOnDisk = (creative.playableDiskUrl != null);
                }
            }

            /** return creative */
            return creative;
        }

        return null;
    }
}
