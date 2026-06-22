let currentPage = 0;
let pageSize = 10;

if (!auth) {
    window.location.href = "login.html";
}

// ================= INIT =================

document.addEventListener("DOMContentLoaded", () => {

    document.getElementById("currentUser")
        .innerText = localStorage.getItem("email");

    const openBtn = document.getElementById("createBtn");
    const modal = document.getElementById("createBoardModal");
    const overlay = document.getElementById("overlay");
    const form = document.getElementById("createBoardForm");
    const toast = document.getElementById("toast");

    const logoutBtn = document.getElementById("logoutBtn");

    logoutBtn.addEventListener("click", () => {
        localStorage.clear();
        window.location.href = "login.html";
    });

    // открыть модалку
    openBtn.addEventListener("click", () => {
        modal.classList.remove("hidden");
        overlay.classList.remove("hidden");
    });

    // закрытие по клику
    overlay.addEventListener("click", closeModal);

    function closeModal() {
        modal.classList.add("hidden");
        overlay.classList.add("hidden");

        form.reset();
        modal.removeAttribute("data-edit-id");
        document.getElementById("submitBoardBtn").innerText = "Create";
        document.getElementById("modalTitle").innerText = "Create Board";
    }

    // submit (CREATE + EDIT)
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const name = document.getElementById("boardName").value;
        const description = document.getElementById("boardDescription").value;

        const editId = modal.getAttribute("data-edit-id");

        try {

            if (editId) {
                // ================= EDIT =================
                await apiFetch(`/boards/${editId}`, {
                    method: "PUT",
                    body: JSON.stringify({
                        boardname: name,
                        description: description
                    })
                });

                showToast("Board updated!");
            } else {
                // ================= CREATE =================
                await apiFetch("/boards", {
                    method: "POST",
                    body: JSON.stringify({
                        boardname: name,
                        description: description
                    })
                });

                showToast("Board created!");
            }

            closeModal();
            loadBoards();

        } catch (err) {
            console.error(err);
            showToast("Error occurred");
        }
    });

    loadBoards();
});

// ================= LOAD BOARDS =================

async function loadBoards() {

    const response = await apiFetch(
        `/boards?page=${currentPage}&size=${pageSize}`
    );

    const container = document.getElementById("boardsContainer");
    container.innerHTML = "";

    const boards = response?.content || [];

    for (const board of boards) {

        const div = document.createElement("div");
        div.classList.add("board-card");

        div.innerHTML = `
            <h3>${board.boardname}</h3>
            <p>${board.description}</p>
        `;

        const role = await apiFetch(`/boards/${board.id}/my-role`);

        // ================= ACTIONS =================
        const actions = document.createElement("div");
        actions.classList.add("board-actions");

        if(role === "OWNER" || role === "EDITOR"){
        const editBtn = document.createElement("button");
        editBtn.innerText = "Edit";
        editBtn.classList.add("editBtn");

        editBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            openEdit(board);
        });

        actions.appendChild(editBtn);
    }

        if(role === "OWNER"){
        const deleteBtn = document.createElement("button");
        deleteBtn.innerText = "Delete";
        deleteBtn.classList.add("deleteBtn");

        deleteBtn.addEventListener("click", async (e) => {
            e.stopPropagation();

            if (!confirm("Delete board?")) return;

            await apiFetch(`/boards/${board.id}`, {
                method: "DELETE"
            });

            showToast("Board deleted!");
            loadBoards();
        });

        actions.appendChild(deleteBtn);

    }

        div.appendChild(actions);

        div.addEventListener("click", () => {
            window.location.href = `board.html?id=${board.id}`;
        });

        container.appendChild(div);
    }

    renderPagination(response.totalPages);
}

// ================= EDIT =================

function openEdit(board) {

    const modal = document.getElementById("createBoardModal");

    document.getElementById("boardName").value = board.boardname;
    document.getElementById("boardDescription").value = board.description;

    modal.setAttribute("data-edit-id", board.id);
    document.getElementById("modalTitle").innerText = "Edit Board";
    document.getElementById("submitBoardBtn").innerText = "Edit";

    modal.classList.remove("hidden");
    document.getElementById("overlay").classList.remove("hidden");
}

// ================= PAGINATION =================

function renderPagination(totalPages) {

    const pagination = document.getElementById("pagination");
    pagination.innerHTML = "";

    const prevBtn = document.createElement("button");
    prevBtn.innerText = "Previous";
    prevBtn.disabled = currentPage === 0;

    prevBtn.addEventListener("click", () => {
        currentPage--;
        loadBoards();
    });

    const pageInfo = document.createElement("span");
    pageInfo.innerText = `Page ${currentPage + 1} of ${totalPages}`;

    const nextBtn = document.createElement("button");
    nextBtn.innerText = "Next";
    nextBtn.disabled = currentPage >= totalPages - 1;

    nextBtn.addEventListener("click", () => {
        currentPage++;
        loadBoards();
    });

    pagination.append(prevBtn, pageInfo, nextBtn);
}

// ================= TOAST =================

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
