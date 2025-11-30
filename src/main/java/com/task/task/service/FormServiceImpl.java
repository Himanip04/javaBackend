package com.task.task.service;

import com.task.task.mapper.FormMapper;
import com.task.task.model.FormSubmission;
import com.task.task.repository.FormRepository;
import com.task.task.interfaces.FormService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.task.validation.ValidationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class FormServiceImpl implements FormService {

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormMapper formMapper;

    @Autowired
    private ValidationService validationService;

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
    return getFormSchema()
        .flatMap(schema -> {
            return validationService.validate(data, schema)
                .flatMap(errors -> {
                    if (!errors.isEmpty()) {
                        return Mono.error(new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            objectToJson(errors)
                        ));
                    }
                    return Mono.just(schema);
                });
        })
        .flatMap(schema -> {
            FormSubmission submission = formMapper.mapToSubmission(data);
            return formRepository.save(submission)
                    .then(Mono.just("Saved"));
        });
}



    @Override
    public Flux<FormSubmission> getAllSubmissions() {
        return formRepository.findAll();
    }

    private String objectToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }


    @Override
public Mono<Object> getPaginatedSubmissions(int page, int limit, String sortBy, String sortOrder) {
    return formRepository.findAll()
            .collectList()
            .flatMap(list -> {
                list.sort((a, b) -> {
                    try {
                        var fieldA = a.getCreatedAt();
                        var fieldB = b.getCreatedAt();

                        int cmp = fieldA.compareTo(fieldB);
                        return sortOrder.equalsIgnoreCase("asc") ? cmp : -cmp;

                    } catch (Exception e) {
                        return 0;
                    }
                });
                int totalCount = list.size();
                int totalPages = (int) Math.ceil((double) totalCount / limit);
                int start = (page - 1) * limit;
                int end = Math.min(start + limit, totalCount);

                if (start >= totalCount) {
                    return Mono.just(
                            Map.of(
                                    "success", true,
                                    "data", List.of(),
                                    "page", page,
                                    "limit", limit,
                                    "totalCount", totalCount,
                                    "totalPages", totalPages
                            )
                    );
                }

                var paginated = list.subList(start, end);
                return Mono.just(
                        Map.of(
                                "success", true,
                                "data", paginated,
                                "page", page,
                                "limit", limit,
                                "totalCount", totalCount,
                                "totalPages", totalPages
                        )
                );
            });
}

}


