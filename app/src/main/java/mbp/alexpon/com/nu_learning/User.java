package mbp.alexpon.com.nu_learning;

/**
 * Created by apple on 15/8/19.
 */
public class User {
    private String name, username, password, department, email;

    public User(String name, String username, String password, String department, String email){
        this.name = name;
        this.username = username;
        this.password = password;
        this.department = department;
        this.email = email;
    }

    public User(String username, String password){
        this.name = "";
        this.username = username;
        this.password = password;
        this.department = "";
        this.email = "";
    }

    public String getName(){
        return name;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public String getDepartment(){
        return department;
    }

    public String getEmail(){
        return email;
    }

}
