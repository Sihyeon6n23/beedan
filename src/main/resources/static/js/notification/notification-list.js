document.addEventListener('DOMContentLoaded', function() {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    let currentFilter = 'ALL';

    const getFetchOptions = (method, body = null) => {
        const options = {
            method: method,
            cache: 'no-store',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        };
        if (body) options.body = JSON.stringify(body);
        return options;
    };

    // 공통 유틸 함수들
    function applyReadDesign(element) {
        if (!element) return;
        element.style.cssText += "border-left-color: transparent !important;";
        const title = element.querySelector('.noti-title');
        if (title) title.style.cssText = "font-weight: 500 !important; opacity: 0.6 !important;";
        const badge = element.querySelector('.noti-badge');
        if (badge) badge.remove();
        element.classList.remove('unread');
    }

    function showToast(message) {
        const toast = document.createElement('div');
        toast.textContent = message;
        toast.className = "fixed top-5 right-5 bg-primary text-white px-5 py-3 rounded z-[1000] text-sm animate-bounce";
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
    }

    function formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleString('ko-KR', {
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit'
        });
    }

    // 전역 노출 함수
    window.handlePageAction = function(type, id = null, clickedElement = null) {
        let url = '';
        let method = 'PATCH';

        switch(type) {
            case 'read': url = `/api/notification/${id}`; break;
            case 'readAll': url = `/api/notification`; method = 'PATCH'; break;
            case 'delete': url = `/api/notification/${id}`; method = 'DELETE'; break;
            case 'deleteAll': url = `/api/notification`; method = 'DELETE'; break;
        }

        if (url) {
            // [즉시 UI 반영 (Optimistic Update)]
            if (type === 'read' && clickedElement) {
                applyReadDesign(clickedElement);
            } else if (type === 'readAll') {
                document.querySelectorAll('.noti-row').forEach(item => applyReadDesign(item));
            } else if (type === 'delete' && clickedElement) {
                clickedElement.closest('li').style.display = 'none';
            } else if (type === 'deleteAll') {
                const container = document.querySelector('.data-list-container');
                if (container) container.innerHTML = '';
            }

            fetch(url, getFetchOptions(method))
              .then(res => res.json())
              .then(updatedList => {
                  if (window.updateUnreadCount) window.updateUnreadCount();

                  if (type === 'delete' || type === 'deleteAll' || type === 'readAll') {
                      loadNotificationList();

                      if (type === 'readAll') showToast('모든 알림을 읽음 처리했습니다.');
                      if (type === 'deleteAll') showToast('모든 알림을 삭제했습니다.');
                  }
            }).catch(err => {
                console.error("요청 처리 실패:", err);
                loadNotificationList();
            });
        }
    };

    window.handleMove = function(refUrl, notiId, element) {
        if (!refUrl || refUrl === 'null') return;

        const row = document.getElementById(`noti-item-${notiId}`);
        const isUnread = row && (row.style.borderLeftColor !== 'transparent' && row.style.borderLeftColor !== '');

        if (isUnread) {
            handlePageAction('read', notiId, row);
        }

        location.href = refUrl;
    };

    // 렌더링 함수
    function loadNotificationList() {
        const url = `/api/notification?filter=${currentFilter}`;

        fetch(url, getFetchOptions('GET'))
        .then(res => {
            if (!res.ok) throw new Error("데이터를 가져오지 못했습니다.");
            return res.json();
        })
        .then(data => {
            renderNotificationList(data);
        })
        .catch(err => {
            console.error("알림 목록 조회 실패: " + err);
            const listBody = document.getElementById('notiPageContent');
            if (listBody) {
                listBody.innerHTML = '<div class="py-40 text-center text-[var(--pub-text-muted)]">알림을 불러올 수 없습니다.</div>';
            }
        });
    }

    function renderNotificationList(notifications) {
        const listBody = document.getElementById('notiPageContent');
        if (!listBody) return;

        if (!notifications || notifications.length === 0) {
            listBody.innerHTML = `
                <div class="py-40 text-center">
                    <div class="w-16 h-16 bg-[var(--pub-bg-main)] rounded-full flex items-center justify-center mx-auto mb-4">
                        <span class="material-symbols-outlined text-3xl text-[var(--pub-text-muted)]">notifications_none</span>
                    </div>
                    <p class="text-[var(--pub-text-muted)] font-medium">수신된 알림이 없습니다.</p>
                </div>`;
            return;
        }

        let html = '<ul class="data-list-container !border-0">';

        notifications.forEach(noti => {
            const isUnread = noti.notiReaYn === false;
            const borderStyle = isUnread ? 'border-left: 4px solid var(--pub-primary);' : 'border-left: 4px solid transparent;';
            const titleStyle = isUnread ? 'font-weight:700' : 'font-weight:500; opacity:0.6;';

            html += `
                <li id="noti-item-${noti.notiId}" class="noti-row data-list-item flex items-center p-6 border-b border-[var(--pub-border)] last:border-0 hover:bg-[var(--pub-bg-main)] transition-all cursor-pointer group"
                    style="${borderStyle}" onclick="handlePageAction('read', ${noti.notiId}, this)">

                            <div class="flex-1 min-w-0">
                                <div class="flex items-center gap-2 mb-1">
                                    <strong class="noti-title text-base text-[var(--pub-text-main)] truncate" style="${titleStyle}">${noti.notiTtl}</strong>
                                    ${isUnread ? '<span id="noti-badge-' + noti.notiId + '" class="noti-badge status-badge !m-0" style="background:var(--pub-primary); color:var(--pub-text-main);">NEW</span>' : ''}
                                </div>

                                <p class="text-sm text-[var(--pub-text-sub)] line-clamp-1 mb-2">${noti.notiCon}</p>

                                <div class="flex items-center gap-4">
                                    <span class="text-[11px] text-[var(--pub-text-muted)] font-medium">${formatDate(noti.notiCreDt)}</span>
                                        ${noti.notiRef ?
                                            `<button type="button" class="flex items-center gap-1 text-[var(--pub-primary)] font-bold text-xs hover:underline cursor-pointer"
                                                    onclick="event.stopPropagation(); handleMove('${noti.notiRef}', ${noti.notiId}, this)">
                                                <span>상세 페이지 이동</span>
                                                <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
                                            </button>
                                        ` : ''}
                                </div>
                            </div>

                            <button type="button" class="close-btn ml-6 p-2 opacity-30 group-hover:opacity-100 hover:text-red-500 transition-all" onclick="event.stopPropagation(); handlePageAction('delete', ${noti.notiId}, this)">
                                <span class="material-symbols-outlined text-[20px]">close</span>
                            </button>
                </li>`;
        });

        html += '</ul>';
        listBody.innerHTML = html;
    }

    function updateFilterUI(activeFilter) {
        document.querySelectorAll('.admin-chat-filter').forEach(btn => {
            btn.classList.remove('is-active');
        });
        const activeBtn = document.querySelector(`.admin-chat-filter[data-status="${activeFilter}"]`);
        if (activeBtn) activeBtn.classList.add('is-active');
    }

    // 이벤트 리스너 등록
    document.addEventListener('click', function(e) {
        const filterBtn = e.target.closest('.admin-chat-filter');
        if (filterBtn) {
            e.preventDefault();
            currentFilter = filterBtn.getAttribute('data-status');
            updateFilterUI(currentFilter);
            loadNotificationList();
        }
    });

    window.addEventListener('notificationUpdated', function(e) {
        loadNotificationList();
    });

    // 초기 로드
    loadNotificationList();
});