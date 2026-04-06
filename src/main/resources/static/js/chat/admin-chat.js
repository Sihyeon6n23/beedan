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
  var detailMemberBizName = detailPage ? detailPage.dataset.memberBizName : "상호명 미등록";
  var detailMemberName = detailPage ? detailPage.dataset.memberName : "이름 미등록";
  var detailCanWrite = detailPage ? detailPage.dataset.canWrite === "true" : false;
  var stompClient = null;
  var roomSubscription = null;
  var wsConnected = false;

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

    var bubble = document.createElement("div");
    bubble.className = "admin-chat-detail-message__bubble admin-chat-detail-message__bubble--admin";
    bubble.textContent = message.chMsCon;

    var messageTime = document.createElement("span");
    messageTime.className = "admin-chat-detail-message__time admin-chat-detail-message__time--admin";
    messageTime.textContent = time.textContent;

    content.appendChild(meta);
    content.appendChild(bubble);
    content.appendChild(messageTime);
    article.appendChild(content);
    adminMessageList.appendChild(article);
    if (adminMessageScrollBody) {
      adminMessageScrollBody.scrollTop = adminMessageScrollBody.scrollHeight;
    }
  }

  // 메시지를 받았을 때 메시지 append
  function appendClientMessage(message) {
      if (!adminMessageList || !message) return;

      var emptyMessage = adminMessageList.querySelector(".admin-chat-room__system--detail");
      if (emptyMessage && emptyMessage.textContent.indexOf("메시지") !== -1) {
        emptyMessage.remove();
      }

      var article = document.createElement("article");
      article.className = "admin-chat-detail-message admin-chat-detail-message--client";

      var avatar = document.createElement("div");
      avatar.className = "admin-chat-detail-message__avatar";

      var avatarIcon = document.createElement("span");
      avatarIcon.className = "material-symbols-outlined";
      avatarIcon.textContent = "corporate_fare";

      avatar.appendChild(avatarIcon);

      var content = document.createElement("div");
      content.className = "admin-chat-detail-message__content";

      var meta = document.createElement("div");
      meta.className = "admin-chat-detail-message__meta";

      var sender = document.createElement("strong");
      sender.textContent = (detailMemberBizName || "상호명 미등록") + " · " + (detailMemberName || "이름 미등록");

      var time = document.createElement("span");
      var createdAt = message.chMsCreDt ? new Date(message.chMsCreDt) : new Date();
      time.textContent = createdAt.toLocaleTimeString("ko-KR", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
      });

      meta.appendChild(sender);

      var bubble = document.createElement("div");
      bubble.className = "admin-chat-detail-message__bubble";
      bubble.textContent = message.chMsCon;

      var messageTime = document.createElement("span");
      messageTime.className = "admin-chat-detail-message__time";
      messageTime.textContent = time.textContent;

      content.appendChild(meta);
      content.appendChild(bubble);
      content.appendChild(messageTime);
      article.appendChild(avatar);
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

  // 관리자가 입력한 메시지를 전송 비동기 요청(서버에 저장 요청)
  function sendAdminChatMessage() {
    if (!detailRoomId || !adminMessageInput || !detailCanWrite) {
      return;
    }

    var content = adminMessageInput.value.trim();
    if (!content) {
      adminMessageInput.focus();
      return;
    }

    // CSRF 헤더 포함해서 POST /api/admin/chat/rooms/{roomId}/messages 요청
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
//      appendAdminMessage(message); // HTTP 기반 일 때 사용(WebSocket 없을때)
      adminMessageInput.focus();
    }).catch(function (error) {
      console.error(error);
    });
  }

  // 관리자가 특정 채팅방을 실시간으로 구독(관리자 실시간 수신용 + 화면 반영)
  function connectAdminChatSocket() {
    if (!detailPage || !detailRoomId || !window.StompJs) {
        return;
    }

    // WebSocket/STOMP 클라이언트 생성
    stompClient = new StompJs.Client({
        brokerURL: "ws://" + window.location.host + "/ws",
        reconnectDelay: 5000,
        debug: function () {}
    });

    stompClient.onConnect = function () {
        wsConnected = true;

        if (roomSubscription) {
            roomSubscription.unsubscribe();
        }

        // 연결 성공 시 해당 채팅방 구독
        roomSubscription = stompClient.subscribe("/sub/chat/rooms/" + detailRoomId, function (frame) {
            var message = JSON.parse(frame.body);

            // 관리자 메시지면
            if (message.chMsSenTy === "ADMIN") {
                appendAdminMessage(message);
                return;
            }

            // 사용자 메시지면
            appendClientMessage(message);
        });
    };

    stompClient.onWebSocketClose = function () {
        wsConnected = false;
    };

    stompClient.onStompError = function (frame) {
        console.error(frame);
    };

    stompClient.activate(); // 실제 연결
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

  // 상세 진입 시
  if (detailPage) {
    setChatState(detailPage.dataset.chatStatus || "ONGOING");
    if (adminMessageScrollBody) {
      adminMessageScrollBody.scrollTop = adminMessageScrollBody.scrollHeight;
    }
    if (adminMessageInput && detailPage.dataset.chatStatus === "ONGOING" && detailCanWrite) {
      adminMessageInput.focus();
    }

    connectAdminChatSocket(); // 소켓 연결
  }
});
