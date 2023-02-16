package net.fishysparadise.mc.entities;

import com.j256.ormlite.field.DatabaseField;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity(name = "users")
public class User {

    @Id
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField()
    private String uniqueId;

    @DatabaseField()
    private String username;

    @OneToOne
    @JoinColumn(name = "home_id")
    private Home home;

    public User() {}

    public Home getHome() { return home; }

    public int getId() { return id; }

    public String getUniqueId() { return uniqueId; }

    public String getUsername() { return username; }

    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public void setUsername(String username) { this.username = username; }

    public void setHome(Home home) {
        this.home = home;
    }


}
