/**
 * 페이지뷰 + 체류 시간 추적
 * layout.html에서 로드하여 모든 페이지에 적용
 */
(function () {
    var startTime = Date.now();
    var pvId = null;
    var dwellSent = false;

    function getParam(name) {
        var match = window.location.search.match(new RegExp('[?&]' + name + '=([^&]*)'));
        return match ? decodeURIComponent(match[1]) : null;
    }

    function detectPage() {
        var path = window.location.pathname;
        if (path.match(/^\/stock\/detail\//)) return { page: 'STOCK_DETAIL', refId: path.split('/').pop() };
        if (path === '/cart') return { page: 'CART', refId: null };
        if (path.match(/^\/quote\/write/)) return { page: 'QUOTE_WRITE', refId: getParam('quId') };
        if (path.match(/^\/quote\/detail/)) return { page: 'QUOTE_DETAIL', refId: getParam('quId') };
        if (path.match(/^\/payment\/check/)) return { page: 'PAYMENT_CHECK', refId: getParam('quId') };
        if (path.match(/^\/payment\/quote-detail/)) return { page: 'PAYMENT_COMPLETE', refId: getParam('quId') };
        if (path.match(/^\/stock/)) return { page: 'STOCK_LIST', refId: null };
        if (path.match(/^\/mypage/)) return { page: 'MYPAGE', refId: null };
        if (path.match(/^\/quote\/negotiation\/detail/)) return { page: 'NEGOTIATION_DETAIL', refId: getParam('ngId') };
        if (path.match(/^\/quote\/negotiation/)) return { page: 'NEGOTIATION_LIST', refId: null };
        if (path.match(/^\/quote\/list/)) return { page: 'QUOTE_LIST', refId: null };
        if (path === '/' || path === '/mainPage') return { page: 'MAIN', refId: null };
        if (path.match(/^\/notice\/detail/)) return { page: 'NOTICE_DETAIL', refId: path.split('/').pop() };
        if (path.match(/^\/notice/)) return { page: 'NOTICE_LIST', refId: null };
        if (path === '/about') return { page: 'ABOUT', refId: null };
        return null;
    }

    var pageInfo = detectPage();
    if (!pageInfo) return;

    // 새로고침 중복 방지 — 같은 세션·같은 페이지·같은 refId면 5초 내 재기록 차단
    var dedup = pageInfo.page + ':' + (pageInfo.refId || '');
    var lastKey = 'pv_last_' + dedup;
    var lastTime = parseInt(sessionStorage.getItem(lastKey) || '0');
    if (Date.now() - lastTime < 5000) return;
    sessionStorage.setItem(lastKey, String(Date.now()));

    fetch('/api/tracking/pageview', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ page: pageInfo.page, refId: pageInfo.refId || '' })
    })
    .then(function (res) { return res.json(); })
    .then(function (data) { pvId = data.pvId || null; })
    .catch(function () {});

    function sendDwell() {
        if (!pvId || dwellSent) return;
        var seconds = Math.round((Date.now() - startTime) / 1000);
        if (seconds < 1) return;
        dwellSent = true;

        var payload = JSON.stringify({ pvId: pvId, seconds: seconds });
        var blob = new Blob([payload], { type: 'application/json' });
        navigator.sendBeacon('/api/tracking/dwell', blob);
    }

    window.addEventListener('beforeunload', sendDwell);
    document.addEventListener('visibilitychange', function () {
        if (document.visibilityState === 'hidden') sendDwell();
    });
})();
