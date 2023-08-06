package fun.fishysparadise.mc.tables;

import com.j256.ormlite.field.DatabaseField;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "fpusers")
public class User {

    @Id
    @DatabaseField(unique = true, id = true)
    private Long id;
    @DatabaseField()
    private String uniqueId;
    @DatabaseField()
    private String mc_username;
    @DatabaseField()
    private String email;
    @DatabaseField()
    private Long dc_id;
    @DatabaseField()
    private String dc_username;
    @DatabaseField()
    private String dc_discriminator;
    @DatabaseField()
    private String dc_avatar;
    @DatabaseField()
    private Long dc_public_flags;
    @DatabaseField()
    private Long dc_flags;
    @DatabaseField()
    private String dc_locale;
    @DatabaseField()
    private boolean dc_mfa_enabled;
    @DatabaseField()
    private boolean whitelisted;
    @DatabaseField()
    private String access_code;
    @DatabaseField()
    private String home_world;
    @DatabaseField()
    private double home_x;
    @DatabaseField()
    private double home_y;
    @DatabaseField()
    private double home_z;
    @DatabaseField()
    private String death_world;
    @DatabaseField()
    private double death_x;
    @DatabaseField()
    private double death_y;
    @DatabaseField()
    private double death_z;

    public User() {}

    public String getUniqueId() {
        return uniqueId;
    }
    public String getUsername() {
        return mc_username;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
    public void setUsername(String username) {
        this.mc_username = username;
    }

    public String getHome_world() {
        return home_world;
    }

    public void setHome_world(String home_world) {
        this.home_world = home_world;
    }

    public double getHome_x() {
        return home_x;
    }

    public void setHome_x(double home_x) {
        this.home_x = home_x;
    }

    public double getHome_y() {
        return home_y;
    }

    public void setHome_y(double home_y) {
        this.home_y = home_y;
    }

    public double getHome_z() {
        return home_z;
    }

    public void setHome_z(double home_z) {
        this.home_z = home_z;
    }

}
