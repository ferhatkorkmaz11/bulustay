package com.venividicode.bulustay.controllers;

import com.venividicode.bulustay.Enums.ServerResponseCodes;
import com.venividicode.bulustay.Objects.Response;
import com.venividicode.bulustay.helpers.DijkstraDBConnectorHelper;
import com.venividicode.bulustay.helpers.PasswordHashHandler;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

@RestController
@CrossOrigin
public class InitializerController {
    private DijkstraDBConnectorHelper dijkstraDBConnectorHelper;
    private Connection conn;

    public InitializerController() {
        dijkstraDBConnectorHelper = new DijkstraDBConnectorHelper();
        conn = dijkstraDBConnectorHelper.getConn();
    }

    @PostMapping("/initializeDB")
    public Response initializeDB() {
        Response response;
        try {
            initializeTables();
            initializeViews();
            initializeTriggers();


        } catch (SQLException e) {
            response = Response.builder().body(null).message("Failed with message " + e.getLocalizedMessage()).code(ServerResponseCodes.GENERAL_DATABASE_EXCEPTION.toString()).build();
            return response;
        }
        response = Response.builder().body(null).message("Everything is fine").code(ServerResponseCodes.SUCCESS.toString()).build();
        return response;

    }

    private void initializeTriggers() throws SQLException {
        String[] triggerNames = new String[]{"increase_num_follower", "decrease_num_follower", "discount_trigger", "user_initialization", "user_event_join_check", "refund_ticket_during_deletion", "discount_trigger_reverse"};
        String[] triggers = new String[]{"CREATE TRIGGER increase_num_follower\n" +
                "AFTER INSERT ON Follows\n" +
                "FOR EACH ROW\n" +
                "BEGIN\n" +
                "    UPDATE Organizer\n" +
                "    SET num_of_followers = num_of_followers + 1\n" +
                "    WHERE Organizer.user_id = NEW.organizer_id;\n" +
                "END;",
                "CREATE TRIGGER decrease_num_follower\n" +
                        "AFTER DELETE ON Follows\n" +
                        "\n" +
                        "FOR EACH ROW\n" +
                        "BEGIN\n" +
                        "    UPDATE Organizer\n" +
                        "    SET num_of_followers = num_of_followers - 1\n" +
                        "    WHERE Organizer.user_id = OLD.organizer_id;\n" +
                        "END;",
                "CREATE TRIGGER discount_trigger\n" +
                        "AFTER INSERT ON Discount\n" +
                        " \n" +
                        "FOR EACH ROW\n" +
                        "BEGIN\n" +
                        "   UPDATE PaidEvent\n" +
                        "   SET ticket_price = (ticket_price / 100) * (100 - NEW.discount_percentage)\n" +
                        "   WHERE NEW.event_id = PaidEvent.event_id;\n" +
                        "END;\n\n",
                "CREATE TRIGGER user_initialization\n" +
                        "AFTER INSERT ON User\n" +
                        "FOR EACH ROW\n" +
                        "BEGIN\n" +
                        "  INSERT INTO Participant (user_id) VALUES (NEW.user_id);\n" +
                        "END;",
                "CREATE TRIGGER user_event_join_check\n" +
                        "    BEFORE INSERT\n" +
                        "    ON Enroll\n" +
                        "    FOR EACH ROW\n" +
                        "BEGIN\n" +
                        "    DECLARE quota INT;\n" +
                        "    DECLARE maxQuota INT;\n" +
                        "    DECLARE ageLimit INT;\n" +
                        "    DECLARE age INT;\n" +
                        "    DECLARE overlapping INT;\n" +
                        "    DECLARE eventPrice FLOAT;\n" +
                        "    DECLARE isPaidCount INT;\n" +
                        "    DECLARE userBalance FLOAT;\n" +
                        "    DECLARE ticketCount INT;\n" +
                        "    DECLARE joinStart DATETIME;\n" +
                        "    DECLARE organizatorkimligi INT;\n" +
                        "\n" +
                        "    SELECT minimum_age\n" +
                        "    INTO ageLimit\n" +
                        "    FROM Event E\n" +
                        "    WHERE E.event_id = NEW.event_id;\n" +
                        "\n" +
                        "    SELECT COUNT(*)\n" +
                        "    INTO quota\n" +
                        "    FROM Enroll E\n" +
                        "    WHERE E.event_id = NEW.event_id;\n" +
                        "\n" +
                        "    SELECT ev.quota\n" +
                        "    INTO maxQuota\n" +
                        "    FROM Event ev\n" +
                        "    WHERE ev.event_id = NEW.event_id;\n" +
                        "\n" +
                        "    SELECT ev.start_date\n" +
                        "    INTO joinStart\n" +
                        "    FROM Event ev\n" +
                        "    WHERE ev.event_id = NEW.event_id;\n" +
                        "\n" +
                        "    WITH user_joined(event_id) as (SELECT e.event_id\n" +
                        "                                   FROM Event e,\n" +
                        "                                        Enroll en\n" +
                        "                                   WHERE e.event_id = en.event_id\n" +
                        "                                     AND en.user_id = NEW.user_id)\n" +
                        "    SELECT COUNT(*)\n" +
                        "    INTO overlapping\n" +
                        "    FROM Event e\n" +
                        "             NATURAL JOIN user_joined u\n" +
                        "    WHERE (e.start_date = joinStart);\n" +
                        "\n" +
                        "    SELECT TIMESTAMPDIFF(YEAR, birthdate, curdate())\n" +
                        "    INTO age\n" +
                        "    from User\n" +
                        "    WHERE user_id = NEW.user_id;\n" +
                        "\n" +
                        "    SELECT COUNT(*)\n" +
                        "    INTO isPaidCount\n" +
                        "    FROM PaidEvent pe\n" +
                        "    WHERE (pe.event_id = NEW.event_id);\n" +
                        "\n" +
                        "    SELECT COUNT(*)\n" +
                        "    INTO ticketCount\n" +
                        "    FROM Ticket t\n" +
                        "    WHERE (t.user_id = NEW.user_id);\n" +
                        "\n" +
                        "    SELECT balance\n" +
                        "    INTO userBalance\n" +
                        "    FROM User u\n" +
                        "    WHERE (u.user_id = NEW.user_id);\n" +
                        "\n" +
                        "    SELECT ticket_price\n" +
                        "    INTO eventPrice\n" +
                        "    FROM PaidEvent pe\n" +
                        "    WHERE (pe.event_id = NEW.event_id);\n" +
                        "    \n" +
                        "    SELECT organizer_id\n" +
                        "    INTO organizatorkimligi\n" +
                        "    FROM Event e\n" +
                        "    WHERE (e.event_id = NEW.event_id);\n" +
                        "    \n" +
                        "    IF NEW.user_id = organizatorkimligi\n" +
                        "    THEN\n" +
                        "        signal sqlstate '19420' set message_text = 'OWN_EVENT_EXCEPTION';\n" +
                        "    END IF;\n" +
                        "    \n" +
                        "    IF quota >= maxQuota\n" +
                        "    THEN\n" +
                        "        signal sqlstate '45000' set message_text = 'QUOTA_EXCEPTION';\n" +
                        "    END IF;\n" +
                        "    IF ageLimit > age\n" +
                        "    THEN\n" +
                        "        signal sqlstate '42424' set message_text = 'AGE_EXCEPTION';\n" +
                        "    END IF;\n" +
                        "    IF overlapping > 0\n" +
                        "    THEN\n" +
                        "        signal sqlstate '19191' set message_text = 'OVERLAPPING_EXCEPTION';\n" +
                        "    END IF;\n" +
                        "    IF isPaidCount > 0\n" +
                        "    THEN\n" +
                        "        IF eventPrice <= userBalance\n" +
                        "        THEN\n" +
                        "            INSERT INTO Ticket(USER_ID, TICKET_ID, TICKET_PRICE, STATUS, EVENT_ID)\n" +
                        "            VALUES (NEW.user_id, ticketCount + 1, eventPrice, 'APPROVED', NEW.event_id);\n" +
                        "            UPDATE User SET balance = userBalance - eventPrice WHERE user_id = NEW.user_id;\n" +
                        "        ELSE\n" +
                        "            signal sqlstate '06060' set message_text = 'BALANCE_EXCEPTION';\n" +
                        "        END IF;\n" +
                        "    END IF;\n" +
                        "END\n" +
                        "\n",
                "CREATE TRIGGER refund_ticket_during_deletion\n" +
                        "BEFORE DELETE ON Event\n" +
                        "FOR EACH ROW\n" +
                        "BEGIN\n" +
                        " IF OLD.start_date >= curdate()\n" +
                        " THEN\n" +
                        "   UPDATE User\n" +
                        "   SET balance = balance + (SELECT ticket_price from Ticket WHERE User.user_id = Ticket.user_id AND Ticket.event_id = OLD.event_id)\n" +
                        "   WHERE user_id in (SELECT user_id FROM Enroll WHERE event_id = OLD.event_id);\n" +
                        " END IF;\n" +
                        "END;\n",
                "CREATE TRIGGER discount_trigger_reverse\n" +
                        "BEFORE DELETE ON Discount\n" +
                        " \n" +
                        "FOR EACH ROW\n" +
                        "BEGIN\n" +
                        "  UPDATE PaidEvent\n" +
                        "  SET ticket_price = (ticket_price / (100 - OLD.discount_percentage)) * 100\n" +
                        "  WHERE OLD.event_id = PaidEvent.event_id;\n" +
                        "END;\n"};

        for (String curTrigger : triggerNames) {
            conn.prepareStatement("DROP TRIGGER IF EXISTS " + curTrigger).executeQuery();
        }

        for (String curTrigger : triggers) {
            System.out.println(">>" + curTrigger);
            conn.prepareStatement(curTrigger).executeQuery();
        }

    }

