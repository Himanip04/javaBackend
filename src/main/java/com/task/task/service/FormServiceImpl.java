package com.task.task.service;

import com.task.task.mapper.FormMapper;
import com.task.task.model.FormSubmission;
import com.task.task.repository.FormRepository;
import com.task.task.interfaces.FormService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Map;

@Service
public class FormServiceImpl implements FormService {

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormMapper formMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Object> getFormSchema() {
        return Mono.fromCallable(() -> 
            objectMapper.readValue(
                new File("src/main/resources/form-schema.json"),
                Object.class
            )
        );
    }

    @Override
    public Mono<String> submitForm(Map<String, Object> data) {
        FormSubmission submission = formMapper.mapToSubmission(data);
        return formRepository.save(submission)
                .then(Mono.just("Saved"));
    }

    @Override
    public Flux<FormSubmission> getAllSubmissions() {
        return formRepository.findAll();
    }
}
