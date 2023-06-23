package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class Meteo {

    @JsonProperty("Temperature_degC")
    private Double temperature ;



    @JsonProperty("Humidite_rel")
    private Double humidite ;

    @JsonProperty("PressionAtmo_hPa")
    private Double pression ;

    public Meteo() {

    }


    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidite() {
        return humidite;
    }

    public void setHumidite(Double humidite) {
        this.humidite = humidite;
    }

    public Double getPression() {
        return pression;
    }

    public void setPression(Double pression) {
        this.pression = pression;
    }


    public Meteo(Double temperature, Double humidite, Double pression) {
        this.temperature = temperature;
        this.humidite = humidite;
        this.pression = pression;
    }
}