    private void initializeTables() throws SQLException {
        String[] tableNames = new String[]{"BaseUser", "User", "Admin", "Participant", "Organizer", "Event", "PaidEvent", "Ticket", "Discount", "Donation", "Complaint", "Report", "Enroll", "Follows", "Favorites", "Rating", "Ban", "Analysis"};
        String[] tables = new String[]{"CREATE TABLE BaseUser(\n" +
                "    user_id int NOT NULL AUTO_INCREMENT,\n" +
                "    name varchar(255) NOT NULL,\n" +
                "    hashed_password varchar(255) NOT NULL,\n" +
                "    password_salt varchar(32) NOT NULL,\n" +
                "    email varchar(255) NOT NULL,\n" +
                "    PRIMARY KEY (user_id),\n" +
                "    CONSTRAINT un_email UNIQUE (email));",
                "CREATE TABLE User(\n" +
                        "    user_id int,\n" +
                        "    birthdate date,\n" +
                        "    city_name varchar(255),\n" +
                        "    district_name varchar(255),\n" +
                        "    neighbourhood varchar(255),\n" +
                        "    street_number varchar(255),\n" +
                        "    street_name varchar(255),\n" +
                        "    building varchar(255),\n" +
                        "    phone_number varchar(255),\n" +
                        "    gender varchar(255),\n" +
                        "    balance float,\n" +
                        "    FOREIGN KEY (user_id) REFERENCES BaseUser(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (user_id));",
                "CREATE TABLE Admin(\n" +
                        "    user_id int,\n" +
                        "    FOREIGN KEY (user_id) REFERENCES BaseUser(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (user_id));",
                "CREATE TABLE Participant(\n" +
                        "    user_id int,\n" +
                        "    FOREIGN KEY (user_id) REFERENCES User(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (user_id));",
                "CREATE TABLE Organizer(\n" +
                        "    user_id int,\n" +
                        "    num_of_followers int,\n" +
                        "    FOREIGN KEY (user_id) REFERENCES User(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (user_id));",
                "CREATE TABLE Event(\n" +
                        "    event_id int NOT NULL AUTO_INCREMENT,\n" +
                        "    event_name varchar(255) NOT NULL,\n" +
                        "    city_name varchar(255),\n" +
                        "    district_name varchar(255),\n" +
                        "    neighbourhood varchar(255),\n" +
                        "    street_number varchar(255),\n" +
                        "    street_name varchar(255),\n" +
                        "    building varchar(255),\n" +
                        "    start_date datetime,\n" +
                        "    description varchar(255),\n" +
                        "    quota int,\n" +
                        "    minimum_age int,\n" +
                        "    event_type varchar(255),\n" +
                        "    organizer_id int,\n" +
                        "    FOREIGN KEY (organizer_id) REFERENCES Organizer(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (event_id))",
                "CREATE TABLE PaidEvent(\n" +
                        "    event_id int,\n" +
                        "    ticket_price float,\n" +
                        "    refund_policy boolean,\n" +
                        "    FOREIGN KEY (event_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (event_id));",
                "CREATE TABLE Ticket(\n" +
                        "    user_id int,\n" +
                        "    ticket_id int NOT NULL,\n" +
                        "    ticket_price float NOT NULL,\n" +
                        "    status varchar(255),\n" +
                        "    event_id int,\n" +
                        "    FOREIGN KEY (user_id) REFERENCES Participant(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (event_id) REFERENCES PaidEvent(event_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (user_id, ticket_id));",
                "CREATE TABLE Discount(\n" +
                        "    event_id int,\n" +
                        "    discount_id int,\n" +
                        "    start_date date,\n" +
                        "    discount_percentage int,\n" +
                        "    organizer_id int,\n" +
                        "    FOREIGN KEY (event_id) REFERENCES PaidEvent(event_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (organizer_id) REFERENCES Organizer(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (event_id, discount_id))",
                "CREATE TABLE Donation(\n" +
                        "    donation_id int NOT NULL AUTO_INCREMENT,\n" +
                        "    amount float,\n" +
                        "    organizer_id int,\n" +
                        "    participant_id int,\n" +
                        "    FOREIGN KEY (participant_id) REFERENCES Participant(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (organizer_id) REFERENCES Organizer(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (donation_id));",
                "CREATE TABLE Report(\n" +
                        "    report_id int NOT NULL AUTO_INCREMENT,\n" +
                        "    description varchar(7999),\n" +
                        "    start_date date,\n" +
                        "    end_date date,\n" +
                        "    admin_id int,\n" +
                        "    FOREIGN KEY (admin_id) REFERENCES Admin(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (report_id));",
                "CREATE TABLE Enroll(\n" +
                        "    event_id int,\n" +
                        "    user_id int,\n" +
                        "    FOREIGN KEY (event_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (user_id) REFERENCES Participant(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (event_id, user_id));",
                "CREATE TABLE Follows(\n" +
                        "    participant_id int,\n" +
                        "    organizer_id int,\n" +
                        "    FOREIGN KEY (participant_id) REFERENCES Participant(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (organizer_id) REFERENCES Organizer(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (participant_id, organizer_id));",
                "CREATE TABLE Favorites(\n" +
                        "    user_id int,\n" +
                        "    event_id int,\n" +
                        "    FOREIGN KEY (event_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (user_id) REFERENCES Participant(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (user_id, event_id));",
                "CREATE TABLE Ban(\n" +
                        "    admin_id int,\n" +
                        "    user_id int,\n" +
                        "    FOREIGN KEY (admin_id) REFERENCES Admin(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (user_id) REFERENCES User(user_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (user_id));",
                "CREATE TABLE Analysis(\n" +
                        "    report_id int,\n" +
                        "    event_id int,\n" +
                        "    FOREIGN KEY (report_id) REFERENCES Report(report_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (event_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                        "    PRIMARY KEY (report_id, event_id));"};


        conn.prepareStatement("DROP DATABASE IF EXISTS ferhat_korkmaz").executeQuery();
        conn.prepareStatement("CREATE DATABASE ferhat_korkmaz CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci").executeQuery();
        dijkstraDBConnectorHelper = new DijkstraDBConnectorHelper();
        conn = dijkstraDBConnectorHelper.getConn();

        for (String curTable : tables) {
            System.out.println(">>" + curTable);
            conn.prepareStatement(curTable).executeQuery();
        }

    }

