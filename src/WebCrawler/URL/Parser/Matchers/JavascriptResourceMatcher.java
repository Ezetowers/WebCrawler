package webcrawler.url.parser.matchers;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import logger.Logger;
import logger.LogLevel;
import webcrawler.url.parser.matchers.ResourceMatcher;

public class JavascriptResourceMatcher extends ResourceMatcher {
    // public ResourceMatched match(String url, String line) {
    public ResourceMatched match(String url, String line, String [] match) {
        Pattern pattern = Pattern.compile("<script [^>]*src=\"([^\"]*\\.css[^\"]*)\"[^>]*>");
        Matcher matcher = pattern.matcher(line);
        String urlMatched = new String();

        if (matcher.find()) {
            // Check if the URL has a <img> tag inside it 
            urlMatched = matcher.group(1);

            if (urlMatched.startsWith("/")) {
                urlMatched = url + urlMatched;
            }
        }
        else {
            // This is not an CSS. Let the next matcher find what to do
            return this.next(url, line, match);
        }

        // If we arrived here, we got a valid URL
        match[0] = urlMatched;
        return ResourceMatched.JS;
    }
}