package com.expensetracker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class LombokTest {
    private String name;
    private int age;
    private String email;

    public static void main(String[] args) {
        // Test constructor
        LombokTest test = new LombokTest("John Doe", 30, "john@example.com");

        // Test toString()
        System.out.println("Object: " + test);

        // Test getters
        System.out.println("Name: " + test.getName());
        System.out.println("Age: " + test.getAge());

        // Test setters
        test.setAge(31);
        System.out.println("Updated age: " + test.getAge());

        // Test logging
        log.info("Lombok is working!");
        log.debug("Debug: {}", test);

        // Test builder
        LombokTest test2 = LombokTest.builder()
                .name("Jane Doe")
                .age(25)
                .email("jane@example.com")
                .build();
        System.out.println("Builder test: " + test2);
    }
}