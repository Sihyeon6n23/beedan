document.addEventListener('DOMContentLoaded', function() {
    const badge = document.getElementById('unreadBadge');
    const notiIcon = document.getElementById('notiIcon');
    const notiDropdown = document.getElementById('notiDropdown');
    const notiContent = document.querySelector(".notification-info-box");

    if (!badge || !notiIcon || !notiDropdown || !notiContent) return;

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    async function apiRequest(url, method = 'GET') {
        const headers = { 'Content-Type': 'application/json' };
        if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

        try {
            const response = await fetch(url, { method, headers });
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error("API 요청 실패:", error);
            throw error;
        }
    }

    window.updateBadgeUI = function(count) { // 미확인 알림 개수 표시
        const numCount = parseInt(count, 10);
        if (numCount > 0) {
            badge.innerText = numCount > 6 ? '6+' : numCount;
            badge.classList.remove('hidden');
        } else {
            badge.innerText = '';
            badge.classList.add('hidden');
        }
    };

    function updateUnreadCount() { // 알림 JS 초기화 시점과 실시간 이벤트 수신 시점에 호출하여 배지 업데이트
        apiRequest('/api/notification/unread-count')
            .then(count => {
                updateBadgeUI(count);
            })
            .catch(err => console.error("배지 업데이트 실패:", err));
    }

    window.addEventListener('newNotification', function(e) { // 웹소켓 연결시 이벤트 실행 (알림 수신 시 서버에서 클라이언트로 count를 보내줄 예정)
        const newCount = e.detail.count;
        console.log("실시간 알림 수신됨 (Event):", newCount);
        updateBadgeUI(newCount);
    });

    // ====== 알림 목록 렌더링 및 기타  ======
    function loadNotifications() {
        apiRequest('/api/notification')
            .then(data => renderNotifications(data))
            .catch(() => {
                notiContent.innerHTML = '<div class="py-24 text-center text-xs text-[var(--pub-text-muted)]">알림을 불러올 수 없습니다.</div>';
            });
    }

    function renderNotifications(data) {
        if (!data || data.length === 0) {
            notiContent.innerHTML = '<div class="py-24 text-center text-xs text-[var(--pub-text-muted)]">새로운 알림이 없습니다.</div>';
            return;
        }

        let html = '<ul class="data-list-container">';
        html += data.map(noti => `
            <li class="data-list-item ${noti.notiReaYn === false ? 'unread' : ''}" onclick="handleAction('read', ${noti.notiId}, this)">
                <div style="flex: 1;">
                    <div style="display: flex; align-items: center; gap: 5px;">
                        <strong class="text-sm text-[var(--pub-text-main)]">${noti.notiTtl}</strong>
                        ${noti.notiReaYn === false ? '<span class="status-badge" style="background:var(--pub-primary); color:var(--pub-text-main);">NEW</span>' : ''}
                    </div>
                    <p class="text-xs text-[var(--pub-text-sub)] mt-1">${noti.notiCon}</p>
                    <small class="text-[10px] text-[var(--pub-text-muted)]">${noti.notiCreDt}</small>
                </div>
                <button type="button" class="close-btn" onclick="event.stopPropagation(); handleAction('delete', ${noti.notiId}, this)">
                    <span class="material-symbols-outlined" style="font-size: 16px;">close</span>
                </button>
            </li>
        `).join('');
        html += '</ul>';

        notiContent.innerHTML = html;
    }

    window.handleAction = function(type, id = null, clickedElement = null) {
        let url = '';
        let method = 'PATCH';

        switch(type) {
            case 'read': url = `/api/notification/${id}`; break;
            case 'readAll': url = `/api/notification`; method = 'PATCH'; break;
            case 'delete': url = `/api/notification/${id}`; method = 'DELETE'; break;
            case 'deleteAll': url = `/api/notification`; method = 'DELETE'; break;
        }

        if (url) {
            apiRequest(url, method)
                .then(updatedList => {
                    renderNotifications(updatedList);
                })
                .catch(err => alert("요청 처리에 실패했습니다."));
        }
    };

    function checkEmptyState() {
        const container = document.querySelector('.data-list-container');
        if (container && container.children.length === 0) {
            notiContent.innerHTML = `
                <div class="py-40 text-center">
                    <span class="material-symbols-outlined text-3xl text-[var(--pub-text-muted)]">notifications_none</span>
                    <p class="text-[var(--pub-text-muted)] font-medium">수신된 알림이 없습니다.</p>
                </div>`;
        }
    }

    // --- 이벤트 리스너 등록 ---
    notiIcon.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        const isHidden = notiDropdown.classList.toggle('hidden');
        if (!isHidden) loadNotifications();
    });

    notiDropdown.addEventListener('click', (e) => e.stopPropagation());

    document.addEventListener('click', () => {
        if (!notiDropdown.classList.contains('hidden')) notiDropdown.classList.add('hidden');
    });

    updateUnreadCount();
});