package it.univaq.f4i.iw.ex.newspaper.data.model.impl;

import it.univaq.f4i.iw.ex.newspaper.data.model.User;
import it.univaq.f4i.iw.framework.data.DataItemImpl;

public class UserImpl extends DataItemImpl<Integer> implements User {

    private String username;
    private String password;

    public UserImpl() {
        super();
        username = "";
        password = "";
    }

    /**
     * @return the username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    @Override
    public void setPassword(String password) {
        this.password = password;
    }

}
