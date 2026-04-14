document.addEventListener("DOMContentLoaded", function () {
  var widget = document.querySelector("[data-member-chat]");
  if (!widget) {
    return;
  }

  var qrApiBaseUrl = widget ? (widget.dataset.qrApiBaseUrl || "") : "";
  var notificationSubscription = null; // 임욱 추가. 알림 구독을 위한 변수
  var panel = widget.querySelector(".member-chat-panel");
  var launcher = widget.querySelector("[data-widget-toggle]");
  var launcherBadge = widget.querySelector(".member-chat-launcher__badge");
  var chatbotContext = widget.querySelector("[data-chatbot-context]");
  var chatbotTopics = widget.querySelector("[data-chatbot-topics]");
  var chatbotResponse = widget.querySelector("[data-chatbot-response]");
  var chatbotActions = widget.querySelector("[data-chatbot-actions]");
  var chatListContent = widget.querySelector(".member-chat-content--list");
  var chatListEnd = widget.querySelector(".member-chat-list-end");
  var chatbotContent = widget.querySelector(".member-chat-content--chatbot");
  var chatRoomTitle = widget.querySelector("[data-chat-room-title]");
  var chatRoomMessages = widget.querySelector("[data-chat-room-messages]");
  var chatRoomCloseButton = widget.querySelector("[data-chat-room-close]");

  // 채팅 입력/전송/이미지 첨부 관련 DOM
  var chatRoomMessageInput = widget.querySelector("[data-member-message-input]");
  var chatRoomSendButton = widget.querySelector("[data-member-message-send]");
  var chatRoomImageTrigger = widget.querySelector("[data-member-image-trigger]");
  var chatRoomImageInput = widget.querySelector("[data-member-image-input]");

  var closeButtons = widget.querySelectorAll("[data-widget-close]");
  var viewButtons = widget.querySelectorAll("[data-view-target]");
  var navButtons = widget.querySelectorAll(".member-chat-bottom-nav__item");
  var chatListNavButton = widget.querySelector('.member-chat-bottom-nav__item[data-view-target="chat-list"]');
  var views = widget.querySelectorAll("[data-chat-view]");
  var modal = widget.querySelector("[data-chat-modal]");
  var modalCloseButtons = widget.querySelectorAll("[data-chat-modal-close]");
  var modalConfirmButton = widget.querySelector("[data-chat-modal-confirm]");
  var modalTitle = widget.querySelector("[data-chat-modal-title]");
  var modalDescription = widget.querySelector("[data-chat-modal-description]");
  var closeConfirmModal = widget.querySelector("[data-close-confirm-modal]");
  var closeConfirmCloseButtons = widget.querySelectorAll("[data-close-confirm-close]");
  var closeConfirmSubmitButton = widget.querySelector("[data-close-confirm-submit]");
  var loginModal = widget.querySelector("[data-login-modal]");
  var loginModalCloseButtons = widget.querySelectorAll("[data-login-modal-close]");
  var loginModalConfirmButton = widget.querySelector("[data-login-modal-confirm]");
  var newChatButton = widget.querySelector("[data-new-chat-trigger]");

  var isFirstTopicsLoaded = false;
  var chatbotStepStack = [];
  var currentTopicId = null;
  var currentTopicName = null;
  var currentChatRoomId = null;
  var currentChatRoomStatus = null;
  var currentChatRoomCloseReason = null;
  var hasLoadedMemberChatRooms = false;
  var stompClient = null;
  var isSendingMemberMessage = false;
  var roomSubscription = null;
  var summarySubscription = null;
  var wsConnected = false;
  var pendingChatRoom = null;
  var currentViewName = "chatbot";
  var memberImageBindingsInitialized = false;
  var panelHideTimer = null;
  var isAuthenticated = widget.dataset.authenticated === "true";
  var loginUrl = widget.dataset.loginUrl || "/auth/signin";
  var csrfToken = document.querySelector('meta[name="_csrf"]')?.content || "";
  var csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || "X-CSRF-TOKEN";
  var wsProtocol = window.location.protocol === "https:" ? "wss://" : "ws://";

  function connectMemberChatSocket() {
      // 사용자 위젯에서 현재 채팅방을 WebSocket으로 수신할 수 있게 연결을 만듦
      if (!window.StompJs) {
        return;
      }

      // 소켓이 없으면 새로 연결
      if (!stompClient) {
        stompClient = new StompJs.Client({
          brokerURL: wsProtocol + window.location.host + "/ws",
          reconnectDelay: 5000,
          debug: function () {}
        });

        // 연결 성공하면 현재 방 구독 함수, 사용자 위젯 채팅 목록/미읽음 갱신 함수를 호출
        stompClient.onConnect = function () {
          wsConnected = true;
          subscribeMemberSummary();
          subscribeCurrentChatRoom();
          subscribeNotificationCount(); // 임욱 추가.
        };

        stompClient.onWebSocketClose = function () {
          wsConnected = false;
        };

        stompClient.onStompError = function (frame) {
          console.error(frame);
        };

        stompClient.activate();
        return;
      }

      // 이미 연결돼 있으면 다시 구독
      if (wsConnected) {
        subscribeMemberSummary();
        subscribeCurrentChatRoom();
        subscribeNotificationCount(); // 임욱 추가.
      }
  }

  // 채팅방 구독
  function subscribeCurrentChatRoom() {
      // 현재 열려 있는 채팅방만 구독하고, 방이 바뀌면 기존 구독은 먼저 해제
      if (!stompClient || !wsConnected || !currentChatRoomId) {
        return;
      }

      // 기존 구독방이 있다면 구독 취소
      if (roomSubscription) {
        roomSubscription.unsubscribe();
      }

      // 현재 채팅방 구독
      roomSubscription = stompClient.subscribe("/sub/chat/rooms/" + currentChatRoomId, function (frame) {
        var message = JSON.parse(frame.body);

        if (message.eventType === "ROOM_STATUS") {
            currentChatRoomStatus = message.chRoStt;
            currentChatRoomCloseReason = message.chRoClsRsn || null;

            updateChatRoomComposerState(message.chRoStt);

            var emptyState = chatRoomMessages ? chatRoomMessages.querySelector(".member-chat-room-empty") : null;
            if (emptyState) {
              emptyState.remove();
            }

            var existingNotice = chatRoomMessages ? chatRoomMessages.querySelector(".member-chat-room-notice") :
          null;
            if (existingNotice) {
              existingNotice.remove();
            }

            if (message.chRoStt === "CLOSED" && chatRoomMessages) {
              appendClosedNotice({
                chRoStt: message.chRoStt,
                chRoClsRsn: message.chRoClsRsn
              });
            }

            loadMemberChatRooms({ animateList: false });
            return;
        }

        if (message.chMsSenTy === "USER") {
          var pendingMessage = chatRoomMessages ? chatRoomMessages.querySelector('[data-pending="true"]') : null;
          if (pendingMessage) {
            pendingMessage.removeAttribute("data-pending");
            pendingMessage.classList.remove("member-chat-animate-in");

            var pendingTime = pendingMessage.querySelector(".member-chat-message__body span");
            if (pendingTime) {
              pendingTime.textContent = formatChatMessageTime(message.chMsCreDt);
            }

            loadMemberChatRooms({ animateList: false });
            return;
          }
        }

        appendMemberChatMessage(message);

          if (message.chMsSenTy !== "USER") {
              // 실제로 위젯이 열려 있고 현재 채팅 상세를 보고 있을 때만 읽음 처리
              if (isCurrentChatRoom(message.chRoId)) {
                markCurrentChatRoomAsRead();
                return;
              }

              loadMemberChatRooms({ animateList: false });
              return;
          }

          loadMemberChatRooms({ animateList: false });
      });
  }

  function clearCurrentChatRoomSubscription() {
      if (roomSubscription) {
        roomSubscription.unsubscribe();
        roomSubscription = null;
      }

      currentChatRoomId = null;
      currentChatRoomStatus = null;
      currentChatRoomCloseReason = null;
  }

  // 사용자 미읽음 목록 갱신을 위한 구독
  function subscribeMemberSummary() {
      if (!stompClient || !wsConnected) {
        return;
      }

      var memberChatElement = document.querySelector("[data-member-chat]");
      if (!memberChatElement) {
        return;
      }

      var memberId = memberChatElement.dataset.memberId;
      if (!memberId) {
        return;
      }

      if (summarySubscription) {
        summarySubscription.unsubscribe();
      }

      summarySubscription = stompClient.subscribe("/sub/chat/users/" + memberId, function () {
        loadMemberChatRooms({ animateList: false });
      });
    }

  // 위젯 런처 미읽음 배지 표시 상태 반영
  function setLauncherUnreadBadgeVisible(isVisible) {
    if (!launcherBadge) {
      setChatListNavUnreadVisible(isVisible);
      return;
    }

    launcherBadge.classList.toggle("is-hidden", !isVisible);
    launcherBadge.setAttribute("aria-hidden", isVisible ? "false" : "true");
    setChatListNavUnreadVisible(isVisible);
  }

  function setChatListNavUnreadVisible(isVisible) {
    if (!chatListNavButton) {
      return;
    }

    chatListNavButton.classList.toggle("has-unread", isVisible);
    chatListNavButton.setAttribute("data-unread", isVisible ? "true" : "false");
  }

  function isChatRoomPanelOpen() {
      return !!panel && !panel.classList.contains("is-hidden");
    }

  function isCurrentChatRoom(chatRoomId) {
      return isChatRoomPanelOpen()
        && currentViewName === "chat-room"
        && currentChatRoomId !== null
        && String(currentChatRoomId) === String(chatRoomId);
  }

  // 챗봇 영역 하단 자동 스크롤
  function scrollChatbotToBottom() {
    if (!chatbotContent) {
      return;
    }

    requestAnimationFrame(function () {
      chatbotContent.scrollTo({
        top: chatbotContent.scrollHeight,
        behavior: "smooth"
      });
    });
  }

  // 챗봇 영역 상단 이동
  function scrollChatbotToTop() {
    if (!chatbotContent) {
      return;
    }

    requestAnimationFrame(function () {
      chatbotContent.scrollTo({
        top: 0,
        behavior: "smooth"
      });
    });
  }

  // 채팅 상세 하단으로 이동: 이미지가 늦게 로드돼도 마지막 메시지까지 보이게 유지
  function scrollChatRoomToBottom(useSmooth) {
    if (!chatRoomMessages) {
      return;
    }

    requestAnimationFrame(function () {
      requestAnimationFrame(function () {
        chatRoomMessages.scrollTo({
          top: chatRoomMessages.scrollHeight,
          behavior: useSmooth === true ? "smooth" : "auto"
        });
      });
    });
  }

  function settleChatRoomScrollAfterRender() {
    if (!chatRoomMessages) {
      return;
    }

    scrollChatRoomToBottom(false);

    var pendingImages = Array.prototype.filter.call(
      chatRoomMessages.querySelectorAll(".member-chat-message__image"),
      function (image) {
        return !image.complete;
      }
    );

    if (pendingImages.length === 0) {
      return;
    }

    var remaining = pendingImages.length;

    function handleSettled() {
      remaining -= 1;
      if (remaining <= 0) {
        scrollChatRoomToBottom(false);
      }
    }

    pendingImages.forEach(function (image) {
      image.addEventListener("load", handleSettled, { once: true });
      image.addEventListener("error", handleSettled, { once: true });
    });
  }

  // 챗봇 현재 단계 스냅샷 저장
  function pushChatbotSnapshot() {
    chatbotStepStack.push({
      contextHtml: chatbotContext ? chatbotContext.innerHTML : "",
      contextActive: chatbotContext ? chatbotContext.classList.contains("is-active") : false,
      topicsHtml: chatbotTopics ? chatbotTopics.innerHTML : "",
      responseHtml: chatbotResponse ? chatbotResponse.innerHTML : "",
      actionsHtml: chatbotActions ? chatbotActions.innerHTML : "",
      topicId: currentTopicId,
      topicName: currentTopicName
    });
  }

  // 챗봇 이전 단계 복원
  function restorePreviousChatbotStep() {
    var snapshot = chatbotStepStack.pop();
    if (!snapshot) {
      resetToFirstTopics();
      return;
    }

    if (chatbotContext) {
      chatbotContext.innerHTML = snapshot.contextHtml;
      chatbotContext.classList.toggle("is-active", snapshot.contextActive);
    }

    if (chatbotTopics) {
      chatbotTopics.innerHTML = snapshot.topicsHtml;
    }

    if (chatbotResponse) {
      chatbotResponse.innerHTML = snapshot.responseHtml;
    }

    if (chatbotActions) {
      chatbotActions.innerHTML = snapshot.actionsHtml;
      bindChatbotActionEvents();
    }

    currentTopicId = snapshot.topicId;
    currentTopicName = snapshot.topicName;
    scrollChatbotToBottom();
  }

  // 챗봇 하단 액션 버튼 이벤트 연결
  function bindChatbotActionEvents() {
    if (!chatbotActions) {
      return;
    }

    var restartButton = chatbotActions.querySelector("[data-chatbot-restart]");
    var consultButton = chatbotActions.querySelector("[data-chatbot-consult]");
    var backButton = chatbotActions.querySelector("[data-chatbot-back]");

    if (restartButton) {
      restartButton.onclick = function () {
        resetToFirstTopics();
      };
    }

    if (consultButton) {
      consultButton.onclick = function () {
        if (!currentTopicId) {
          openNewInquiryChatRoom().then(function (chatRoom) {
            handleChatRoomOpenResult(chatRoom);
          });
          return;
        }

        openChatRoomFromChatbot(currentTopicId).then(function (chatRoom) {
          handleChatRoomOpenResult(chatRoom);
        });
      };
    }

    if (backButton) {
      backButton.onclick = function () {
        restorePreviousChatbotStep();
      };
    }
  }

  // 챗봇 첫 화면 하단 액션 버튼 출력
  function renderChatbotHomeActions() {
    if (!chatbotActions) {
      return;
    }

    chatbotActions.innerHTML = "";

    var consultButton = document.createElement("button");
    var consultIcon = document.createElement("span");
    var consultLabel = document.createElement("span");

    consultButton.type = "button";
    consultButton.className = "member-chat-primary-button member-chat-primary-button--accent member-chat-action-button";
    consultButton.dataset.chatbotConsult = "true";
    consultIcon.className = "material-symbols-outlined member-chat-action-button__icon";
    consultIcon.textContent = "support_agent";
    consultLabel.textContent = "상담 연결";

    consultButton.appendChild(consultIcon);
    consultButton.appendChild(consultLabel);
    chatbotActions.appendChild(consultButton);
    bindChatbotActionEvents();
  }

  // 챗봇 단계 이동 뒤로 버튼 출력
  function renderTopicActions() {
    if (!chatbotActions) {
      return;
    }

    chatbotActions.innerHTML = "";

    if (chatbotStepStack.length === 0) {
      renderChatbotHomeActions();
      return;
    }

    var backButton = document.createElement("button");
    backButton.type = "button";
    backButton.className = "member-chat-secondary-button";
    backButton.classList.add("member-chat-animate-in");
    backButton.dataset.chatbotBack = "true";
    backButton.textContent = "이전 단계";
    chatbotActions.appendChild(backButton);
    bindChatbotActionEvents();
    scrollChatbotToBottom();
  }

  // 챗봇 상단 문맥 출력
  function renderChatbotContext(title, description) {
    if (!chatbotContext) {
      return;
    }

    if (!title && !description) {
      chatbotContext.innerHTML = "";
      chatbotContext.classList.remove("is-active");
      return;
    }

    chatbotContext.innerHTML = "";
    chatbotContext.classList.add("is-active");

    if (title) {
      var heading = document.createElement("strong");
      heading.textContent = title;
      chatbotContext.appendChild(heading);
    }

    if (description) {
      var guide = document.createElement("p");
      guide.textContent = description;
      chatbotContext.appendChild(guide);
    }

    scrollChatbotToBottom();
  }

  // 기존 생성방 모달 문구 출력
  function renderExistingRoomModalCopy(title, description) {
    if (modalTitle) {
      modalTitle.textContent = title;
    }

    if (modalDescription) {
      modalDescription.textContent = description;
    }
  }

  // 챗봇 질의 버튼 목록 출력
  function renderTopicButtons(topics) {
    if (!chatbotTopics) {
      return;
    }

    chatbotTopics.innerHTML = "";

    topics.forEach(function (topic, index) {
      var button = document.createElement("button");
      var title = document.createElement("span");
      var icon = document.createElement("span");

      button.type = "button";
      button.className = "member-chat-topic-button";
      button.classList.add("member-chat-animate-in");
      button.dataset.topicId = String(topic.cbTpId);

      title.textContent = topic.cbTpNm;
      icon.className = "material-symbols-outlined";
      icon.textContent = "arrow_forward";
      button.style.animationDelay = (index * 45) + "ms";

      button.appendChild(title);
      button.appendChild(icon);
      chatbotTopics.appendChild(button);
    });

    renderTopicActions();
    scrollChatbotToBottom();
  }

  // 선택한 질의 말풍선을 사용자 메시지처럼 출력
  function appendSelectedTopic(topicName) {
    if (!chatbotResponse || !topicName) {
      return;
    }

    var article = document.createElement("article");
    var bubble = document.createElement("div");
    var text = document.createElement("p");

    article.className = "member-chat-bot-stack member-chat-bot-stack--user";
    article.classList.add("member-chat-animate-in");
    bubble.className = "member-chat-bubble member-chat-bubble--user";
    text.textContent = topicName;

    bubble.appendChild(text);
    article.appendChild(bubble);
    chatbotResponse.appendChild(article);
    scrollChatbotToBottom();
  }

  // 챗봇 안내 말풍선 누적 출력
  function appendBotGuideMessage(message) {
    if (!chatbotResponse || !message) {
      return;
    }

    var article = document.createElement("article");
    var label = document.createElement("div");
    var icon = document.createElement("span");
    var brand = document.createElement("strong");
    var bubble = document.createElement("div");
    var text = document.createElement("p");

    article.className = "member-chat-bot-stack";
    article.classList.add("member-chat-animate-in");
    label.className = "member-chat-bubble__label";
    icon.className = "member-chat-bubble__icon material-symbols-outlined";
    icon.textContent = "smart_toy";
    brand.textContent = "BEEDAN";
    bubble.className = "member-chat-bubble member-chat-bubble--bot";
    text.textContent = message;

    label.appendChild(icon);
    label.appendChild(brand);
    bubble.appendChild(text);
    article.appendChild(label);
    article.appendChild(bubble);
    chatbotResponse.appendChild(article);
    scrollChatbotToBottom();
  }

  // 최종 응답 제목과 본문 출력
  function renderResponse(response) {
    if (!chatbotResponse) {
      return;
    }

    if (chatbotTopics) {
      chatbotTopics.innerHTML = "";
    }
    renderChatbotContext(null, null);

    var article = document.createElement("article");
    var title = document.createElement("h4");
    var content = document.createElement("p");
    var guide = document.createElement("small");

    article.className = "member-chat-response-card";
    article.classList.add("member-chat-animate-in");
    title.textContent = response.cbResTtl;
    content.textContent = response.cbResCon;
    guide.textContent = "추가 문의가 필요하시면 상담 연결을 이용해 주세요.";

    article.appendChild(title);
    article.appendChild(content);
    article.appendChild(guide);
    chatbotResponse.appendChild(article);
    scrollChatbotToBottom();
  }

  // 최종 응답 화면 액션 버튼 출력
  function renderResponseActions(response) {
    if (!chatbotActions) {
      return;
    }

    chatbotActions.innerHTML = "";

    if (response.cbResLnkBtnNm && response.cbResLnkUrl) {
      var linkButton = document.createElement("button");
      var linkIcon = document.createElement("span");
      var linkLabel = document.createElement("span");
      linkButton.type = "button";
      linkButton.className = "member-chat-secondary-button member-chat-action-button";
      linkButton.dataset.chatbotLink = "true";
      linkIcon.className = "material-symbols-outlined member-chat-action-button__icon";
      linkIcon.textContent = "open_in_new";
      linkLabel.textContent = response.cbResLnkBtnNm;
      linkButton.appendChild(linkIcon);
      linkButton.appendChild(linkLabel);
      linkButton.addEventListener("click", function () {
        window.location.href = response.cbResLnkUrl;
      });
      chatbotActions.appendChild(linkButton);
    }

    var consultButton = document.createElement("button");
    var consultIcon = document.createElement("span");
    var consultLabel = document.createElement("span");
    consultButton.type = "button";
    consultButton.className = "member-chat-primary-button member-chat-primary-button--accent member-chat-action-button";
    consultButton.dataset.chatbotConsult = "true";
    consultIcon.className = "material-symbols-outlined member-chat-action-button__icon";
    consultIcon.textContent = "support_agent";
    consultLabel.textContent = "상담 연결";
    consultButton.appendChild(consultIcon);
    consultButton.appendChild(consultLabel);
    chatbotActions.appendChild(consultButton);

    var restartButton = document.createElement("button");
    var restartIcon = document.createElement("span");
    var restartLabel = document.createElement("span");
    restartButton.type = "button";
    restartButton.className = "member-chat-secondary-button member-chat-action-button";
    restartButton.dataset.chatbotRestart = "true";
    restartIcon.className = "material-symbols-outlined member-chat-action-button__icon";
    restartIcon.textContent = "refresh";
    restartLabel.textContent = "처음으로";
    restartButton.appendChild(restartIcon);
    restartButton.appendChild(restartLabel);
    chatbotActions.appendChild(restartButton);
    bindChatbotActionEvents();
    scrollChatbotToBottom();
  }

  // 채팅방 상태 배지 정보 출력
  function getChatRoomStateMeta(status) {
    if (status === "CLOSED") {
      return { label: "종료", badgeClass: "member-chat-state member-chat-state--closed" };
    }

    if (status === "OPEN") {
      return { label: "접수", badgeClass: "member-chat-state member-chat-state--ongoing" };
    }

    return { label: "상담중", badgeClass: "member-chat-state member-chat-state--ongoing" };
  }

  // 목록 시간 형식 변환
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
  function renderMemberChatRooms(chatRooms, animateList) {
    if (!chatListContent) {
      return;
    }

    var fragment = document.createDocumentFragment();

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
      var stateMeta = getChatRoomStateMeta(chatRoom.chRoStt);
      var summaryMessage = chatRoom.lastMessageContent;
      var unread = isCurrentChatRoom(chatRoom.chRoId) ? false : !!chatRoom.unread;
      var unreadDot = null;

      if (!summaryMessage) {
        summaryMessage = chatRoom.chRoStt === "CLOSED"
          ? getClosedSummaryMessage(chatRoom)
          : "상담 대기중입니다.";
      }

      roomButton.type = "button";
      roomButton.className = "member-chat-room-item" + (unread ? " is-active" : "");
      if (animateList !== false) {
        roomButton.classList.add("member-chat-animate-in");
      }
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
      time.textContent = formatChatRoomTime(chatRoom.lastMessageCreatedAt || chatRoom.chRoCreDt);

      titleWrap.appendChild(title);
      titleWrap.appendChild(state);
      head.appendChild(titleWrap);
      head.appendChild(time);

      summary.className = "member-chat-room-item__summary";
      summaryText.textContent = summaryMessage;
      summary.appendChild(summaryText);

      if (unread) {
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

      fragment.appendChild(roomButton);
    });

    chatListContent.querySelectorAll(".member-chat-room-item").forEach(function (item) {
      item.remove();
    });

    if (chatListEnd) {
      chatListContent.insertBefore(fragment, chatListEnd);
    } else {
      chatListContent.appendChild(fragment);
    }
  }

  // 상세 메시지 시간 형식 변환
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

  function getChatMessageDateKey(dateTime) {
    if (!dateTime) {
      return "";
    }

    var date = new Date(dateTime);
    if (Number.isNaN(date.getTime())) {
      return "";
    }

    var year = date.getFullYear();
    var month = String(date.getMonth() + 1).padStart(2, "0");
    var day = String(date.getDate()).padStart(2, "0");
    return year + "-" + month + "-" + day;
  }

  function formatConversationStartDate(dateTime) {
    if (!dateTime) {
      return "";
    }

    var date = new Date(dateTime);
    if (Number.isNaN(date.getTime())) {
      return "";
    }

    var year = date.getFullYear();
    var month = String(date.getMonth() + 1).padStart(2, "0");
    var day = String(date.getDate()).padStart(2, "0");
    return "상담 시작 · " + year + "." + month + "." + day;
  }

  function formatChatDateDivider(dateTime) {
    if (!dateTime) {
      return "";
    }

    var date = new Date(dateTime);
    if (Number.isNaN(date.getTime())) {
      return "";
    }

    var now = new Date();
    var year = date.getFullYear();
    var month = String(date.getMonth() + 1).padStart(2, "0");
    var day = String(date.getDate()).padStart(2, "0");

    if (year === now.getFullYear()) {
      return month + "." + day;
    }

    return year + "." + month + "." + day;
  }

  function appendMemberConversationStart(dateTime) {
    if (!chatRoomMessages || !dateTime) {
      return;
    }

    var guide = document.createElement("div");
    guide.className = "member-chat-room-system member-chat-room-system--start";
    guide.textContent = formatConversationStartDate(dateTime);
    chatRoomMessages.appendChild(guide);
  }

  function appendMemberDateDivider(dateTime) {
    if (!chatRoomMessages || !dateTime) {
      return;
    }

    var divider = document.createElement("div");
    divider.className = "member-chat-room-system member-chat-room-system--date";
    divider.dataset.dateKey = getChatMessageDateKey(dateTime);
    divider.textContent = formatChatDateDivider(dateTime);
    chatRoomMessages.appendChild(divider);
  }

  function getLastMemberMessageDateKey() {
    if (!chatRoomMessages) {
      return "";
    }

    var lastMessage = chatRoomMessages.querySelector(".member-chat-message:last-of-type");
    if (!lastMessage) {
      return "";
    }

    return lastMessage.dataset.messageDateKey || "";
  }

  function appendMemberDateDividerIfNeeded(dateTime) {
    var nextDateKey = getChatMessageDateKey(dateTime);
    if (!nextDateKey) {
      return;
    }

    var lastDateKey = getLastMemberMessageDateKey();
    if (!lastDateKey || lastDateKey === nextDateKey) {
      return;
    }

    appendMemberDateDivider(dateTime);
  }

  // 견적 카드 본문 생성: 제목/설명(선택)/링크/QR을 카드형으로 묶음
  function buildMemberChatQuoteCard(message) {
    var card = document.createElement("div");
    card.className = "member-chat-quote-card";

    var title = document.createElement("strong");
    title.className = "member-chat-quote-card__title";
    title.textContent = message.chMsLnkTtl || "견적 초안";
    card.appendChild(title);

    if (message.chMsCon) {
      var desc = document.createElement("p");
      desc.className = "member-chat-quote-card__desc";
      desc.textContent = message.chMsCon;
      card.appendChild(desc);
    }

    var link = document.createElement("a");
    link.className = "member-chat-quote-card__link";
    link.href = message.chMsLnkUrl || "#";
    link.target = "_blank";
    link.rel = "noopener noreferrer";
    link.textContent = "견적 초안 열기";
    card.appendChild(link);

    if (message.chMsLnkUrl) {
        var qr = document.createElement("img");
        qr.className = "member-chat-quote-card__qr";
        qr.alt = "견적 QR 코드";

        // QR이 늦게 로드되면 카드 높이가 뒤늦게 커지므로, 로드 후 다시 하단 스크롤 맞춤
        qr.addEventListener("load", function () {
          scrollChatRoomToBottom(false);
        });

        qr.addEventListener("error", function () {
          scrollChatRoomToBottom(false);
        });

        if (qrApiBaseUrl) {
          qr.src = qrApiBaseUrl + encodeURIComponent(message.chMsLnkUrl);
        }
        card.appendChild(qr);
      }

    return card;
  }

  // 이미지 메시지 본문 생성: 이미지가 있으면 미리보기, 없으면 안내 문구 출력
  function buildMemberChatImageBubble(message, isUserMessage, shouldScrollOnLoad) {
    var bubble = document.createElement("div");
    bubble.className = "member-chat-message__bubble member-chat-message__bubble--image";

    if (isUserMessage) {
      bubble.classList.add("member-chat-message__bubble--accent");
    }

    if (message.imageFile && message.imageFile.fileUuid) {
      var link = document.createElement("a");
      link.className = "member-chat-message__image-link";
      link.href = "/api/files/view/" + message.imageFile.fileUuid;
      link.target = "_blank";
      link.rel = "noopener noreferrer";

      var image = document.createElement("img");
      image.className = "member-chat-message__image";
      image.src = "/api/files/view/" + message.imageFile.fileUuid;
      image.loading = "eager";
      image.decoding = "async";
      if (shouldScrollOnLoad) {
        image.addEventListener("load", function () {
          scrollChatRoomToBottom(false);
        });
      }
      image.addEventListener("load", function () {
        link.classList.add("is-loaded");
      });
      image.addEventListener("error", function () {
        link.classList.remove("is-loaded");
      });
      image.alt = "첨부 이미지";

      link.appendChild(image);
      bubble.appendChild(link);
    } else {
      var empty = document.createElement("p");
      empty.className = "member-chat-message__image-empty";
      empty.textContent = "이미지를 불러올 수 없습니다.";
      bubble.appendChild(empty);
    }

    return bubble;
  }

  // 메시지 타입에 따라 텍스트/이미지/견적카드 본문을 분기
  function buildMemberChatMessageBubble(message, isUserMessage, shouldScrollOnLoad) {
    var type = message.chMsTp || "TEXT";

    if (type === "IMAGE") {
      return buildMemberChatImageBubble(message, isUserMessage, shouldScrollOnLoad);
    }

    if (type === "QUOTE_CARD") {
      var quoteBubble = document.createElement("div");
      quoteBubble.className = "member-chat-message__bubble member-chat-message__bubble--quote-card";

      if (isUserMessage) {
        quoteBubble.classList.add("member-chat-message__bubble--accent");
      }

      quoteBubble.appendChild(buildMemberChatQuoteCard(message));
      return quoteBubble;
    }

    var textBubble = document.createElement("div");
    textBubble.className = "member-chat-message__bubble" + (isUserMessage ? " member-chat-message__bubble--accent" : "");
    textBubble.textContent = message.chMsCon || "";
    return textBubble;
  }

  function getClosedSummaryMessage(chatRoom) {
    if (chatRoom && chatRoom.chRoClsRsn === "AUTO") {
      return "3일 동안 메시지가 없어 자동 종료되었습니다.";
    }

    return "상담이 종료되었습니다.";
  }

  function getClosedDetailDescription() {
    if (currentChatRoomCloseReason === "AUTO") {
      return "3일 동안 메시지가 없어 채팅방이 자동 종료되었습니다.";
    }

    return "상담 시작 전에 종료된 채팅방입니다.";
  }

  function appendClosedNotice(chatRoomDetail) {
    if (!chatRoomMessages || !chatRoomDetail || chatRoomDetail.chRoStt !== "CLOSED") {
      return;
    }

    var notice = document.createElement("div");
    var icon = document.createElement("span");
    var text = document.createElement("p");

    notice.className = "member-chat-room-notice";
    icon.className = "material-symbols-outlined";
    icon.textContent = "schedule";
    text.textContent = chatRoomDetail.chRoClsRsn === "AUTO"
      ? "3일 동안 메시지가 없어 채팅방이 자동 종료되었습니다."
      : "종료된 상담은 추가 메시지를 보낼 수 없고, 이력만 확인할 수 있습니다.";

    notice.appendChild(icon);
    notice.appendChild(text);
    chatRoomMessages.appendChild(notice);
  }

  // 메시지 없는 상세 화면 안내 출력
  function renderEmptyChatRoomDetail() {
    if (!chatRoomMessages) {
      return;
    }

    var emptyState = document.createElement("div");
    var icon = document.createElement("span");
    var title = document.createElement("strong");
    var description = document.createElement("p");

    emptyState.className = "member-chat-room-empty";
    icon.className = "material-symbols-outlined member-chat-room-empty__icon";
    icon.textContent = currentChatRoomStatus === "CLOSED" ? "chat_bubble" : "edit_square";
    title.textContent = currentChatRoomStatus === "CLOSED"
      ? "대화 이력이 없습니다."
      : "문의 내용을 남겨주세요.";
    description.textContent = currentChatRoomStatus === "CLOSED"
      ? getClosedDetailDescription()
      : "상담원이 내용을 확인한 뒤 순차적으로 답변드립니다.";

    emptyState.appendChild(icon);
    emptyState.appendChild(title);
    emptyState.appendChild(description);
    chatRoomMessages.appendChild(emptyState);
  }

  // 채팅 입력 영역 상태 반영
  function updateChatRoomComposerState(status) {
    var isClosed = status === "CLOSED";
    currentChatRoomStatus = status;

    if (chatRoomMessageInput) {
      chatRoomMessageInput.disabled = isClosed;
      chatRoomMessageInput.placeholder = isClosed
        ? (currentChatRoomCloseReason === "AUTO"
            ? "자동 종료된 채팅방입니다."
            : "종료된 채팅방입니다.")
        : "메시지를 입력해 주세요.";
    }

    if (chatRoomSendButton) {
      chatRoomSendButton.disabled = isClosed;
    }

    if (chatRoomImageTrigger) {
      chatRoomImageTrigger.disabled = isClosed;
    }

    if (chatRoomImageInput) {
      chatRoomImageInput.disabled = isClosed;
    }

    if (!memberImageBindingsInitialized && chatRoomImageTrigger && chatRoomImageInput) {
      // 첨부 버튼 클릭 시 숨겨진 file input 열기
      chatRoomImageTrigger.addEventListener("click", function () {
        if (!currentChatRoomId || chatRoomMessageInput.disabled) {
          return;
        }
        chatRoomImageInput.click();
      });

      // 파일 선택이 끝나면 바로 이미지 메시지 전송
      chatRoomImageInput.addEventListener("change", function () {
        var file = chatRoomImageInput.files && chatRoomImageInput.files[0];
        if (!file) {
          return;
        }
        sendMemberChatImage(file);
      });

      memberImageBindingsInitialized = true;
    }

    if (chatRoomCloseButton) {
      chatRoomCloseButton.disabled = isClosed;
    }
  }

  // 채팅방 상세 화면 출력
  function renderMemberChatRoomDetail(chatRoomDetail) {
    if (!chatRoomMessages || !chatRoomTitle) {
      return;
    }

    currentChatRoomId = chatRoomDetail.chRoId;
    currentChatRoomCloseReason = chatRoomDetail.chRoClsRsn || null;
    updateChatRoomComposerState(chatRoomDetail.chRoStt);
    chatRoomTitle.textContent = chatRoomDetail.chRoTtl;
    chatRoomMessages.innerHTML = "";

    appendMemberConversationStart(chatRoomDetail.chRoCreDt);

    if (!chatRoomDetail.messages || chatRoomDetail.messages.length === 0) {
      renderEmptyChatRoomDetail();
      return;
    }

    chatRoomDetail.messages.forEach(function (message, index) {
      if (index > 0) {
        var previousMessage = chatRoomDetail.messages[index - 1];
        if (getChatMessageDateKey(previousMessage.chMsCreDt) !== getChatMessageDateKey(message.chMsCreDt)) {
          appendMemberDateDivider(message.chMsCreDt);
        }
      }

      var article = document.createElement("article");
      var body = document.createElement("div");
      var bubble = document.createElement("div");
      var time = document.createElement("span");
      var isUserMessage = message.chMsSenTy === "USER";

      article.className = "member-chat-message " + (isUserMessage ? "member-chat-message--right" : "member-chat-message--left");
      article.dataset.messageCreatedAt = message.chMsCreDt || "";
      article.dataset.messageDateKey = getChatMessageDateKey(message.chMsCreDt);
      if (!isUserMessage) {
        var avatar = document.createElement("div");
        avatar.className = "member-chat-message__avatar";
        avatar.textContent = "B";
        article.appendChild(avatar);
      }

      body.className = "member-chat-message__body";
      bubble = buildMemberChatMessageBubble(message, isUserMessage, false);
      time.textContent = formatChatMessageTime(message.chMsCreDt);

      body.appendChild(bubble);
      body.appendChild(time);
      article.appendChild(body);
      chatRoomMessages.appendChild(article);
    });

    appendClosedNotice(chatRoomDetail);
  }

  // 사용자 메시지 1건 즉시 추가
  function appendMemberChatMessage(message) {
    // 서버에서 확정된 사용자/관리자 메시지를 채팅방 화면에 추가
    if (!chatRoomMessages || !message) {
      return;
    }

    var emptyState = chatRoomMessages.querySelector(".member-chat-room-empty");
    if (emptyState) {
      emptyState.remove();
    }
    // 실시간 수신 메시지 날짜가 바뀌었으면 먼저 날짜 divider 추가
    appendMemberDateDividerIfNeeded(message.chMsCreDt);

    var article = document.createElement("article");
    var body = document.createElement("div");
    var bubble = document.createElement("div");
    var time = document.createElement("span");
    var isUserMessage = message.chMsSenTy === "USER";

    article.className = "member-chat-message " + (isUserMessage ? "member-chat-message--right" : "member-chat-message--left");
    article.classList.add("member-chat-animate-in");
    article.dataset.messageCreatedAt = message.chMsCreDt || "";
    article.dataset.messageDateKey = getChatMessageDateKey(message.chMsCreDt);

    if (!isUserMessage) {
      var avatar = document.createElement("div");
      avatar.className = "member-chat-message__avatar";
      avatar.textContent = "B";
      article.appendChild(avatar);
    }

    body.className = "member-chat-message__body";
    bubble = buildMemberChatMessageBubble(message, isUserMessage, true);
    time.textContent = formatChatMessageTime(message.chMsCreDt);

    body.appendChild(bubble);
    body.appendChild(time);
    article.appendChild(body);
    chatRoomMessages.appendChild(article);

    scrollChatRoomToBottom(true);
  }

  function appendPendingMemberChatMessage(messageText) {
    // 전송 직후 바로 보이도록 임시 사용자 메시지를 먼저 붙임
    if (!chatRoomMessages || !messageText) {
      return null;
    }

    var emptyState = chatRoomMessages.querySelector(".member-chat-room-empty");
    if (emptyState) {
      emptyState.remove();
    }

    var article = document.createElement("article");
    var body = document.createElement("div");
    var bubble = document.createElement("div");
    var time = document.createElement("span");

    article.className = "member-chat-message member-chat-message--right";
    article.classList.add("member-chat-animate-in");
    article.dataset.pending = "true";

    body.className = "member-chat-message__body";
    bubble.className = "member-chat-message__bubble member-chat-message__bubble--accent";
    bubble.textContent = messageText;
    time.textContent = "";

    body.appendChild(bubble);
    body.appendChild(time);
    article.appendChild(body);
    chatRoomMessages.appendChild(article);

    scrollChatRoomToBottom(true);

    return article;
  }

  // 회원 채팅방 목록 비동기 조회
  function loadMemberChatRooms(options) {
    var animateList = !options || options.animateList !== false;

    return fetch("/api/chat/rooms", {
      cache: "no-store"
    })
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Failed to load member chat rooms.");
        }

        return response.json();
      })
      .then(function (chatRooms) {
        var memberChatRooms = chatRooms || [];
        hasLoadedMemberChatRooms = true;
        renderMemberChatRooms(memberChatRooms, animateList);
        setLauncherUnreadBadgeVisible(memberChatRooms.some(function (chatRoom) {
          return !isCurrentChatRoom(chatRoom.chRoId) && chatRoom.unread;
        }));
      })
      .catch(function (error) {
        hasLoadedMemberChatRooms = false;
        console.error(error);
      });
  }

  // 챗봇 첫 화면 복귀
  function resetToFirstTopics() {
    chatbotStepStack = [];
    currentTopicId = null;
    currentTopicName = null;
    renderChatbotContext(null, null);
    if (chatbotResponse) {
      chatbotResponse.innerHTML = "";
    }
    if (chatbotActions) {
      chatbotActions.innerHTML = "";
    }
    renderChatbotHomeActions();
    return loadFirstLevelTopics().then(function () {
      scrollChatbotToTop();
    });
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
        chatbotStepStack = [];
        renderChatbotContext(null, null);
        if (chatbotResponse) {
          chatbotResponse.innerHTML = "";
        }
        if (chatbotActions) {
          chatbotActions.innerHTML = "";
        }
        renderTopicButtons(topics);
        renderChatbotHomeActions();
        isFirstTopicsLoaded = true;
        scrollChatbotToTop();
      })
      .catch(function (error) {
        console.error(error);
      });
  }

  // 다음 단계 비동기 조회
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
        appendSelectedTopic(currentTopicName);

        if (nextStep.stepType === "TOPIC") {
          renderTopicButtons(nextStep.topics || []);
          renderChatbotContext(currentTopicName, "아래 항목에서 원하시는 내용을 선택해 주세요.");
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

  // 채팅방 상세 비동기 조회
  function loadMemberChatRoomDetail(chRoId) {
    return fetch("/api/chat/rooms/" + chRoId, {
      cache: "no-store"
    })
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Failed to load member chat room detail.");
        }

        return response.json();
      })
      .then(function (chatRoomDetail) {
        renderMemberChatRoomDetail(chatRoomDetail); // -> currentChatRoomId 세팅
        connectMemberChatSocket(); // currentChatRoomId 기준으로 구독
        setPanelOpen(true);
        setView("chat-room");
        settleChatRoomScrollAfterRender();
        loadMemberChatRooms({ animateList: false });
      })
      .catch(function (error) {
        console.error(error);
      });
  }

  function markCurrentChatRoomAsRead() {
      if (!currentChatRoomId) {
        return Promise.resolve();
      }

      return fetch("/api/chat/rooms/" + currentChatRoomId, {
        cache: "no-store"
      })
        .then(function (response) {
          if (!response.ok) {
            throw new Error("Failed to refresh member chat room detail.");
          }

          return response.json();
        })
        .then(function (chatRoomDetail) {
          currentChatRoomCloseReason = chatRoomDetail.chRoClsRsn || null;
          updateChatRoomComposerState(chatRoomDetail.chRoStt);
          loadMemberChatRooms({ animateList: false });
        })
        .catch(function (error) {
          console.error(error);
        });
  }

  // 회원 메시지 비동기 전송
  function sendMemberChatMessage() {
    // 입력 메시지를 REST로 전송 요청하고, 성공 반영은 WebSocket 수신 결과로 처리
    if (!currentChatRoomId || !chatRoomMessageInput || isSendingMemberMessage) {
      return;
    }

    var messageContent = chatRoomMessageInput.value;
    if (!messageContent || !messageContent.trim()) {
      return;
    }

    var trimmedMessage = messageContent.trim();

    // 서버 응답을 기다리지 않고 입력창을 먼저 비워 연속 입력 시 체감 지연을 줄임
    chatRoomMessageInput.value = "";
    chatRoomMessageInput.focus();
    isSendingMemberMessage = true;

    var pendingMessageElement = appendPendingMemberChatMessage(trimmedMessage);

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
        chMsCon: trimmedMessage
      })
    })
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Failed to send member chat message.");
        }

        return response.json();
      })
      .then(function () {
        return loadMemberChatRooms({ animateList: false });
      })
      .catch(function (error) {
        if (pendingMessageElement) {
          pendingMessageElement.remove();
        }

        // 전송 실패 시 사용자가 다시 보낼 수 있게 입력값을 복구
        if (chatRoomMessageInput && !chatRoomMessageInput.value.trim()) {
          chatRoomMessageInput.value = trimmedMessage;
          chatRoomMessageInput.focus();
        }

        console.error(error);
      })
      .finally(function () {
        isSendingMemberMessage = false;
      });
  }

  // 사용자 이미지 전송: 파일 선택 직후 바로 업로드/전송 비동기 처리
  function sendMemberChatImage(file) {
    if (!currentChatRoomId || !file) {
      return;
    }

    var formData = new FormData();
    formData.append("imageFile", file);

    var headers = {};
    if (csrfToken) {
      headers[csrfHeader] = csrfToken;
    }

    return fetch("/api/chat/rooms/" + currentChatRoomId + "/image", {
      method: "POST",
      headers: headers,
      body: formData
    })
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Failed to send member chat image.");
        }

        return response.json();
      })
      .then(function () {
        if (chatRoomImageInput) {
          chatRoomImageInput.value = "";
        }
        return loadMemberChatRooms({ animateList: false });
      })
      .catch(function (error) {
        console.error(error);
        if (chatRoomImageInput) {
          chatRoomImageInput.value = "";
        }
      });
  }

  // 회원 채팅방 종료 비동기 요청
  function closeMemberChatRoom() {
    if (!currentChatRoomId) {
      return;
    }

    var headers = {};
    if (csrfToken) {
      headers[csrfHeader] = csrfToken;
    }

    return fetch("/api/chat/rooms/" + currentChatRoomId + "/close", {
      method: "PATCH",
      headers: headers
    })
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Failed to close member chat room.");
        }

        currentChatRoomId = null;
        currentChatRoomStatus = null;
        return loadMemberChatRooms();
      })
      .then(function () {
        setView("chat-list");
      })
      .catch(function (error) {
        console.error(error);
      });
  }

  // 챗봇 상담 연결 채팅방 비동기 생성 또는 기존 방 반환
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

  // 새 문의하기 채팅방 비동기 생성 또는 기존 방 반환
  function openNewInquiryChatRoom() {
    var headers = {};
    if (csrfToken) {
      headers[csrfHeader] = csrfToken;
    }

    return fetch("/api/chat/rooms", {
      method: "POST",
      headers: headers
    })
      .then(function (response) {
        if (!response.ok) {
          throw new Error("Failed to open new inquiry chat room.");
        }

        return response.json();
      })
      .catch(function (error) {
        console.error(error);
      });
  }

  // 채팅방 오픈 결과 분기 처리
  function handleChatRoomOpenResult(chatRoom) {
    if (!chatRoom) {
      return;
    }

    if (chatRoom.existingRoom) {
      pendingChatRoom = chatRoom;
      renderExistingRoomModalCopy("이미 진행 중인 채팅이 있습니다.", "현재 진행 중인 상담방으로 이동하시겠습니까?");
      setModalOpen(true);
      return;
    }

    loadMemberChatRoomDetail(chatRoom.chRoId);
  }

  // 위젯 패널 열기 또는 닫기
  function setPanelOpen(isOpen) {
    if (!panel) {
      return;
    }

    if (panelHideTimer) {
      window.clearTimeout(panelHideTimer);
      panelHideTimer = null;
    }

    if (isOpen) {
      panel.classList.remove("is-hidden");
      requestAnimationFrame(function () {
        panel.classList.add("member-chat-panel--open");
      });
    } else {
      panel.classList.remove("member-chat-panel--open");
      panelHideTimer = window.setTimeout(function () {
        panel.classList.add("is-hidden");
      }, 180);
    }

    panel.setAttribute("aria-hidden", isOpen ? "false" : "true");
    launcher.setAttribute("aria-expanded", isOpen ? "true" : "false");
    if (!isOpen) {
      setModalOpen(false);
      setCloseConfirmModalOpen(false);
    }
  }

  // 위젯 화면 전환
  function setView(viewName, transitionDirection) {
    var navViewName = viewName === "chat-room" ? "chat-list" : viewName;
    var direction = transitionDirection;

    if (currentViewName === "chat-room" && viewName !== "chat-room") {
      clearCurrentChatRoomSubscription();
    }

    if (!direction) {
      if (currentViewName === "chat-list" && viewName === "chat-room") {
        direction = "forward";
      } else if (currentViewName === "chat-room" && viewName === "chat-list") {
        direction = "back";
      } else {
        direction = "neutral";
      }
    }

    views.forEach(function (view) {
      view.classList.remove("member-chat-view--slide-forward", "member-chat-view--slide-back", "member-chat-view--still");
      view.classList.toggle("is-active", view.dataset.chatView === viewName);
      if (view.dataset.chatView === viewName) {
        if (direction === "forward") {
          view.classList.add("member-chat-view--slide-forward");
        } else if (direction === "back") {
          view.classList.add("member-chat-view--slide-back");
        } else {
          view.classList.add("member-chat-view--still");
        }
      }
    });

    navButtons.forEach(function (button) {
      button.classList.toggle("is-active", button.dataset.viewTarget === navViewName);
    });

    if (viewName === "chat-room") {
      requestAnimationFrame(function () {
        requestAnimationFrame(function () {
          if (chatRoomMessageInput && !chatRoomMessageInput.disabled) {
            chatRoomMessageInput.focus();
          }
        });
      });
    }

    currentViewName = viewName;
  }

  // 기존 생성 채팅방 안내 모달 열기 또는 닫기
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

  // 로그인 안내 모달 열기 또는 닫기
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

  // 상담 종료 확인 모달 열기 또는 닫기
  function setCloseConfirmModalOpen(isOpen) {
    if (!closeConfirmModal) {
      return;
    }

    if (!isOpen && closeConfirmModal.contains(document.activeElement)) {
      document.activeElement.blur();
      if (chatRoomCloseButton) {
        chatRoomCloseButton.focus();
      }
    }

    closeConfirmModal.classList.toggle("is-hidden", !isOpen);
    closeConfirmModal.setAttribute("aria-hidden", isOpen ? "false" : "true");
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

  document.addEventListener("click", function (event) {
    if (!panel || panel.classList.contains("is-hidden")) {
          return;
        }

    var eventPath = typeof event.composedPath === "function" ? event.composedPath() : [];
    if (eventPath.includes(widget) || widget.contains(event.target)) {
      return;
    }

    setPanelOpen(false);
  });

  closeButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      setPanelOpen(false);
    });
  });

  viewButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      setPanelOpen(true);
      if (button.dataset.viewTarget === "chat-list" && isAuthenticated) {
        setView("chat-list");
        if (!hasLoadedMemberChatRooms) {
          loadMemberChatRooms({ animateList: false });
        }
        return;
      }
      setView(button.dataset.viewTarget);
    });
  });

  if (newChatButton) {
    newChatButton.addEventListener("click", function () {
      setPanelOpen(true);
      openNewInquiryChatRoom().then(function (chatRoom) {
        handleChatRoomOpenResult(chatRoom);
      });
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

  closeConfirmCloseButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      setCloseConfirmModalOpen(false);
    });
  });

  if (modalConfirmButton) {
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

      pushChatbotSnapshot();
      currentTopicName = topicButton.querySelector("span") ? topicButton.querySelector("span").textContent : null;
      loadNextStep(topicButton.dataset.topicId);
    });
  }

  if (chatListContent) {
    chatListContent.addEventListener("click", function (event) {
      var roomButton = event.target.closest(".member-chat-room-item");
      if (!roomButton || !roomButton.dataset.chatRoomId) {
        return;
      }

      loadMemberChatRoomDetail(roomButton.dataset.chatRoomId);
    });
  }

  if (chatRoomSendButton) {
    chatRoomSendButton.addEventListener("click", function () {
      sendMemberChatMessage();
    });
  }

  if (chatRoomMessageInput) {
    chatRoomMessageInput.addEventListener("keydown", function (event) {
      if (event.key !== "Enter" || event.shiftKey) {
        return;
      }

      event.preventDefault();
      sendMemberChatMessage();
    });
  }

  if (chatRoomCloseButton) {
    chatRoomCloseButton.addEventListener("click", function () {
      setCloseConfirmModalOpen(true);
    });
  }

  if (closeConfirmSubmitButton) {
    closeConfirmSubmitButton.addEventListener("click", function () {
      closeMemberChatRoom().then(function () {
        setCloseConfirmModalOpen(false);
      });
    });
  }

  setPanelOpen(false);
  setView("chatbot");
  setModalOpen(false);
  setCloseConfirmModalOpen(false);
  setLoginModalOpen(false);
  setLauncherUnreadBadgeVisible(false);

  if (isAuthenticated) {
    connectMemberChatSocket();
    loadMemberChatRooms({ animateList: false });
  }

  function subscribeNotificationCount() {
        if (!stompClient || !wsConnected) { return; }
        if (notificationSubscription) {notificationSubscription.unsubscribe();} // 기존 구독이 있다면 해제

        notificationSubscription = stompClient.subscribe('/user/sub/unread-count', function (message) {
                var count = parseInt(message.body, 10);

                // [핵심] UI 수정 코드는 모두 지우고, 이벤트만 발생시킵니다.
                // 이를 통해 notification.js가 동작하게 합니다.
                var event = new CustomEvent('newNotification', { detail: { count: count } });
                window.dispatchEvent(event);
        });
    }

});


