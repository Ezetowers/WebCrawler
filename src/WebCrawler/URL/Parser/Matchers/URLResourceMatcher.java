package webcrawler.url.parser.matchers;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import webcrawler.url.parser.matchers.ResourceMatcher;

public class URLResourceMatcher extends ResourceMatcher {
    // public ResourceMatched match(String url, String line) {
    public ResourceMatched match(String url, String line, String[] match) {
        // <a ...href="http://wanted"...>
        Pattern pattern = Pattern.compile("<a [^>]*href=\"([^\"]*)\"[^>]*>");
        Matcher matcher = pattern.matcher(line);
        String urlMatched = new String();

        if (matcher.find()) {
            urlMatched = matcher.group(1);
            String nestedUrlMatched = null;

            // Check if the URL has a <img> tag inside it 
            pattern = Pattern.compile("<img [^>]*src=\"([^\"]*)\"[^>]*>");
            Matcher nestedMatcher = pattern.matcher(line);
            if (nestedMatcher.find()) {
                // It is an image, we cannot process this
                return this.next(url, line, match);
            }

            if (urlMatched.startsWith("/")) {
                urlMatched = url + urlMatched;
            }
            else if (! urlMatched.startsWith("http://") 
                && ! urlMatched.startsWith("https://")) {
                return this.next(url, line, match);
            }
        }
        else {
            // This is not an image. Let the next matcher find what to do
            return this.next(url, line, match);
        }

        // Check if the URL has an extension
        String extRegex = "^.*\\.(jpg|JPG|gif|GIF|doc|DOC|docx|DOCX|pdf|PDF)$";
        pattern = Pattern.compile(extRegex);
        matcher = pattern.matcher(urlMatched);
        if (matcher.find()) {
            match[0] = urlMatched;

            switch (matcher.group(1)) {
                case "jpg":
                case "JPG":
                case "gif":
                case "GIF":
                    return ResourceMatched.IMG;
                case "doc":
                case "DOC":
                case "docx":
                case "DOCX":
                case "pdf":
                case "PDF":
                    return ResourceMatched.DOC;
            }
        }

        // If we arrived here, we got a valid URL
        match[0] = urlMatched;
        return ResourceMatched.URL;
    }
}