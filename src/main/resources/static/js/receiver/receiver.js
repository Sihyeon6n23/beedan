document.addEventListener('DOMContentLoaded', function() {
    // --- 1. 보안 및 UI 요소 설정 ---
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    const formSection = document.getElementById('receiverFormSection');
    const formTitle = document.getElementById('formTitle');
    const btnOpenAddForm = document.getElementById('btnOpenAddForm');

    // --- 2. 유틸리티 함수 (공통 로직) ---
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

    // --- 3. 이벤트 리스너 등록 ---

    // [추가] 버튼 클릭 시 폼 열기
    if (btnOpenAddForm) {
        btnOpenAddForm.addEventListener('click', function() {
            resetForm();
            if(formTitle) formTitle.innerText = "새 배송지 등록";
            formSection.style.display = 'block';
            this.style.display = 'none';
        });
    }

    // [취소] 버튼 클릭 시 폼 닫기
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

    // [저장하기] 버튼
    const saveBtn = document.getElementById('btnSaveReceiver');
    if (saveBtn) {
        saveBtn.addEventListener('click', saveReceiver);
    }

    // [목록 내 수정/삭제] - 이벤트 위임 (가장 중요: 클래스명 매칭)
    document.addEventListener('click', function(e) {
        // .btn-delete 또는 .btn-edit 클래스를 가진 가장 가까운 요소를 찾음
        const btnDelete = e.target.closest('.btn-delete');
        const btnEdit = e.target.closest('.btn-edit');

        if (btnDelete) {
            const rcId = btnDelete.getAttribute('data-id');
            if (rcId && confirm('정말 삭제하시겠습니까?')) {
                deleteReceiver(rcId);
            }
        } else if (btnEdit) {
            const rcId = btnEdit.getAttribute('data-id');
            if (rcId) {
                loadReceiverDetail(rcId);
            }
        }
    });

    // --- 4. 핵심 비동기 함수들 ---

    // [초기 로드] 페이지 진입 시 목록 가져오기
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

    // [조회] 수정 버튼 클릭 시 단건 데이터 로드 및 폼 열기
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

            // 폼 위치로 부드럽게 이동
            window.scrollTo({ top: formSection.offsetTop - 50, behavior: 'smooth' });
        })
        .catch(err => alert(err.message));
    }

    // [저장] 등록(POST) 또는 수정(PATCH)
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

    // [삭제] DELETE 요청
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

    // [렌더링] HTML 목록 생성 및 주입
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