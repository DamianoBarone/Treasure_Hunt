/*
     * To change this license header, choose License Headers in Project Properties.
     * To change this template file, choose Tools | Templates
     * and open the template in the editor.
 */
package code;

import com.mysql.jdbc.Connection;
import java.io.BufferedReader;
import java.time.Instant;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletInputStream;
import java.io.OutputStreamWriter;
import java.sql.Blob;
import java.util.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Damiano
 */
@WebServlet(name = "Servlet", urlPatterns = {"/Servlet"})
public class Servlet extends HttpServlet {

    private static boolean x = true;
    String idUser = null;
    Connection conn = null;
    Statement stmt = null;

    //  Database credentials
    String USER = "root";
    String PASS = "password";

    // JDBC driver name and database URL
    String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    String DB_URL = "jdbc:mysql://localhost:3306/mydb_treasure";

    double lenPasso = 0.3; // K meters
    double kmToLat = 0.009, kmToLon = 0.011; //0.54/Math.cos(Math.toRadians(center.lat));
    //kmToLon=kmToLon/1.852;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Servlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Servlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String start_date, end_date;

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            conn = (Connection) DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            String sql = "SELECT * FROM Event where now() < date_start";
            ResultSet res = stmt.executeQuery(sql);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */

            // Open a connection                    
            //    conn = (Connection) DriverManager.getConnection(DB_URL, USER, PASS);
            //  stmt = conn.createStatement();
            if (request.getParameter("city") != null) {
                //add event
                //2016-01-01 00:00:00
                start_date = request.getParameter("start_data").substring(6, 10) + "-" + request.getParameter("start_data").substring(0, 2) + "-" + request.getParameter("start_data").substring(3, 5) + ' ';
                start_date += request.getParameter("start_hour").substring(0, 5) + ":00";
                end_date = request.getParameter("end_data").substring(6, 10) + "-" + request.getParameter("end_data").substring(0, 2) + "-" + request.getParameter("end_data").substring(3, 5) + ' ';
                end_date += request.getParameter("end_hour").substring(0, 5) + ":00";

                String sql = "INSERT INTO `mydb_treasure`.`Event`(`date_start`, `date_end`, `name_event`,  `City_name`, `number_step`) VALUES ('" + start_date + "', '" + end_date + "', '" + request.getParameter("name_event") + "', '" + request.getParameter("city") + "', '" + request.getParameter("number_step") + "')";
                stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                ResultSet newIdEvent = stmt.getGeneratedKeys();
                String IdEvent;
                if (newIdEvent.next()) {
                    IdEvent = newIdEvent.getString(1);
                    System.out.println(IdEvent);
                    //Timer t = new Timer();
                    //t.schedule(new StartEvent(IdEvent), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(start_date));
                    out.println("<!DOCTYPE html>");
                    out.println("<html>");
                    out.println("<head> <meta http-equiv='refresh' content='3; url=http://localhost:8080/Treasure_server/' >");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("Event added!!");
                    out.println("</body>");
                    out.println("</html>");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*catch (ParseException ex) {
            Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            conn = (Connection) DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            String sql = "SELECT * FROM Event where now() < date_start";
            ResultSet res = stmt.executeQuery(sql);
            // Register JDBC driver
            try (PrintWriter out = response.getWriter()) {

                BufferedReader reader = request.getReader();

                String firstline = null;
                firstline = reader.readLine();
                if (firstline != null) {
                    System.out.println(firstline);

                    switch (firstline) {
                        //login
                        case "{'message_type':'1',":
                            login(reader, response, stmt, out);
                            break;
                        case "{'message_type':'2',":
                            register(reader, response, stmt, out);
                            System.out.println("register");

                            break;
                        case "{'message_type':'3',":
                            getList(reader, response, stmt, out);
                            System.out.println("getList");
                            break;
                        case "{'message_type':'4',":
                            AddedUserEvent(reader, response, stmt, out);
                            System.out.println("add in the match");
                            break;
                        case "{'message_type':'5',"://LOLLO
                            send_image(reader, response, stmt, out);
                            System.out.println("fine send image");
                            break;
                        case "{'message_type':'6',":
                            getStartData(reader, response, stmt, out);
                            System.out.println("getStartData");
                            break;
                        case "{'message_type':'7',":
                            getTipsData(reader, response, stmt, out);
                            System.out.println("getTipsData");
                            break;
                        case "{'message_type':'8',":
                            userFoundTreasure(reader, response, stmt, out);
                            System.out.println("userFoundTreasure");
                            break;
                        case "{'message_type':'9',":
                            timeExceeded(reader, response, stmt, out);
                            System.out.println("userFoundTreasure");
                            break;
                        case "{'message_type':'10',":
                            matchResume(reader, response, stmt, out);
                            System.out.println("userFoundTreasure");
                            break;
                        default:
                            System.out.println("non capisco il mex");
                    }

                }

            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>
// escape character ";"// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>
// escape character ";"// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>
// escape character ";"// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>// </editor-fold>
// escape character ";"

    private void getList(BufferedReader reader, HttpServletResponse response, Statement stmt, PrintWriter out) throws IOException, SQLException {
        // select only events that user had not jet completed
        String resp = "", query = "Select * from User where idUser= " + idUser;
        ResultSet res = stmt.executeQuery(query);
        res = stmt.executeQuery(query);
        if (res.next()) {
            resp += res.getString("name") + "," + res.getString("points") + ";";
        }
        System.out.println("titolo " + resp);

        query = "SELECT * FROM event join city where event.city_name=city.name and now() < Event.date_end and idEvent not in (select Event_idEvent FROM mydb_treasure.event_has_user where User_idUser='" + idUser + "' and NOT rank<=>null) order by date_Start"; // old query "SELECT * FROM event join city where event.city_name=city.name";
        res = stmt.executeQuery(query);

        while (res.next()) {
            resp += res.getString("name_event") + "," + res.getString("City_name") + "," + res.getString("date_start") + "," + res.getString("date_end") + "," + res.getString("latitude") + "," + res.getString("longitude") + "," + res.getString("idEvent") + ";";
        }

        if (resp.length() == 0) {
            System.out.println("no events in db");
            resp = "...";
        }

        System.out.println(resp); // ***

        response.setContentType("text/plain");
        response.setContentLength(resp.length());
        PrintWriter reply = response.getWriter();
        reply.println(resp);
        out.close(); //non so se serve out
        out.flush();
    }

    private void login(BufferedReader reader, HttpServletResponse response, Statement stmt1, PrintWriter out) throws IOException, SQLException, ServletException {
        System.out.println("login");
        try {
            if (x) {
                init();
                x = false;
            }
        } catch (ServletException ex) {
            Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        ResultSet rs;
        String line = null;
        String email = null;
        String password = null;
        String message = null;

        String sql;
        line = reader.readLine();
        email = line.substring(10, line.length() - 2);
        line = reader.readLine();
        password = line.substring(13, line.length() - 2);
        sql = "SELECT  * FROM User WHERE email='" + email + "' AND password='" + password + "'";
        rs = stmt.executeQuery(sql);
        if (!rs.next()) {
            System.out.println("empty");
            message = "you are not in the db";
        } else {
            idUser = rs.getString("idUser");
            message = "ok login";
        }

        response.setContentType("text/plain");
        response.setContentLength(message.length());
        PrintWriter reply = response.getWriter();
        reply.println(message);
        out.close(); //non so se serve out
        out.flush();

    }

    private void register(BufferedReader reader, HttpServletResponse response, Statement stmt, PrintWriter out) throws IOException, SQLException {
        ResultSet rs;
        System.out.println("register");

        String line = null;
        String email = null;
        String password = null;
        String message = null;

        String sql;
        line = reader.readLine();
        email = line.substring(10, line.length() - 2);
        line = reader.readLine();
        password = line.substring(13, line.length() - 2);
        line = reader.readLine();
        String name = line.substring(9, line.length() - 2);
        line = reader.readLine();
        String surname = line.substring(12, line.length() - 2);
        sql = "INSERT INTO `mydb_treasure`.`User`(`name`, `surname`, `points`,  `email`, `password`) VALUES ('" + name + "', '" + surname + "', '" + "0" + "', '" + email + "', '" + password + "')";
        stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet newIdUser = stmt.getGeneratedKeys();
        if (newIdUser.next()) {
            idUser = newIdUser.getString(1);
            System.out.println(idUser);

        } else {
            ; // exception!!! ***
        }
        message = "ok register";
        response.setContentType("text/plain");
        response.setContentLength(message.length());
        PrintWriter reply = response.getWriter();
        reply.println(message);
        out.close(); //non so se serve out
        out.flush();

    }

    private void AddedUserEvent(BufferedReader reader, HttpServletResponse response, Statement stmt, PrintWriter out) throws IOException, SQLException {
        System.out.println("addUserEvent inizio");
        String sql, idEvent;
        ResultSet rs;
        String line = reader.readLine();
        idEvent = line.substring(12, line.length() - 2);
        sql = "SELECT  * FROM Event_has_User WHERE Event_idEvent='" + idEvent + "' AND User_idUser='" + idUser + "'";
        rs = stmt.executeQuery(sql);
        if (!rs.next()) {//we have to add the user in the match
            System.out.println("user added");
            sql = "INSERT INTO `mydb_treasure`.`Event_has_User`(`User_idUser`, `Event_idEvent`) VALUES ('" + idUser + "', '" + idEvent + "')";
            stmt.executeUpdate(sql);

        }

        String query = "SELECT * FROM event where event.idEvent='" + idEvent + "'";
        ResultSet res = stmt.executeQuery(query);

        if (!res.next()) {
            System.out.println("evend id doesn't exist: " + idEvent);
            return;
        }

        String city = res.getString("City_name");
        String start_lat = res.getString("start_lat");

        query = "SELECT * FROM city where name='" + city + "'";
        res = stmt.executeQuery(query);

        if (!res.next()) {
            System.out.println("evend id doesn't exist: " + idEvent);
            return;
        }

        if (start_lat.compareTo("0") == 0) {
            System.out.println("create start point");
            Location start = new Location(), center = new Location(Double.parseDouble(res.getString("latitude")), Double.parseDouble(res.getString("longitude")));
            double d = Double.parseDouble(res.getString("diameter"));

            double randLat = (Math.random() * d) - (d / 2);
            double randLon = (Math.random() * d) - (d / 2);

            start.lat = center.lat + randLat * kmToLat;
            start.lon = center.lon + randLon * kmToLon;

            query = "UPDATE `mydb_treasure`.`event` SET `start_lat`='" + start.lat + "', `start_lon`='" + start.lon + "' WHERE `idEvent`='" + idEvent + "'";
            stmt.executeUpdate(query);
        }

        String message = "ok added user in the event";
        response.setContentType("text/plain");
        response.setContentLength(message.length());
        PrintWriter reply = response.getWriter();
        reply.println(message);
        out.close(); //non so se serve out
        out.flush();

    }

    private void send_image(BufferedReader reader, HttpServletResponse response, Statement stmt, PrintWriter out) throws SQLException, IOException {
        String line = reader.readLine();
        String idStep = line.substring(12, line.length() - 2);

        Blob test = null;
        byte[] bytesEncoded;
        String sql;
        if (idStep.compareTo("'-1") == 0) // treasure found!
        {
            sql = "select answer, correct, image, question from question join answer where question.idQuestion=answer.Question_idQuestion and question.idQuestion='6' order by correct desc";
        } else {
            sql = "select answer, correct, image, question from (step join question) join answer where step.idStep=" + idStep + "' and step.Question_idQuestion=question.idQuestion and question.idQuestion=answer.Question_idQuestion order by correct desc";
        }
        ResultSet rs = stmt.executeQuery(sql);
        if (!rs.next()) {
            System.out.println("vuoto blob!");
        }

        String mex = rs.getString("question") + ";";
        int num_risp = 0;
        do {
            test = rs.getBlob(3);
            bytesEncoded = Base64.encodeBase64(test.getBytes(1, (int) test.length()));
            mex += rs.getString("answer") + "," + rs.getString("correct") + "," + new String(bytesEncoded) + ";";
            num_risp++;
        } while (rs.next());
        mex += "" + num_risp;

        System.out.println("fine send_image, inviate " + num_risp + " risposte");

        response.setContentType("text/plain");
        response.setContentLength(mex.length());
        PrintWriter reply = response.getWriter();
        reply.println(mex);
        out.close(); //non so se serve out
        out.flush();
    }

    private void userFoundTreasure(BufferedReader reader, HttpServletResponse response, Statement stmt, PrintWriter out) throws SQLException, IOException {

        String line = reader.readLine();
        String sql;
        String idEvent = line.substring(13, line.length() - 2);
        // String time=((new Timestamp((new Date()).getTime())).toString()).split(".")[0]; // split per eliminare i nanosecondi
        String time = Instant.now().toString().replace("T", " ").replace("Z", "").substring(0, 19); // Replace 'T', delete 'Z'. I recommend leaving the `Z` or any other such [offset-from-UTC][7] or [time zone][7] indicator to make the meaning clear, but your choice of course.
        System.out.println(time);
        int point;
        int rank; // numero utenti con rank != null + 1 e poi mettere punti su user
        sql = "select count(*) as num from event_has_user where Event_idEvent=" + idEvent + " and  NOT rank<=>null";
        ResultSet res = stmt.executeQuery(sql);
        if (!res.next()) {
            System.out.println("error user found treasure");
            return;
        }

        rank = Integer.parseInt(res.getString("num")) + 1;
        switch (rank) {
            case 1:
                point = 10;
                break;
            case 2:
                point = 6;
                break;
            case 3:
                point = 3;
                break;
            default:
                point = 1;
        }

        sql = "UPDATE `mydb_treasure`.`event_has_user` SET `rank`='" + rank + "', `arrive`='" + time + "' WHERE `Event_idEvent`='" + idEvent + "' and `User_idUser`='" + idUser + "'";

        stmt.executeUpdate(sql);
        sql = "Select points from user where idUser='" + idUser + "'";

        res = stmt.executeQuery(sql);
        if (!res.next()) {
            System.out.println("Nessun user");
        }
        point += Integer.parseInt(res.getString("points"));
        sql = "UPDATE `mydb_treasure`.`user` SET `points`='" + point + "' WHERE `idUser`='" + idUser + "'";
        stmt.executeUpdate(sql);
        System.out.println("Fine partita per utente: " + idUser + ", con tempo: " + time);

        out.close(); //non so se serve out
        out.flush();
    }

    private void timeExceeded(BufferedReader reader, HttpServletResponse response, Statement stmt, PrintWriter out) throws SQLException, IOException {
        String line = reader.readLine();
        String idEvent = line.substring(13, line.length() - 2);
        String time = Instant.now().toString().replace("T", " ").replace("Z", "").substring(0, 19); // Replace 'T', delete 'Z'. I recommend leaving the `Z` or any other such [offset-from-UTC][7] or [time zone][7] indicator to make the meaning clear, but your choice of course.
        String rank = "0"; // scegliere un valore

        String sql = "UPDATE `mydb_treasure`.`event_has_user` SET `rank`='" + rank + "', `arrive`='" + time + "' WHERE `Event_idEvent`='" + idEvent + "' and `User_idUser`='" + idUser + "'";
        stmt.executeUpdate(sql);

        System.out.println("Fine partita per utente: " + idUser + ", con tempo: " + time);

        out.close(); //non so se serve out
        out.flush();
    }

    private void getStartData(BufferedReader reader, HttpServletResponse response, Statement stmt, PrintWriter out) throws IOException, SQLException {
        String line = reader.readLine();
        String idEvent = line.substring(12, line.length() - 2);

        String query = "select * from event where idEvent=" + idEvent + "'";
        ResultSet res = stmt.executeQuery(query);

        if (!res.next()) {
            System.out.println("Nessun evento");
        }

        String resp = res.getString("start_lat") + "," + res.getString("start_lon");

        response.setContentType("text/plain");
        response.setContentLength(resp.length());
        PrintWriter reply = response.getWriter();
        reply.println(resp);
        out.close(); //non so se serve out
        out.flush();

    }

    private void getTipsData(BufferedReader reader, HttpServletResponse response, Statement stmt, PrintWriter out) throws IOException, SQLException {
        String line = reader.readLine();
        String idEvent = line.substring(12, line.length() - 2);

        String query = "select * from step where Event_idEvent=" + idEvent + "'";
        ResultSet res = stmt.executeQuery(query);

        if (!res.next()) {
            System.out.println("Nessuno steps");
            createSteps(idEvent, stmt);
        }
        getSteps(idEvent, response, stmt, out);

        System.out.println("fine getEventData");
    }

    private void createSteps(String idEvent, Statement stmt) throws IOException, SQLException {

        String query = "SELECT * FROM event join city where event.city_name=city.name and event.idEvent=" + idEvent + "'"; // TODO && event.idEvent=idEvent
        ResultSet res = stmt.executeQuery(query);

        if (!res.next()) {
            System.out.println("Nessun evento");
        }

        Location start = new Location(Double.parseDouble(res.getString("start_lat")), Double.parseDouble(res.getString("start_lon")));
        Location center = new Location(Double.parseDouble(res.getString("latitude")), Double.parseDouble(res.getString("longitude")));
        double d = Double.parseDouble(res.getString("diameter"));

        double randLat = (start.lat - center.lat) / kmToLat;
        double randLon = (start.lon - center.lon) / kmToLon;

        System.out.println("center: " + Double.parseDouble(res.getString("latitude")) + ", " + Double.parseDouble(res.getString("longitude")));
        System.out.println("randLat: " + randLat + "   randLon: " + randLon);
        System.out.println("randLat*kmToLat: " + (randLat * kmToLat) + "   randLon*kmToLon: " + (randLon * kmToLon));

        double a = -randLon, b = -randLat;
        double teta = 0;
        if (a == 0 && b == 0)
            ; // error, recalculate randLat and randLon
        if (b > 0) {
            teta = Math.toDegrees(Math.atan(a / b));
        }
        if (a >= 0 && b < 0) {
            teta = 180 + Math.toDegrees(Math.atan(a / b));
        }
        if (a < 0 && b < 0) {
            teta = -180 + Math.toDegrees(Math.atan(a / b));
        }
        if (a > 0 && b == 0) {
            teta = 90;
        }
        if (a < 0 && b == 0) {
            teta = -90;
        }
        if (teta < 0) {
            teta += 360;
        }

        double offset = (teta + 90 < 360) ? (teta + 90) : (teta + 90 - 360);
        System.out.println("teta: " + teta + "   offset: " + offset);

        query = "select number_step from event where idEvent=" + idEvent + "'";
        res = stmt.executeQuery(query);
        if (!res.next()) {
            System.out.println("vuoto numero step nell'evento");
        }

        // at the beginning of the game
        int N = Integer.parseInt(res.getString("number_step")); //numero utenti fare limite damiano
        double passo = 180 / N;

        // get rsndom id question
        query = "SELECT idQuestion FROM mydb_treasure.question where idQuestion<>6 order by rand()";
        res = stmt.executeQuery(query);
        if (!res.next()) {
            System.out.println("Nessuna domanda nel db");
        }
        List<String> questionsId = new ArrayList<String>();
        do {
            questionsId.add(res.getString("idQuestion"));
        } while (res.next());

        double[] tete = new double[N + 1];
        for (int i = 0; i < N + 1; i++) { //FORSE PROBLME SE STEP> QUESTION ****************
            tete[i] = (offset - i * passo < 0.0) ? ((offset - i * passo) + 360) : (offset - i * passo);
            query = "INSERT INTO `mydb_treasure`.`step`(`angle`, `len_step`, `Question_idQuestion`, `event_idEvent`) VALUES ('" + tete[i] + "', '" + lenPasso + "', '" + questionsId.get(i % questionsId.size()) + "', " + idEvent + "')";
            stmt.executeUpdate(query);
        }
    }

    private void getSteps(String idEvent, HttpServletResponse response, Statement stmt, PrintWriter out) throws IOException, SQLException {
        System.out.println("getSteps");

        String query = "SELECT * FROM event join city where event.city_name=city.name and event.idEvent=" + idEvent + "'";
        ResultSet res = stmt.executeQuery(query);

        if (!res.next()) {
            System.out.println("Nessun evento");
        }

        Location start = new Location(Double.parseDouble(res.getString("start_lat")), Double.parseDouble(res.getString("start_lon")));

        query = "select count(*) as num from step where Event_idEvent=" + idEvent + "'";
        res = stmt.executeQuery(query);
        if (!res.next()) {
            System.out.println("Nessuno steps nell' evento 1");
        }

        int N = Integer.parseInt(res.getString("num")) - 1;
        if (N == -1) {
            System.out.println("Nessuno steps nell' evento 2");
        }

        query = "select * from step where Event_idEvent=" + idEvent + "' order by rand()";
        res = stmt.executeQuery(query);
        if (!res.next()) {
            System.out.println("Nessuno step nell' evento");
        }

        double ai, bi;
        double[] tete = new double[N + 1];
        Location[] indizzi = new Location[N + 1];

        String resp = "";
        for (int i = 0; i < N + 1; i++) {
            tete[i] = Double.parseDouble(res.getString("angle"));
            bi = lenPasso * Math.cos(Math.toRadians(computeTeta(tete[i])));
            if (tete[i] > 90 && tete[i] < 270) {
                bi *= -1;
            }
            ai = lenPasso * Math.sin(Math.toRadians(computeTeta(tete[i])));
            if (tete[i] > 180 && tete[i] < 360) {
                ai *= -1;
            }
            System.out.println(i + " -> bi: " + bi + "   ai:" + ai + "   teta: " + tete[i]);
            indizzi[i] = new Location();
            if (i == 0) {
                indizzi[i].lat = start.lat + bi * kmToLat;
                indizzi[i].lon = start.lon + ai * kmToLon;
            } else {
                indizzi[i].lat = indizzi[i - 1].lat + bi * kmToLat;
                indizzi[i].lon = indizzi[i - 1].lon + ai * kmToLon;
            }
            resp += indizzi[i].lat + "," + indizzi[i].lon + "," + res.getString("idStep") + ";";
            res.next();
        }
        resp += "" + (N + 1);

        System.out.println("fine getSteps: " + resp); // ***
        System.out.flush();

        response.setContentType("text/plain");
        response.setContentLength(resp.length());
        PrintWriter reply = response.getWriter();
        reply.println(resp);
        out.close(); //non so se serve out
        out.flush();
    }

    private double computeTeta(double teta) {
        if (teta >= 0 && teta <= 90) {
            return teta;
        }
        if (teta > 90 && teta <= 180) {
            return 180 - teta;
        }
        if (teta > 180 && teta <= 270) {
            return teta - 180;
        }
        //if (teta>270 && teta<360)
        return 360 - teta;
    }

    private void matchResume(BufferedReader reader, HttpServletResponse response, Statement stmt, PrintWriter out) throws IOException, SQLException {
        String resp = "", sql;
        String line = reader.readLine();
        String idEvent = line.substring(12, line.length() - 2);
        sql = "SELECT * FROM Event_has_User where Event_idEvent='" + idEvent + "' and User_idUser='" + idUser + "'";
        ResultSet res = stmt.executeQuery(sql);
        String point;
        if (!res.next()) {
            System.out.println("vuoto event has user");
            resp = " ,";
        } else {
            resp = res.getString("rank");
        }
        switch (resp) {
            case "0":
                point = ",+0,";
                break;
            case "1":
                point = ",+10,";
                break;
            case "2":
                point = ",+6,";
                break;
            case "3":
                point = ",+3,";
                break;
            default:
                point = ",+1,";
        }
        resp = resp.concat(point);
        sql = "SELECT * FROM User where  idUser='" + idUser + "'";
        res = stmt.executeQuery(sql);
        if (!res.next()) {
            System.out.println("vuoto event has user");
        }
        resp = resp.concat(res.getString("points"));
        System.out.println(resp);

        response.setContentType("text/plain");
        response.setContentLength(resp.length());
        PrintWriter reply = response.getWriter();
        reply.println(resp);
        out.close(); //non so se serve out
        out.flush();
    }

    private class Location {

        public double lat, lon;

        Location(double x, double y) {
            lat = x;
            lon = y;
        }

        Location() {
            lat = lon = 0.0;
        }
    }

}
