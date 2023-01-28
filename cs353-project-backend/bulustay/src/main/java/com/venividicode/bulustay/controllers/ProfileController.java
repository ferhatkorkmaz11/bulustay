package com.venividicode.bulustay.controllers;

import com.venividicode.bulustay.Enums.ServerResponseCodes;
import com.venividicode.bulustay.Objects.Response;
import com.venividicode.bulustay.helpers.DijkstraDBConnectorHelper;
import com.venividicode.bulustay.helpers.PasswordHashHandler;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class ProfileController {

    private DijkstraDBConnectorHelper dijkstraDBConnectorHelper;
    private PasswordHashHandler passwordHashHandler;
    private Connection conn;

    public ProfileController() {
        dijkstraDBConnectorHelper = new DijkstraDBConnectorHelper();
        passwordHashHandler = new PasswordHashHandler();
        conn = dijkstraDBConnectorHelper.getConn();
    }

    @GetMapping("/myProfile")
    public Response myProfile(@RequestParam Map<String, Object> req) {
        Response response;
        if (req.get("userId") == null || ((String) req.get("userId")).equals("")) {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        } else {
            try {
                PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM User u WHERE u.user_id = ?");
                preparedStatement.setInt(1, Integer.parseInt((String) req.get("userId")));
                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    if (rs.getInt("curCount") <= 0) {
                        response = Response.builder().message("User with id " + req.get("userId") + " not found.").code(ServerResponseCodes.USER_NOT_FOUND.toString()).body(null).build();
                        return response;
                    }
                }
                preparedStatement = conn.prepareStatement("SELECT * FROM User u, BaseUser b WHERE  u.user_id = b.user_id AND u.user_id = ?");
                preparedStatement.setInt(1, Integer.parseInt((String) req.get("userId")));
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    Map<String, Object> responseBody = new HashMap<>();
                    responseBody.put("birthdate", rs.getDate("birthdate"));
                    responseBody.put("city", rs.getString("city_name"));
                    responseBody.put("district", rs.getString("district_name"));
                    responseBody.put("neighbourhood", rs.getString("neighbourhood"));
                    responseBody.put("streetNo", rs.getString("street_number"));
                    responseBody.put("street", rs.getString("street_name"));
                    responseBody.put("building", rs.getString("building"));
                    responseBody.put("gender", rs.getString("gender"));
                    responseBody.put("balance", rs.getFloat("balance"));
                    responseBody.put("name", rs.getString("name"));
                    responseBody.put("email", rs.getString("email"));
                    responseBody.put("phone", rs.getString("phone_number"));
                    response = Response.builder().body(responseBody).code(ServerResponseCodes.SUCCESS.toString()).message("Successfully fetched user information with Id " + (String) req.get("userId")).build();
                    return response;
                }
                response = Response.builder().message("Cannot get the user information due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
                return response;
            } catch (SQLException e) {
                response = Response.builder().message("Cannot get the user information due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
                System.out.println(e.getLocalizedMessage());
                return response;
            }
        }
    }

    @PatchMapping("/editProfile")
    public Response editProfile(@RequestParam Map<String, Object> req) {
        Response response;
        String city;
        String district;
        String neighbourhood;
        String streetNo;
        String street;
        String building;
        String phone;
        if (req.get("userId") == null || req.get("userId").equals("")) {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM User u WHERE u.user_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("userId")));
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                if (rs.getInt("curCount") <= 0) {
                    response = Response.builder().message("User with id " + req.get("userId") + " not found.").code(ServerResponseCodes.USER_NOT_FOUND.toString()).body(null).build();
                    return response;
                }
            }

            preparedStatement = conn.prepareStatement("SELECT * FROM User u, BaseUser b WHERE  u.user_id = b.user_id AND u.user_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("userId")));
            rs = preparedStatement.executeQuery();
            rs.next();
            city = rs.getString("city_name");
            district = rs.getString("district_name");
            neighbourhood = rs.getString("neighbourhood");
            streetNo = rs.getString("street_number");
            street = rs.getString("street_name");
            building = rs.getString("building");
            phone = rs.getString("phone_number");


            if (req.get("city") != null && !((String) req.get("city")).equals("")) {
                city = (String) req.get("city");
            }
            if (req.get("district") != null && !((String) req.get("district")).equals("")) {
                district = (String) req.get("district");
            }
            if (req.get("neighbourhood") != null && !((String) req.get("neighbourhood")).equals("")) {
                neighbourhood = (String) req.get("neighbourhood");
            }
            if (req.get("streetNo") != null && !((String) req.get("streetNo")).equals("")) {
                streetNo = (String) req.get("streetNo");
            }
            if (req.get("street") != null && !((String) req.get("street")).equals("")) {
                street = (String) req.get("street");
            }
            if (req.get("building") != null && !((String) req.get("building")).equals("")) {
                building = (String) req.get("building");
            }
            if (req.get("phone") != null && !((String) req.get("phone")).equals("")) {
                phone = (String) req.get("phone");
            }

            preparedStatement = conn.prepareStatement("UPDATE User SET city_name = ?, district_name = ?,  neighbourhood = ?, street_number = ?, street_name = ?, building = ?, phone_number = ? WHERE user_id = ?");

            preparedStatement.setString(1, city);
            preparedStatement.setString(2, district);
            preparedStatement.setString(3, neighbourhood);
            preparedStatement.setString(4, streetNo);
            preparedStatement.setString(5, street);
            preparedStatement.setString(6, building);
            preparedStatement.setString(7, phone);
            preparedStatement.setInt(8, rs.getInt("user_id"));
            preparedStatement.executeQuery();
            response = Response.builder().message("Successfully edited the user info.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
            return response;

        } catch (SQLException e) {
            response = Response.builder().message("Cannot get edit the user information due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    @PostMapping("/addBalance")
    public Response addBalance(@RequestBody Map<String, Object> req) {
        Response response;
        if (req.get("CVV") == null || ((String) req.get("CVV")).equals("")
                || req.get("cardNo") == null || ((String) req.get("cardNo")).equals("")
                || req.get("cardOwner") == null || ((String) req.get("cardOwner")).equals("")
                || req.get("expirationDate") == null || ((String) req.get("expirationDate")).equals("")
                || req.get("userId") == null
                || req.get("amount") == null) {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }
        int userId = (int) req.get("userId");
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM User u WHERE u.user_id = ?");
            preparedStatement.setInt(1, userId);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                if (rs.getInt("curCount") <= 0) {
                    response = Response.builder().message("User with id " + req.get("userId") + " not found.").code(ServerResponseCodes.USER_NOT_FOUND.toString()).body(null).build();
                    return response;
                }
            }
            //negative balance check
            if (Double.parseDouble((String) req.get("amount")) < 0.0)
            {
                response = Response.builder().message("Balance can not be negative!").code(ServerResponseCodes.NEGATIVE_PRICE_ERROR.toString()).body(null).build();
                return response;
            }

            preparedStatement = conn.prepareStatement("SELECT balance FROM User u WHERE u.user_id = ?");
            preparedStatement.setInt(1, userId);
            rs = preparedStatement.executeQuery();
            rs.next();
            double curBalance = rs.getFloat("balance");
            double addedBalance = Double.parseDouble((String) req.get("amount"));
            double newBalance = curBalance + addedBalance;
            preparedStatement = conn.prepareStatement("UPDATE User u SET balance = ? WHERE u.user_id = ?");
            preparedStatement.setDouble(1, newBalance);
            preparedStatement.setInt(2, userId);
            rs = preparedStatement.executeQuery();
            response = Response.builder().message("Successfully added balance.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
            return response;
        } catch (SQLException e) {
            response = Response.builder().message("Cannot add balance due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }
}
