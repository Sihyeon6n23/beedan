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
  var pendingChatRoom = null;
  var isAuthenticated = widget.dataset.authenticated === "true";
  var loginUrl = widget.dataset.loginUrl || "/auth/signin";
  // CSRF 토큰은 레이아웃 공통 meta 태그에서 읽음
  var csrfToken = document.querySelector('meta[name="_csrf"]')?.content || "";
  var csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || "X-CSRF-TOKEN";

  // 버튼 목록을 받아와 챗봇 질의 영역에 다시 출력
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

  // 최종 응답은 제목과 본문 출력 (임시)
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

  // 최종 응답 화면에서 관련 페이지 이동, 상담사 연결, 처음으로 버튼 출력
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

        // 이미 활성 방이 있으면 모달을 띄워 이동 여부를 한 번 더 확인
        if (chatRoom.existingRoom) {
          pendingChatRoom = chatRoom;
          setModalOpen(true);
          return;
        }

        renderCreatedChatRoom(chatRoom);
        setPanelOpen(true);
        setView("chat-list");
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

  // 생성되거나 반환된 채팅방을 목록 상단에 바로 출력
  function renderCreatedChatRoom(chatRoom) {
    if (!chatListContent) {
      return;
    }

    var existingRoomElement = chatListContent.querySelector('[data-chat-room-id="' + chatRoom.chRoId + '"]');
    if (existingRoomElement) {
      existingRoomElement.remove();
    }

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

    roomButton.type = "button";
    roomButton.className = "member-chat-room-item member-chat-room-item--ongoing is-active";
    roomButton.dataset.viewTarget = "chat-room";
    roomButton.dataset.chatRoomId = String(chatRoom.chRoId);

    iconWrap.className = "member-chat-room-item__icon";
    icon.className = "material-symbols-outlined";
    icon.textContent = "support_agent";
    iconWrap.appendChild(icon);

    body.className = "member-chat-room-item__body";
    head.className = "member-chat-room-item__head";
    titleWrap.className = "member-chat-room-item__title";
    title.textContent = chatRoom.chRoTtl;
    state.className = "member-chat-state member-chat-state--ongoing";
    state.textContent = chatRoom.existingRoom ? "진행 중" : "접수됨";
    time.className = "member-chat-room-item__time";
    time.textContent = "방금";

    titleWrap.appendChild(title);
    titleWrap.appendChild(state);
    head.appendChild(titleWrap);
    head.appendChild(time);

    summary.className = "member-chat-room-item__summary";
    summaryText.textContent = chatRoom.existingRoom
      ? "이미 진행 중인 채팅방으로 이동할 수 있습니다."
      : "채팅방이 생성되었습니다. 상담사를 기다리는 중입니다.";
    summary.appendChild(summaryText);

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
      chatListContent.prepend(roomButton);
    }
  }

  // 챗봇 첫 화면으로 돌아가 1차 질의 목록 다시 출력
  function resetToFirstTopics() {
    currentTopicId = null;
    return loadFirstLevelTopics();
  }

  // 챗봇 첫 화면에 노출할 1차 질의 목록 조회
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

  // 사용자가 선택한 질의의 다음 단계가 2차 질의 목록인지 최종 응답인지 조회
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

  // 챗봇 최종 응답에서 상담사 연결을 누르면 활성 방을 반환하거나 새 방을 생성
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

    // 모달을 닫을 때는 내부 버튼에 남아 있는 포커스를 위젯 런처로 돌림
    if (!isOpen && modal.contains(document.activeElement)) {
      document.activeElement.blur();
      if (launcher) {
        launcher.focus();
      }
    }

    modal.classList.toggle("is-hidden", !isOpen);
    modal.setAttribute("aria-hidden", isOpen ? "false" : "true");
  }

  function setLoginModalOpen(isOpen) {
    if (!loginModal) {
      return;
    }

    // 로그인 안내 모달을 닫을 때도 포커스를 런처로 돌린다.
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
    // 비로그인 사용자는 위젯을 열지 않고 로그인 안내 모달만 보여줌
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
    // 모달 확인 시 기존 활성 방을 목록에 표시하고 채팅 목록으로 이동
    modalConfirmButton.addEventListener("click", function () {
      if (!pendingChatRoom) {
        setModalOpen(false);
        return;
      }

      renderCreatedChatRoom(pendingChatRoom);
      pendingChatRoom = null;
      setModalOpen(false);
      setPanelOpen(true);
      setView("chat-list");
    });
  }

  if (loginModalConfirmButton) {
    loginModalConfirmButton.addEventListener("click", function () {
      window.location.href = loginUrl;
    });
  }

  if (chatbotTopics) {
    // 동적으로 생성된 topic 버튼도 처리할 수 있도록 컨테이너에 이벤트를 위임
    chatbotTopics.addEventListener("click", function (event) {
      var topicButton = event.target.closest(".member-chat-topic-button");
      if (!topicButton) {
        return;
      }

      loadNextStep(topicButton.dataset.topicId);
    });
  }

  setPanelOpen(false);
  setView("chatbot");
  setModalOpen(false);
  setLoginModalOpen(false);
});
