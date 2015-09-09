package webcrawler.url;

public class URLData {
    public URLData(int nestingLevel,
                   String rhsUrl) {
        nestingLevel_ = nestingLevel;
        url = rhsUrl;
    }

    public void incNestingLevel() {
        ++nestingLevel_;
    }

    public int nestingLevel() {
        return nestingLevel_;
    }

    public String url;
    public String body = "";
    private int nestingLevel_;
}