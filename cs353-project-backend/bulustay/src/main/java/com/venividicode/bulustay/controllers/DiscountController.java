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
public class DiscountController
{
    private DijkstraDBConnectorHelper dijkstraDBConnectorHelper;
    private PasswordHashHandler passwordHashHandler;
    private Connection conn;

    public DiscountController()
    {
        dijkstraDBConnectorHelper = new DijkstraDBConnectorHelper();
        passwordHashHandler = new PasswordHashHandler();
        conn = dijkstraDBConnectorHelper.getConn();
    }

    //Create discount
    @PostMapping("/createDiscount")
    public Response createDiscount(@RequestBody Map<String, Object> req)
    {
        Response response;
        if (req.get("eventId") == null || req.get("discountPercentage") == null || req.get("organizerId") == null)
        {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }

        //discount existence check
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM Discount WHERE event_id = ?");
            preparedStatement.setInt(1, (int) req.get("eventId"));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("found") != 0)
            {
                response = Response.builder().message("There is already a discount for this event.").code(ServerResponseCodes.DISCOUNT_ALREADY_EXISTS.toString()).body(null).build();
                return response;
            }
        } catch (SQLException e)
        {
            response = Response.builder().message("Discount existence check error").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        // discountPercentage range check
        if ((int) req.get("discountPercentage") <= 0 && (int) req.get("discountPercentage") >= 100)
        {
            response = Response.builder().message("Discount percentage is not in a valid range").code(ServerResponseCodes.DISCOUNT_PERCENTAGE_RANGE_ERROR.toString()).body(null).build();
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

        //organizer existence check
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM Organizer WHERE user_id = ?");
            preparedStatement.setInt(1, (int) req.get("organizerId"));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("found") == 0)
            {
                response = Response.builder().message("There is no organizer with this ID.").code(ServerResponseCodes.ORGANIZER_ID_NOT_FOUND.toString()).body(null).build();
                return response;
            }
        } catch (SQLException e)
        {
            response = Response.builder().message("Organizer existence check error").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        //if it is organizer's event check
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM Event WHERE event_id = ? AND organizer_id = ?");
            preparedStatement.setInt(1, (int) req.get("eventId"));
            preparedStatement.setInt(2, (int) req.get("organizerId"));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("found") == 0)
            {
                response = Response.builder().message("Discount successfully created").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
                return response;
            }
        } catch (SQLException e)
        {
            response = Response.builder().message("Event-Organizer match check error").code(ServerResponseCodes.ORGANIZER_EVENT_MISMATCH.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        //create discount
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO Discount(event_id, discount_id, start_date, discount_percentage, organizer_id) VALUES(?, 1, NOW(), ?, ?)");
            preparedStatement.setInt(1, (int) req.get("eventId"));
            preparedStatement.setInt(2, (int) req.get("discountPercentage"));
            preparedStatement.setInt(3, (int) req.get("organizerId"));
            ResultSet rs = preparedStatement.executeQuery();
            response = Response.builder().message("Discount created.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
            return response;

        } catch (SQLException e)
        {
            response = Response.builder().message("Discount creation error").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //Get discounts
    @GetMapping("/getAllDiscounts")
    public Response getAllDiscounts()
    {
        Response response;
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM Discount");
            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
            while (rs.next())
            {
                HashMap<String, Object> cur = new HashMap<>();
                cur.put("eventId", rs.getInt("event_id"));
                cur.put("discountId", rs.getInt("discount_id"));
                cur.put("startDate", rs.getString("start_date"));
                cur.put("discountPercentage", rs.getInt("discount_percentage"));
                cur.put("organizerId", rs.getInt("organizer_id"));
                resultBody.add(cur);
            }
            response = Response.builder().message("Discounts are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the discounts due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //Get discounts of organizer
    @GetMapping("/getOrganizerDiscounts")
    public Response getOrganizerDiscounts(@RequestParam Map<String, Object> req)
    {
        Response response;
        if(req.get("organizerId") == null || ((String)req.get("organizerId")).equals(""))
        {
            response = Response.builder().message("You can not pass empty organizerId.").code(ServerResponseCodes.ORGANIZER_ID_NOT_ENTERED.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM Discount WHERE organizer_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String)req.get("organizerId")));
            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
            while (rs.next())
            {
                HashMap<String, Object> cur = new HashMap<>();
                cur.put("eventId", rs.getInt("event_id"));
                cur.put("discountId", rs.getInt("discount_id"));
                cur.put("startDate", rs.getString("start_date"));
                cur.put("discountPercentage", rs.getInt("discount_percentage"));
                cur.put("organizerId", rs.getInt("organizer_id"));
                resultBody.add(cur);
            }
            response = Response.builder().message("Discounts are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the discounts due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //Delete discount
    @DeleteMapping("/deleteDiscount")
    public Response deleteDiscount(@RequestParam Map<String, Object> req)
    {
        Response response;
        if (req.get("eventId") == null || ((String)req.get("eventId")).equals(""))
        {
            response = Response.builder().message("You should enter an ID.").code(ServerResponseCodes.EVENT_ID_NOT_ENTERED.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM Discount WHERE event_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("eventId")));
            preparedStatement.executeQuery();
        } catch (SQLException e)
        {
            response = Response.builder().message("Something went wrong...").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        response = Response.builder().message("Discount is deleted.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
        return response;
    }
}
