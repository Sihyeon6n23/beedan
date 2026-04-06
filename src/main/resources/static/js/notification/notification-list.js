document.addEventListener('DOMContentLoaded', function() {
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    // 현재 선택된 필터 상태
    let currentFilter = 'ALL';

    const getFetchOptions = (method, body = null) => {
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        };
        if (body) options.body = JSON.stringify(body);
        return options;
    };

    // 필터 버튼 클릭 이벤트
    document.addEventListener('click', function(e) {
        const filterBtn = e.target.closest('.admin-chat-filter');
        if (filterBtn) {
            e.preventDefault();
            const filterStatus = filterBtn.getAttribute('data-status');

            // 필터 상태 업데이트
            currentFilter = filterStatus;

            // UI 업데이트 (활성 상태 표시)
            updateFilterUI(filterStatus);

            // 목록 다시 로드
            loadNotificationList();
        }
    });

    // 필터 UI 업데이트
    function updateFilterUI(activeFilter) {
        // 모든 필터 버튼에서 is-active 클래스 제거
        document.querySelectorAll('.admin-chat-filter').forEach(btn => {
            btn.classList.remove('is-active');
        });

        // 선택된 필터 버튼에 is-active 클래스 추가
        const activeBtn = document.querySelector(`.admin-chat-filter[data-status="${activeFilter}"]`);
        if (activeBtn) {
            activeBtn.classList.add('is-active');
        }
    }

    // 알림 목록 로드
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

    // 알림 목록 렌더링
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
                <li id="noti-item-${noti.notiId}"
                    class="noti-row data-list-item flex items-center p-6 border-b border-[var(--pub-border)] last:border-0 hover:bg-[var(--pub-bg-main)] transition-all cursor-pointer group"
                    style="${borderStyle}"
                    onclick="handleAction('read', ${noti.notiId}, this)">

                    <div class="flex-1 min-w-0">
                        <div class="flex items-center gap-2 mb-1">
                            <strong class="noti-title text-base text-[var(--pub-text-main)] truncate" style="${titleStyle}">[[${noti.notiTtl}]]</strong>
                            ${isUnread ? '<span id="noti-badge-' + noti.notiId + '" class="noti-badge status-badge !m-0" style="background:var(--pub-primary); color:var(--pub-text-main);">NEW</span>' : ''}
                        </div>

                        <p class="text-sm text-[var(--pub-text-sub)] line-clamp-1 mb-2">${noti.notiCon}</p>

                        <span class="text-[11px] text-[var(--pub-text-muted)] font-medium">${formatDate(noti.notiCreDt)}</span>
                    </div>

                    <button type="button" class="close-btn ml-6 p-2 opacity-30 group-hover:opacity-100 hover:text-red-500 transition-all"
                            onclick="event.stopPropagation(); handleAction('delete', ${noti.notiId}, this)">
                        <span class="material-symbols-outlined text-[20px]">close</span>
                    </button>
                </li>`;
        });

        html += '</ul>';
        listBody.innerHTML = html;
    }

    // 날짜 포맷팅 함수
    function formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    // 액션 처리 함수 (전역으로 노출)
    window.handleAction = function(type, id = null, clickedElement = null) {
        let url = '';
        let method = 'PATCH';

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
                method = 'DELETE';
                break;
        }

        if (url) {
            fetch(url, getFetchOptions(method))
            .then(res => {
                if (!res.ok) throw new Error("요청에 실패했습니다.");
                return res.json();
            })
            .then(updatedList => {
                // 필터 상태를 유지하면서 목록 다시 로드
                loadNotificationList();

                if (type === 'readAll') {
                    // 전체 읽음 시 성공 메시지
                    showToast('모든 알림을 읽음 처리했습니다.');
                } else if (type === 'deleteAll') {
                    // 전체 삭제 시 성공 메시지
                    showToast('모든 알림을 삭제했습니다.');
                }
            })
            .catch(err => {
                console.error("요청 처리 실패:", err);
                alert("요청 처리에 실패했습니다.");
            });
        }
    };

    // 토스트 메시지 표시 함수
    function showToast(message) {
        // 간단한 토스트 구현 (필요시 개선)
        const toast = document.createElement('div');
        toast.textContent = message;
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: var(--pub-primary);
            color: white;
            padding: 12px 20px;
            border-radius: 4px;
            z-index: 1000;
            font-size: 14px;
        `;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.remove();
        }, 3000);
    }

    // 초기 로드
    loadNotificationList();
});
