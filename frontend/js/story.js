if(!auth){
    window.location.href = "login.html"
}

const params =
    new URLSearchParams(window.location.search);

const boardId = params.get("id");

let currentPage = 0;
let pageSize = 15;

document.addEventListener("DOMContentLoaded", () => {

    document.getElementById("currentUser")
        .innerText = localStorage.getItem("email");
    const openBtn = document.getElementById("createBtn");
    const modal = document.getElementById("createStoryModal");
    const overlay = document.getElementById("overlay");
    const form = document.getElementById("createStoryForm");

    const logoutBtn = document.getElementById("logoutBtn");

    logoutBtn.addEventListener("click", () => {
        localStorage.clear();
        window.location.href = "login.html";
    });

    openBtn.addEventListener("click", () => {

        modal.classList.remove("hidden");
        overlay.classList.remove("hidden");

});


    overlay.addEventListener("click", closeModal);

    function closeModal() {

        modal.classList.add("hidden");
        overlay.classList.add("hidden");
        form.reset();
        modal.removeAttribute("data-edit-id");
        document.getElementById("submitStoryBtn")
            .innerText = "Create";
        document.getElementById("modalTitle")
            .innerText = "Create Story";
    }

    form.addEventListener("submit", async (e) => {

        e.preventDefault();
        const storyText =
            document.getElementById("storyTextInput").value;
        const storyPoints =
            document.getElementById("storyPointsInput").value;
        const editId =
            modal.getAttribute("data-edit-id");
        try {

            if (editId) {

                // UPDATE
                await apiFetch(`/stories/${editId}`, {
                    method: "PUT",
                    body: JSON.stringify({
                        story_text: storyText,
                        story_points: storyPoints
                    })
                });
                showToast("Story updated");

            } else {

                // CREATE
                await apiFetch(`/boards/${boardId}/stories`, {
                    method: "POST",
                    body: JSON.stringify({
                        story_text: storyText,
                        story_points: storyPoints
                    })
                });
                showToast("Story created");
            }

            closeModal();
            loadStories();

        } catch (err) {
            showToast("Error", "error");
        }
    });

    loadStories();

});

async function loadStories() {

    const response = await apiFetch(
        `/boards/${boardId}/stories?page=${currentPage}&size=${pageSize}`
    );

    const board = await apiFetch(`/boards/${boardId}`);
    const title = document.getElementById("boardTitle");
    const description = document.getElementById("boardDescription"); 

    title.innerText = board.boardname;
    description.innerText = board.description;


    const stories = response.content;

    const container =
        document.getElementById("storiesContainer");

    container.innerHTML = "";

    for (const story of stories) {

    const statusResponse = await apiFetch(
        `/stories/${story.id}/statuses/latest`
    );

    const div = document.createElement("div");
    div.classList.add("story-card");
    div.style.cursor = "pointer";

    div.addEventListener("click", () => {
        window.location.href = `story.html?id=${story.id}`;
    });

    div.innerHTML = `
        <h3>${story.number}</h3>
        <p>${story.story_text}</p>
        <span class="story-status">
            ${statusResponse.status}
        </span>
    `;

    const actions = document.createElement("div");
    actions.classList.add("story-actions");

    // EDIT
    const editBtn = document.createElement("button");
    editBtn.innerText = "Edit";
    editBtn.classList.add("editBtn");

    editBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        openEdit(story);
    });

    // DELETE
    const deleteBtn = document.createElement("button");
    deleteBtn.innerText = "Delete";
    deleteBtn.classList.add("deleteBtn");


    deleteBtn.addEventListener("click", async (e) => {
        e.stopPropagation();

        if (!confirm("Delete story?")) return;

        try {
            await apiFetch(`/stories/${story.id}`, {
                method: "DELETE"
            });

            div.remove(); // мгновенно убираем из UI
            showToast("Story deleted");

        } catch (err) {
            console.error(err);
            showToast("Delete failed", "error");
        }
    });

    actions.appendChild(editBtn);
    actions.appendChild(deleteBtn);

    div.appendChild(actions);
    container.appendChild(div);
    
}
    renderPagination(response.totalPages);
}


function renderPagination(totalPages) {

    const pagination =
        document.getElementById("pagination");

    pagination.innerHTML = "";

    const prevBtn =
        document.createElement("button");

    prevBtn.innerText = "Previous";
    prevBtn.disabled = currentPage === 0;

    prevBtn.addEventListener("click", () => {
        currentPage--;
        loadStories();
    });

    pagination.appendChild(prevBtn);

    const pageInfo =
        document.createElement("span");
    pageInfo.innerText =
        `Page ${currentPage + 1} of ${totalPages}`;
    pagination.appendChild(pageInfo);

    const nextBtn =
        document.createElement("button");
    nextBtn.innerText = "Next";
    nextBtn.disabled =
        currentPage >= totalPages - 1;

    nextBtn.addEventListener("click", () => {

        currentPage++;
        loadStories();
    });

    pagination.appendChild(nextBtn);
}

function openEdit(story) {

    const modal =
        document.getElementById("createStoryModal");
    document.getElementById("storyTextInput").value =
        story.story_text;
    document.getElementById("storyPointsInput").value =
        story.story_points;
    modal.setAttribute("data-edit-id", story.id);
    document.getElementById("submitStoryBtn")
        .innerText = "Edit";
    document.getElementById("modalTitle")
        .innerText = "Edit story";
    modal.classList.remove("hidden");
    document.getElementById("overlay")
        .classList.remove("hidden");
}

function showToast(message, type = "success") {

    const toast = document.getElementById("toast");
    toast.innerText = message;
    toast.className = "toast"; // сброс классов
    toast.classList.add(type); // success / error / warning
    toast.classList.remove("hidden");

    setTimeout(() => {
        toast.classList.add("hidden");
    }, 2000);
}