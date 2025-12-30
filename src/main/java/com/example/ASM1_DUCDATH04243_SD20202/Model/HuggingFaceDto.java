package com.example.ASM1_DUCDATH04243_SD20202.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

// DTO này định nghĩa cấu trúc JSON cho request và response của Hugging Face
public class HuggingFaceDto {

    // DTO cho Request: chỉ cần một trường "inputs"
    public record InferenceRequest(String inputs) {}

    // DTO cho Response: API sẽ trả về một trường "generated_text"
    public record InferenceResponse(
            @JsonProperty("generated_text") String generatedText
    ) {}
}