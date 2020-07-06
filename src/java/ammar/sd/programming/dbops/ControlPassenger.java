package ammar.sd.programming.dbops;

import ammar.sd.programming.calc.LatLng;
import ammar.sd.programming.calc.Place;
import ammar.sd.programming.calc.SortPlaces;
import static ammar.sd.programming.dbops.SocketIOServerApp.listSIOCDriver;
import com.corundumstudio.socketio.SocketIOClient;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.riversun.fcm.FcmClient;
import org.riversun.fcm.model.EntityMessage;
import org.riversun.fcm.model.FcmResponse;

/**
 *
 * @author Ammar Abbas
 */
public class ControlPassenger {

    public static ServerSocket serverSocket1;

    //sql operation method:
    public boolean sqlStatement(String sql) {
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            PreparedStatement preparedStatement = conn
                    .prepareStatement(sql);
            if (preparedStatement.executeUpdate() == 1) {
                return true;
            }
            preparedStatement.close();
            conn.close();
        } catch (Exception ex) {
            System.out.println("sqlStatement Safari EX " + ex.getMessage());
            return false;
        }
        return true;
    }
    //sql operation method:

    public int sqlGetInt(String sql) {
        int result = -1;
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {//if we found phone:
                result = rs.getInt(1);
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("sqlGetInt Safari EX " + e.getMessage());
        }
        return result;
    }

    public boolean sqlStrings(int data, String sql) {
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            PreparedStatement preparedStatement = conn
                    .prepareStatement(sql);
            preparedStatement.setInt(1, data);
            if (preparedStatement.executeUpdate() == 1) {
                return true;
            }
            preparedStatement.close();
            conn.close();
        } catch (Exception ex) {
            System.out.println("sqlStrings Safari EX " + ex.getMessage());
            return false;
        }
        return true;
    }

    public String getPhoneNumber(String SerialNumber) {
        String phone = "0";
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            String query = "SELECT phone FROM  passenger WHERE serial_number ='" + SerialNumber + "'" + " \n"
                    + "union all" + " \n"
                    + "SELECT phone FROM driver WHERE serial_number ='" + SerialNumber + "'";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {//if we found phone:
                phone = rs.getString(1);
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            phone = "-1";
            System.out.println("getPhoneNumber Safari EX " + e.getMessage());
        }
        return phone;
    }

    public String getforbiddenPhoneNumber(String PhoneNumber) {
        String Flag = "-1";//error case
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            String query = "SELECT phone FROM block WHERE phone = '" + PhoneNumber + "'";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {//if we found PhoneNumber:
                Flag = "1";
            } else {
                Flag = "0";
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("getforbiddenPhoneNumber Safari EX " + e.getMessage());
        }
        return Flag;
    }

    public JSONObject SelectCustomers(String passengerId, String OrderID) {
        JSONObject json = new JSONObject();
        Connection conn;
        conn = ConnectionDB.getConnection();
        try {
            String query = "SELECT * FROM passenger WHERE passenger_id = '" + passengerId + "'";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {//`CustomerID`, `CustomerName`, `Phone`, `SerialNumber`, `Email`, `ImageProFile`
                json.put("CustomerID", rs.getInt(1));
                json.put("CustomerName", rs.getString(2));
                json.put("Phone", rs.getString(3));
                json.put("SerialNumber", rs.getString(4));
                json.put("Email", rs.getString(5));
                json.put("ImageProFile", rs.getString(6));
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("SelectCustomers Safari EX " + e.getMessage());
            return null;
        }

        try {
            String query2 = "SELECT status, time_count FROM trip WHERE trip_id = '" + OrderID + "'";
            PreparedStatement ps2 = conn.prepareStatement(query2);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                json.put("Status", rs2.getInt(1));
                json.put("TimeTrips", rs2.getString(2));
            } else {
                json.put("Status", "0");
                json.put("TimeTrips", "0");
            }
            rs2.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("TimeTrips Safari EX " + e.getMessage());
            return null;
        }
        return json;
    }

    public String insertCustomers(String Phone, String SerialNumber, String IPAddress) {
        int CustomerID = 0;
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            PreparedStatement ps2 = conn.prepareStatement("SELECT passenger_id, serial_number FROM passenger WHERE phone = '" + Phone + "'");
            ResultSet rs2 = ps2.executeQuery();
            String SerialNumberDB = "-1";
            if (rs2.next()) {
                CustomerID = rs2.getInt(1);
                SerialNumberDB = rs2.getString(2);
            }
            rs2.close();
            conn.close();
            //============== doing checks:
            if (CustomerID != 0) {
                if (!SerialNumberDB.equalsIgnoreCase(SerialNumber)) {
                    String query = "UPDATE passenger SET serial_number = 'NULL' WHERE serial_number = '" + SerialNumber + "'";
                    if (sqlStatement(query)) {
                    }
                    if (sqlStatement("UPDATE passenger SET serial_number = '" + SerialNumber + "', token = '" + IPAddress + "' WHERE phone = '" + Phone + "'")) {
                    }
                } else if (sqlStatement("UPDATE passenger SET token = '" + IPAddress + "' WHERE phone = '" + Phone + "'")) {
                }
            } else {
                String query = "UPDATE passenger SET serial_number = 'NULL' WHERE serial_number = '" + SerialNumber + "'";
                if (sqlStatement(query)) {
                }
                if (sqlStatement("INSERT INTO passenger (passenger_id, passenger_name, phone, serial_number, email, personal_image, token, token_fierbase)"
                        + " VALUES (NULL, 'NULL', '" + Phone + "', '" + SerialNumber + "', 'NULL', 'NULL', '" + IPAddress + "', 'NULL')") == true) {
                    CustomerID = sqlGetInt("select max(passenger_id) from passenger");
                } else {
                    CustomerID = -1;
                }
            }
        } catch (Exception e) {
            System.out.println("insertCustomers Safari EX " + e.getMessage());
            CustomerID = -1;//error flag
        }
        return CustomerID + "";
    }

    public JSONObject signUpPassenger(String Phone, String SerialNumber) {
        JSONObject jSONObject = new JSONObject();
        int passengerId = 0;
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            PreparedStatement ps2 = conn.prepareStatement("SELECT passenger_id, serial_number FROM passenger WHERE phone = '" + Phone + "'");
            ResultSet rs2 = ps2.executeQuery();
            String SerialNumberDB = "-1";
            if (rs2.next()) {
                passengerId = rs2.getInt(1);
                SerialNumberDB = rs2.getString(2);
            }
            rs2.close();
            conn.close();
            if (passengerId != 0) {
                if (!SerialNumberDB.equalsIgnoreCase(SerialNumber)) {
                    String query = "UPDATE passenger SET serial_number = 'NULL' WHERE serial_number = '" + SerialNumber + "'";
                    if (sqlStatement(query)) {
                    }
                    if (sqlStatement("UPDATE passenger SET serial_number = '" + SerialNumber + "' WHERE phone = '" + Phone + "'")) {
                    }
                }
            } else {
                String query = "UPDATE passenger SET serial_number = 'NULL' WHERE serial_number = '" + SerialNumber + "'";
                if (sqlStatement(query)) {
                }
                if (sqlStatement("INSERT INTO passenger (passenger_id, passenger_name, phone, serial_number, email, personal_image, token, token_fierbase)"
                        + " VALUES (NULL, 'NULL', '" + Phone + "', '" + SerialNumber + "', 'NULL', 'NULL', 'NULL', 'NULL')") == true) {
                    passengerId = sqlGetInt("select max(passenger_id) from passenger");
                } else {
                    passengerId = -1;
                }
            }
        } catch (Exception e) {
            System.out.println("SignUp Safari EX " + e.getMessage());
            passengerId = -1;//error flag
            return null;
        }

        String tockenPassenger = Config.generateToken();
        jSONObject.put("passengerId", String.valueOf(passengerId));
        jSONObject.put("tocken", tockenPassenger);
        if (passengerId > 0) {
            setTockenPassenger("passenger" + passengerId, tockenPassenger);
        }
        return jSONObject;
    }

    public String UpDateCustomers(String passengerId, String CustomerName, String Email) {
        String query = "UPDATE passenger SET passenger_name = '" + CustomerName + "', email = '" + Email + "'"
                + " WHERE passenger_id = '" + passengerId + "'";
        if (sqlStatement(query)) {
            return passengerId;
        }
        return "-1";//error flag
    }

    public String UpDateIPAddress(String CustomerID, String IPAddress) {
        String query = "UPDATE passenger SET token = '" + IPAddress + "' WHERE passenger_id = '" + CustomerID + "'";
        if (sqlStatement(query)) {
            return IPAddress;
        }
        return "-1";//error flag
    }

    public String UpDatePhone(String CustomerID, String PhoneNew) {
        String query = "UPDATE passenger SET phone = '" + PhoneNew + "'"
                + " WHERE passenger_id = '" + CustomerID + "'";
        if (sqlStatement(query)) {
            return "1";
        }
        return "-1";//error flag
    }

    public String DeleteImage(String passengerId, String tocken) {
        String query = "UPDATE passenger SET personal_image = 'NULL' WHERE passenger_id = '" + passengerId + "'";
        if (sqlStatement(query)) {
            return passengerId;
        }
        return "-1";//error flag
    }

    public String insertRate(int DriverID, double ratingBar, String passengerId) {
        String query = "INSERT INTO rate VALUES (NULL , '" + DriverID + "', '" + ratingBar + "')";
        if (sqlStatement(query)) {
            return DriverID + "";
        }
        return "-1";//error flag
    }

    public String OrderDelete(int passengerId, int OrderDeleteID, String tocken) {
        String query = "UPDATE trip SET status = '12' WHERE passenger_id = '" + passengerId + "' AND trip_id = '" + OrderDeleteID + "'";
        if (sqlStatement(query)) {
            return "1";
        }
        return "-1";//error flag
    }

    public String CancelOrder(int CustomerID, int OrderID, int Status) {
        String query;
        query = "UPDATE trip SET status = '" + Status + "' WHERE trip_id = '"
                + OrderID + "' AND passenger_id = '" + CustomerID + "'";
        if (sqlStatement(query)) {
            query = "update driver set status ='onLine' where driver_id = "
                    + "(select driver_id from trip where trip_id = '" + OrderID + "')";
            sqlStatement(query);

            return "1";
        }
        return "-1";//error flag
    }

