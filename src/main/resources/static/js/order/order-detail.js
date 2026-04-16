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

    const hasDomesticDetails = data.details && data.details.length > 0;    // 국내 배송 데이터 존재 여부 확인
    const customsAccordionOpen = hasDomesticDetails ? '' : 'open';  // 국내 배송이 없으면 통관 내역을 열어둠(open), 있으면 닫아둠

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

    // 1. 해외 통관 내역
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

    // 2. 국내 배송 내역
    if (hasDomesticDetails) {
        html += '<h4 style="font-size: 15px; margin-bottom: 15px; color: #333; padding-left: 5px;">국내 배송 현황</h4>';
        html += '<ul class="tracking-timeline-list" style="padding-left: 20px; list-style: none; margin: 0;">';

        const reversedDetails = data.details.slice().reverse();

        reversedDetails.forEach((item, index) => {
            const isFirst = index === 0;
            const timeStr = item.time ? item.time.replace('T', ' ').substring(0, 16) : '';

            html += `
                <li style="margin-bottom: 20px; position: relative; padding-left: 20px; border-left: 2px solid #eee;">
                    <div style="position: absolute; left: -7px; top: 0; width: 12px; height: 12px;
                                background: ${isFirst ? '#d9534f' : '#ccc'}; border-radius: 50%;
                                border: 2px solid #fff; box-shadow: 0 0 0 1px ${isFirst ? '#d9534f' : '#ccc'};"></div>
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

// Swiper 초기화 함수
function initSwipers() {
    const swipers = document.querySelectorAll('.mySwiper');
    swipers.forEach((swiperElement, index) => {
        new Swiper(swiperElement, {
            loop: true,
            navigation: {
                nextEl: swiperElement.querySelector('.swiper-button-next'),
                prevEl: swiperElement.querySelector('.swiper-button-prev'),
            },
            pagination: {
                el: swiperElement.querySelector('.swiper-pagination'),
                clickable: true,
            },
        });
    });
}

// 페이지 로드 시 Swiper 초기화
document.addEventListener('DOMContentLoaded', function() {
    initSwipers();
});