    private void initializeViews() throws SQLException {
        String[] viewNames = new String[]{"Enrolled_participants_for_organizer", "Events_for_participant", "Organizers_for_participant"};
        String[] views = new String[]{"CREATE VIEW Enrolled_participants_for_organizer AS\n" +
                "SELECT b.name, b.email, u.birthdate, u.phone_number, u.user_id, e.event_id\n" +
                "FROM BaseUser b, User u, Enroll e\n" +
                "WHERE b.user_id = u.user_id and u.user_id = e.user_id",
                "CREATE VIEW Events_for_participant AS\n" +
                        "SELECT event_name, organizer_id, city_name, district_name, neighbourhood, street_number, street_name, building, start_date, description, quota, minimum_age, event_type, event_id\n" +
                        "FROM Event;",
                "CREATE VIEW Organizers_for_participant AS\n" +
                        "SELECT DISTINCT b.name as username,  num_of_followers, event_name,  city_name, district_name, neighbourhood, street_number, street_name, building, start_date, description, quota, minimum_age, event_type\n" +
                        "FROM Organizer o, BaseUser b, Event e\n" +
                        "WHERE o.user_id = b.user_id AND e.organizer_id = o.user_id;"};

        for (String curView : viewNames) {
            conn.prepareStatement("DROP VIEW IF EXISTS " + curView).executeQuery();
        }

        for (String curView : views) {
            System.out.println(">>" + curView);
            conn.prepareStatement(curView).executeQuery();
        }

    }
}
