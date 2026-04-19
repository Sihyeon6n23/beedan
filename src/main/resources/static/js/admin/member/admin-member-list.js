document.addEventListener("DOMContentLoaded", function() {
    if (typeof editSuccess !== 'undefined' && editSuccess) {
            showGuideModal("회원 정보가 성공적으로 수정되었습니다.", null, "MODIFIED", "check_circle");
    }

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

// 검색 버튼 클릭 시 실행
function searchMembers() {
    loadMemberList(0);
}

// 초기화 버튼 클릭 시 실행
function resetSearch() {
    document.getElementById('search-input').value = '';
    loadMemberList(0);
}

// =============================== 기본 목록 템플릿 =================================

const DASHBOARD_TEMPLATE = `
    <section class="modal-col col-order">
        <div class="col-header">
            <h3>주문 목록</h3>
            <span class="material-symbols-outlined">receipt_long</span>
        </div>
        <div class="col-content"></div>
        <div class="col-footer">
            <button class="btn-outline-yellow">전체 주문 목록 조회</button>
        </div>
    </section>
    <section class="modal-col col-shipping">
        <div class="col-header bg-gray">
            <h3>배송 목록</h3>
            <span class="material-symbols-outlined">local_shipping</span>
        </div>
        <div class="col-content"></div>
        <div class="col-footer">
            <button class="btn-solid-black">전체 배송 목록 조회</button>
        </div>
    </section>
    <section class="modal-col col-inquiry">
        <div class="col-header">
            <h3>문의 목록</h3>
            <span class="material-symbols-outlined">support_agent</span>
        </div>
        <div class="col-content"></div>
        <div class="col-footer">
            <button class="btn-solid-yellow">전체 문의 내역 조회</button>
        </div>
    </section>
`;


// =============================== 회원 목록 조회 영역 =================================

function loadMemberList(page) {
    const activeFilter = document.querySelector('.admin-chat-filter.is-active');
    const status = activeFilter ? activeFilter.dataset.status : 'ALL';
    const keyword = document.getElementById('search-input').value.trim();

    const params = new URLSearchParams({
        page: page,
        size: 4,
        status: status
    });

    if (keyword) {
        params.append('keyword', keyword);
    }

    fetch(`/api/admin/member/list?${params.toString()}`)
        .then(response => {
            if (!response.ok) throw new Error("데이터 로드 실패");
            return response.json();
        })
       .then(data => {
                   renderMemberList(data.memberList.content, data.isRoot);
                   renderPagination(data.memberList);
       })
        .catch(error => console.error('Error:', error));
}

function renderMemberList(members, isRoot) {
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
                    ${isRoot ? `<a class="btn-edit" href="/admin/member/edit?id=${member.memId}">수정하기</a>` : ''}
                </div>
            </div>
        `;
    });
    listBody.innerHTML = html;
}

function getStatusInfo(status) {
    const map = {
        'ACTIVE':   { className: 'admin-chat-badge--approved',    text: '활성' },
        'INACTIVE': { className: 'admin-chat-badge--ongoing', text: '비활성' },
        'LOCK':     { className: 'admin-chat-badge--open',  text: '잠금' }
    };
    return map[status] || { className: '', text: '기타' };
}

function renderPagination(pageData) {
    const area = document.getElementById('paginationArea');
    if (!area) return;

    // 데이터가 없거나 페이지가 1개 미만일 때 처리
    if (!pageData || pageData.totalPages <= 0) {
        area.innerHTML = '';
        return;
    }

    const currentPage = pageData.number;
    const totalPages = pageData.totalPages;

    // 5개 단위로 페이지 그룹 계산
    const startPage = Math.floor(currentPage / 5) * 5;
    const endPage = Math.min(startPage + 4, totalPages - 1);

    let html = '<div class="admin-chat-pagination">';

    // --- [Prev 버튼] ---
    if (pageData.first || totalPages <= 1) {
        html += `<button class="admin-chat-page-button admin-chat-page-button--wide is-disabled">Prev</button>`;
    } else {
        html += `<button class="admin-chat-page-button admin-chat-page-button--wide" onclick="loadMemberList(${currentPage - 1})">Prev</button>`;
    }

    for (let i = startPage; i <= endPage; i++) {
        const isCurrent = i === currentPage;
        const pageLabel = i + 1;
        html += `<button class="admin-chat-page-button ${isCurrent ? 'is-current' : ''}"
                 onclick="${isCurrent ? '' : 'loadMemberList(' + i + ')'}">${pageLabel}</button>`;
    }

    // --- [Next 버튼] ---
    if (pageData.last || totalPages <= 1) {
        html += `<button class="admin-chat-page-button admin-chat-page-button--wide is-disabled">Next</button>`;
    } else {
        html += `<button class="admin-chat-page-button admin-chat-page-button--wide" onclick="loadMemberList(${currentPage + 1})">Next</button>`;
    }

    html += '</div>';
    area.innerHTML = html;
}

// =============================== 모달 제어 영역 =================================

function openMemberModal(memId) {
    const modal = document.getElementById('memberDetailModal');
    modal.classList.remove('hidden');

    document.body.classList.add('modal-open');
    modal.dataset.currentMemberId = memId;

    setupDashboardLayout();
    initModalData(memId);
}

function setupDashboardLayout() {
    const contentArea = document.getElementById('modalContentArea');
    contentArea.innerHTML = DASHBOARD_TEMPLATE;
}

function initModalData(memId) {
    fetch(`/api/admin/member/${memId}/summary`)
        .then(response => {
            if (!response.ok) throw new Error("회원 상세 정보를 불러오지 못했습니다.");
            return response.json();
        })
        .then(data => {
            document.querySelector('.member-id').textContent = data.memLgnId;
            document.querySelector('.join-date').textContent = data.memCreDt ? data.memCreDt.split('T')[0] : '';

            document.querySelector('.member-nm').textContent = data.memNm;

            renderModalOrders(data.recentOrders);
            renderModalShipments(data.recentShipments);
            renderModalInquiries(data.recentInquiries?.content || []);

            updateModalFooterButtons(memId);
        })
        .catch(error => {
            console.error('Error:', error);

            const nameEl = document.querySelector('.member-nm');
            if(nameEl) nameEl.textContent = "데이터 로드 실패";
        });
}

function closeMemberModal() {
    const modal = document.getElementById('memberDetailModal');

    document.body.classList.remove('modal-open');

    modal.classList.add('hidden');
}

function restoreDashboard() {
    const modal = document.getElementById('memberDetailModal');

    const memId = modal.dataset.currentMemberId;

    if (memId) {
        setupDashboardLayout();

        initModalData(memId);
    } else {
        console.error("회원 ID를 찾을 수 없어 대시보드를 복구할 수 없습니다.");
    }
}

// =============================== 모달 세부 렌더링 영역 =================================

function renderModalOrders(orders) {
    const container = document.querySelector('.col-order .col-content');
    if(!container) return;
    container.innerHTML = '';

    if (!orders || orders.length === 0) {
        container.innerHTML = '<p style="padding:20px; text-align:center;">최근 주문 내역이 없습니다.</p>';
        return;
    }

    let html = '';
    orders.forEach(order => {
        const dateStr = order.ordBaseCreDt ? order.ordBaseCreDt.split('T')[0] : '-';

        const sttInfo = getOrderBadgeTheme(order.ordBaseStt);

        html += `
            <article class="list-item ${order.ordBaseStt === 'DELIVERING' ? 'border-yellow' : ''}">
                <span class="item-meta">${dateStr} • #${order.ordBaseNo}</span>
                <h4 class="item-title">${order.ordSummaryNm}</h4>
                <div class="item-footer">
                    <span class="item-price">${order.ordBaseTtAm ? order.ordBaseTtAm.toLocaleString() : 0}원</span>
                    <span class="badge-status ${sttInfo.badgeClass}">${sttInfo.text}</span>
                </div>
            </article>
        `;
    });
    container.innerHTML = html;
}

function renderModalShipments(shipments) {
    const container = document.querySelector('.col-shipping .col-content');
    if(!container) return;
    container.innerHTML = '';

    if (!shipments || shipments.length === 0) {
        container.innerHTML = '<p style="padding:20px; text-align:center;">진행 중인 배송이 없습니다.</p>';
        return;
    }

    let html = '';
    shipments.forEach(shipment => {
        const sttName = shipment.shStt || '상태없음';

        html += `
            <div class="shipping-card border-yellow">
                <span class="shipping-status">운송장 번호: ${shipment.shTraNo || '운송장 미등록'}</span>

                <span class="tracking-label">
                    <p class="tracking-number">배송 상태: ${sttName}</p>
                </span>

                <div class="shipping-address">
                    <div class="address-content">
                        <span class="material-symbols-outlined icon-yellow">location_on</span>
                        <span class="address-text" style="font-size: 13px;">배송지: ${shipment.shAdr || '주소 정보 없음'}</span>
                    </div>
                </div>

            </div>

        `;
    });
    container.innerHTML = html;
}

function renderModalInquiries(input) {
    const container = document.querySelector('.col-inquiry .col-content');
    const inquiries = Array.isArray(input) ? input : (input?.content || []);

    if(!container) return;
    container.innerHTML = '';

    if (!inquiries || inquiries.length === 0) {
        container.innerHTML = '<p style="padding:20px; text-align:center;">최근 문의 내역이 없습니다.</p>';
        return;
    }

     const statusMap = {
              RECEIVED: '확인대기',
              IN_PROGRESS: '답변진행중',
              ANSWERED: '답변완료'
     };

    let html = '';
    inquiries.forEach((inquiry, index) => {
        const statusText = statusMap[inquiry.brdInqStt] || '취소';
        const isResolved = inquiry.brdInqStt === 'ANSWERED';
        const dateStr = inquiry.brdCreDt ? inquiry.brdCreDt.split('T')[0] : '-';

        html += `
            <article class="list-item ${isResolved ? 'opacity-60' : ''}">
                <div class="inquiry-meta">
                    <span class="status-text ${isResolved ? '' : 'text-yellow'}">${statusText}</span>
                    <span class="time">${dateStr}</span>
                </div>

                <h4 class="item-title" style="cursor:pointer;">${inquiry.brdTtl}</h4>

                <p class="item-desc">#${inquiry.brdId}번 문의사항입니다.</p>
            </article>
            ${index < inquiries.length - 1 ? '<hr class="divider">' : ''}
        `;
    });
    container.innerHTML = html;
}

// =============================== 모달 상세 목록 조회 영역 =================================

function updateModalFooterButtons(memId) {
    const orderBtn = document.querySelector('.col-order .btn-outline-yellow');
    const shippingBtn = document.querySelector('.col-shipping .btn-solid-black');
    const inquiryBtn = document.querySelector('.col-inquiry .btn-solid-yellow');

    if(orderBtn) orderBtn.setAttribute('onclick', `viewFullOrderList('${memId}', 0)`);
    if(shippingBtn) shippingBtn.setAttribute('onclick', `viewFullShipmentList('${memId}', 0)`);
    if(inquiryBtn) inquiryBtn.setAttribute('onclick', `viewFullInquiryIList('${memId}', 0)`);
}

async function viewFullList(url, title) {
    const contentArea = document.getElementById('modalContentArea');

    contentArea.innerHTML = `
        <div class="full-list-container">
            <div class="list-header">
                <button class="btn-back" onclick="restoreDashboard()">
                    <span class="material-symbols-outlined">arrow_back</span> 이전으로
                </button>
                <h3>${title}</h3>
            </div>
            <div class="list-table-wrapper">
                <table>
                    <thead>
                        <tr id="list-thead"></tr>
                    </thead>
                    <tbody id="list-tbody">
                        <tr><td colspan="5" style="text-align:center;"></td></tr>
                    </tbody>
                </table>
            </div>
        </div>
    `;

    try {
        const response = await fetch(url);
        const data = await response.json();

        renderTableData(data, title);
    } catch (error) {
        console.error("데이터 조회 실패:", error);
        document.getElementById('list-tbody').innerHTML = '<tr><td colspan="5">데이터 로드에 실패했습니다.</td></tr>';
    }
}

function getOrderBadgeTheme(status) {
    switch(status) {
        case 'PREPARING':
            return { text: '상품준비', badgeClass: 'badge-yellow' };
        case 'DELIVERING':
            return { text: '배송중', badgeClass: 'badge-yellow' };
        case 'DELIVERED':
            return { text: '배송완료', badgeClass: 'badge-green' };
        default:
            return { text: '주문취소', badgeClass: 'badge-red' };
    }
}

async function viewFullOrderList(memId, page = 0) {
    const contentArea = document.getElementById('modalContentArea');

    contentArea.innerHTML = `
        <div class="order-history-fragment">
            <div class="fragment-top">
                <button class="btn-go-back" onclick="restoreDashboard()">
                    <span class="material-symbols-outlined">arrow_back</span> 이전으로
                </button>
                <div class="title-row">
                    <h2 class="fragment-title">ORDER History</h2>
                </div>
            </div>

            <div class="stats-row" id="orderStatsRow" style="display: none;"></div>

            <div class="table-container">
                <table class="fragment-table">
                    <thead>
                        <tr>
                            <th style="text-align: center;">주문번호</th>
                            <th>주문일자</th>
                            <th>주문품목</th>
                            <th style="text-align: center;">수령자</th>
                            <th class="text-center">총금액</th>
                            <th class="text-center">상태</th>
                            <th style="text-align: center;">관리</th>
                        </tr>
                    </thead>
                    <tbody id="orderListBody">
                        <tr><td colspan="6" style="text-align: center; padding: 30px;">데이터를 불러오는 중...</td></tr>
                    </tbody>
                </table>
                <div class="table-footer-row" id="orderPaginationArea"></div>
            </div>
        </div>
    `;

    try {
        const url = `/api/admin/member/order/${memId}?page=${page}`;
        const response = await fetch(url);
        const data = await response.json();

        const listBody = document.getElementById('orderListBody');
        const paginationArea = document.getElementById('orderPaginationArea');

        if (!data.content || data.content.length === 0) {
            listBody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 30px;">주문 내역이 없습니다.</td></tr>`;
            paginationArea.innerHTML = '';
            return;
        }

        const rowsHtml = data.content.map(order => {
            const sttInfo = getOrderBadgeTheme(order.ordBaseStt);
            const amount = order.ordBaseTtAm ? order.ordBaseTtAm.toLocaleString('ko-KR') : '0';
            const dateStr = order.ordBaseCreDt ? order.ordBaseCreDt.substring(0, 10).replaceAll('-', '.') : '-';

            const isCanceled = order.ordBaseStt === 'CANCELED';

            return `
                <tr>
                    <td class="order-id" style="text-align: center;">${order.ordBaseNo}</td>
                    <td class="order-date">${dateStr}</td>
                    <td class="order-summary">
                        <strong>${order.ordSummaryNm || '상품 정보 없음'}</strong><br>
                    </td>
                    <td class="order-address" style="text-align: center;">
                        <p>${order.ordBaseRcvNm}</p>
                    </td>
                    <td class="order-amount text-right" style="text-align: right;">${amount}원</td>
                    <td class="order-status text-center">
                        <span class="badge-status ${sttInfo.badgeClass}">${sttInfo.text}</span>
                    </td>
                    <td class="action-cell">
                        <div class="admin-action-wrapper">
                            <select id="status-select-${order.ordBaseId}"
                                    class="status-update-select"
                                    ${isCanceled ? 'disabled' : ''}>
                                <option value="" disabled selected>${isCanceled ? '변경 불가' : '상태 변경'}</option>
                                <option value="PREPARING">상품준비</option>
                                <option value="DELIVERING">배송중</option>
                                <option value="DELIVERED">배송완료</option>
                                <option value="CANCELED">주문취소</option>
                            </select>

                            <button class="btn-icon-sm"
                                    onclick="submitOrderStatusUpdate('${order.ordBaseId}', ${page})"
                                    title="변경상태저장"
                                    ${isCanceled ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''}>
                                <span class="material-symbols-outlined">edit</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        listBody.innerHTML = rowsHtml;

        const startItem = (data.number * data.size) + 1;
        const endItem = Math.min(startItem + data.size - 1, data.totalElements);

        let paginationHtml = `<span class="showing-text">Showing ${startItem}-${endItem} of ${data.totalElements} orders</span>`;
        paginationHtml += `<div class="fragment-pagination">`;

        if(data.first) {
            paginationHtml += `<button class="page-arrow" disabled><span class="material-symbols-outlined" style="color:#ccc;">chevron_left</span></button>`;
        } else {
            paginationHtml += `<button class="page-arrow" onclick="viewFullOrderList('${memId}', ${data.number - 1})">
                <span class="material-symbols-outlined">chevron_left</span></button>`;
        }

        for (let i = 0; i < data.totalPages; i++) {
            const activeClass = (i === data.number) ? 'active' : '';
            paginationHtml += `<button class="page-num ${activeClass}" onclick="viewFullOrderList('${memId}', ${i})">${i + 1}</button>`;
        }

        if(data.last) {
            paginationHtml += `<button class="page-arrow" disabled><span class="material-symbols-outlined" style="color:#ccc;">chevron_right</span></button>`;
        } else {
            paginationHtml += `<button class="page-arrow" onclick="viewFullOrderList('${memId}', ${data.number + 1})"><span class="material-symbols-outlined">chevron_right</span></button>`;
        }

        paginationHtml += `</div>`;
        paginationArea.innerHTML = paginationHtml;

    } catch (error) {
        console.error("데이터 로드 에러:", error);
        document.getElementById('orderListBody').innerHTML = `<tr><td colspan="6" style="text-align: center; color: red; padding: 30px;">데이터 로드에 실패했습니다.</td></tr>`;
    }
}

// =============================== 주문 상태 수정 영역 =================================

async function submitOrderStatusUpdate(orderId, currentPage) {
    const selectElement = document.getElementById(`status-select-${orderId}`);
    const newStatus = selectElement.value;
    const memberId = document.getElementById('memberDetailModal').dataset.currentMemberId;
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    try {
        const response = await fetch(`/api/admin/member/order/${memberId}/status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({
                ordBaseId: orderId,
                ordBaseStt: newStatus
            })
        });

        if (!response.ok) {
            throw new Error('상태 업데이트에 실패했습니다.');
        }

        alert('주문 상태가 성공적으로 변경되었습니다.');

        viewFullOrderList(memberId, currentPage);

    } catch (error) {
        console.error('Update Error:', error);
        alert(error.message);
    }
}

