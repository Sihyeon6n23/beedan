/* ============================================================
   feeCard.js — 운임/대행 수수료 카드 & 모달 공통 모듈
   feeCards.html fragment 와 함께 사용
   ============================================================ */
document.addEventListener('DOMContentLoaded', function () {

    // ── admin 감지 + 제목 변경 ───────────────────────
    var isAdmin = !!document.getElementById('js-is-admin');
    if (isAdmin) {
        var titleEl = document.getElementById('fc-logistics-title');
        if (titleEl) titleEl.textContent = '운임 비용';
        var mdTitleEl = document.getElementById('md-logistics-title');
        if (mdTitleEl) mdTitleEl.textContent = '운임 비용';
        var applyBtn = document.getElementById('md-admin-apply');
        if (applyBtn) applyBtn.style.display = '';
    }

    // admin 수동 입력값 저장소 (key: factory index)
    window._adminFeeOverrides = window._adminFeeOverrides || {};
    // 마지막 updateFeeCards 호출 인자 보관 (재호출용)
    var _lastFeeCardsArgs = null;

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

    var countryNames = { JP: '일본(JP)', CN: '중국(CN)', VN: '베트남(VN)', TW: '대만(TW)', TH: '태국(TH)', ID: '인도네시아(ID)', US: '미국(US)', KR: '한국(KR)' };

    // DB에서 로드된 국가 코드 목록으로 옵션 빌드
    function buildCountryOptions() {
        var source = document.getElementById('js-country-codes');
        var html = '<option value="">선택</option>';
        if (source) {
            Array.prototype.forEach.call(source.options, function (opt) {
                var code = opt.value;
                var name = countryNames[code] || code;
                html += '<option value="' + code + '">' + name + '</option>';
            });
        } else {
            // fallback: countryNames 전체
            Object.keys(countryNames).forEach(function (code) {
                html += '<option value="' + code + '">' + countryNames[code] + '</option>';
            });
        }
        return html;
    }
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
    function renderFactoryGroups(factories, shippingRate, buyerGrade) {
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

            // 해외 운임 할인 표시
            var shippingHtml;
            if (shippingRate > 0 && (f.shippingFee || 0) > 0) {
                var discShipping = Math.round(f.shippingFee * (1 - shippingRate));
                shippingHtml = '<span class="fee-value has-discount">' +
                    '<span class="original-price">' + fmt(f.shippingFee) + '</span>' +
                    '<span class="discounted-line"><span class="grade-badge">' + buyerGrade + '</span>' + fmt(discShipping) + '</span></span>';
            } else {
                shippingHtml = '<span class="fee-value">' + (f.shippingFee ? fmt(f.shippingFee) : '큐레이터가 공급처 확인 후 재안내드립니다') + '</span>';
            }

            group.innerHTML =
                '<div class="fc-factory-header">' +
                    '<div class="fc-factory-label">' +
                        '<span class="fc-factory-name">' + displayName + '</span>' +
                        '<span class="fc-factory-meta">' + f.itemCount + '개 상품 · ' + (sizeNames[f.sizeType] || f.sizeType) + '</span>' +
                    '</div>' +
                    '<span class="fc-factory-amount">' + fmt(f.subtotal) + '</span>' +
                '</div>' +
                '<div class="fc-factory-details">' +
                    '<div class="fee-item"><span class="fee-label">해외 운임</span>' + shippingHtml + '</div>' +
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

        // 인자 보관 (admin 운임 적용 시 재호출용)
        _lastFeeCardsArgs = { data: data, itemTotalKrw: itemTotalKrw, dest: dest, insuranceExtra: insuranceExtra, inspectionExtra: inspectionExtra, domesticFee: domesticFee, insuranceInfo: insuranceInfo, domesticData: domesticData };

        // ── 기존 견적 운임 데이터로 오버라이드 초기화 (재작성/복원 시) ──
        if (window._prevShipFees && window._prevShipFees.length > 0 && data.factories) {
            data.factories.forEach(function (f, idx) {
                var prev = null;
                for (var i = 0; i < window._prevShipFees.length; i++) {
                    var ps = window._prevShipFees[i];
                    if (ps.qsfFaNm === f.factoryName && ps.qsfFaCCd === f.countryCode) { prev = ps; break; }
                    if (!ps.qsfFaNm && !f.factoryName) { prev = ps; break; }
                }
                if (prev && !window._adminFeeOverrides[idx]) {
                    window._adminFeeOverrides[idx] = {
                        shippingFee: prev.qsfSrAm || 0,
                        portFee: prev.qsfPrtAm || 0,
                        customsFee: prev.qsfCstAm || 0,
                        hsCodeFee: prev.qsfHsCd || 0
                    };
                    f.shippingFee = prev.qsfSrAm || 0;
                    f.portFee = prev.qsfPrtAm || 0;
                    f.customsFee = prev.qsfCstAm || 0;
                    f.hsCodeFee = prev.qsfHsCd || 0;

                    // CIF/관세/부가세도 기존 값으로 패치
                    f.cifAmount = prev.qsfCifAm || 0;
                    f.dutyAmount = prev.qsfDty || 0;
                    f.vatAmount = prev.qsfVat || 0;
                    f.insuranceFee = prev.qsfInsYn ? (prev.qsfInsAm || 0) : 0;
                    f.subtotal = prev.qsfTtl || 0;
                }
            });

            // logisticsTotal 등 합계도 재계산
            var newLogistics = 0, newDuty = 0, newVat = 0;
            data.factories.forEach(function (f) {
                newLogistics += (f.subtotal || 0);
                newDuty += (f.dutyAmount || 0);
                newVat += (f.vatAmount || 0);
            });
            data.logisticsTotal = newLogistics;
            data.dutyAmount = newDuty;
            data.vatAmount = newVat;

            window._prevShipFees = null; // 한 번만 적용
        }

        // ── admin 수동 오버라이드 재적용 (estimate-fees 재호출 후에도 유지) ──
        if (isAdmin && window._adminFeeOverrides && Object.keys(window._adminFeeOverrides).length > 0 && data.factories) {
            data.factories.forEach(function (f, idx) {
                var ov = window._adminFeeOverrides[idx];
                if (!ov) return;

                if (ov.shippingFee != null) f.shippingFee = ov.shippingFee;
                if (ov.portFee != null) f.portFee = ov.portFee;
                if (ov.customsFee != null) f.customsFee = ov.customsFee;
                if (ov.hsCodeFee != null) f.hsCodeFee = ov.hsCodeFee;

                // CIF 재계산 (상품가 + 운임 + 보험)
                var supply = f.supplySubtotal || 0;
                var ship = f.shippingFee || 0;
                var ins = f.insuranceFee || 0;
                f.cifAmount = supply + ship + ins;

                // 관세/부가세 재계산
                var dutyRate = f.dutyRate || 0.13;
                f.dutyAmount = Math.round(f.cifAmount * dutyRate);
                f.vatAmount = Math.round((f.cifAmount + f.dutyAmount) * 0.10);

                // 소계 재계산
                f.subtotal = ship + (f.portFee || 0) + (f.customsFee || 0) + (f.hsCodeFee || 0)
                    + ins + f.dutyAmount + f.vatAmount;
            });

            // 총계 재계산
            var ovLogistics = 0, ovDuty = 0, ovVat = 0;
            data.factories.forEach(function (f) {
                ovLogistics += (f.subtotal || 0);
                ovDuty += (f.dutyAmount || 0);
                ovVat += (f.vatAmount || 0);
            });
            data.logisticsTotal = ovLogistics;
            data.dutyAmount = ovDuty;
            data.vatAmount = ovVat;
        }

        // ── 공장별 그룹 렌더링 ──
        var shippingRate = parseFloat(data.shippingDiscountRate) || 0;
        renderFactoryGroups(data.factories, shippingRate, data.buyerGrade || '');

        // ── 관리자: 공급처 미등록 감지 → 버튼 표시 ──
        var isAdmin = !!document.getElementById('js-is-admin');
        var unknownFactories = (data.factories || []).filter(function (f) {
            return !f.countryCode;
        });
        var btnFactoryInput = document.getElementById('btnFactoryInput');
        if (btnFactoryInput) {
            btnFactoryInput.style.display = (isAdmin && unknownFactories.length > 0) ? '' : 'none';
        }
        // 미등록 공장 데이터 보관 (모달 렌더링용)
        window._unknownFactories = unknownFactories;

        // ── 카드 총계 ──
        // 국내 운임 (건수 포함 + 할인)
        var domesticEl = el('fc-domestic');
        if (domesticData && domesticData.totalCount > 0) {
            if (shippingRate > 0 && domesticFee > 0) {
                var discDomestic = Math.round(domesticFee * (1 - shippingRate));
                domesticEl.className = 'fee-value has-discount';
                domesticEl.innerHTML =
                    '<span class="original-price">' + fmt(domesticFee) + ' (' + domesticData.totalCount + '건)</span>' +
                    '<span class="discounted-line"><span class="grade-badge">' + (data.buyerGrade || '') + '</span>' + fmt(discDomestic) + '</span>';
            } else {
                domesticEl.className = 'fee-value';
                domesticEl.textContent = fmt(domesticFee) + ' (' + domesticData.totalCount + '건)';
            }
        } else {
            domesticEl.className = 'fee-value';
            domesticEl.textContent = '없음';
        }

        var logisticsWithExtras = (data.logisticsTotal || 0) + insuranceExtra + domesticFee;
        var logisticsEl = el('fc-logistics-total');
        if (shippingRate > 0 && logisticsWithExtras > 0) {
            var discountedLogistics = Math.round(logisticsWithExtras * (1 - shippingRate));
            logisticsEl.className = 'fee-subtotal-value has-discount';
            logisticsEl.innerHTML =
                '<span class="original-price">' + fmt(logisticsWithExtras) + '</span>' +
                '<span class="discounted-line"><span class="grade-badge">' + (data.buyerGrade || '') + '</span>' + fmt(discountedLogistics) + '</span>';
        } else {
            logisticsEl.className = 'fee-subtotal-value';
            logisticsEl.textContent = fmt(logisticsWithExtras);
        }

        // ── 대행 카드 ──
        var standardSvcFee = parseFloat(data.standardServiceFee) || 0;
        var svcEl = el('fc-service');
        if (standardSvcFee > 0 && standardSvcFee > (data.serviceFee || 0)) {
            svcEl.className = 'fee-value has-discount';
            svcEl.innerHTML =
                '<span class="original-price">' + fmt(standardSvcFee) + '</span>' +
                '<span class="discounted-line"><span class="grade-badge">' + (data.buyerGrade || '') + '</span>' + fmt(data.serviceFee) + '</span>';
        } else {
            svcEl.className = 'fee-value';
            svcEl.textContent = fmt(data.serviceFee);
        }
        el('fc-inspection').textContent = inspectionExtra > 0 ? fmt(inspectionExtra) : '없음';

        var procurementWithExtras = (data.procurementTotal || 0) + inspectionExtra;
        var procurementEl = el('fc-procurement-total');
        if (standardSvcFee > 0 && standardSvcFee > (data.serviceFee || 0)) {
            var standardProcurement = standardSvcFee + inspectionExtra;
            procurementEl.className = 'fee-subtotal-value has-discount';
            procurementEl.innerHTML =
                '<span class="original-price">' + fmt(standardProcurement) + '</span>' +
                '<span class="discounted-line"><span class="grade-badge">' + (data.buyerGrade || '') + '</span>' + fmt(procurementWithExtras) + '</span>';
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
                    '<span style="text-decoration:line-through;color:var(--color-text-light);margin-right:8px;">' + fmt(logisticsWithExtras) + '</span>' + fmt(mdDiscounted);
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

                // 모달 해외 운임 할인 표시
                var mdShippingVal;
                if (shippingRate > 0 && (f.shippingFee || 0) > 0) {
                    var mdDiscShip = Math.round(f.shippingFee * (1 - shippingRate));
                    mdShippingVal = '<span style="text-decoration:line-through;color:var(--color-text-light);margin-right:8px;">' + fmt(f.shippingFee) + '</span>' + fmt(mdDiscShip);
                } else {
                    mdShippingVal = fmt(f.shippingFee);
                }

                // admin 오버라이드 확인
                var ov = window._adminFeeOverrides[idx] || {};
                var ovShip = ov.shippingFee != null ? ov.shippingFee : f.shippingFee;
                var ovPort = ov.portFee != null ? ov.portFee : f.portFee;
                var ovCust = ov.customsFee != null ? ov.customsFee : f.customsFee;
                var ovHs = ov.hsCodeFee != null ? ov.hsCodeFee : f.hsCodeFee;

                // admin 편집 가능 필드 vs 읽기 전용
                var shippingHtml, portHtml, customsHtml, hsHtml;
                if (isAdmin) {
                    shippingHtml = '<input type="text" class="admin-fee-input" data-idx="' + idx + '" data-field="shippingFee" value="' + Math.round(ovShip || 0) + '" />';
                    portHtml = '<input type="text" class="admin-fee-input" data-idx="' + idx + '" data-field="portFee" value="' + Math.round(ovPort || 0) + '" />';
                    customsHtml = '<input type="text" class="admin-fee-input" data-idx="' + idx + '" data-field="customsFee" value="' + Math.round(ovCust || 0) + '" />';
                    hsHtml = '<input type="text" class="admin-fee-input" data-idx="' + idx + '" data-field="hsCodeFee" value="' + Math.round(ovHs || 0) + '" />';
                } else {
                    shippingHtml = mdShippingVal;
                    portHtml = fmt(f.portFee);
                    customsHtml = fmt(f.customsFee);
                    hsHtml = fmt(f.hsCodeFee);
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
                            '<div class="modal-detail-row"><span class="modal-row-key">해외 운임' + tipBtn(tips.shippingFee) + '</span><span class="modal-row-val">' + shippingHtml + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">항만비' + tipBtn(tips.portFee) + '</span><span class="modal-row-val">' + portHtml + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">통관비</span><span class="modal-row-val">' + customsHtml + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">HS 신고비</span><span class="modal-row-val">' + hsHtml + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">보험' + tipBtn(tips.insurance) + '</span><span class="modal-row-val">' + fmt(f.insuranceFee) + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">CIF' + tipBtn(tips.cif) + '</span><span class="modal-row-val admin-cif-val" data-idx="' + idx + '">' + fmt(f.cifAmount) + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">관세율' + tipBtn(tips.dutyRate) + '</span><span class="modal-row-val">' + (f.dutyRate ? (f.dutyRate * 100).toFixed(1) + '%' : '-') + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">관세' + tipBtn(tips.duty) + '</span><span class="modal-row-val admin-duty-val" data-idx="' + idx + '">' + fmt(f.dutyAmount) + '</span></div>' +
                            '<div class="modal-detail-row"><span class="modal-row-key">부가세' + tipBtn(tips.vat) + '</span><span class="modal-row-val admin-vat-val" data-idx="' + idx + '">' + fmt(f.vatAmount) + '</span></div>' +
                            '<div class="modal-detail-row row-subtotal"><span class="modal-row-key">출발지 소계</span><span class="modal-row-val admin-subtotal-val" data-idx="' + idx + '">' + fmt(f.subtotal) + '</span></div>' +
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

            // admin 수동 입력 이벤트 바인딩
            if (isAdmin) {
                // 공장별 재계산 + 모달·총계 갱신 공통 함수
                function recalcAdminOverride(fidx) {
                    var f = data.factories[fidx];
                    if (!f) return;
                    var ov = window._adminFeeOverrides[fidx] || {};
                    var ship = ov.shippingFee != null ? ov.shippingFee : (f.shippingFee || 0);
                    var port = ov.portFee != null ? ov.portFee : (f.portFee || 0);
                    var cust = ov.customsFee != null ? ov.customsFee : (f.customsFee || 0);
                    var hs = ov.hsCodeFee != null ? ov.hsCodeFee : (f.hsCodeFee || 0);
                    var ins = f.insuranceFee || 0;
                    var supply = f.supplySubtotal || 0;

                    var newCif = supply + ship + ins;
                    var dutyRate = f.dutyRate || 0.13;
                    var newDuty = Math.round(newCif * dutyRate);
                    var newVat = Math.round((newCif + newDuty) * 0.10);
                    var newSubtotal = ship + port + cust + hs + ins + newDuty + newVat;

                    // 모달 내 해당 공장 갱신
                    var cifEl = modalBody.querySelector('.admin-cif-val[data-idx="' + fidx + '"]');
                    var dutyEl = modalBody.querySelector('.admin-duty-val[data-idx="' + fidx + '"]');
                    var vatEl = modalBody.querySelector('.admin-vat-val[data-idx="' + fidx + '"]');
                    var subEl = modalBody.querySelector('.admin-subtotal-val[data-idx="' + fidx + '"]');
                    if (cifEl) cifEl.textContent = fmt(newCif);
                    if (dutyEl) dutyEl.textContent = fmt(newDuty);
                    if (vatEl) vatEl.textContent = fmt(newVat);
                    if (subEl) subEl.textContent = fmt(newSubtotal);

                    // 모달 총 운임 합계 갱신
                    var totalLogistics = 0;
                    data.factories.forEach(function (ff, ii) {
                        var oov = window._adminFeeOverrides[ii] || {};
                        var ss = oov.shippingFee != null ? oov.shippingFee : (ff.shippingFee || 0);
                        var pp = oov.portFee != null ? oov.portFee : (ff.portFee || 0);
                        var cc = oov.customsFee != null ? oov.customsFee : (ff.customsFee || 0);
                        var hh = oov.hsCodeFee != null ? oov.hsCodeFee : (ff.hsCodeFee || 0);
                        var ii2 = ff.insuranceFee || 0;
                        var sp = ff.supplySubtotal || 0;
                        var cif2 = sp + ss + ii2;
                        var dr = ff.dutyRate || 0.13;
                        var d2 = Math.round(cif2 * dr);
                        var v2 = Math.round((cif2 + d2) * 0.10);
                        totalLogistics += ss + pp + cc + hh + ii2 + d2 + v2;
                    });
                    var mdTotalEl = el('md-logistics-total');
                    if (mdTotalEl) {
                        var logWithExtras = totalLogistics + (insuranceExtra || 0) + (domesticFee || 0);
                        mdTotalEl.textContent = fmt(logWithExtras);
                    }
                }

                modalBody.querySelectorAll('.admin-fee-input').forEach(function (input) {
                    // input 이벤트: 실시간 계산
                    input.addEventListener('input', function () {
                        var fidx = parseInt(input.dataset.idx);
                        var field = input.dataset.field;
                        var val = parseInt(input.value.replace(/[^\d]/g, '')) || 0;

                        if (!window._adminFeeOverrides[fidx]) window._adminFeeOverrides[fidx] = {};
                        window._adminFeeOverrides[fidx][field] = val;

                        recalcAdminOverride(fidx);
                    });
                    // blur 이벤트: 값 정리 (포맷팅)
                    input.addEventListener('blur', function () {
                        var val = parseInt(input.value.replace(/[^\d]/g, '')) || 0;
                        input.value = val;
                    });
                });
            }
        }

        // ── 대행 수수료 모달 상세 ──
        el('md-buyer-grade').textContent = data.buyerGrade || '-';
        el('md-item-total-krw').textContent = '₩' + fmtNum(itemTotalKrw);
        // 서비스 수수료 — 등급 할인 표시
        if (standardSvcFee > 0 && standardSvcFee > (data.serviceFee || 0)) {
            el('md-service-fee').innerHTML =
                '<span style="text-decoration:line-through;color:var(--color-text-light);font-size:13px;margin-right:8px;">' + fmt(standardSvcFee) + '</span>' + fmt(data.serviceFee);
        } else {
            el('md-service-fee').textContent = fmt(data.serviceFee);
        }
        if (standardSvcFee > 0 && standardSvcFee > (data.serviceFee || 0)) {
            var mdStdProc = standardSvcFee + inspectionExtra;
            el('md-procurement-total').innerHTML =
                '<span style="text-decoration:line-through;color:var(--color-text-light);margin-right:8px;">' + fmt(mdStdProc) + '</span>' + fmt(procurementWithExtras);
        } else {
            el('md-procurement-total').textContent = fmt(procurementWithExtras);
        }

        // ── 정책 기간 ──
        if (el('md-service-date')) {
            el('md-service-date').textContent = data.serviceFeeEffFrom
                ? '적용: ' + fmtPeriod(data.serviceFeeEffFrom, data.serviceFeeEffTo) : '';
        }
    };

    // ── 공급처 입력 모달 렌더링 ──────────────────────
    function renderFactoryInputModal() {
        var list = document.getElementById('factory-input-list');
        if (!list) return;
        list.innerHTML = '';

        var unknowns = window._unknownFactories || [];
        if (unknowns.length === 0) {
            list.innerHTML = '<p style="color:var(--color-text-muted);">미등록 공급처가 없습니다.</p>';
            return;
        }

        // 미등록 공장에 속한 stId 수집
        var unknownStIds = {};
        unknowns.forEach(function (f) {
            if (f.stIds) f.stIds.forEach(function (id) { unknownStIds[String(id)] = true; });
        });

        // 테이블 행에서 미등록 공장 상품만 브랜드별 그룹핑
        var rows = document.querySelectorAll('.quote-row');
        var brandMap = {};
        rows.forEach(function (row) {
            var stId = row.dataset.stId;
            if (!unknownStIds[stId]) return; // 미등록 공장 상품만
            var brandEl = row.querySelector('.product-material');
            var nameEl = row.querySelector('.product-name');
            var brand = brandEl ? brandEl.textContent.trim() : '알 수 없음';
            var name = nameEl ? nameEl.textContent.trim() : '';
            if (!brandMap[brand]) {
                brandMap[brand] = { brand: brand, products: [] };
            }
            brandMap[brand].products.push({ stId: stId, name: name });
        });

        Object.keys(brandMap).forEach(function (brandKey) {
            var info = brandMap[brandKey];
            var card = document.createElement('div');
            card.className = 'factory-input-card';

            var productList = info.products.map(function (p) {
                return '<span class="factory-input-product">' + p.name + '</span>';
            }).join('');

            card.innerHTML =
                '<div class="factory-input-card-header">' +
                    '<span class="factory-input-brand">' + info.brand + '</span>' +
                    '<span class="factory-input-count">' + info.products.length + '개 상품</span>' +
                '</div>' +
                '<div class="factory-input-products">' + productList + '</div>' +
                '<div class="factory-input-fields">' +
                    '<div class="factory-input-row">' +
                        '<label class="factory-input-label">공장명</label>' +
                        '<input type="text" class="factory-input-text" data-field="name" placeholder="공장명을 입력하세요" />' +
                    '</div>' +
                    '<div class="factory-input-row">' +
                        '<label class="factory-input-label">국가 코드</label>' +
                        '<select class="factory-input-select" data-field="country">' +
                            buildCountryOptions() +
                        '</select>' +
                    '</div>' +
                    '<div class="factory-input-row">' +
                        '<label class="factory-input-label">도시</label>' +
                        '<input type="text" class="factory-input-text" data-field="city" placeholder="도시명 (선택)" />' +
                    '</div>' +
                '</div>';

            list.appendChild(card);
        });
    }

    // 모달 열릴 때 렌더링
    var origOpen = window.openModal;
    window.openModal = function (type) {
        if (type === 'factory-input') renderFactoryInputModal();
        origOpen(type);
    };

    // ── 공급처 입력 적용 버튼 ────────────────────────
    var btnConfirm = document.getElementById('btnFactoryInputConfirm');
    if (btnConfirm) {
        btnConfirm.addEventListener('click', function () {
            var cards = document.querySelectorAll('#factory-input-list .factory-input-card');
            var requests = [];

            cards.forEach(function (card) {
                var name = card.querySelector('[data-field="name"]').value.trim();
                var country = card.querySelector('[data-field="country"]').value;
                var city = card.querySelector('[data-field="city"]').value.trim();

                if (!name || !country) return;

                // 이 브랜드에 해당하는 stId 목록 추출
                var brandName = card.querySelector('.factory-input-brand').textContent;
                var stIds = [];
                document.querySelectorAll('.quote-row').forEach(function (row) {
                    var brandEl = row.querySelector('.product-material');
                    if (brandEl && brandEl.textContent.trim() === brandName) {
                        stIds.push(parseInt(row.dataset.stId));
                    }
                });

                requests.push({ stIds: stIds, name: name, countryCode: country, city: city || null });
            });

            if (requests.length === 0) {
                alert('공장명과 국가 코드를 입력해주세요.');
                return;
            }

            btnConfirm.disabled = true;
            btnConfirm.textContent = '저장 중...';

            var csrfMeta = document.querySelector('meta[name="_csrf"]');
            var csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
            var headers = { 'Content-Type': 'application/json' };
            if (csrfHeaderMeta && csrfMeta) {
                headers[csrfHeaderMeta.content] = csrfMeta.content;
            }

            fetch('/api/quote/factory', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(requests)
            })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data.status === 'ok') {
                    closeModal('factory-input');
                    // estimate-fees 재호출하여 운임 재계산
                    if (typeof doFetchEstimateFees === 'function') {
                        doFetchEstimateFees();
                    }
                } else {
                    alert('저장 실패: ' + (data.message || ''));
                }
            })
            .catch(function (e) {
                alert('저장 실패: ' + e.message);
            })
            .finally(function () {
                btnConfirm.disabled = false;
                btnConfirm.textContent = '적용';
            });
        });
    }

    // ── admin "운임 적용" 버튼 ───────────────────────
    var btnApplyManual = document.getElementById('btnApplyManualFees');
    if (btnApplyManual && isAdmin) {
        btnApplyManual.addEventListener('click', function () {
            if (!_lastFeeCardsArgs || !_lastFeeCardsArgs.data || !_lastFeeCardsArgs.data.factories) return;

            var data = _lastFeeCardsArgs.data;
            var overrides = window._adminFeeOverrides || {};

            // lastEstimateData의 factories를 수동 값으로 패치
            data.factories.forEach(function (f, idx) {
                var ov = overrides[idx];
                if (!ov) return;

                if (ov.shippingFee != null) f.shippingFee = ov.shippingFee;
                if (ov.portFee != null) f.portFee = ov.portFee;
                if (ov.customsFee != null) f.customsFee = ov.customsFee;
                if (ov.hsCodeFee != null) f.hsCodeFee = ov.hsCodeFee;

                // CIF 재계산 (상품가 + 운임 + 보험)
                var supply = f.supplySubtotal || 0;
                var ship = f.shippingFee || 0;
                var ins = f.insuranceFee || 0;
                f.cifAmount = supply + ship + ins;

                // 관세/부가세 재계산 (그룹 CIF × 대표 관세율 — 근사치, 정확한 안분은 서버에서)
                var dutyRate = f.dutyRate || 0.13;
                f.dutyAmount = Math.round(f.cifAmount * dutyRate);
                f.vatAmount = Math.round((f.cifAmount + f.dutyAmount) * 0.10);

                // 소계 재계산
                f.subtotal = ship + (f.portFee || 0) + (f.customsFee || 0) + (f.hsCodeFee || 0)
                    + ins + f.dutyAmount + f.vatAmount;
            });

            // logisticsTotal 재계산
            var newLogistics = 0;
            var newDuty = 0;
            var newVat = 0;
            data.factories.forEach(function (f) {
                newLogistics += (f.subtotal || 0);
                newDuty += (f.dutyAmount || 0);
                newVat += (f.vatAmount || 0);
            });
            data.logisticsTotal = newLogistics;
            data.dutyAmount = newDuty;
            data.vatAmount = newVat;

            // updateFeeCards 재호출
            var a = _lastFeeCardsArgs;
            updateFeeCards(data, a.itemTotalKrw, a.dest, a.insuranceExtra, a.inspectionExtra, a.domesticFee, a.insuranceInfo, a.domesticData);

            // quote-write.js의 lastEstimateData도 갱신
            if (typeof window.lastEstimateData !== 'undefined') {
                window.lastEstimateData = data;
            }

            // 총 주문 명세 갱신 (quote-write.js의 grandTotal 재계산)
            var totalTax = newDuty + newVat;
            var intShipOnly = newLogistics - totalTax;
            var sdRate = parseFloat(data.shippingDiscountRate) || 0;
            var logisticsRaw = intShipOnly + (a.insuranceExtra || 0) + (a.domesticFee || 0);
            var logisticsDiscounted = sdRate > 0 ? Math.round(logisticsRaw * (1 - sdRate)) : logisticsRaw;
            var procurementVal = (data.procurementTotal || 0) + (a.inspectionExtra || 0);
            var grandTotal = (a.itemTotalKrw || 0) + logisticsDiscounted + procurementVal + totalTax;

            var gtEl = document.getElementById('os-grand-total');
            if (gtEl) gtEl.textContent = grandTotal > 0 ? '₩' + Math.round(grandTotal).toLocaleString('ko-KR') : '-';
            var logEl = document.getElementById('os-logistics');
            if (logEl) logEl.textContent = logisticsDiscounted > 0 ? '₩' + Math.round(logisticsDiscounted).toLocaleString('ko-KR') : '-';
            var taxEl = document.getElementById('os-tax');
            if (taxEl) taxEl.textContent = totalTax > 0 ? '₩' + Math.round(totalTax).toLocaleString('ko-KR') : '-';

            closeModal('logistics');
        });
    }
});
