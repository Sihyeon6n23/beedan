document.addEventListener("DOMContentLoaded", function () {
  var filterGroups = document.querySelectorAll("[data-filter-group]");
  var listPage = document.querySelector(".admin-chat-list-page");
  var detailPage = document.querySelector(".admin-chat-detail-page");
  var stateButtons = document.querySelectorAll("[data-chat-state-target]");
  var modalTriggers = document.querySelectorAll("[data-modal-open]");
  var modalClosers = document.querySelectorAll("[data-modal-close]");
  var assignConfirmButton = document.querySelector("[data-assign-confirm]");
  var closeConfirmButton = document.querySelector("[data-modal-confirm-close]");
  var adminMessageInput = document.querySelector("[data-admin-message-input]");
  var adminMessageSendButton = document.querySelector("[data-admin-message-send]");
  var adminMessageList = document.querySelector("[data-admin-message-list]");
  var adminMessageScrollBody = document.querySelector(".admin-chat-detail-body");
  var pendingDetailUrl = "";
  var pendingRoomId = "";
    var detailRoomId = detailPage ? detailPage.dataset.roomId : "";
    var detailAdminName = detailPage ? detailPage.dataset.adminName : "담당자";
    var detailMemberName = detailPage ? detailPage.dataset.memberName : "이름 미등록";

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

  // CSRF 헤더 조회
  function getCsrfHeaders() {
    var tokenMeta = document.querySelector('meta[name="_csrf"]');
    var headerMeta = document.querySelector('meta[name="_csrf_header"]');

    if (!tokenMeta || !headerMeta) {
      return {};
    }

    var headers = {};
    headers[headerMeta.getAttribute("content")] = tokenMeta.getAttribute("content");
    return headers;
  }

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

  // 모달 열기/닫기
  function toggleModal(name, isOpen) {
    var modal = document.querySelector('[data-modal="' + name + '"]');
    if (!modal) {
      return;
    }

    modal.classList.toggle("is-hidden", !isOpen);
    modal.setAttribute("aria-hidden", isOpen ? "false" : "true");
  }

  // 관리자 메시지 HTML 추가
  function appendAdminMessage(message) {
    if (!adminMessageList || !message) {
      return;
    }

    var emptyMessage = adminMessageList.querySelector(".admin-chat-room__system--detail");
    if (emptyMessage && emptyMessage.textContent.indexOf("메시지") !== -1) {
      emptyMessage.remove();
    }

    var article = document.createElement("article");
    article.className = "admin-chat-detail-message admin-chat-detail-message--admin";
    article.style.animationDelay = "0ms";

    var content = document.createElement("div");
    content.className = "admin-chat-detail-message__content";

    var meta = document.createElement("div");
    meta.className = "admin-chat-detail-message__meta admin-chat-detail-message__meta--admin";

    var time = document.createElement("span");
    var createdAt = message.chMsCreDt ? new Date(message.chMsCreDt) : new Date();
    time.textContent = createdAt.toLocaleTimeString("ko-KR", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: false
    });

    var adminName = document.createElement("strong");
    adminName.textContent = detailAdminName || "담당자";

    meta.appendChild(time);
    meta.appendChild(adminName);

    var bubble = document.createElement("div");
    bubble.className = "admin-chat-detail-message__bubble admin-chat-detail-message__bubble--admin";
    bubble.textContent = message.chMsCon;

    content.appendChild(meta);
    content.appendChild(bubble);
    article.appendChild(content);
    adminMessageList.appendChild(article);
    if (adminMessageScrollBody) {
      adminMessageScrollBody.scrollTop = adminMessageScrollBody.scrollHeight;
    }
  }

  modalTriggers.forEach(function (trigger) {
    trigger.addEventListener("click", function () {
      pendingDetailUrl = trigger.dataset.detailUrl || "";
      pendingRoomId = trigger.dataset.roomId || "";
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

  // 관리자 상담 시작 비동기 요청
  function startAdminChatRoom() {
    if (!pendingRoomId) {
      return;
    }

    fetch("/api/admin/chat/rooms/" + pendingRoomId + "/start", {
      method: "PATCH",
      headers: getCsrfHeaders()
    }).then(function (response) {
      if (!response.ok) {
        throw new Error("상담 시작 요청에 실패했습니다.");
      }

      toggleModal("assign", false);

      if (listPage && pendingDetailUrl) {
        window.location.href = pendingDetailUrl;
      }
    }).catch(function (error) {
      console.error(error);
    });
  }

  // 관리자 상담 종료 비동기 요청
  function closeAdminChatRoom() {
    if (!detailRoomId) {
      return;
    }

    fetch("/api/admin/chat/rooms/" + detailRoomId + "/close", {
      method: "PATCH",
      headers: getCsrfHeaders()
    }).then(function (response) {
      if (!response.ok) {
        throw new Error("상담 종료 요청에 실패했습니다.");
      }

      toggleModal("close", false);
      setChatState("CLOSED");
    }).catch(function (error) {
      console.error(error);
    });
  }

  // 관리자 메시지 전송 비동기 요청
  function sendAdminChatMessage() {
    if (!detailRoomId || !adminMessageInput) {
      return;
    }

    var content = adminMessageInput.value.trim();
    if (!content) {
      adminMessageInput.focus();
      return;
    }

    var headers = getCsrfHeaders();
    headers["Content-Type"] = "application/json";

    fetch("/api/admin/chat/rooms/" + detailRoomId + "/messages", {
      method: "POST",
      headers: headers,
      body: JSON.stringify({
        chMsCon: content
      })
    }).then(function (response) {
      if (!response.ok) {
        throw new Error("메시지 전송 요청에 실패했습니다.");
      }

      return response.json();
    }).then(function (message) {
      adminMessageInput.value = "";
      appendAdminMessage(message);
      adminMessageInput.focus();
    }).catch(function (error) {
      console.error(error);
    });
  }

  if (assignConfirmButton) {
    assignConfirmButton.addEventListener("click", function () {
      startAdminChatRoom();
    });
  }

  if (closeConfirmButton) {
    closeConfirmButton.addEventListener("click", function () {
      closeAdminChatRoom();
    });
  }

  if (adminMessageSendButton) {
    adminMessageSendButton.addEventListener("click", function () {
      sendAdminChatMessage();
    });
  }

  // Enter 키 입력 시 관리자 메시지 전송
  if (adminMessageInput) {
    adminMessageInput.addEventListener("keydown", function (event) {
      if (event.key !== "Enter" || event.shiftKey) {
        return;
      }

      event.preventDefault();
      sendAdminChatMessage();
    });
  }

  if (detailPage) {
    setChatState(detailPage.dataset.chatStatus || "ONGOING");
    if (adminMessageScrollBody) {
      adminMessageScrollBody.scrollTop = adminMessageScrollBody.scrollHeight;
    }
    if (adminMessageInput && detailPage.dataset.chatStatus === "ONGOING") {
      adminMessageInput.focus();
    }
  }
});
