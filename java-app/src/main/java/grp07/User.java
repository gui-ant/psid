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

    public int getRole() {
        return role;
    }

    public boolean isEqual(User u) {
        if(id == u.getId() && email.equals(u.getEmail()) && name.equals(u.getName()) && role == u.getRole()) {
            return true;
        }
        else {
            return false;
        }
    }


    // TODO - APENAS PARA TESTE, APAGAR!!!
    public void setRole(int r) {
        this.role = r;
    }
}
