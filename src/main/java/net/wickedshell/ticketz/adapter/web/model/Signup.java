package net.wickedshell.ticketz.adapter.web.model;

import lombok.Data;

@Data
public class Signup {

    private String lastname;
    private String firstname;
    private String email;
    private String password;
    private String confirmPassword;
}
