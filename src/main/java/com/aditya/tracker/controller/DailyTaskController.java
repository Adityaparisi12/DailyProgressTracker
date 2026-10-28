package com.aditya.tracker.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.aditya.tracker.entity.DailyTask;
import com.aditya.tracker.service.DailyTaskService;

@RestController
@RequestMapping("/api/tasks")
public class DailyTaskController 
{

    private final DailyTaskService service;

    public DailyTaskController(DailyTaskService service) 
    {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DailyTask> addTask(
            @RequestBody DailyTask task) 
    {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.addTask(task));
    }

    @GetMapping
    public List<DailyTask> getAllTasks() 
    {
        return service.getAllTasks();
    }

    @PutMapping("/{id}")
    public DailyTask updateTask(
            @PathVariable Long id,
            @RequestBody DailyTask task) 
    {
        return service.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id)
    {
        service.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() 
    {
        return service.getStats();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(
            IllegalArgumentException ex) 
    {
        return ResponseEntity.badRequest()
                .body(Map.of("error", ex.getMessage()));
    }
}