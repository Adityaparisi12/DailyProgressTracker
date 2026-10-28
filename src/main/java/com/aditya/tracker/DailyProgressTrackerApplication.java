package com.aditya.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DailyProgressTrackerApplication 
{
	public static void main(String[] args) 
	{
		SpringApplication.run(DailyProgressTrackerApplication.class, args);
		System.out.println("Application is Running Succesfully");
	}

}
