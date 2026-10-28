const AUTH_URL = "/api/auth";

function showToast(message, type = "success") {
    const oldToast = document.querySelector(".toast");

    if (oldToast) {
        oldToast.remove();
    }

    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.setAttribute("role", "status");
    toast.setAttribute("aria-live", "polite");

    const icon = document.createElement("span");
    icon.className = "toast-icon";
    icon.textContent = type === "success" ? "✓" : "✕";

    const text = document.createElement("span");
    text.textContent = message;

    toast.append(icon, text);
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add("hide");
        toast.addEventListener("animationend", () => toast.remove(), {
            once: true
        });
    }, 3000);
}

function showPendingToast() {
    const message = sessionStorage.getItem("toastMessage");

    if (message) {
        sessionStorage.removeItem("toastMessage");
        showToast(message);
    }
}

document.addEventListener("DOMContentLoaded", showPendingToast);

async function getCsrfToken() {
    const response = await fetch(`${AUTH_URL}/csrf`, {
        credentials: "same-origin",
        cache: "no-store"
    });

    if (!response.ok) {
        throw new Error("Unable to initialize security. Please try again.");
    }

    const data = await response.json();
    return data.token;
}

function displayAuthMessage(text, type = "error") {
    const element = document.getElementById("authMessage");
    element.textContent = text;
    element.className = `message ${type}`;
}

async function readError(response) {
    try {
        const data = await response.json();
        return data.error || data.message || "Something went wrong.";
    } catch {
        return `Request failed (${response.status}). Please try again.`;
    }
}

// SIGNUP
const signupForm = document.getElementById("signupForm");

if (signupForm) {
    signupForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const fullName = document.getElementById("fullName").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;
        const confirmPassword =
            document.getElementById("confirmPassword").value;

        const button = document.getElementById("signupButton");

        if (!fullName || !email || !password) {
            displayAuthMessage("Please fill in all required fields.");
            return;
        }

        if (password.length < 8) {
            displayAuthMessage(
                "Your password must contain at least 8 characters."
            );
            return;
        }

        if (password !== confirmPassword) {
            displayAuthMessage("Your passwords do not match.");
            return;
        }

        button.disabled = true;
        button.textContent = "Creating account...";

        try {
            const csrfToken = await getCsrfToken();

            const response = await fetch(`${AUTH_URL}/signup`, {
                method: "POST",
                credentials: "same-origin",
                headers: {
                    "Content-Type": "application/json",
                    "X-CSRF-TOKEN": csrfToken
                },
                body: JSON.stringify({
                    fullName,
                    email,
                    password
                })
            });

            if (!response.ok) {
                throw new Error(await readError(response));
            }

            displayAuthMessage(
                "Account created! Redirecting to login...",
                "success"
            );

            signupForm.reset();

            setTimeout(() => {
                window.location.assign("/login.html");
            }, 900);

        } catch (error) {
            displayAuthMessage(error.message);
        } finally {
            button.disabled = false;
            button.textContent = "Create account →";
        }
    });
}

// LOGIN
const loginForm = document.getElementById("loginForm");

if (loginForm) {
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;
        const button = document.getElementById("loginButton");

        button.disabled = true;
        button.textContent = "Logging in...";

        try {
            const csrfToken = await getCsrfToken();

            const response = await fetch(`${AUTH_URL}/login`, {
                method: "POST",
                credentials: "same-origin",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "X-CSRF-TOKEN": csrfToken
                },
                body: new URLSearchParams({
                    email,
                    password
                })
            });

            if (!response.ok) {
                throw new Error(await readError(response));
            }

            // Spring Security may change the session or CSRF token
            // during authentication. Obtain a fresh token.
            await getCsrfToken();

            displayAuthMessage("Login successful!", "success");
            window.location.assign("/");

        } catch (error) {
            displayAuthMessage(error.message);
        } finally {
            button.disabled = false;
            button.textContent = "Log in →";
        }
    });
}