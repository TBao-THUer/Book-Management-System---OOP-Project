package models;

import javax.swing.*;
import java.sql.*;
import models.*;

public class Member extends Person {
    public Member(String username, String password) {
        super(username, password, "MEMBER");
    }

    @Override
    public boolean canManageBooks() { return false; }

    @Override
    public boolean canViewStaff() { return false; }
}

