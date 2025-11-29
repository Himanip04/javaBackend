package com.task.task.mapper;

import com.task.task.model.FormSubmission;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class FormMapper {

    public FormSubmission mapToSubmission(Map<String, Object> data) {
        FormSubmission submission = new FormSubmission();
        submission.setId(System.currentTimeMillis());  
        submission.setCreatedAt(LocalDateTime.now().toString());
        submission.setData(data);
        return submission;
    }
}

