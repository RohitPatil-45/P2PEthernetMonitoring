/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Kratos
 */
public class Datasource {

    public static Connection getConnection() {
        Connection con = null;

        String jdbcURL = "jdbc:mysql://localhost:9007/bescom?useSSL=false&rewriteBatchedStatements=true";

        String username = "root";
        String password = "Syst3m4$";
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(jdbcURL, username, password);

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Excep DB Connection" + e);
        }

        return con;
    }

}
