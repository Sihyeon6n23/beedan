/**
 * 페이지뷰 + 체류 시간 추적
 * layout.html에서 로드하여 모든 페이지에 적용
 */
(function () {
    var startTime = Date.now();
    var pvId = null;

    // 페이지 식별자 결정 (URL 기반)
    function detectPage() {
        var path = window.location.pathname;
        if (path.match(/^\/stock\/detail\//)) return { page: 'STOCK_DETAIL', refId: path.split('/').pop() };
        if (path === '/cart') return { page: 'CART', refId: null };
        if (path.match(/^\/quote\/write/)) return { page: 'QUOTE_WRITE', refId: null };
        if (path.match(/^\/quote\/detail/)) return { page: 'QUOTE_DETAIL', refId: null };
        if (path.match(/^\/admin\/quote\/write/)) return { page: 'ADMIN_QUOTE_WRITE', refId: null };
        if (path.match(/^\/admin\/quote\/detail/)) return { page: 'ADMIN_QUOTE_DETAIL', refId: null };
        if (path.match(/^\/payment\/check/)) return { page: 'PAYMENT_CHECK', refId: null };
        if (path.match(/^\/payment\/receipt/)) return { page: 'PAYMENT_RECEIPT', refId: null };
        if (path.match(/^\/stock/)) return { page: 'STOCK_LIST', refId: null };
        if (path.match(/^\/mypage/)) return { page: 'MYPAGE', refId: null };
        if (path.match(/^\/quote\/negotiation/)) return { page: 'NEGOTIATION', refId: null };
        if (path.match(/^\/quote\/list/)) return { page: 'QUOTE_LIST', refId: null };
        if (path === '/' || path === '/mainPage') return { page: 'MAIN', refId: null };
        return null; // 추적하지 않을 페이지
    }

    var pageInfo = detectPage();
    if (!pageInfo) return;

    // CSRF
    var csrfMeta = document.querySelector('meta[name="_csrf"]');
    var csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    var csrfToken = csrfMeta ? csrfMeta.content : '';
    var csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.content : '';

    // 페이지뷰 기록 (pvId 받아옴)
    var headers = { 'Content-Type': 'application/json' };
    if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

    fetch('/api/tracking/pageview', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({ page: pageInfo.page, refId: pageInfo.refId || '' })
    })
    .then(function (res) { return res.json(); })
    .then(function (data) { pvId = data.pvId || null; })
    .catch(function () {});

    // 페이지 이탈 시 체류 시간 전송
    function sendDwell() {
        if (!pvId) return;
        var seconds = Math.round((Date.now() - startTime) / 1000);
        if (seconds < 1) return;

        var payload = JSON.stringify({ pvId: pvId, seconds: seconds });

        // sendBeacon이 가장 안정적 (페이지 닫힐 때도 전송됨)
        if (navigator.sendBeacon) {
            var blob = new Blob([payload], { type: 'application/json' });
            navigator.sendBeacon('/api/tracking/dwell', blob);
        } else {
            // fallback
            var xhr = new XMLHttpRequest();
            xhr.open('POST', '/api/tracking/dwell', false); // 동기
            xhr.setRequestHeader('Content-Type', 'application/json');
            if (csrfHeader && csrfToken) xhr.setRequestHeader(csrfHeader, csrfToken);
            xhr.send(payload);
        }
    }

    window.addEventListener('beforeunload', sendDwell);
    document.addEventListener('visibilitychange', function () {
        if (document.visibilityState === 'hidden') sendDwell();
    });
})();
