package com.alexistdev.geobill.exceptions;

import com.alexistdev.geobill.utils.MessagesUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler(new MessagesUtils(messageSource)))
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("Body yang tidak dikirim dijawab 400, bukan 500")
    void missingBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/dummy/tickets").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Request body is required"));
    }

    @Test
    @DisplayName("JSON yang rusak dijawab 400")
    void malformedBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/dummy/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"summary\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Request body is not valid JSON"));
    }

    @Test
    @DisplayName("Tipe field yang salah menyebut nama fieldnya")
    void invalidFieldType_namesTheField() throws Exception {
        mockMvc.perform(post("/dummy/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"summary\":\"halo\",\"departmentId\":\"bukan-uuid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Field departmentId has an invalid value"));
    }

    @Test
    @DisplayName("Body valid secara JSON tapi gagal constraint tetap lewat handleValidation")
    void blankField_returnsConstraintMessage() throws Exception {
        mockMvc.perform(post("/dummy/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"summary\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Summary is required"));
    }

    @Test
    @DisplayName("Parameter wajib yang kosong dijawab 400")
    void missingParameter_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/dummy/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Request parameter keyword is required"));
    }

    @Test
    @DisplayName("Tipe parameter yang salah dijawab 400")
    void parameterTypeMismatch_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/dummy/search").param("keyword", "x").param("departmentId", "bukan-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Request parameter departmentId has an invalid value"));
    }

    @Test
    @DisplayName("Constraint pada parameter controller dijawab 400")
    void negativePage_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/dummy/search").param("keyword", "x").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    @DisplayName("DuplicateException dijawab 400 sesuai konvensi")
    void duplicate_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/dummy/duplicate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Department already exists"));
    }

    @Test
    @DisplayName("SuspendedException dijawab 403")
    void suspended_returnsForbidden() throws Exception {
        mockMvc.perform(get("/dummy/suspended"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.messages[0]").value("Account is suspended"));
    }

    @Test
    @DisplayName("Error tak terduga dijawab 500 tanpa membocorkan detail internal")
    void unexpected_returnsGenericMessage() throws Exception {
        mockMvc.perform(get("/dummy/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.messages[0]")
                        .value("An unexpected error occurred, please try again later"));
    }

    @RestController
    static class DummyController {

        @PostMapping("/dummy/tickets")
        public String create(@Valid @RequestBody DummyRequest request) {
            return "ok";
        }

        @GetMapping("/dummy/search")
        public String search(@RequestParam String keyword,
                             @RequestParam(required = false) UUID departmentId,
                             @RequestParam(defaultValue = "0") @PositiveOrZero int page) {
            return "ok";
        }

        @GetMapping("/dummy/duplicate")
        public String duplicate() {
            throw new DuplicateException("Department already exists");
        }

        @GetMapping("/dummy/suspended")
        public String suspended() {
            throw new SuspendedException("Account is suspended");
        }

        @GetMapping("/dummy/boom")
        public String boom() {
            throw new IllegalStateException("connection pool exhausted at com.alexistdev.internal.Pool");
        }
    }

    static class DummyRequest {

        @NotBlank(message = "Summary is required")
        private String summary;

        private UUID departmentId;

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public UUID getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(UUID departmentId) {
            this.departmentId = departmentId;
        }
    }
}
