document.addEventListener('DOMContentLoaded', function () {

    // ── 유틸 ──────────────────────────────────────────
    function getActiveRate() {
        var slide = document.querySelector('.er-slide--active');
        if (!slide) return 0;
        var numEl = slide.querySelector('.er-rate-number');
        if (!numEl) return 0;
        return parseFloat(numEl.textContent.replace(/,/g, '')) || 0;
    }

    function formatNumber(n) {
        return Math.round(n).toLocaleString('ko-KR');
    }

    var csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    // ── 테이블 행 재계산 ──────────────────────────────
    function recalcRow(row) {
        var input = row.querySelector('.qty-input');
        var qty = parseInt(input.value) || 1;
        if (qty < 1) { qty = 1; input.value = 1; }

        var stPr = parseFloat(row.dataset.stPr) || 0;
        var stCur = row.dataset.stCur || '¥';

        // 규격 (셀렉트에서 단위 수량 가져오기)
        var specSelect = row.querySelector('.spec-select');
        var unGQn = specSelect ? parseInt(specSelect.value) || 1 : 1;
        var dozenCount = Math.ceil(qty / unGQn);
        row.querySelector('.spec-value').textContent = dozenCount;

        // 통화 기호 매핑
        var curSymbol = { '¥': '¥', '$': '$', '€': '€', 'JPY': '¥', 'USD': '$', 'EUR': '€', 'CNY': 'CNY' }[stCur] || stCur;

        // 공급가 총액
        var totalPr = stPr * qty;
        row.querySelector('.total-pr-value').textContent = curSymbol + formatNumber(totalPr);

        // 환율 기반 계산
        var rate = getActiveRate();
        var krwTotal = totalPr * rate;

        // 한화 기준 공급가 총액
        var krwTotalEl = row.querySelector('.subtotal-krw-total');
        krwTotalEl.textContent = rate > 0 ? '₩' + formatNumber(krwTotal) : '-';

        // 세액 계산 (한화 공급가 총액 × 관세율)
        var hsDuRa = parseFloat(row.dataset.hsDuRa) || 0;
        var taxAmountEl = row.querySelector('.tax-amount');
        if (rate > 0 && hsDuRa > 0) {
            var dutyAmount = krwTotal * hsDuRa;
            taxAmountEl.textContent = '₩' + formatNumber(dutyAmount);
        } else {
            taxAmountEl.textContent = '-';
        }

        // subtotal (한화) — 자동 계산, 사용자가 직접 수정하지 않은 경우만
        var subtotalInput = row.querySelector('.subtotal-input');
        if (!subtotalInput.dataset.userEdited) {
            if (rate > 0) {
                var subtotalKrw = totalPr * rate;
                subtotalInput.value = formatNumber(subtotalKrw);
                subtotalInput.placeholder = '자동 계산';
            } else {
                subtotalInput.value = '';
                subtotalInput.placeholder = '환율 없음';
            }
        }
    }

    // ── 행 이벤트 바인딩 ──────────────────────────────
    var rows = document.querySelectorAll('.quote-row');
    rows.forEach(function (row) {
        recalcRow(row);

        var input = row.querySelector('.qty-input');
        input.addEventListener('input', function () { recalcRow(row); });

        row.querySelector('.qty-down').addEventListener('click', function () {
            var v = parseInt(input.value) || 1;
            if (v > 1) { input.value = v - 1; recalcRow(row); }
        });
        row.querySelector('.qty-up').addEventListener('click', function () {
            var v = parseInt(input.value) || 1;
            input.value = v + 1;
            recalcRow(row);
        });

        // 규격 셀렉트 변경
        var specSelect = row.querySelector('.spec-select');
        if (specSelect) {
            specSelect.addEventListener('change', function () { recalcRow(row); });
        }

        var subtotalInput = row.querySelector('.subtotal-input');
        subtotalInput.addEventListener('input', function () {
            subtotalInput.dataset.userEdited = 'true';
        });
        subtotalInput.addEventListener('blur', function () {
            if (subtotalInput.value.trim() === '') {
                delete subtotalInput.dataset.userEdited;
                recalcRow(row);
            }
        });
    });

    // ── 환율 셀렉트 연동 ─────────────────────────────
    var erSelect = document.getElementById('erCurrencySelect');
    if (erSelect) {
        erSelect.addEventListener('change', function () {
            setTimeout(function () {
                rows.forEach(function (row) {
                    var subtotalInput = row.querySelector('.subtotal-input');
                    if (!subtotalInput.dataset.userEdited) recalcRow(row);
                });
            }, 50);
        });
    }

    // ── 예상 운임 비용 조회 ─────────────────────────────
    var feeDebounceTimer = null;

    function fetchEstimateFees() {
        clearTimeout(feeDebounceTimer);
        feeDebounceTimer = setTimeout(doFetchEstimateFees, 400);
    }

    function doFetchEstimateFees() {
        var rate = getActiveRate();
        var stIds = [];
        var totalDozen = 0;
        var itemTotalForeign = 0;
        var dutyRateSum = 0;
        var dutyRateCount = 0;

        rows.forEach(function (row) {
            var stId = row.dataset.stId;
            if (stId) stIds.push(parseInt(stId));

            var qty = parseInt(row.querySelector('.qty-input').value) || 1;
            var specSelect = row.querySelector('.spec-select');
            var unGQn = specSelect ? parseInt(specSelect.value) || 1 : 1;
            totalDozen += Math.ceil(qty / unGQn);

            var stPr = parseFloat(row.dataset.stPr) || 0;
            itemTotalForeign += stPr * qty;

            var hsDuRa = parseFloat(row.dataset.hsDuRa) || 0;
            if (hsDuRa > 0) { dutyRateSum += hsDuRa; dutyRateCount++; }
        });

        var itemTotalKrw = rate > 0 ? Math.round(itemTotalForeign * rate) : 0;
        var avgDutyRate = dutyRateCount > 0 ? dutyRateSum / dutyRateCount : 0;

        // 배송지 정보
        var firstShipBtn = document.querySelector('.shipping-btn');
        var region = firstShipBtn ? firstShipBtn.dataset.region : null;
        var shipMeta = firstShipBtn ? firstShipBtn.querySelector('.shipping-meta') : null;
        var shipCount = 1;
        if (shipMeta) {
            var match = shipMeta.textContent.match(/(\d+)건/);
            if (match) shipCount = parseInt(match[1]);
        }

        var headers = { 'Content-Type': 'application/json' };
        if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

        fetch('/api/quote/estimate-fees', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({
                stIds: stIds,
                totalDozen: totalDozen,
                region: region,
                shipmentCount: shipCount,
                itemTotalKrw: itemTotalKrw,
                avgDutyRate: avgDutyRate
            })
        })
        .then(function (res) { return res.json(); })
        .then(function (data) {
            var el = function (id) { return document.getElementById(id); };
            var fmt = function (v) { return v ? '₩' + formatNumber(v) : '-'; };

            // 카드 요약
            el('fc-shipping').textContent = fmt(data.shippingFee);
            el('fc-port-customs').textContent = fmt(
                (data.portFee || 0) + (data.customsFee || 0) + (data.hsCodeFee || 0));
            el('fc-insurance').textContent = fmt(data.insuranceFee);
            el('fc-duty-vat').textContent = fmt(
                (data.dutyAmount || 0) + (data.vatAmount || 0));
            el('fc-logistics-total').textContent = fmt(data.logisticsTotal);
            el('fc-service').textContent = fmt(data.serviceFee);
            el('fc-doc').textContent = fmt(data.docFee);
            el('fc-procurement-total').textContent = fmt(data.procurementTotal);

            // 운임 모달 상세
            var transportNames = { SEA: '해상 운송 (FCL)', AIR: '항공 운송', EXPRESS: '특급 배송' };
            var sizeNames = { SMALL: '소형', MEDIUM: '중형', LARGE: '대형' };
            var origin = data.factoryCity || data.countryCode || '-';
            var shipBtn = document.querySelector('.shipping-btn');
            var dest = shipBtn ? shipBtn.querySelector('.shipping-addr').textContent : '수령지';

            el('md-route').textContent = origin + ' → ' + dest;
            el('md-transport').textContent = transportNames[data.transportType] || data.transportType || '-';
            el('md-basis').textContent = data.countryCode ? 'CIF 부산항' : '-';
            el('md-size-type').textContent = sizeNames[data.sizeType] || data.sizeType || '-';
            el('md-shipping-fee').textContent = fmt(data.shippingFee);
            el('md-port-fee').textContent = fmt(data.portFee);
            el('md-customs-fee').textContent = fmt(data.customsFee);
            el('md-hscode-fee').textContent = fmt(data.hsCodeFee);
            el('md-port-customs-total').textContent = fmt(
                (data.portFee || 0) + (data.customsFee || 0) + (data.hsCodeFee || 0));
            el('md-insurance-fee').textContent = fmt(data.insuranceFee);
            el('md-cif').textContent = fmt(data.cifAmount);
            el('md-duty-rate').textContent = data.dutyRate
                ? (data.dutyRate * 100).toFixed(1) + '%' : '-';
            el('md-duty').textContent = fmt(data.dutyAmount);
            el('md-vat').textContent = fmt(data.vatAmount);
            el('md-logistics-total').textContent = fmt(data.logisticsTotal);

            // 대행 수수료 모달 상세
            el('md-buyer-grade').textContent = data.buyerGrade || '-';
            el('md-item-total-krw').textContent = '₩' + formatNumber(itemTotalKrw);
            el('md-service-fee').textContent = fmt(data.serviceFee);
            el('md-doc-fee').textContent = fmt(data.docFee);
            el('md-procurement-total').textContent = fmt(data.procurementTotal);

            // 총 주문 명세
            var totalTax = (data.dutyAmount || 0) + (data.vatAmount || 0);
            var logisticsVal = data.logisticsTotal || 0;
            var procurementVal = data.procurementTotal || 0;
            // 최종 합계 = 물품대금 + 운임(세금 포함) + 대행수수료
            var grandTotal = itemTotalKrw + logisticsVal + procurementVal;

            el('os-item-total').textContent = '₩' + formatNumber(itemTotalKrw);
            el('os-logistics').textContent = fmt(data.logisticsTotal);
            el('os-procurement').textContent = fmt(data.procurementTotal);
            el('os-tax').textContent = totalTax > 0 ? '₩' + formatNumber(totalTax) : '-';
            el('os-grand-total').textContent = grandTotal > 0 ? '₩' + formatNumber(grandTotal) : '-';
        })
        .catch(function () {});
    }

    // 수량/규격 변경 시 운임도 재조회
    rows.forEach(function (row) {
        var input = row.querySelector('.qty-input');
        input.addEventListener('input', fetchEstimateFees);
        row.querySelector('.qty-down').addEventListener('click', fetchEstimateFees);
        row.querySelector('.qty-up').addEventListener('click', fetchEstimateFees);
        var specSelect = row.querySelector('.spec-select');
        if (specSelect) specSelect.addEventListener('change', fetchEstimateFees);
    });

    // 환율 변경 시에도 재조회
    if (erSelect) {
        erSelect.addEventListener('change', function () {
            setTimeout(fetchEstimateFees, 100);
        });
    }

    // 초기 로딩 시 조회
    fetchEstimateFees();

    // ── 임시저장 ─────────────────────────────────────
    var saveDraftBtn = document.getElementById('btnSaveDraft');
    if (saveDraftBtn) {
        saveDraftBtn.addEventListener('click', function () {
            var quId = parseInt(saveDraftBtn.dataset.quId);
            if (!quId) { alert('견적 ID가 없습니다.'); return; }

            var items = [];
            rows.forEach(function (row) {
                var stId = parseInt(row.dataset.stId);
                var qty = parseInt(row.querySelector('.qty-input').value) || 1;
                var specSelect = row.querySelector('.spec-select');
                var selectedOption = specSelect ? specSelect.options[specSelect.selectedIndex] : null;

                items.push({
                    stId: stId,
                    qty: qty,
                    unGId: selectedOption ? parseInt(selectedOption.dataset.ugId) || null : null,
                    unGNm: specSelect ? specSelect.options[specSelect.selectedIndex].text : null,
                    unGQn: specSelect ? parseInt(specSelect.value) || 1 : 1
                });
            });

            var memo = document.getElementById('quoteMemo')
                ? document.getElementById('quoteMemo').value : '';

            var headers = { 'Content-Type': 'application/json' };
            if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

            saveDraftBtn.disabled = true;
            saveDraftBtn.textContent = '저장 중...';

            fetch('/api/quote/draft', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ quId: quId, items: items, memo: memo })
            })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data.status === 'ok') {
                    alert('임시저장 완료');
                } else {
                    alert('저장 실패: ' + (data.message || ''));
                }
            })
            .catch(function (e) {
                alert('저장 실패: ' + e.message);
            })
            .finally(function () {
                saveDraftBtn.disabled = false;
                saveDraftBtn.innerHTML =
                    '<svg width="10" height="10" viewBox="0 0 10 10" fill="none" xmlns="http://www.w3.org/2000/svg">' +
                    '<path d="M1 1h6l2 2v6H1V1zm2 0v3h4V1M3 6h4" stroke="#1a1c1c" stroke-width="1.2" stroke-linejoin="round"/>' +
                    '</svg> 임시저장';
            });
        });
    }

    // ── 배송지 모달 ──────────────────────────────────
    var currentShippingBtn = null;
    var currentShipType = 'single';
    var shipCards = [];
    var totalItemQty = 0;

    window.openShippingModal = function (btn) {
        currentShippingBtn = btn;
        var overlay = document.getElementById('modal-shipping');
        overlay.classList.add('is-open');
        document.body.style.overflow = 'hidden';

        var row = btn.closest('.quote-row');
        totalItemQty = parseInt(row.querySelector('.qty-input').value) || 1;

        var meta = btn.querySelector('.shipping-meta');
        currentShipType = (meta && meta.textContent.indexOf('분할') >= 0) ? 'split' : 'single';

        if (currentShipType === 'single' || shipCards.length === 0) {
            shipCards = [{ qty: totalItemQty, name: '홍길동', addr: '서울특별시 강남구 테헤란로 88, 402호', phone: '010-1234-5678', memo: '' }];
        }

        updateShipTypeUI();
        renderAddrCards();
        fetchShippingFee();
    };

    window.closeShippingModal = function () {
        var overlay = document.getElementById('modal-shipping');
        overlay.classList.remove('is-open');
        document.body.style.overflow = '';
        currentShippingBtn = null;
    };

    window.confirmShipping = function () {
        if (currentShippingBtn) {
            var meta = currentShippingBtn.querySelector('.shipping-meta');
            if (meta) {
                meta.textContent = currentShipType === 'split'
                    ? '분할배송 (' + shipCards.length + '건)'
                    : '단일배송';
            }
        }
        closeShippingModal();
        fetchEstimateFees();
    };

    // 배송 유형 토글
    var shipTypeBtns = document.querySelectorAll('.ship-type-btn');
    var shipTypeDesc = document.getElementById('shipTypeDesc');
    var addBtn = document.getElementById('shipAddrAddBtn');

    shipTypeBtns.forEach(function (b) {
        b.addEventListener('click', function () {
            currentShipType = b.dataset.type;
            if (currentShipType === 'single') {
                shipCards = [{ qty: totalItemQty, name: '홍길동', addr: '서울특별시 강남구 테헤란로 88, 402호', phone: '010-1234-5678', memo: '' }];
            }
            updateShipTypeUI();
            renderAddrCards();
            fetchShippingFee();
        });
    });

    function updateShipTypeUI() {
        shipTypeBtns.forEach(function (b) {
            b.classList.toggle('ship-type-btn--active', b.dataset.type === currentShipType);
        });
        shipTypeDesc.textContent = currentShipType === 'split'
            ? '상품을 여러 배송지로 나누어 발송합니다.'
            : '모든 상품을 하나의 배송지로 발송합니다.';
        addBtn.style.display = currentShipType === 'split' ? 'flex' : 'none';
        document.getElementById('smCount').textContent = shipCards.length + '건';
    }

    // + 버튼: 수량 분배하여 카드 추가
    addBtn.addEventListener('click', function () {
        var count = shipCards.length + 1;
        var base = Math.floor(totalItemQty / count);
        var remainder = totalItemQty % count;

        shipCards.push({ qty: 0, name: '수령인 ' + count, addr: '주소를 입력하세요', phone: '-', memo: '' });

        for (var i = 0; i < shipCards.length; i++) {
            shipCards[i].qty = base + (i < remainder ? 1 : 0);
        }
        renderAddrCards();
        fetchShippingFee();
    });

    // 카드 렌더링
    function renderAddrCards() {
        var list = document.getElementById('shipAddrList');
        list.innerHTML = '';

        shipCards.forEach(function (card, idx) {
            var el = document.createElement('div');
            el.className = 'ship-addr-card';
            el.innerHTML =
                '<div class="ship-addr-card-header">' +
                    '<span class="ship-addr-card-no">' + (idx + 1) + '</span>' +
                    '<div class="ship-addr-card-qty">' +
                        '<label>수량</label>' +
                        '<input type="number" class="ship-qty-input" value="' + card.qty + '" min="1" data-idx="' + idx + '" />' +
                    '</div>' +
                    (shipCards.length > 1 ? '<button type="button" class="ship-addr-remove" data-idx="' + idx + '" aria-label="삭제">&#10005;</button>' : '') +
                '</div>' +
                '<!-- TODO: 배송지 입력 fragment 자리 -->' +
                '<div class="ship-addr-preview">' +
                    '<span class="ship-addr-name">수령인: ' + card.name + '</span>' +
                    '<span class="ship-addr-detail">' + card.addr + '</span>' +
                    '<span class="ship-addr-phone">' + card.phone + '</span>' +
                '</div>' +
                '<textarea class="ship-addr-memo" data-idx="' + idx + '" placeholder="배달 요구사항 (예: 경비실 보관, 전화 후 배송 등)">' + (card.memo || '') + '</textarea>';
            list.appendChild(el);
        });

        // 수량 변경 이벤트
        list.querySelectorAll('.ship-qty-input').forEach(function (inp) {
            inp.addEventListener('change', function () {
                var idx = parseInt(inp.dataset.idx);
                var newVal = parseInt(inp.value) || 1;
                if (newVal < 1) newVal = 1;

                var otherTotal = 0;
                shipCards.forEach(function (c, i) { if (i !== idx) otherTotal += c.qty; });
                var max = totalItemQty - otherTotal;
                if (newVal > max) newVal = max;
                if (newVal < 1) newVal = 1;

                shipCards[idx].qty = newVal;

                var remaining = totalItemQty - newVal;
                var otherCount = shipCards.length - 1;
                if (otherCount > 0) {
                    var perOther = Math.floor(remaining / otherCount);
                    var rem = remaining % otherCount;
                    var j = 0;
                    shipCards.forEach(function (c, i) {
                        if (i !== idx) {
                            c.qty = perOther + (j < rem ? 1 : 0);
                            j++;
                        }
                    });
                }
                renderAddrCards();
                fetchShippingFee();
            });
        });

        // 삭제 버튼 이벤트
        list.querySelectorAll('.ship-addr-remove').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var idx = parseInt(btn.dataset.idx);
                shipCards.splice(idx, 1);
                var base = Math.floor(totalItemQty / shipCards.length);
                var rem = totalItemQty % shipCards.length;
                shipCards.forEach(function (c, i) {
                    c.qty = base + (i < rem ? 1 : 0);
                });
                if (shipCards.length <= 1) {
                    currentShipType = 'single';
                    updateShipTypeUI();
                }
                renderAddrCards();
                fetchShippingFee();
            });
        });

        // 메모 변경 이벤트
        list.querySelectorAll('.ship-addr-memo').forEach(function (ta) {
            ta.addEventListener('input', function () {
                var idx = parseInt(ta.dataset.idx);
                shipCards[idx].memo = ta.value;
            });
        });

        document.getElementById('smCount').textContent = shipCards.length + '건';
    }

    // 배달비 비동기 조회
    function fetchShippingFee() {
        if (!currentShippingBtn) return;
        var region = currentShippingBtn.dataset.region;
        if (!region) return;

        var items = shipCards.map(function (c) {
            return { region: region, qty: c.qty, splitShipment: false };
        });

        var headers = { 'Content-Type': 'application/json' };
        if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

        fetch('/api/quote/delivery-fee', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ items: items })
        })
        .then(function (res) { return res.json(); })
        .then(function (data) {
            document.getElementById('smRegion').textContent = data.regionName || '-';
            document.getElementById('smBase').textContent = data.baseFee ? '₩' + formatNumber(data.baseFee) : '-';
            document.getElementById('smExtra').textContent = data.extraFee ? '₩' + formatNumber(data.extraFee) : '₩0';
            document.getElementById('smTotal').textContent = data.totalFee ? '₩' + formatNumber(data.totalFee) : '-';
        })
        .catch(function () {
            document.getElementById('smBase').textContent = '조회 실패';
            document.getElementById('smTotal').textContent = '-';
        });
    }

    // ESC / 배경 클릭 닫기
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeShippingModal();
    });
    var shippingOverlay = document.getElementById('modal-shipping');
    if (shippingOverlay) {
        shippingOverlay.addEventListener('click', function (e) {
            if (e.target === shippingOverlay) closeShippingModal();
        });
    }
});
