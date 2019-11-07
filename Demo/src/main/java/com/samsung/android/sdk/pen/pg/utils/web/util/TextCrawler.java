package com.samsung.android.sdk.pen.pg.utils.web.util;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by EUNJI on 2016-09-29.
 */
public class TextCrawler {

    private final static String TAG = "TextCrawler";

    public static final int ALL = -1;
    public static final int NONE = -2;

    private final String HTTP_PROTOCOL = "http://";
    private final String HTTPS_PROTOCOL = "https://";

    public TextCrawler() {
    }

    public LinkSourceContent makePreview(String url) {
        return new GetCode(1).executeSync(url);
    }

    /**
     * Get html code
     */
    public class GetCode extends AsyncTask<String, Void, Void> {

        private LinkSourceContent linkSourceContent = new LinkSourceContent();
        private int imageQuantity;
        private ArrayList<String> urls;

        public GetCode(int imageQuantity) {
            this.imageQuantity = imageQuantity;
        }

        public LinkSourceContent executeSync(String url) {
            doInBackground(url);
            return linkSourceContent;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(String... params) {
            // Don't forget the http:// or https://
            urls = Utils.matches(params[0]);

            if (urls.size() > 0) {
                linkSourceContent.setFinalUrl(unshortenUrl(extendedTrim(urls.get(0))));
            } else {
                linkSourceContent.setFinalUrl("");
            }

            if (!linkSourceContent.getFinalUrl().equals("")) {
                if (isImage(linkSourceContent.getFinalUrl())
                        && !linkSourceContent.getFinalUrl().contains("dropbox")) {
                    linkSourceContent.setSuccess(true);

                    linkSourceContent.getImages().add(linkSourceContent.getFinalUrl());

                    linkSourceContent.setTitle("");
                    linkSourceContent.setDescription("");

                } else {
                    final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
                    final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
                    try {
                        Log.d(TAG, "GetCode$doInBackground, url : " + linkSourceContent.getFinalUrl());
                        Connection conn = Jsoup.connect(linkSourceContent.getFinalUrl()).header("User-Agent", USER_AGENT)
                                .header("Accept", ACCEPT);

                        CookieManager cm = CookieManager.getInstance();
                        String cookie = cm.getCookie(linkSourceContent.getFinalUrl());
                        if (!TextUtils.isEmpty(cookie)) {
                            Log.d(TAG, "GetCode$doInBackground, find cookie");
                            conn.cookie(linkSourceContent.getFinalUrl(), cookie);
                        }

                        Log.d(TAG, "GetCode$doInBackground, start get");
                        Document doc = null;
                        try {
                            doc = conn.get();
                        } catch (HttpStatusException e) {
                            Log.d(TAG, "GetCode$doInBackground, e: " + e.getMessage());
                            String html = request(linkSourceContent.getFinalUrl()).toString();
                            doc = Jsoup.parse(html);
                        }
                        Log.d(TAG, "GetCode$doInBackground, done get");

                        linkSourceContent.setHtmlCode(extendedTrim(doc.toString()));

                        String schemeHost = "";
                        Uri uri = Uri.parse(linkSourceContent.getFinalUrl());
                        if (uri != null) {
                            String scheme = uri.getScheme();
                            String host = uri.getHost();
                            if (!TextUtils.isEmpty(scheme) && !TextUtils.isEmpty(host)) {
                                schemeHost = scheme + "://" + host;
                            }
                        }
                        Log.d(TAG, "GetCode$doInBackground, schemeHost: " + schemeHost);

                        HashMap<String, String> metaTags = getMetaTags(linkSourceContent
                                .getHtmlCode(), schemeHost);

                        linkSourceContent.setMetaTags(metaTags);

                        linkSourceContent.setTitle(metaTags.get("title"));
                        linkSourceContent.setDescription(metaTags.get("description"));

                        if (linkSourceContent.getTitle().equals("")) {
                            Log.d(TAG, "GetCode$doInBackground, get title from title tag.");
                            String title = doc.title();
                            Log.d(TAG, "GetCode$doInBackground, title: " + title);
                            if (!TextUtils.isEmpty(title)) {
                                final int MAX_TITLE_CHAR_CNT = 49;
                                if (title.length() > MAX_TITLE_CHAR_CNT) {
                                    title = title.substring(0, MAX_TITLE_CHAR_CNT);
                                }
                                linkSourceContent.setTitle(title);
                            }
                        }

                        if (linkSourceContent.getDescription().equals("")) {
                            Log.d(TAG, "GetCode$doInBackground, get description from doc.");
                            String description = doc.text();
                            Log.d(TAG, "GetCode$doInBackground, description: " + description);
                            if (!TextUtils.isEmpty(description)) {
                                final int MAX_DESCRIPTION_CHAR_CNT = 200;
                                if (description.length() > MAX_DESCRIPTION_CHAR_CNT) {
                                    description = description.substring(0, MAX_DESCRIPTION_CHAR_CNT);
                                }
                                linkSourceContent.setDescription(description);
                            }
                        }

                        Log.d(TAG, "GetCode$doInBackground, remove script pattern.");
                        linkSourceContent.setDescription(linkSourceContent
                                .getDescription().replaceAll(Constants.SCRIPT_PATTERN, ""));

                        Log.d(TAG, "GetCode$doInBackground, set image uris.");
                        if (imageQuantity != NONE) {
                            if (!metaTags.get("image").equals("")) {
                                linkSourceContent.getImages().add(metaTags.get("image"));
                            } else {
                                linkSourceContent.setImages(getImages(doc,
                                        imageQuantity));
                            }
                        }

                        Log.d(TAG, "GetCode$doInBackground, setSuccess");
                        linkSourceContent.setSuccess(true);
                    } catch (Exception e) {
                        linkSourceContent.setSuccess(false);
                        Log.d(TAG, "GetCode$doInBackground, e : " + e.toString());
                    }
                }
            }

            String[] finalLinkSet = linkSourceContent.getFinalUrl().split("&");
            linkSourceContent.setUrl(finalLinkSet[0]);

            linkSourceContent.setCannonicalUrl(cannonicalPage(linkSourceContent
                    .getFinalUrl()));
            linkSourceContent.setDescription(stripTags(linkSourceContent
                    .getDescription()));

            Log.d(TAG, "GetCode$doInBackground, done");
            return null;
        }

        /**
         * Verifies if the content could not be retrieved
         */
        public boolean isNull() {
            return !linkSourceContent.isSuccess() &&
                    extendedTrim(linkSourceContent.getHtmlCode()).equals("") &&
                    !isImage(linkSourceContent.getFinalUrl());
        }

    }

