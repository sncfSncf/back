package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Embeddable
public class BE_R1 {

    @ElementCollection
    @JsonProperty("X")
    @OrderColumn(name = "x_index")
    private List<Double> x;

    @ElementCollection
    @JsonProperty("Y")
    @OrderColumn(name = "y_index")
    private List<Double> y;

    @ElementCollection
    @JsonProperty("Z")
    @OrderColumn(name = "z_index")
    private List<Double> z;


    @ElementCollection
    @JsonProperty("X_Fond")
    @OrderColumn(name = "x_Fond_index")
    private List<String> xFond;

    @ElementCollection
    @JsonProperty("Y_Fond")
    @OrderColumn(name = "y_Fond_index")
    private List<String> yFond;


    @ElementCollection
    @JsonProperty("Z_Fond")
    @OrderColumn(name = "Z_Fond_index")
    private List<String> zFond;

    public BE_R1(List<Double> x, List<Double> y, List<Double> z, List<String> xFond, List<String> yFond, List<String> zFond) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xFond = xFond;
        this.yFond = yFond;
        this.zFond = zFond;
    }

    public  BE_R1(){}


    public List<Double> getX() {
        return x;
    }

    public void setX(List<Double> x) {
        this.x = x;
    }

    public List<Double> getY() {
        return y;
    }

    public void setY(List<Double> y) {
        this.y = y;
    }

    public List<Double> getZ() {
        return z;
    }

    public void setZ(List<Double> z) {
        this.z = z;
    }

    public List<String> getxFond() {
        return xFond;
    }

    public void setxFond(List<String> xFond) {
        this.xFond = xFond;
    }

    public List<String> getyFond() {
        return yFond;
    }

    public void setyFond(List<String> yFond) {
        this.yFond = yFond;
    }

    public List<String> getzFond() {
        return zFond;
    }

    public void setzFond(List<String> zFond) {
        this.zFond = zFond;
    }
}
