
package ammar.sd.programming.calc;

/**
 *
 * @author Ammar Abbas
 */
import java.io.Serializable;
import java.util.Objects;

/** A place on Earth, represented by a latitude/longitude pair. */
public class LatLng implements Serializable {

  private static final long serialVersionUID = 1L;

  /** The latitude of this location. */
  public double latitude;

  /** The longitude of this location. */
  public double longitude;

  /**
   * Constructs a location with a latitude/longitude pair.
   *
   * @param latitude The latitude of this location.
   * @param longitude The longitude of this location.
   */
  public LatLng(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public LatLng() {}




  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LatLng latLng = (LatLng) o;
    return Double.compare(latLng.latitude, latitude) == 0 && Double.compare(latLng.longitude, longitude) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitude, longitude);
  }
}