package com.aditya.tracker.service;

import java.util.List;
import java.util.Map;

import com.aditya.tracker.entity.DailyTask;

public interface DailyTaskService 
{

  public  DailyTask addTask(DailyTask task);

  public  List<DailyTask> getAllTasks();

  public  DailyTask updateTask(Long id, DailyTask task);

  public  void deleteTask(Long id);

  public  Map<String, Object> getStats();
  

}