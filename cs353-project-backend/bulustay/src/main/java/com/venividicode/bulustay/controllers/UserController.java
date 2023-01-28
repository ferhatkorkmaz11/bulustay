package com.venividicode.bulustay.controllers;

import com.venividicode.bulustay.Enums.ServerResponseCodes;
import com.venividicode.bulustay.Objects.Response;
import com.venividicode.bulustay.helpers.DijkstraDBConnectorHelper;
import com.venividicode.bulustay.helpers.PasswordHashHandler;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class UserController {
    private DijkstraDBConnectorHelper dijkstraDBConnectorHelper;
    private PasswordHashHandler passwordHashHandler;
    private Connection conn;

    public UserController() {
        dijkstraDBConnectorHelper = new DijkstraDBConnectorHelper();
        passwordHashHandler = new PasswordHashHandler();
        conn = dijkstraDBConnectorHelper.getConn();
    }

    @GetMapping("/getAllUsers")
    public Response getAllUsers(@RequestParam Map<String, Object> req) {
        Response response;

        try {
            List<Map<String, Object>> responseBody = new ArrayList<>();
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM User u, BaseUser b WHERE  u.user_id = b.user_id");

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Map<String, Object> curResponse = new HashMap<>();
                curResponse.put("birthdate", rs.getDate("birthdate"));
                curResponse.put("city", rs.getString("city_name"));
                curResponse.put("district", rs.getString("district_name"));
                curResponse.put("neighbourhood", rs.getString("neighbourhood"));
                curResponse.put("streetNo", rs.getString("street_number"));
                curResponse.put("street", rs.getString("street_name"));
                curResponse.put("building", rs.getString("building"));
                curResponse.put("gender", rs.getString("gender"));
                curResponse.put("balance", rs.getFloat("balance"));
                curResponse.put("name", rs.getString("name"));
                curResponse.put("email", rs.getString("email"));
                curResponse.put("phone", rs.getString("phone_number"));
                curResponse.put("userId", rs.getInt("user_id"));
                responseBody.add(curResponse);
            }
            response = Response.builder().body(responseBody).code(ServerResponseCodes.SUCCESS.toString()).message("Successfully retrieved all users").build();
            return response;
        } catch (SQLException e) {
            response = Response.builder().message("Cannot get the user information due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    @PostMapping("/banUser")
    public Response banUser(@RequestBody Map<String, Object> req) {
        Response response;
        if (req.get("userId") == null || req.get("adminId") == null) {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM User u WHERE u.user_id = ?");
            preparedStatement.setInt(1, (int) req.get("userId"));
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                if (rs.getInt("curCount") <= 0) {
                    response = Response.builder().message("User with id " +  (int) req.get("userId") + " not found.").code(ServerResponseCodes.USER_NOT_FOUND.toString()).body(null).build();
                    return response;
                }
            }

            preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM Admin a WHERE user_id = ?");
            preparedStatement.setInt(1, (int) req.get("adminId"));
            rs = preparedStatement.executeQuery();

            while (rs.next()) {
                if (rs.getInt("curCount") <= 0) {
                    response = Response.builder().message("Admin with id " + req.get("adminId") + " not found.").code(ServerResponseCodes.ADMIN_NOT_FOUND.toString()).body(null).build();
                    return response;
                }
            }

            preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM Ban WHERE user_id = ?");
            preparedStatement.setInt(1, (int) req.get("userId"));
            rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("curCount") > 0) {
                response = Response.builder().message("User with Id " + (int)req.get("userId") + " is already banned.").code(ServerResponseCodes.USER_IS_ALREADY_BANNED.toString()).body(null).build();
                return response;
            }

            preparedStatement = conn.prepareStatement("INSERT INTO Ban(admin_id, user_id) VALUES (?, ?)");
            preparedStatement.setInt(1, (int) req.get("adminId"));
            preparedStatement.setInt(2, (int) req.get("userId"));
            rs = preparedStatement.executeQuery();
            preparedStatement = conn.prepareStatement("DELETE FROM Organizer WHERE user_id = ?");
            preparedStatement.setInt(1, (int) req.get("userId"));
            rs = preparedStatement.executeQuery();
            response = Response.builder().message("User with Id " + (int)req.get("userId") + " has been banned.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
            return response;

        } catch (SQLException e) {
            response = Response.builder().message("Cannot get the user information due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    @PostMapping("/forgiveUser")
    public Response forgiveUser(@RequestBody Map<String, Object> req) {
        Response response;
        if (req.get("userId") == null || req.get("adminId") == null) {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM User u WHERE u.user_id = ?");
            preparedStatement.setInt(1, (int) req.get("userId"));
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                if (rs.getInt("curCount") <= 0) {
                    response = Response.builder().message("User with id " +  (int) req.get("userId") + " not found.").code(ServerResponseCodes.USER_NOT_FOUND.toString()).body(null).build();
                    return response;
                }
            }

            preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM Admin a WHERE user_id = ?");
            preparedStatement.setInt(1, (int) req.get("adminId"));
            rs = preparedStatement.executeQuery();

            while (rs.next()) {
                if (rs.getInt("curCount") <= 0) {
                    response = Response.builder().message("Admin with id " + req.get("adminId") + " not found.").code(ServerResponseCodes.ADMIN_NOT_FOUND.toString()).body(null).build();
                    return response;
                }
            }

            preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM Ban WHERE user_id = ?");
            preparedStatement.setInt(1, (int) req.get("userId"));
            rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("curCount") <= 0) {
                response = Response.builder().message("User with Id " + (int)req.get("userId") + " is not banned.").code(ServerResponseCodes.USER_IS_ALREADY_BANNED.toString()).body(null).build();
                return response;
            }

            preparedStatement = conn.prepareStatement("DELETE FROM Ban WHERE user_id = ?");
            preparedStatement.setInt(1, (int) req.get("userId"));
            rs = preparedStatement.executeQuery();
            response = Response.builder().message("User with Id " + (int)req.get("userId") + " has been forgiven my lord.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
            return response;

        } catch (SQLException e) {
            response = Response.builder().message("Cannot get the user information due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    @GetMapping("/getBannedUsers")
    public Response getBannedUsers(@RequestParam Map<String, Object> req) {
        Response response;

        try {
            List<Map<String, Object>> responseBody = new ArrayList<>();
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM User u, BaseUser b WHERE  u.user_id = b.user_id");

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Map<String, Object> curResponse = new HashMap<>();
                curResponse.put("birthdate", rs.getDate("birthdate"));
                curResponse.put("city", rs.getString("city_name"));
                curResponse.put("district", rs.getString("district_name"));
                curResponse.put("neighbourhood", rs.getString("neighbourhood"));
                curResponse.put("streetNo", rs.getString("street_number"));
                curResponse.put("street", rs.getString("street_name"));
                curResponse.put("building", rs.getString("building"));
                curResponse.put("gender", rs.getString("gender"));
                curResponse.put("balance", rs.getFloat("balance"));
                curResponse.put("name", rs.getString("name"));
                curResponse.put("email", rs.getString("email"));
                curResponse.put("phone", rs.getString("phone_number"));
                curResponse.put("userId", rs.getInt("user_id"));
                if(isBanned(rs.getInt("user_id"))) {
                    responseBody.add(curResponse);
                }
            }
            response = Response.builder().body(responseBody).code(ServerResponseCodes.SUCCESS.toString()).message("Successfully retrieved banned users").build();
            return response;
        } catch (SQLException e) {
            response = Response.builder().message("Cannot get the user information due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    private boolean isBanned(int userId) throws SQLException{
        boolean result = false;
        PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM Ban WHERE user_id = ?");
        preparedStatement.setInt(1, userId);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        if (resultSet.getInt("curCount") > 0) {
            result = true;
            return result;
        }
        return  result;
    }

}
