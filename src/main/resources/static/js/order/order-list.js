document.addEventListener('DOMContentLoaded', () => {
    fetchOrders(0);
});

async function fetchOrders(page) {
    const listBody = document.getElementById('order-list-body');
    const pagination = document.getElementById('order-pagination');
    const activeFilter = document.querySelector('.admin-chat-filter.is-active');
    const status = activeFilter ? activeFilter.dataset.status : 'ALL';

    try {
        const response = await fetch(`/api/orders/list?page=${page}&status=${status}`);
        if (!response.ok) throw new Error("데이터 요청에 실패했습니다.");

        const data = await response.json();

        renderOrderList(data.content);
        renderPagination(data);
    } catch (error) {
        console.error("데이터 로드 실패:", error);
        if (listBody) {
            listBody.innerHTML = '<div style="text-align: center; padding: 20px;">데이터를 불러오는 중 오류가 발생했습니다.</div>';
        }
    }
}

document.querySelectorAll('.admin-chat-filter').forEach(button => {
    button.addEventListener('click', (e) => {
        e.preventDefault();

        document.querySelectorAll('.admin-chat-filter').forEach(btn => btn.classList.remove('is-active'));
        button.classList.add('is-active');

        fetchOrders(0);
    });
});

function renderOrderList(orders) {
    const listBody = document.getElementById('order-list-body');
    const activeFilter = document.querySelector('.admin-chat-filter.is-active');
    const status = activeFilter ? activeFilter.dataset.status : 'ALL';

    if (!listBody) return;

    if (!orders || orders.length === 0) {
        listBody.innerHTML = '<div style="text-align: center; padding: 20px;"><p>주문 이력이 없습니다</p></div>';
        return;
    }

    listBody.innerHTML = orders.map(order => {
        let statusText = '주문취소';
        let badgeClass = 'admin-chat-badge--closed';

        if (order.ordBaseStt === 'PREPARING') {
            statusText = '상품준비';
            badgeClass = 'admin-chat-badge--open';
        } else if (order.ordBaseStt === 'DELIVERING') {
            statusText = '배송중';
            badgeClass = 'admin-chat-badge--ongoing';
        } else if (order.ordBaseStt === 'DELIVERED') {
            statusText = '배송완료';
            badgeClass = 'admin-chat-badge--ongoing';
        }

        const formattedAmount = new Intl.NumberFormat().format(order.ordBaseTtAm || 0);
        const thumbImg = order.ordThumbUrl ?
        `<img src="${order.ordThumbUrl}" alt="상품 썸네일" class="order-thumb-img" style="border-radius: 5px; width: 70px; height: 70px; object-fit: cover;">`
            : `<div class="order-thumb-none"><i class="fas fa-box"></i></div>`;

        return `
            <div class="admin-chat-row">
                <div class="admin-chat-row__primary">
                    <div class="admin-chat-row__text">
                        <h2 style="margin-left: 15px">${order.ordBaseNo}</h2>
                    </div>
                </div>
                <div class="admin-chat-row__text" style="display: flex; align-items: center; gap: 10px; margin-left: 30px;">
                    ${thumbImg}
                    <p>${order.ordSummaryNm}</p>
                </div>
                <div class="admin-chat-row__amount">
                    <strong>${formattedAmount}원</strong>
                </div>
                <div class="admin-chat-row__status" style="text-align: center;">
                    <span class="admin-chat-badge ${badgeClass}">${statusText}</span>
                </div>
                <div class="action-btns">
                    <a class="btn-edit" href="/order/detail?id=${order.ordBaseId}">상세보기</a>
                </div>
            </div>
        `;
    }).join('');
}

function renderPagination(data) {
    const pagination = document.getElementById('order-pagination');
    if (!pagination) return;

    const currentPage = data.number;
    const totalPages = data.totalPages || 1;

    let startPage = Math.floor(currentPage / 5) * 5;
    let endPage = Math.max(startPage, Math.min(startPage + 4, totalPages - 1));

    let html = `<div class="admin-chat-pagination">`;

    // Prev
    if (data.first || totalPages <= 1) {
        html += `<span class="admin-chat-page-button admin-chat-page-button--wide is-disabled">Prev</span>`;
    } else {
        html += `<a href="javascript:void(0)" class="admin-chat-page-button admin-chat-page-button--wide" onclick="fetchOrders(${currentPage - 1})">Prev</a>`;
    }

    // Numbers (최소 1은 무조건 나옴)
    for (let i = startPage; i <= endPage; i++) {
        const activeClass = (i === currentPage) ? 'is-current' : '';
        html += `<a href="javascript:void(0)" class="admin-chat-page-button ${activeClass}" onclick="fetchOrders(${i})">${i + 1}</a>`;
    }

    // Next
    if (data.last || totalPages <= 1) {
        html += `<span class="admin-chat-page-button admin-chat-page-button--wide is-disabled">Next</span>`;
    } else {
        html += `<a href="javascript:void(0)" class="admin-chat-page-button admin-chat-page-button--wide" onclick="fetchOrders(${currentPage + 1})">Next</a>`;
    }

    html += `</div>`;
    pagination.innerHTML = html;
}