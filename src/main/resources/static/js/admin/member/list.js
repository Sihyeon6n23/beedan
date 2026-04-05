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

// =============================== 상수 영역 =================================

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
    const status = activeFilter ? activeFilter.getAttribute('data-status') : 'ALL';

    const params = new URLSearchParams({
        page: page,
        size: 10
    });

    if (status && status !== 'ALL') {
        params.append('status', status);
    }

    fetch(`/api/admin/member/list?${params.toString()}`)
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
    if (!area) return;

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

// =============================== 모달 제어 영역 (Refactored) =================================

function openMemberModal(memberId) {
    const modal = document.getElementById('memberDetailModal');
    modal.classList.remove('hidden');

    modal.setAttribute('data-current-member-id', memberId);

    setupDashboardLayout();
    initModalData(memberId);
}

function setupDashboardLayout() {
    const contentArea = document.getElementById('modalContentArea');
    contentArea.innerHTML = DASHBOARD_TEMPLATE;
}

function initModalData(memberId) {
    fetch(`/api/admin/member/${memberId}/summary`)
        .then(response => {
            if (!response.ok) throw new Error("회원 상세 정보를 불러오지 못했습니다.");
            return response.json();
        })
        .then(data => {
            document.querySelector('.member-id').textContent = data.memLgnId;
            document.querySelector('.join-date').textContent = data.memCreDt ? data.memCreDt.split('T')[0] : '';
            document.querySelector('.member-name').textContent = data.memNm;

            renderModalOrders(data.recentOrders);
            renderModalShipments(data.recentShipments);
            renderModalInquiries(data.recentInquiries);

            updateModalFooterButtons(memberId);
        })
        .catch(error => {
            console.error('Error:', error);
            document.querySelector('.member-name').textContent = "데이터 로드 실패";
        });
}

function closeMemberModal() {
    const modal = document.getElementById('memberDetailModal');
    modal.classList.add('hidden');
}

function restoreDashboard() {
    const modal = document.getElementById('memberDetailModal');

    const memberId = modal.getAttribute('data-current-member-id');

    if (memberId) {
        setupDashboardLayout();

        initModalData(memberId);
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
        const sttName = order.ordBaseStt === 'DELIVERED' ? '배송완료' : (order.ordBaseStt === 'DELIVERING' ? '배송중' : order.ordBaseStt);

        html += `
            <article class="list-item ${order.ordBaseStt === 'DELIVERING' ? 'border-yellow' : ''}">
                <span class="item-meta">${dateStr} • #${order.ordBaseNo}</span>
                <h4 class="item-title">${order.ordSummaryNm}</h4>
                <div class="item-footer">
                    <span class="item-price">${order.ordBaseTtAm ? order.ordBaseTtAm.toLocaleString() : 0}원</span>
                    <span class="badge ${order.ordBaseStt === 'DELIVERED' ? 'badge-yellow' : 'badge-gray'}">${sttName}</span>
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
                <span class="shipping-status">배송 정보 (${sttName})</span>
                <h4 class="tracking-number">${shipment.shTraNo || '운송장 미등록'}</h4>
                <span class="tracking-label">운송장 번호</span>

                <div class="tracking-timeline">
                    <div class="timeline-item active">
                        <span class="location">배송 준비 중</span>
                        <span class="time">-</span>
                    </div>
                </div>
            </div>

            <div class="shipping-address">
                <h5>Destination Address</h5>
                <div class="address-content">
                    <span class="material-symbols-outlined icon-yellow">location_on</span>
                    <span class="address-text">${shipment.shAdr || '주소 정보 없음'}</span>
                </div>
            </div>
        `;
    });
    container.innerHTML = html;
}

function renderModalInquiries(inquiries) {
    const container = document.querySelector('.col-inquiry .col-content');
    if(!container) return;
    container.innerHTML = '';

    if (!inquiries || inquiries.length === 0) {
        container.innerHTML = '<p style="padding:20px; text-align:center;">최근 문의 내역이 없습니다.</p>';
        return;
    }

    let html = '';
    inquiries.forEach((inquiry, index) => {
        const isResolved = inquiry.status === 'CLOSED';

        html += `
            <article class="list-item ${isResolved ? 'opacity-60' : ''}">
                <div class="inquiry-meta">
                    <span class="status-text ${!isResolved ? 'text-yellow' : ''}">${inquiry.statusName}</span>
                    <span class="time">${inquiry.timeAgo}</span>
                </div>
                <h4 class="item-title">${inquiry.title}</h4>
                <p class="item-desc">"${inquiry.previewText}"</p>
            </article>
            ${index < inquiries.length - 1 ? '<hr class="divider">' : ''}
        `;
    });
    container.innerHTML = html;
}

