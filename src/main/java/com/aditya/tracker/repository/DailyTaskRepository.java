package com.aditya.tracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aditya.tracker.entity.DailyTask;

@Repository
public interface DailyTaskRepository extends JpaRepository<DailyTask, Long> 
{

    List<DailyTask> findAllByOrderByTaskDateDescIdDesc();

    List<DailyTask> findByStatus(String status);
    
    List<DailyTask> findAllByUser_IdOrderByTaskDateDescIdDesc(
            Long userId);

    Optional<DailyTask> findByIdAndUser_Id(
            Long id, Long userId);
}