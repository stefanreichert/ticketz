package net.wickedshell.ticketz.adapter.web;

public class Action {

    public static final String TICKET_NUMBER_NEW = "new";
    public static final String PROJECT_CODE_NEW = "new";

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
    public static final String ACTION_SAVE_TICKET_DETAILS = "/secure/tickets/{ticketNumber}/details";
    public static final String ACTION_SAVE_TICKET_STATUS = "/secure/tickets/{ticketNumber}/status";
    public static final String ACTION_SAVE_TICKET_COMMENT = "/secure/tickets/{ticketNumber}/comment";
    public static final String ACTION_SHOW_PROJECT_LIST = "/secure/projects";
    public static final String ACTION_NEW_PROJECT = "/secure/projects/" + PROJECT_CODE_NEW;
    public static final String ACTION_SHOW_PROJECT = "/secure/projects/{code}";
    public static final String ACTION_SAVE_PROJECT = "/secure/projects/{code}";
    public static final String ACTION_SHOW_PREFERENCES = "/secure/preferences/{email}";
    public static final String ACTION_SAVE_PREFERENCES_NAME = "/secure/preferences/{email}/name";
    public static final String ACTION_SAVE_PREFERENCES_PASSWORD = "/secure/preferences/{email}/password";
    public static final String ACTION_LOGOUT = "/secure/logout";
    public static final String ACTION_SHOW_USER_LIST = "/secure/users";
    public static final String ACTION_SHOW_USER = "/secure/users/{email}";
    public static final String ACTION_SAVE_USER_ROLES = "/secure/users/{email}/roles";

    private Action() {
        // private constructor to prevent instantiation
    }

    public static String redirectTo(String action) {
        return "redirect:" + action;
    }
}
