package org.example.service;

import org.example.model.Result;
import org.example.repository.ResultRepository;
import org.springframework.stereotype.Service;

@Service
public class ResultService {

    private final ResultRepository resultRepository;

    public ResultService(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }


    public Result save(Result result) {
        return resultRepository.save(result);
    }
}
