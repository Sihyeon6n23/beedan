document.addEventListener('DOMContentLoaded', function() {
    let allReceivers = [];
    let currentPage = 0;
    const itemsPerPage = 4;

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    const formSection = document.getElementById('receiverFormSection');
    const formTitle = document.getElementById('formTitle');
    const btnOpenAddForm = document.getElementById('btnOpenAddForm');

    const getFetchOptions = (method, body = null) => {
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        };
        if (body) options.body = JSON.stringify(body);
        return options;
    };

    function displayPage(page) {
        currentPage = page;
        const totalPages = Math.ceil(allReceivers.length / itemsPerPage);

        const startIndex = page * itemsPerPage;
        const endIndex = startIndex + itemsPerPage;
        const paginatedList = allReceivers.slice(startIndex, endIndex);

        renderReceiverList(paginatedList);

        renderPagination(totalPages, page);
    }

    function renderPagination(totalPages, activePage) {
        const paginationEl = document.getElementById('order-pagination');
        if (!paginationEl) return;

        let html = '';

        // 데이터가 없어도 최소 1페이지는 보여주거나, 아예 안 보여주려면 totalPages === 0 체크
        const displayTotalPages = Math.max(1, totalPages);

        // [Prev] 버튼
        if (activePage === 0 || displayTotalPages <= 1) {
            html += `<span class="admin-chat-page-button admin-chat-page-button--wide is-disabled">Prev</span>`;
        } else {
            html += `<a href="javascript:void(0)" class="admin-chat-page-button admin-chat-page-button--wide" onclick="displayPage(${activePage - 1})">Prev</a>`;
        }

        // [Numbers] (1부터 시작하는 페이지 번호)
        for (let i = 0; i < displayTotalPages; i++) {
            const activeClass = (i === activePage) ? 'is-current' : '';
            html += `<a href="javascript:void(0)" class="admin-chat-page-button ${activeClass}" onclick="displayPage(${i})">${i + 1}</a>`;
        }

        // [Next] 버튼
        if (activePage === displayTotalPages - 1 || displayTotalPages <= 1) {
            html += `<span class="admin-chat-page-button admin-chat-page-button--wide is-disabled">Next</span>`;
        } else {
            html += `<a href="javascript:void(0)" class="admin-chat-page-button admin-chat-page-button--wide" onclick="displayPage(${activePage + 1})">Next</a>`;
        }

        paginationEl.innerHTML = html;
    }

    window.displayPage = displayPage;

    function loadReceiverList() {
        fetch(`/api/receiver`)
        .then(res => {
            if (!res.ok) throw new Error("데이터를 가져오지 못했습니다.");
            return res.json();
        })
        .then(data => {
            allReceivers = data; // 전체 데이터를 전역 변수에 저장
            displayPage(0);      // 첫 페이지 표시
        })
        .catch(err => console.error("전체 목록 조회 실패: " + err));
    }

    function saveReceiver() {
    const rcId = document.getElementById('rcId').value;
    const rcNm = document.getElementById('rcNm').value.trim();
    const rcPhn = document.getElementById('rcPhn').value.trim();
    const rcAdr = document.getElementById('baseAddress').value.trim();
    const rcAdrDt = document.getElementById('detailAddress').value.trim();

    if (!rcNm || !rcAdr) {
        alert("이름과 주소는 필수 입력 사항입니다.");
        return;
    }

    const isDuplicate = allReceivers.some(receiver => {
        if (rcId && String(receiver.rcId) === String(rcId)) return false;

        return (
            receiver.rcNm === rcNm &&
            receiver.rcPhn === rcPhn &&
            receiver.rcAdr === rcAdr &&
            (receiver.rcAdrDt || '') === rcAdrDt
        );
    });

    if (isDuplicate) {
        alert("이미 동일한 정보로 등록된 배송지가 존재합니다.");
        return;
    }

    const data = { rcNm, rcPhn, rcAdr, rcAdrDt };
    const method = rcId ? 'PATCH' : 'POST';
    const url = rcId ? `/api/receiver/${rcId}` : '/api/receiver';

        fetch(url, getFetchOptions(method, data))
        .then(res => {
            if (!res.ok) throw new Error("저장에 실패했습니다.");
            return res.json();
        })
        .then(newList => {
            alert(rcId ? "수정되었습니다." : "등록되었습니다.");
            allReceivers = newList; // 최신 목록으로 갱신
            displayPage(0);        // 첫 페이지로 리렌더링
            closeForm();
        })
        .catch(err => alert(err.message));
    }

    function deleteReceiver(rcId) {
        fetch(`/api/receiver/${rcId}`, getFetchOptions('DELETE'))
        .then(res => {
            if (!res.ok) throw new Error("삭제에 실패했습니다.");
            return res.json();
        })
        .then(newList => {
            allReceivers = newList;
            const maxPage = Math.ceil(allReceivers.length / itemsPerPage) - 1;
            const targetPage = currentPage > maxPage ? Math.max(0, maxPage) : currentPage;
            displayPage(targetPage);
            alert("삭제되었습니다.");
        })
        .catch(err => alert(err.message));
    }

    function resetForm() {
        document.getElementById('rcId').value = '';
        document.getElementById('rcNm').value = '';
        document.getElementById('rcPhn').value = '';
        document.getElementById('postcode').value = '';
        document.getElementById('baseAddress').value = '';
        document.getElementById('detailAddress').value = '';
    }

    function closeForm() {
        resetForm();
        formSection.style.display = 'none';
        if (btnOpenAddForm) btnOpenAddForm.style.display = 'block';
    }

    if (btnOpenAddForm) {
        btnOpenAddForm.addEventListener('click', function() {
            resetForm();
            if(formTitle) formTitle.innerText = "새 배송지 등록";
            formSection.style.display = 'block';
            this.style.display = 'none';
        });
    }

    const cancelBtns = ['btnCancel', 'btnReset'];
    cancelBtns.forEach(id => {
        const btn = document.getElementById(id);
        if (btn) btn.addEventListener('click', closeForm);
    });

    const searchPostBtn = document.getElementById('searchPostCode');
    if (searchPostBtn) {
        searchPostBtn.addEventListener('click', execDaumPostcode);
    }

    const phonInput = document.getElementById('rcPhn');
    if (phonInput) {
        phonInput.addEventListener('input', function() {
            let value = this.value.replace(/[^0-9]/g, '');

            if (value.length > 11) {
                value = value.slice(0, 11);
            }

            let formatted = '';
            if (value.length <= 3) {
                formatted = value;
            } else if (value.length <= 7) {
                formatted = value.slice(0, 3) + '-' + value.slice(3);
            } else {
                formatted = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7);
            }

            this.value = formatted;
        });
    }

    const saveBtn = document.getElementById('btnSaveReceiver');
    if (saveBtn) saveBtn.addEventListener('click', saveReceiver);

    document.addEventListener('click', function(e) {
        const btnDelete = e.target.closest('.btn-delete');
        const btnEdit = e.target.closest('.btn-edit');
        if (btnDelete) {
            const rcId = btnDelete.dataset.id;
            if (rcId && confirm('정말 삭제하시겠습니까?')) deleteReceiver(rcId);
        } else if (btnEdit) {
            const rcId = btnEdit.dataset.id;
            if (rcId) loadReceiverDetail(rcId);
        }
    });

    function loadReceiverDetail(rcId) {
        fetch(`/api/receiver/${rcId}`)
        .then(res => {
            if (!res.ok) throw new Error("데이터를 가져오지 못했습니다.");
            return res.json();
        })
        .then(data => {
            document.getElementById('rcId').value = data.rcId;
            document.getElementById('rcNm').value = data.rcNm;
            document.getElementById('rcPhn').value = data.rcPhn;
            document.getElementById('baseAddress').value = data.rcAdr;
            document.getElementById('detailAddress').value = data.rcAdrDt || '';
            if(formTitle) formTitle.innerText = "배송지 수정";
            formSection.style.display = 'block';
            if (btnOpenAddForm) btnOpenAddForm.style.display = 'none';
            window.scrollTo({ top: formSection.offsetTop - 50, behavior: 'smooth' });
        })
        .catch(err => alert(err.message));
    }

    function renderReceiverList(list) {
        const listBody = document.getElementById('receiver-body');
        if (!listBody) return;

        let html = `
        <section class="admin-chat-shell admin-chat-shell--list">
            <div class="admin-chat-list-body">`;

        if (list.length === 0) {
            html += `<div class="admin-chat-row" style="text-align: center; padding: 20px;">등록된 배송지가 없습니다.</div>`;
        } else {
            list.forEach(receiver => {
                const isIsland = receiver.rcIamYn === true;
                const badgeClass = isIsland ? 'admin-chat-badge--open' : 'admin-chat-badge--closed';
                const badgeText = isIsland ? '도서산간' : '일반지역';

                html += `
                <div class="admin-chat-row">
                    <div class="admin-chat-row__primary">
                        <div class="admin-chat-row__text">
                            <h2>${receiver.rcNm}</h2>
                            <p>${receiver.rcPhn}</p>
                        </div>
                    </div>
                    <div class="admin-chat-row__address">
                        <div class="admin-chat-row__text">
                            <p>${receiver.rcAdr}</p>
                            <p>${receiver.rcAdrDt || ''}</p>
                        </div>
                    </div>
                    <div class="admin-chat-row__status">
                        <div class="admin-chat-badge ${badgeClass}">${badgeText}</div>
                    </div>
                    <div class="action-btns">
                        <button type="button" class="btn-edit" data-id="${receiver.rcId}">수정하기</button>
                        <button type="button" class="btn-delete" data-id="${receiver.rcId}">삭제하기</button>
                    </div>
                </div>`;
            });
        }
        html += `</div></section>`;
        listBody.innerHTML = html;
    }

    // 최초 데이터 로드
    loadReceiverList();
});