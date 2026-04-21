let allShipments = [];      // 서버에서 받은 전체 배송지 데이터 보관
let currentOrderId = null;  // 현재 주문 ID
let currentOrderStt = null; // 현재 주문 상태
const PAGE_SIZE = 4;        // 한 페이지에 보여줄 배송지 개수

document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('id');

    if (orderId) {
        fetchOrderDetail(orderId);
    } else {
        console.error("주문 ID가 존재하지 않습니다.");
        const body = document.getElementById('order-detail-body');
        if (body) body.innerHTML = '<div style="text-align:center; padding:20px;">잘못된 접근입니다.</div>';
    }
});

// ====== 렌더링 =========
async function fetchOrderDetail(orderId) {
    try {
        const response = await fetch(`/api/orders/${orderId}`);
        if (!response.ok) throw new Error("데이터를 불러오는데 실패했습니다.");

        const data = await response.json();

        // 페이지 네이션을 전역 변수 할당
        allShipments = data.shipmentResponses || [];
        currentOrderId = data.ordBaseId;
        currentOrderStt = data.ordBaseStt;

        renderPagedShipments(0);

    } catch (error) {
        console.error('Error:', error);
        const body = document.getElementById('order-detail-body');
        if (body) body.innerHTML = `<div style="text-align:center; padding:20px; color:red;">${error.message}</div>`;
    }
}

function renderPagedShipments(page) {
    const listBody = document.getElementById('order-detail-body');
    if (!listBody) return;

    const startIndex = page * PAGE_SIZE;
    const pagedData = allShipments.slice(startIndex, startIndex + PAGE_SIZE);

    if (pagedData.length === 0 && page === 0) {
        listBody.innerHTML = '<div class="admin-chat-row"><span style="display:block; width:100%; text-align:center;">배송 정보가 없습니다.</span></div>';
        return;
    }

    listBody.innerHTML = pagedData.map(ship => {
        const statusData = getStatusInfo(ship);
        const swiperHtml = generateSwiperHtml(ship);
        const itemTexts = ship.shipmentItems.map(item => `<p style="text-align: left;">${item.ordItmNm} (${item.shQn}개)</p>`).join('');

        // 주문 상태가 'PREPARING'일 때만 취소 버튼 노출
        const cancelBtnHtml = currentOrderStt === 'PREPARING'
            ? `<button class="btn-edit" onclick="cancelOrder(${currentOrderId})" type="button">주문 취소</button>`
            : '';

        const isShipmentCanceled = (ship.shCanYn === true);

        return `
            <div class="admin-chat-row">
                <div class="admin-chat-row__text">
                    <h2>${ship.shRcvNm}</h2>
                    <p>${ship.shAdr}</p>
                    <p>${ship.shAdrDt}</p>
                </div>

                <div class="swiper mySwiper">
                    <div class="swiper-wrapper">
                        ${swiperHtml}
                    </div>
                    <div class="swiper-button-next"></div>
                    <div class="swiper-button-prev"></div>
                </div>

                <div class="admin-chat-row__text">
                    ${itemTexts}
                </div>

                <div class="admin-chat-row__status">
                    <span class="admin-chat-badge ${statusData.badgeClass}">${statusData.statusText}</span>
                </div>

                <div class="action-btns">
                    <button type="button" class="btn-detail" onclick="openShipmentModal(${ship.shId})"
                        ${isShipmentCanceled ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''}>
                        ${isShipmentCanceled ? '주문 취소됨' : '배송 현황'}
                    </button>
                    ${cancelBtnHtml}
                </div>
            </div>`;
    }).join('');

    renderPagination(page);
    initSwipers();
}

function renderPagination(currentPage) {
    const pagination = document.getElementById('order-pagination');
    if (!pagination) return;

    const totalPages = Math.ceil(allShipments.length / PAGE_SIZE) || 1;
    let startPage = Math.floor(currentPage / 5) * 5;
    let endPage = Math.max(startPage, Math.min(startPage + 4, totalPages - 1));

    let html = `<div class="admin-chat-pagination">`;

    // Prev
    if (currentPage === 0 || totalPages <= 1) {
        html += `<span class="admin-chat-page-button admin-chat-page-button--wide is-disabled">Prev</span>`;
    } else {
        html += `<a href="javascript:void(0)" class="admin-chat-page-button admin-chat-page-button--wide" onclick="renderPagedShipments(${currentPage - 1})">Prev</a>`;
    }

    // Numbers
    for (let i = startPage; i <= endPage; i++) {
        const activeClass = (i === currentPage) ? 'is-current' : '';
        html += `<a href="javascript:void(0)" class="admin-chat-page-button ${activeClass}" onclick="renderPagedShipments(${i})">${i + 1}</a>`;
    }

    // Next
    if (currentPage === totalPages - 1 || totalPages <= 1) {
        html += `<span class="admin-chat-page-button admin-chat-page-button--wide is-disabled">Next</span>`;
    } else {
        html += `<a href="javascript:void(0)" class="admin-chat-page-button admin-chat-page-button--wide" onclick="renderPagedShipments(${currentPage + 1})">Next</a>`;
    }

    html += `</div>`;
    pagination.innerHTML = html;
}