    private static final String ACCEPT_LANG_FOR_US_LOCALE = "en-US";

    public static String getCurrentAcceptLanguage(Locale locale) {
        final StringBuilder buffer = new StringBuilder();
        addLocaleToHttpAcceptLanguage(buffer, locale);
        if (!Locale.US.equals(locale)) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(ACCEPT_LANG_FOR_US_LOCALE);
        }
        return buffer.toString();
    }

    private static String convertObsoleteLanguageCodeToNew(String langCode) {
        if (langCode == null) {
            return null;
        }
        if ("iw".equals(langCode)) {
            // Hebrew
            return "he";
        } else if ("in".equals(langCode)) {
            // Indonesian
            return "id";
        } else if ("ji".equals(langCode)) {
            // Yiddish
            return "yi";
        }
        return langCode;
    }
    private static void addLocaleToHttpAcceptLanguage(StringBuilder builder, Locale locale) {
        final String language = convertObsoleteLanguageCodeToNew(locale.getLanguage());
        if (language != null) {
            builder.append(language);
            final String country = locale.getCountry();
            if (country != null) {
                builder.append("-");
                builder.append(country);
            }
        }
    }
    private StringBuffer request(String urlString) {

        StringBuffer sb = new StringBuffer("");
        final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept-Language", getCurrentAcceptLanguage(Locale.getDefault()));
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            Log.e(TAG, "request", e);
        }

