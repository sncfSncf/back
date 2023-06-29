package org.example.service;

import org.example.model.M_50592;
import org.example.model.Sam;
import org.example.repository.M_50592Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class M_50592Service {

    private final M_50592Repository m50592Repository;

    public M_50592Service(M_50592Repository m50592Repository) {
        this.m50592Repository = m50592Repository;
    }

    public Iterable<M_50592> list() {
        return m50592Repository.findAll();
    }

    public M_50592 save(M_50592 m50592) {
        return m50592Repository.save(m50592);
    }
    public boolean existsByfileName(String nomFichier) {
        return m50592Repository.existsByfileName(nomFichier);
    }

    public void save(List<M_50592> m_50592s) {


        m50592Repository.saveAll(m_50592s);
    }

    public List<M_50592> findAll() {
        return m50592Repository.findAll();
    }


    public M_50592 findById(Long id) {
        return m50592Repository.findById(id).orElse(null);
    }
}
