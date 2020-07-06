/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ammar.sd.programming.dbops;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;
import static java.lang.System.out;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Ammar Abbas
 */
public class SocketIOServerApp {

    private static int port = 2020;
    private static SocketIOServer iOServer;
    public static Map<String, SocketIOClient> listSIOCPassenger = new HashMap();
    public static Map<String, SocketIOClient> listSIOCDriver = new HashMap();
    public static Map<String, SocketIOClient> listSIOCStatusPassenger = new HashMap();

    public String startSocketIOServer() {
        try {
            socketIOServer();
            return "1";
        } catch (Exception e) {
            return "0";
        }
    }

    private void socketIOServer() throws InterruptedException, UnsupportedEncodingException {
        Configuration configuration = new Configuration();
        configuration.setPort(port);
        iOServer = new SocketIOServer(configuration);
        System.out.println("Server Safari Is Running...");
        
        iOServer.addConnectListener((sioc) -> {
            System.out.println("Safari ConnectListener... " + sioc);
        });

        iOServer.addDisconnectListener((sioc) -> {
            System.out.println("Safari DisconnectListener... " + sioc);
//            ControlDriver controlDriver = new ControlDriver();
//            controlDriver.updateStatusDriver(String.valueOf(sioc));
        });

        iOServer.addEventListener("Passenger", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient iOClient, String Response, AckRequest ackRequest) {
                try {
                    controlPassenger(Response, iOClient);
                } catch (Exception e) {
                    System.out.println("Exception Safari Passenger " + e);
                    Logger.getLogger(SocketIOServer.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        });

        iOServer.addEventListener("Driver", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient iOClient, String Response, AckRequest ackRequest) {
                try {
                    controlDriver(Response, iOClient);
                } catch (Exception e) {
                    System.out.println("Exception Safari Driver " + e);
                    Logger.getLogger(SocketIOServer.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        });

        System.out.println("Project version 3 Listener .... ");
        iOServer.start();
        Thread.sleep(Integer.MAX_VALUE);
        iOServer.stop();
    }

    public void addSocketIOClientPassenger(String keySocket, SocketIOClient socket) {
        SocketIOClient iOClient = listSIOCPassenger.get(keySocket);
        if (iOClient == null) {
            listSIOCPassenger.put(keySocket, socket);
        } else {
            listSIOCPassenger.replace(keySocket, socket);
        }
    }

    public static void addSocketIOClientStatusPassenger(String keySocket, SocketIOClient socket) {
        SocketIOClient iOClient = listSIOCStatusPassenger.get(keySocket);
        if (iOClient == null) {
            listSIOCStatusPassenger.put(keySocket, socket);
        } else {
            listSIOCStatusPassenger.replace(keySocket, socket);
        }
    }

    private void controlPassenger(String Response, SocketIOClient iOClient) {
        Encrypt encrypt = new Encrypt();
        ControlPassenger controlPassenger = new ControlPassenger();
        try {
            if (Response != null) {
                if (!Response.isEmpty()) {
                    String ResponseEncrypt = encrypt.Decrypt(Response);
                    JSONObject jSONObject = new JSONObject(ResponseEncrypt);
                    String passengerId = jSONObject.getString("passengerId");
                    String functionName = jSONObject.getString("functionName");
                    String statusApp = jSONObject.getString("statusApp");
                    addSocketIOClientPassenger("passenger" + passengerId, iOClient);
                    if (statusApp.equals("onLine")) {
                        addSocketIOClientStatusPassenger("statusApp" + passengerId, iOClient);
                    } else {
                        addSocketIOClientStatusPassenger("statusApp" + passengerId, null);
                    }
                    if (functionName.equals("availableCaptin")) {
                        String latitude = jSONObject.getString("latitude");
                        String longitude = jSONObject.getString("longitude");
                        controlPassenger.availableCaptin(passengerId, Double.parseDouble(latitude), Double.parseDouble(longitude));
                    } else if (functionName.equals("token")) {
                        String token = jSONObject.getString("token");
                        controlPassenger.upDateToken(Integer.valueOf(passengerId), token);
                    } else if (functionName.equals("cancelTrip")) {
                        String tripId = jSONObject.getString("tripId");
                        String driverId = jSONObject.getString("driverId");
                    } else if (functionName.equals("locationDriver")) {
                        String driverId = jSONObject.getString("driverId");
                        String tripId = jSONObject.getString("tripId");
                        controlPassenger.locationDriver(passengerId, Integer.valueOf(driverId), Integer.valueOf(tripId));
                    }
                } else {
                    System.out.println("Else Safari Passenger IsEmpty");
                }
            } else {
                System.out.println("Else Safari Passenger Null");
            }
        } catch (Exception e) {
            System.out.println("controlPassenger Safari EX " + e);
        }
    }

    public void addSocketIOClientDriver(String driverId, String keySocket, SocketIOClient socket) {
        SocketIOClient iOClient = listSIOCDriver.get(keySocket);
        if (iOClient == null) {
            listSIOCDriver.put(keySocket, socket);
        } else {
            listSIOCDriver.replace(keySocket, socket);
        }

        ControlDriver controlDriver = new ControlDriver();
        controlDriver.updateSocket(driverId, String.valueOf(socket));
    }

    private void controlDriver(String Response, SocketIOClient iOClient) {
        Encrypt encrypt = new Encrypt();
        ControlDriver controlDriver = new ControlDriver();
        try {
            if (Response != null) {
                if (!Response.isEmpty()) {
                    String ResponseEncrypt = encrypt.Decrypt(Response);
                    JSONObject jSONObject = new JSONObject(ResponseEncrypt);
                    String functionName = jSONObject.getString("functionName");
                    String driverId = jSONObject.getString("driverId");
                    addSocketIOClientDriver(driverId, "driver" + driverId, iOClient);
                    if (functionName.equals("upDateLocation")) {
                        String latitude = jSONObject.getString("latitude");
                        String longitude = jSONObject.getString("longitude");
                        controlDriver.upDateLocation(driverId, Double.parseDouble(latitude), Double.parseDouble(longitude));
                    } else if (functionName.equals("token")) {
                        String token = jSONObject.getString("token");
                        controlDriver.upDateToken(Integer.valueOf(driverId), token);
                    } else if (functionName.equals("acceptTrip")) {
                        String time = jSONObject.getString("time");
                        String tripId = jSONObject.getString("tripId");
                        String passengerId = jSONObject.getString("passengerId");
                        String Result = controlDriver.tripAccepted(Integer.valueOf(time), Integer.valueOf(tripId),
                                Integer.valueOf(driverId), Integer.valueOf(passengerId));
                    } else if (functionName.equals("accessDriver")) {
                        String tripId = jSONObject.getString("tripId");
                        String Result = controlDriver.driverAccess(Integer.valueOf(tripId));
                    } else if (functionName.equals("beginTrip")) {
                        String tripId = jSONObject.getString("tripId");
                        String Result = controlDriver.tripBegin(Integer.valueOf(tripId));
                    } else if (functionName.equals("endTrip")) {
                        String tripId = jSONObject.getString("tripId");
                        String price = jSONObject.getString("price");
                        String distance = jSONObject.getString("distance");
                        String Result = controlDriver.tripEnd(Integer.valueOf(tripId), Integer.valueOf(driverId),
                                Double.parseDouble(price), Double.parseDouble(distance));
                    } else if (functionName.equals("cancelTrip")) {
                        String tripId = jSONObject.getString("tripId");
                        String typeCancel = jSONObject.getString("typeCancel");
                        String passengerId = jSONObject.getString("passengerId");
                        String Result = controlDriver.cancelTrip(driverId, typeCancel, tripId, passengerId);
                    }
                } else {
                    System.out.println("Else Safari Driver IsEmpty");
                }
            } else {
                System.out.println("Else Safari Driver Null");
            }
        } catch (Exception e) {
            System.out.println("controlDriver Safari EX " + e);
        }
    }
}
