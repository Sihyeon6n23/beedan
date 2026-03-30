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
            <li class="data-list-item ${noti.notiReaYn === false ? 'unread' : ''}" onclick="handleAction('read', ${noti.notiId})">
                <div style="flex: 1;">
                    <div style="display: flex; align-items: center; gap: 5px;">
                        <strong class="text-sm text-[var(--pub-text-main)]">${noti.notiTtl}</strong>
                        ${noti.notiReaYn === false ? '<span class="status-badge" style="background:var(--pub-primary); color:var(--pub-text-main);">NEW</span>' : ''}
                    </div>
                    <p class="text-xs text-[var(--pub-text-sub)] mt-1">${noti.notiCon}</p>
                    <small class="text-[10px] text-[var(--pub-text-muted)]">${noti.notiCreDt}</small>
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
    // 세 번째 파라미터 clickedElement 추가
    window.handleAction = function(type, id = null, clickedElement = null) {
        let url = '';
        let method = 'PATCH'; // 기본은 PATCH (읽음 처리용)

        switch(type) {
            case 'read':
                url = `/api/notification/${id}`;
                break;
            case 'readAll':
                url = `/api/notification`;
                method = 'PATCH';
                break;
            case 'delete':
                url = `/api/notification/${id}`;
                method = 'DELETE';
                break;
            case 'deleteAll':
                url = `/api/notification`;
                method = 'DELETE'; // 전체 삭제 API 호출
                break;
        }

        if (url) {
            apiRequest(url, method)
                .then(updatedList => {
                    // 상단 드롭다운 갱신
                    renderNotifications(updatedList);
                    updateUnreadCount();

                    // [실시간 반영 로직]
                    if (type === 'read' && clickedElement) {
                        const li = clickedElement.closest('li');
                        li.style.borderLeftColor = 'transparent'; // 좌측 바 투명화
                        const badge = li.querySelector('.noti-badge');
                        if (badge) badge.remove();
                        const title = li.querySelector('.noti-title');
                        if (title) { title.style.fontWeight = '500'; title.style.opacity = '0.6'; }
                    }
                    else if (type === 'delete' && clickedElement) {
                        clickedElement.closest('li').remove();
                        checkEmptyState();
                    }
                    else if (type === 'deleteAll') {
                        // 전체 삭제 성공 시 목록 비우기
                        const container = document.querySelector('.data-list-container');
                        if (container) container.innerHTML = '';
                        checkEmptyState();
                    }
                    else if (type === 'readAll') {
                        // 전체 읽음 시 모든 리스트의 바와 뱃지 제거
                        document.querySelectorAll('.data-list-item').forEach(li => {
                            li.style.borderLeftColor = 'transparent';
                            const b = li.querySelector('.noti-badge');
                            if (b) b.remove();
                        });
                    }
                })
                .catch(err => alert("요청 처리에 실패했습니다."));
        }
    };

    // 빈 화면 처리 함수
    function checkEmptyState() {
        const container = document.querySelector('.data-list-container');
        const pageContent = document.getElementById('notiPageContent');
        if (container && container.children.length === 0) {
            pageContent.innerHTML = `
                <div class="py-40 text-center">
                    <div class="w-16 h-16 bg-[var(--pub-bg-main)] rounded-full flex items-center justify-center mx-auto mb-4">
                        <span class="material-symbols-outlined text-3xl text-[var(--pub-text-muted)]">notifications_none</span>
                    </div>
                    <p class="text-[var(--pub-text-muted)] font-medium">수신된 알림이 없습니다.</p>
                </div>`;
        }
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
                           badge.style.setProperty('display', 'flex', 'important');
                           badge.style.setProperty('border-radius', '999px', 'important');
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