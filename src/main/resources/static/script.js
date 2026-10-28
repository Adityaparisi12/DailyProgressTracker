const API_URL = "/api/tasks";
const AUTH_URL = "/api/auth";

const taskForm = document.getElementById("taskForm");
const taskDate = document.getElementById("taskDate");
const titleInput = document.getElementById("title");
const descriptionInput = document.getElementById("description");
const statusInput = document.getElementById("status");
const saveButton = document.getElementById("saveButton");
const formMessage = document.getElementById("formMessage");
const taskList = document.getElementById("taskList");

function getToday() {
    const now = new Date();
    const localDate = new Date(
        now.getTime() - now.getTimezoneOffset() * 60000
    );

    return localDate.toISOString().slice(0, 10);
}

taskDate.value = getToday();

document.getElementById("todayLabel").textContent =
    new Date().toLocaleDateString("en-IN", {
        weekday: "short",
        day: "numeric",
        month: "short",
        year: "numeric"
    });

function showMessage(message, type = "success") {
    formMessage.textContent = message;
    formMessage.className = `message ${type}`;
}

async function getCsrfToken() {
    const response = await fetch(`${AUTH_URL}/csrf`, {
        credentials: "same-origin",
        cache: "no-store"
    });

    if (!response.ok) {
        throw new Error("Unable to initialize security.");
    }

    const data = await response.json();
    return data.token;
}