// ====== 주문 취소 =======
function cancelOrder(ordId) {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    showConfirmModal(
        "정말로 주문을 취소하시겠습니까?",

        function() {
            fetch(`/api/orders/${ordId}`, {
                method: 'DELETE',
                headers: { [csrfHeader]: csrfToken }
            })
            .then(response => {
                if (!response.ok) throw new Error('취소 실패');

                showGuideModal(
                    "주문이 성공적으로 취소되었습니다.",
                    function() {
                        location.reload();
                    },
                    "CANCELED",
                    "check_circle"
                );
            })
            .catch(error => {
                showGuideModal( "오류가 발생했습니다. 다시 시도해주세요.", null, "ERROR", "error" );
            });
        },
        "주문 취소", "warning"
    );
}

// ====== 배송 현황 =======
function openShipmentModal(shId) {
    const modalOverlay = document.querySelector('#orderTrackingModal');
    const modalBody = modalOverlay.querySelector('.modal-body');

    modalOverlay.style.display = 'flex';
    modalBody.innerHTML = '<div style="text-align:center; padding:30px;">배송 정보를 불러오는 중입니다...</div>';

    fetch(`/api/shipments/${shId}/track`)
        .then(response => {
            if (!response.ok) throw new Error('데이터 로드 실패');
            return response.json();
        })
        .then(data => {
            modalBody.innerHTML = loadShipmentDetail(data);
        })
        .catch(error => {
            modalBody.innerHTML = '<div style="text-align:center; padding:30px; color:red;">정보를 가져오지 못했습니다.</div>';
        });
}

function loadShipmentDetail(data) {
    if (!data) return '<div style="padding:20px; text-align:center;">데이터 없음</div>';

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

    // 1. 해외 통관 내역 포맷팅 적용
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

    // 2. 국내 배송 현황 포맷팅 적용
    if (hasDomestic) {
        html += '<h4 style="margin:15px 0 12px; font-size:16px;">국내 배송 현황</h4><ul style="padding-left:25px; border-left:2px solid #eee; list-style:none; margin:0;">';

        data.details.slice().reverse().forEach((item, idx) => {
            const isFirst = idx === 0;
            // T가 포함된 표준 형식이면 그대로 쓰고, 생숫자면 formatTrackingDate 사용
            const displayTime = item.time?.includes('T')
                                ? item.time.replace('T', ' ').substring(0, 16)
                                : formatTrackingDate(item.time);

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

    return html + '</div>';
}

// ====== 공통 영역 ======
function getStatusInfo(ship) {
    if (ship.shCanYn) return { statusText: '주문취소', badgeClass: 'admin-chat-badge--closed' };

    const stt = ship.shStt;
    if (stt === 'PREPARING') return { statusText: '배송준비', badgeClass: 'admin-chat-badge--open' };
    if (stt === 'DELIVERING' || stt === 'SHIPPING') return { statusText: '배송중', badgeClass: 'admin-chat-badge--ongoing' };
    if (stt === 'DELIVERED') return { statusText: '배송완료', badgeClass: 'admin-chat-badge--ongoing' };

    return { statusText: '주문취소', badgeClass: 'admin-chat-badge--closed' };
}

function formatTrackingDate(dateStr) {
    if (!dateStr || dateStr.length < 12) return dateStr || '-';

    const y = dateStr.substring(0, 4);
    const m = dateStr.substring(4, 6);
    const d = dateStr.substring(6, 8);
    const hh = dateStr.substring(8, 10);
    const mm = dateStr.substring(10, 12);

    return `${y}-${m}-${d} ${hh}:${mm}`;
}

function generateSwiperHtml(ship) {
    if (!ship.shipmentItems) return '';
    return ship.shipmentItems.map(item => `
        <div class="swiper-slide" style="display: flex; align-items: center; justify-content: center;">
            <img src="${item.ordItmThumbUrl}" alt="${item.ordItmNm}" class="order-thumb-img"
                 title="${item.ordItmNm}" style="max-width: 100%; max-height: 100%; object-fit: contain;">
        </div>
    `).join('');
}

function closeModal() {
    const modal = document.querySelector('#orderTrackingModal');
    if (modal) modal.style.display = 'none';
}

function initSwipers() {
    const swipers = document.querySelectorAll('.mySwiper');
    swipers.forEach((el) => {
        const slides = el.querySelectorAll('.swiper-slide').length;
        new Swiper(el, {
            loop: slides > 1,
            navigation: { nextEl: '.swiper-button-next', prevEl: '.swiper-button-prev' },
            watchOverflow: true,
        });
    });
}