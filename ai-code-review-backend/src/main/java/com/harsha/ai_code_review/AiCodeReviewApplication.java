    package com.harsha.ai_code_review;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

    @SpringBootApplication(
            exclude = {
                    DataSourceAutoConfiguration.class}
    )
    public class AiCodeReviewApplication {

        public static void main(String[] args) {
            SpringApplication.run(AiCodeReviewApplication.class, args);
        }

    }
