document.addEventListener("DOMContentLoaded", function () {
  var filterGroups = document.querySelectorAll("[data-filter-group]");
  var listPage = document.querySelector(".admin-chat-list-page");
  var detailPage = document.querySelector(".admin-chat-detail-page");
  var stateButtons = document.querySelectorAll("[data-chat-state-target]");
  var modalTriggers = document.querySelectorAll("[data-modal-open]");
  var modalClosers = document.querySelectorAll("[data-modal-close]");
  var assignConfirmButton = document.querySelector("[data-assign-confirm]");
  var closeConfirmButton = document.querySelector("[data-modal-confirm-close]");
  var adminActionErrorTitle = document.querySelector("[data-admin-action-error-title]");
  var adminActionErrorLead = document.querySelector("[data-admin-action-error-lead]");
  var adminActionErrorDescription = document.querySelector("[data-admin-action-error-description]");
  var adminMessageInput = document.querySelector("[data-admin-message-input]");
  var adminMessageSendButton = document.querySelector("[data-admin-message-send]");
  var adminMessageList = document.querySelector("[data-admin-message-list]");
  var qrApiBaseUrl = detailPage ? (detailPage.dataset.qrApiBaseUrl || "") : "";
  var adminImageTrigger = document.querySelector("[data-admin-image-trigger]");
  var adminImageInput = document.querySelector("[data-admin-image-input]");
  var adminMessageScrollBody = document.querySelector(".admin-chat-detail-body");
  var adminChatToast = document.querySelector("[data-admin-chat-toast]");
  var adminChatToastText = document.querySelector("[data-admin-chat-toast-text]");
  var adminChatFeedback = document.querySelector("[data-admin-chat-feedback]");
  var adminChatFeedbackText = document.querySelector("[data-admin-chat-feedback-text]");
  var pendingDetailUrl = "";
  var pendingRoomId = "";
  var isSendingAdminMessage = false;
  var detailRoomId = detailPage ? detailPage.dataset.roomId : "";
  var adminChatToastTimer = null;
  var detailAdminName = detailPage ? detailPage.dataset.adminName : "담당자";
  var detailMemberBizName = detailPage ? detailPage.dataset.memberBizName : "상호명 미등록";
  var detailMemberName = detailPage ? detailPage.dataset.memberName : "이름 미등록";
  var detailCanWrite = detailPage ? detailPage.dataset.canWrite === "true" : false;
  var stompClient = null;
  var roomSubscription = null;
  var summarySubscription = null;
  var wsConnected = false;
  var isListRefreshing = false;
  var feedbackBox = document.querySelector("[data-admin-chat-feedback]");
  var feedbackText = document.querySelector("[data-admin-chat-feedback-text]");
  var feedbackTimer = null;
  var wsProtocol = window.location.protocol === "https:" ? "wss://" : "ws://";

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

  function showAdminChatFeedback(message) {
    if (!adminChatFeedback || !adminChatFeedbackText) {
      return;
    }

    adminChatFeedbackText.textContent = message;
    adminChatFeedback.classList.remove("is-hidden");
    adminChatFeedback.setAttribute("aria-hidden", "false");
  }

  function hideAdminChatFeedback() {
    if (!adminChatFeedback) {
      return;
    }

    adminChatFeedback.classList.add("is-hidden");
    adminChatFeedback.setAttribute("aria-hidden", "true");
  }

  function showAdminChatToast(message) {
      if (!adminChatToast || !adminChatToastText || !message) {
        return;
      }

      adminChatToastText.textContent = message;

      if (adminChatToastTimer) {
        window.clearTimeout(adminChatToastTimer);
        adminChatToastTimer = null;
      }

      adminChatToast.classList.remove("is-hidden");
      requestAnimationFrame(function () {
        adminChatToast.classList.add("is-visible");
        adminChatToast.setAttribute("aria-hidden", "false");
      });

      // 짧게 보여주고 자동으로 사라지게
      adminChatToastTimer = window.setTimeout(function () {
        adminChatToast.classList.remove("is-visible");
        adminChatToast.setAttribute("aria-hidden", "true");

        window.setTimeout(function () {
          adminChatToast.classList.add("is-hidden");
        }, 200);
      }, 2600);
    }

   function setAdminChatListLoading(isLoading) {
        if (!listPage) {
          return;
        }

        var toolbar = document.querySelector(".admin-chat-shell--toolbar");
        var list = document.querySelector(".admin-chat-shell--list");
        var pagination = document.querySelector(".admin-chat-shell--pagination");

        [toolbar, list, pagination].forEach(function (element) {
          if (!element) {
            return;
          }

          element.classList.toggle("is-loading", isLoading);
          element.setAttribute("aria-busy", isLoading ? "true" : "false");
        });
   }

   // 지금 커서가 검색창에 있는지 확인
   function shouldPauseAdminChatSummaryRefresh() {
       var searchInput = document.querySelector('[data-admin-chat-search-form] input[name="keyword"]');
       if (!searchInput) {
         return false;
       }

       var isFocused = document.activeElement === searchInput;
       var hasDraft = searchInput.value !== searchInput.defaultValue;

       return isFocused || hasDraft;
   }

  // /admin/chat/list?... HTML을 받아옴 -> 툴바, 목록, 페이지네이션만 추출해서 현재 화면이랑 교체, URL 최신화
  async function refreshAdminChatList(requestUrl, pushHistory) {
        if (!listPage) {
          return;
        }

        if (isListRefreshing) { // 중복 클릭 잠금
          return;
        }

        isListRefreshing = true;
        setAdminChatListLoading(true); // 로딩 시각화

        try {
          var response = await fetch(requestUrl, {
            headers: {
              "X-Requested-With": "XMLHttpRequest"
            }
          });

          if (!response.ok) {
            throw new Error("관리자 채팅 목록을 불러오지 못했습니다.");
          }

          var html = await response.text();
          var parser = new DOMParser();
          var doc = parser.parseFromString(html, "text/html");

          var nextToolbar = doc.querySelector(".admin-chat-shell--toolbar");
          var nextList = doc.querySelector(".admin-chat-shell--list");
          var nextPagination = doc.querySelector(".admin-chat-shell--pagination");

          var currentToolbar = document.querySelector(".admin-chat-shell--toolbar");
          var currentList = document.querySelector(".admin-chat-shell--list");
          var currentPagination = document.querySelector(".admin-chat-shell--pagination");

          if (!nextToolbar || !nextList || !nextPagination || !currentToolbar || !currentList || !currentPagination) {
            throw new Error("관리자 채팅 목록 화면을 갱신할 수 없습니다.");
          }

          currentToolbar.replaceWith(nextToolbar);
          currentList.replaceWith(nextList);
          currentPagination.replaceWith(nextPagination);

          // 목록 갱신이 성공했으면 이전 에러 상태 숨김
          hideAdminChatFeedback();

          if (pushHistory !== false) {
            window.history.pushState({}, "", requestUrl);
          }
        } catch (error) {
            showAdminChatFeedback("목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
            console.error(error);
        } finally { // 성공/실패 상관없이 잠금 해제
          isListRefreshing = false;
          setAdminChatListLoading(false);
        }
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

    if (nextState === "CLOSED") {
        moveClosedPanelToBottom();
    }

    var closeTrigger = document.querySelector("[data-close-trigger]");
    if (closeTrigger) {
      closeTrigger.classList.toggle("is-hidden", nextState === "CLOSED");
    }

    stateButtons.forEach(function (button) {
      button.classList.toggle("is-active", button.dataset.chatStateTarget === nextState);
    });
  }

  function moveClosedPanelToBottom() {
    if (!adminMessageList) {
      return;
    }

    var closedPanel = adminMessageList.querySelector("[data-closed-panel]");
    if (!closedPanel) {
      return;
    }

    adminMessageList.appendChild(closedPanel);
  }

  function removeEmptyDetailPlaceholder() {
    if (!adminMessageList) {
      return;
    }

    var messageArticles = adminMessageList.querySelectorAll(".admin-chat-detail-message");
    if (messageArticles.length > 0) {
      return;
    }

    var systemMessages = adminMessageList.querySelectorAll(".admin-chat-room__system--detail");
    if (!systemMessages.length) {
      return;
    }

    // 메시지가 하나도 없을 때 마지막에 렌더된 안내 문구가 빈 상태 문구
    var emptyPlaceholder = systemMessages[systemMessages.length - 1];
        emptyPlaceholder.remove();
  }

  function formatAdminChatMessageTime(createdAtValue) {
    var createdAt = createdAtValue ? new Date(createdAtValue) : new Date();
    return createdAt.toLocaleTimeString("ko-KR", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: false
    });
  }

  function getAdminChatMessageDateKey(createdAtValue) {
    if (!createdAtValue) {
      return "";
    }

    var createdAt = new Date(createdAtValue);
    if (Number.isNaN(createdAt.getTime())) {
      return "";
    }

    var year = createdAt.getFullYear();
    var month = String(createdAt.getMonth() + 1).padStart(2, "0");
    var day = String(createdAt.getDate()).padStart(2, "0");
    return year + "-" + month + "-" + day;
  }

  function formatAdminChatDateDivider(createdAtValue) {
    if (!createdAtValue) {
      return "";
    }

    var createdAt = new Date(createdAtValue);
    if (Number.isNaN(createdAt.getTime())) {
      return "";
    }

    var now = new Date();
    var year = createdAt.getFullYear();
    var month = String(createdAt.getMonth() + 1).padStart(2, "0");
    var day = String(createdAt.getDate()).padStart(2, "0");

    if (year === now.getFullYear()) {
      return month + "." + day;
    }

    return year + "." + month + "." + day;
  }

  function createAdminChatDateDivider(createdAtValue) {
    var divider = document.createElement("div");
    divider.className = "admin-chat-room__system admin-chat-room__system--detail admin-chat-room__system--date";
    divider.dataset.dateKey = getAdminChatMessageDateKey(createdAtValue);
    divider.textContent = formatAdminChatDateDivider(createdAtValue);
    return divider;
  }

  function getLastAdminMessageDateKey() {
    if (!adminMessageList) {
      return "";
    }

    var messageArticles = adminMessageList.querySelectorAll(".admin-chat-detail-message");
    if (!messageArticles.length) {
      return "";
    }

    return messageArticles[messageArticles.length - 1].dataset.messageDateKey || "";
  }

  function appendAdminDateDividerIfNeeded(createdAtValue) {
    if (!adminMessageList) {
      return;
    }

    var nextDateKey = getAdminChatMessageDateKey(createdAtValue);
    var lastDateKey = getLastAdminMessageDateKey();

    if (!nextDateKey || !lastDateKey || nextDateKey === lastDateKey) {
      return;
    }

    adminMessageList.appendChild(createAdminChatDateDivider(createdAtValue));
  }

  function renderExistingAdminDateDividers() {
    if (!adminMessageList) {
      return;
    }

    adminMessageList.querySelectorAll(".admin-chat-room__system--date").forEach(function (divider) {
      divider.remove();
    });

    var messageArticles = adminMessageList.querySelectorAll(".admin-chat-detail-message");
    var previousDateKey = "";

    messageArticles.forEach(function (article, index) {
      var createdAtValue = article.dataset.messageCreatedAt;
      var dateKey = getAdminChatMessageDateKey(createdAtValue);
      article.dataset.messageDateKey = dateKey;

      if (index > 0 && dateKey && previousDateKey && previousDateKey !== dateKey) {
        adminMessageList.insertBefore(createAdminChatDateDivider(createdAtValue), article);
      }

      previousDateKey = dateKey || previousDateKey;
    });
  }

  function scrollAdminChatToBottom(useSmooth) {
        if (!adminMessageScrollBody) {
            return;
        }

        window.setTimeout(function () {
            adminMessageScrollBody.scrollTo({
                top: adminMessageScrollBody.scrollHeight,
                behavior: useSmooth ? "smooth" : "auto"
            });
        }, 90);
    }

  // 견적 카드(제목 + 링크 + QR Code) 만들기
  function buildAdminChatQuoteCard(message) {
    var card = document.createElement("div");
    card.className = "admin-chat-quote-card";

    var title = document.createElement("strong");
    title.className = "admin-chat-quote-card__title";
    title.textContent = message.chMsLnkTtl || "견적 초안";
    card.appendChild(title);

    if (message.chMsCon) {
      var desc = document.createElement("p");
      desc.className = "admin-chat-quote-card__desc";
      desc.textContent = message.chMsCon;
      card.appendChild(desc);
    }

    var link = document.createElement("a");
    link.className = "admin-chat-quote-card__link";
    link.href = message.chMsLnkUrl || "#";
    link.target = "_blank";
    link.rel = "noopener noreferrer";
    link.textContent = "견적 초안 열기";
    card.appendChild(link);

    if (message.chMsLnkUrl) {
        var qr = document.createElement("img");
        qr.className = "admin-chat-quote-card__qr";
        qr.alt = "견적 QR 코드";
        qr.dataset.qrUrl = message.chMsLnkUrl;
        applyQuoteQrImage(qr);
        card.appendChild(qr);
    }

    return card;
  }

  function applyQuoteQrImage(img) {
    if (!img) {
      return;
    }

    var qrUrl = img.dataset.qrUrl;
    if (!qrUrl || !qrApiBaseUrl) {
        return;
    }

    img.src = qrApiBaseUrl + encodeURIComponent(qrUrl);
  }

  function hydrateExistingAdminQuoteCards() {
    document.querySelectorAll(".admin-chat-quote-card__qr[data-qr-url]").forEach(function (img) {
      applyQuoteQrImage(img);
    });
  }

  // 이미지 메시지 버블 만들기
  function buildAdminChatImageBubble(message, isAdmin) {
    var bubble = document.createElement("div");
    bubble.className = "admin-chat-detail-message__bubble admin-chat-detail-message__bubble--image";
    if (isAdmin) {
      bubble.classList.add("admin-chat-detail-message__bubble--admin");
    }

    if (message.imageFile && message.imageFile.fileUuid) {
      var link = document.createElement("a");
      link.className = "admin-chat-detail-message__image-link";
      // 원본 이미지 보기와 채팅 미리보기 모두 파일 view API 사용
      link.href = "/api/files/view/" + message.imageFile.fileUuid;
      link.target = "_blank";
      link.rel = "noopener noreferrer";

      var image = document.createElement("img");
      image.className = "admin-chat-detail-message__image";
      image.src = "/api/files/view/" + message.imageFile.fileUuid;
      image.alt = "첨부 이미지";
      image.addEventListener("load", scrollAdminChatToBottom);

      link.appendChild(image);
      bubble.appendChild(link);
    } else {
      var empty = document.createElement("p");
      empty.className = "admin-chat-detail-message__image-empty";
      empty.textContent = "이미지를 불러올 수 없습니다.";
      bubble.appendChild(empty);
    }

    return bubble;
  }

  // 메시지(이미지/견적 카드) 버블 만들기
  function buildAdminChatMessageBubble(message, isAdmin) {
    var type = message.chMsTp || "TEXT";

    if (type === "IMAGE") {
      return buildAdminChatImageBubble(message, isAdmin);
    }

    if (type === "QUOTE_CARD") {
      var quoteBubble = document.createElement("div");
      quoteBubble.className = "admin-chat-detail-message__bubble admin-chat-detail-message__bubble--quote-card";
      if (isAdmin) {
        quoteBubble.classList.add("admin-chat-detail-message__bubble--admin");
      }
      quoteBubble.appendChild(buildAdminChatQuoteCard(message));
      return quoteBubble;
    }

    var textBubble = document.createElement("div");
    textBubble.className = "admin-chat-detail-message__bubble";
    if (isAdmin) {
      textBubble.classList.add("admin-chat-detail-message__bubble--admin");
    }
    textBubble.textContent = message.chMsCon || "";
    return textBubble;
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

  function showAdminActionErrorModal(title, lead, description) {
    if (!adminActionErrorTitle || !adminActionErrorLead || !adminActionErrorDescription) {
      return;
    }

    // 상담 시작/종료 실패 시 공용 에러 모달에 문구를 채워 보여줌
    adminActionErrorTitle.textContent = title || "처리 실패";
    adminActionErrorLead.textContent = lead || "요청 처리 중 오류가 발생했습니다.";
    adminActionErrorDescription.textContent = description || "잠시 후 다시 시도해 주세요.";

    toggleModal("action-error", true);
  }

  // 관리자 메시지 HTML 추가
  function appendAdminMessage(message) {
    // 관리자 본인이 보낸 확정 메시지를 우측 말풍선으로 렌더링
    if (!adminMessageList || !message) {
      return;
    }

    removeEmptyDetailPlaceholder();

    // 실시간 수신 메시지 날짜가 바뀌었으면 먼저 날짜 divider 추가
    appendAdminDateDividerIfNeeded(message.chMsCreDt);

    var article = document.createElement("article");
    article.className = "admin-chat-detail-message admin-chat-detail-message--admin";
    article.classList.add("admin-chat-animate-in");

    var content = document.createElement("div");
    content.className = "admin-chat-detail-message__content";

    var meta = document.createElement("div");
    meta.className = "admin-chat-detail-message__meta admin-chat-detail-message__meta--admin";

    var time = document.createElement("span");
    time.textContent = formatAdminChatMessageTime(message.chMsCreDt);

    var bubble = buildAdminChatMessageBubble(message, true);

    var messageTime = document.createElement("span");
    messageTime.className = "admin-chat-detail-message__time admin-chat-detail-message__time--admin";
    messageTime.textContent = time.textContent;

    article.dataset.messageCreatedAt = message.chMsCreDt || "";
    article.dataset.messageDateKey = getAdminChatMessageDateKey(message.chMsCreDt);
    content.appendChild(meta);
    content.appendChild(bubble);
    content.appendChild(messageTime);
    article.appendChild(content);
    adminMessageList.appendChild(article);
      scrollAdminChatToBottom(true);
      moveClosedPanelToBottom();
  }

  function appendPendingAdminMessage(messageText) {
    // 전송 직후 지연 체감을 줄이기 위해 임시 관리자 메시지를 먼저 붙임
    if (!adminMessageList || !messageText) {
      return null;
    }

    removeEmptyDetailPlaceholder();

    var article = document.createElement("article");
    article.className = "admin-chat-detail-message admin-chat-detail-message--admin";
    article.classList.add("admin-chat-animate-in");
    article.dataset.pending = "true";

    var content = document.createElement("div");
    content.className = "admin-chat-detail-message__content";

    var meta = document.createElement("div");
    meta.className = "admin-chat-detail-message__meta admin-chat-detail-message__meta--admin";

    var bubble = document.createElement("div");
    bubble.className = "admin-chat-detail-message__bubble admin-chat-detail-message__bubble--admin";
    bubble.textContent = messageText;

    var messageTime = document.createElement("span");
    messageTime.className = "admin-chat-detail-message__time admin-chat-detail-message__time--admin";
    messageTime.textContent = "";

    content.appendChild(meta);
    content.appendChild(bubble);
    content.appendChild(messageTime);
    article.appendChild(content);
    adminMessageList.appendChild(article);

      scrollAdminChatToBottom(true);

      return article;
  }

  // 메시지를 받았을 때 메시지 append
  function appendClientMessage(message) {
      // 사용자가 보낸 메시지를 좌측 말풍선으로 그리고 회사/회원 정보를 함께 보여줌
      if (!adminMessageList || !message) return;

      removeEmptyDetailPlaceholder();

      // 실시간 수신 메시지 날짜가 바뀌었으면 먼저 날짜 divider 추가
      appendAdminDateDividerIfNeeded(message.chMsCreDt);

      var article = document.createElement("article");
      article.className = "admin-chat-detail-message admin-chat-detail-message--client";
      article.classList.add("admin-chat-animate-in");

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
      time.textContent = formatAdminChatMessageTime(message.chMsCreDt);

      meta.appendChild(sender);

      var bubble = buildAdminChatMessageBubble(message, false);

      var messageTime = document.createElement("span");
      messageTime.className = "admin-chat-detail-message__time";
      messageTime.textContent = time.textContent;

      content.appendChild(meta);
      content.appendChild(bubble);
      content.appendChild(messageTime);
      article.dataset.messageCreatedAt = message.chMsCreDt || "";
      article.dataset.messageDateKey = getAdminChatMessageDateKey(message.chMsCreDt);
      article.appendChild(avatar);
      article.appendChild(content);
      adminMessageList.appendChild(article);
        scrollAdminChatToBottom(true);
        moveClosedPanelToBottom();
    }

  document.addEventListener("click", function (event) {
    var trigger = event.target.closest("[data-modal-open]");
    if (!trigger) {
      return;
    }

    pendingDetailUrl = trigger.dataset.detailUrl || "";
    pendingRoomId = trigger.dataset.roomId || "";
    toggleModal(trigger.dataset.modalOpen, true);
  });

  modalClosers.forEach(function (closer) {
    closer.addEventListener("click", function () {
      document.querySelectorAll(".admin-chat-modal").forEach(function (modal) {
        modal.classList.add("is-hidden");
        modal.setAttribute("aria-hidden", "true");
      });
    });
  });

  // 관리자 상담(배정) 시작 비동기 요청
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
      // 확인 모달은 닫고, 실패 전용 에러 모달 출력
      toggleModal("assign", false);
      showAdminActionErrorModal(
        "상담 시작 실패",
        "상담 시작 처리 중 오류가 발생했습니다.",
        "잠시 후 다시 시도해 주세요."
      );
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
      // 확인 모달은 닫고, 실패 전용 에러 모달 출력
      toggleModal("close", false);
      showAdminActionErrorModal(
        "상담 종료 실패",
        "상담 종료 처리 중 오류가 발생했습니다.",
        "잠시 후 다시 시도해 주세요."
      );
      console.error(error);
    });
  }

  // 관리자가 입력한 메시지를 전송 비동기 요청(서버에 저장 요청)
  function sendAdminChatMessage() {
    // 입력 메시지는 REST로 저장 요청하고, 실제 화면 확정은 WebSocket 수신 결과로 처리
    if (!detailRoomId || !adminMessageInput || !detailCanWrite || isSendingAdminMessage) {
      return;
    }

    var content = adminMessageInput.value.trim();
    if (!content) {
      adminMessageInput.focus();
      return;
    }

    // 서버 응답을 기다리지 않고 입력창을 먼저 비워 연속 입력 시 체감 지연을 줄임
    adminMessageInput.value = "";
    adminMessageInput.focus();
    isSendingAdminMessage = true;

    var pendingMessageElement = appendPendingAdminMessage(content);

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
        throw new Error("메시지 전송에 실패했습니다.");
      }

      return response.json();
    }).catch(function (error) {
      if (pendingMessageElement) {
        pendingMessageElement.remove();
      }

      // 전송 실패 시 사용자가 다시 보낼 수 있게 입력값을 복구
      if (adminMessageInput && !adminMessageInput.value.trim()) {
        adminMessageInput.value = content;
        adminMessageInput.focus();
      }

      showAdminChatToast("메시지 전송에 실패했습니다. 다시 시도해 주세요.");
      console.error(error);
    }).finally(function () {
      isSendingAdminMessage = false;
    });
  }

  // 관리자 이미지 메시지 전송 비동기 요청
  function sendAdminChatImage(file) {
    if (!detailRoomId || !detailCanWrite || !file) {
      return;
    }

    var formData = new FormData();
    formData.append("imageFile", file);

    fetch("/api/admin/chat/rooms/" + detailRoomId + "/image", {
      method: "POST",
      headers: getCsrfHeaders(),
      body: formData
    }).then(function (response) {
      if (!response.ok) {
        throw new Error("이미지 전송 요청에 실패했습니다.");
      }

      return response.json();
    }).then(function () {
      if (adminImageInput) {
        adminImageInput.value = "";
      }
    }).catch(function (error) {
      showAdminChatToast("이미지 전송에 실패했습니다. 다시 시도해 주세요.");
      console.error(error);
      if (adminImageInput) {
        adminImageInput.value = "";
      }
    });
  }

  // 관리자 채팅 목록 페이지에서 관리자 summary 채널 구독, 새 메시지 오면 목록 부분만 갱신
  function subscribeAdminSummary() {
    if (!stompClient || !wsConnected || !listPage) {
      return;
    }

    if (summarySubscription) {
      summarySubscription.unsubscribe();
    }

    summarySubscription = stompClient.subscribe("/sub/chat/admin/summary", function () {
        if (shouldPauseAdminChatSummaryRefresh()) {
          return;
        }

        refreshAdminChatList(window.location.href, false);
    });
  }

  // 관리자가 특정 채팅방을 실시간으로 구독(관리자 실시간 수신용 + 화면 반영)
  function connectAdminChatSocket() {
      // 상세 또는 목록 페이지에서만 소켓 연결 허용
      if (!window.StompJs || (!listPage && !detailPage)) {
        return;
      }

      // WebSocket/STOMP 클라이언트 생성
      stompClient = new StompJs.Client({
        brokerURL: wsProtocol + window.location.host + "/ws",
        reconnectDelay: 5000,
        debug: function () {},

        onConnect: function () {
          wsConnected = true;
          subscribeAdminSummary();

          // 목록 페이지에서는 summary 채널만 구독
          if (!detailPage || !detailRoomId) {
            return;
          }

          if (roomSubscription) {
            roomSubscription.unsubscribe();
          }

          // 연결 성공 시 해당 채팅방 구독
          roomSubscription = stompClient.subscribe("/sub/chat/rooms/" + detailRoomId, function (frame) {
            var message = JSON.parse(frame.body);

            if (message.eventType === "ROOM_STATUS") {
                if (detailPage) {
                  detailPage.dataset.chatStatus = message.chRoStt;
                }
                setChatState(message.chRoStt);
                return;
            }

            // 관리자 메시지면
            if (message.chMsSenTy === "ADMIN") {
              var pendingMessage = adminMessageList ? adminMessageList.querySelector('[data-pending="true"]') : null;

              if (pendingMessage) {
                pendingMessage.removeAttribute("data-pending");
                pendingMessage.classList.remove("admin-chat-animate-in");

                var pendingTime = pendingMessage.querySelector(".admin-chat-detail-message__time");
                if (pendingTime) {
                  pendingTime.textContent = new Date(message.chMsCreDt).toLocaleTimeString("ko-KR", {
                    hour: "2-digit",
                    minute: "2-digit",
                    hour12: false
                  });
                }
                return;
              }

              appendAdminMessage(message);
              return;
            }

            // 사용자 메시지면
            appendClientMessage(message);
          });
        },

        onWebSocketClose: function () {
          wsConnected = false;
        },

        onStompError: function (frame) {
          console.error(frame);
        }
      });

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

  if (adminImageTrigger && adminImageInput) {
    adminImageTrigger.addEventListener("click", function () {
      if (!detailCanWrite) {
        return;
      }
      adminImageInput.click();
    });

    adminImageInput.addEventListener("change", function () {
      var file = adminImageInput.files && adminImageInput.files[0];
      if (!file) {
        return;
      }
      sendAdminChatImage(file);
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
    renderExistingAdminDateDividers();
    hydrateExistingAdminQuoteCards();
    setChatState(detailPage.dataset.chatStatus || "ONGOING");
    scrollAdminChatToBottom(false);
    if (adminMessageInput && detailPage.dataset.chatStatus === "ONGOING" && detailCanWrite) {
      adminMessageInput.focus();
    }
  }

  // 목록, 상세 어디서든 소켓 연결 시작
  if (listPage || detailPage) {
     connectAdminChatSocket();
  }

   // 상태, 담당 필터, 페이지네이션 클릭을 가로채서 비동기 목록 갱신
   document.addEventListener("click", function (event) {
        if (!listPage) {
          return;
        }

        var filterLink = event.target.closest(
          '.admin-chat-shell--toolbar a.admin-chat-filter, .admin-chat-shell--pagination a.admin-chat-page-button'
        );

        if (!filterLink) {
          return;
        }

        event.preventDefault();

        refreshAdminChatList(filterLink.href, true);
   });

   // 검색 폼 전체 새로고침 없이 목록만 갱신
   document.addEventListener("submit", function (event) {
         if (!listPage) {
           return;
         }

         var searchForm = event.target.closest("[data-admin-chat-search-form]");
         if (!searchForm) {
           return;
         }

         event.preventDefault();

         var formData = new FormData(searchForm);
         var params = new URLSearchParams(formData);
         var requestUrl = searchForm.action + "?" + params.toString();

         refreshAdminChatList(requestUrl, true);
   });

   window.addEventListener("popstate", function () {
        if (!listPage) {
          return;
        }

        refreshAdminChatList(window.location.href, false);
   });

});
