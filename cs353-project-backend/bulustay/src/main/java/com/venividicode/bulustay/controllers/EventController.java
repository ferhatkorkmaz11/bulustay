package com.venividicode.bulustay.controllers;


import com.venividicode.bulustay.Enums.ServerResponseCodes;
import com.venividicode.bulustay.Objects.Response;
import com.venividicode.bulustay.helpers.DijkstraDBConnectorHelper;
import com.venividicode.bulustay.helpers.PasswordHashHandler;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.ZoneOffset;

@RestController
@CrossOrigin
public class EventController
{

    private DijkstraDBConnectorHelper dijkstraDBConnectorHelper;
    private PasswordHashHandler passwordHashHandler;
    private Connection conn;

    public EventController()
    {
        dijkstraDBConnectorHelper = new DijkstraDBConnectorHelper();
        passwordHashHandler = new PasswordHashHandler();
        conn = dijkstraDBConnectorHelper.getConn();
    }

    @PostMapping("/createEvent")
    public Response createEvent(@RequestBody Map<String, Object> req)
    {
        Response response;
        if (req.get("eventName") == null || req.get("city") == null || req.get("district") == null ||
                req.get("neighbourhood") == null || req.get("streetNo") == null || req.get("street") == null ||
                req.get("building") == null || req.get("startDate") == null ||
                req.get("description") == null || req.get("quota") == null || req.get("minimumAge") == null ||
                req.get("eventType") == null || req.get("organizerId") == null || req.get("ticketPrice") == null || req.get("refundPolicy") == null)
        {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }

        //negative age check
        if ((int) req.get("minimumAge") < 0)
        {
            response = Response.builder().message("Age can not be negative!").code(ServerResponseCodes.NEGATIVE_AGE_ERROR.toString()).body(null).build();
            return response;
        }
        //negative price check
        if (Double.parseDouble((String) req.get("ticketPrice")) < 0.0)
        {
            response = Response.builder().message("Price can not be negative!").code(ServerResponseCodes.NEGATIVE_PRICE_ERROR.toString()).body(null).build();
            return response;
        }
        //negative quota check
        if ((int) req.get("quota") <= 0)
        {
            response = Response.builder().message("Quota can not be negative!").code(ServerResponseCodes.NEGATIVE_QUOTA_ERROR.toString()).body(null).build();
            return response;
        }

        //Date check
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Istanbul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime eventDateTime = LocalDateTime.parse(((String) req.get("startDate")), formatter);
        if (eventDateTime.isBefore(now)) {
            response = Response.builder().message("Event date has already passed.").code(ServerResponseCodes.EVENT_DATE_PASSED.toString()).body(null).build();
            return response;
        }

        //Organizer table check
        try
        {
            int found = 0;
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM Organizer WHERE user_id = ?");
            preparedStatement.setInt(1, (int) req.get("organizerId"));

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
            {
                if (resultSet.getInt("curCount") >= 1)
                {
                    found = 1;
                }
            }
            if (found == 0)
            {
                try
                {
                    PreparedStatement organizerStatement = conn.prepareStatement("INSERT INTO Organizer(user_id, num_of_followers) VALUES (?, ?)");
                    organizerStatement.setInt(1, (int) req.get("organizerId"));
                    organizerStatement.setInt(2, 0);
                    organizerStatement.executeQuery();
                } catch (SQLException e)
                {
                    response = Response.builder().message("Organizer creation error.").code(ServerResponseCodes.ORGANIZER_EVENT_CREATION_ISSUE.toString()).body(null).build();
                    System.out.println(e.getLocalizedMessage());
                    return response;
                }
            }
        } catch (SQLException e)
        {
            response = Response.builder().message("General database exception").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        //CREATE
        ResultSet rs;
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO Event(event_name, city_name, district_name, neighbourhood," +
                    " street_number, street_name, building, start_date, description, quota , minimum_age, event_type, organizer_id)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, (String) req.get("eventName"));
            preparedStatement.setString(2, (String) req.get("city"));
            preparedStatement.setString(3, (String) req.get("district"));
            preparedStatement.setString(4, (String) req.get("neighbourhood"));
            preparedStatement.setString(5, (String) req.get("streetNo"));
            preparedStatement.setString(6, (String) req.get("street"));
            preparedStatement.setString(7, (String) req.get("building"));
            preparedStatement.setString(8, (String) req.get("startDate"));
            preparedStatement.setString(9, (String) req.get("description"));
            preparedStatement.setInt(10, (int) req.get("quota"));
            preparedStatement.setInt(11, (int) req.get("minimumAge"));
            preparedStatement.setString(12, (String) req.get("eventType"));
            preparedStatement.setInt(13, (int) req.get("organizerId"));
            preparedStatement.executeQuery();
            rs = preparedStatement.getGeneratedKeys();
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot create an event due to database exception during event creation.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        //FREE/PAID EVENT CREATION
        try
        {
            rs.next();
            int id = rs.getInt(1);
            if (Double.parseDouble((String) req.get("ticketPrice")) == 0.0) //FREE
            {
                PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO FreeEvent(event_id) VALUES (?)");
                preparedStatement.setInt(1, id);
                preparedStatement.executeQuery();
            } else //PAID
            {
                PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO PaidEvent(event_id, ticket_price, refund_policy) VALUES (?, ?, ?)");
                preparedStatement.setInt(1, id);
                preparedStatement.setDouble(2, Double.parseDouble((String) req.get("ticketPrice")));
                preparedStatement.setBoolean(3, (boolean) req.get("refundPolicy"));
                preparedStatement.executeQuery();
            }
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot create an event due to database exception during free/paid event creation.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        response = Response.builder().message("Event is successfully created.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
        return response;
    }

    //GET ALL EVENTS
    @GetMapping("/getAllEvents")
    public Response getAllEvents(@RequestParam Map<String, Object> req)
    {
        Response response;
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT event_id, event_name, city_name, district_name, neighbourhood, street_number, street_name, building, start_date, description, quota, minimum_age, event_type, IFNULL(ticket_price, 0) as ticket_price, refund_policy, organizer_id FROM Events_for_participant NATURAL LEFT OUTER JOIN PaidEvent");
            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
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
                if(discountRS.next())
                {
                    cur.put("discountId", discountRS.getInt("discount_id"));
                    cur.put("discountPercentage", discountRS.getInt("discount_percentage"));
                }
                else
                {
                    cur.put("discountId", null);
                    cur.put("discountPercentage", 0);
                }

                PreparedStatement favoriteStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM Favorites WHERE event_id = ? AND user_id = ?");
                favoriteStatement.setInt(1, rs.getInt("event_id"));
                favoriteStatement.setInt(2, Integer.parseInt((String) req.get("userId")));
                ResultSet favoriteRS = favoriteStatement.executeQuery();
                favoriteRS.next();
                if(favoriteRS.getInt("found") == 0)
                {
                    cur.put("isFavorited", false);
                }
                else
                {
                    cur.put("isFavorited", true);
                }

                resultBody.add(cur);
            }
            response = Response.builder().message("Events are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the events due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //GET FILTERED EVENTS
    @GetMapping("/getFilteredEvents")
    public Response getFilteredEvents(@RequestParam Map<String, Object> req)
    {
        int minAge = 0;
        double minPrice = 0.0;
        double maxPrice = 999999999.0;
        String minDate = "0000-00-00";
        String maxDate = "9999-12-12";
        String t = "true";
        boolean onlyAvailable = t.equalsIgnoreCase((String) req.get("onlyAvailable"));

        if (req.get("minAge") != null && !((String)req.get("minAge")).equals(""))
        {
            minAge = Integer.parseInt((String) req.get("minAge"));
        }

        if (req.get("minPrice") != null && !((String)req.get("minPrice")).equals(""))
        {
            minPrice = Double.parseDouble((String) req.get("minPrice"));
            ;
        }

        if (req.get("maxPrice") != null && !((String)req.get("maxPrice")).equals(""))
        {
            maxPrice = Double.parseDouble((String) req.get("maxPrice"));
        }

        if (req.get("minDate") != null && !((String)req.get("minDate")).equals(""))
        {
            minDate = (String) req.get("minDate");
        }

        if (req.get("maxDate") != null && !((String)req.get("maxDate")).equals(""))
        {
            maxDate = (String) req.get("maxDate");
        }

        Response response;
        ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();

        if (req.get("eventType") != null && !((String)req.get("eventType")).equals(""))
        {

            String eventType = (String) req.get("eventType");
            if (req.get("city") != null && !((String)req.get("city")).equals(""))
            {
                String city = (String) req.get("city");
                try
                {
                    PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM (SELECT event_id, event_name, city_name, district_name, neighbourhood, street_number, street_name, building, start_date, description, quota, minimum_age, event_type, IFNULL(ticket_price, 0) as ticket_price, refund_policy, organizer_id FROM Events_for_participant NATURAL LEFT OUTER JOIN PaidEvent) mytable WHERE event_type = ? AND city_name = ? AND minimum_age >= ? AND ticket_price >= ? AND ticket_price <= ?  AND start_date >= ?  AND start_date <= ?");
                    preparedStatement.setString(1, eventType);
                    preparedStatement.setString(2, city);
                    preparedStatement.setInt(3, minAge);
                    preparedStatement.setDouble(4, minPrice);
                    preparedStatement.setDouble(5, maxPrice);
                    preparedStatement.setString(6, minDate);
                    preparedStatement.setString(7, maxDate);
                    ResultSet rs = preparedStatement.executeQuery();
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
                        if(discountRS.next())
                        {
                            cur.put("discountId", discountRS.getInt("discount_id"));
                            cur.put("discountPercentage", discountRS.getInt("discount_percentage"));
                        }
                        else
                        {
                            cur.put("discountId", null);
                            cur.put("discountPercentage", 0);
                        }

                        PreparedStatement favoriteStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM Favorites WHERE event_id = ? AND user_id = ?");
                        favoriteStatement.setInt(1, rs.getInt("event_id"));
                        favoriteStatement.setInt(2, Integer.parseInt((String) req.get("userId")));
                        ResultSet favoriteRS = favoriteStatement.executeQuery();
                        favoriteRS.next();
                        if(favoriteRS.getInt("found") == 0)
                        {
                            cur.put("isFavorited", false);
                        }
                        else
                        {
                            cur.put("isFavorited", true);
                        }
                        resultBody.add(cur);
                    }
                } catch (SQLException e)
                {
                    response = Response.builder().message("Cannot get the events due to database exception (Case 1).").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
                    System.out.println(e.getLocalizedMessage());
                    return response;
                }
            } else
            {
                try
                {
                    PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM (SELECT event_id, event_name, city_name, district_name, neighbourhood, street_number, street_name, building, start_date, description, quota, minimum_age, event_type, IFNULL(ticket_price, 0) as ticket_price, refund_policy  FROM Events_for_participant NATURAL LEFT OUTER JOIN PaidEvent) mytable WHERE event_type = ? AND minimum_age >= ? AND ticket_price >= ? AND ticket_price <= ?  AND start_date >= ?  AND start_date <= ?");
                    preparedStatement.setString(1, eventType);
                    preparedStatement.setInt(2, minAge);
                    preparedStatement.setDouble(3, minPrice);
                    preparedStatement.setDouble(4, maxPrice);
                    preparedStatement.setString(5, minDate);
                    preparedStatement.setString(6, maxDate);
                    ResultSet rs = preparedStatement.executeQuery();
                    while (rs.next())
                    {
                        HashMap<String, Object> cur = new HashMap<>();
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
                } catch (SQLException e)
                {
                    response = Response.builder().message("Cannot get the events due to database exception (Case 2).").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
                    System.out.println(e.getLocalizedMessage());
                    return response;
                }
            }
        } else
        {
            if (req.get("city") != null && !((String)req.get("city")).equals(""))
            {
                String city = (String) req.get("city");
                try
                {
                    PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM (SELECT event_id, event_name, city_name, district_name, neighbourhood, street_number, street_name, building, start_date, description, quota, minimum_age, event_type, IFNULL(ticket_price, 0) as ticket_price, refund_policy  FROM Events_for_participant NATURAL LEFT OUTER JOIN PaidEvent) mytable WHERE city_name = ? AND minimum_age >= ? AND ticket_price >= ? AND ticket_price <= ?  AND start_date >= ?  AND start_date <= ?");
                    preparedStatement.setString(1, city);
                    preparedStatement.setInt(2, minAge);
                    preparedStatement.setDouble(3, minPrice);
                    preparedStatement.setDouble(4, maxPrice);
                    preparedStatement.setString(5, minDate);
                    preparedStatement.setString(6, maxDate);
                    ResultSet rs = preparedStatement.executeQuery();
                    while (rs.next())
                    {
                        HashMap<String, Object> cur = new HashMap<>();
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
                } catch (SQLException e)
                {
                    response = Response.builder().message("Cannot get the events due to database exception (Case 3).").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
                    System.out.println(e.getLocalizedMessage());
                    return response;
                }
            } else
            {
                try
                {
                    PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM (SELECT event_id, event_name, city_name, district_name, neighbourhood, street_number, street_name, building, start_date, description, quota, minimum_age, event_type, IFNULL(ticket_price, 0) as ticket_price, refund_policy  FROM Events_for_participant NATURAL LEFT OUTER JOIN PaidEvent) mytable WHERE minimum_age >= ? AND ticket_price >= ? AND ticket_price <= ?  AND start_date >= ?  AND start_date <= ?");
                    preparedStatement.setInt(1, minAge);
                    preparedStatement.setDouble(2, minPrice);
                    preparedStatement.setDouble(3, maxPrice);
                    preparedStatement.setString(4, minDate);
                    preparedStatement.setString(5, maxDate);
                    ResultSet rs = preparedStatement.executeQuery();
                    while (rs.next())
                    {
                        HashMap<String, Object> cur = new HashMap<>();
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
                } catch (SQLException e)
                {
                    response = Response.builder().message("Cannot get the events due to database exception (Case 4).").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
                    System.out.println(e.getLocalizedMessage());
                    return response;
                }
            }
        }

        //ONLY SHOW AVAILABLE QUOTAS
        if (onlyAvailable)
        {
            for (int i = 0; i < resultBody.size(); i++)
            {
                HashMap<String, Object> res = resultBody.get(i);
                int curID = (int) res.get("eventId");
                try
                {
                    PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curQuota FROM Enroll WHERE event_id = ?");
                    preparedStatement.setInt(1, curID);
                    ResultSet rs = preparedStatement.executeQuery();
                    int curQuota = rs.next() ? rs.getInt("curQuota") : 0;
                    if ((int) res.get("quota") <= curQuota)
                    {
                        resultBody.remove(i);
                        i--;
                    }

                } catch (SQLException e)
                {
                    response = Response.builder().message("Quota availability check error.").code(ServerResponseCodes.QUOTA_CHECK_ERROR.toString()).body(null).build();
                    System.out.println(e.getLocalizedMessage());
                }
            }
        }
        response = Response.builder().message("Events are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
        return response;
    }

    //DELETE EVENT
    @DeleteMapping("/deleteEvent")
    public Response deleteEvent(@RequestParam Map<String, Object> req)
    {
        Response response;
        if (req.get("eventId") == null || ((String)req.get("eventId")).equals(""))
        {
            response = Response.builder().message("You should enter an ID.").code(ServerResponseCodes.EVENT_ID_NOT_ENTERED.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM Event WHERE event_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("eventId")));
            preparedStatement.executeQuery();
        } catch (SQLException e)
        {
            response = Response.builder().message("Something went wrong...").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        response = Response.builder().message("Event is deleted.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
        return response;
    }

    //EDIT EVENT
    @PatchMapping("/editEvent")
    public Response editEvent(@RequestParam Map<String, Object> req)
    {
        Response response;
        int eventId;
        String eventName;
        String city;
        String district;
        String neighbourhood;
        String streetNo;
        String street;
        String building;
        String startDate;
        String description;
        int quota;
        int minimumAge;
        String eventType;
        int organizerId;
        float ticketPrice ;
        boolean refundPolicy;

        if (req.get("eventId") == null || ((String)req.get("eventId")).equals(""))
        {
            response = Response.builder().message("eventIc cannot be null").code(ServerResponseCodes.EVENT_ID_NOT_ENTERED.toString()).body(null).build();
        }

        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM Event WHERE event_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("eventId")));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("found") == 0)
            {
                response = Response.builder().message("There is no event with this ID").code(ServerResponseCodes.EVENT_ID_NOT_FOUND.toString()).body(null).build();
                return response;
            }

        } catch (SQLException e)
        {
            response = Response.builder().message("Something went wrong while checking Event existence").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        ResultSet rs;
        try
        {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Event WHERE event_id = ?");
            ps.setInt(1, Integer.parseInt((String) req.get("eventId")));
            rs = ps.executeQuery();
            rs.next();
            eventId = rs.getInt("event_id");
            eventName = rs.getString("event_name");
            city = rs.getString("city_name");
            district = rs.getString("district_name");
            neighbourhood = rs.getString("neighbourhood");
            streetNo = rs.getString("street_number");
            street = rs.getString("street_name");
            building = rs.getString("building");
            startDate = rs.getString("start_date");
            description = rs.getString("description");
            quota = rs.getInt("quota");
            minimumAge = rs.getInt("minimum_age");
            eventType = rs.getString("event_type");
            organizerId = rs.getInt("organizer_id");
        } catch (SQLException e)
        {
            response = Response.builder().message("Something went wrong while accessing event from Event table").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        if(req.get("eventName") != null && !((String) req.get("eventName")).equals(""))
        {
            eventName = (String) req.get("eventName");
        }

        if(req.get("description") != null && !((String) req.get("description")).equals(""))
        {
            description = (String) req.get("description");
        }

        if(req.get("quota") != null && !((String) req.get("quota")).equals(""))
        {
            quota = Integer.parseInt((String) req.get("quota"));
        }

        //negative quota check
        if (Integer.parseInt((String)req.get("quota")) <= 0)
        {
            response = Response.builder().message("Quota can not be negative!").code(ServerResponseCodes.NEGATIVE_QUOTA_ERROR.toString()).body(null).build();
            return response;
        }

        if(req.get("eventType") != null && !((String) req.get("eventType")).equals(""))
        {
            eventType = (String) req.get("eventType");
        }

        try
        {
            PreparedStatement ps = conn.prepareStatement("UPDATE Event SET event_name = ?, city_name = ?, district_name = ?, " +
                    "neighbourhood = ?, street_number = ?, street_name = ?, building = ?, start_date = ?, description = ?, quota = ?, minimum_age = ?, event_type = ?, organizer_id = ?" +
                    " WHERE event_id = ?");
            ps.setString(1, eventName);
            ps.setString(2, city);
            ps.setString(3, district);
            ps.setString(4, neighbourhood);
            ps.setString(5, streetNo);
            ps.setString(6, street);
            ps.setString(7, building);
            ps.setString(8, startDate);
            ps.setString(9, description);
            ps.setInt(10, quota);
            ps.setInt(11, minimumAge);
            ps.setString(12, eventType);
            ps.setInt(13, organizerId);
            ps.setInt(14, eventId);
            ps.executeQuery();
        } catch (SQLException e)
        {
            response = Response.builder().message("Something went wrong while updating Event").code(ServerResponseCodes.EVENT_UPDATE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM PaidEvent WHERE event_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("eventId")));
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            if (resultSet.getInt("found") == 0)
            {
                response = Response.builder().message("Event is edited.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
                return response;
            }

        } catch (SQLException e)
        {
            response = Response.builder().message("Something went wrong while checking Event existence in PaidEvent").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM PaidEvent WHERE event_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("eventId")));
            ResultSet paidSet = preparedStatement.executeQuery();
            paidSet.next();
            ticketPrice = paidSet.getFloat("ticket_price");
            refundPolicy = paidSet.getBoolean("refund_policy");

            if(req.get("ticketPrice") != null && !((String) req.get("ticketPrice")).equals(""))
            {
                ticketPrice = Float.parseFloat((String)req.get("ticketPrice"));
            }

            if(req.get("refundPolicy") != null && !((String) req.get("refundPolicy")).equals(""))
            {
                refundPolicy = ((String) req.get("refundPolicy")).equalsIgnoreCase("true");

            }
        } catch (SQLException e)
        {
            response = Response.builder().message("Something went wrong while accessing event from PaidEvent table").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        try
        {
            PreparedStatement ps = conn.prepareStatement("UPDATE PaidEvent SET ticket_price = ?, refund_policy = ? WHERE event_id = ?");
            ps.setFloat(1, ticketPrice);
            ps.setBoolean(2, refundPolicy);
            ps.setInt(3, Integer.parseInt((String) req.get("eventId")));
            ps.executeQuery();

        }catch (SQLException e)
        {
            response = Response.builder().message("Something went wrong while updating PaidEvent").code(ServerResponseCodes.EVENT_UPDATE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        response = Response.builder().message("Event is edited.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
        return response;
    }

    //GET ORGANIZER'S EVENTS
    @GetMapping("/getOrganizerEvents")
    public Response getOrganizerEvents(@RequestParam Map<String, Object> req)
    {
        Response response;
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT event_id, event_name, city_name, district_name, neighbourhood, street_number, street_name, building, start_date, description, quota, minimum_age, event_type, IFNULL(ticket_price, 0) as ticket_price, refund_policy, organizer_id FROM (Event NATURAL LEFT OUTER JOIN PaidEvent) WHERE organizer_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("organizerId")));
            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
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
                if(discountRS.next())
                {
                    cur.put("discountId", discountRS.getInt("discount_id"));
                    cur.put("discountPercentage", discountRS.getInt("discount_percentage"));
                }
                else
                {
                    cur.put("discountId", null);
                    cur.put("discountPercentage", 0);
                }

                PreparedStatement favoriteStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM Favorites WHERE event_id = ? AND user_id = ?");
                favoriteStatement.setInt(1, rs.getInt("event_id"));
                favoriteStatement.setInt(2, Integer.parseInt((String) req.get("userId")));
                ResultSet favoriteRS = favoriteStatement.executeQuery();
                favoriteRS.next();
                if(favoriteRS.getInt("found") == 0)
                {
                    cur.put("isFavorited", false);
                }
                else
                {
                    cur.put("isFavorited", true);
                }
                resultBody.add(cur);
            }
            response = Response.builder().message("Organizer's events are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the events of organizer due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //SEARCH EVENTS WITH NAME
    @GetMapping("/getEventsWithName")
    public Response getEventsWithName(@RequestParam Map<String, Object> req)
    {
        Response response;
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT event_id, event_name, city_name, district_name, neighbourhood, street_number, street_name, building, start_date, description, quota, minimum_age, event_type, IFNULL(ticket_price, 0) as ticket_price, refund_policy, organizer_id FROM (Event NATURAL LEFT OUTER JOIN PaidEvent) WHERE event_name LIKE '" + (String) req.get("eventName") + "%'");;
            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
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
                if(discountRS.next())
                {
                    cur.put("discountId", discountRS.getInt("discount_id"));
                    cur.put("discountPercentage", discountRS.getInt("discount_percentage"));
                }
                else
                {
                    cur.put("discountId", null);
                    cur.put("discountPercentage", 0);
                }

                PreparedStatement favoriteStatement = conn.prepareStatement("SELECT COUNT(*) as found FROM Favorites WHERE event_id = ? AND user_id = ?");
                favoriteStatement.setInt(1, rs.getInt("event_id"));
                favoriteStatement.setInt(2, Integer.parseInt((String) req.get("userId")));
                ResultSet favoriteRS = favoriteStatement.executeQuery();
                favoriteRS.next();
                if(favoriteRS.getInt("found") == 0)
                {
                    cur.put("isFavorited", false);
                }
                else
                {
                    cur.put("isFavorited", true);
                }
                resultBody.add(cur);
            }
            response = Response.builder().message("Organizer's events are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the events of organizer due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //GET PARTICIPANT'S EVENTS
    @GetMapping("/getParticipantEvents")
    public Response getParticipantEvents(@RequestParam Map<String, Object> req)
    {
        Response response;
        try
        {
            PreparedStatement enrollStatement = conn.prepareStatement("SELECT * FROM Enroll WHERE user_id = ?");
            enrollStatement.setInt(1, Integer.parseInt((String) req.get("userId")));
            ResultSet enrollSet = enrollStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
            while (enrollSet.next())
            {
                PreparedStatement preparedStatement = conn.prepareStatement("SELECT event_id, event_name, city_name, district_name, neighbourhood, street_number, street_name, building, start_date, description, quota, minimum_age, event_type, IFNULL(ticket_price, 0) as ticket_price, refund_policy, organizer_id FROM (Event NATURAL LEFT OUTER JOIN PaidEvent) WHERE event_id = ?");
                preparedStatement.setInt(1, enrollSet.getInt("event_id"));
                ResultSet rs = preparedStatement.executeQuery();
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
                    resultBody.add(cur);
                }
            }
            response = Response.builder().message("Participant's events are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the events of participant due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //GET Favorite EVENTS
    @GetMapping("/getFavoriteEvents")
    public Response getFavoriteEvents(@RequestParam Map<String, Object> req)
    {
        Response response;
        try
        {
            PreparedStatement favoriteStatement = conn.prepareStatement("SELECT * FROM Favorites WHERE user_id = ?");
            favoriteStatement.setInt(1, Integer.parseInt((String) req.get("userId")));
            ResultSet enrollSet = favoriteStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
            while (enrollSet.next())
            {
                PreparedStatement preparedStatement = conn.prepareStatement("SELECT event_id, event_name, city_name, district_name, neighbourhood, street_number, street_name, building, start_date, description, quota, minimum_age, event_type, IFNULL(ticket_price, 0) as ticket_price, refund_policy, organizer_id FROM (Event NATURAL LEFT OUTER JOIN PaidEvent) WHERE event_id = ?");
                preparedStatement.setInt(1, enrollSet.getInt("event_id"));
                ResultSet rs = preparedStatement.executeQuery();
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

                    resultBody.add(cur);
                }
            }
            response = Response.builder().message("Favorite events are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the favorite events  due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }
}

