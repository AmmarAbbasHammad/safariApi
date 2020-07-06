package ammar.sd.programming.dbops;

import ammar.sd.programming.control.Driver;
import ammar.sd.programming.control.Passenger;
import static ammar.sd.programming.dbops.ControlPassenger.sendPushNotification;
import com.corundumstudio.socketio.SocketIOClient;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.riversun.fcm.FcmClient;
import org.riversun.fcm.model.EntityMessage;
import org.riversun.fcm.model.FcmResponse;

/**
 *
 * @author Ammar Abbas
 */
public class ControlDriver {

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

    public String getPhoneNumber(String serialNumber) {
        String phone = "0";
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            String query = "SELECT phone FROM driver WHERE serial_number ='" + serialNumber + "'";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {//if we found phone:
                phone = rs.getString(1);
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("getPhoneNumber" + e.getMessage());
            return "-1";
        }
        return phone;
    }

    public String getforbiddenPhoneNumber(String phoneNumber) {
        String Flag = "-1";//error case
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            String query = "SELECT phone FROM block WHERE phone = '" + phoneNumber + "'";
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
            return "-1";
        }
        return Flag;
    }

    public String getTypeServices(String phoneNumber) {
        int ServicesID = 0;
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            String query = "SELECT service_id FROM driver WHERE phone =  '" + phoneNumber + "'";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ServicesID = rs.getInt(1);
            }
            rs.close();
            conn.close();
            if (ServicesID != 0) {
                return ServicesID + "";
            } else {
                return "0";
            }
        } catch (Exception e) {
            return "-1";
        }
    }

    public String insertRate(int DriverID, double ratingBar) {
        String query = "INSERT INTO `rate` VALUES (NULL , '" + DriverID + "', '" + ratingBar + "')";
        if (sqlStatement(query)) {
            return DriverID + "";
        }
        return "-1";//error flag

    }

    public String insertCustomers(String Phone, String SerialNumber, String IPAddress) {
        int DriverID = 0;

        try {
            Connection conn2;
            conn2 = ConnectionDB.getConnection();
            PreparedStatement ps2 = conn2.prepareStatement("SELECT driver_id, serial_number FROM driver WHERE phone =  '" + Phone + "'");
            ResultSet rs2 = ps2.executeQuery();
            String SerialNumberDB = "-1";
            if (rs2.next()) {
                DriverID = rs2.getInt(1);
                SerialNumberDB = rs2.getString(2);
            }
            rs2.close();
            conn2.close();
            //============== doing checks:
            if (DriverID != 0) {
                if (!SerialNumberDB.equalsIgnoreCase(SerialNumber)) {
                    String query = "UPDATE `driver` SET serial_number = 'NULL' WHERE serial_number = '" + SerialNumber + "'";
                    if (sqlStatement(query)) {
                    }
                    if (sqlStatement("UPDATE driver SET serial_number = '" + SerialNumber + "', token = '" + IPAddress + "' WHERE phone = '" + Phone + "'")) {
                    }
                } else if (sqlStatement("UPDATE driver SET token = '" + IPAddress + "' WHERE phone = '" + Phone + "'")) {
                }
            } else {
                String query = "UPDATE `driver` SET serial_number = 'NULL' WHERE serial_number = '" + SerialNumber + "'";
                if (sqlStatement(query)) {
                }
                if (sqlStatement("INSERT INTO `driver`"
                        + "(`driver_id`, `driver_name`, `phone`, `car_number`, `car_id`, `car_name`, `car_color`, `latitude`, `longitude`, `status`,"
                        + " `active`, `serial_number`, `personal_image`, `service_id`, `id_number`, `emp_id`, `token`, `token_fierbase`)"
                        + " VALUES (NULL, 'NULL', '" + Phone + "', 'NULL', '0', 'NULL', 'NULL', '0', '0', "
                        + "'offLine', 'notActive', '" + SerialNumber + "', 'NULL', '1', 'NULL', '0', '" + IPAddress + "', 'NULL')") == true) {
                    return sqlGetInt("select max(driver_id) from driver") + "";
                } else {
                    DriverID = -1;
                }
            }
        } catch (Exception e) {
            System.out.println("insertCustomers" + e.getMessage());
            return "-1";//error flag
        }
        return DriverID + "";

    }

    public JSONObject signUpDriver(String Phone, String SerialNumber) {
        JSONObject jSONObject = new JSONObject();
        int driverId = 0;

        try {
            Connection conn2;
            conn2 = ConnectionDB.getConnection();
            PreparedStatement ps2 = conn2.prepareStatement("SELECT driver_id, serial_number FROM driver WHERE phone =  '" + Phone + "'");
            ResultSet rs2 = ps2.executeQuery();
            String SerialNumberDB = "-1";
            if (rs2.next()) {
                driverId = rs2.getInt(1);
                SerialNumberDB = rs2.getString(2);
            }
            rs2.close();
            conn2.close();
            //============== doing checks:
            if (driverId != 0) {
                if (!SerialNumberDB.equalsIgnoreCase(SerialNumber)) {
                    String query = "UPDATE `driver` SET serial_number = 'NULL' WHERE serial_number = '" + SerialNumber + "'";
                    if (sqlStatement(query)) {
                    }
                    if (sqlStatement("UPDATE driver SET serial_number = '" + SerialNumber + "' WHERE phone = '" + Phone + "'")) {
                    }
                }
            } else {
                String query = "UPDATE `driver` SET serial_number = 'NULL' WHERE serial_number = '" + SerialNumber + "'";
                if (sqlStatement(query)) {
                }
                if (sqlStatement("INSERT INTO `driver`"
                        + "(`driver_id`, `driver_name`, `phone`, `car_number`, `car_id`, `car_name`, `car_color`, `latitude`, `longitude`, `status`,"
                        + " `active`, `serial_number`, `personal_image`, `service_id`, `id_number`, `emp_id`, `token`, `token_fierbase`)"
                        + " VALUES (NULL, 'NULL', '" + Phone + "', 'NULL', '0', 'NULL', 'NULL', '0', '0', "
                        + "'offLine', 'notActive', '" + SerialNumber + "', 'NULL', '1', 'NULL', '0', 'NULL', 'NULL')") == true) {
                    driverId = sqlGetInt("select max(driver_id) from driver");
                } else {
                    driverId = -1;
                }
            }
        } catch (Exception e) {
            System.out.println("SignUp Safari EX " + e.getMessage());
            driverId = -1;//error flag
        }

        String tockenDriver = Config.generateToken();
        jSONObject.put("driverId", String.valueOf(driverId));
        jSONObject.put("tocken", tockenDriver);
        if (driverId > 0) {
            setTockenDriver("driver" + driverId, tockenDriver);
        }
        return jSONObject;
    }

    public JSONObject ListOrder(int DriverID) {
        JSONObject jcontainer = new JSONObject();
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            String query = "SELECT start_location_name,"
                    + " end_location_name, service_id, trip_price,(SELECT car_name FROM car where car_id = trip.car_id)as car_name"
                    + ",(SELECT passenger_name FROM passenger where passenger_id = trip.passenger_id)as passenger_name,"
                    + "trip_date_time "
                    + "FROM trip where driver_id = '" + DriverID + "' AND status in (7, 8)";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            JSONArray container = new JSONArray();
            while (rs.next()) {
                JSONObject json = new JSONObject();
                json.put("LocationNameBegin", rs.getString(1));
                json.put("LocationNameEnd", rs.getString(2));
                json.put("ServiceID", rs.getInt(3));
                json.put("Price", rs.getDouble(4));
                json.put("TaxiName", rs.getString(5));
                json.put("CustomerName", rs.getString(6));
                Date date = rs.getTimestamp(7);
                json.put("Year", new SimpleDateFormat("yyyy").format(date));
                json.put("Month", new SimpleDateFormat("MMMM").format(date));
                json.put("Time", new SimpleDateFormat("hh:mm a").format(date));
                json.put("Day", new SimpleDateFormat("EEEE").format(date));

                container.add(json);
            }
            rs.close();
            conn.close();
            jcontainer.put("0", container);
        } catch (Exception e) {
            return null;
        }
        return jcontainer;

    }

    public String OrderDelete(int DriverID, int OrderDeleteID) {
        String query = "UPDATE `trip` SET status = '11' WHERE driver_id = '" + DriverID + "' AND trip_id = '" + OrderDeleteID + "'";
        if (sqlStatement(query)) {
            return "1";
        }
        return "-1";//error flag
    }

    public String SelectAccount(String DriverID) {
        String Account = "0";
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            String query = "SELECT amount FROM payment WHERE driver_id = '" + DriverID + "'";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Account = rs.getDouble(1) + "";
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            return "-1";
        }
        return Account;

    }

    public JSONObject SelectFreeCar(String DriverID) {
        JSONObject jsonContainer = new JSONObject();
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();
            String query = "SELECT car_id, status_car FROM driver_car WHERE driver_id =  '" + DriverID + "'";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            JSONArray JArr = new JSONArray();
            while (rs.next()) {
                JSONObject json = new JSONObject();
                json.put("CarNumber", rs.getInt(1));
                json.put("Status", rs.getString(2));
                JArr.add(json);
            }
            jsonContainer.put("ArrayFreeCar", JArr);
            rs.close();
            conn.close();
        } catch (Exception e) {
            return null;
        }
        return jsonContainer;

    }

    public String UpDateAccount(int DriverID, double Cost) {
        if (sqlStatement("UPDATE payment SET amount  = amount  -((" + Cost + "*7)/100) WHERE driver_id ='" + DriverID + "'")) {
            return "1";
        }
        return "-1";
    }

    public String UpDateIPAddress(String DriverID, String IPAddress) {
        if (sqlStatement("UPDATE driver SET token = '" + IPAddress + "' WHERE driver_id ='" + DriverID + "'")) {
            return IPAddress;
        }
        return "-1";
    }
    
    public void updateSocket(String driverId, String socket) {
        if (sqlStatement("UPDATE driver SET token = '" + socket + "' WHERE driver_id ='" + driverId + "'")) {
        }
    }
    
    public void updateStatusDriver(String socket) {
        if (sqlStatement("UPDATE driver SET status = 'offLine' WHERE token ='" + socket + "'")) {
        }
    }

    public String UpDateAccountOrder(int OrderID, int DriverID, String Status, double price, double Distance) {
        if (sqlStatement("UPDATE trip SET trip_price = '" + price + "', status = '" + Status + "', trip_distance = '" + Distance + "' "
                + "WHERE trip_id = '" + OrderID + "' AND driver_id = '" + DriverID + "'")) {
            if (sqlStatement("UPDATE payment SET amount = amount -((" + price + "7)/100) WHERE driver_id ='" + DriverID + "'")
                    && sqlStatement("UPDATE driver set status ='onLine' where driver_id = '" + DriverID + "'")) {
                return "1";
            }
        }
        return "-1";
    }

    public void UpDateLocation(String driverId, double Latitude, double Longitude, int lable) {
        Encrypt encrypt = new Encrypt();
        if (sqlStatement("UPDATE driver SET latitude = '" + Latitude + "', longitude = '" + Longitude + "' WHERE driver_id = '" + driverId + "'")) {
            try {
                String messageEncrypt = encrypt.Encrypt("successfuly");
                SocketIOClient iOClient = SocketIOServerApp.listSIOCDriver.get("driver" + driverId);
                iOClient.sendEvent("update-location", messageEncrypt);
            } catch (Exception e) {
                System.out.println("Exception Safari UpDateLocation " + e);
            }
        }
    }

    public String UpDateOnLineOffLine(int DriverID, String Status, String CarNumber, int Table) {
        if (Table == 1) {
            if (sqlStatement("UPDATE driver SET status = '" + Status + "' WHERE driver_id = '" + DriverID + "'")) {
                return "1";
            }
        } else {
            if (sqlStatement("UPDATE driver_car SET status_car = '" + Status + "' WHERE driver_id = '" + DriverID + "' AND car_id = '" + CarNumber + "'")) {
                return "1";
            }
        }
        return "-1";
    }

    public String UpDateStatus(int orderId, int driverId, int statusOrder, int time) {
        if (statusOrder == 0) {//driver reject order from first time
            if (sqlStatement("UPDATE trip SET driver_id = 0 WHERE trip_id = '" + orderId + "' AND driver_id = '" + driverId + "'")
                    && sqlStatement("UPDATE driver set status ='onLine' where driver_id = '" + driverId + "'")) {
                return "1";
            }
        } else {
            int status = sqlGetInt("select status FROM trip where trip_id = '" + orderId + "'");
            int driverid = sqlGetInt("select driver_id FROM trip where trip_id = '" + orderId + "'");
            if (driverid == driverId && status != 9) {
                String query;
                if (statusOrder == 1) {
                    query = "UPDATE trip SET status = '" + statusOrder + "', arrive_time = '" + time + "' WHERE status <>9 AND trip_id = '" + orderId + "' AND driver_id = '" + driverId + "'";
//                    servicePassenger.locationDriver(orderId);
                } else {
                    query = "UPDATE trip SET status = '" + statusOrder + "' WHERE status <>9 AND trip_id = '" + orderId + "' AND driver_id = '" + driverId + "'";
                }
                if (sqlStatement(query)) {
                    if (statusOrder == 7) {//journy copmlete
                        sqlStatement("UPDATE driver set status ='onLine' where driver_id = '" + driverId + "'");
                    }
                    if (statusOrder == 1) {//driver accept order
                        timerTrips1(orderId, --time, 59);
//                        CancelTripApp(OrderID, 3);
                        sqlStatement("UPDATE driver set status ='offLine' where driver_id = '" + driverId + "'");
                    }
                    if (statusOrder == 3) {
                        timerTrips2(orderId, 0, 0, 3);
                    }
                    if (statusOrder == 5) {
                        timerTrips2(orderId, 0, 0, 5);
                    }
                    return "1";
                }
            } else if (driverid != driverId) {
                return "2";
            } else if (status == 9) {
                return "3";
            }
        }
        return "-1";
    }

    public String UpDateTime(int OrderID, int DriverID, int Time) {
        int status = sqlGetInt("select status FROM trip where trip_id = '" + OrderID + "'");
        if (status != 9) {
            if (sqlStatement("UPDATE trip SET arrive_time = '" + Time + "' WHERE trip_id = '" + OrderID + "' AND driver_id = '" + DriverID + "'")) {
                timerTrips1(OrderID, --Time, 59);
//                CancelTripApp(OrderID, 3);
                return "1";
            }
        } else {
            return "3";
        }
        return "-1";//
    }
    //upload image:

    public String UploadProFile(String customerID, HttpServletRequest request) {
        try {
            String realPath = request.getSession().getServletContext().getRealPath(File.separator + "driver" + File.separator + "UploadProFile").toString();
            File fileSaveDir = new File(realPath);
            String Data;
            try {
                Data = request.getParameter("ImageProFile");
                String imgname = customerID + ".png".toString();
                byte dearr[] = Base64.decodeBase64(Data);
                FileOutputStream fos = new FileOutputStream(fileSaveDir + File.separator + imgname);
                fos.write(dearr);

                fos.close();
            } catch (Exception e) {
                System.out.println("UploadProFile Safari Dri SQLException 1 " + e);
                return "-1";
            }
            Connection conn;
            conn = ConnectionDB.getConnection();
            PreparedStatement preparedStatement = conn
                    .prepareStatement("UPDATE driver SET personal_image =? WHERE driver_id=?");
//            preparedStatement.setString(1, "https://jaaiktaxi.com:8443/TaxiBackEnd/WaselDriver/UploadProFile/" + customerID + ".png");
//            preparedStatement.setString(1, "http://192.168.43.220:8080/safari/driver/UploadProFile/" + customerID + ".png");
            preparedStatement.setString(1, "https://safari-sd.com:8443/safari/driver/UploadProFile/" + customerID + ".png");
            preparedStatement.setInt(2, Integer.parseInt(customerID));
            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("UploadProFile Safari Dri SQLException " + e);
            return "-1";
        }
        return "1";
    }

    public String CancelTrips(String DriverID, String typeTrip, String OrderID) {
        if (Integer.parseInt(typeTrip) == 1) {//discount 3 bounds from driver account

            String passengerId = "", CarID = "", ServiceID = "", Price = "", LatitudeBegin = "", LongitudeBegin = "";
            // get data for canceled order to start up pocking :
            try {
                Connection conn;
                conn = ConnectionDB.getConnection();
                String query = "SELECT passenger_id, car_id, service_id, trip_price, start_latitude, start_longitude FROM trip "
                        + "WHERE trip_id = '" + OrderID + "'";
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {//if we found phone:
                    passengerId = rs.getInt("CustomerID") + "";
                    CarID = rs.getInt("CarID") + "";
                    ServiceID = rs.getInt("ServiceID") + "";
                    Price = rs.getDouble("Price") + "";
                    LatitudeBegin = rs.getDouble("LatitudeBegin") + "";
                    LongitudeBegin = rs.getDouble("LongitudeBegin") + "";
                }
                rs.close();
                conn.close();
            } catch (Exception e) {
                System.out.println("CancelTrips SQLException...................................... " + e);
                return "-1";
            }

            String query1 = "UPDATE payment SET amount = amount-15 WHERE driver_id = '" + DriverID + "'";
            String query2 = "UPDATE trip SET driver_id =0, status = 10,arrive_time = 0, time_count = 0 WHERE trip_id = '" + OrderID + "'";
            String query3 = "UPDATE driver SET status ='onLine' WHERE driver_id = '" + DriverID + "'";
            if (sqlStatement(query1) && sqlStatement(query2) && sqlStatement(query3)) {
                //loop till serive this order:
                ControlPassenger controlPassenger = new ControlPassenger();
                controlPassenger.newTrip(passengerId, DriverID, Integer.parseInt(OrderID), CarID, ServiceID, Price, LatitudeBegin, LongitudeBegin);
                return "1";
            }
        } else {
            if (sqlStatement("UPDATE trip  SET status = 12 WHERE trip_id  = '" + OrderID + "'")
                    && sqlStatement("UPDATE driver SET status ='onLine' WHERE driver_id = '" + DriverID + "'")) {
                return "1";
            }
        }
        return "2";
    }

    public void timerTrips1(int OrderID, int Time1, int Time2) {
        new java.util.Timer().schedule(new TimerTask() {
            int t1 = Time1, t2 = Time2, Counter = 0;

            @Override
            public void run() {
                try {
                    if (t1 >= 0) {
                        if (t2 > 0) {
                            --t2;
                        } else {
                            --t1;
                            t2 = 59;
                        }
                    } else {
                        t1 = -1;
                    }
                    String timer = t1 + ":" + t2;
                    if (Counter == 5) {
                        String query = "UPDATE trip SET time_count = '" + timer + "' where trip_id = '" + OrderID + "' "
                                + "and status in(1, 2)";
                        if (sqlStatement(query)) {
                        }
                        int Status = sqlGetInt("select status FROM trip where trip_id = '" + OrderID + "'");
                        if (Status == 3 || Status == 4 || Status == 9 || Status == 10 || Status == 11) {
                            this.cancel();
                        }
                        Counter = 0;
                    }
                    Counter++;
                } catch (Exception e) {
                    System.out.println("TimerTrips1 Safari Exception " + e);
                }
            }
        }, 1000, 1000 * 1);
    }

    public void timerTrips2(int OrderID, int Time1, int Time2, int st) {
        new java.util.Timer().schedule(new TimerTask() {
            int t1 = Time1, t2 = Time2, Counter = 0, status = st;

            @Override
            public void run() {
                try {
                    if (t2 == 59) {
                        ++t1;
                        t2 = 0;
                    } else {
                        ++t2;
                    }
                    String timer = t1 + ":" + t2;
                    if (Counter == 5) {
                        String query = "UPDATE trip SET time_count = '" + timer + "' where trip_id = '" + OrderID + "' "
                                + "and status in('" + status + "', '" + (status + 1) + "')";
                        if (sqlStatement(query)) {
                        }
                        int Status = sqlGetInt("select status FROM trip where trip_id = '" + OrderID + "'");
                        if (Status == status + 2 || Status == status + 3 || Status == 9 || Status == 10 || Status == 11 || Status == 12 || Status == 13) {
                            this.cancel();
                        }
                        Counter = 0;
                    }
                    Counter++;
                } catch (Exception e) {
                    System.out.println("TimerTrips1 Safari Exception" + e);
                }
            }
        }, 1000, 1000 * 1);
    }

    public JSONArray getDataDriver(String SerialNumber, String driverId, String flag, String OrderID, String PhoneNumber, String IPAddress) {
        JSONArray jSONArray = new JSONArray();
        String tockenDriver = Config.generateToken();
        try {
            Connection conn;
            conn = ConnectionDB.getConnection();

            try {
                JSONObject jSONObject = new JSONObject();
                String query = "SELECT car_id, car_name FROM car";
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                JSONArray array = new JSONArray();
                while (rs.next()) {
                    JSONObject json = new JSONObject();
                    json.put("TaxiID", rs.getInt(1));
                    json.put("TaxiName", rs.getString(2));
                    array.add(json);
                }
                jSONArray.add(0, array);
                rs.close();
            } catch (Exception e) {
                System.out.println("SQLException1 Safari " + e.getMessage());
                return null;
            }

            if (flag.equals("true")) {
                try {
                    String Query = "UPDATE `driver` SET `status` = 'offLine' WHERE `driver`.`driver_id` = '" + driverId + "'";
                    sqlStatement(Query);
                    JSONObject jSONObject = new JSONObject();
                    String query = "SELECT version_driver, call_center, check_update_driver,"
                            + "(SELECT driver_name FROM driver where driver_id = '" + driverId + "')as driver_name,"
                            + "(SELECT phone FROM driver where driver_id = '" + driverId + "')as phone,"
                            + "(SELECT personal_image FROM driver where driver_id = '" + driverId + "')as personal_image,"
                            + "(SELECT car_number FROM driver where driver_id = '" + driverId + "')as car_number,"
                            + "(SELECT car_name FROM driver where driver_id = '" + driverId + "')as car_name,"
                            + "(SELECT car_color FROM driver where driver_id = '" + driverId + "')as car_color,"
                            + "(SELECT car_id FROM driver where driver_id = '" + driverId + "')as car_id,"
                            + "(SELECT active FROM driver where driver_id = '" + driverId + "')as active,"
                            + "(SELECT time_count FROM trip where trip_id = '" + OrderID + "')as time_count,"
                            + "(SELECT phone FROM block where phone = '" + PhoneNumber + "')as phone"
                            + " FROM version";

                    PreparedStatement ps = conn.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        jSONObject.put("AppDriver", rs.getString(1));
                        jSONObject.put("callCenter", rs.getString(2));
                        jSONObject.put("Cost", String.valueOf(5));
                        jSONObject.put("UpDateDriver", rs.getString(3));
                        jSONObject.put("DriverName", rs.getString(4));
                        jSONObject.put("Phone", rs.getString(5));
                        jSONObject.put("ImageProFile", rs.getString(6));
                        jSONObject.put("CarNumber", rs.getString(7));
                        jSONObject.put("CarName", rs.getString(8));
                        jSONObject.put("CarColor", rs.getString(9));
                        jSONObject.put("CarNo", rs.getInt(10));
                        jSONObject.put("Status", rs.getString(11));
                        jSONObject.put("TimeTrips", rs.getString(12));
                        jSONObject.put("PhoneNumber", rs.getString(13));
                        jSONObject.put("tocken", tockenDriver);//add
                    }
                    jSONArray.add(1, jSONObject);//driver
                    setTockenDriver("driver" + driverId, tockenDriver);
                    rs.close();
                } catch (Exception e) {
                    System.out.println("SQLException2  Safari " + e.getMessage());
                    return null;
                }

                try {
                    JSONObject jSONObject = new JSONObject();
                    String query = "SELECT car_id, status_car FROM driver_car WHERE driver_id =  '" + driverId + "'";
                    PreparedStatement ps = conn.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    JSONArray array = new JSONArray();
                    while (rs.next()) {
                        JSONObject json = new JSONObject();
                        json.put("CarNumber", rs.getInt(1));
                        json.put("Status", rs.getString(2));
                        array.add(json);
                    }
                    jSONArray.add(2, array);
                    rs.close();
                } catch (Exception e) {
                    System.out.println("SQLException1  ..........." + e.getMessage());
                    return null;
                }

            } else {
                JSONObject jSONObject = new JSONObject();
                try {
                    String query = "SELECT version_driver, call_center, check_update_driver,"
                            + "(SELECT phone FROM driver where serial_number = '" + SerialNumber + "')as phone"
                            + " FROM version";
                    PreparedStatement ps = conn.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {//if we found phone:
                        jSONObject.put("AppDriver", rs.getString(1));
                        jSONObject.put("callCenter", rs.getString(2));
                        jSONObject.put("Cost", String.valueOf(5));
                        jSONObject.put("UpDateDriver", rs.getString(3));
                        jSONObject.put("Phone", rs.getString(4));
                        jSONObject.put("tocken", tockenDriver);//add
                    }
                    jSONArray.add(1, jSONObject);
                    rs.close();
                    setTockenDriver("driver" + driverId, tockenDriver);
                } catch (Exception e) {
                    System.out.println("SQLException3 Safari " + e.getMessage());
                    return null;
                }
            }
            conn.close();
        } catch (Exception e) {
            System.out.println("SQLException4 Safari " + e.getMessage());
        }
        return jSONArray;
    }

    //new service driver
    public static void sendTripApp(int OrderID, String driverId) {
        JSONObject json = new JSONObject();
        Encrypt encrypt = new Encrypt();
        String message = "NULL";
        try {
            Connection connection = ConnectionDB.getConnection();
            String query = "SELECT trip_id, passenger_id, start_latitude, start_longitude,"
                    + " end_latitude, end_longitude, start_location_name, end_location_name,"
                    + "trip_price, trip_distance, trip_note, driver_id, (SELECT phone FROM passenger WHERE passenger_id = trip.passenger_id)as phone, "
                    + "(SELECT km_price FROM car WHERE car_id = trip.trip_id)as km_price "
                    + ",service_id,(SELECT passenger_name FROM passenger WHERE passenger_id = trip.passenger_id)as passenger_name"
                    + ",(SELECT personal_image FROM passenger WHERE passenger_id = trip.passenger_id)as personal_image"
                    + ",(SELECT car_name FROM car WHERE car_id = trip.car_id)as car_name "
                    + ",(SELECT less_price FROM car WHERE car_id = trip.car_id)as less_price "
                    + ",(SELECT token FROM driver WHERE driver_id = trip.driver_id)as token "
                    + ",(SELECT open_price FROM car WHERE car_id = trip.car_id)as open_price "
                    + " FROM trip where trip_id = '" + OrderID + "'";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                json.put("OrderID", resultSet.getInt(1));
                json.put("CustomerID", resultSet.getInt(2));
                json.put("LatitudeBegin", resultSet.getDouble(3));
                json.put("LongitudeBegin", resultSet.getDouble(4));
                json.put("LatitudeEnd", resultSet.getDouble(5));
                json.put("LongitudeEnd", resultSet.getDouble(6));
                json.put("LocationNameBegin", resultSet.getString(7));
                json.put("LocationNameEnd", resultSet.getString(8));
                json.put("Price", resultSet.getDouble(9));
                json.put("Distance", resultSet.getDouble(10));
                json.put("Noting", resultSet.getString(11));
                json.put("DriverID", resultSet.getString(12));
                json.put("Phone", resultSet.getString(13));
                json.put("CostKm", resultSet.getInt(14));
                json.put("ServiceID", resultSet.getInt(15));
                json.put("CustomerName", resultSet.getString(16));
                json.put("ImageProFile", resultSet.getString(17));
                json.put("TaxiName", resultSet.getString(18));
                json.put("Rang", resultSet.getString(19));
                json.put("IPAddress", resultSet.getString(20));
                json.put("Cost", resultSet.getString(21));

                message = String.valueOf(json);
            }
            resultSet.close();
        } catch (Exception e) {
            message = "ERROR";
            System.out.println("sendTripApp Safari Ex " + e);
        }
        String event = "new-trip";
        sendEventDriver(driverId, message, event);
    }

    public String tripAccepted(int time, int tripId, int driverId, int passengerId) {
        String Result = "0";
        try {
            int Status = sqlGetInt("select status FROM trip where trip_id = '" + tripId + "'");
            int driverid = sqlGetInt("select driver_id FROM trip where trip_id = '" + tripId + "'");
            if (Status != 9 && driverid == driverId) {
                String query = "UPDATE trip SET status = '1', arrive_time = '" + time + "' WHERE status <>9 AND trip_id = '" + tripId + "' AND driver_id = '" + driverId + "'";
                if (sqlStatement(query)) {
                    Result = "1";
                    ControlPassenger controlPassenger = new ControlPassenger();
//                    controlPassenger.serviceTrip(tripId);
                    controlPassenger.threadSocket(tripId);//replase
                    timerTrips1(tripId, --time, 59);
                    controlPassenger.locationDriver(String.valueOf(passengerId), driverId, tripId);
                }
            } else if (driverid != driverId) {
                Result = "2";
            } else if (Status == 9) {
                Result = "3";
            }
        } catch (Exception e) {
            Result = "-1";
        }
        return Result;
    }

    public String driverAccess(int tripId) {
        String Result = "0";
        try {
            String query = "UPDATE trip SET status = '3' WHERE trip_id = '" + tripId + "'";
            if (sqlStatement(query)) {
                Result = "1";
                ControlPassenger controlPassenger = new ControlPassenger();
//                controlPassenger.serviceTrip(tripId);
                controlPassenger.threadSocket(tripId);//replase
                timerTrips2(tripId, 0, 0, 3);
            }
        } catch (Exception e) {
            Result = "-1";
        }
        return Result;
    }

    public String tripBegin(int tripId) {
        String Result = "0";
        try {
            String query = "UPDATE trip SET status = '5' WHERE trip_id = '" + tripId + "'";
            if (sqlStatement(query)) {
                Result = "1";
                ControlPassenger controlPassenger = new ControlPassenger();
//                controlPassenger.serviceTrip(tripId);
                controlPassenger.threadSocket(tripId);//replase
                timerTrips2(tripId, 0, 0, 5);
            }
        } catch (Exception e) {
            Result = "-1";
        }
        return Result;
    }

    public String tripEnd(int tripId, int driverId, double price, double Distance) {
        if (sqlStatement("UPDATE trip SET trip_price = '" + price + "', status = '7', trip_distance = '" + Distance + "' "
                + "WHERE trip_id = '" + tripId + "' AND driver_id = '" + driverId + "'")) {
            if (sqlStatement("UPDATE payment SET amount = amount -((" + price + " * 5)/100) WHERE driver_id ='" + driverId + "'")
                    && sqlStatement("UPDATE driver set status ='onLine' where driver_id = '" + driverId + "'")) {
                ControlPassenger controlPassenger = new ControlPassenger();
                controlPassenger.threadSocket(tripId);
                return "1";
            }
        }
        return "-1";
    }

    public static void threadSocket(String massege, String driverId, String event) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendEventDriver(driverId, massege, event);
                    sendPushNotification(massege, String.valueOf(driverId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static void sendPushNotification(String massege, String driverId) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("massege", massege);
            String token;
            Connection connection = ConnectionDB.getConnection();
            String query = "SELECT token_fierbase FROM driver WHERE driver_id = '" + driverId + "'";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                token = resultSet.getString(1);
                FcmClient fcmClient = new FcmClient();
                fcmClient.setAPIKey(Config.APIKeyDriver);
                EntityMessage entityMessage = new EntityMessage();
                entityMessage.addRegistrationToken(token);
                entityMessage.putStringData("driver", String.valueOf(jSONObject));
                FcmResponse fcmResponse = fcmClient.pushToEntities(entityMessage);
            }
            resultSet.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("fierBase Safari Ex " + e);
        }
    }

    public String cancelTrip(String driverId, String typeCancel, String tripId, String passengerId) {
        String Result = "2", massege = "", event = "";
        ControlPassenger controlPassenger = new ControlPassenger();
        if (Integer.parseInt(typeCancel) == 1) {//discount 3 bounds from driver account
            String carId = "", ServiceID = "", Price = "", LatitudeBegin = "", LongitudeBegin = "";
            // get data for canceled order to start up pocking :
            try {
                Connection conn;
                conn = ConnectionDB.getConnection();
                String query = "SELECT car_id, service_id, trip_price, start_latitude, start_longitude FROM trip "
                        + "WHERE trip_id = '" + tripId + "'";
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {//if we found phone:
                    carId = String.valueOf(rs.getInt(1));
                    ServiceID = String.valueOf(rs.getInt(2));
                    Price = String.valueOf(rs.getDouble(3));
                    LatitudeBegin = String.valueOf(rs.getDouble(4));
                    LongitudeBegin = String.valueOf(rs.getDouble(5));
                }
                rs.close();
                conn.close();
            } catch (Exception e) {
                System.out.println("CancelTrips Safari SQLException " + e);
                Result = "-1";
            }

            String query1 = "UPDATE payment SET amount = amount-15 WHERE driver_id = '" + driverId + "'";
            String query2 = "UPDATE trip SET driver_id = 0, status = 10,arrive_time = 0, time_count = 0 WHERE trip_id = '" + tripId + "'";
            String query3 = "UPDATE driver SET status = 'onLine' WHERE driver_id = '" + driverId + "'";
            if (sqlStatement(query1) && sqlStatement(query2) && sqlStatement(query3)) {
                //loop till serive this order:

                controlPassenger.newTrip(passengerId, driverId, Integer.parseInt(tripId), carId, ServiceID, Price, LatitudeBegin, LongitudeBegin);
                Result = "1";
                controlPassenger.serviceTrip(Integer.valueOf(tripId));
            }
        } else {
            if (sqlStatement("UPDATE trip SET status = 12 WHERE trip_id = '" + tripId + "'")
                    && sqlStatement("UPDATE driver SET status = 'onLine' WHERE driver_id = '" + driverId + "'")) {
                Result = "1";
                controlPassenger.serviceTrip(Integer.valueOf(tripId));
//                controlPassenger.threadSocket(Integer.valueOf(tripId));//replase
            }
        }
        SocketIOServerApp.addSocketIOClientStatusPassenger("statusApp" + passengerId, null);
        return Result;
    }

    public void upDateLocation(String driverId, double latitude, double longitude) {
        Encrypt encrypt = new Encrypt();
        String message = "";
        String event = "";
        if (sqlStatement("UPDATE driver SET latitude = '" + latitude + "', longitude = '" + longitude + "' WHERE driver_id = '" + driverId + "'")) {
            message = "successfully";
        } else {
            message = "Error";
        }
        event = "update-location";
        sendEventDriver(driverId, message, event);
    }

    public void upDateToken(int driverId, String token) {
        String query;
        query = "UPDATE driver SET token_fierbase = '" + token + "' WHERE driver_id = '" + driverId + "'";
        if (sqlStatement(query)) {
        }
    }

    public static void sendEventDriver(String driverId, String massege, String eventName) {
        try {
            Encrypt encrypt = new Encrypt();
            if (massege != null) {
                if (!massege.isEmpty()) {
                    String Result = encrypt.Encrypt(massege);
                    SocketIOClient iOClient = SocketIOServerApp.listSIOCDriver.get("driver" + driverId);
                    iOClient.sendEvent(eventName, Result);
                } else {
                    System.out.println("else Safari Driver IsEmpty");
                }
            } else {
                System.out.println("else Safari Driver Null");
            }
        } catch (Exception e) {
            System.out.println("sendEventDriver Safari EX " + e);
        }
    }

    public void setTockenDriver(String keyTocken, String tocken) {
        try {
            String tockenDriver = Config.listTockenDriver.get(keyTocken);
            if (tockenDriver == null) {
                Config.listTockenDriver.put(keyTocken, tocken);
            } else {
                Config.listTockenDriver.replace(keyTocken, tocken);
            }
        } catch (Exception e) {

        }
    }
}
