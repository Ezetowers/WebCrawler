package webcrawler.url.parser.matchers;

public abstract class ResourceMatcher {
    public enum ResourceMatched {
        URL,
        IMG,
        CSS,
        JS,
        DOC,
        UNKNOWN;
    }

    public ResourceMatcher() {
        next_ = null;
    }

    public void setNext(ResourceMatcher matcher) {
        next_ = matcher;
    }

    protected ResourceMatched next(String url, String line, String[] match) {
        if (next_ != null) {
            return next_.match(url, line, match);
        }

        return ResourceMatched.UNKNOWN;
    }

    abstract public ResourceMatched match(String url, String line, String [] match);

    protected ResourceMatcher next_;
}