//    public String DeleteOrder(int CustomerID, int OrderID) {
//        String query = "DELETE FROM trip WHERE passenger_id = '" + CustomerID + "' AND trip_id = '" + OrderID + "'";
//        if (sqlStatement(query)) {
//            return "1";
//        }
//        return "-1";//error flag
//    }
    public String CreateOrder(String CarID, String passengerId, String LatitudeBegin, String LongitudeBegin,
            String LatitudeEnd, String LongitudeEnd, String LocationNameBegin, String LocationNameEnd,
            String Cost, String Distance, String ServiceID, String Noting) {
        String query = "INSERT INTO trip(car_id, passenger_id, start_latitude, start_longitude, end_latitude, end_longitude,"
                + " start_location_name, end_location_name, driver_id, status, arrive_time, trip_price, trip_distance, service_id, trip_note, time_count) VALUES "
                + "('" + Integer.parseInt(CarID) + "', '" + Integer.parseInt(passengerId)
                + "', '" + Double.parseDouble(LatitudeBegin) + "', '" + Double.parseDouble(LongitudeBegin) + "', '" + Double.parseDouble(LatitudeEnd) + "',"
                + " '" + Double.parseDouble(LongitudeEnd) + "', '" + LocationNameBegin + "', '" + LocationNameEnd + "', '0', '0', '0', '" + Double.parseDouble(Cost) + "', '" + Distance + "',"
                + "'" + Integer.parseInt(ServiceID) + "', '" + Noting + "', '0')";
        if (sqlStatement(query)) {
            int orderId = sqlGetInt("select max(trip_id) FROM trip");
            //here we rearrange drivers locations accoording to order place:
            newTrip(passengerId, "0", orderId, CarID, ServiceID, Cost, LatitudeBegin, LongitudeBegin);
            return orderId + "";
        }
        return "-1";//error flag
    }

    public void newTrip(String passengerId, String CancelDriver, int orderId, String CarID, String ServiceID, String Cost, String LatitudeBegin, String LongitudeBegin) {
        ArrayList<Place> listDriver = driverList(CancelDriver, CarID, ServiceID,
                Double.parseDouble(Cost), Double.parseDouble(LatitudeBegin), Double.parseDouble(LongitudeBegin));
        if (listDriver.size() > 0) {
            Collections.sort(listDriver, new SortPlaces(new LatLng(Double.parseDouble(LatitudeBegin), Double.parseDouble(LongitudeBegin))));
            new java.util.Timer().schedule(new TimerTask() {
                int counter = 0;

                @Override
                public void run() {
                    if (counter <= listDriver.size()
                            && sqlGetInt("SELECT trip_id FROM trip WHERE status in(0, 10, 11) and trip_id ='"
                                    + orderId + "'") == orderId) {//while not reach the tail of a list:6
                        if (counter == listDriver.size()) {
                            if (sqlStatement("update trip set driver_id = 0, trip_note = 'cancel'"
                                    + " where trip_id = '" + orderId + "'")) {
                                sqlStatement("update driver set status ='onLine' where driver_id = '" + listDriver.get(counter - 1).driverid + "'");
                                this.cancel();
                                String massege = "timeOutTrip";
                                String event = "time-out-trip";
                                ControlDriver.sendEventDriver(listDriver.get(counter - 1).driverid, massege, event);
                                massege = "noDriverAvailable";
                                event = "no-driver-available";
                                sendEventPassenger(passengerId, massege, event);
                            }
                        } else {
                            String driverId = listDriver.get(counter).driverid;
                            if (sqlStatement("update trip set driver_id = '"
                                    + driverId + "' where trip_id = '" + orderId + "'")
                                    && sqlStatement("update driver set status ='offLine' where driver_id = '" + driverId + "'")) {
                                ControlDriver.sendTripApp(orderId, listDriver.get(counter).driverid);
                                JSONObject jSONObject = new JSONObject();
                                jSONObject.put("driverId", listDriver.get(counter).driverid);
                                jSONObject.put("latitude", listDriver.get(counter).latlng.latitude);
                                jSONObject.put("longitude", listDriver.get(counter).latlng.longitude);
                                String Result = String.valueOf(jSONObject);
                                sendEventPassenger(passengerId, Result, "driver-available");
                                boolean update = (counter > 0) ? sqlStatement("update driver set status ='onLine' where driver_id = '" + listDriver.get(counter - 1).driverid + "'") : false;
                                if (counter > 0) {
                                    String massege = "timeOutTrip";
                                    String event = "time-out-trip";
                                    ControlDriver.sendEventDriver(listDriver.get(counter - 1).driverid, massege, event);
                                }
                                counter++;
                            }
                        }
                    } else {
                        this.cancel();
                        System.out.println("finish Safari list driver");
                    }
                }
            }, 1000, 1000 * 35);
        } else {
            String massege = "noDriverAvailable";
            String event = "no-driver-available";
            sendEventPassenger(passengerId, massege, event);
            System.out.println("List driver Safari isEmpty");
        }
    }

    //upload image:
    public String UploadProFile(String passengerId, HttpServletRequest request) {
        try {
            String realPath = request.getSession().getServletContext().getRealPath("/passenger/UploadProFile").toString();
            File fileSaveDir = new File(realPath);
            try {
                String Data = request.getParameter("ImageProFile");
                String imgname = passengerId + ".png".toString();
                byte dearr[] = Base64.decodeBase64(Data);
                FileOutputStream fos = new FileOutputStream(fileSaveDir + File.separator + imgname);
                fos.write(dearr);
                fos.close();
            } catch (Exception ex) {
                System.out.println("UploadProFile Safari pass SQLException1 " + ex);
                return "-1";
            }
            Connection conn;
            conn = ConnectionDB.getConnection();
            PreparedStatement preparedStatement = conn
                    .prepareStatement("UPDATE passenger SET personal_image =? WHERE passenger_id =?");
//            preparedStatement.setString(1, "https://jaaiktaxi.com:8443/TaxiBackEnd/TaxiAS/UploadProFile/" + customerID + ".png");
//            preparedStatement.setString(1, "http://192.168.8.15:8080/jayeek/TaxiAS/UploadProFile/" + passengerId + ".png");
            preparedStatement.setString(1, "https://safari-sd.com:8443/safari/passenger/UploadProFile/" + passengerId + ".png");
            preparedStatement.setInt(2, Integer.parseInt(passengerId));
            preparedStatement.executeUpdate();
            conn.close();
        } catch (Exception e) {
            System.out.println("UploadProFile Safari pass SQLException2 " + e);
            return "-1";
        }
        return "1";
    }

    public JSONObject ListOrder(int passengerId) {
        JSONObject container = new JSONObject();

        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            String query1 = "SELECT trip_id, car_id, start_location_name,"
                    + " end_location_name, driver_id, status, service_id, trip_price,(SELECT car_name FROM car where car_id = trip.car_id)as car_name, "
                    + "(SELECT driver_name FROM driver where driver_id = trip.driver_id)as driver_name,"
                    + "(SELECT car_number FROM driver where driver_id = trip.driver_id)as car_number,"
                    + "(SELECT car_name FROM driver where driver_id = trip.driver_id)as car_name,"
                    + "(SELECT car_color FROM driver where driver_id = trip.driver_id)as car_color ,"
                    + "trip_date_time "
                    + "FROM trip where passenger_id = '" + passengerId + "' AND status <14";
            PreparedStatement ps1 = conn.prepareStatement(query1);
            ResultSet rs = ps1.executeQuery();
            JSONArray jsonarr = new JSONArray();
            while (rs.next()) {
                JSONObject json = new JSONObject();
                json.put("OrderID", rs.getInt(1));
                json.put("LocationNameBegin", rs.getString(3));
                json.put("LocationNameEnd", rs.getString(4));
                json.put("DriverID", rs.getInt(5));
                json.put("Status", rs.getInt(6));
                json.put("ServiceID", rs.getInt(7));
                json.put("Price", rs.getDouble(8));
                json.put("TaxiName", rs.getString(9));
                json.put("DriverName", rs.getString(10));
                json.put("CarNumber", rs.getString(11));
                json.put("CarName", rs.getString(12));
                json.put("CarColor", rs.getString(13));
                Date date = rs.getTimestamp(14);
                json.put("Year", new SimpleDateFormat("yyyy").format(date));
                json.put("Month", new SimpleDateFormat("MMMM").format(date));
                json.put("Time", new SimpleDateFormat("hh:mm a").format(date));
                json.put("Day", new SimpleDateFormat("EEEE").format(date));
                jsonarr.add(json);
            }
            container.put("ListOrder", jsonarr);
            rs.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("ListOrder Safari pss SQLException " + e.getMessage());
            return null;
        }
        return container;
    }

    public ArrayList<Place> driverList(String CancelDriver, String CarNumber, String ServiceID, double price,
            double latitude, double longitude) {
        ArrayList<Place> driverList = new ArrayList<>();
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            String query = "";
            if (Integer.parseInt(ServiceID) == 1) {
                query = "SELECT driver_id , latitude, longitude from driver where driver_id in "
                        + "(select driver_id from driver_car where car_id ='" + CarNumber + "' and status_car ='onLine') and status = 'onLine' "
                        + "and service_id in (1,3) and ((SELECT amount from payment where driver_id = driver.driver_id)"
                        + ">=(" + price + "*7)/100)and latitude BETWEEN " + (latitude - 0.02) + " and "
                        + (latitude + 0.02) + "  and longitude BETWEEN " + (longitude - 0.02) + " and "
                        + (longitude + 0.02) + " and driver_id <>" + CancelDriver + "";
            } else {
                //2 safari selecting car not allowed
                query
                        = "SELECT driver_id , latitude, longitude from driver where status = 'onLine' "
                        + "and service_id in (2,3) and ((SELECT amount from payment where driver_id = driver.driver_id)"
                        + ">=(" + price + "*7)/100)and latitude BETWEEN " + (latitude - .1) + " and "
                        + (latitude + .1) + "  and longitude  BETWEEN " + (longitude - .1) + " and "
                        + (longitude + .1) + " and driver_id <>" + CancelDriver + "";
            }
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Place place = new Place((rs.getInt(1) + ""), new LatLng(rs.getDouble(2), rs.getDouble(3)));
                driverList.add(place);
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("DriverList Safari SQLException " + e.getMessage());
        }
