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

// Forms marked data-reflects-request must always show the request the server answered. When a page
// is restored from the back/forward cache the browser keeps whatever the user last picked (e.g. the
// filters submitted from this page), so put the controls back to their server-rendered values.
window.addEventListener("pageshow", function (event) {
    if (!event.persisted) {
        return;
    }
    document.querySelectorAll("form[data-reflects-request]").forEach(function (form) {
        form.reset();
    });
});
