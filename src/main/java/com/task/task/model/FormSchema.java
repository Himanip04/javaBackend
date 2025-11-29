
package com.task.task.model;

import lombok.Data;
import java.util.List;

@Data
public class FormSchema {
    private String title;
    private List<FormField> fields;
}
