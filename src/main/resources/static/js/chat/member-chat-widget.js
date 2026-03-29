document.addEventListener("DOMContentLoaded", function () {
  var widget = document.querySelector("[data-member-chat]");
  if (!widget) {
    return;
  }

  var panel = widget.querySelector(".member-chat-panel");
  var launcher = widget.querySelector("[data-widget-toggle]");
  var chatbotTopics = widget.querySelector("[data-chatbot-topics]");
  var chatbotResponse = widget.querySelector("[data-chatbot-response]");
  var chatbotActions = widget.querySelector("[data-chatbot-actions]");
  var chatListContent = widget.querySelector(".member-chat-content--list");
  var chatListEnd = widget.querySelector(".member-chat-list-end");
  var chatRoomTitle = widget.querySelector("[data-chat-room-title]");
  var chatRoomMessages = widget.querySelector("[data-chat-room-messages]");
  var chatRoomCloseButton = widget.querySelector("[data-chat-room-close]");
  var chatRoomMessageInput = widget.querySelector(".member-chat-room-footer input");
  var chatRoomSendButton = widget.querySelector(".member-chat-send-button");
  var closeButtons = widget.querySelectorAll("[data-widget-close]");
  var viewButtons = widget.querySelectorAll("[data-view-target]");
  var navButtons = widget.querySelectorAll(".member-chat-bottom-nav__item");
  var views = widget.querySelectorAll("[data-chat-view]");
  var modal = widget.querySelector("[data-chat-modal]");
  var modalCloseButtons = widget.querySelectorAll("[data-chat-modal-close]");
  var modalConfirmButton = widget.querySelector("[data-chat-modal-confirm]");
  var loginModal = widget.querySelector("[data-login-modal]");
  var loginModalCloseButtons = widget.querySelectorAll("[data-login-modal-close]");
  var loginModalConfirmButton = widget.querySelector("[data-login-modal-confirm]");
  var newChatButton = widget.querySelector("[data-new-chat-trigger]");
  var isFirstTopicsLoaded = false;
  var currentTopicId = null;
  var currentChatRoomId = null;
  var pendingChatRoom = null;
  var isAuthenticated = widget.dataset.authenticated === "true";
  var loginUrl = widget.dataset.loginUrl || "/auth/signin";
  var csrfToken = document.querySelector('meta[name="_csrf"]')?.content || "";
  var csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || "X-CSRF-TOKEN";

  // 챗봇 질의 버튼 목록 출력
  function renderTopicButtons(topics) {
    if (!chatbotTopics) {
      return;
    }

    chatbotTopics.innerHTML = "";
    if (chatbotResponse) {
      chatbotResponse.innerHTML = "";
    }
    if (chatbotActions) {
      chatbotActions.innerHTML = "";
    }

    topics.forEach(function (topic) {
      var button = document.createElement("button");
      var title = document.createElement("span");
      var icon = document.createElement("span");

      button.type = "button";
      button.className = "member-chat-topic-button";
      button.dataset.topicId = String(topic.cbTpId);

      title.textContent = topic.cbTpNm;

      icon.className = "material-symbols-outlined";
      icon.textContent = "arrow_forward";

      button.appendChild(title);
      button.appendChild(icon);
      chatbotTopics.appendChild(button);
    });
  }

  // 최종 응답 제목/본문 출력
  function renderResponse(response) {
    if (!chatbotResponse) {
      return;
    }

    if (chatbotTopics) {
      chatbotTopics.innerHTML = "";
    }
    chatbotResponse.innerHTML = "";

    var title = document.createElement("h4");
    var content = document.createElement("p");

    title.textContent = response.cbResTtl;
    content.textContent = response.cbResCon;

    chatbotResponse.appendChild(title);
    chatbotResponse.appendChild(content);
  }

  // 최종 응답 화면 액션 버튼 구성
  function renderResponseActions(response) {
    if (!chatbotActions) {
      return;
    }

    chatbotActions.innerHTML = "";

    if (response.cbResLnkBtnNm && response.cbResLnkUrl) {
      var linkButton = document.createElement("button");
      linkButton.type = "button";
      linkButton.className = "member-chat-primary-button";
      linkButton.textContent = response.cbResLnkBtnNm;
      linkButton.addEventListener("click", function () {
        window.location.href = response.cbResLnkUrl;
      });
      chatbotActions.appendChild(linkButton);
    }

    var consultButton = document.createElement("button");
    consultButton.type = "button";
    consultButton.className = "member-chat-primary-button";
    consultButton.textContent = "상담사 연결";
    consultButton.addEventListener("click", function () {
      if (!currentTopicId) {
        return;
      }

      openChatRoomFromChatbot(currentTopicId).then(function (chatRoom) {
        if (!chatRoom) {
          return;
        }

        if (chatRoom.existingRoom) {
          pendingChatRoom = chatRoom;
          setModalOpen(true);
          return;
        }

        loadMemberChatRoomDetail(chatRoom.chRoId);
      });
    });
    chatbotActions.appendChild(consultButton);

    var restartButton = document.createElement("button");
    restartButton.type = "button";
    restartButton.className = "member-chat-primary-button";
    restartButton.textContent = "처음으로";
    restartButton.addEventListener("click", function () {
      resetToFirstTopics();
    });
    chatbotActions.appendChild(restartButton);
  }

  // 채팅방 상태 배지 정보 변환
  function getChatRoomStateMeta(status) {
    if (status === "CLOSED") {
      return { label: "종료", badgeClass: "member-chat-state member-chat-state--closed" };
    }

    if (status === "OPEN") {
      return { label: "접수", badgeClass: "member-chat-state member-chat-state--ongoing" };
    }

    return { label: "상담중", badgeClass: "member-chat-state member-chat-state--ongoing" };
  }

  // 목록 시간 표시 형식 변환
  function formatChatRoomTime(dateTime) {
    if (!dateTime) {
      return "방금";
    }

    var date = new Date(dateTime);
    if (Number.isNaN(date.getTime())) {
      return "방금";
    }

    var now = new Date();
    var isSameDay = now.toDateString() === date.toDateString();
    if (isSameDay) {
      return date.toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit", hour12: false });
    }

    return date.toLocaleDateString("ko-KR", { month: "2-digit", day: "2-digit" });
  }

  // 회원 채팅방 목록 출력
  function renderMemberChatRooms(chatRooms) {
    if (!chatListContent) {
      return;
    }

    chatListContent.querySelectorAll(".member-chat-room-item").forEach(function (item) {
      item.remove();
    });

    chatRooms.forEach(function (chatRoom) {
      var roomButton = document.createElement("button");
      var iconWrap = document.createElement("div");
      var icon = document.createElement("span");
      var body = document.createElement("div");
      var head = document.createElement("div");
      var titleWrap = document.createElement("div");
      var title = document.createElement("strong");
      var state = document.createElement("span");
      var time = document.createElement("span");
      var summary = document.createElement("div");
      var summaryText = document.createElement("p");
      var blade = document.createElement("span");
      var unreadDot = null;
      var stateMeta = getChatRoomStateMeta(chatRoom.chRoStt);
      var summaryMessage = chatRoom.lastMessageContent || "상담 대기 중입니다.";
      var displayTime = formatChatRoomTime(chatRoom.lastMessageCreatedAt || chatRoom.chRoCreDt);

      roomButton.type = "button";
      roomButton.className = "member-chat-room-item" + (chatRoom.unread ? " is-active" : "");
      roomButton.dataset.chatRoomId = String(chatRoom.chRoId);

      iconWrap.className = "member-chat-room-item__icon";
      icon.className = "material-symbols-outlined";
      icon.textContent = chatRoom.chRoStt === "CLOSED" ? "chat_bubble" : "support_agent";
      iconWrap.appendChild(icon);

      body.className = "member-chat-room-item__body";
      head.className = "member-chat-room-item__head";
      titleWrap.className = "member-chat-room-item__title";
      title.textContent = chatRoom.chRoTtl;
      state.className = stateMeta.badgeClass;
      state.textContent = stateMeta.label;
      time.className = "member-chat-room-item__time";
      time.textContent = displayTime;

      titleWrap.appendChild(title);
      titleWrap.appendChild(state);
      head.appendChild(titleWrap);
      head.appendChild(time);

      summary.className = "member-chat-room-item__summary";
      summaryText.textContent = summaryMessage;
      summary.appendChild(summaryText);

      if (chatRoom.unread) {
        unreadDot = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        unreadDot.setAttribute("class", "member-chat-room-item__unread");
        unreadDot.setAttribute("aria-hidden", "true");
        unreadDot.setAttribute("viewBox", "0 0 10 10");
        unreadDot.setAttribute("focusable", "false");

        var circle = document.createElementNS("http://www.w3.org/2000/svg", "circle");
        circle.setAttribute("cx", "5");
        circle.setAttribute("cy", "5");
        circle.setAttribute("r", "5");
        unreadDot.appendChild(circle);
        summary.appendChild(unreadDot);
      }

      body.appendChild(head);
      body.appendChild(summary);

      blade.className = "member-chat-room-item__blade";
      blade.setAttribute("aria-hidden", "true");

      roomButton.appendChild(iconWrap);
      roomButton.appendChild(body);
      roomButton.appendChild(blade);

      if (chatListEnd) {
        chatListContent.insertBefore(roomButton, chatListEnd);
      } else {
        chatListContent.appendChild(roomButton);
      }
    });
  }

  // 상세 메시지 시간 표시 형식 변환
  function formatChatMessageTime(dateTime) {
    if (!dateTime) {
      return "";
    }

    var date = new Date(dateTime);
    if (Number.isNaN(date.getTime())) {
      return "";
    }

    return date.toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit", hour12: false });
  }

  // 메시지 없는 상세 화면 안내 출력
  function renderEmptyChatRoomDetail() {
    if (!chatRoomMessages) {
      return;
    }

    var emptyText = document.createElement("p");
    emptyText.className = "member-chat-room-empty";
    emptyText.textContent = "아직 등록된 메시지가 없습니다.";
    chatRoomMessages.appendChild(emptyText);
  }

  // 채팅방 상세 화면 출력
  function renderMemberChatRoomDetail(chatRoomDetail) {
    if (!chatRoomMessages || !chatRoomTitle) {
      return;
    }

    currentChatRoomId = chatRoomDetail.chRoId;
    chatRoomTitle.textContent = chatRoomDetail.chRoTtl;
    chatRoomMessages.innerHTML = "";

    if (!chatRoomDetail.messages || chatRoomDetail.messages.length === 0) {
      renderEmptyChatRoomDetail();
      return;
    }

    chatRoomDetail.messages.forEach(function (message) {
      var article = document.createElement("article");
      var body = document.createElement("div");
      var bubble = document.createElement("div");
      var time = document.createElement("span");
      var isUserMessage = message.chMsSenTy === "USER";

      article.className = "member-chat-message " + (isUserMessage ? "member-chat-message--right" : "member-chat-message--left");

      if (!isUserMessage) {
        var avatar = document.createElement("div");
        avatar.className = "member-chat-message__avatar";
        avatar.textContent = "B";
        article.appendChild(avatar);
      }

      body.className = "member-chat-message__body";
      bubble.className = "member-chat-message__bubble" + (isUserMessage ? " member-chat-message__bubble--accent" : "");
      bubble.textContent = message.chMsCon;
      time.textContent = formatChatMessageTime(message.chMsCreDt);

      body.appendChild(bubble);
      body.appendChild(time);
      article.appendChild(body);
      chatRoomMessages.appendChild(article);
    });
  }

  // 회원 채팅방 목록 비동기 조회
  function loadMemberChatRooms() {
    return fetch("/api/chat/rooms")
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Failed to load member chat rooms.");
        }

        return response.json();
      })
      .then(function (chatRooms) {
        renderMemberChatRooms(chatRooms || []);
      })
      .catch(function (error) {
        console.error(error);
      });
  }

  // 챗봇 첫 화면 복귀
  function resetToFirstTopics() {
    currentTopicId = null;
    return loadFirstLevelTopics();
  }

  // 1차 질의 목록 비동기 조회
  function loadFirstLevelTopics() {
    return fetch("/api/chatbot/topics/first")
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Failed to load first-level topics.");
        }

        return response.json();
      })
      .then(function (topics) {
        renderTopicButtons(topics);
        isFirstTopicsLoaded = true;
      })
      .catch(function (error) {
        console.error(error);
      });
  }

  // 선택한 질의의 다음 단계(하위 질의 또는 최종 응답)를 비동기로 불러옴
  function loadNextStep(topicId) {
    currentTopicId = topicId;

    return fetch("/api/chatbot/topics/" + topicId + "/next")
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Failed to load next chatbot step.");
        }

        return response.json();
      })
      .then(function (nextStep) {
        if (nextStep.stepType === "TOPIC") {
          renderTopicButtons(nextStep.topics || []);
          return;
        }

        if (nextStep.stepType === "RESPONSE" && nextStep.response) {
          renderResponse(nextStep.response);
          renderResponseActions(nextStep.response);
        }
      })
      .catch(function (error) {
        console.error(error);
      });
  }

  // 채팅방 상세를 비동기로 불러와 chat-room 화면으로 이동
  function loadMemberChatRoomDetail(chRoId) {
    return fetch("/api/chat/rooms/" + chRoId)
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Failed to load member chat room detail.");
        }

        return response.json();
      })
      .then(function (chatRoomDetail) {
        renderMemberChatRoomDetail(chatRoomDetail);
        setPanelOpen(true);
        setView("chat-room");
      })
      .catch(function (error) {
        console.error(error);
      });
  }

  // 현재 상세 채팅방에 회원 메시지를 비동기로 전송하고 저장 후 상세를 다시 불러옴
  function sendMemberChatMessage() {
    if (!currentChatRoomId || !chatRoomMessageInput) {
      return;
    }

    var messageContent = chatRoomMessageInput.value;
    if (!messageContent || !messageContent.trim()) {
      return;
    }

    var headers = {
      "Content-Type": "application/json"
    };

    if (csrfToken) {
      headers[csrfHeader] = csrfToken;
    }

    return fetch("/api/chat/rooms/" + currentChatRoomId + "/messages", {
      method: "POST",
      headers: headers,
      body: JSON.stringify({
        chMsCon: messageContent
      })
    })
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Failed to send member chat message.");
        }

        return response.json();
      })
      .then(function () {
        chatRoomMessageInput.value = "";
        return loadMemberChatRoomDetail(currentChatRoomId);
      })
      .catch(function (error) {
        console.error(error);
      });
  }

  // 챗봇 상담 연결 요청으로 채팅방을 생성하거나 기존 활성 방을 반환받음
  function openChatRoomFromChatbot(topicId) {
    var headers = {};
    if (csrfToken) {
      headers[csrfHeader] = csrfToken;
    }

    return fetch("/api/chat/rooms/from-chatbot/" + topicId, {
      method: "POST",
      headers: headers
    })
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Failed to open chat room from chatbot.");
        }

        return response.json();
      })
      .catch(function (error) {
        console.error(error);
      });
  }

  // 위젯 패널 열림/닫힘 상태를 제어
  function setPanelOpen(isOpen) {
    panel.classList.toggle("is-hidden", !isOpen);
    panel.setAttribute("aria-hidden", isOpen ? "false" : "true");
    launcher.setAttribute("aria-expanded", isOpen ? "true" : "false");
    if (!isOpen) {
      setModalOpen(false);
    }
  }

  // 현재 보고 있는 위젯 화면을 전환
  function setView(viewName) {
    var navViewName = viewName === "chat-room" ? "chat-list" : viewName;

    views.forEach(function (view) {
      view.classList.toggle("is-active", view.dataset.chatView === viewName);
    });

    navButtons.forEach(function (button) {
      button.classList.toggle("is-active", button.dataset.viewTarget === navViewName);
    });

    if (viewName === "chat-list" && isAuthenticated) {
      loadMemberChatRooms();
    }
  }

  // 기존 활성 채팅방 안내 모달을 열고 닫음
  function setModalOpen(isOpen) {
    if (!modal) {
      return;
    }

    if (!isOpen && modal.contains(document.activeElement)) {
      document.activeElement.blur();
      if (launcher) {
        launcher.focus();
      }
    }

    modal.classList.toggle("is-hidden", !isOpen);
    modal.setAttribute("aria-hidden", isOpen ? "false" : "true");
  }

  // 비로그인 사용자용 로그인 안내 모달을 열고 닫음
  function setLoginModalOpen(isOpen) {
    if (!loginModal) {
      return;
    }

    if (!isOpen && loginModal.contains(document.activeElement)) {
      document.activeElement.blur();
      if (launcher) {
        launcher.focus();
      }
    }

    loginModal.classList.toggle("is-hidden", !isOpen);
    loginModal.setAttribute("aria-hidden", isOpen ? "false" : "true");
  }

  launcher.addEventListener("click", function () {
    if (!isAuthenticated) {
      setLoginModalOpen(true);
      return;
    }

    var willOpen = panel.classList.contains("is-hidden");
    setPanelOpen(willOpen);
    if (willOpen) {
      setView("chatbot");
      if (!isFirstTopicsLoaded) {
        loadFirstLevelTopics();
      }
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
      pendingChatRoom = null;
      setModalOpen(false);
    });
  });

  loginModalCloseButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      setLoginModalOpen(false);
    });
  });

  if (modalConfirmButton) {
    // 기존 활성 방 상세 화면 이동
    modalConfirmButton.addEventListener("click", function () {
      if (!pendingChatRoom) {
        setModalOpen(false);
        return;
      }

      var targetRoomId = pendingChatRoom.chRoId;
      pendingChatRoom = null;
      setModalOpen(false);
      loadMemberChatRoomDetail(targetRoomId);
    });
  }

  if (loginModalConfirmButton) {
    loginModalConfirmButton.addEventListener("click", function () {
      window.location.href = loginUrl;
    });
  }

  if (chatbotTopics) {
    chatbotTopics.addEventListener("click", function (event) {
      var topicButton = event.target.closest(".member-chat-topic-button");
      if (!topicButton) {
        return;
      }

      loadNextStep(topicButton.dataset.topicId);
    });
  }

  if (chatListContent) {
    // 채팅방 목록 클릭은 이벤트 위임으로 처리
    chatListContent.addEventListener("click", function (event) {
      var roomButton = event.target.closest(".member-chat-room-item");
      if (!roomButton || !roomButton.dataset.chatRoomId) {
        return;
      }

      loadMemberChatRoomDetail(roomButton.dataset.chatRoomId);
    });
  }

  if (chatRoomSendButton) {
    // 전송 버튼 클릭 시 현재 상세 채팅방에 메시지를 저장
    chatRoomSendButton.addEventListener("click", function () {
      sendMemberChatMessage();
    });
  }

  if (chatRoomMessageInput) {
    // 입력창에서 Enter를 누르면 메시지를 전송
    chatRoomMessageInput.addEventListener("keydown", function (event) {
      if (event.key !== "Enter" || event.shiftKey) {
        return;
      }

      event.preventDefault();
      sendMemberChatMessage();
    });
  }

  if (chatRoomCloseButton) {
    // 종료 기능은 이후 단계에서 연결
    chatRoomCloseButton.addEventListener("click", function () {
      if (!currentChatRoomId) {
        return;
      }
    });
  }

  setPanelOpen(false);
  setView("chatbot");
  setModalOpen(false);
  setLoginModalOpen(false);
});