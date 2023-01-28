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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class EnrollmentController
{

    private DijkstraDBConnectorHelper dijkstraDBConnectorHelper;
    private PasswordHashHandler passwordHashHandler;
    private Connection conn;



    public EnrollmentController()
    {
        dijkstraDBConnectorHelper = new DijkstraDBConnectorHelper();
        passwordHashHandler = new PasswordHashHandler();
        conn = dijkstraDBConnectorHelper.getConn();
    }

    @PostMapping("/enroll")
    public Response enroll(@RequestBody Map<String, Object> req)
    {
        LocalDateTime eventDateTime = LocalDateTime.MAX;
        Response response;
        if (req.get("userId") == null || req.get("eventId") == null)
        {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM User u WHERE u.user_id = ?");
            preparedStatement.setInt(1, (int) req.get("userId"));
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next())
            {
                if (rs.getInt("curCount") <= 0)
                {
                    response = Response.builder().message("User with id " + req.get("userId") + " not found.").code(ServerResponseCodes.USER_NOT_FOUND.toString()).body(null).build();
                    return response;
                }
            }

            preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM Event e WHERE e.event_id = ?");
            preparedStatement.setInt(1, (int) req.get("eventId"));
            rs = preparedStatement.executeQuery();

            while (rs.next())
            {
                if (rs.getInt("curCount") <= 0)
                {
                    response = Response.builder().message("Event with id " + req.get("eventId") + " not found.").code(ServerResponseCodes.EVENT_ID_NOT_FOUND.toString()).body(null).build();
                    return response;
                }
            }

            preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM Enroll e WHERE e.event_id = ? AND e.user_id = ?");
            preparedStatement.setInt(1, (int) req.get("eventId"));
            preparedStatement.setInt(2, (int) req.get("userId"));
            rs = preparedStatement.executeQuery();

            while (rs.next())
            {
                if (rs.getInt("curCount") >= 1)
                {
                    response = Response.builder().message("You are already enrolled to the event.").code(ServerResponseCodes.ALREADY_ENROLLED.toString()).body(null).build();
                    return response;
                }
            }

            LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Istanbul"));
            preparedStatement = conn.prepareStatement("SELECT * FROM Event WHERE event_id = ?");
            preparedStatement.setInt(1, (int) req.get("eventId"));
            rs = preparedStatement.executeQuery();
            rs.next();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            eventDateTime = LocalDateTime.parse(rs.getString("start_date"), formatter);
            if (eventDateTime.isBefore(now))
            {
                response = Response.builder().message("Event date has already passed.").code(ServerResponseCodes.EVENT_DATE_PASSED.toString()).body(null).build();
                return response;
            }
            preparedStatement = conn.prepareStatement("INSERT INTO Enroll(user_id, event_id) VALUES (?, ?)");
            preparedStatement.setInt(1, (int) req.get("userId"));
            preparedStatement.setInt(2, (int) req.get("eventId"));
            rs = preparedStatement.executeQuery();
            response = Response.builder().message("Successfully enrolled to the event.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
            return response;
        } catch (SQLException e)
        {
            if (e.getSQLState().equals("19420"))
            {
                response = Response.builder().message("Cannot enroll to your own event.").code(ServerResponseCodes.OWN_EVENT_EXCEPTION.toString()).body(null).build();
                System.out.println(e.getLocalizedMessage());
                return response;

            } else if (e.getSQLState().equals("45000"))
            {
                response = Response.builder().message("The event with Id " + (int) req.get("eventId") + " has not enough quota.").code(ServerResponseCodes.QUOTA_EXCEPTION.toString()).body(null).build();
                System.out.println(e.getLocalizedMessage());
                return response;

            } else if (e.getSQLState().equals("42424"))
            {
                response = Response.builder().message("You are not old enough to enroll to this event.").code(ServerResponseCodes.AGE_EXCEPTION.toString()).body(null).build();
                System.out.println(e.getLocalizedMessage());
                return response;
            } else if (e.getSQLState().equals("19191"))
            {
                response = Response.builder().message("You have another event enrolled at " + eventDateTime.toString()).code(ServerResponseCodes.OVERLAPPING_EXCEPTION.toString()).body(null).build();
                System.out.println(e.getLocalizedMessage());
                return response;

            } else if (e.getSQLState().equals("06060"))
            {
                response = Response.builder().message("You do not have enough balance to enroll to this event").code(ServerResponseCodes.FUKARA_EXCEPTION.toString()).body(null).build();
                System.out.println(e.getLocalizedMessage());
                return response;

            } else
            {
                response = Response.builder().message("General database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
                System.out.println(e.getSQLState());
                System.out.println(e.getLocalizedMessage());
                return response;
            }
        }
    }

    @GetMapping("/getEnrolledEvents")
    public Response getEnrolledEvents(@RequestParam Map<String, Object> req)
    {
        Response response;
        if (req.get("userId") == null || ((String) req.get("userId")).equals(""))
        {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM User u WHERE u.user_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("userId")));
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next())
            {
                if (rs.getInt("curCount") <= 0)
                {
                    response = Response.builder().message("User with id " + req.get("userId") + " not found.").code(ServerResponseCodes.USER_NOT_FOUND.toString()).body(null).build();
                    return response;
                }
            }

            preparedStatement = conn.prepareStatement("SELECT * FROM (Events_for_participant NATURAL LEFT OUTER JOIN PaidEvent NATURAL JOIN Enroll En) WHERE user_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("userId")));
            rs = preparedStatement.executeQuery();
            List<HashMap<String, Object>> responseBody = new ArrayList<>();
            while (rs.next())
            {
                HashMap<String, Object> cur = new HashMap<>();
                cur.put("eventId", rs.getInt("event_id"));
                cur.put("organizerId", rs.getInt("organizer_id"));
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
                PreparedStatement discountStatement = conn.prepareStatement("SELECT discount_id, discount_percentage FROM Discount WHERE event_id = " + rs.getInt("event_id"));
                ResultSet discountRS = discountStatement.executeQuery();
                if (discountRS.next())
                {
                    cur.put("discountId", discountRS.getInt("discount_id"));
                    cur.put("discountPercentage", discountRS.getInt("discount_percentage"));
                } else
                {
                    cur.put("discountId", null);
                    cur.put("discountPercentage", 0);
                }

                PreparedStatement favoriteStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM Favorites WHERE event_id = ? AND user_id = ?");
                favoriteStatement.setInt(1, rs.getInt("event_id"));
                favoriteStatement.setInt(2, Integer.parseInt((String) req.get("userId")));
                ResultSet favoriteRS = favoriteStatement.executeQuery();
                favoriteRS.next();
                if (favoriteRS.getInt("found") == 0)
                {
                    cur.put("isFavorited", false);
                } else
                {
                    cur.put("isFavorited", true);
                }

                responseBody.add(cur);
            }
            response = Response.builder().message("Enrolled events of the user has been successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(responseBody).build();
            return response;

        } catch (SQLException e)
        {
            response = Response.builder().message("General database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;

        }
    }

    //SEE ENROLLED PARTICIPANTS => ORGANIZER MAY NEED
    @GetMapping("/getEnrolledParticipants")
    public Response getEnrolledParticipants(@RequestParam Map<String, Object> req)
    {
        Response response;
        if (req.get("eventId") == null || ((String) req.get("eventId")).equals(""))
        {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM Event e WHERE e.event_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("eventId")));
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next())
            {
                if (rs.getInt("curCount") <= 0)
                {
                    response = Response.builder().message("Event with id " + req.get("eventId") + " not found.").code(ServerResponseCodes.EVENT_ID_NOT_FOUND.toString()).body(null).build();
                    return response;
                }
            }

            preparedStatement = conn.prepareStatement("SELECT * FROM Enrolled_participants_for_organizer e WHERE e.event_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("eventId")));
            rs = preparedStatement.executeQuery();
            List<HashMap<String, Object>> responseBody = new ArrayList<>();
            while (rs.next())
            {
                HashMap<String, Object> cur = new HashMap<>();
                cur.put("name", rs.getString("name"));
                cur.put("email", rs.getString("email"));
                cur.put("birthdate", rs.getString("birthdate"));
                cur.put("phone", rs.getString("phone_number"));
                cur.put("userId", rs.getString("user_id"));
                responseBody.add(cur);
            }
            response = Response.builder().message("Enrollments of the event has been successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(responseBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("General database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

    }

    //CANCEL ENROLLMENT (by participant)
    @DeleteMapping("/cancelEnrollment")
    public Response cancelEnrollment(@RequestParam Map<String, Object> req)
    {
        Response response;
        if (req.get("eventId") == null || ((String) req.get("eventId")).equals(""))
        {
            response = Response.builder().message("You should enter an event ID.").code(ServerResponseCodes.EVENT_ID_NOT_ENTERED.toString()).body(null).build();
            return response;
        }

        if (req.get("userId") == null || ((String) req.get("userId")).equals(""))
        {
            response = Response.builder().message("You should enter an user ID.").code(ServerResponseCodes.USER_ID_NOT_ENTERED.toString()).body(null).build();
            return response;
        }

        try
        {
            //Check if paid, then delete ticket & apply refund policy
            PreparedStatement checkStatement = conn.prepareStatement("SELECT * FROM PaidEvent WHERE event_id = ?");
            checkStatement.setInt(1, Integer.parseInt((String) req.get("eventId")));
            ResultSet rs = checkStatement.executeQuery();
            if(rs.next())
            {
                if(rs.getInt("refund_policy")== 1)
                {
                    checkStatement = conn.prepareStatement("SELECT SUM(ticket_price) AS sum FROM Ticket WHERE event_id = ? AND user_id = ?");
                    checkStatement.setInt(1, Integer.parseInt((String) req.get("eventId")));
                    checkStatement.setInt(2, Integer.parseInt((String) req.get("userId")));
                    ResultSet sumRes = checkStatement.executeQuery();
                    sumRes.next();
                    float sum = sumRes.getFloat("sum");

                    PreparedStatement deleteStatement = conn.prepareStatement("DELETE FROM Ticket WHERE event_id = ? AND user_id = ?");
                    deleteStatement.setInt(1, Integer.parseInt((String) req.get("eventId")));
                    deleteStatement.setInt(2, Integer.parseInt((String) req.get("userId")));
                    deleteStatement.executeQuery();

                    PreparedStatement updateStatement = conn.prepareStatement("UPDATE User SET balance = balance + ? WHERE user_id = ?");
                    updateStatement.setFloat(1, sum);
                    updateStatement.setInt(2, Integer.parseInt((String) req.get("userId")));
                    updateStatement.executeQuery();
                }
            }

            //Delete enrollment
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM Enroll WHERE event_id = ? AND user_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("eventId")));
            preparedStatement.setInt(2, Integer.parseInt((String) req.get("userId")));
            preparedStatement.executeQuery();
        } catch (SQLException e)
        {
            response = Response.builder().message("Something went wrong...").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        response = Response.builder().message("Enrollment is deleted.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
        return response;
    }
}
