document.addEventListener("DOMContentLoaded", function () {
  var widget = document.querySelector("[data-member-chat]");
  if (!widget) {
    return;
  }

  var panel = widget.querySelector(".member-chat-panel");
  var launcher = widget.querySelector("[data-widget-toggle]");
  var closeButtons = widget.querySelectorAll("[data-widget-close]");
  var viewButtons = widget.querySelectorAll("[data-view-target]");
  var navButtons = widget.querySelectorAll(".member-chat-bottom-nav__item");
  var views = widget.querySelectorAll("[data-chat-view]");
  var modal = widget.querySelector("[data-chat-modal]");
  var modalCloseButtons = widget.querySelectorAll("[data-chat-modal-close]");
  var modalConfirmButton = widget.querySelector("[data-chat-modal-confirm]");
  var newChatButton = widget.querySelector("[data-new-chat-trigger]");

  function setPanelOpen(isOpen) {
    panel.classList.toggle("is-hidden", !isOpen);
    panel.setAttribute("aria-hidden", isOpen ? "false" : "true");
    launcher.setAttribute("aria-expanded", isOpen ? "true" : "false");
    if (!isOpen) {
      setModalOpen(false);
    }
  }

  function setView(viewName) {
    var navViewName = viewName === "chat-room" ? "chat-list" : viewName;

    views.forEach(function (view) {
      view.classList.toggle("is-active", view.dataset.chatView === viewName);
    });

    navButtons.forEach(function (button) {
      button.classList.toggle("is-active", button.dataset.viewTarget === navViewName);
    });
  }

  function setModalOpen(isOpen) {
    if (!modal) {
      return;
    }

    modal.classList.toggle("is-hidden", !isOpen);
    modal.setAttribute("aria-hidden", isOpen ? "false" : "true");
  }

  launcher.addEventListener("click", function () {
    var willOpen = panel.classList.contains("is-hidden");
    setPanelOpen(willOpen);
    if (willOpen) {
      setView("chatbot");
    }
  });

  closeButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      setPanelOpen(false);
    });
  });

  viewButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      setPanelOpen(true);
      setView(button.dataset.viewTarget);
    });
  });

  if (newChatButton) {
    newChatButton.addEventListener("click", function () {
      setPanelOpen(true);
      setModalOpen(true);
    });
  }

  modalCloseButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      setModalOpen(false);
    });
  });

  if (modalConfirmButton) {
    modalConfirmButton.addEventListener("click", function () {
      setModalOpen(false);
      setPanelOpen(true);
      setView("chat-room");
    });
  }

  setPanelOpen(false);
  setView("chatbot");
  setModalOpen(false);
});
