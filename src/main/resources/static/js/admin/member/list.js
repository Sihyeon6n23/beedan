document.addEventListener("DOMContentLoaded", function() {
    loadMemberList(0);

    const filterButtons = document.querySelectorAll('.admin-chat-filter');

    filterButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();

            filterButtons.forEach(btn => btn.classList.remove('is-active'));
            this.classList.add('is-active');

            loadMemberList(0);
        });
    });
});


let cachedSummaryHtml = "";

function loadMemberList(page) {
    const activeFilter = document.querySelector('.admin-chat-filter.is-active');
    const status = activeFilter ? activeFilter.getAttribute('data-status') : 'ALL';

    const params = new URLSearchParams({
        page: page,
        size: 10
    });

    if (status && status !== 'ALL') {
        params.append('status', status);
    }

    fetch(`/api/member/list?${params.toString()}`)
        .then(response => {
            if (!response.ok) throw new Error("데이터 로드 실패");
            return response.json();
        })
        .then(data => {
            renderMemberList(data.content);
            renderPagination(data);
        })
        .catch(error => console.error('Error:', error));
}

function openMemberModal(memberId) {
    const modal = document.getElementById('memberDetailModal');
    const memberIdElement = modal.querySelector('.member-id');
    if (memberIdElement) {
        memberIdElement.textContent = "MEMBER ID: " + memberId;
    }
    modal.classList.remove('hidden');
}

function closeMemberModal() {
    const modal = document.getElementById('memberDetailModal');
    modal.classList.add('hidden');
}

function renderMemberList(members) {
    const listBody = document.getElementById('memberListBody');
    listBody.innerHTML = '';

    if (!members || members.length === 0) {
        listBody.innerHTML = '<div style="text-align:center; padding:20px;">회원이 없습니다.</div>';
        return;
    }

    let html = '';
    members.forEach(member => {
        const statusInfo = getStatusInfo(member.memStt);

        html += `
            <div class="admin-chat-row">
                <div class="admin-chat-row__primary">
                    <div class="admin-chat-row__text">
                        <h2>${member.memLgnId}</h2>
                        <p>${member.memNm}</p>
                    </div>
                </div>

                <div class="admin-chat-row__text">
                    <p>${member.memBizTtl}</p>
                    <p>${member.memCeoNm}</p>
                </div>

                <div class="admin-chat-row__status">
                    <span class="admin-chat-badge ${statusInfo.className}">${statusInfo.text}</span>
                </div>

                <div class="admin-chat-row__text" >
                    <p style="text-align:center;">${member.memCreDt ? member.memCreDt.split('T')[0] : '-'}</p>
                </div>

                <div class="action-btns">
                    <button class="btn-detail" onclick="openMemberModal('${member.memId}')">상세보기</button>
                    <a class="btn-edit" href="/admin/detail?id=${member.memId}">수정하기</a>
                </div>
            </div>
        `;
    });
    listBody.innerHTML = html;
}

function getStatusInfo(status) {
    const map = {
        'ACTIVE':   { className: 'admin-chat-badge--open',    text: '활성' },
        'INACTIVE': { className: 'admin-chat-badge--ongoing', text: '비활성' },
        'LOCK':     { className: 'admin-chat-badge--locked',  text: '잠금' },
        'PENDING':  { className: 'admin-chat-badge--pending', text: '대기' },
        'WITHDRAWN':{ className: 'admin-chat-badge--closed',  text: '탈퇴' }
    };
    return map[status] || { className: '', text: '기타' };
}

function renderPagination(pageData) {
    const area = document.getElementById('paginationArea');
    if (!area) return; // HTML에 id="paginationArea"가 있는지 확인하세요.

    if (pageData.totalPages <= 0) { area.innerHTML = ''; return; }

    let html = '<div class="admin-chat-pagination">';
    html += `<button class="admin-chat-page-button ${pageData.first ? 'is-disabled' : ''}" onclick="${!pageData.first ? `loadMemberList(${pageData.number - 1})` : ''}">Prev</button>`;

    const startPage = Math.floor(pageData.number / 5) * 5;
    const endPage = Math.min(startPage + 4, pageData.totalPages - 1);

    for (let i = startPage; i <= endPage; i++) {
        html += `<button class="admin-chat-page-button ${i === pageData.number ? 'is-current' : ''}" onclick="loadMemberList(${i})">${(i + 1).toString().padStart(2, '0')}</button>`;
    }

    html += `<button class="admin-chat-page-button ${pageData.last ? 'is-disabled' : ''}" onclick="${!pageData.last ? `loadMemberList(${pageData.number + 1})` : ''}">Next</button>`;
    html += '</div>';
    area.innerHTML = html;
}