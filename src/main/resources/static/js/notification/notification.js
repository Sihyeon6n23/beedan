document.addEventListener('DOMContentLoaded', function() {
    const notiIcon = document.getElementById('notiIcon');
    const notiDropdown = document.getElementById('notiDropdown');
    const notiContent = document.querySelector(".notification-info-box");

    if (!notiIcon || !notiDropdown || !notiContent) return;

    // CSRF 토큰 추출
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    // 공통 API 요청 함수
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

    // 1. 알림 목록 로드
    function loadNotifications() {
        apiRequest('/api/notification')
            .then(data => renderNotifications(data))
            .catch(() => {
                notiContent.innerHTML = '<div class="py-24 text-center text-xs text-[var(--pub-text-muted)]">알림을 불러올 수 없습니다.</div>';
            });
    }

    // 2. 화면 렌더링 (데이터 목록 디자인 적용)
    function renderNotifications(data) {
        if (!notiContent) return;

        if (!data || data.length === 0) {
            notiContent.innerHTML = '<div class="py-24 text-center text-xs text-[var(--pub-text-muted)]">새로운 알림이 없습니다.</div>';
            return;
        }

        let html = '<ul class="data-list-container">';
        html += data.map(noti => `
            <li class="data-list-item ${noti.readYn === 'N' ? 'unread' : ''}" onclick="handleAction('read', ${noti.notiId})">
                <div style="flex: 1;">
                    <div style="display: flex; align-items: center; gap: 5px;">
                        <strong class="text-sm text-[var(--pub-text-main)]">${noti.notiTtl}</strong>
                        ${noti.readYn === 'N' ? '<span class="status-badge" style="background:var(--pub-primary); color:var(--pub-text-main);">NEW</span>' : ''}
                    </div>
                    <p class="text-xs text-[var(--pub-text-sub)] mt-1">${noti.notiCon}</p>
                    <small class="text-[10px] text-[var(--pub-text-muted)]">${formatDate(noti.notiCreAt)}</small>
                </div>
                <button type="button" class="close-btn" onclick="event.stopPropagation(); handleAction('delete', ${noti.notiId})">
                    <span class="material-symbols-outlined" style="font-size: 16px;">close</span>
                </button>
            </li>
        `).join('');
        html += '</ul>';

        notiContent.innerHTML = html;
    }

    // 3. 통합 액션 처리 (전역 window 객체에 할당하여 HTML에서 호출 가능케 함)
    window.handleAction = function(type, id = null) {
        let url = '';
        let method = 'PATCH';

        switch(type) {
            case 'read': url = `/api/notification/${id}`; break;
            case 'readAll': url = `/api/notification`; break;
            case 'delete':
                url = `/api/notification/${id}`;
                method = 'DELETE';
                break;
            case 'deleteAll':
                url = `/api/notification`;
                method = 'DELETE';
                break;
        }

        if (url) {
            apiRequest(url, method)
                .then(updatedList => {
                    renderNotifications(updatedList);
                    updateUnreadCount();
                })
                .catch(err => console.error("요청 실패:", err));
        }
    };

    // 날짜 포맷 함수
    function formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    }

    // [중요 수정] 알림 아이콘 클릭 (토글 및 전파 방지)
    notiIcon.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation(); // 클릭 이벤트가 document로 퍼져서 창이 바로 닫히는 것을 방지

        const isHidden = notiDropdown.classList.toggle('hidden');
        if (!isHidden) {
            loadNotifications();
        }
    });

    // [중요 수정] 알림창 내부 클릭 시 창이 닫히지 않도록 방지
    notiDropdown.addEventListener('click', function(e) {
        e.stopPropagation();
    });

    // 이벤트 리스너: 외부 클릭 시 닫기
    document.addEventListener('click', (e) => {
        if (!notiDropdown.classList.contains('hidden')) {
            notiDropdown.classList.add('hidden');
        }
    });

   function updateUnreadCount() {
           apiRequest('/api/notification/unread-count')
               .then(count => {
                   const badge = document.querySelector('#notiIcon span.bg-primary');
                   if (badge) {
                       if (count > 0) {
                           badge.removeAttribute('style');

                           badge.style.setProperty('display', 'flex', 'important');
                           badge.innerText = count > 99 ? '99+' : count;
                       } else {
                           badge.style.setProperty('display', 'none', 'important');
                       }
                   }
               })
               .catch(err => console.error("배지 업데이트 실패:", err));
       }

    updateUnreadCount();
});