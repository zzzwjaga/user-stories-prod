const API_URL = "http://localhost:8080/api";
const auth = localStorage.getItem("auth");


async function apiFetch(url, options = {}) {

    const response = await fetch(API_URL + url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Basic ${auth}`
        }
    });

    if (!response.ok) {
        throw new Error("Request failed");
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}