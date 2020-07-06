package ammar.sd.programming.control;

import ammar.sd.programming.dbops.Config;
import ammar.sd.programming.dbops.ControlPassenger;
import ammar.sd.programming.dbops.SocketIOServerApp;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Ammar Abbas
 */
//@ServletSecurity(
//@HttpConstraint(rolesAllowed = {"dalas"}))
public class Passenger extends HttpServlet {

    ControlPassenger controlPassenger = new ControlPassenger();
    SocketIOServerApp iOServerPassenger = new SocketIOServerApp();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try {
            String reqcode = request.getParameter("action");
            String tocken = request.getHeader("Authorization");

            if (reqcode.equalsIgnoreCase("getPhoneNumber.php")) {
                response.getWriter().write(controlPassenger.getPhoneNumber(request.getParameter("SerialNumber")));
            } else if (reqcode.equalsIgnoreCase("getforbiddenPhoneNumber.php")) {
                response.getWriter().write(controlPassenger.getforbiddenPhoneNumber(request.getParameter("PhoneNumber")));
            } else if (reqcode.equalsIgnoreCase("SelectCustomers.php")) {
                response.setContentType("application/json");
                String passengerId = request.getParameter("CustomerID");
                if (tocken.equals(getTockenPassenger("passenger" + passengerId))) {
                    response.getWriter().write(controlPassenger.SelectCustomers(request.getParameter("CustomerID"),
                            request.getParameter("OrderID")).toString());
                }
            } else if (reqcode.equalsIgnoreCase("UpDateCustomers.php")) {
                String passengerId = request.getParameter("CustomerID");
                if (tocken.equals(getTockenPassenger("passenger" + passengerId))) {
                    response.getWriter().write(controlPassenger.UpDateCustomers(request.getParameter("CustomerID"),
                            request.getParameter("CustomerName"),
                            request.getParameter("Email")).toString());
                }
            } else if (reqcode.equalsIgnoreCase("UpDatePhone.php")) {
                response.getWriter().write(controlPassenger.UpDatePhone(request.getParameter("CustomerID"),
                        request.getParameter("PhoneNew")));
            } else if (reqcode.equalsIgnoreCase("UploadProFile.php")) {
                String passengerId = request.getParameter("CustomerID");
                if (tocken.equals(getTockenPassenger("passenger" + passengerId))) {
                    response.getWriter().write(controlPassenger.UploadProFile(request.getParameter("CustomerID"), request));
                }
            } else if (reqcode.equalsIgnoreCase("CreateOrder.php")) {
                String passengerId = request.getParameter("CustomerID");
                if (tocken.equals(getTockenPassenger("passenger" + passengerId))) {
                    response.getWriter().write(controlPassenger.CreateOrder(request.getParameter("CarID"),
                            request.getParameter("CustomerID"),
                            request.getParameter("LatitudeBegin"),
                            request.getParameter("LongitudeBegin"),
                            request.getParameter("LatitudeEnd"),
                            request.getParameter("LongitudeEnd"),
                            request.getParameter("LocationNameBegin"),
                            request.getParameter("LocationNameEnd"),
                            request.getParameter("Cost"),
                            request.getParameter("Distance"),
                            request.getParameter("ServiceID"),
                            request.getParameter("Noting")));
                }
            } else if (reqcode.equalsIgnoreCase("signUpPassenger")) {
                response.getWriter().write(controlPassenger.signUpPassenger(request.getParameter("Phone"),
                        request.getParameter("SerialNumber")).toString());
            } else if (reqcode.equalsIgnoreCase("insertRate.php")) {
                String passengerId = request.getParameter("passengerId");
                if (tocken.equals(getTockenPassenger("passenger" + passengerId))) {
                    response.getWriter().write(controlPassenger.insertRate(Integer.parseInt(request.getParameter("DriverID")),
                            Double.parseDouble(request.getParameter("ratingBar")),
                            request.getParameter("passengerId")));
                }
            } else if (reqcode.equalsIgnoreCase("ListOrder.php")) {
                response.setContentType("application/json");
                String passengerId = request.getParameter("CustomerID");
                if (tocken.equals(getTockenPassenger("passenger" + passengerId))) {
                    response.getWriter().write(controlPassenger.ListOrder(Integer.parseInt(request.getParameter("CustomerID"))).toString());
                }
            } else if (reqcode.equalsIgnoreCase("OrderDelete.php")) {//DeleteImage
                String passengerId = request.getParameter("CustomerID");
                if (tocken.equals(getTockenPassenger("passenger" + passengerId))) {
                    response.getWriter().write(controlPassenger.OrderDelete(Integer.parseInt(request.getParameter("CustomerID")),
                            Integer.parseInt(request.getParameter("OrderDeleteID")),
                            request.getParameter("tocken")));
                }
            } else if (reqcode.equalsIgnoreCase("DeleteImage.php")) {
                String passengerId = request.getParameter("CustomerID");
                if (tocken.equals(getTockenPassenger("passenger" + passengerId))) {
                    response.getWriter().write(controlPassenger.DeleteImage(request.getParameter("CustomerID"),
                            request.getParameter("tocken")));
                }
            } else if (reqcode.equalsIgnoreCase("CancelOrder.php")) {
                response.getWriter().write(controlPassenger.CancelOrder(Integer.parseInt(request.getParameter("CustomerID")),
                        Integer.parseInt(request.getParameter("OrderID")),
                        Integer.parseInt(request.getParameter("Status"))));
            } else if (reqcode.equalsIgnoreCase("getDataCustomer.php")) {
                response.getWriter().write(controlPassenger.getDataCustomer(request.getParameter("SerialNumber"),
                        request.getParameter("CustomerID"),
                        request.getParameter("flag"),
                        request.getParameter("OrderID"),
                        request.getParameter("PhoneNumber")).toString());
            } else if (reqcode.equalsIgnoreCase("UpDateIPAddress.php")) {
                response.getWriter().write(controlPassenger.UpDateIPAddress(request.getParameter("CustomerID"),
                        request.getParameter("IPAddress")).toString());
            } else if (reqcode.equalsIgnoreCase("StartSocket.php")) {
//                response.getWriter().write(controlPassenger.StartSocket());
            } else if (reqcode.equalsIgnoreCase("startSocketIO")) {
                response.getWriter().write(iOServerPassenger.startSocketIOServer());
            } else if (reqcode.equalsIgnoreCase("cancelTrip")) {
                String passengerId = request.getParameter("passengerId");
                if (tocken.equals(getTockenPassenger("passenger" + passengerId))) {
                    response.getWriter().write(controlPassenger.cancelTrip(Integer.parseInt(request.getParameter("passengerId")),
                            Integer.parseInt(request.getParameter("tripId")),
                            request.getParameter("driverId")));
                }
            }
        } catch (Exception ex) {
            System.out.println("Passenger Safari error code 001 " + ex.getMessage());
        }
    }

    public static String getTockenPassenger(String keyTocken) {
        try {
            String tockenPassenger = Config.listTockenPassenger.get(keyTocken);
            return tockenPassenger;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
