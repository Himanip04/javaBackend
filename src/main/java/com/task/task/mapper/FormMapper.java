package com.task.task.mapper;

import com.task.task.model.FormSubmission;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class FormMapper {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public FormSubmission mapToSubmission(Map<String, Object> data) {
        FormSubmission submission = new FormSubmission();
        submission.setId(System.currentTimeMillis());  

        // Format date as day-month-year
        String formattedDate = LocalDateTime.now().format(FORMATTER);
        submission.setCreatedAt(formattedDate);

        submission.setData(data);
        return submission;
    }
}

