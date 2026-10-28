
package com.aditya.tracker.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aditya.tracker.entity.DailyTask;
import com.aditya.tracker.entity.User;
import com.aditya.tracker.repository.DailyTaskRepository;
import com.aditya.tracker.repository.UserRepository;

@Service
@Transactional
public class DailyTaskServiceImpl implements DailyTaskService {

    private final DailyTaskRepository repository;
    private final UserRepository userRepository;

    public DailyTaskServiceImpl(
            DailyTaskRepository repository,
            UserRepository userRepository) {

        this.repository = repository;
        this.userRepository = userRepository;
    }

    // Get the currently authenticated user.
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {

            throw new AccessDeniedException("Please log in first");
        }

        String email = authentication.getName()
                .trim()
                .toLowerCase();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AccessDeniedException("User not found"));
    }

    // Add a task for the logged-in user.
    @Override
    public DailyTask addTask(DailyTask task) {

        if (task.getTaskDate() == null
                || task.getTitle() == null
                || task.getTitle().isBlank()) {

            throw new IllegalArgumentException(
                    "Task date and title are required");
        }

        User user = getCurrentUser();

        task.setTitle(task.getTitle().trim());
        task.setStatus(normalizeStatus(task.getStatus()));

        // This fixes the user_id cannot be null error.
        task.setUser(user);

        return repository.save(task);
    }

    // Return only the logged-in user's tasks.
    @Override
    @Transactional(readOnly = true)
    public List<DailyTask> getAllTasks() {

        User user = getCurrentUser();

        return repository
                .findAllByUser_IdOrderByTaskDateDescIdDesc(user.getId());
    }

    // Update only a task owned by the logged-in user.
    @Override
    public DailyTask updateTask(Long id, DailyTask updatedTask) {

        User user = getCurrentUser();

        DailyTask existing = repository
                .findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Task not found: " + id));

        if (updatedTask.getTaskDate() == null
                || updatedTask.getTitle() == null
                || updatedTask.getTitle().isBlank()) {

            throw new IllegalArgumentException(
                    "Task date and title are required");
        }

        existing.setTaskDate(updatedTask.getTaskDate());
        existing.setTitle(updatedTask.getTitle().trim());
        existing.setDescription(updatedTask.getDescription());
        existing.setStatus(normalizeStatus(updatedTask.getStatus()));

        // Preserve the original owner.
        return repository.save(existing);
    }

    // Delete only a task owned by the logged-in user.
    @Override
    public void deleteTask(Long id) {

        User user = getCurrentUser();

        DailyTask existing = repository
                .findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Task not found: " + id));

        repository.delete(existing);
    }

    private String normalizeStatus(String status) {

        if (status == null || status.isBlank()) {
            return "IN_PROGRESS";
        }

        String normalized = status.trim()
                .toUpperCase()
                .replace(' ', '_');

        if (!normalized.equals("COMPLETED")
                && !normalized.equals("IN_PROGRESS")) {

            throw new IllegalArgumentException(
                    "Status must be COMPLETED or IN_PROGRESS");
        }

        return normalized;
    }

    // Calculate statistics only from the logged-in user's tasks.
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {

        User user = getCurrentUser();

        List<DailyTask> tasks = repository
                .findAllByUser_IdOrderByTaskDateDescIdDesc(user.getId());

        Set<LocalDate> completedDates = new HashSet<>();

        for (DailyTask task : tasks) {

            if ("COMPLETED".equals(task.getStatus())
                    && task.getTaskDate() != null) {

                completedDates.add(task.getTaskDate());
            }
        }

        LocalDate today = LocalDate.now();
        LocalDate streakStart;

        if (completedDates.contains(today)) {
            streakStart = today;
        } else if (completedDates.contains(today.minusDays(1))) {
            streakStart = today.minusDays(1);
        } else {
            streakStart = null;
        }

        int currentStreak = 0;

        if (streakStart != null) {

            LocalDate date = streakStart;

            while (completedDates.contains(date)) {
                currentStreak++;
                date = date.minusDays(1);
            }
        }

        int longestStreak = 0;
        int runningStreak = 0;
        LocalDate previousDate = null;

        for (LocalDate date : new TreeSet<>(completedDates)) {

            if (previousDate != null
                    && date.equals(previousDate.plusDays(1))) {

                runningStreak++;

            } else {
                runningStreak = 1;
            }

            longestStreak = Math.max(longestStreak, runningStreak);
            previousDate = date;
        }

        long completedTasks = tasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();

        long inProgressTasks = tasks.stream()
                .filter(t -> "IN_PROGRESS".equals(t.getStatus()))
                .count();

        return Map.of(
                "currentStreak", currentStreak,
                "longestStreak", longestStreak,
                "completedTasks", completedTasks,
                "inProgressTasks", inProgressTasks,
                "completedDays", completedDates.size()
        );
    }
}
