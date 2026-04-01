/* ============================================================
   feeCard.js — 운임/대행 수수료 카드 & 모달 공통 모듈
   feeCards.html fragment 와 함께 사용
   ============================================================ */
document.addEventListener('DOMContentLoaded', function () {

    // ── 모달 열기 / 닫기 ─────────────────────────────
    window.openModal = function (type) {
        var overlay = document.getElementById('modal-' + type);
        if (overlay) { overlay.classList.add('is-open'); document.body.style.overflow = 'hidden'; }
    };
    window.closeModal = function (type) {
        var overlay = document.getElementById('modal-' + type);
        if (overlay) { overlay.classList.remove('is-open'); document.body.style.overflow = ''; }
    };
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal-overlay.is-open').forEach(function (el) { el.classList.remove('is-open'); });
            document.body.style.overflow = '';
        }
    });
    document.addEventListener('click', function (e) {
        if (e.target.classList.contains('modal-overlay') && e.target.classList.contains('is-open')) {
            e.target.classList.remove('is-open');
            document.body.style.overflow = '';
        }
    });

    // ── 툴팁 토글 (fee-info-btn + modal-info-btn) ────
    var tooltipSelector = '.fee-info-btn, .modal-info-btn';
    document.querySelectorAll(tooltipSelector).forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            var wasActive = btn.classList.contains('is-active');
            document.querySelectorAll(tooltipSelector + '.is-active').forEach(function (b) {
                b.classList.remove('is-active');
            });
            if (!wasActive) btn.classList.add('is-active');
        });
    });
    document.addEventListener('click', function () {
        document.querySelectorAll(tooltipSelector + '.is-active').forEach(function (b) {
            b.classList.remove('is-active');
        });
    });

    // ── 공통 유틸 ────────────────────────────────────
    function fmt(v) { return v ? '₩' + fmtNum(v) : '없음'; }
    function fmtNum(n) { return Math.round(n).toLocaleString('ko-KR'); }
    function fmtPeriod(from, to) {
        if (!from) return '';
        return from + ' ~ ' + (to || '무기한');
    }

    var countryNames = { JP: '일본(JP)', CN: '중국(CN)', VN: '베트남(VN)', TW: '대만(TW)', TH: '태국(TH)', ID: '인도네시아(ID)', US: '미국(US)' };
    var transportNames = { SEA: '해상 운송 (FCL)', AIR: '항공 운송', EXPRESS: '특급 배송' };
    var sizeNames = { SMALL: '소형', MEDIUM: '중형', LARGE: '대형' };

    // 툴팁 텍스트 (hidden element에서 로드)
    var tipEl = document.getElementById('md-tooltip-data');
    var tips = tipEl ? tipEl.dataset : {};

    // info 아이콘 SVG
    var infoSvg = '<svg width="11" height="11" viewBox="0 0 12 12" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="6" cy="6" r="5.25" stroke="currentColor" stroke-width="1.2"/><path d="M6 5.25V8.25M6 3.75v.01" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>';

    function tipBtn(text) {
        if (!text) return '';
        return ' <button type="button" class="modal-info-btn">' + infoSvg + '<span class="modal-tooltip">' + text + '</span></button>';
    }

    // ── 공장별 카드 그룹 렌더링 ─────────────────────
    function renderFactoryGroups(factories) {
        var container = document.getElementById('fc-factory-groups');
        if (!container) return;
        container.innerHTML = '';

        if (!factories || factories.length === 0) {
            container.innerHTML = '<div class="fee-item"><span class="fee-label" style="color:var(--color-text-light)">상품을 추가하면 운임이 산출됩니다</span></div>';
            return;
        }

        factories.forEach(function (f, idx) {
            var group = document.createElement('div');
            group.className = 'fc-factory-group';

            var dutyVat = (f.dutyAmount || 0) + (f.vatAmount || 0);
            var portCustoms = (f.portFee || 0) + (f.customsFee || 0) + (f.hsCodeFee || 0);
            var displayName = (f.factoryCity || f.factoryName || '알 수 없음')
                + (f.countryCode ? ' (' + f.countryCode + ')' : '');

            group.innerHTML =
                '<div class="fc-factory-header">' +
                    '<div class="fc-factory-label">' +
                        '<span class="fc-factory-name">' + displayName + '</span>' +
                        '<span class="fc-factory-meta">' + f.itemCount + '개 상품 · ' + (sizeNames[f.sizeType] || f.sizeType) + '</span>' +
                    '</div>' +
                    '<span class="fc-factory-amount">' + fmt(f.subtotal) + '</span>' +
                '</div>' +
                '<div class="fc-factory-details">' +
                    '<div class="fee-item"><span class="fee-label">해외 운임</span><span class="fee-value">' + fmt(f.shippingFee) + '</span></div>' +
                    '<div class="fee-item"><span class="fee-label">항만/통관/HS</span><span class="fee-value">' + fmt(portCustoms) + '</span></div>' +
                    '<div class="fee-item"><span class="fee-label">보험</span><span class="fee-value">' + fmt(f.insuranceFee) + '</span></div>' +
                    '<div class="fee-item"><span class="fee-label">관세 + 부가세</span><span class="fee-value">' + fmt(dutyVat) + '</span></div>' +
                '</div>';

            // 클릭으로 상세 토글
            var header = group.querySelector('.fc-factory-header');
            header.addEventListener('click', function (e) {
                e.stopPropagation();
                group.classList.toggle('is-open');
            });

            container.appendChild(group);
        });
    }

    // ── 카드 + 모달 UI 갱신 ──────────────────────────
    window.updateFeeCards = function (data, itemTotalKrw, dest, insuranceExtra, inspectionExtra, domesticFee, insuranceInfo, domesticData) {
        var el = function (id) { return document.getElementById(id); };
        insuranceExtra = insuranceExtra || 0;
        inspectionExtra = inspectionExtra || 0;
        domesticFee = domesticFee || 0;
        insuranceInfo = insuranceInfo || { name: null, rate: 0 };
        domesticData = domesticData || null;

        // ── 공장별 그룹 렌더링 ──
        renderFactoryGroups(data.factories);

        // ── 카드 총계 ──
        // 국내 운임 (건수 포함)
        if (domesticData && domesticData.totalCount > 0) {
            el('fc-domestic').textContent = fmt(domesticFee) + ' (' + domesticData.totalCount + '건)';
        } else {
            el('fc-domestic').textContent = '없음';
        }

        var logisticsWithExtras = (data.logisticsTotal || 0) + insuranceExtra + domesticFee;

        // ── 운송 비용 할인 (SHIPPING fee policy) ──
        var shippingRate = parseFloat(data.shippingDiscountRate) || 0;
        var logisticsEl = el('fc-logistics-total');
        if (shippingRate > 0 && logisticsWithExtras > 0) {
            var discountedLogistics = Math.round(logisticsWithExtras * (1 - shippingRate));
            logisticsEl.className = 'fee-subtotal-value has-discount';
            logisticsEl.innerHTML =
                '<span class="original-price">' + fmt(logisticsWithExtras) + '</span>' +
                '<span class="discounted-price"><span class="grade-badge">' + (data.buyerGrade || '') + '</span>' + fmt(discountedLogistics) + '</span>';
        } else {
            logisticsEl.className = 'fee-subtotal-value';
            logisticsEl.textContent = fmt(logisticsWithExtras);
        }

        // ── 대행 카드 ──
        el('fc-service').textContent = fmt(data.serviceFee);
        el('fc-inspection').textContent = inspectionExtra > 0 ? fmt(inspectionExtra) : '없음';

        var procurementWithExtras = (data.procurementTotal || 0) + inspectionExtra;

        // ── 대행 서비스 할인 (SERVICE_COMMISSION 등급 차이) ──
        var standardSvcFee = parseFloat(data.standardServiceFee) || 0;
        var procurementEl = el('fc-procurement-total');
        if (standardSvcFee > 0 && standardSvcFee > (data.procurementTotal || 0)) {
            var standardProcurement = standardSvcFee + inspectionExtra;
            procurementEl.className = 'fee-subtotal-value has-discount';
            procurementEl.innerHTML =
                '<span class="original-price">' + fmt(standardProcurement) + '</span>' +
                '<span class="discounted-price"><span class="grade-badge">' + (data.buyerGrade || '') + '</span>' + fmt(procurementWithExtras) + '</span>';
        } else {
            procurementEl.className = 'fee-subtotal-value';
            procurementEl.textContent = fmt(procurementWithExtras);
        }

        // ── 운임 모달 — 공장별 섹션 동적 렌더링 ──
        var modalBody = document.querySelector('#modal-logistics .modal-body');
        if (modalBody && data.factories) {
            // 기존 동적 섹션 제거 (.md-factory-section)
            modalBody.querySelectorAll('.md-factory-section').forEach(function (s) { s.remove(); });
            // 기존 총계 행 갱신
            if (shippingRate > 0 && logisticsWithExtras > 0) {
                var mdDiscounted = Math.round(logisticsWithExtras * (1 - shippingRate));
                el('md-logistics-total').innerHTML =
                    '<span style="text-decoration:line-through;color:var(--color-text-light);font-size:13px;margin-right:8px;">' + fmt(logisticsWithExtras) + '</span>' + fmt(mdDiscounted);
            } else {
                el('md-logistics-total').textContent = fmt(logisticsWithExtras);
            }

            var insertBefore = modalBody.querySelector('.modal-note');

            data.factories.forEach(function (f, idx) {
                var section = document.createElement('div');
                section.className = 'md-factory-section';

                var num = String(idx + 1).padStart(2, '0');
                var displayName = (f.factoryCity || f.factoryName || '알 수 없음')
                    + (f.countryCode ? ' (' + f.countryCode + ')' : '');
                var portCustoms = (f.portFee || 0) + (f.customsFee || 0) + (f.hsCodeFee || 0);
                var dutyVat = (f.dutyAmount || 0) + (f.vatAmount || 0);

                var rateTableHtml = '';
                if (f.srSmQn != null) {
                    var smActive = f.sizeType === 'SMALL' ? ' rate-active' : '';
                    var mdActive = f.sizeType === 'MEDIUM' ? ' rate-active' : '';
                    var lgActive = f.sizeType === 'LARGE' ? ' rate-active' : '';
                    var caption = (countryNames[f.countryCode] || f.countryCode || '-')
                        + (f.srSince ? '  ·  ' + f.srSince + ' 책정' : '');

                    rateTableHtml =
                        '<div class="shipping-rate-table">' +
                            '<p class="rate-table-caption">' + caption + ' 기준 해상 운임 구간표</p>' +
                            '<table><thead><tr><th>규모</th><th>기준</th><th>운임비</th></tr></thead><tbody>' +
                            '<tr class="' + smActive + '"><td>소형</td><td>' + f.srSmQn + '유닛 미만</td><td>₩' + fmtNum(f.srSmAm) + '</td></tr>' +
                            '<tr class="' + mdActive + '"><td>중형</td><td>' + f.srMdQn + '유닛 이상</td><td>₩' + fmtNum(f.srMdAm) + '</td></tr>' +
                            '<tr class="' + lgActive + '"><td>대형</td><td>' + f.srLgQn + '유닛 이상</td><td>₩' + fmtNum(f.srLgAm) + '</td></tr>' +
                            '</tbody></table></div>';
                }

                section.innerHTML =
                    '<div class="modal-divider"></div>' +
                    '<div>' +
                        '<p class="modal-section-label">' + num + ' · ' + displayName +
                            ' <span class="fc-factory-meta">' + f.itemCount + '개 상품</span>' +
                            (f.shippingRateUpdatedAt ? ' <span class="modal-policy-date">갱신: ' + f.shippingRateUpdatedAt + '</span>' : '') +
                        '</p>' +
                        '<div class="modal-detail-rows">' +
                            '<div class="modal-detail-row"><span class="modal-row-key">운송 수단' + tipBtn(tips.transport) + '</span><span class="modal-row-val">' + (transportNames[f.transportType] || f.transportType || '-') + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">운임 기준' + tipBtn(tips.basis) + '</span><span class="modal-row-val">CIF 부산항</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">적용 규모' + tipBtn(tips.sizeType) + '</span><span class="modal-row-val">' + (sizeNames[f.sizeType] || '-') + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">해외 운임' + tipBtn(tips.shippingFee) + '</span><span class="modal-row-val">' + fmt(f.shippingFee) + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">항만/통관/HS' + tipBtn(tips.portFee) + '</span><span class="modal-row-val">' + fmt(portCustoms) + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">보험' + tipBtn(tips.insurance) + '</span><span class="modal-row-val">' + fmt(f.insuranceFee) + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">CIF' + tipBtn(tips.cif) + '</span><span class="modal-row-val">' + fmt(f.cifAmount) + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">관세율' + tipBtn(tips.dutyRate) + '</span><span class="modal-row-val">' + (f.dutyRate ? (f.dutyRate * 100).toFixed(1) + '%' : '-') + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">관세' + tipBtn(tips.duty) + '</span><span class="modal-row-val">' + fmt(f.dutyAmount) + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">부가세' + tipBtn(tips.vat) + '</span><span class="modal-row-val">' + fmt(f.vatAmount) + '</span></div>' +
                            '<div class="modal-detail-row row-subtotal"><span class="modal-row-key">출발지 소계</span><span class="modal-row-val">' + fmt(f.subtotal) + '</span></div>' +
                        '</div>' +
                        rateTableHtml +
                    '</div>';

                modalBody.insertBefore(section, insertBefore);
            });

            // 동적 생성된 tooltip 버튼에 이벤트 바인딩
            modalBody.querySelectorAll('.md-factory-section .modal-info-btn').forEach(function (btn) {
                btn.addEventListener('click', function (e) {
                    e.stopPropagation();
                    var wasActive = btn.classList.contains('is-active');
                    document.querySelectorAll('.modal-info-btn.is-active').forEach(function (b) { b.classList.remove('is-active'); });
                    if (!wasActive) btn.classList.add('is-active');
                });
            });
        }

        // ── 대행 수수료 모달 상세 ──
        el('md-buyer-grade').textContent = data.buyerGrade || '-';
        el('md-item-total-krw').textContent = '₩' + fmtNum(itemTotalKrw);
        el('md-service-fee').textContent = fmt(data.serviceFee);
        if (standardSvcFee > 0 && standardSvcFee > (data.procurementTotal || 0)) {
            var mdStdProc = standardSvcFee + inspectionExtra;
            el('md-procurement-total').innerHTML =
                '<span style="text-decoration:line-through;color:var(--color-text-light);font-size:13px;margin-right:8px;">' + fmt(mdStdProc) + '</span>' + fmt(procurementWithExtras);
        } else {
            el('md-procurement-total').textContent = fmt(procurementWithExtras);
        }

        // ── 정책 기간 ──
        if (el('md-service-date')) {
            el('md-service-date').textContent = data.serviceFeeEffFrom
                ? '적용: ' + fmtPeriod(data.serviceFeeEffFrom, data.serviceFeeEffTo) : '';
        }
    };
});
