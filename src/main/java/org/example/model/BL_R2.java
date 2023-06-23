package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.OrderColumn;
import java.util.List;

@Data
@Embeddable
public class BL_R2 {

    @ElementCollection
    @JsonProperty("X")
    @OrderColumn(name = "x_index")
    private List<Double> xl2;

    @ElementCollection
    @JsonProperty("Y")
    @OrderColumn(name = "y_index")
    private List<Double> yl2;

    @ElementCollection
    @JsonProperty("Z")
    @OrderColumn(name = "z_index")
    private List<Double> zl2;


    @ElementCollection
    @JsonProperty("X_Fond")
    @OrderColumn(name = "x_Fond_index")
    private List<String> xFondl2;

    @ElementCollection
    @JsonProperty("Y_Fond")
    @OrderColumn(name = "y_Fond_index")
    private List<String> yFondl2;


    @ElementCollection
    @JsonProperty("Z_Fond")
    @OrderColumn(name = "z_Fond_index")
    private List<String> zFondl2;

    public  BL_R2(){}

    public List<Double> getXl2() {
        return xl2;
    }

    public void setXl2(List<Double> xl2) {
        this.xl2 = xl2;
    }

    public List<Double> getYl2() {
        return yl2;
    }

    public void setYl2(List<Double> yl2) {
        this.yl2 = yl2;
    }

    public List<Double> getZl2() {
        return zl2;
    }

    public void setZl2(List<Double> zl2) {
        this.zl2 = zl2;
    }

    public List<String> getxFondl2() {
        return xFondl2;
    }

    public void setxFondl2(List<String> xFondl2) {
        this.xFondl2 = xFondl2;
    }

    public List<String> getyFondl2() {
        return yFondl2;
    }

    public void setyFondl2(List<String> yFondl2) {
        this.yFondl2 = yFondl2;
    }

    public List<String> getzFondl2() {
        return zFondl2;
    }

    public void setzFondl2(List<String> zFondl2) {
        this.zFondl2 = zFondl2;
    }
}