        Log.d(TAG, "request, sb length: " + sb.length());
        return sb;
    }

    /**
     * Gets content from a html tag
     */
    private String getTagContent(String tag, String content) {

        String pattern = "<" + tag + "(.*?)>(.*?)</" + tag + ">";
        String result = "", currentMatch = "";

        List<String> matches = Utils.pregMatchAll(content, pattern, 2);

        int matchesSize = matches.size();
        for (int i = 0; i < matchesSize; i++) {
            currentMatch = stripTags(matches.get(i));
            if (currentMatch.length() >= 120) {
                result = extendedTrim(currentMatch);
                break;
            }
        }

        if (result.equals("")) {
            String matchFinal = Utils.pregMatch(content, pattern, 2);
            result = extendedTrim(matchFinal);
        }

        result = result.replaceAll("&nbsp;", "");

        return htmlDecode(result);
    }

    /**
     * Gets images from the html code
     */
    public List<String> getImages(Document document, int imageQuantity) {
        List<String> matches = new ArrayList<String>();

        Elements media = document.select("[src]");

        for (Element srcElement : media) {
            if (srcElement.tagName().equals("img")) {
                matches.add(srcElement.attr("abs:src"));
            }
        }

        if (imageQuantity != ALL && matches.size() > 0)
            matches = matches.subList(0, imageQuantity);

        return matches;
    }

    /**
     * Transforms from html to normal string
     */
    private String htmlDecode(String content) {
        return Jsoup.parse(content).text();
    }

    /**
     * Crawls the code looking for relevant information
     */
    private String crawlCode(String content) {
        String result = "";
        String resultSpan = "";
        String resultParagraph = "";
        String resultDiv = "";

        resultSpan = getTagContent("span", content);
        resultParagraph = getTagContent("p", content);
        resultDiv = getTagContent("div", content);

        result = resultSpan;

        if (resultParagraph.length() > resultSpan.length()
                && resultParagraph.length() >= resultDiv.length())
            result = resultParagraph;
        else if (resultParagraph.length() > resultSpan.length()
                && resultParagraph.length() < resultDiv.length())
            result = resultDiv;
        else
            result = resultParagraph;

        return htmlDecode(result);
    }

    /**
     * Returns the cannoncial url
     */
    private String cannonicalPage(String url) {

        String cannonical = "";
        if (url.startsWith(HTTP_PROTOCOL)) {
            url = url.substring(HTTP_PROTOCOL.length());
        } else if (url.startsWith(HTTPS_PROTOCOL)) {
            url = url.substring(HTTPS_PROTOCOL.length());
        }

        int urlLength = url.length();
        for (int i = 0; i < urlLength; i++) {
            if (url.charAt(i) != '/')
                cannonical += url.charAt(i);
            else
                break;
        }

        return cannonical;

    }

    /**
     * Strips the tags from an element
     */
    private String stripTags(String content) {
        return Jsoup.parse(content).text();
    }

    /**
     * Verifies if the url is an image
     */
    private boolean isImage(String url) {
        return url.matches(Constants.IMAGE_PATTERN);
    }

    /**
     * Returns meta tags from html code
     */
    private HashMap<String, String> getMetaTags(String content, String schemeHost) {

        Log.d(TAG, "getMetaTags, schemeHost: " + schemeHost);

        HashMap<String, String> metaTags = new HashMap<String, String>();
        metaTags.put("url", "");
        metaTags.put("title", "");
        metaTags.put("description", "");
        metaTags.put("image", "");

        List<String> matches = Utils.pregMatchAll(content,
                Constants.METATAG_PATTERN, 1);

        for (String match : matches) {
            if (match.toLowerCase().contains("property=\"og:url\"")
                    || match.toLowerCase().contains("property='og:url'")
                    || match.toLowerCase().contains("name=\"url\"")
                    || match.toLowerCase().contains("name='url'")) {
                metaTags.put("url", separeMetaTagsContent(match));
            } else if (match.toLowerCase().contains("property=\"og:title\"")
                    || match.toLowerCase().contains("property='og:title'")
                    || match.toLowerCase().contains("name=\"title\"")
                    || match.toLowerCase().contains("name='title'")) {
                metaTags.put("title", separeMetaTagsContent(match));
            } else if (match.toLowerCase().contains("property=\"og:description\"")
                    || match.toLowerCase().contains("property='og:description'")
                    || match.toLowerCase().contains("name=\"description\"")
                    || match.toLowerCase().contains("name='description'")) {
                metaTags.put("description", separeMetaTagsContent(match));
            } else if (match.toLowerCase().contains("property=\"og:image\"")
                    || match.toLowerCase().contains("property='og:image'")
                    || match.toLowerCase().contains("itemprop=\"image\"")
                    || match.toLowerCase().contains("name=\"image\"")
                    || match.toLowerCase().contains("name='image'")) {
                String tagContent = separeMetaTagsContent(match);
                Log.d(TAG, "getMetaTags, tagContent: " + tagContent);
                if (!TextUtils.isEmpty(tagContent)
                        && tagContent.startsWith("/")
                        && !TextUtils.isEmpty(schemeHost)) {
                    tagContent = schemeHost + tagContent;
                    Log.d(TAG, "getMetaTags, tagContent: " + tagContent);
                }
                metaTags.put("image", tagContent);
            }
        }

        Log.d(TAG, "getMetaTags, done");
        return metaTags;
    }

    /**
     * Gets content from metatag
     */
    private String separeMetaTagsContent(String content) {
        String result = Utils.pregMatch(content, Constants.METATAG_CONTENT_PATTERN,
                1);
        return htmlDecode(result);
    }

    /**
     * Unshortens a short url
     */
    private String unshortenUrl(String shortURL) {
        if (!shortURL.startsWith(HTTP_PROTOCOL)
                && !shortURL.startsWith(HTTPS_PROTOCOL))
            return "";

        URLConnection urlConn = connectURL(shortURL);
        if (urlConn == null) {
            return "";
        }
        //urlConn.getHeaderFields();

        String finalResult = urlConn.getURL().toString();

        urlConn = connectURL(finalResult);
        if (urlConn == null) {
            return "";
        }
        //urlConn.getHeaderFields();

        shortURL = urlConn.getURL().toString();

        while (!shortURL.equals(finalResult)) {
            finalResult = unshortenUrl(finalResult);
        }

        return finalResult;
    }

    /**
     * Takes a valid url and return a URL object representing the url address.
     */
    private URLConnection connectURL(String strURL) {
        URLConnection conn = null;
        try {
            URL inputURL = new URL(strURL);
            conn = inputURL.openConnection();
        } catch (MalformedURLException e) {
            System.out.println("Please input a valid URL");
        } catch (IOException ioe) {
            System.out.println("Can not connect to the URL");
        }
        return conn;
    }

    /**
     * Removes extra spaces and trim the string
     */
    public static String extendedTrim(String content) {
        return content.replaceAll("\\s+", " ").replace("\n", " ")
                .replace("\r", " ").trim();
    }

}
