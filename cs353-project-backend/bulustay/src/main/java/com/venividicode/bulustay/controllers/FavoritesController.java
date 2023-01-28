package com.venividicode.bulustay.controllers;


import com.venividicode.bulustay.Enums.ServerResponseCodes;
import com.venividicode.bulustay.Objects.Response;
import com.venividicode.bulustay.helpers.DijkstraDBConnectorHelper;
import com.venividicode.bulustay.helpers.PasswordHashHandler;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class FavoritesController
{
    private DijkstraDBConnectorHelper dijkstraDBConnectorHelper;
    private Connection conn;

    public FavoritesController()
    {
        dijkstraDBConnectorHelper = new DijkstraDBConnectorHelper();
        conn = dijkstraDBConnectorHelper.getConn();
    }

    //Create Favorites
    @PostMapping("/addFavorites")
    public Response addFavorites(@RequestBody Map<String, Object> req)
    {
        Response response;
        if (req.get("userId") == null || req.get("eventId") == null)
        {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }

        //event existence check
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM Event WHERE event_id = ?");
            preparedStatement.setInt(1, (int) req.get("eventId"));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("found") == 0)
            {
                response = Response.builder().message("There is no event with this ID.").code(ServerResponseCodes.EVENT_ID_NOT_FOUND.toString()).body(null).build();
                return response;
            }
        } catch (SQLException e)
        {
            response = Response.builder().message("Event existence check error").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        //user existence check
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM User WHERE user_id = ?");
            preparedStatement.setInt(1, (int) req.get("userId"));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("found") == 0)
            {
                response = Response.builder().message("There is no User with this ID.").code(ServerResponseCodes.USER_NOT_FOUND.toString()).body(null).build();
                return response;
            }
        } catch (SQLException e)
        {
            response = Response.builder().message("User existence check error").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        // Already Favorites check
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT (*) as found FROM Favorites WHERE user_id = ? AND event_id = ?");
            preparedStatement.setInt(1, (int) req.get("userId"));
            preparedStatement.setInt(2, (int) req.get("eventId"));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("found") != 0)
            {
                response = Response.builder().message("User already favorites Event").code(ServerResponseCodes.ALREADY_FOLLOWS_EXCEPTION.toString()).body(null).build();
                return response;
            }
        } catch (SQLException e)
        {
            response = Response.builder().message("Favorites check error").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        //create Favorites
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO Favorites(user_id, event_id) VALUES(?, ?)");
            preparedStatement.setInt(1, (int) req.get("userId"));
            preparedStatement.setInt(2, (int) req.get("eventId"));
            ResultSet rs = preparedStatement.executeQuery();
            response = Response.builder().message("Favorites created.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
            return response;

        } catch (SQLException e)
        {
            response = Response.builder().message("Favorites creation error").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //Get Favorites
    @GetMapping("/getFavorites")
    public Response getFavorites(@RequestParam Map<String, Object> req)
    {
        Response response;
        if (req.get("userId") == null || ((String) req.get("userId")).equals(""))
        {
            response = Response.builder().message("You can not pass empty userId.").code(ServerResponseCodes.USER_ID_NOT_ENTERED.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM Favorites NATURAL JOIN (SELECT event_id, event_name, city_name, district_name, neighbourhood, street_number, street_name, building, start_date, description, quota, minimum_age, event_type, IFNULL(ticket_price, 0) as ticket_price, refund_policy  FROM Events_for_participant NATURAL LEFT OUTER JOIN PaidEvent) mytable WHERE user_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("userId")));
            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
            while (rs.next())
            {
                HashMap<String, Object> cur = new HashMap<>();
                //cur.put("userId", rs.getInt("user_id"));
                cur.put("eventId", rs.getInt("event_id"));
                cur.put("eventName", rs.getString("event_name"));
                cur.put("city", rs.getString("city_name"));
                cur.put("district", rs.getString("district_name"));
                cur.put("neighbourhood", rs.getString("neighbourhood"));
                cur.put("streetNo", rs.getString("street_number"));
                cur.put("street", rs.getString("street_name"));
                cur.put("building", rs.getString("building"));
                cur.put("startDate", rs.getString("start_date"));
                cur.put("description", rs.getString("description"));
                cur.put("quota", rs.getInt("quota"));
                cur.put("minimumAge", rs.getInt("minimum_age"));
                cur.put("eventType", rs.getString("event_type"));
                cur.put("ticketPrice", rs.getFloat("ticket_price"));
                cur.put("refundPolicy", rs.getInt("refund_policy") == 1 ? true : false);
                resultBody.add(cur);
            }
            response = Response.builder().message("Favorites are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the Favorites due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //Delete Favorites
    @DeleteMapping("/removeFavorites")
    public Response deleteFavorites(@RequestParam Map<String, Object> req)
    {
        Response response;
        if (req.get("userId") == null || req.get("eventId") == null)
        {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM Favorites WHERE user_id = ? AND event_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("userId")));
            preparedStatement.setInt(2, Integer.parseInt((String) req.get("eventId")));
            preparedStatement.executeQuery();
        } catch (SQLException e)
        {
            response = Response.builder().message("Something went wrong...").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        response = Response.builder().message("Event is removed from favorites.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
        return response;
    }
}