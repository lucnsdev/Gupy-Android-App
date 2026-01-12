package lucns.gupy.rh.models;

public class Locality {
    public String state, city, country;
    public int id;

    public Locality() {}

    public Locality(String country, String state, String city) {
        this.country = country;
        this.state = state;
        this.city = city;
    }
}
