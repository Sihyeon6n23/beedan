  document.addEventListener('DOMContentLoaded', function() {
    const notiIcon = document.getElementById('notiIcon');
    const notiDropdown = document.getElementById('notificationDropdown');

    notiIcon.addEventListener('click', function(e) {
        e.stopPropagation();
        notiDropdown.classList.toggle('hidden');

        if (!notiDropdown.classList.contains('hidden')) {
            const notiContent = document.querySelector(".notification-info-box");

            fetch('/api/notification')
                .then(response => {
                    if (!response.ok) throw new Error("서버 응답 에러");
                    return response.json();
                })
                .then(data => {
                    notiContent.innerHTML = '';

                    if (data.length === 0) {
                        notiContent.innerHTML = '<p>새로운 알림이 없습니다.</p>';
                        return;
                    }

                    const htmlTemplate = data.map(noti => `
                        <div class="noti-item" style="border-bottom: 1px solid #eee; padding: 10px;">
                            <strong>${noti.notiTtl}</strong>
                            <p style="margin: 5px 0;">${noti.notiCon}</p>
                            <small style="color: #888;">${noti.notiCreAt}</small>
                        </div>
                    `).join('');

                    notiContent.innerHTML = htmlTemplate;
                })
                .catch(error => {
                    console.error("알림 로드 실패:", error);
                    notiContent.innerHTML = '<p>알림을 불러올 수 없습니다.</p>';
                });
        }
    });

    // 화면 다른 곳 클릭 시 닫기
    document.addEventListener('click', function(e) {
        if (!notiDropdown.contains(e.target) && e.target !== notiIcon) {
            notiDropdown.classList.add('hidden');
        }
    });
});