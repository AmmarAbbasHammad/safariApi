/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ammar.sd.programming.calc;

/**
 *
 * @author Ammar Abbas
 */
public class Place{
    public String driverid;
    public LatLng latlng;

    public Place(String driverId, LatLng latlng) {
        this.driverid = driverId;
        this.latlng = latlng;
    }
}
