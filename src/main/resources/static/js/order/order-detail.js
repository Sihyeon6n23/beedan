document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('id');

    if (orderId) {
        fetchOrderDetail(orderId);
    } else {
        document.getElementById('order-detail-body').innerHTML = '<div style="text-align:center; padding:30px;">잘못된 접근입니다. 주문 ID가 없습니다.</div>';
    }
});

// 1. 주문 상세 데이터 가져오기
async function fetchOrderDetail(orderId) {
    try {
        const response = await fetch(`/api/orders/${orderId}`);
        if (!response.ok) throw new Error("상세 데이터를 불러오는데 실패했습니다.");
        
        const orderData = await response.json();
        renderOrderDetail(orderData);
        
        // 중요: DOM이 모두 그려진 후 Swiper 초기화
        initSwipers(); 
    } catch (error) {
        console.error(error);
        document.getElementById('order-detail-body').innerHTML = '<div style="text-align:center; padding:30px; color:red;">데이터를 가져오지 못했습니다.</div>';
    }
}

// 2. 화면 렌더링 로직
function renderOrderDetail(order) {
    const listBody = document.getElementById('order-detail-body');
    const pageInfo = document.getElementById('page-info');
    const pageNumbers = document.getElementById('page-numbers');

    const shipments = order.shipmentResponses || [];

    if (pageNumbers) {
        pageNumbers.innerHTML = shipments.length > 0 ?
            `<span class="admin-chat-page-button is-current">1</span>` : '';
    }


    if (!order.shipmentResponses || order.shipmentResponses.length === 0) {
        listBody.innerHTML = `
            <div class="admin-chat-row">
                <span style="display: block; width: 100%; text-align: center; padding: 20px;">배송지 정보가 없습니다.</span>
            </div>`;
        return;
    }

    const html = order.shipmentResponses.map(ship => {
        let statusText = '주문취소';
        let badgeClass = 'admin-chat-badge--closed';

        if (!ship.shCanYn) {
            if (ship.shStt === 'PREPARING') {
                statusText = '배송준비';
                badgeClass = 'admin-chat-badge--open';
            } else if (ship.shStt === 'DELIVERING') {
                statusText = '배송중';
                badgeClass = 'admin-chat-badge--ongoing';
            } else if (ship.shStt === 'SHIPPING') {
                statusText = '통관진행';
                badgeClass = 'admin-chat-badge--ongoing';
            } else if (ship.shStt === 'DELIVERED') {
                statusText = '배송완료';
                badgeClass = 'admin-chat-badge--ongoing';
            }
        }

        // Swiper 슬라이드 생성
        const swiperSlides = ship.shipmentItems.map(item => `
            <div class="swiper-slide" style="display: flex; align-items: center; justify-content: center;">
                <img src="${item.ordItmThumbUrl}" alt="${item.ordItmNm}" class="order-thumb-img" title="${item.ordItmNm}" style="max-width: 100%; max-height: 100%; object-fit: contain;">
            </div>
        `).join('');

        const itemTexts = ship.shipmentItems.map(item => `
            <p style="text-align: left;">${item.ordItmNm} (${item.shQn}개)</p>
        `).join('');

        // 버튼 비활성화 여부 및 취소 버튼 렌더링 처리
        const isDetailDisabled = order.ordBaseStt === 'CANCELED' ? 'disabled' : '';
        const cancelBtnHtml = order.ordBaseStt === 'PREPARING' 
            ? `<button class="btn-edit" onclick="cancelOrder(${order.ordBaseId})" type="button">주문 취소</button>` 
            : '';

        return `
            <div class="admin-chat-row">
                <div class="admin-chat-row__text">
                    <h2>${ship.shRcvNm}</h2>
                    <p>${ship.shAdr}</p>
                    <p>${ship.shAdrDt}</p>
                </div>

                <div class="swiper mySwiper">
                    <div class="swiper-wrapper">
                        ${swiperSlides}
                    </div>
                    <div class="swiper-button-next"></div>
                    <div class="swiper-button-prev"></div>
                </div>

                <div class="admin-chat-row__text">
                    ${itemTexts}
                </div>

                <div class="admin-chat-row__status">
                    <span class="admin-chat-badge ${badgeClass}">${statusText}</span>
                </div>

                <div class="action-btns">
                    <button type="button" class="btn-detail" ${isDetailDisabled} onclick="openShipmentModal(${ship.shId})">
                        배송 현황
                    </button>
                    ${cancelBtnHtml}
                </div>
            </div>
        `;
    }).join('');

    listBody.innerHTML = html;
}

function openShipmentModal(shId) {
    const modalOverlay = document.querySelector('#orderTrackingModal');
    const modalBody = modalOverlay.querySelector('.modal-body');

    modalOverlay.style.display = 'flex';
    modalBody.innerHTML = '<div style="text-align:center; padding:30px;">배송 정보를 불러오는 중입니다...</div>';

    fetch(`/api/shipments/${shId}/track`)
        .then(response => {
            if (!response.ok) throw new Error('네트워크 응답에 문제가 있습니다.');
            return response.json();
        })
        .then(data => {
            const html = loadShipmentDetail(data);
            modalBody.innerHTML = html;
        })
        .catch(error => {
            console.error('Error:', error);
            modalBody.innerHTML = '<div style="text-align:center; padding:30px; color:red;">배송 정보를 가져오는데 실패했습니다.</div>';
        });
}