//        System.out.println("DriverList size.......................... " + driverList.size());
        return driverList;
    }

    public JSONArray getDataCustomer(String SerialNumber, String passengerId, String flag, String OrderID, String PhoneNumber) {
        JSONArray jSONArray = new JSONArray();
        String tockenPassenger = Config.generateToken();
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();

            try {
//                JSONObject jSONObject = new JSONObject();
                String query = "SELECT * FROM car";
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                JSONArray array = new JSONArray();
                while (rs.next()) {
                    JSONObject json = new JSONObject();
                    json.put("TaxiID", rs.getInt(1));
                    json.put("TaxiName", rs.getString(2));
                    json.put("Rang", rs.getInt(3));
                    json.put("TaxiImage", rs.getString(4));
                    json.put("Price", rs.getInt(5));
                    json.put("Number", rs.getInt(6));
                    json.put("available", rs.getInt(7));
                    json.put("Cost", rs.getDouble(8));
                    array.add(json);
                }
                jSONArray.add(0, array);
                rs.close();
            } catch (SQLException e) {
                System.out.println("DataCustomer Safari SQLException1 " + e.getMessage());
                return null;
            }

            if (flag.equals("true")) {
                try {
                    JSONObject jSONObject = new JSONObject();
                    String query = "SELECT version_passenger, call_center, check_update_passenger,"
                            + "(SELECT passenger_name FROM passenger where passenger_id = '" + passengerId + "')as passenger_name,"
                            + "(SELECT phone FROM passenger where passenger_id = '" + passengerId + "')as phone,"
                            + "(SELECT email FROM passenger where passenger_id = '" + passengerId + "')as email,"
                            + "(SELECT personal_image FROM passenger where passenger_id = '" + passengerId + "')as personal_image,"
                            + "(SELECT time_count FROM trip where trip_id = '" + OrderID + "')as time_count,"
                            + "(SELECT status FROM trip where trip_id = '" + OrderID + "')as status,"
                            + "(SELECT phone FROM block where phone = '" + PhoneNumber + "')as phone"
                            + " FROM version";
                    PreparedStatement ps = conn.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        jSONObject.put("AppCustomer", rs.getString(1));
                        jSONObject.put("callCenter", rs.getString(2));
                        jSONObject.put("UpDateCustomer", rs.getString(3));
                        jSONObject.put("CustomerName", rs.getString(4));
                        jSONObject.put("Phone", rs.getString(5));
                        jSONObject.put("Email", rs.getString(6));
                        jSONObject.put("ImageProFile", rs.getString(7));
                        jSONObject.put("TimeTrips", rs.getString(8));
                        jSONObject.put("Status", rs.getInt(9));
                        jSONObject.put("PhoneNumber", rs.getString(10));
                        jSONObject.put("tocken", tockenPassenger);//add
                    }
                    jSONArray.add(1, jSONObject);
                    rs.close();
                } catch (Exception e) {
                    System.out.println("DataCustomer Safari SQLException2 " + e.getMessage());
                    return null;
                }
            } else {
                JSONObject jSONObject = new JSONObject();
                try {
                    String query = "SELECT version_passenger, call_center, check_update_passenger,"
                            + "(SELECT phone FROM passenger where serial_number = '" + SerialNumber + "')as phone"
                            + " FROM version";
                    PreparedStatement ps = conn.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {//if we found phone:
                        jSONObject.put("AppCustomer", rs.getString(1));
                        jSONObject.put("callCenter", rs.getString(2));
                        jSONObject.put("UpDateCustomer", rs.getString(3));
                        jSONObject.put("Phone", rs.getString(4));
                        jSONObject.put("tocken", tockenPassenger);//add
                    }
                    jSONArray.add(1, jSONObject);
                    rs.close();
                } catch (Exception e) {
                    System.out.println("DataCustomer Safari SQLException3 " + e.getMessage());
                    return null;
                }
            }
            conn.close();
            setTockenPassenger("passenger" + passengerId, tockenPassenger);
        } catch (Exception e) {
            System.out.println("DataCustomer Safari SQLException4 " + e.getMessage());
            return null;
        }
        return jSONArray;
    }

    //new service passenger
    //sendPushNotivication
    public static void sendPushNotification(String massege, String passengerId) {
        try {
            String token;
            Connection connection = ConnectionDB.getConnection();
            String query = "SELECT token_fierbase FROM passenger WHERE passenger_id = '" + passengerId + "'";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                token = resultSet.getString(1);
                FcmClient fcmClient = new FcmClient();
                fcmClient.setAPIKey(Config.APIKeyPassenger);
                EntityMessage entityMessage = new EntityMessage();
                entityMessage.addRegistrationToken(token);
                entityMessage.putStringData("passenger", massege);
                FcmResponse fcmResponse = fcmClient.pushToEntities(entityMessage);
            }
            resultSet.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("fierBase Safari pas Ex " + e);
        }
    }

    public void threadSocket(int tripId) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serviceTrip(tripId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    //get online drivers:
    public void availableCaptin(String passengerId, double latitude, double longitude) {
        JSONArray jSONArray = new JSONArray();
        String massege = "NULL";
        try {
            Connection connection = ConnectionDB.getConnection();
            String query = "SELECT latitude , longitude FROM driver WHERE status = 'online' "
                    + "AND latitude BETWEEN " + (latitude - 0.02) + " AND "
                    + (latitude + 0.02) + " AND longitude  BETWEEN " + (longitude - 0.02) + " AND "
                    + (longitude + 0.02);
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                JSONObject json = new JSONObject();
                json.put("Latitude", resultSet.getDouble(1));
                json.put("Longitude", resultSet.getDouble(2));
                jSONArray.add(json);
                massege = String.valueOf(jSONArray);
            }
            resultSet.close();
            connection.close();

        } catch (Exception e) {
            massege = "ERROR";
            System.out.println("availableCaptin Safari Ex " + e);
        }
        String event = "available-captin";
        sendEventPassenger(passengerId, massege, event);
    }

    public String cancelTrip(int passengerId, int tripId, String driverId) {
        String query;
        query = "UPDATE trip SET status = '9' WHERE trip_id = '"
                + tripId + "' AND passenger_id = '" + passengerId + "'";
        if (sqlStatement(query)) {
            query = "update driver set status = 'onLine' where driver_id = "
                    + "(select driver_id from trip where trip_id = '" + tripId + "')";
            sqlStatement(query);
            String massege = "cancelTrip";
            String event = "cancel-trip";
            if (!driverId.equals("0")) {
                ControlDriver.threadSocket(massege, driverId, event);
            }
            return "1";
        }
        return "-1";//error flag
    }

    public void locationDriver(String passengerId, int driverId, int tripId) {
        new java.util.Timer().schedule(new TimerTask() {
            String massege;

            @Override
            public void run() {
                JSONObject jSONObject = new JSONObject();
                try {
                    Connection connection = ConnectionDB.getConnection();
                    String query = "SELECT latitude, longitude FROM driver WHERE driver_id = '" + driverId + "'";
                    PreparedStatement statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        jSONObject.put("latitude", resultSet.getDouble(1));
                        jSONObject.put("longitude", resultSet.getDouble(2));
                        SocketIOClient iOClient = SocketIOServerApp.listSIOCStatusPassenger.get("statusApp" + passengerId);
                        if (iOClient == null) {
                            this.cancel();
                        } else {
                            massege = String.valueOf(jSONObject);
                            String event = "location-driver-trip";
                            sendEventPassenger(passengerId, massege, event);
                        }
                    }
                    int Status = sqlGetInt("select status FROM trip where trip_id = '" + tripId + "'");
                    if (Status >= 7) {
                        this.cancel();
                    }
                    resultSet.close();
                    connection.close();
                } catch (Exception e) {
                    System.out.println("getLocationDriver Safari Exception " + e);
                }
            }
        }, 1000, 1000 * 3);
    }

    public void serviceTrip(int orderId) {
        JSONObject json = new JSONObject();
        Encrypt encrypt = new Encrypt();
        try {
            int statusorderId = 0;
            Connection conn;
            conn = ConnectionDB.getConnection();
            String query = "SELECT passenger_id, trip_id, driver_id, status, arrive_time, trip_price, trip_distance,"
                    + "(SELECT token FROM passenger where passenger_id = trip.passenger_id) as token,"
                    + "(SELECT driver_name FROM driver where driver_id = trip.driver_id) as driver_name,"
                    + "(SELECT car_number FROM driver where driver_id = trip.driver_id) as car_number,"
                    + "(SELECT car_name FROM driver where driver_id = trip.driver_id) as car_name,"
                    + "(SELECT car_color FROM driver where driver_id = trip.driver_id) as car_color,"
                    + "(SELECT latitude FROM driver where driver_id = trip.driver_id) as latitude,"
                    + "(SELECT longitude FROM driver where driver_id = trip.driver_id) as longitude,"
                    + "(SELECT phone FROM driver where driver_id = trip.driver_id) as phone,"
                    + "(SELECT personal_image FROM driver where driver_id = trip.driver_id) as personal_image"
                    + " FROM trip where status in(0, 1, 3, 5, 7, 9, 10, 12) AND trip_id = '" + orderId + "'";

            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int passengerId = rs.getInt(1);
                json.put("OrderID", rs.getInt(2));
                json.put("DriverID", rs.getInt(3));
                json.put("Status", rs.getInt(4));
                json.put("Time", rs.getInt(5));
                json.put("Price", rs.getDouble(6));
                json.put("Distance", rs.getDouble(7));
                json.put("IPAddress", rs.getString(8));
                json.put("DriverName", rs.getString(9));
                json.put("CarNumber", rs.getString(10));
                json.put("CarName", rs.getString(11));
                json.put("CarColor", rs.getString(12));
                json.put("Latitude", rs.getDouble(13));
                json.put("Longitude", rs.getDouble(14));
                json.put("Phone", rs.getString(15));
                json.put("ImageProFile", rs.getString(16));

                statusorderId = rs.getInt(4);
                if (statusorderId == 9) {
                } else if (statusorderId > 0) {
                    try {
                        String massege = String.valueOf(json);
                        String event = "service-trip";
                        sendEventPassenger(String.valueOf(passengerId), massege, event);
                        sendPushNotification(massege, String.valueOf(passengerId));

                        if (statusorderId == 1) {
                            sqlStrings(orderId, "UPDATE trip SET status = '2' WHERE trip_id = ?");
                        } else if (statusorderId == 3) {
                            sqlStrings(orderId, "UPDATE trip SET status = '4' WHERE trip_id = ?");
                        } else if (statusorderId == 5) {
                            sqlStrings(orderId, "UPDATE trip SET status = '6' WHERE trip_id = ?");
                        } else if (statusorderId == 7) {
                            sqlStrings(orderId, "UPDATE trip SET status = '8' WHERE trip_id = ?");
                        } else if (statusorderId == 10) {
                            sqlStrings(orderId, "UPDATE trip SET status = '11' WHERE trip_id = ?");
                        } else if (statusorderId == 12) {
                            sqlStrings(orderId, "UPDATE trip SET status = '13' WHERE trip_id = ?");
                        }

                    } catch (Exception e) {
                        System.out.println("ServicsOrder Safari Exception " + e);
                    }
                }
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            System.out.println(" SSSQLException Safari " + e);
        }
    }

    public void upDateToken(int passengerId, String token) {
        String query;
        query = "UPDATE passenger SET token_fierbase = '" + token + "' WHERE passenger_id = '" + passengerId + "'";
        if (sqlStatement(query)) {
        }
    }

    public static void sendEventPassenger(String passengerId, String massege, String eventName) {
        try {
            Encrypt encrypt = new Encrypt();
            if (massege != null) {
                if (!massege.isEmpty()) {
                    String Result = encrypt.Encrypt(massege);
                    SocketIOClient iOClient = SocketIOServerApp.listSIOCPassenger.get("passenger" + passengerId);
                    iOClient.sendEvent(eventName, Result);
                } else {
                    System.out.println("else Safari Passenger IsEmpty");
                }
            } else {
                System.out.println("else Safari Passenger Null");
            }
        } catch (Exception e) {
            System.out.println("sendEventPassenger Safari EX " + e);
        }
    }

    public void setTockenPassenger(String keyTocken, String tocken) {
        try {
            String tockenPassenger = Config.listTockenPassenger.get(keyTocken);
            if (tockenPassenger == null) {
                Config.listTockenPassenger.put(keyTocken, tocken);
            } else {
                Config.listTockenPassenger.replace(keyTocken, tocken);
            }
        } catch (Exception e) {
        }
    }
}
