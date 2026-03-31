document.addEventListener('DOMContentLoaded', function () {

    // ── 부가 서비스 아코디언 토글 ──────────────────
    document.querySelectorAll('.quote-option-toggle').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            var item = btn.closest('.quote-option-item');
            var detail = item.querySelector('.quote-option-detail');
            if (!detail) return;
            var isOpen = detail.classList.toggle('is-open');
            btn.classList.toggle('is-open', isOpen);
        });
    });

    // ── 헤더 info 툴팁 토글 ─────────────────────────
    document.querySelectorAll('.th-info-btn').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            var wasActive = btn.classList.contains('is-active');
            document.querySelectorAll('.th-info-btn.is-active').forEach(function (b) {
                b.classList.remove('is-active');
            });
            if (!wasActive) btn.classList.add('is-active');
        });
    });
    document.addEventListener('click', function () {
        document.querySelectorAll('.th-info-btn.is-active').forEach(function (b) {
            b.classList.remove('is-active');
        });
    });

    // ── 유틸 ──────────────────────────────────────────

    // 통화 기호 → 통화 코드 매핑
    var currencySymbolToCode = { '¥': 'JPY', '$': 'USD', '€': 'EUR', '₩': 'KRW' };

    function normalizeCurrency(cur) {
        if (!cur) return '';
        var trimmed = cur.trim();
        return (currencySymbolToCode[trimmed] || trimmed).toUpperCase();
    }

    // 모든 환율 슬라이드에서 통화별 환율 맵 구축
    function buildRateMap() {
        var map = {};
        var slides = document.querySelectorAll('.er-slide');
        slides.forEach(function (slide) {
            // 슬라이드 텍스트에서 통화 코드 추출: "1 JPY = 9.14 KRW"
            var valueEl = slide.querySelector('.er-slide-value');
            if (!valueEl) return;
            var text = valueEl.textContent.trim();
            // "1 JPY = 9.14 KRW" 형태에서 통화 코드 파싱
            var match = text.match(/1\s+(\w+)\s*=/);
            if (!match) return;
            var code = match[1].toUpperCase();

            var numEl = slide.querySelector('.er-rate-number');
            if (!numEl) return;
            var rate = parseFloat(numEl.textContent.replace(/,/g, '')) || 0;
            if (rate > 0) map[code] = rate;
        });
        return map;
    }

    // 특정 통화에 대한 환율 반환 (KRW면 1, 없으면 0)
    function getRateForCurrency(stCur) {
        var code = normalizeCurrency(stCur);
        if (code === 'KRW') return 1;
        var map = buildRateMap();
        return map[code] || 0;
    }

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

    // ── 부가 서비스 체크 유틸 ─────────────────────────
    function isAnyInsuranceChecked() {
        var checked = false;
        document.querySelectorAll('.quote-option-item[data-type="insurance"]').forEach(function (item) {
            if (item.querySelector('.qo-service-check').checked) checked = true;
        });
        return checked;
    }

    function updateServiceValues(supplyTotal) {
        document.querySelectorAll('.quote-option-item[data-type="insurance"]').forEach(function (item) {
            var rate = parseFloat(item.dataset.rate) || 0;
            var valueEl = item.querySelector('.quote-option-value');
            var check = item.querySelector('.qo-service-check');
            if (check.checked && supplyTotal > 0) {
                valueEl.textContent = '₩' + formatNumber(supplyTotal * rate);
            } else {
                valueEl.textContent = '없음';
            }
        });
    }

    // (국내 배달비는 estimate-fees 응답에 포함)

    // 선택된 보험 금액 합산 (공급가 × rate)
    function getSelectedInsuranceAmount(supplyTotal) {
        var total = 0;
        document.querySelectorAll('.quote-option-item[data-type="insurance"]').forEach(function (item) {
            if (item.querySelector('.qo-service-check').checked) {
                total += supplyTotal * (parseFloat(item.dataset.rate) || 0);
            }
        });
        return Math.round(total);
    }

    // 선택된 보험 상세 정보 (이름, 요율)
    function getSelectedInsuranceInfo() {
        var info = { name: null, rate: 0 };
        document.querySelectorAll('.quote-option-item[data-type="insurance"]').forEach(function (item) {
            if (item.querySelector('.qo-service-check').checked) {
                info.name = item.querySelector('.quote-option-name').textContent;
                info.rate = parseFloat(item.dataset.rate) || 0;
            }
        });
        return info;
    }

    // 선택된 검사 금액 합산 (고정금액)
    function getSelectedInspectionAmount() {
        var total = 0;
        document.querySelectorAll('.quote-option-item[data-type="inspection"]').forEach(function (item) {
            if (item.querySelector('.qo-service-check').checked) {
                total += parseFloat(item.dataset.amount) || 0;
            }
        });
        return Math.round(total);
    }

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

        // 환율 기반 계산 — 상품 자체 통화 기준으로 환율 조회
        var rate = getRateForCurrency(stCur);
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
                subtotalInput.value = formatNumber(krwTotal);
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
            // 소계 변경 시 총 주문 명세·운임 재계산
            fetchEstimateFees();
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

    // ── 배송지 변수 (doFetchEstimateFees에서 사용하므로 먼저 선언) ──
    var currentShippingBtn = null;
    var currentShipType = 'single';
    var shipCards = [];
    var totalItemQty = 0;
    var rcEl = document.getElementById('js-default-region');
    var defaultRegion = rcEl ? (rcEl.dataset.region || 'SEOUL') : 'SEOUL';
    var defaultReceiverName = rcEl ? (rcEl.dataset.name || '') : '';
    var defaultReceiverAddr = rcEl ? (rcEl.dataset.addr || '') : '';

    // ── 예상 운임 비용 조회 ─────────────────────────────
    var feeDebounceTimer = null;

    function fetchEstimateFees() {
        clearTimeout(feeDebounceTimer);
        feeDebounceTimer = setTimeout(doFetchEstimateFees, 400);
    }

    function doFetchEstimateFees() {
        var estimateItems = [];
        var itemTotalKrw = 0;
        var supplyTotalKrw = 0;

        rows.forEach(function (row) {
            var stId = row.dataset.stId;
            if (!stId) return;

            var qty = parseInt(row.querySelector('.qty-input').value) || 1;
            var specSelect = row.querySelector('.spec-select');
            var unGQn = specSelect ? parseInt(specSelect.value) || 1 : 1;
            var dozen = Math.ceil(qty / unGQn);

            // 사용자 조정 소계
            var subtotalInput = row.querySelector('.subtotal-input');
            var subtotalVal = parseFloat((subtotalInput.value || '').replace(/,/g, '')) || 0;
            itemTotalKrw += Math.round(subtotalVal);

            // 공급가
            var stPr = parseFloat(row.dataset.stPr) || 0;
            var stCur = row.dataset.stCur || '';
            var rowRate = getRateForCurrency(stCur);
            var supplyKrw = rowRate > 0 ? Math.round(stPr * qty * rowRate) : 0;
            supplyTotalKrw += supplyKrw;

            var hsDuRa = parseFloat(row.dataset.hsDuRa) || 0;

            estimateItems.push({
                stId: parseInt(stId),
                qty: qty,
                dozen: dozen,
                supplyKrw: supplyKrw,
                dutyRate: hsDuRa
            });
        });

        // 선택된 보험 요율
        var insuranceInfo = getSelectedInsuranceInfo();

        var headers = { 'Content-Type': 'application/json' };
        if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

        // 배송지 지역 코드 수집 (shipCards가 세팅되어 있으면 사용, 아니면 백엔드가 기본 수령지 사용)
        var shipRegions = [];
        if (shipCards.length > 0) {
            shipCards.forEach(function (c) { shipRegions.push(c.region || defaultRegion); });
        }

        fetch('/api/quote/estimate-fees', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({
                items: estimateItems,
                itemTotalKrw: itemTotalKrw,
                insuranceYn: isAnyInsuranceChecked(),
                insuranceRate: insuranceInfo.rate || 0,
                shipRegions: shipRegions.length > 0 ? shipRegions : null
            })
        })
        .then(function (res) {
            if (!res.ok) { console.error('estimate-fees HTTP', res.status); return Promise.reject('HTTP ' + res.status); }
            return res.json();
        })
        .then(function (data) {
            console.log('estimate-fees response:', JSON.stringify(data).substring(0, 500));
            var el = function (id) { return document.getElementById(id); };
            var fmt = function (v) { return v ? '₩' + formatNumber(v) : '-'; };

            // 부가 서비스 금액 갱신
            updateServiceValues(supplyTotalKrw);

            // 상품가 소계 (물품 대금 + 행별 관세 합산)
            var rowDutyTotal = 0;
            rows.forEach(function (row) {
                var taxEl = row.querySelector('.tax-amount');
                if (taxEl && taxEl.textContent !== '-') {
                    rowDutyTotal += parseFloat(taxEl.textContent.replace(/[₩,]/g, '')) || 0;
                }
            });
            var subtotal = itemTotalKrw + Math.round(rowDutyTotal);
            el('qo-item-total').textContent = itemTotalKrw > 0
                ? '₩' + formatNumber(itemTotalKrw) : '-';
            el('qo-duty-total').textContent = rowDutyTotal > 0
                ? '₩' + formatNumber(rowDutyTotal) : '-';
            el('qo-subtotal').textContent = subtotal > 0
                ? '₩' + formatNumber(subtotal) : '-';

            // 국내 배달비를 응답에서 직접 사용
            var domesticFee = data.domesticFee || 0;
            var domesticData = { totalFee: domesticFee, totalCount: data.domesticCount || 0, regions: data.domesticRegions || [] };

            // feeCard.js 의 공통 렌더링 함수 호출
            var selectedInsurance = getSelectedInsuranceAmount(supplyTotalKrw);
            var selectedInspection = getSelectedInspectionAmount();
            var insuranceInfo = getSelectedInsuranceInfo();
            var shipBtn = document.querySelector('.shipping-btn');
            var dest = shipBtn ? shipBtn.querySelector('.shipping-addr').textContent : '수령지';
            if (typeof updateFeeCards === 'function') {
                updateFeeCards(data, itemTotalKrw, dest, selectedInsurance, selectedInspection, domesticFee, insuranceInfo, domesticData);
            }

            // 총 주문 명세 (부가 서비스 포함)
            var totalTax = (data.dutyAmount || 0) + (data.vatAmount || 0);
            var logisticsVal = (data.logisticsTotal || 0) + selectedInsurance + domesticFee;
            var procurementVal = (data.procurementTotal || 0) + selectedInspection;
            var grandTotal = itemTotalKrw + logisticsVal + procurementVal;

            el('os-item-total').textContent = '₩' + formatNumber(itemTotalKrw);
            el('os-logistics').textContent = fmt(logisticsVal);
            el('os-procurement').textContent = fmt(procurementVal);
            el('os-tax').textContent = totalTax > 0 ? '₩' + formatNumber(totalTax) : '-';
            el('os-grand-total').textContent = grandTotal > 0 ? '₩' + formatNumber(grandTotal) : '-';
        })
        .catch(function (err) { console.error('estimate-fees error:', err); });
    }

    // 부가 서비스 체크박스 — 같은 카드 내 하나만 선택 + 재조회
    document.querySelectorAll('.qo-service-check').forEach(function (check) {
        check.addEventListener('change', function () {
            if (check.checked) {
                var card = check.closest('.quote-options-card');
                card.querySelectorAll('.qo-service-check').forEach(function (other) {
                    if (other !== check) other.checked = false;
                });
            }
            fetchEstimateFees();
        });
    });

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

    // 초기 로딩 — estimate-fees 한 번으로 모든 값 채움 (국내 배달비 포함)
    doFetchEstimateFees();

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

    // 지역 옵션 생성 (hidden select에서 데이터 읽기)
    function buildRegionOptions(selectedRegion) {
        var regionNames = { SEOUL: '서울특별시', GYEONGGI: '경기도', METRO: '수도권', PROVINCE: '지방', JEJU: '제주', ISLAND: '도서산간' };
        var source = document.getElementById('js-domestic-regions');
        var html = '';
        if (source) {
            Array.prototype.forEach.call(source.options, function (opt) {
                var code = opt.value;
                var eam = parseInt(opt.dataset.eam) || 0;
                var name = regionNames[code] || code;
                var extra = eam > 0 ? ' (+₩' + formatNumber(eam) + ')' : '';
                var sel = (code === selectedRegion) ? ' selected' : '';
                html += '<option value="' + code + '"' + sel + '>' + name + extra + '</option>';
            });
        }
        return html || '<option value="SEOUL">서울특별시</option>';
    }

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
            shipCards = [{ qty: totalItemQty, region: defaultRegion, name: defaultReceiverName, addr: defaultReceiverAddr, phone: '', memo: '' }];
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
                shipCards = [{ qty: totalItemQty, region: defaultRegion, name: defaultReceiverName, addr: defaultReceiverAddr, phone: '', memo: '' }];
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

        shipCards.push({ qty: 0, region: defaultRegion, name: '수령인 ' + count, addr: '주소를 입력하세요', phone: '-', memo: '' });

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
                '<div class="ship-addr-region">' +
                    '<label class="ship-addr-region-label">배송 지역</label>' +
                    '<select class="ship-region-select" data-idx="' + idx + '">' + buildRegionOptions(card.region) + '</select>' +
                '</div>' +
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

        // 지역 변경 이벤트
        list.querySelectorAll('.ship-region-select').forEach(function (sel) {
            sel.addEventListener('change', function () {
                var idx = parseInt(sel.dataset.idx);
                shipCards[idx].region = sel.value;
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

    // 배달비 비동기 조회 (지역별 그룹핑)
    function fetchShippingFee() {
        var items = shipCards.map(function (c) {
            return { region: c.region || defaultRegion, qty: c.qty, splitShipment: false };
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
            // 지역별 그룹 렌더링
            var container = document.getElementById('smRegionGroups');
            if (container) {
                container.innerHTML = '';
                if (data.regions && data.regions.length > 0) {
                    data.regions.forEach(function (rg) {
                        var extra = rg.extraFeeUnit > 0
                            ? '<div class="ship-fee-row"><span class="ship-fee-label">추가금</span><span class="ship-fee-value">₩' + formatNumber(rg.extraFeeUnit) + '/건</span></div>'
                            : '';
                        var div = document.createElement('div');
                        div.className = 'ship-fee-region-group';
                        div.innerHTML =
                            '<div class="ship-fee-row"><span class="ship-fee-label">' + rg.regionName + '</span><span class="ship-fee-value">' + rg.count + '건</span></div>' +
                            '<div class="ship-fee-row"><span class="ship-fee-label">기본 배달비</span><span class="ship-fee-value">₩' + formatNumber(rg.baseFeeUnit) + '/건</span></div>' +
                            extra +
                            '<div class="ship-fee-row"><span class="ship-fee-label">지역 소계</span><span class="ship-fee-value">₩' + formatNumber(rg.subtotal) + '</span></div>';
                        container.appendChild(div);
                    });
                }
            }
            document.getElementById('smCount').textContent = data.totalCount || 0;
            document.getElementById('smTotal').textContent = data.totalFee ? '₩' + formatNumber(data.totalFee) : '-';
        })
        .catch(function () {
            document.getElementById('smTotal').textContent = '조회 실패';
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
