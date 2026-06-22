if (!auth) {
    window.location.href = "login.html";
}

const params = new URLSearchParams(window.location.search);
const storyId = params.get("id");

let currentStory = null;

/* =========================
   INIT
========================= */
document.addEventListener("DOMContentLoaded", () => {

    loadStory();

    document.getElementById("currentUser")
        .innerText = localStorage.getItem("email");

    const overlay = document.getElementById("overlay");

    if (overlay) {
        overlay.addEventListener("click", closeModals);
    }

    const saveBtn = document.getElementById("saveStoryBtn");
    if (saveBtn) {
        document.getElementById("storyForm")
            .addEventListener("submit", handleSaveStory);
    }

    const runInvestBtn = document.getElementById("runInvestBtn");
    if (runInvestBtn) {
        runInvestBtn.addEventListener("click", runInvest);
    }

    const changeStatusBtn = document.getElementById("changeStatusBtn");
    if (changeStatusBtn) {
        changeStatusBtn.addEventListener("click", changeStatus);
    }

    const closeInvestBtn = document.getElementById("closeInvestModalBtn");
    if (closeInvestBtn) {
        closeInvestBtn.addEventListener("click", closeInvestModal);
    }

    const deleteBtn = document.getElementById("deleteStoryBtn");
    if (deleteBtn) {
        deleteBtn.addEventListener("click", deleteStory);
    }

    const editBtn = document.getElementById("editStoryBtn");
    if (editBtn) {
        editBtn.addEventListener("click", openEditModal);
    }

    const logoutBtn = document.getElementById("logoutBtn");

    logoutBtn.addEventListener("click", () => {
        localStorage.clear();
        window.location.href = "login.html";
    });

    document.getElementById("statusSelect").addEventListener("change", (e) => {
    changeStatus(e.target.value);
});
});

/* =========================
   LOAD STORY
========================= */
async function loadStory() {

    try {
        const story = await apiFetch(`/stories/${storyId}`);
        currentStory = story;

        const status = await apiFetch(`/stories/${story.id}/statuses/latest`);

        document.getElementById("storyNumber").innerText =
            `${story.number}`;

        document.getElementById("storyText").innerText =
            story.story_text;

        document.getElementById("storyPoints").innerText =
            `Story points: ${story.story_points}`;

        loadLatestInvest();

    } catch (err) {
        console.error(err);
        showToast("Failed to load story", "error");
    }
}

/* =========================
   DELETE STORY
========================= */
async function deleteStory() {

    if (!confirm("Delete story?")) return;

    try {
        await apiFetch(`/stories/${storyId}`, {
            method: "DELETE"
        });

        showToast("Story deleted");

        window.location.href = "index.html";

    } catch (err) {
        console.error(err);
        showToast("Delete failed", "error");
    }
}

/* =========================
   EDIT STORY
========================= */
function openEditModal() {

    const modal = document.getElementById("storyModal");

    document.getElementById("storyTextInput").value =
        currentStory.story_text;

    document.getElementById("storyPointsInput").value =
        currentStory.story_points;

    modal.setAttribute("data-edit-id", currentStory.id);

    modal.classList.remove("hidden");
    document.getElementById("overlay").classList.remove("hidden");
}

/* =========================
   SAVE STORY (EDIT)
========================= */
async function handleSaveStory(e) {

    e.preventDefault();

    const modal = document.getElementById("storyModal");

    const editId = modal.getAttribute("data-edit-id");

    const storyText =
        document.getElementById("storyTextInput").value;

    const storyPoints =
        document.getElementById("storyPointsInput").value;

    try {

        await apiFetch(`/stories/${editId}`, {
            method: "PUT",
            body: JSON.stringify({
                story_text: storyText,
                story_points: storyPoints
            })
        });

        showToast("Story updated");

        modal.classList.add("hidden");
        document.getElementById("overlay").classList.add("hidden");

        loadStory();

    } catch (err) {
        console.error(err);
        showToast("Update failed", "error");
    }
}
/* =========================
   INVEST MODAL
========================= */
async function runInvest() {

    try {
        await apiFetch(`/invest/story/${storyId}`, {
            method: "POST"
        });

        showToast("INVEST completed");

        loadLatestInvest();

    } catch (err) {
        console.error(err);
        showToast("INVEST failed", "error");
    }
}

async function loadLatestInvest() {

    const container = document.getElementById("latestInvestContainer");

    try {
        const res = await apiFetch(`/invest/story/${storyId}/latest`);

        if (!res) {
            container.innerHTML = "<p>No INVEST results</p>";
            return;
        }

        container.innerHTML = "";
        container.appendChild(renderInvest(res));

    } catch (err) {
        container.innerHTML = "<p>Error loading INVEST</p>";
    }
}

function renderInvest(r) {

    const div = document.createElement("div");

    div.innerHTML = `
        <h3>Latest INVEST</h3>

        <div class="grid">
            <div>I: ${r.independentScore}</div>
            <div>N: ${r.negotiableScore}</div>
            <div>V: ${r.valuableScore}</div>
            <div>E: ${r.estimableScore}</div>
            <div>S: ${r.smallScore}</div>
            <div>T: ${r.testableScore}</div>
        </div>

        <details>
            <summary>Issues</summary>
            <p>${r.issues || "-"}</p>
        </details>

        <details>
            <summary>Suggestions</summary>
            <p>${r.suggestions || "-"}</p>
        </details>
    `;

    return div;
}

/* =========================
   STATUS CHANGE
========================= */
async function changeStatus(newStatus) {
    try {
        await apiFetch(`/stories/${storyId}/status?status=${newStatus}`, {
            method: "PATCH"
        });

        // Обновляем локальный объект
        currentStory.status = newStatus;
        
        // Обновляем select
        const statusSelect = document.getElementById("statusSelect");
        if (statusSelect) {
            statusSelect.value = newStatus;
        }
    
    
        showToast(`Status → ${newStatus}`);

    } catch (err) {
        console.error('Status update error:', err);
        showToast("Status update failed", "error");
    }
}

/* =========================
   MODALS
========================= */
function closeModals() {

    document.getElementById("storyModal")?.classList.add("hidden");
    document.getElementById("investModal")?.classList.add("hidden");
    document.getElementById("overlay")?.classList.add("hidden");
}

function closeInvestModal() {

    document.getElementById("investModal").classList.add("hidden");
    document.getElementById("overlay").classList.add("hidden");
}

/* =========================
   TOAST
========================= */
function showToast(message, type = "success") {

    const toast = document.getElementById("toast");

    toast.innerText = message;
    toast.className = "toast";
    toast.classList.add(type);
    toast.classList.remove("hidden");

    setTimeout(() => {
        toast.classList.add("hidden");
    }, 2000);
}