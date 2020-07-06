/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ammar.sd.programming.control;

import ammar.sd.programming.dbops.Config;
import ammar.sd.programming.dbops.ControlDriver;
import ammar.sd.programming.dbops.SocketIOServerApp;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Ammar Abbas
 */
//@MultipartConfig
//@ServletSecurity(
//@HttpConstraint(rolesAllowed = {"dalas"}))
public class Driver extends HttpServlet {

    ControlDriver controlDriver = new ControlDriver();
    SocketIOServerApp iOServerApp = new SocketIOServerApp();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String tocken = request.getHeader("Authorization");

        try {
//https://safari-sd.com:8443/safari/Driver?action=startSocketIO
            String reqcode = request.getParameter("action");
            if (reqcode.equalsIgnoreCase("getPhoneNumber.php")) {
                response.getWriter().write(controlDriver.getPhoneNumber(request.getParameter("SerialNumber")));
            } else if (reqcode.equalsIgnoreCase("getforbiddenPhoneNumber.php")) {
                response.getWriter().write(controlDriver.getforbiddenPhoneNumber(request.getParameter("PhoneNumber")));
            } else if (reqcode.equalsIgnoreCase("getCheckVersion.php")) {
//                response.getWriter().write(controlDriver.getCheckVersion());
            } else if (reqcode.equalsIgnoreCase("getTypeServices.php")) {
                response.getWriter().write(controlDriver.getTypeServices(request.getParameter("PhoneNumber")));
            } else if (reqcode.equalsIgnoreCase("signUpDriver")) {
                response.getWriter().write(controlDriver.signUpDriver(request.getParameter("Phone"),
                        request.getParameter("SerialNumber")).toString());
            } else if (reqcode.equalsIgnoreCase("insertRate.php")) {
                response.getWriter().write(controlDriver.insertRate(Integer.parseInt(request.getParameter("DriverID")),
                        Double.parseDouble(request.getParameter("ratingBar"))));
            } else if (reqcode.equalsIgnoreCase("ListOrder.php")) {
                String driverId = request.getParameter("driverId");
                if (tocken.equals(getTockenDriver("driver" + driverId))) {
                    response.setContentType("application/json");
                    response.getWriter().write(controlDriver.ListOrder(Integer.parseInt(request.getParameter("driverId"))).toString());
                }
            } else if (reqcode.equalsIgnoreCase("OrderDelete.php")) {
                response.getWriter().write(controlDriver.OrderDelete(Integer.parseInt(request.getParameter("DriverID")), Integer.parseInt(request.getParameter("OrderDeleteID"))));
            } else if (reqcode.equalsIgnoreCase("SelectAccount.php")) {
                String driverId = request.getParameter("driverId");
                if (tocken.equals(getTockenDriver("driver" + driverId))) {
                    response.setContentType("application/json");
                    response.getWriter().write(controlDriver.SelectAccount(request.getParameter("driverId")).toString());
                }
            } else if (reqcode.equalsIgnoreCase("SelectFreeCar.php")) {
                String driverId = request.getParameter("driverId");
                if (tocken.equals(getTockenDriver("driver" + driverId))) {
                    response.setContentType("application/json");
                    response.getWriter().write(controlDriver.SelectFreeCar(request.getParameter("driverId")).toString());
                }
            } else if (reqcode.equalsIgnoreCase("ServicesOrder.php")) {
                response.setContentType("application/json");
//                response.getWriter().write(controlDriver.ServicesOrder(request.getParameter("DriverID")).toString());
            } else if (reqcode.equalsIgnoreCase("UpDateAccount.php")) {
                response.getWriter().write(controlDriver.UpDateAccount(Integer.parseInt(request.getParameter("DriverID")),
                        Double.parseDouble(request.getParameter("Proportion"))).toString());
            } else if (reqcode.equalsIgnoreCase("UpDateAccountOrder.php")) {
                response.getWriter().write(controlDriver.UpDateAccountOrder(Integer.parseInt(request.getParameter("OrderID")),
                        Integer.parseInt(request.getParameter("DriverID")),
                        request.getParameter("Status"),
                        Double.parseDouble(request.getParameter("Price")),
                        Double.parseDouble(request.getParameter("Distance"))));
            } else if (reqcode.equalsIgnoreCase("UpDateOnLineOffLine.php")) {
                String driverId = request.getParameter("driverId");
                if (tocken.equals(getTockenDriver("driver" + driverId))) {
                    response.getWriter().write(controlDriver.UpDateOnLineOffLine(Integer.parseInt(request.getParameter("driverId")),
                            request.getParameter("Status"),
                            request.getParameter("CarNumber"),
                            Integer.parseInt(request.getParameter("Table"))));
                }
            } else if (reqcode.equalsIgnoreCase("UpDateTime.php")) {
                response.getWriter().write(controlDriver.UpDateTime(Integer.parseInt(request.getParameter("OrderID")),
                        Integer.parseInt(request.getParameter("DriverID")),
                        Integer.parseInt(request.getParameter("Time"))));
            } else if (reqcode.equalsIgnoreCase("UploadProFile.php")) {
                String driverId = request.getParameter("driverId");
                if (tocken.equals(getTockenDriver("driver" + driverId))) {
                    response.getWriter().write(controlDriver.UploadProFile(request.getParameter("driverId"), request));
                }
            } else if (reqcode.equalsIgnoreCase("CancelTrips.php")) {
                response.getWriter().write(controlDriver.CancelTrips(request.getParameter("DriverID"),
                        request.getParameter("typeTrip"), request.getParameter("OrderID")));
            } else if (reqcode.equalsIgnoreCase("getDataDriver.php")) {
                response.setContentType("application/json");
                response.getWriter().write(controlDriver.getDataDriver(request.getParameter("SerialNumber"),
                        request.getParameter("DriverID"),
                        request.getParameter("flag"),
                        request.getParameter("OrderID"),
                        request.getParameter("PhoneNumber"),
                        request.getParameter("IPAddress")).toString());
            } else if (reqcode.equalsIgnoreCase("UpDateIPAddress.php")) {
                response.getWriter().write(controlDriver.UpDateIPAddress(request.getParameter("DriverID"),
                        request.getParameter("IPAddress")).toString());
                //https://safari-sd.com:8443/safari/Driver?action=startSocketIO
            } else if (reqcode.equalsIgnoreCase("startSocketIO")) {
                response.getWriter().write(iOServerApp.startSocketIOServer());
            } else if (reqcode.equalsIgnoreCase("tripAccepted")) {
                String driverId = request.getParameter("driverId");
                if (tocken.equals(getTockenDriver("driver" + driverId))) {
                    response.getWriter().write(controlDriver.tripAccepted(Integer.parseInt(request.getParameter("time")),
                            Integer.parseInt(request.getParameter("tripId")),
                            Integer.parseInt(request.getParameter("driverId")),
                            Integer.parseInt(request.getParameter("passengerId"))));
                }
            } else if (reqcode.equalsIgnoreCase("driverAccess")) {
                String driverId = request.getParameter("driverId");
                if (tocken.equals(getTockenDriver("driver" + driverId))) {
                    response.getWriter().write(controlDriver.driverAccess(Integer.parseInt(request.getParameter("tripId"))));
                }
            } else if (reqcode.equalsIgnoreCase("tripBegin")) {
                String driverId = request.getParameter("driverId");
                if (tocken.equals(getTockenDriver("driver" + driverId))) {
                    response.getWriter().write(controlDriver.tripBegin(Integer.parseInt(request.getParameter("tripId"))));
                }
            } else if (reqcode.equalsIgnoreCase("tripEnd")) {
                String driverId = request.getParameter("driverId");
                if (tocken.equals(getTockenDriver("driver" + driverId))) {
                    response.getWriter().write(controlDriver.tripEnd(Integer.parseInt(request.getParameter("tripId")),
                            Integer.parseInt(request.getParameter("driverId")),
                            Double.parseDouble(request.getParameter("price")),
                            Double.parseDouble(request.getParameter("distance"))));
                }
            } else if (reqcode.equalsIgnoreCase("cancelTrip")) {
                String driverId = request.getParameter("driverId");
                if (tocken.equals(getTockenDriver("driver" + driverId))) {
                    response.getWriter().write(controlDriver.cancelTrip(request.getParameter("driverId"),
                            request.getParameter("typeCancel"),
                            request.getParameter("tripId"),
                            request.getParameter("passengerId")));
                }
            }else if (reqcode.equalsIgnoreCase("upLoadImage")) {
                    response.getWriter().write(controlDriver.UploadProFile(request.getParameter("driverId"), request));
            }
        } catch (Exception ex) {//
            System.out.println("Dri Safari error code 001d : " + ex.getMessage());
        }
    }

    public static String getTockenDriver(String keyTocken) {
        try {
            String tockenDriver = Config.listTockenDriver.get(keyTocken);
            return tockenDriver;
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
