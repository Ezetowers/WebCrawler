package webcrawler.url.parser.matchers;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import logger.Logger;
import logger.LogLevel;
import webcrawler.url.parser.matchers.ResourceMatcher;

public class ImgResourceMatcher extends ResourceMatcher {
    // public ResourceMatched match(String url, String line) {
    public ResourceMatched match(String url, String line, String [] match) {
        Pattern pattern = Pattern.compile("<a [^>]*href=\"([^\"]*)\"[^>]*>");
        Matcher matcher = pattern.matcher(line);
        String urlMatched = new String();

        if (matcher.find()) {
            // Check if the URL has a <img> tag inside it 
            urlMatched = matcher.group(1);

            pattern = Pattern.compile("<img [^>]*src=\"([^\"]*)\"[^>]*>");
            Matcher nestedMatcher = pattern.matcher(line);
            if (nestedMatcher.find()) {
                if (urlMatched.startsWith("/")) {
                    urlMatched = url + urlMatched + nestedMatcher.group(1);
                }
            }
            else {
                // This is not an image. Let the next matcher find what to do
                return this.next(url, line, match);
            }
        }
        else {
            // The img tag could be alone in the line
            pattern = Pattern.compile("<img [^>]*src=\"([^\"]*)\"[^>]*>");
            matcher = pattern.matcher(line);

            if (matcher.find()) {
                urlMatched = matcher.group(1);

                if (urlMatched.startsWith("/")) {
                    urlMatched = url + urlMatched;
                }
            }
            else {
                // This is not an image. Let the next matcher find what to do
                return this.next(url, line, match);
            }
        }

        // If we arrived here, we got a valid URL
        resource_ = urlMatched;
        match[0] = urlMatched;
        return ResourceMatched.IMG;
    }
}