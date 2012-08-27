package cool.sm.khaloopp.data.entity;

import javax.persistence.Entity;

@Entity
public class KhalooppoEntity extends NotifierTarget{

    protected long number;
    protected String link;
    protected String title;
    protected String khaloopper;
    protected String yaHachuuu;
    protected String location;
    protected String description;
    protected String contact;
    protected String posted;

    public long getNumber() {
        return this.number;
    }
    public void setNumber(long number) {
        this.number = number;
    }
    public String getLink() {
        return this.link;
    }
    public void setLink(String idref) {
        this.link = idref;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getYaHachuuu() {
        return this.yaHachuuu;
    }
    public void setYaHachuuu(String yaHachuuu) {
        this.yaHachuuu = yaHachuuu;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getContact() {
        return this.contact;
    }
    public void setContact(String contact) {
        this.contact = contact;
    }
    public String getKhaloopper() {
        return this.khaloopper;
    }
    public void setKhaloopper(String khaloopper) {
        this.khaloopper = khaloopper;
    }
    public String getLocation() {
        return this.location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getPosted() {
        return this.posted;
    }
    public void setPosted(String posted) {
        this.posted = posted;
    }
}