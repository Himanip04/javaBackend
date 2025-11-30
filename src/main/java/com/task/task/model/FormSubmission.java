package com.task.task.model;

import lombok.Data;
import java.util.Map;

@Data
public class FormSubmission {
    private Long id;
    private String createdAt;
    private Map<String, Object> data;
}
