package com.venividicode.bulustay.controllers;


import com.venividicode.bulustay.Enums.ServerResponseCodes;
import com.venividicode.bulustay.Objects.Response;
import com.venividicode.bulustay.helpers.DijkstraDBConnectorHelper;
import com.venividicode.bulustay.helpers.PasswordHashHandler;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class AuthenticationController {

    private DijkstraDBConnectorHelper dijkstraDBConnectorHelper;
    private PasswordHashHandler passwordHashHandler;
    private Connection conn;

    public AuthenticationController() {
        dijkstraDBConnectorHelper = new DijkstraDBConnectorHelper();
        passwordHashHandler = new PasswordHashHandler();
        conn = dijkstraDBConnectorHelper.getConn();
    }

    @PostMapping("/login")
    public Response login(@RequestBody Map<String, Object> req) {
        Response response;
        if (req.get("email") == null || req.get("password") == null) {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }

        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM BaseUser WHERE email = ?");
            preparedStatement.setString(1, (String) req.get("email"));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getInt("curCount") <= 0) {
                    response = Response.builder().message("The user with the email " + req.get("email") + " does not exist.").code(ServerResponseCodes.USER_NOT_FOUND.toString()).body(null).build();
                    return response;
                }
            }
        } catch (SQLException e) {
            response = Response.builder().message("General database exception").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        String salt = "";
        String hashedPassword = "";
        int userId = -1;
        boolean admin = false;
        boolean banned = false;
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM BaseUser WHERE email = ?");
            preparedStatement.setString(1, (String) req.get("email"));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                salt = resultSet.getString("password_salt");
                hashedPassword = resultSet.getString("hashed_password");
                userId = resultSet.getInt("user_id");
                preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM Admin WHERE user_id = ?");
                preparedStatement.setInt(1, resultSet.getInt("user_id"));
                resultSet = preparedStatement.executeQuery();
                resultSet.next();
                if (resultSet.getInt("curCount") > 0) {
                    admin = true;
                }
                preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM Ban WHERE user_id = ?");
                preparedStatement.setInt(1, userId);
                resultSet = preparedStatement.executeQuery();
                resultSet.next();
                if (resultSet.getInt("curCount") > 0) {
                    banned = true;
                }
            }
            if (salt.equals("") || hashedPassword.equals("")) {
                response = Response.builder().message("General database exception").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
                return response;
            }

        } catch (SQLException e) {
            response = Response.builder().message("General database exception").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        String passwordWithSalt = (String) req.get("password") + salt;
        String clientHashedPassword = passwordHashHandler.hashPassword(passwordWithSalt);
        if (!clientHashedPassword.equals(hashedPassword)) {
            response = Response.builder().message("Invalid email or password").code(ServerResponseCodes.INVALID_CREDENTIALS.toString()).body(null).build();
            return response;
        }

        Map<String, Object> responseBody = new HashMap<>();
        if(banned) {
            response = Response.builder().message("You are banned. You shall not pass.").code(ServerResponseCodes.USER_IS_BANNED.toString()).body(null).build();
            return response;
        }
        responseBody.put("email", (String) req.get("email"));
        responseBody.put("userId", userId);
        responseBody.put("isAdmin", admin);
        response = Response.builder().message("User is successfuly logged in.").code(ServerResponseCodes.SUCCESS.toString()).body(responseBody).build();
        return response;
    }

    @PostMapping("/register")
    public Response register(@RequestBody Map<String, Object> req) {
        Response response;

        if (req.get("email") == null || req.get("password") == null || req.get("name") == null) {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }

        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM BaseUser WHERE email = ?");
            preparedStatement.setString(1, (String) req.get("email"));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getInt("curCount") >= 1) {
                    response = Response.builder().message("The user with the email " + req.get("email") + " already exists.").code(ServerResponseCodes.USER_ALREADY_EXISTS.toString()).body(null).build();
                    return response;
                }
            }
        } catch (SQLException e) {
            response = Response.builder().message("General database exception").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        try {
            String salt = passwordHashHandler.generateSalt(32);
            String passwordWithSalt = (String) req.get("password") + salt;
            String hashedPasswordWithSalt = passwordHashHandler.hashPassword(passwordWithSalt);
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO BaseUser(name, hashed_password, password_salt, email) VALUES (?, ?, ?, ?)");

            preparedStatement.setString(1, (String) req.get("name"));
            preparedStatement.setString(2, hashedPasswordWithSalt);
            preparedStatement.setString(3, salt);
            preparedStatement.setString(4, (String) req.get("email"));
            preparedStatement.executeQuery();

        } catch (SQLException e) {
            response = Response.builder().message("Cannot register the user due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        response = Response.builder().message("User has successfully been registered.").code(ServerResponseCodes.SUCCESS.toString()).body(req).build();
        return response;
    }

    @PostMapping("/createAdmin")
    public Response createAdmin(@RequestBody Map<String, Object> req) {
        Response response;

        if (req.get("email") == null || req.get("password") == null || req.get("name") == null) {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }

        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM BaseUser WHERE email = ?");
            preparedStatement.setString(1, (String) req.get("email"));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getInt("curCount") >= 1) {
                    response = Response.builder().message("The user with the email " + req.get("email") + " already exists.").code(ServerResponseCodes.USER_ALREADY_EXISTS.toString()).body(null).build();
                    return response;
                }
            }
        } catch (SQLException e) {
            response = Response.builder().message("General database exception").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        try {
            String salt = passwordHashHandler.generateSalt(32);
            String passwordWithSalt = (String) req.get("password") + salt;
            String hashedPasswordWithSalt = passwordHashHandler.hashPassword(passwordWithSalt);
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO BaseUser(name, hashed_password, password_salt, email) VALUES (?, ?, ?, ?)");

            preparedStatement.setString(1, (String) req.get("name"));
            preparedStatement.setString(2, hashedPasswordWithSalt);
            preparedStatement.setString(3, salt);
            preparedStatement.setString(4, (String) req.get("email"));
            preparedStatement.executeQuery();
            preparedStatement = conn.prepareStatement("SELECT user_id FROM BaseUser WHERE email= ?");
            preparedStatement.setString(1, (String) req.get("email"));
            int adminId = -1;
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                adminId = resultSet.getInt("user_id");
            }

            preparedStatement = conn.prepareStatement("INSERT INTO Admin (user_id) VALUES (?)");
            preparedStatement.setInt(1, adminId);
            preparedStatement.executeQuery();

        } catch (SQLException e) {
            response = Response.builder().message("Cannot register the admin due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        response = Response.builder().message("Admin has successfully been registered.").code(ServerResponseCodes.SUCCESS.toString()).body(req).build();
        return response;
    }

    @PostMapping("/saveAdditionalRegisterInformation")
    public Response saveAdditionalRegisterInformation(@RequestBody Map<String, Object> req) {
        Response response;

        if (req.get("email") == null || req.get("birthdate") == null || req.get("city") == null || req.get("district") == null
                || req.get("neighbourhood") == null || req.get("streetNo") == null || req.get("street") == null
                || req.get("building") == null || req.get("phone") == null || req.get("gender") == null) {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }
        Statement query = null;
        int userId = -1;
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT user_id FROM BaseUser WHERE email= ?");
            preparedStatement.setString(1, (String) req.get("email"));

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                userId = resultSet.getInt("user_id");
            }
            if (userId == -1) {
                response = Response.builder().message("User with email " + (String) req.get("email") + " not found.").code(ServerResponseCodes.USER_NOT_FOUND.toString()).body(null).build();
                return response;
            }
        } catch (SQLException e) {
            response = Response.builder().message("General database exception").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO User(user_id, birthdate, city_name, district_name, neighbourhood, street_number, street_name, building, phone_number, gender, balance)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0.0)");

            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, (String) req.get("birthdate"));
            preparedStatement.setString(3, (String) req.get("city"));
            preparedStatement.setString(4, (String) req.get("district"));
            preparedStatement.setString(5, (String) req.get("neighbourhood"));
            preparedStatement.setString(6, (String) req.get("streetNo"));
            preparedStatement.setString(7, (String) req.get("street"));
            preparedStatement.setString(8, (String) req.get("building"));
            preparedStatement.setString(9, (String) req.get("phone"));
            preparedStatement.setString(10, (String) req.get("gender"));
            preparedStatement.executeQuery();

        } catch (SQLException e) {
            response = Response.builder().message("Cannot save additional info of the user due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        response = Response.builder().message("User's additional info have successfully been saved.").code(ServerResponseCodes.SUCCESS.toString()).body(req).build();
        return response;

    }

}
