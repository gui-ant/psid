package grp07;

public class User {
    private final long id;
    private String email;
    private String name;
    private int role; // TODO

    public User(long id) {
        this.id = id;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }


    // TODO - APENAS PARA TESTE, APAGAR!!!
    public void setRole(int r) {
        this.role = r;
    }
}
