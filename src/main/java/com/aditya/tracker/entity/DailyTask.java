package com.aditya.tracker.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "daily_tasks")
public class DailyTask 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate taskDate;

    private String title;

    private String description;

    private String status;

    private LocalDateTime createdAt;

    public DailyTask() 
    {
    	
    }

    @PrePersist
    public void onCreate() 
    {
        if (createdAt == null) 
        {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id) 
    {
        this.id = id;
    }

    public LocalDate getTaskDate()
    {
        return taskDate;
    }

    public void setTaskDate(LocalDate taskDate) 
    {
        this.taskDate = taskDate;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() 
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) 
    {
        this.createdAt = createdAt;
    }
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
}