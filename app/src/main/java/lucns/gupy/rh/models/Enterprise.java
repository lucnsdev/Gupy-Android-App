package lucns.gupy.rh.models;

public class Enterprise {
    public int id;
    public String name, url;
    public Vacancy[] vacancies;

    public Enterprise(int id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }
}
