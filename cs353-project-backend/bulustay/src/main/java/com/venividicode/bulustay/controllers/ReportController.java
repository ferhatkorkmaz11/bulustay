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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class ReportController
{

    private DijkstraDBConnectorHelper dijkstraDBConnectorHelper;
    private PasswordHashHandler passwordHashHandler;
    private Connection conn;

    public ReportController()
    {
        dijkstraDBConnectorHelper = new DijkstraDBConnectorHelper();
        passwordHashHandler = new PasswordHashHandler();
        conn = dijkstraDBConnectorHelper.getConn();
    }

    @PostMapping("/createReport")
    public Response createReport(@RequestBody Map<String, Object> req)
    {
        Response response;
        if (req.get("adminId") == null || req.get("month") == null || req.get("year") == null || req.get("reportType") == null || req.get("resultAmount") == null)
        {
            response = Response.builder().message("Missing properties in request object.").code(ServerResponseCodes.INVALID_REQUEST_BODY.toString()).body(null).build();
            return response;
        }

        //Admin exist check
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) as curCount FROM Admin u WHERE u.user_id = ?");
            preparedStatement.setInt(1, (int) req.get("adminId"));
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next())
            {
                if (rs.getInt("curCount") <= 0)
                {
                    response = Response.builder().message("Admin with id " + req.get("userId") + " not found.").code(ServerResponseCodes.USER_NOT_FOUND.toString()).body(null).build();
                    return response;
                }
            }
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot check admin existence.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }

        //result amount negativity check
        if((int) req.get("resultAmount") <= 0)
        {
            response = Response.builder().message("Invalid result amount.").code(ServerResponseCodes.INVALID_AMOUNT.toString()).body(null).build();
        }

        int reportType = (int) req.get("reportType");
        if (reportType == 0) // Most participated events in that month
        {
            try
            {
                PreparedStatement preparedStatement = conn.prepareStatement("WITH Participation(event_id, total) AS\n" +
                        "(SELECT event_id, count(user_id)\n" +
                        "FROM Enroll\n" +
                        "GROUP BY event_id)\n" +
                        "SELECT E.event_id, event_name, total\n" +
                        "FROM Event E, Participation P\n" +
                        "WHERE E.event_id = P.event_id AND MONTH(E.start_date) = ? AND YEAR(E.start_date) = ? ORDER BY total DESC LIMIT ? ;");
                preparedStatement.setInt(1, (int) req.get("month"));
                preparedStatement.setInt(2, (int) req.get("year"));
                preparedStatement.setInt(3, (int) req.get("resultAmount"));
                ResultSet rs = preparedStatement.executeQuery();
                ResultSet rs2 = preparedStatement.executeQuery();
                int count = 1;
                String description = "Most participated events in " + (int) req.get("month") + "-" + (int) req.get("year") + "\n";
                while(rs.next())
                {
                    description = description + count + ") Event ID: " + rs.getInt("event_id") + " | Event Name: " + rs.getString("event_name")+ " | Participation: " + rs.getInt("total") + "\n" ;
                    count++;
                }

                preparedStatement = conn.prepareStatement("INSERT INTO Report(admin_id, description) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setInt(1, (int) req.get("adminId"));
                preparedStatement.setString(2,description);
                preparedStatement.executeQuery();
                ResultSet reportResult = preparedStatement.getGeneratedKeys();
                reportResult.next();
                while (rs2.next())
                {
                    preparedStatement = conn.prepareStatement("INSERT INTO Analysis(report_id, event_id) VALUES(?,?)");
                    preparedStatement.setInt(1, reportResult.getInt("insert_id"));
                    preparedStatement.setInt(2, rs2.getInt("event_id"));
                    preparedStatement.executeQuery();
                }

                response = Response.builder().message("Report successfully created.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
                return response;
            }
            catch (SQLException e)
            {
                response = Response.builder().message("Error while creating report.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
                System.out.println(e.getLocalizedMessage());
                return response;
            }
        }
        else if (reportType == 1) // Total enrollments to each eventType in that month
        {
            try
            {
                PreparedStatement preparedStatement = conn.prepareStatement("WITH Participation(event_id, total) AS\n" +
                        "(SELECT event_id, count(user_id)\n" +
                        "FROM Enroll\n" +
                        "GROUP BY event_id)\n" +
                        "SELECT event_type, sum(total) as total\n" +
                        "FROM Event E, Participation P\n" +
                        "WHERE E.event_id = P.event_id AND MONTH(E.start_date) = ? AND YEAR(E.start_date) = ?\n" +
                        "GROUP BY event_type ORDER BY total DESC;");
                preparedStatement.setInt(1, (int) req.get("month"));
                preparedStatement.setInt(2, (int) req.get("year"));
                ResultSet rs = preparedStatement.executeQuery();
                int count = 1;
                String description = "Most participated event types in " + (int) req.get("month") + "-" + (int) req.get("year") + "\n";
                while(rs.next())
                {
                    description = description + count + ") Event Type: " + rs.getString("event_type")+ " | Participation: " + rs.getInt("total") + "\n" ;
                    count++;
                }

                preparedStatement = conn.prepareStatement("INSERT INTO Report(admin_id, description) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setInt(1, (int) req.get("adminId"));
                preparedStatement.setString(2,description);
                preparedStatement.executeQuery();
                response = Response.builder().message("Report successfully created.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
                return response;
            }
            catch (SQLException e)
            {
                response = Response.builder().message("Error while creating report.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
                System.out.println(e.getLocalizedMessage());
                return response;
            }
        }
        else if (reportType == 2) // Total enrollments to events of each Organizer this month
        {
            try
            {
                PreparedStatement preparedStatement = conn.prepareStatement("WITH Participation(event_id, total) AS\n" +
                        "(SELECT event_id, count(user_id)\n" +
                        "FROM Enroll\n" +
                        "GROUP BY event_id)\n" +
                        "SELECT organizer_id, sum(total) as total\n" +
                        "FROM Event E, Participation P\n" +
                        "WHERE E.event_id = P.event_id AND MONTH(E.start_date) = ? AND YEAR(E.start_date) = ? \n" +
                        "GROUP BY organizer_id ORDER BY total DESC LIMIT ?");
                preparedStatement.setInt(1, (int) req.get("month"));
                preparedStatement.setInt(2, (int) req.get("year"));
                preparedStatement.setInt(3, (int) req.get("resultAmount"));
                ResultSet rs = preparedStatement.executeQuery();
                int count = 1;
                String description = "Organizers who have most participants to their events in " + (int) req.get("month") + "-" + (int) req.get("year") + "\n";
                while(rs.next())
                {
                    description = description + count + ") Organizer ID: " + rs.getInt("organizer_id") + " | Participation: " + rs.getInt("total") + "\n" ;
                    count++;
                }

                preparedStatement = conn.prepareStatement("INSERT INTO Report(admin_id, description) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setInt(1, (int) req.get("adminId"));
                preparedStatement.setString(2,description);
                preparedStatement.executeQuery();
                response = Response.builder().message("Report successfully created.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
                return response;
            }
            catch (SQLException e)
            {
                response = Response.builder().message("Error while creating report.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
                System.out.println(e.getLocalizedMessage());
                return response;
            }
        }
        else
        {
            response = Response.builder().message("Wrong report type id is entered.").code(ServerResponseCodes.WRONG_REPORT_TYPE.toString()).body(null).build();
            return response;
        }
    }

    @GetMapping("/getAllReports")
    public Response getAllReports()
    {
        Response response;
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * From Report ORDER BY report_id DESC");
            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
            while (rs.next())
            {
                HashMap<String, Object> cur = new HashMap<>();
                cur.put("reportId", rs.getInt("report_id"));
                cur.put("adminId", rs.getInt("admin_id"));
                String description = rs.getString("description");
                String title = "";
                int count = 0;
                while(description.charAt(count) != '\n' )
                {
                    title = title + description.charAt(count);
                    count++;
                }
                count++;
                cur.put("title", title);
                cur.put("content", description.substring(count));

                resultBody.add(cur);
            }
            response = Response.builder().message("Reports are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the reports due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    @GetMapping("/getReportsOfEvent")
    public Response getReportsOfEvent(@RequestParam Map<String, Object> req)
    {
        Response response;
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * From Report NATURAL JOIN Analysis WHERE event_id = ? ORDER BY report_id DESC");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("eventId")));
            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<HashMap<String, Object>> resultBody = new ArrayList<>();
            while (rs.next())
            {
                HashMap<String, Object> cur = new HashMap<>();
                cur.put("reportId", rs.getInt("report_id"));
                cur.put("adminId", rs.getInt("admin_id"));
                String description = rs.getString("description");
                String title = "";
                int count = 0;
                while(description.charAt(count) != '\n' )
                {
                    title = title + description.charAt(count);
                    count++;
                }
                count++;
                cur.put("title", title);
                cur.put("content", description.substring(count));

                resultBody.add(cur);
            }
            response = Response.builder().message("Reports are successfully retrieved.").code(ServerResponseCodes.SUCCESS.toString()).body(resultBody).build();
            return response;
        } catch (SQLException e)
        {
            response = Response.builder().message("Cannot get the reports due to database exception.").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
    }

    //DELETE report
    @DeleteMapping("/deleteReport")
    public Response deleteReport(@RequestParam Map<String, Object> req)
    {
        Response response;
        if (req.get("reportId") == null || ((String)req.get("reportId")).equals(""))
        {
            response = Response.builder().message("You should enter an ID.").code(ServerResponseCodes.REPORT_ID_NOT_ENTERED.toString()).body(null).build();
            return response;
        }
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM Report WHERE report_id = ?");
            preparedStatement.setInt(1, Integer.parseInt((String) req.get("reportId")));
            preparedStatement.executeQuery();
        } catch (SQLException e)
        {
            response = Response.builder().message("Something went wrong...").code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).body(null).build();
            System.out.println(e.getLocalizedMessage());
            return response;
        }
        response = Response.builder().message("Report is deleted.").code(ServerResponseCodes.SUCCESS.toString()).body(null).build();
        return response;
    }
}
