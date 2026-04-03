document.addEventListener("DOMContentLoaded", () => {
    const groups = Array.from(document.querySelectorAll(".sidebar-menu__group"));

    groups.forEach((group) => {
        const toggle = group.querySelector(".sidebar-menu__toggle");
        if (!toggle) {
            return;
        }

        toggle.addEventListener("click", () => {
            const isOpen = group.classList.contains("sidebar-menu__group--open");

            groups.forEach((targetGroup) => {
                const targetToggle = targetGroup.querySelector(".sidebar-menu__toggle");
                if (!targetToggle) {
                    return;
                }

                targetGroup.classList.remove("sidebar-menu__group--open");
                targetToggle.setAttribute("aria-expanded", "false");
            });

            if (!isOpen) {
                group.classList.add("sidebar-menu__group--open");
                toggle.setAttribute("aria-expanded", "true");
            }
        });
    });
});
