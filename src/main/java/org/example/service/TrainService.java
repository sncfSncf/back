package org.example.service;


import org.example.model.Train;
import org.example.repository.TrainRepository;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrainService {

    private final TrainRepository trainRepository;
    private final Map<Long, Train> trainMap;

    public TrainService(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
        this.trainMap = new HashMap<>();
    }

    public Iterable<Train> list() {
        return trainRepository.findAll();
    }


    public Train save(Train train) {
        return trainRepository.save(train);
    }



    public void save(List<Train> trains) {

        trainRepository.saveAll(trains);
    }


    public Train findById(Long id) {
        return trainRepository.findById(id).orElse(null);
    }




    public void loadTrains() {
        List<Train> trains = (List<Train>) trainRepository.findAll();
        for (Train train : trains) {
            trainMap.put(train.getId(), train);
        }
    }


    public List<Train> findAll() {
return trainRepository.findAll();
    }




}
