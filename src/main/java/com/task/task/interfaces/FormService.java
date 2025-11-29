package com.task.task.interfaces;

import com.task.task.model.FormSubmission;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/form")
public interface FormService {

    @GetMapping("/schema")
   public Mono<Object> getFormSchema();

    @PostMapping("/submit")
   public Mono<String> submitForm(@RequestBody Map<String, Object> data);

    @GetMapping("/submissions")
   public Flux<FormSubmission> getAllSubmissions();
}
