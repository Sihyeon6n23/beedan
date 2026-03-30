const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

window.openModal = function() {
    document.getElementById('addReceiverModal').style.display = 'flex';
};

window.closeModal = function() {
    document.getElementById('addReceiverModal').style.display = 'none';
};

window.addReceiver = function() {
    const rcNm = document.getElementById('rcNm').value;
    const rcPhn = document.getElementById('rcPhn').value;
    const rcAdr = document.getElementById('baseAddress').value;
    const rcAdrDt = document.getElementById('detailAddress').value;
    const rcMsg = document.getElementById('rcMsg').value;

    const receiverDto = {
        rcNm: rcNm,
        rcPhn: rcPhn,
        rcAdr: rcAdr,
        rcAdrDt: rcAdrDt,
        rcMsg: rcMsg
    };

    fetch('/api/receiver', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(receiverDto)
    })
    .then(response => {
        if (!response.ok) throw new Error('배송지 추가 실패');
        return response.json();
    })
    .then(data => {
        closeModal();
        getReceiverList(); // 리스트 새로고침
    })
    .catch(e => console.log(e));
};

window.getReceiverList = function(){
    fetch('/api/receiver')
     .then(response => {
        if(!response.ok) throw new Error('응답 요청에 실패했습니다');
     return response.json()
     })
     .then(data => {
        const dataListContainer = document.querySelector('.data-list-container');
        dataListContainer.innerHTML = '';

        data.forEach(receiver => {
            const li = document.createElement('li');
            li.className = 'data-list-content';
            li.innerHTML = `
                <div class="flex-1 min-w-0">
                    <h3 class="text-base font-semibold text-[var(--pub-text-main)]">${receiver.rcNm}</h3>
                    <p class="text-sm text-[var(--pub-text-sub)]">전화번호: ${receiver.rcPhn}</p>
                    <p class="text-sm text-[var(--pub-text-sub)]">주소: ${receiver.rcAdr} ${receiver.rcAdrDt || ''}</p>
                    <p class="text-sm text-[var(--pub-text-sub)]">메시지: ${receiver.rcMsg || ''}</p>
                </div>
                <button type="button" class="close-btn ml-6 p-2 opacity-30 group-hover:opacity-100 hover:text-red-500 transition-all" onclick="deleteReceiver(${receiver.rcId})">
                    <span class="material-symbols-outlined text-[20px]">close</span>
                </button>
            `;
            dataListContainer.appendChild(li);
        });
     })
     .catch(e => console.log(e));
};

window.deleteReceiver = function(rcId) {
    fetch(`/api/receiver/${rcId}`, {
        method: 'DELETE',
        headers: {
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
        if (!response.ok) throw new Error('삭제 실패');
        getReceiverList();
    })
    .catch(e => console.log(e));
};

document.addEventListener('DOMContentLoaded', function() {
    getReceiverList();
});
