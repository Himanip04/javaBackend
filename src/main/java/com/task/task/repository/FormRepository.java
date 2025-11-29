package com.task.task.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.task.model.FormSubmission;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FormRepository {

    private final String FILE_PATH = "src/main/resources/submissions.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<Void> save(FormSubmission submission) {
        return Mono.fromCallable(() -> {
            List<FormSubmission> all = readFile();
            all.add(submission);
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(FILE_PATH), all);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Flux<FormSubmission> findAll() {
        return Mono.fromCallable(this::readFile)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    private List<FormSubmission> readFile() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) return new ArrayList<>();

            return objectMapper.readValue(file, new TypeReference<List<FormSubmission>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