function loadShipmentDetail(data) {
    if (!data) return '<div style="padding:20px; text-align:center;">데이터를 불러올 수 없습니다.</div>';

    const hasDomesticDetails = data.details && data.details.length > 0;
    const customsAccordionOpen = hasDomesticDetails ? '' : 'open';

    let html = `
        <div class="tracking-info-summary">
            <div class="info-item">
                <span class="label">택배사</span>
                <div class="value">${data.carrierName || '-'}</div>
            </div>
            <div class="info-item">
                <span class="label">수령인</span>
                <div class="value">${data.shRcvNm || '-'}</div>
            </div>
            <div class="info-item info-item--address">
                <span class="label">배송지</span>
                <div class="value" style="line-height: 1.4; word-break: keep-all;">${data.shAdr || '-'}</div>
            </div>
            <div class="info-item">
                <span class="label">송장번호</span>
                <div class="value">${data.trackingNumber || '정보 없음'}</div>
            </div>
            <div class="info-item info-item--status">
                <span class="label">현재 상태</span>
                <div class="value">
                    <span style="font-size: 14px; font-weight: bold;">${data.statusText || '-'}</span>
                </div>
            </div>
        </div>
        <div class="tracking-timeline" style="margin-top: 20px;">
    `;

    if (data.customsDetails && data.customsDetails.length > 0) {
        html += `
            <div style="margin-bottom: 20px;">
                <details style="background: #f8f9fa; border: 1px solid #e9ecef; overflow: hidden;" ${customsAccordionOpen}>
                    <summary style="padding: 12px 15px; cursor: pointer; font-weight: bold; outline: none; display: flex; justify-content: space-between; align-items: center;">
                        <span>해외 통관 상세 내역 (${data.customsDetails.length}건)</span>
                        <span style="font-size: 12px;">▼</span>
                    </summary>
                    <div style="padding: 0 15px 15px 15px; border-top: 1px dashed #dee2e6; margin-top: 5px;">
        `;
        data.customsDetails.forEach((c, index) => {
            const isLast = index === data.customsDetails.length - 1;
            html += `
                <div style="padding: 10px 0; ${isLast ? '' : 'border-bottom: 1px solid #f1f3f5;'}">
                    <div style="font-size: 11px; color: #adb5bd;">${c.time}</div>
                    <div style="font-size: 13px; font-weight: 600; color: #343a40;">${c.status}</div>
                    <div style="font-size: 12px; color: #6c757d;">${c.description}</div>
                </div>
            `;
        });
        html += `</div></details></div>`;
    }

    if (hasDomesticDetails) {
        html += '<h4 style="font-size: 15px; margin-bottom: 15px; color: #333; padding-left: 5px;">국내 배송 현황</h4>';
        html += '<ul class="tracking-timeline-list" style="padding-left: 20px; list-style: none; margin: 0;">';
        
        const reversedDetails = data.details.slice().reverse();
        reversedDetails.forEach((item, index) => {
            const isFirst = index === 0;
            const timeStr = item.time ? item.time.replace('T', ' ').substring(0, 16) : '';
            html += `
                <li style="margin-bottom: 20px; position: relative; padding-left: 20px; border-left: 2px solid #eee;">
                    <div style="position: absolute; left: -7px; top: 0; width: 12px; height: 12px; background: ${isFirst ? '#d9534f' : '#ccc'}; border-radius: 50%; border: 2px solid #fff; box-shadow: 0 0 0 1px ${isFirst ? '#d9534f' : '#ccc'};"></div>
                    <div style="font-size: 12px; color: #999;">${timeStr}</div>
                    <div style="margin-top: 4px;">
                        <strong style="font-size: 14px; color: ${isFirst ? '#333' : '#666'};">${item.status}</strong>
                        <p style="font-size: 13px; color: #777; margin: 2px 0 0 0;">${item.description || ''}</p>
                    </div>
                </li>`;
        });
        html += '</ul>';
    } else {
         html += `
            <div style="text-align: center; padding: 40px; color: #888; background: #fafafa; border-radius: 8px;">
                <p style="margin: 0;">아직 배송 정보가 등록되지 않았습니다.</p>
            </div>
         `;
    }
    html += '</div>';
    return html;
}

function cancelOrder(ordId) {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    if (!confirm("정말로 주문을 취소하시겠습니까?")) return;

    fetch(`/api/orders/${ordId}`, {
        method: 'DELETE',
        headers: {
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
        if (!response.ok) throw new Error('주문 취소에 실패했습니다.');
        alert("주문이 취소되었습니다.");
        location.reload();
    })
    .catch(error => {
        console.error('Error:', error);
        alert("주문 취소 중 오류가 발생했습니다. 다시 시도해주세요.");
    });
}

function closeModal() {
    const modalOverlay = document.querySelector('#orderTrackingModal');
    if(modalOverlay) {
        modalOverlay.style.display = 'none';
    }
}

function initSwipers() {
    const swipers = document.querySelectorAll('.mySwiper');
    swipers.forEach((swiperElement) => {
        const slideCount = swiperElement.querySelectorAll('.swiper-slide').length;
        new Swiper(swiperElement, {
            loop: slideCount > 1,
            navigation: {
                nextEl: swiperElement.querySelector('.swiper-button-next'),
                prevEl: swiperElement.querySelector('.swiper-button-prev'),
            },
            pagination: {
                el: swiperElement.querySelector('.swiper-pagination'),
                clickable: true,
            },
            watchOverflow: true,
        });
    });
}