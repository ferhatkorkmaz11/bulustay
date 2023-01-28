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
public class FollowController
{
    private DijkstraDBConnectorHelper dijkstraDBConnectorHelper;
    private Connection conn;

    public FollowController()
    {
        dijkstraDBConnectorHelper = new DijkstraDBConnectorHelper();
        conn = dijkstraDBConnectorHelper.getConn();
    }

    //Create Follow
    @PostMapping("/follow")
    public Response createFollow(@RequestBody Map<String, Object> req)
    {
        Response response;
        if (req.get("participantId") == null || req.get("organizerId") == null)
        {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
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

        // Already following check
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT (*) as found FROM Follows WHERE participant_id = ? AND organizer_id = ?");
            preparedStatement.setInt(1, (int) req.get("participantId"));
            preparedStatement.setInt(2, (int) req.get("organizerId"));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("found") != 0)
            {
                response = Response.builder().message("Participant already follows Organizer").code(ServerResponseCodes.ALREADY_FOLLOWS_EXCEPTION.toString()).body(null).build();
                return response;
            }
        } catch (SQLException e)
        {
            response = Response.builder().message("Following check error").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        //create Follow
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO Follows(participant_id, organizer_id) VALUES(?, ?)");
            preparedStatement.setInt(1, (int) req.get("participantId"));
            preparedStatement.setInt(2, (int) req.get("organizerId"));
            ResultSet rs = preparedStatement.executeQuery();
            response = Response.builder().message("Follow created.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
            return response;

        } catch (SQLException e)
        {
            response = Response.builder().message("Follow creation error").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //Get Followers
    @GetMapping("/getFollowers")
    public Response getFollowers(@RequestParam Map<String, Object> req)
    {
        Response response;
        if (req.get("organizerId") == null || ((String) req.get("organizerId")).equals(""))
        {
            response = Response.builder().message("You can not pass empty organizerId.").code(ServerResponseCodes.ORGANIZER_ID_NOT_ENTERED.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM Follows WHERE organizer_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("organizerId")));
            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
            while (rs.next())
            {
                HashMap<String, Object> cur = new HashMap<>();
                cur.put("participantId", rs.getInt("participant_id"));
                cur.put("organizerId", rs.getInt("organizer_id"));
                resultBody.add(cur);
            }
            response = Response.builder().message("Followers are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the Followers due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //Get Followed
    @GetMapping("/getFollowed")
    public Response getAllFollowing(@RequestParam Map<String, Object> req)
    {
        Response response;
        if (req.get("participantId") == null || ((String) req.get("participantId")).equals(""))
        {
            response = Response.builder().message("You can not pass empty participantId.").code(ServerResponseCodes.PARTICIPANT_ID_NOT_ENTERED.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM Follows WHERE participant_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("participantId")));
            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
            while (rs.next())
            {
                HashMap<String, Object> cur = new HashMap<>();
                cur.put("participantId", rs.getInt("participant_id"));
                cur.put("organizerId", rs.getInt("organizer_id"));
                resultBody.add(cur);
            }
            response = Response.builder().message("Followed are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the Followed due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //Delete Follow
    @DeleteMapping("/unfollow")
    public Response deleteDiscount(@RequestParam Map<String, Object> req)
    {
        Response response;
        if (req.get("participantId") == null || req.get("organizerId") == null)
        {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM Follows WHERE participant_id = ? AND organizer_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("participantId")));
            preparedStatement.setInt(2, Integer.parseInt((String) req.get("organizerId")));
            preparedStatement.executeQuery();
        } catch (SQLException e)
        {
            response = Response.builder().message("Something went wrong...").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        response = Response.builder().message("Follow is deleted.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
        return response;
    }
}