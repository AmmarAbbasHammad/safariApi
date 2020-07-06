/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ammar.sd.programming.control;

import ammar.sd.programming.dbops.SMSHelper;
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
public class SmsGate extends HttpServlet {

  
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       request.setCharacterEncoding("UTF-8");
       response.setCharacterEncoding("UTF-8");
       try {
           String PhoneNumber = request.getParameter("PhoneNumber");
           String SMS = request.getParameter("SMS");
           SMSHelper.sendSMS(PhoneNumber, SMS);
       } catch (NullPointerException ex) {//
            System.out.println("error code smsGate : " + ex.getMessage());
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
