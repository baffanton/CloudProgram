package org.example.lab1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Label(
        @JsonProperty("eng")
        String name,
        @JsonProperty("coord")
        List<Integer> coordination
) {
}
