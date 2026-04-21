/**
 * 사업자 등록증 새창 열기
 * @param pullPath 서버에서 받은 UUID (또는 경로가 포함된 문자열)
 */
function openBizFile(pullPath) {
    if(!pullPath || pullPath === 'null/null' || pullPath === '/') {
        showGuideModal(
            "첨부된 파일이 없습니다.",
            null,
            "INFO",
            "info"
        );
        return;
    }
    const url = `/files/${pullPath}`;
    window.open(url, '_blank', 'width=800, height=1000, scrollbars=yes');
}

/**
 * 승인/거절 비동기 처리 (Axios 활용)
 * @param memId 회원 PK
 * @param action 'approve' 또는 'deny'
 */
function processMember(memId, action) {
    const confirmMsg = action === 'approve' ? "승인하시겠습니까?" : "거절하시겠습니까?";
    if(!confirm(confirmMsg)) return;

    // 1. CSRF 토큰 추출
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

    if(!csrfMeta || !csrfHeaderMeta) {
        console.error("CSRF meta tags are missing.");
        showGuideModal(
            "보안토큰이 만료되었습니다\n 페이지를 새로고침 해주세요.",
            null,
            "ERROR",
            "error"
        );
        return;
    }

    const csrfToken = csrfMeta.getAttribute('content');
    const csrfHeader = csrfHeaderMeta.getAttribute('content');

    const url = `/api/root/member/${action}/${memId}`;

    // 2. Axios 설정
    const config = {
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }
    };

    // 3. Axios 요청
    axios.post(url, null, config)
        .then(response => {
            showGuideModal(
                action === 'approve' ? "승인 완료되었습니다." : "거절 처리되었습니다.",
                null,
                action === 'approve' ? "APPROVED" : "REJECTED",
                action === 'approve' ? "check_circle" : "info" // 승인은 체크, 거절은 info 아이콘
            );

            const row = document.getElementById(`row-${memId}`);
            if(row) {
                row.remove();
            }

            // 목록 비었는지 확인
            const body = document.getElementById('memberListBody');
            // 'row-'로 시작하는 div가 하나도 없으면 메시지 표시
            if(body && body.querySelectorAll('.admin-chat-row').length === 0) {
                body.innerHTML = '<div style="padding: 50px; text-align: center; color: #999;">승인 대기 중인 회원이 없습니다.</div>';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            const status = error.response ? error.response.status : 'Network Error';
            showGuideModal(
                `처리 중 오류가 발생했습니다. (상태: ${status})`,
                null,
                "ERROR",
                "error"
            );
        });
}