package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.OrderColumn;
import java.util.List;

@Data
@Embeddable
public class BE_R2 {

    @ElementCollection
    @JsonProperty("X")
    @OrderColumn(name = "x_index")
    private List<Double> x1;

    @ElementCollection
    @JsonProperty("Y")
    @OrderColumn(name = "y_index")
    private List<Double> y1;

    @ElementCollection
    @JsonProperty("Z")
    @OrderColumn(name = "z_index")
    private List<Double> z1;


    @ElementCollection
    @JsonProperty("X_Fond")
    @OrderColumn(name = "x_Fond_index")
    private List<String> xFond1;

    @ElementCollection
    @JsonProperty("Y_Fond")
    @OrderColumn(name = "y_Fond_index")
    private List<String> yFond1;


    @ElementCollection
    @JsonProperty("Z_Fond")
    @OrderColumn(name = "z_Fond_index")
    private List<String> zFond1;

    public  BE_R2(){}

    public List<Double> getX1() {
        return x1;
    }

    public void setX1(List<Double> x1) {
        this.x1 = x1;
    }

    public List<Double> getY1() {
        return y1;
    }

    public void setY1(List<Double> y1) {
        this.y1 = y1;
    }

    public List<Double> getZ1() {
        return z1;
    }

    public void setZ1(List<Double> z1) {
        this.z1 = z1;
    }

    public List<String> getxFond1() {
        return xFond1;
    }

    public void setxFond1(List<String> xFond1) {
        this.xFond1 = xFond1;
    }

    public List<String> getyFond1() {
        return yFond1;
    }

    public void setyFond1(List<String> yFond1) {
        this.yFond1 = yFond1;
    }

    public List<String> getzFond1() {
        return zFond1;
    }

    public void setzFond1(List<String> zFond1) {
        this.zFond1 = zFond1;
    }
}