// =============================== 배송 목록 조회 =================================
function getShipmentBadgeTheme(status) {
    switch(status) {
        case 'PREPARING':
            return { text: '배송준비', badgeClass: 'badge-gray' };
        case 'DELIVERING':
            return { text: '배송중', badgeClass: 'badge-yellow' };
        case 'SHIPPING':
            return { text: '통관진행', badgeClass: 'badge-yellow' };
        case 'DELIVERED':
            return { text: '배송완료', badgeClass: 'badge-green' };
        default:
            return { text: status || '미확인상태', badgeClass: 'badge-gray' };
    }
}

// 전체 배송 목록 조회
async function viewFullShipmentList(memId, page = 0) {
    const contentArea = document.getElementById('modalContentArea');

    contentArea.innerHTML = `
        <div class="order-history-fragment">
            <div class="fragment-top">
                <button class="btn-go-back" onclick="restoreDashboard()">
                    <span class="material-symbols-outlined">arrow_back</span> 이전으로
                </button>
                <div class="title-row">
                    <h2 class="fragment-title">SHIPPING History</h2>
                </div>
            </div>

            <div class="table-container">
                <table class="fragment-table">
                    <thead>
                        <tr>
                            <th style="text-align: center;">운송장번호</th>
                            <th>배송지</th>
                            <th style="text-align: center;">수령자</th>
                            <th>등록일</th>
                            <th class="text-center">상태</th>
                            <th style="text-align: center;">관리</th>
                        </tr>
                    </thead>
                    <tbody id="shipmentListBody">
                        <tr><td colspan="5" style="text-align: center; padding: 30px;">데이터를 불러오는 중...</td></tr>
                    </tbody>
                </table>
                <div class="table-footer-row" id="shipmentPaginationArea"></div>
            </div>
        </div>
    `;

    try {
        const url = `/api/admin/member/shipment/${memId}?page=${page}`;
        const response = await fetch(url);
        const data = await response.json();

        const listBody = document.getElementById('shipmentListBody');
        const paginationArea = document.getElementById('shipmentPaginationArea');

        if (!data.content || data.content.length === 0) {
            listBody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 30px;">배송 내역이 없습니다.</td></tr>`;
            paginationArea.innerHTML = '';
            return;
        }

        const rowsHtml = data.content.map(shipment => {
            const sttInfo = getShipmentBadgeTheme(shipment.shStt);
            const dateStr = shipment.shCreDt ? shipment.shCreDt.substring(0, 10).replaceAll('-', '.') : '-';
            const traNo = shipment.shTraNo || '운송장 미등록';
            const address = shipment.shAdr || '주소 정보 없음';
            const addressDetail = shipment.shAdrDt || '-';
            const rcvNm = shipment.shRcvNm || '수령자 없음';

            const isShipmentCanceled = (shipment.shCanYn === true || shipment.shCanYn === 'Y');

            return `
                <tr>
                    <td style="text-align: center;"><strong>${traNo}</strong></td>
                    <td class="order-address">
                        <p>${address}</p>
                        <p>${addressDetail}
                    </td>

                    <td style="text-align: center;">${rcvNm}</td>

                    <td class="order-date">${dateStr}</td>
                    <td class="order-status text-center">
                        <span class="badge-status ${sttInfo.badgeClass}">${sttInfo.text}</span>
                    </td>
                    <td class="action-cell">
                        <div class="admin-action-wrapper" style="justify-content: center;">
                            <button class="btn-detail"
                                    onclick="shipmentDetail('${shipment.shId}', '${memId}', ${page})"
                                    ${isShipmentCanceled ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''}>
                                ${isShipmentCanceled ? '배송 취소됨' : '배송 현황'}
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        listBody.innerHTML = rowsHtml;

        const startItem = (data.number * data.size) + 1;
        const endItem = Math.min(startItem + data.size - 1, data.totalElements);

        let paginationHtml = `<span class="showing-text">Showing ${startItem}-${endItem} of ${data.totalElements} shipments</span>`;
        paginationHtml += `<div class="fragment-pagination">`;

        if(data.first) {
            paginationHtml += `<button class="page-arrow" disabled><span class="material-symbols-outlined" style="color:#ccc;">chevron_left</span></button>`;
        } else {
            paginationHtml += `<button class="page-arrow" onclick="viewFullShipmentList('${memId}', ${data.number - 1})">
                <span class="material-symbols-outlined">chevron_left</span></button>`;
        }

        for (let i = 0; i < data.totalPages; i++) {
            const activeClass = (i === data.number) ? 'active' : '';
            paginationHtml += `<button class="page-num ${activeClass}" onclick="viewFullShipmentList('${memId}', ${i})">${i + 1}</button>`;
        }

        if(data.last) {
            paginationHtml += `<button class="page-arrow" disabled><span class="material-symbols-outlined" style="color:#ccc;">chevron_right</span></button>`;
        } else {
            paginationHtml += `<button class="page-arrow" onclick="viewFullShipmentList('${memId}', ${data.number + 1})"><span class="material-symbols-outlined">chevron_right</span></button>`;
        }

        paginationHtml += `</div>`;
        paginationArea.innerHTML = paginationHtml;

    } catch (error) {
        console.error("데이터 로드 에러:", error);
        document.getElementById('shipmentListBody').innerHTML = `<tr><td colspan="5" style="text-align: center; color: red; padding: 30px;">데이터 로드에 실패했습니다.</td></tr>`;
    }
}

// 배송 상세 현황
function formatTrackingDate(dateStr) {
    if (!dateStr || dateStr.length < 12) return dateStr || '-';

    // 이미 ISO 형식(하이픈 포함)이라면 T만 공백으로 치환
    if (dateStr.includes('-')) {
        return dateStr.replace('T', ' ').substring(0, 16);
    }

    const y = dateStr.substring(0, 4);
    const m = dateStr.substring(4, 6);
    const d = dateStr.substring(6, 8);
    const hh = dateStr.substring(8, 10);
    const mm = dateStr.substring(10, 12);

    return `${y}-${m}-${d} ${hh}:${mm}`;
}

async function shipmentDetail(shId, memId, page) {
    const contentArea = document.getElementById('modalContentArea');

    // 1. 관리자용 상단 헤더 렌더링 (목록 버튼, 다음 단계 버튼 유지)
    contentArea.innerHTML = `
        <div class="order-history-fragment">
            <div class="fragment-top">
                <button class="btn-go-back" onclick="viewFullShipmentList('${memId}', ${page})">
                    <span class="material-symbols-outlined">arrow_back</span> 목록으로 돌아가기
                </button>

                <div class="title-row" style="display: flex; justify-content: space-between; align-items: center;">
                    <h2 class="fragment-title" style="margin: 0;">TRACKING Details</h2>
                    <button id="demoNextBtn" onclick="advanceDemoStatus('${shId}', '${memId}', ${page})"
                            style="display:none; background-color:#ff4757; color:white; border:none; padding:8px 12px; border-radius:4px; cursor:pointer;">
                        다음 배송 단계
                    </button>
                </div>
            </div>

            <div id="adminTrackingContent" class="tracking-content-wrapper">
                <div style="text-align:center; padding:30px; color:#666;">배송 데이터를 불러오는 중입니다...</div>
            </div>
        </div>
    `;

    try {
        const response = await fetch(`/api/admin/member/shipment/${shId}/track`);
        if (!response.ok) throw new Error("서버와 통신 중 문제가 발생했습니다.");

        const data = await response.json();
        const nextBtn = document.getElementById('demoNextBtn');
        const trackingContent = document.getElementById('adminTrackingContent');

        // [핵심 수정] 운송장 번호에 'TEST'가 포함되어 있는지 확인 (관리자 전용 기능)
        const isTestShipment = data.trackingNumber && data.trackingNumber.includes('TEST');

        if (nextBtn) {
            if (isTestShipment) {
                nextBtn.style.display = 'block';
                if (data.shCanYn === 'Y' || data.isCanceled) {
                    nextBtn.disabled = true;
                    nextBtn.style.backgroundColor = '#ccc';
                    nextBtn.innerText = '취소된 배송';
                    nextBtn.style.cursor = 'not-allowed';
                }
            } else {
                nextBtn.style.display = 'none';
            }
        }

        // --- 여기서부터 회원 쪽 디자인 양식 적용 ---
        const hasDomestic = data.details && data.details.length > 0;
        const customsOpen = hasDomestic ? '' : 'open';

        let html = `
            <div class="tracking-info-summary">
                <div class="info-item"><span class="label">택배사</span><div class="value">${data.carrierName || '-'}</div></div>
                <div class="info-item"><span class="label">수령인</span><div class="value">${data.shRcvNm || '-'}</div></div>
                <div class="info-item info-item--address"><span class="label">배송지</span><div class="value">${data.shAdr || '-'}</div></div>
                <div class="info-item"><span class="label">송장번호</span><div class="value">${data.trackingNumber || '정보 없음'}</div></div>
                <div class="info-item info-item--status"><span class="label">현재 상태</span><div class="value"><strong>${data.statusText || '-'}</strong></div></div>
            </div>
            <div class="tracking-timeline" style="margin-top: 20px;">
        `;

        // 해외 통관 내역 포맷팅 적용
        if (data.customsDetails && data.customsDetails.length > 0) {
            html += `<details ${customsOpen} style="margin-bottom:20px; background:#f8f9fa; border:1px solid #eee; border-radius:4px;">
                        <summary style="padding:10px; cursor:pointer; font-weight:bold;">해외 통관 내역 (${data.customsDetails.length}건)</summary>
                        <div style="padding:10px;">`;
            data.customsDetails.forEach(c => {
                html += `<div style="padding:8px 0; border-bottom:1px solid #f1f1f1;">
                            <small style="color:#888; font-size: 12px;">${formatTrackingDate(c.time)}</small>
                                <div style="margin-top:2px;">${c.status}</div>
                            <small style="color:#666;">${c.description}</small>
                         </div>`;
            });
            html += `</div></details>`;
        }

        // 국내 배송 현황 포맷팅 적용
        if (hasDomestic) {
            html += '<h4 style="margin:15px 0 12px; font-size:16px;">국내 배송 현황</h4><ul style="padding-left:25px; border-left:2px solid #eee; list-style:none; margin:0;">';

            data.details.slice().reverse().forEach((item, idx) => {
                const isFirst = idx === 0;
                // 포맷팅 통합 적용
                const displayTime = formatTrackingDate(item.time);

                html += `<li style="margin-bottom:20px; position:relative;">
                            <span style="position:absolute; left:-31px; top:5px; width:10px; height:10px; border-radius:50%; background:${isFirst ? '#d9534f' : '#ccc'}; box-shadow:${isFirst ? '0 0 0 3px rgba(217,83,79,0.2)' : 'none'};"></span>
                            <small style="color:#888; font-size: 12px;">${displayTime}</small>
                            <div style="margin-top:2px;"><strong>${item.status}</strong></div>
                            <p style="margin:2px 0 0; font-size:13px; color:#777;">${item.description || ''}</p>
                         </li>`;
            });
            html += '</ul>';
        } else {
            html += '<div style="text-align:center; padding:30px; background:#fafafa; border-radius:4px; color:#999;">아직 국내 배송 정보가 없습니다.</div>';
        }

        html += '</div>';

        // 구성된 HTML을 렌더링 영역에 삽입
        trackingContent.innerHTML = html;

    } catch (error) {
        document.getElementById('adminTrackingContent').innerHTML = `
            <div style="color:red; text-align:center; padding: 30px;">
                오류: ${error.message}
            </div>
        `;
    }
}

// 시연용 강제 상태 업데이트
async function advanceDemoStatus(shId, memId, page) {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    if(!confirm("배송 상태를 다음 단계로 이동시킬까요? (물류 데모 시연용)")) return;

    try {
        const response = await fetch(`/api/admin/member/shipment/${shId}/demo-progress`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        });

        if (response.ok) {
            alert("상태가 성공적으로 업데이트 되었습니다.");
            // 상태 업데이트 후, 다시 타임라인 화면을 새로고침하여 바뀐 상태 반영
            shipmentDetail(shId, memId, page);
        } else {
            alert("상태 업데이트 실패: 처리 중 문제가 발생했습니다.");
        }
    } catch (error) {
        console.error("통신 오류:", error);
        alert("네트워크 오류가 발생했습니다.");
    }
}

// =========================== 문의 전체 목록 조회 ==============================

async function viewFullInquiryIList(memId, page = 0) {
    const contentArea = document.getElementById('modalContentArea');

    contentArea.innerHTML = `
        <div class="order-history-fragment">
            <div class="fragment-top">
                <button class="btn-go-back" onclick="restoreDashboard()">
                    <span class="material-symbols-outlined">arrow_back</span> 이전으로
                </button>
                <div class="title-row">
                    <h2 class="fragment-title">INQUIRY History</h2>
                </div>
            </div>

            <div class="table-container">
                <table class="fragment-table">
                    <thead>
                        <tr>
                            <th>문의 제목</th>
                            <th style="text-align: center;">상태</th>
                            <th>회사 상호명</th>
                            <th>등록일</th>
                            <th class="text-center">관리</th>
                        </tr>
                    </thead>
                    <tbody id="inquiryListBody">
                        <tr><td colspan="5" style="text-align: center; padding: 30px;">데이터를 불러오는 중...</td></tr>
                    </tbody>
                </table>
                <div class="table-footer-row" id="inquiryPaginationArea"></div>
            </div>
        </div>
    `;

    try {
        const url = `/api/admin/inquiries/${memId}?page=${page}&size=8`;
        const response = await fetch(url);
        const data = await response.json();

        const listBody = document.getElementById('inquiryListBody');
        const paginationArea = document.getElementById('inquiryPaginationArea');

        if (!data.content || data.content.length === 0) {
            listBody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 30px;">문의 내역이 없습니다.</td></tr>`;
            paginationArea.innerHTML = '';
            return;
        }
// 8개
        const rowsHtml = data.content.map(inquiry => {
            const statusMap = {
                RECEIVED: { text: '접수', className: 'badge-yellow' },
                IN_PROGRESS: { text: '처리중', className: 'badge-blue' },
                ANSWERED: { text: '답변완료', className: 'badge-green' },
                CANCELLED: { text: '취소', className: 'badge-red' }
            };
            const sttInfo = statusMap[inquiry.brdInqStt] || { text: '미확인', className: 'badge-gray' };
            const dateStr = inquiry.brdCreDt ? inquiry.brdCreDt.substring(0, 10).replaceAll('-', '.') : '-';
            const companyName = inquiry.memBizTtl || '상호명 미등록';

            return `
                <tr>
                    <td class="inquiry-title">
                        <strong>${inquiry.brdTtl || '제목 없음'}</strong>
                    </td>
                    <td class="inquiry-status text-center">
                        <span class="badge-status ${sttInfo.className}">${sttInfo.text}</span>
                    </td>
                    <td class="inquiry-company">${companyName}</td>
                    <td class="inquiry-date">${dateStr}</td>
                    <td class="action-cell text-center">
                           <a class="btn-edit" href="/admin/inquiry/detail?id=${inquiry.brdId}">상세보기</a>
                    </td>
                </tr>
            `;
        }).join('');

        listBody.innerHTML = rowsHtml;

        const startItem = (data.number * data.size) + 1;
        const endItem = Math.min(startItem + data.size - 1, data.totalElements);

        let paginationHtml = `<span class="showing-text">Showing ${startItem}-${endItem} of ${data.totalElements} inquiries</span>`;
        paginationHtml += `<div class="fragment-pagination">`;

        if(data.first) {
            paginationHtml += `<button class="page-arrow" disabled><span class="material-symbols-outlined" style="color:#ccc;">chevron_left</span></button>`;
        } else {
            paginationHtml += `<button class="page-arrow" onclick="viewFullInquiryIList('${memId}', ${data.number - 1})">
                        <span class="material-symbols-outlined">chevron_left</span></button>`;
        }

        for (let i = 0; i < data.totalPages; i++) {
            const activeClass = (i === data.number) ? 'active' : '';
            paginationHtml += `<button class="page-num ${activeClass}" onclick="viewFullInquiryIList('${memId}', ${i})">${i + 1}</button>`;
        }

        if (data.last) {
            paginationHtml += `<button class="page-arrow" disabled><span class="material-symbols-outlined" style="color:#ccc;">chevron_right</span></button>`;
        } else {
            paginationHtml += `<button class="page-arrow" onclick="viewFullInquiryIList('${memId}', ${data.number + 1})">
                <span class="material-symbols-outlined">chevron_right</span></button>`;
        }

        paginationHtml += `</div>`;
        paginationArea.innerHTML = paginationHtml;

    } catch (error) {
        console.error("문의 목록 조회 실패:", error);
        document.getElementById('inquiryListBody').innerHTML = '<tr><td colspan="5">데이터 로드에 실패했습니다.</td></tr>';
    }
}

