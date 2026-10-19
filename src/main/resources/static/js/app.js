// Prevent duplicate submissions on forms marked with data-once (UI_UX_Design section 4.4).
document.addEventListener("submit", function (event) {
    var form = event.target;
    if (!form.hasAttribute("data-once")) {
        return;
    }
    if (form.dataset.submitted === "true") {
        event.preventDefault();
        return;
    }
    form.dataset.submitted = "true";
    form.querySelectorAll("button[type=submit]").forEach(function (button) {
        button.disabled = true;
        button.setAttribute("aria-busy", "true");
    });
});
