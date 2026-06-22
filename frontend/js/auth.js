document.getElementById("loginForm")
.addEventListener("submit", async (e) => {

    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const auth = btoa(`${email}:${password}`);

    try {

        const response = await fetch("http://localhost:8080/api/boards?page=0&size=1", {
            headers: {
                Authorization: `Basic ${auth}`
            }
        });

        // 🔴 ВАЖНО: проверка статуса
        if (response.status === 401) {
            showError("Invalid email or password");
            return;
        }

        if (!response.ok) {
            showError("Login error");
            return;
        }

        // ✔ только если ОК
        localStorage.setItem("auth", auth);
        localStorage.setItem("email", email);

        window.location.href = "index.html";

    } catch (err) {
        showError("Server error");
    }
});

function showError(message) {
    const error = document.getElementById("error");
    error.innerText = message;
    error.classList.remove("hidden");
}