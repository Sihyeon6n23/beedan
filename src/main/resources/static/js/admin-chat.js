document.addEventListener("DOMContentLoaded", function () {
  var filterGroups = document.querySelectorAll("[data-filter-group]");
  var listPage = document.querySelector(".admin-chat-list-page");
  var detailPage = document.querySelector(".admin-chat-detail-page");
  var stateButtons = document.querySelectorAll("[data-chat-state-target]");
  var modalTriggers = document.querySelectorAll("[data-modal-open]");
  var modalClosers = document.querySelectorAll("[data-modal-close]");
  var assignConfirmButton = document.querySelector("[data-assign-confirm]");
  var closeConfirmButton = document.querySelector("[data-modal-confirm-close]");
  var pendingDetailUrl = "";

  filterGroups.forEach(function (group) {
    group.addEventListener("click", function (event) {
      var button = event.target.closest(".admin-chat-filter");
      if (!button) {
        return;
      }

      group.querySelectorAll(".admin-chat-filter").forEach(function (item) {
        item.classList.remove("is-active");
      });
      button.classList.add("is-active");
    });
  });

  function setChatState(nextState) {
    if (!detailPage) {
      return;
    }

    detailPage.dataset.chatStatus = nextState;

    document.querySelectorAll("[data-status-badge]").forEach(function (badge) {
      badge.classList.toggle("is-hidden", badge.dataset.statusBadge !== nextState);
    });

    document.querySelectorAll("[data-composer]").forEach(function (composer) {
      composer.classList.toggle("is-hidden", composer.dataset.composer !== nextState);
    });

    var closedPanel = document.querySelector("[data-closed-panel]");
    if (closedPanel) {
      closedPanel.classList.toggle("is-hidden", nextState !== "CLOSED");
    }

    var closeTrigger = document.querySelector("[data-close-trigger]");
    if (closeTrigger) {
      closeTrigger.classList.toggle("is-hidden", nextState === "CLOSED");
    }

    stateButtons.forEach(function (button) {
      button.classList.toggle("is-active", button.dataset.chatStateTarget === nextState);
    });
  }

  stateButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      setChatState(button.dataset.chatStateTarget);
    });
  });

  function toggleModal(name, isOpen) {
    var modal = document.querySelector('[data-modal="' + name + '"]');
    if (!modal) {
      return;
    }

    modal.classList.toggle("is-hidden", !isOpen);
    modal.setAttribute("aria-hidden", isOpen ? "false" : "true");
  }

  modalTriggers.forEach(function (trigger) {
    trigger.addEventListener("click", function () {
      pendingDetailUrl = trigger.dataset.detailUrl || "";
      toggleModal(trigger.dataset.modalOpen, true);
    });
  });

  modalClosers.forEach(function (closer) {
    closer.addEventListener("click", function () {
      document.querySelectorAll(".admin-chat-modal").forEach(function (modal) {
        modal.classList.add("is-hidden");
        modal.setAttribute("aria-hidden", "true");
      });
    });
  });

  if (assignConfirmButton) {
    assignConfirmButton.addEventListener("click", function () {
      toggleModal("assign", false);
      if (listPage && pendingDetailUrl) {
        window.location.href = pendingDetailUrl;
      }
    });
  }

  if (closeConfirmButton) {
    closeConfirmButton.addEventListener("click", function () {
      toggleModal("close", false);
      setChatState("CLOSED");
    });
  }

  if (detailPage) {
    setChatState(detailPage.dataset.chatStatus || "ONGOING");
  }
});
