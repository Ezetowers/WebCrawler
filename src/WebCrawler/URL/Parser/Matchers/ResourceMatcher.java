package webcrawler.url.parser.matchers;

public abstract class ResourceMatcher {
    public enum ResourceMatched {
        URL,
        IMG,
        CSS,
        JS,
        UNKNOWN;
    }

    public ResourceMatcher() {
        next_ = null;
        resource_ = null;
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

    public String resource() {
        if (resource_ != null && next_ != null) {
            return resource_;
        }

        if (next_ != null) {
            return next_.resource();
        }

        return null;
    }

    abstract public ResourceMatched match(String url, String line, String [] match);

    protected ResourceMatcher next_;
    protected String resource_;
}