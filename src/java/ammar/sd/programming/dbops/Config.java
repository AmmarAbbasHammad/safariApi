/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ammar.sd.programming.dbops;

import com.corundumstudio.socketio.SocketIOClient;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ammar Abbas
 */
public class Config {

    public static String APIKeyPassenger = "AAAA-co3w-A:APA91bF-EycFxaFyasM2GsvtH01SlMzxKkVf0PZtCb4kWMgEjUjLy1KxJ6H3YIklpY9mUXEAKjsKffekH7nXG6G3oOdQrXl28PnPU34eb6m16sGJlkbZz3Cp56z3NepQJJS9pGn2vOLi";
    public static String APIKeyDriver = "AAAA-co3w-A:APA91bF-EycFxaFyasM2GsvtH01SlMzxKkVf0PZtCb4kWMgEjUjLy1KxJ6H3YIklpY9mUXEAKjsKffekH7nXG6G3oOdQrXl28PnPU34eb6m16sGJlkbZz3Cp56z3NepQJJS9pGn2vOLi";

    public static Map<String, String> listTockenPassenger = new HashMap();
    public static Map<String, String> listTockenDriver = new HashMap();
    
    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    public static String generateToken() {
        byte[] randomBytes = new byte[512];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
