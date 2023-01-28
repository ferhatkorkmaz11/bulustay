package com.venividicode.bulustay.helpers;

import lombok.Getter;
import lombok.Setter;

import java.sql.*;

@Getter
public class DijkstraDBConnectorHelper {

    private Connection conn;

    public DijkstraDBConnectorHelper() {
        conn = null;
        try{
            Class.forName("org.mariadb.jdbc.Driver");
            String username = "DB_USERNAME";
            String password = "DB_PASS";
            String dbName = "DB_NAME";
            conn = DriverManager.getConnection("jdbc:mariadb://dijkstra.ug.bilkent.edu.tr/" + dbName, username, password);

        } catch (Exception e) {
            System.out.println("Cannot connect to database");
        }
    }

}
