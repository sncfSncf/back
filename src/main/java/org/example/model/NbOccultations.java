package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;

@Data
public class NbOccultations {



    @JsonProperty("NbOccultations")
    private Integer NbOccultations;

    public NbOccultations() {

    }



    public Integer getNbOccultations() {
        return NbOccultations;
    }

    public void setNbOccultations(Integer nbOccultations) {
        NbOccultations = nbOccultations;
    }


    public NbOccultations( Integer nbOccultations) {

        NbOccultations = nbOccultations;
    }
}
