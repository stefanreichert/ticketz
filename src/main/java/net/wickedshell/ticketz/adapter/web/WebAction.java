package net.wickedshell.ticketz.adapter.web;

public class WebAction {

    public static final String ACTION_SHOW_INDEX = "/index";
    public static final String ACTION_SHOW_LOGIN = "/show_login";
    public static final String ACTION_LOGIN = "/login";
    public static final String ACTION_SHOW_SIGNUP = "/show_signup";
    public static final String ACTION_SIGNUP = "/signup";
    public static final String ACTION_SHOW_TICKET_LIST = "/secure/tickets";
    public static final String ACTION_NEW_TICKET = "/secure/tickets:new";
    public static final String ACTION_SHOW_TICKET = "/secure/tickets/{ticketNumber}";
    public static final String ACTION_DELETE_TICKET = "/secure/tickets/{ticketNumber}:delete";
    public static final String ACTION_SAVE_TICKET = "/secure/tickets/{ticketNumber}";

    private WebAction() {
        // private constructor to prevent instantiation
    }

    public static String redirectTo(String action) {
        return "redirect:" + action;
    }
}
