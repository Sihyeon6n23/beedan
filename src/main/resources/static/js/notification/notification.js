document.addEventListener('DOMContentLoaded', function() {
    const notiIcon = document.getElementById('notiIcon');
    const notiDropdown = document.getElementById('notificationDropdown');
    const notiContent = document.querySelector(".notification-info-box");

    // CSRF 토큰 (Thymeleaf 사용 시 HTML head의 meta 태그에서 가져옴)
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    // 1. 알림 목록 불러오기 함수 (분리)
    function loadNotifications() {
        fetch('/api/notification/list')
            .then(response => {
                if (!response.ok) throw new Error("서버 응답 에러");
                return response.json();
            })
            .then(data => {
                renderNotifications(data);
            })
            .catch(error => {
                console.error("알림 로드 실패:", error);
                notiContent.innerHTML = '<p style="text-align:center; padding: 20px;">알림을 불러올 수 없습니다.</p>';
            });
    }

    // 2. 화면에 알림 그리기 함수
    function renderNotifications(data) {
        if (!data || data.length === 0) {
            notiContent.innerHTML = '<p style="text-align:center; padding: 20px; color: #888;">새로운 알림이 없습니다.</p>';
            return;
        }

        // 상단: 모두 읽음 / 모두 삭제 버튼
        const headerHtml = `
            <div style="display: flex; justify-content: flex-end; padding: 10px; border-bottom: 1px solid #ddd; background: #f9f9f9;">
                <button class="noti-action-btn read-all-btn" style="margin-right: 10px; font-size: 12px; cursor: pointer;">모두 읽음</button>
                <button class="noti-action-btn delete-all-btn" style="font-size: 12px; cursor: pointer; color: red;">모두 삭제</button>
            </div>
        `;

        const listHtml = data.map(noti => `
            <div class="noti-item" style="border-bottom: 1px solid #eee; padding: 15px; display: flex; justify-content: space-between; align-items: flex-start;">
                <div style="flex-grow: 1;">
                    <strong style="display: block; margin-bottom: 5px;">${noti.notiTtl}</strong>
                    <p style="margin: 0 0 5px 0; font-size: 13px; color: #555;">${noti.notiCon}</p>
                    <small style="color: #999;">${formatDate(noti.notiCreAt)}</small>
                </div>
                <div style="display: flex; flex-direction: column; gap: 8px; margin-left: 10px;">
                    <button class="noti-action-btn read-btn" data-id="${noti.notiId}" style="font-size: 11px; cursor: pointer;">읽음</button>
                    <button class="noti-action-btn delete-btn" data-id="${noti.notiId}" style="font-size: 11px; cursor: pointer; color: red;">삭제</button>
                </div>
            </div>
        `).join('');

        notiContent.innerHTML = headerHtml + listHtml;
    }

    function formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    }

    notiIcon.addEventListener('click', function(e) {
        e.stopPropagation();
        notiDropdown.classList.toggle('hidden');

        if (!notiDropdown.classList.contains('hidden')) {
            notiContent.innerHTML = '<p style="text-align:center; padding: 20px;">로딩 중...</p>';
            loadNotifications();
        }
    });

    document.addEventListener('click', function(e) {
        if (!notiDropdown.contains(e.target) && e.target !== notiIcon) {
            notiDropdown.classList.add('hidden');
        }
    });

    notiContent.addEventListener('click', function(e) {
        const target = e.target;
        if (!target.classList.contains('noti-action-btn')) return;

        const headers = { 'Content-Type': 'application/json' };
        if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

        let url = '';

        if (target.classList.contains('read-btn')) {
            url = `/api/notification/${target.dataset.id}/read`;
        } else if (target.classList.contains('delete-btn')) {
            url = `/api/notification/${target.dataset.id}/delete`;
        } else if (target.classList.contains('read-all-btn')) {
            url = `/api/notification/read-all`;
        } else if (target.classList.contains('delete-all-btn')) {
            url = `/api/notification/delete-all`;
        }

        if (url) {
            fetch(url, { method: 'PATCH', headers: headers })
                .then(response => {
                    if (!response.ok) throw new Error("처리 실패");
                    return response.json();
                })
                .then(updatedList => {
                    renderNotifications(updatedList); // 화면 즉시 새로고침

                    // 알림 카운트는 나중에 구현
                    // document.getElementById('notiBadge').innerText = updatedList.length;
                })
                .catch(error => console.error("오류 발생:", error));
        }
    });
});