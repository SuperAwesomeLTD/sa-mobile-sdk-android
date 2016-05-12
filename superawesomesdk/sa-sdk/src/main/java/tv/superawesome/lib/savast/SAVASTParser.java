package tv.superawesome.lib.savast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import tv.superawesome.lib.sautils.SAAsyncTask;
import tv.superawesome.lib.sautils.SAApplication;
import tv.superawesome.lib.sautils.SAFileDownloader;
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

    public void execute(final String url, final SAVASTParserInterface listener) {
        this.listener = listener;

        SAAsyncTask task = new SAAsyncTask(SAApplication.getSAApplicationContext(), new SAAsyncTask.SAAsyncTaskInterface() {
            @Override
            public Object taskToExecute() throws IOException, SAXException, ParserConfigurationException  {
                List<SAVASTAd> ads = parseVAST(url);
                return ads;
            }

            @Override
            public void onFinish(Object result) {
                List<SAVASTAd> ads = new ArrayList<>();
                if (result != null){
                    ads = (List<SAVASTAd>)result;
                }

                if (listener != null) {
                    listener.didParseVAST(ads);
                }
            }

            @Override
            public void onError() {
                List<SAVASTAd> ads = new ArrayList<>();
                if (listener != null) {
                    listener.didParseVAST(ads);
                }
            }
        });
    }

    /**
     * The main parseing function
     * @param url - URL to the VAST
     * @return an array of VAST ads
     */
    private List<SAVASTAd> parseVAST(String url) throws IOException, ParserConfigurationException, SAXException {
        /** create the array of ads that should be returned */
        final List<SAVASTAd> lads = new ArrayList<SAVASTAd>();

        /** step 1: get the XML */
        SANetwork network = new SANetwork();
        String VAST = network.syncGet(url);

        if (VAST == null) {
            /**
             * return empty ads if  VAST string is NULL - this can sometimes happen because of SSL certificate issues
             */
            return lads;
        }

        /** create the Doc builder factory */
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        /** Parse the XML file */
        Document doc = db.parse(new InputSource(new ByteArrayInputStream(VAST.getBytes("utf-8"))));
        doc.getDocumentElement().normalize();

        /** step 2. get the correct reference to the root XML element */
        final Element root = (Element) doc.getElementsByTagName("VAST").item(0);

        /** step 3. check if the loaded XML document has "Ad" children */
        boolean hasAds = SAXML.checkSiblingsAndChildrenOf(root, "Ad");
        if (!hasAds) {
            return lads;
        }

        /** step 4. start finding ads and parsing them */
        SAXML.searchSiblingsAndChildrenOf(root, "Ad", new SAXML.SAXMLIterator() {
            @Override
            public void foundElement(Element e) {

                boolean isInLine = SAXML.checkSiblingsAndChildrenOf(e, "InLine");
                boolean isWrapper = SAXML.checkSiblingsAndChildrenOf(e, "Wrapper");

                if (isInLine){
                    SAVASTAd inlineAd = parseAdXML(e);
                    lads.add(inlineAd);
                } else if (isWrapper) {
                    SAVASTAd wrapperAd = parseAdXML(e);

                    String VASTAdTagURI = "";
                    Element uriPointer = SAXML.findFirstInstanceInSiblingsAndChildrenOf(e, "VASTAdTagURI");
                    if (uriPointer != null){
                        VASTAdTagURI = uriPointer.getTextContent();
                    }

                    try {
                        List<SAVASTAd> foundAds = parseVAST(VASTAdTagURI);
                        wrapperAd.creatives = SAUtils.removeAllButFirstElement(wrapperAd.creatives);

                        /** merge foundAds with wrapper ad */
                        for (SAVASTAd foundAd : foundAds) {
                            foundAd.sumAd(wrapperAd);
                        }

                        /** add to final return array */
                        for (SAVASTAd foundAd : foundAds) {
                            lads.add(foundAd);
                        }
                    } catch (IOException | ParserConfigurationException | SAXException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    /** do nothing */
                }
            }
        });

        return lads;
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
        ad.creatives = new ArrayList<>();

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

        /** get creatives */
        SAXML.searchSiblingsAndChildrenOf(adElement, "Creative", new SAXML.SAXMLIterator() {
            @Override
            public void foundElement(Element e) {
                SAVASTCreative linear = parseCreativeXML(e);
                if (linear != null){
                    ad.creatives.add(linear);
                }
            }
        });

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
