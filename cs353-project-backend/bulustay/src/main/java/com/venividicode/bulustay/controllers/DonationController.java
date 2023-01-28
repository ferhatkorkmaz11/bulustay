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
public class DonationController
{
    private DijkstraDBConnectorHelper dijkstraDBConnectorHelper;
    private PasswordHashHandler passwordHashHandler;
    private Connection conn;

    public DonationController()
    {
        dijkstraDBConnectorHelper = new DijkstraDBConnectorHelper();
        passwordHashHandler = new PasswordHashHandler();
        conn = dijkstraDBConnectorHelper.getConn();
    }

    //Create donation
    @PostMapping("/createDonation")
    public Response createDonation(@RequestBody Map<String, Object> req)
    {
        Response response;
        if (req.get("amount") == null || req.get("participantId") == null || req.get("organizerId") == null)
        {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }

        //amount validation
        double amount = Double.parseDouble((String) req.get("amount"));
        if (amount <= 0)
        {
            response = Response.builder().message("Amount can not be zero or negative.").code(ServerResponseCodes.INVALID_AMOUNT.toString()).body(null).build();
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

        //participant existence check
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM Participant WHERE user_id = ?");
            preparedStatement.setInt(1, (int) req.get("participantId"));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("found") == 0)
            {
                response = Response.builder().message("There is no participant with this ID.").code(ServerResponseCodes.PARTICIPANT_ID_NOT_FOUND.toString()).body(null).build();
                return response;
            }
        } catch (SQLException e)
        {
            response = Response.builder().message("Participant existence check error").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        // Participant balance check
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT balance FROM User WHERE user_id = ?");
            preparedStatement.setInt(1, (int) req.get("participantId"));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("balance") < amount)
            {
                response = Response.builder().message("There is not enough balance of participant for this donation.").code(ServerResponseCodes.NOT_ENOUGH_BALANCE.toString()).body(null).build();
                return response;
            }
        } catch (SQLException e)
        {
            response = Response.builder().message("Balance check error").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        //update participant balance
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE User SET balance = balance + ? WHERE user_id = ?");
            preparedStatement.setDouble(1, amount);
            preparedStatement.setInt(2, (int) req.get("organizerId"));
            preparedStatement.executeQuery();
        } catch (SQLException e)
        {
            response = Response.builder().message("Participant balance update error").code(ServerResponseCodes.PARTICIPANT_BALANCE_UPDATE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        //update organizer balance
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE User SET balance = balance - ? WHERE user_id = ?");
            preparedStatement.setDouble(1, amount);
            preparedStatement.setInt(2, (int) req.get("participantId"));
            preparedStatement.executeQuery();
        } catch (SQLException e)
        {
            response = Response.builder().message("Organizer balance update error").code(ServerResponseCodes.ORGANIZER_BALANCE_UPDATE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        //create donation
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO Donation(participant_id, organizer_id, amount) VALUES(?, ?, ?)");
            preparedStatement.setInt(1, (int) req.get("participantId"));
            preparedStatement.setInt(2, (int) req.get("organizerId"));
            preparedStatement.setDouble(3, amount);
            ResultSet rs = preparedStatement.executeQuery();
            response = Response.builder().message("Donation created.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
            return response;

        } catch (SQLException e)
        {
            response = Response.builder().message("Donation creation error").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //Get Donations
    @GetMapping("/getAllDonations")
    public Response getAllDonations()
    {
        Response response;
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM Donation");
            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
            while (rs.next())
            {
                HashMap<String, Object> cur = new HashMap<>();
                cur.put("donationId", rs.getInt("donation_id"));
                cur.put("participantId", rs.getInt("participant_id"));
                cur.put("amount", rs.getFloat("amount"));
                cur.put("organizerId", rs.getInt("organizer_id"));
                resultBody.add(cur);
            }
            response = Response.builder().message("Donations are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the donations due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //Get Donations of organizer
    @GetMapping("/getOrganizerDonations")
    public Response getOrganizerDonations(@RequestParam Map<String, Object> req)
    {
        Response response;
        if (req.get("organizerId") == null || ((String) req.get("organizerId")).equals(""))
        {
            response = Response.builder().message("You can not pass empty organizerId.").code(ServerResponseCodes.ORGANIZER_ID_NOT_ENTERED.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM Donation WHERE organizer_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("organizerId")));
            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
            while (rs.next())
            {
                HashMap<String, Object> cur = new HashMap<>();
                cur.put("donationId", rs.getInt("donation_id"));
                cur.put("participantId", rs.getInt("participant_id"));
                cur.put("amount", rs.getFloat("amount"));
                cur.put("organizerId", rs.getInt("organizer_id"));
                resultBody.add(cur);
            }
            response = Response.builder().message("Donations are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the donations due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }
}
