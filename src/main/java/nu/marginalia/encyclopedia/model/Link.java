package nu.marginalia.encyclopedia.model;

public record Link(String url, String text) {
    public String url() {
        return url.replace("/", "%2F");
    }
}
