package org.example.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.net.PasswordAuthentication;

@Entity
@Data
@Table(name = "Utilisateur")
public class Utilisateur {


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;


    @Column(name = "nom")
    private String nom;
    @Column(name = "site")
    private String site;
    @Column(name = "prenom")
    private String prenom;

    @Column(name = "login")
    private String login;


    @Column(name = "password")
    private String  password;

    @Column(name = "role")
    private String role;

    @Column(name = "etat")
    private String etat;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public Utilisateur(){

    }
}
