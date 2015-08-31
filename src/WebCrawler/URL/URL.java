package webcrawler.url;


public class URL {
    public URL(String url) {
        url_ = url;
    }

    public String get() {
        return url_;
    }

    private String url_;
}