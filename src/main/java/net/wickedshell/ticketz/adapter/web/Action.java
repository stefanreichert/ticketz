package net.wickedshell.ticketz.adapter.web;

public class Action {

    public static final String TICKET_NUMBER_NEW = "new";

    public static final String ACTION_SHOW_INDEX = "/index";
    public static final String ACTION_SHOW_LOGIN = "/show_login";
    public static final String ACTION_LOGIN = "/login";
    public static final String ACTION_SHOW_SIGNUP = "/show_signup";
    public static final String ACTION_SIGNUP = "/signup";
    public static final String ACTION_SHOW_TICKET_LIST = "/secure/tickets";
    public static final String ACTION_NEW_TICKET = "/secure/tickets/" + TICKET_NUMBER_NEW;
    public static final String ACTION_SHOW_TICKET = "/secure/tickets/{ticketNumber}";
    public static final String ACTION_DELETE_TICKET = "/secure/tickets/{ticketNumber}:delete";
    public static final String ACTION_SAVE_TICKET = "/secure/tickets/{ticketNumber}";
    public static final String ACTION_LOGOUT = "/secure/logout";

    private Action() {
        // private constructor to prevent instantiation
    }

    public static String redirectTo(String action) {
        return "redirect:" + action;
    }
}
