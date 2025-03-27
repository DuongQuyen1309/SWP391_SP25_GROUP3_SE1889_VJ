package com.demoproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class NoteRequest {
    @NotBlank(message = "Note Name cannot be empty")
    private String note;

    @NotBlank(message = "Note image cannot be empty")
    private String imagePath;
}