async function apiRequest(url, options = {}) {
    const method = (options.method || "GET").toUpperCase();
    const headers = { ...options.headers };

    if (["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
        headers["X-CSRF-TOKEN"] = await getCsrfToken();
    }

    if (options.body && !(options.body instanceof URLSearchParams)) {
        headers["Content-Type"] = "application/json";
    }

    const response = await fetch(url, {
        ...options,
        method,
        headers,
        credentials: "same-origin"
    });

    if (response.status === 401) {
        window.location.replace("/login.html");
        throw new Error("Please log in to continue.");
    }

    if (response.status === 403) {
        throw new Error(
            "Access denied. Refresh the page and try again."
        );
    }

    if (!response.ok) {
        let errorMessage = `Request failed (${response.status}).`;

        try {
            const data = await response.json();
            errorMessage = data.error || data.message || errorMessage;
        } catch {
            // Keep the default message.
        }

        throw new Error(errorMessage);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

// Save a task.
taskForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const task = {
        taskDate: taskDate.value,
        title: titleInput.value.trim(),
        description: descriptionInput.value.trim(),
        status: statusInput.value
    };

    if (!task.taskDate || !task.title) {
        showMessage("Please enter a date and task title.", "error");
        return;
    }

    saveButton.disabled = true;
    saveButton.textContent = "Saving...";

    try {
        await apiRequest(API_URL, {
            method: "POST",
            body: JSON.stringify(task)
        });

        showMessage("Your daily update has been saved!");
        taskForm.reset();
        taskDate.value = getToday();
        statusInput.value = "COMPLETED";

        await loadDashboard();
    } catch (error) {
        showMessage(error.message, "error");
    } finally {
        saveButton.disabled = false;
        saveButton.textContent = "Save daily update →";
    }
});

async function loadCurrentUser() {
    const welcomeName = document.getElementById("welcomeName");

    if (!welcomeName) return;

    try {
        const response = await fetch("/api/auth/me", {
            credentials: "same-origin",
            cache: "no-store"
        });

        if (response.status === 401 || response.status === 403) {
            window.location.replace("/login.html");
            return;
        }

        if (!response.ok) {
            throw new Error("Unable to load your profile.");
        }

        const user = await response.json();
        welcomeName.textContent = user.fullName || "there";

    } catch (error) {
        console.error("Profile loading failed:", error);
    }
}

loadCurrentUser();


// Load tasks and personal statistics.
async function loadDashboard() {
    try {
        const [tasks, stats] = await Promise.all([
            apiRequest(API_URL),
            apiRequest(`${API_URL}/stats`)
        ]);

        document.getElementById("currentStreak").textContent =
            stats.currentStreak ?? 0;

        document.getElementById("completedTasks").textContent =
            stats.completedTasks ?? 0;

        document.getElementById("completedDays").textContent =
            stats.completedDays ?? 0;

        document.getElementById("longestStreak").textContent =
            stats.longestStreak ?? 0;

        document.getElementById("taskCount").textContent =
            `${tasks.length} ${tasks.length === 1 ? "task" : "tasks"}`;

        renderTasks(tasks);
    } catch (error) {
        if (error.message !== "Please log in to continue.") {
            taskList.replaceChildren();

            const text = document.createElement("p");
            text.textContent = error.message;
            taskList.appendChild(text);
        }
    }
}

// Safely render task content without inserting user input as HTML.
function renderTasks(tasks) {
    taskList.replaceChildren();

    if (!tasks.length) {
        const empty = document.createElement("div");
        empty.className = "empty-state";

        const icon = document.createElement("div");
        icon.className = "empty-icon";
        icon.textContent = "✦";

        const heading = document.createElement("h3");
        heading.textContent = "Your journey starts here";

        const text = document.createElement("p");
        text.textContent =
            "Add your first learning update and start building your streak.";

        empty.append(icon, heading, text);
        taskList.appendChild(empty);
        return;
    }

    for (const task of tasks) {
        const card = document.createElement("article");
        card.className = "task-card";

        const top = document.createElement("div");
        top.className = "task-card-top";

        const info = document.createElement("div");

        const heading = document.createElement("h3");
        heading.textContent = task.title;

        const date = document.createElement("div");
        date.className = "task-date";
        date.textContent = task.taskDate;

        info.append(heading, date);

        const status = document.createElement("span");
        status.className = `status ${
            task.status === "COMPLETED" ? "COMPLETED" : "IN_PROGRESS"
        }`;
        status.textContent =
            task.status === "COMPLETED" ? "Completed" : "In progress";

        top.append(info, status);

        const description = document.createElement("p");
        description.className = "task-description";
        description.textContent =
            task.description || "No description added.";

        const actions = document.createElement("div");
        actions.className = "task-actions";

        const toggle = document.createElement("button");
        toggle.type = "button";
        toggle.className = "button button-outline";
        toggle.textContent = task.status === "COMPLETED"
            ? "Mark in progress"
            : "Mark completed";

        toggle.addEventListener("click", async () => {
            toggle.disabled = true;

            try {
                await apiRequest(`${API_URL}/${task.id}`, {
                    method: "PUT",
                    body: JSON.stringify({
                        taskDate: task.taskDate,
                        title: task.title,
                        description: task.description,
                        status: task.status === "COMPLETED"
                            ? "IN_PROGRESS"
                            : "COMPLETED"
                    })
                });

                await loadDashboard();
            } catch (error) {
                showMessage(error.message, "error");
                toggle.disabled = false;
            }
        });

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "button button-delete";
        deleteButton.textContent = "Delete";

        deleteButton.addEventListener("click", async () => {
            if (!window.confirm(`Delete "${task.title}"?`)) {
                return;
            }

            deleteButton.disabled = true;

            try {
                await apiRequest(`${API_URL}/${task.id}`, {
                    method: "DELETE"
                });

                showMessage("Task deleted.");
                await loadDashboard();
            } catch (error) {
                showMessage(error.message, "error");
                deleteButton.disabled = false;
            }
        });

        actions.append(toggle, deleteButton);
        card.append(top, description, actions);
        taskList.appendChild(card);
    }
}

// Logout through Spring Security.
document.getElementById("logoutButton").addEventListener("click", async () => {
    const button = document.getElementById("logoutButton");
    button.disabled = true;

    try {
        await apiRequest(`${AUTH_URL}/logout`, {
            method: "POST"
        });

        window.location.replace("/login.html");
    } catch (error) {
        showMessage(error.message, "error");
        button.disabled = false;
    }
});

loadDashboard();