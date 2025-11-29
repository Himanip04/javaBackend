package com.task.task.model;

import lombok.Data;
import java.util.List;

@Data
public class FormField {
    private String name;
    private String label;
    private String type;
    private boolean required;

    private Integer min;
    private Integer max;

    private Integer minLength;
    private Integer maxLength;

    private String minDate;

    private List<String> options;
    private Integer minSelected;
    private Integer maxSelected;

    private String placeholder;
}