// =============================== 모달 상세 목록 조회 영역 =================================

function updateModalFooterButtons(memberId) {
    const orderBtn = document.querySelector('.col-order .btn-outline-yellow');
    const shippingBtn = document.querySelector('.col-shipping .btn-solid-black');
    const inquiryBtn = document.querySelector('.col-inquiry .btn-solid-yellow');

    if(orderBtn) orderBtn.setAttribute('onclick', `viewFullOrderList('${memberId}', 0)`);
    if(shippingBtn) shippingBtn.setAttribute('onclick', `viewFullShipmentList('${memberId}', 0)`);
    if(inquiryBtn) inquiryBtn.setAttribute('onclick', `viewFullList('/api/admin/inquiry/${memberId}', '전체 문의 내역')`);
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

async function viewFullOrderList(memberId, page = 0) {
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
                            <th>주문번호</th>
                            <th>주문일자</th>
                            <th>주문품목</th>
                            <th>배송지/수령자</th>
                            <th class="text-center">총금액</th>
                            <th class="text-center">상태</th>
                            <th class="text-center">관리</th>
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
        const url = `/api/admin/member/order/${memberId}?page=${page}`;
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
            const dateStr = order.ordBaseCreDt ? order.ordBaseCreDt.substring(0, 10).replace(/-/g, '.') : '-';

            return `
                <tr>
                    <td class="order-id">#${order.ordBaseNo}</td>
                    <td class="order-date">${dateStr}</td>

                    <td class="order-summary">
                        <strong>${order.ordSummaryNm || '상품 정보 없음'}</strong><br>
                    </td>

                    <td class="order-address">
                        <p>${order.ordBaseAdr}</p>
                        <p>${order.ordBaseRcvNm}</p>
                    </td>

                    <td class="order-amount text-right">${amount}원</td>

                    <td class="order-status text-center">
                        <span class="badge-status ${sttInfo.badgeClass}">${sttInfo.text}</span>
                    </td>

                    <td class="action-cell">
                        <div class="admin-action-wrapper">
                            <select class="status-update-select" onchange="updateOrderStatus(${order.ordBaseId}, this.value)">
                                <option value="" disabled selected>상태 변경</option>
                                <option value="PREPARING">상품준비</option>
                                <option value="DELIVERING">배송중</option>
                                <option value="DELIVERED">배송완료</option>
                                <option value="CANCELED">주문취소</option>
                            </select>

                            <button class="btn-icon-sm" onclick="window.open('/api/member/order, '_blank')" title="상태변경">
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

        if (!data.first) {
            paginationHtml += `<button class="page-arrow" onclick="viewFullOrderList('${memberId}', ${data.number - 1})"><span class="material-symbols-outlined">chevron_left</span></button>`;
        } else {
            paginationHtml += `<button class="page-arrow" disabled><span class="material-symbols-outlined" style="color:#ccc;">chevron_left</span></button>`;
        }

        for (let i = 0; i < data.totalPages; i++) {
            const activeClass = (i === data.number) ? 'active' : '';
            paginationHtml += `<button class="page-num ${activeClass}" onclick="viewFullOrderList('${memberId}', ${i})">${i + 1}</button>`;
        }

        if (!data.last) {
            paginationHtml += `<button class="page-arrow" onclick="viewFullOrderList('${memberId}', ${data.number + 1})"><span class="material-symbols-outlined">chevron_right</span></button>`;
        } else {
            paginationHtml += `<button class="page-arrow" disabled><span class="material-symbols-outlined" style="color:#ccc;">chevron_right</span></button>`;
        }

        paginationHtml += `</div>`;
        paginationArea.innerHTML = paginationHtml;

    } catch (error) {
        console.error("데이터 로드 에러:", error);
        document.getElementById('orderListBody').innerHTML = `<tr><td colspan="6" style="text-align: center; color: red; padding: 30px;">데이터 로드에 실패했습니다.</td></tr>`;
    }
}

// =============================== 배송 상세 목록 조회 영역 =================================

function getShipmentBadgeTheme(status) {
    switch(status) {
        case 'PREPARING':
            return { text: '배송준비', badgeClass: 'badge-gray' };
        case 'DELIVERING':
            return { text: '배송중', badgeClass: 'badge-yellow' };
        case 'SHIPPING':
            return { text: '통관 진행중', badgeClass: 'badge-yellow' };
        case 'DELIVERED':
            return { text: '배송완료', badgeClass: 'badge-green' };
        default:
            return { text: status || '미확인상태', badgeClass: 'badge-gray' };
    }
}

async function viewFullShipmentList(memberId, page = 0) {
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
                            <th>송장번호</th>
                            <th>배송 시작일자</th>
                            <th>배송물품</th>
                            <th>배송지 / 배송현황</th>
                            <th class="text-center">상태</th>
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
        const url = `/api/admin/member/shipment/${memberId}?page=${page}`;
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
            const dateStr = shipment.shCreDt ? shipment.shCreDt.substring(0, 10).replace(/-/g, '.') : '-';

            let itemSummary = '상품 정보 없음';
            if (shipment.items && shipment.items.length > 0) {
                const firstItemName = shipment.items[0].ordItmNm;
                itemSummary = shipment.items.length > 1
                    ? `${firstItemName} 외 ${shipment.items.length - 1}건`
                    : firstItemName;
            }

            return `
                <tr>
                    <td class="shipment-id">#${shipment.shTraNo}</td>
                    <td class="shipment-date">${dateStr}</td>
                    <td class="shipment-summary">
                        <strong>${shipment.shRcvNm || '수령인 없음'}</strong><br>
                        <span>${itemSummary}</span>
                    </td>
                    <td class="shipment-summary">
                        <strong>${shipment.shAdr || '-'}</strong><br>
                        <span style="color:#666;">${shipment.shCarCd || '택배사 미정'} ${shipment.shTraNo || ''}</span>
                    </td>
                    <td class="order-status text-center">
                        <span class="badge-status ${sttInfo.badgeClass}">${sttInfo.text}</span>
                    </td>
                </tr>
            `;
        }).join('');

        listBody.innerHTML = rowsHtml;

        const startItem = (data.number * data.size) + 1;
        const endItem = Math.min(startItem + data.size - 1, data.totalElements);

        let paginationHtml = `<span class="showing-text">Showing ${startItem}-${endItem} of ${data.totalElements} shipments</span>`;
        paginationHtml += `<div class="fragment-pagination">`;

        // Prev
        if (!data.first) {
            paginationHtml += `<button class="page-arrow" onclick="viewFullShipmentList('${memberId}', ${data.number - 1})"><span class="material-symbols-outlined">chevron_left</span></button>`;
        } else {
            paginationHtml += `<button class="page-arrow" disabled><span class="material-symbols-outlined" style="color:#ccc;">chevron_left</span></button>`;
        }

        // Pages
        for (let i = 0; i < data.totalPages; i++) {
            const activeClass = (i === data.number) ? 'active' : '';
            paginationHtml += `<button class="page-num ${activeClass}" onclick="viewFullShipmentList('${memberId}', ${i})">${i + 1}</button>`;
        }

        // Next
        if (!data.last) {
            paginationHtml += `<button class="page-arrow" onclick="viewFullShipmentList('${memberId}', ${data.number + 1})"><span class="material-symbols-outlined">chevron_right</span></button>`;
        } else {
            paginationHtml += `<button class="page-arrow" disabled><span class="material-symbols-outlined" style="color:#ccc;">chevron_right</span></button>`;
        }

        paginationHtml += `</div>`;
        paginationArea.innerHTML = paginationHtml;

    } catch (error) {
        console.error("배송 데이터 로드 에러:", error);
        document.getElementById('shipmentListBody').innerHTML = `<tr><td colspan="5" style="text-align: center; color: red; padding: 30px;">데이터 로드에 실패했습니다.</td></tr>`;
    }
}