package org.example.service;

import org.example.model.Mr;
import org.example.repository.MrRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MrService {

    public List<Mr> findAll() {
        return mrRepository.findAll();
    }

    public Mr findById(Long id) {
        return mrRepository.findById(id).orElse(null);
    }

    private final MrRepository mrRepository ;

    public MrService(MrRepository mrRepository){
        this.mrRepository = mrRepository;
    }

    public Iterable<Mr> list() {
        return mrRepository.findAll();
    }

    public Mr save(Mr mr) {
        return mrRepository.save(mr);
    }

    public void save(List<Mr> mrs) {
        mrRepository.saveAll(mrs);
    }
}
