document.addEventListener('DOMContentLoaded', function() {
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

    function resetForm() {
        document.getElementById('rcId').value = '';  // hidden value
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

    // [우편번호 찾기] 버튼
    const searchPostBtn = document.getElementById('searchPostCode');
    if (searchPostBtn) {
        searchPostBtn.addEventListener('click', execDaumPostcode);
    }

    // [전화번호 포매팅]
    const phonInput = document.getElementById('rcPhn');
    if (phonInput) {
        phonInput.addEventListener('input', function() {
            // 숫자만 입력 가능하도록 필터링
            let value = this.value.replace(/[^0-9]/g, '');

            // 최대 12자리까지만 허용 (4-4-4 형식)
            if (value.length > 12) {
                value = value.slice(0, 12);
            }

            // 포매팅: 4-4-4 형식
            let formatted = '';
            if (value.length <= 4) {
                formatted = value;
            } else if (value.length <= 8) {
                formatted = value.slice(0, 4) + '-' + value.slice(4);
            } else {
                formatted = value.slice(0, 4) + '-' + value.slice(4, 8) + '-' + value.slice(8, 12);
            }

            this.value = formatted;
        });
    }

    const saveBtn = document.getElementById('btnSaveReceiver');
    if (saveBtn) {
        saveBtn.addEventListener('click', saveReceiver);
    }

    document.addEventListener('click', function(e) {
        const btnDelete = e.target.closest('.btn-delete');
        const btnEdit = e.target.closest('.btn-edit');

        if (btnDelete) {
            const rcId = btnDelete.dataset.id;
            if (rcId && confirm('정말 삭제하시겠습니까?')) {
                deleteReceiver(rcId);
            }
        } else if (btnEdit) {
            const rcId = btnEdit.dataset.id;
            if (rcId) {
                loadReceiverDetail(rcId);
            }
        }
    });


    loadReceiverList();

    function loadReceiverList() {
        fetch(`/api/receiver`)
        .then(res => {
            if (!res.ok) throw new Error("데이터를 가져오지 못했습니다.");
            return res.json();
        })
        .then(data => {
            renderReceiverList(data);
        })
        .catch(err => console.error("전체 목록 조회 실패: " + err));
    }

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

    function saveReceiver() {
        const rcId = document.getElementById('rcId').value;
        const data = {
            rcNm: document.getElementById('rcNm').value,
            rcPhn: document.getElementById('rcPhn').value,
            rcAdr: document.getElementById('baseAddress').value,
            rcAdrDt: document.getElementById('detailAddress').value,
        };

        if (!data.rcNm || !data.rcAdr) {
            alert("이름과 주소는 필수 입력 사항입니다.");
            return;
        }

        const method = rcId ? 'PATCH' : 'POST';
        const url = rcId ? `/api/receiver/${rcId}` : '/api/receiver';

        fetch(url, getFetchOptions(method, data))
        .then(res => {
            if (!res.ok) throw new Error("저장에 실패했습니다.");
            return res.json();
        })
        .then(newList => {
            alert(rcId ? "수정되었습니다." : "등록되었습니다.");
            renderReceiverList(newList);
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
            renderReceiverList(newList);
            alert("삭제되었습니다.");
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
            html += `
                <div class="admin-chat-row" style="text-align: center; padding: 20px;">
                    등록된 배송지가 없습니다.
                </div>`;
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
                        <div class="admin-chat-badge ${badgeClass}">
                            ${badgeText}
                        </div>
                    </div>

                    <div class="action-btns">
                        <button type="button" class="btn-edit" data-id="${receiver.rcId}">수정하기</button>
                        <button type="button" class="btn-delete" data-id="${receiver.rcId}">삭제하기</button>
                    </div>
                </div>`;
            });
        }

        html += `
            </div>
        </section>`;

        listBody.innerHTML = html;
    }
});