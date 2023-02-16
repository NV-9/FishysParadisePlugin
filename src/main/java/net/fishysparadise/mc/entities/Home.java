package net.fishysparadise.mc.entities;

import com.j256.ormlite.field.DatabaseField;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity(name = "home")
public class Home {

    @Id
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField()
    private String world;

    @DatabaseField()
    private double x;

    @DatabaseField()
    private double y;

    @DatabaseField()
    private double z;

    @OneToOne(mappedBy = "users")
    private User user;

    public Home() {}

    public Home(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Home(double x, double y, double z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public void setCoordinates(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public User getUser() { return user; }

    public String getWorld() { return world; }

    public int getId() { return id; }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setWorld(String world) { this.world = world; }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

}
