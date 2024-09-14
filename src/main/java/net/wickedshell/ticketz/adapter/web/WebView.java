package net.wickedshell.ticketz.adapter.web;

public enum WebView {

    INDEX("/welcome"), TICKET_LIST("/ticket_list");

    private final String route;

    WebView(String route) {
        this.route = route;
    }

    public String route() {
        return route;
    }

    public String redirectedRoute() {
        return prefixRoute("redirect");
    }

    private String prefixRoute(String prefix) {
        return String.format("%s:%s", prefix, route);
    }
}
