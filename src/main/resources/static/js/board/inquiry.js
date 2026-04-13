/**
 * inquiry.js
 * 문의 목록, 작성 폼, 상세 화면 이벤트 제어
 */
(function () {
  initInquiryListPage();
  initInquiryEditorForm();
  initInquiryDetailActions();

  function initInquiryListPage() {
    const listPage = document.querySelector('.inquiry-list-page');
    if (!listPage) return;

    if (window.__inquiryListAsyncBound) return;
    window.__inquiryListAsyncBound = true;

    const listPathPattern = /^\/(admin\/)?inquiry\/list$/;
    const isInquiryListPath = (pathname) => listPathPattern.test(pathname || '');

    const replaceListPage = (htmlText) => {
      const parser = new DOMParser();
      const nextDocument = parser.parseFromString(htmlText, 'text/html');
      const nextListPage = nextDocument.querySelector('.inquiry-list-page');
      const currentListPage = document.querySelector('.inquiry-list-page');

      if (!nextListPage || !currentListPage) throw new Error('list_page_not_found');

      currentListPage.replaceWith(nextListPage);
      if (nextDocument.title) document.title = nextDocument.title;
    };

    const fetchListPage = async (url, options = {}) => {
      const { pushState = true } = options;
      const response = await fetch(url, { method: 'GET', headers: { 'X-Requested-With': 'XMLHttpRequest' } });
      if (!response.ok) throw new Error('list_request_failed');

      const htmlText = await response.text();
      replaceListPage(htmlText);

      if (pushState) {
        window.history.pushState({ inquiryList: true }, '', url);
      }
    };

    document.addEventListener('click', async (event) => {
      const currentListPage = document.querySelector('.inquiry-list-page');
      if (!currentListPage) return;

      const link = event.target.closest('.inquiry-filter[href], .inquiry-page-button[href]');
      if (!link || !currentListPage.contains(link)) return;

      event.preventDefault();
      try {
        await fetchListPage(link.href);
      } catch (error) {
        window.location.href = link.href;
      }
    });

    document.addEventListener('submit', async (event) => {
      const currentListPage = document.querySelector('.inquiry-list-page');
      if (!currentListPage) return;

      const form = event.target.closest('.inquiry-search-form');
      if (!form || !currentListPage.contains(form)) return;

      event.preventDefault();

      const url = new URL(form.action, window.location.origin);
      const formData = new FormData(form);
      formData.forEach((value, key) => {
        const stringValue = String(value).trim();
        if (stringValue) url.searchParams.set(key, stringValue);
      });

      try {
        await fetchListPage(url.toString());
      } catch (error) {
        window.location.href = url.toString();
      }
    });

    window.addEventListener('popstate', async () => {
      if (!isInquiryListPath(window.location.pathname)) return;
      if (!document.querySelector('.inquiry-list-page')) return;
      try {
        await fetchListPage(window.location.href, { pushState: false });
      } catch (error) {
        window.location.reload();
      }
    });
  }

  function initInquiryEditorForm() {
    const form = document.getElementById('inquiry-write-form');
    if (!form) return;

    const titleInput = form.querySelector('#brdTtl');
    const contentTextarea = form.querySelector('#brdCon');
    const titleError = form.querySelector('[data-inquiry-title-error]');
    const contentError = form.querySelector('[data-inquiry-content-error]');

    // 에디터/업로더 초기화
    if (window.InquiryUploader) {
      InquiryUploader.init();
    }

    const hideEditorErrors = () => {
      titleError?.classList.add('is-hidden');
      contentError?.classList.add('is-hidden');
    };

    const getEditorContent = () => {
      if (window.tinymce) {
        const editor = window.tinymce.get('brdCon');
        if (editor) return editor.getContent({ format: 'text' }).trim();
      }
      return (contentTextarea?.value || '').trim();
    };

    const validateEditorForm = () => {
      hideEditorErrors();
      const titleValue = (titleInput?.value || '').trim();
      const contentValue = getEditorContent();
      let hasError = false;

      if (!titleValue) {
        titleError?.classList.remove('is-hidden');
        titleInput?.focus();
        hasError = true;
      }

      if (!contentValue) {
        contentError?.classList.remove('is-hidden');
        if (!hasError) {
          const editor = window.tinymce?.get('brdCon');
          if (editor) {
            editor.focus();
          } else {
            contentTextarea?.focus();
          }
        }
        hasError = true;
      }
      return !hasError;
    };

    titleInput?.addEventListener('input', () => {
      if ((titleInput.value || '').trim()) titleError?.classList.add('is-hidden');
    });

    contentTextarea?.addEventListener('input', () => {
      if ((contentTextarea.value || '').trim()) contentError?.classList.add('is-hidden');
    });

    if (window.tinymce) {
      tinymce.init({
        selector: '#brdCon',
        height: 420,
        menubar: false,
        resize: false,
        plugins: 'lists link table wordcount preview',
        toolbar: 'blocks fontsize | bold italic underline strikethrough | forecolor backcolor | alignleft aligncenter alignright | bullist numlist | link table | removeformat',
        block_formats: '본문=p; 제목1=h2; 제목2=h3; 제목3=h4',
        font_size_formats: '10px 12px 14px 16px 18px 20px 24px 28px 36px',
        placeholder: '문의 내용을 자세히 입력해 주세요.',
        content_style: 'body { font-family: Inter, Noto Sans KR, sans-serif; font-size: 14px; color: #1a1c1c; }',
        branding: false,
        promotion: false,
        license_key: 'gpl',
        setup: (editor) => {
          editor.on('input change keyup setcontent', () => {
            if (editor.getContent({ format: 'text' }).trim()) contentError?.classList.add('is-hidden');
          });
        }
      });
    }

    form.addEventListener('submit', (event) => {
      if (window.tinymce) tinymce.triggerSave();

      // 전송 전 파일 Input 최종 동기화
      if (window.InquiryUploader) {
        const uploadRoot = form.querySelector('[data-inquiry-upload]');
        InquiryUploader.get(uploadRoot)?.syncToInput();
      }

      if (!validateEditorForm()) event.preventDefault();
    });
  }

  function initInquiryDetailActions(useCustomUpload = true) {
    const detailPage = document.querySelector('.inquiry-detail-page');
    if (!detailPage) return;

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

    const request = async (url, options = {}) => {
      const headers = { ...(options.headers || {}) };
      if (csrfToken) headers[csrfHeader] = csrfToken;
      const response = await fetch(url, { ...options, headers });
      if (!response.ok) throw new Error('request_failed');
      return response;
    };

    const confirmModal = detailPage.querySelector('[data-inquiry-modal="confirm"]');
    const formModal = detailPage.querySelector('[data-inquiry-modal="form"]');
    const confirmTitle = detailPage.querySelector('[data-inquiry-modal-title]');
    const confirmLead = detailPage.querySelector('[data-inquiry-modal-lead]');
    const confirmDescription = detailPage.querySelector('[data-inquiry-modal-description]');
    const confirmButton = detailPage.querySelector('[data-inquiry-modal-confirm]');
    const confirmCancelButton = confirmModal?.querySelector('.inquiry-modal__secondary[data-inquiry-modal-close]');

    const formTitle = detailPage.querySelector('[data-inquiry-form-title]');
    const formLead = detailPage.querySelector('[data-inquiry-form-lead]');
    const formDescription = detailPage.querySelector('[data-inquiry-form-description]');
    const formTextarea = detailPage.querySelector('[data-inquiry-form-textarea]');
    const formError = detailPage.querySelector('[data-inquiry-form-error]');
    const formSubmitButton = detailPage.querySelector('[data-inquiry-form-submit]');

    let pendingConfirmAction = null;
    let pendingFormAction = null;

    const handleError = (message = '처리 중 오류가 발생했습니다.') => {
      if (!confirmModal || !confirmTitle || !confirmLead || !confirmDescription || !confirmButton) {
        window.alert(message);
        return;
      }
      openConfirmModal({ title: '안내', lead: message, description: '잠시 후 다시 시도해 주세요.', confirmText: '확인', hideCancel: true, onConfirm: null });
    };

    const refreshDetailPage = async () => {
      const response = await fetch(window.location.href, { method: 'GET', headers: { 'X-Requested-With': 'XMLHttpRequest' } });
      if (!response.ok) throw new Error('detail_request_failed');
      const htmlText = await response.text();
      const parser = new DOMParser();
      const nextDocument = parser.parseFromString(htmlText, 'text/html');
      const nextDetailPage = nextDocument.querySelector('.inquiry-detail-page');
      const currentDetailPage = document.querySelector('.inquiry-detail-page');

      if (!nextDetailPage || !currentDetailPage) throw new Error('detail_page_not_found');

      currentDetailPage.replaceWith(nextDetailPage);
      if (nextDocument.title) document.title = nextDocument.title;
      initInquiryDetailActions(true);
    };

    const closeModals = () => {
      detailPage.querySelectorAll('.inquiry-modal').forEach(modal => {
        modal.classList.add('is-hidden');
        modal.setAttribute('aria-hidden', 'true');
      });
      pendingConfirmAction = null;
      pendingFormAction = null;
      if (confirmCancelButton) confirmCancelButton.classList.remove('is-hidden');
      if (formTextarea) formTextarea.value = '';
      if (formError) formError.classList.add('is-hidden');
    };

    const openConfirmModal = (options) => {
      if (!confirmModal) return;
      pendingConfirmAction = options.onConfirm || null;
      confirmTitle.textContent = options.title;
      confirmLead.textContent = options.lead;
      confirmDescription.textContent = options.description;
      confirmButton.textContent = options.confirmText;
      if (confirmCancelButton) confirmCancelButton.classList.toggle('is-hidden', Boolean(options.hideCancel));

      confirmModal.classList.remove('is-hidden');
      confirmModal.setAttribute('aria-hidden', 'false');
    };

    const openFormModal = (options) => {
      if (!formModal) return;
      pendingFormAction = options.onSubmit;
      formTitle.textContent = options.title;
      formLead.textContent = options.lead;
      formDescription.textContent = options.description;
      formSubmitButton.textContent = options.submitText;
      formTextarea.value = options.initialValue || '';
      formTextarea.placeholder = options.placeholder || '';
      formError.textContent = options.errorText || '내용을 입력해 주세요.';
      formError.classList.add('is-hidden');
      formModal.classList.remove('is-hidden');
      formModal.setAttribute('aria-hidden', 'false');
      setTimeout(() => formTextarea.focus(), 0);
    };

    detailPage.querySelectorAll('[data-inquiry-modal-close]').forEach(closer => {
      closer.addEventListener('click', closeModals);
    });

    if (confirmButton) {
      confirmButton.addEventListener('click', async () => {
        if (!pendingConfirmAction) {
          closeModals();
          return;
        }
        const action = pendingConfirmAction;
        closeModals();
        try { await action(); } catch (error) { handleError(); }
      });
    }

    if (formSubmitButton) {
      formSubmitButton.addEventListener('click', async () => {
        if (!pendingFormAction) return;
        const value = (formTextarea.value || '').trim();
        if (!value) {
          formError.classList.remove('is-hidden');
          formTextarea.focus();
          return;
        }
        const action = pendingFormAction;
        closeModals();
        try { await action(value); } catch (error) { handleError(); }
      });
    }

    const deleteSuccess = Number(detailPage.dataset.resultDeleteSuccess || 0);
    const uploadSuccess = Number(detailPage.dataset.resultUploadSuccess || 0);
    const failCount = Number(detailPage.dataset.resultFail || 0);
    const failReasons = (detailPage.dataset.resultFailReasons || '').split('||').map(v => v.trim()).filter(Boolean);

    const openBoardResultModal = (result, onConfirm = null) => {
      if (!result) return;
      const lines = [];
      if (result.deleteSuccess > 0) lines.push(`기존 파일 삭제: ${result.deleteSuccess}건`);
      if (result.uploadSuccess > 0) lines.push(`파일 업로드 성공: ${result.uploadSuccess}건`);
      if (result.fail > 0) lines.push(`파일 처리 실패: ${result.fail}건`);

      openConfirmModal({
        title: '처리 결과',
        lead: lines.join('\n'),
        description: result.failReason?.length ? result.failReason.join('\n') : '',
        confirmText: '확인',
        hideCancel: true,
        onConfirm
      });
    };

    if (deleteSuccess > 0 || uploadSuccess > 0 || failCount > 0) {
      openBoardResultModal({ deleteSuccess, uploadSuccess, fail: failCount, failReason: failReasons });
    }

    // 상태 변경 액션들
    const userCancelBtn = detailPage.querySelector('[data-inquiry-cancel-id]');
    if (userCancelBtn) {
      userCancelBtn.addEventListener('click', () => {
        openConfirmModal({
          title: '문의 취소', lead: '이 문의를 취소할까요?', description: '취소 후에는 다시 이전 상태로 되돌릴 수 없습니다.', confirmText: '확인',
          onConfirm: async () => {
            await request(`/api/inquiries/${userCancelBtn.dataset.inquiryCancelId}/cancel`, { method: 'PATCH' });
            window.location.href = '/inquiry/list';
          }
        });
      });
    }

    const adminProgressBtn = detailPage.querySelector('[data-inquiry-progress-id]');
    if (adminProgressBtn) {
      adminProgressBtn.addEventListener('click', () => {
        openConfirmModal({
          title: '처리 시작', lead: '이 문의를 처리중 상태로 전환할까요?', description: '확인 후 답변을 작성할 수 있습니다.', confirmText: '확인',
          onConfirm: async () => {
            await request(`/api/admin/inquiries/${adminProgressBtn.dataset.inquiryProgressId}/status?status=IN_PROGRESS`, { method: 'PATCH' });
            await refreshDetailPage();
          }
        });
      });
    }

    const adminCancelBtn = detailPage.querySelector('[data-inquiry-admin-cancel-id]');
    if (adminCancelBtn) {
      adminCancelBtn.addEventListener('click', () => {
        openFormModal({
          title: '문의 취소', lead: '취소 사유를 작성해 주세요.', description: '작성한 사유는 사용자에게 보여집니다.', submitText: '확인', placeholder: '예: 중복 문의로 확인되어 취소 처리합니다.', errorText: '취소 사유를 입력해 주세요.',
          onSubmit: async (reason) => {
            await request(`/api/admin/inquiries/${adminCancelBtn.dataset.inquiryAdminCancelId}/status?status=CANCELLED&brdCanRe=${encodeURIComponent(reason)}`, { method: 'PATCH' });
            await refreshDetailPage();
          }
        });
      });
    }

    // TinyMCE 및 폼 데이터 제어 로직
    const initInlineEditor = async (textarea, initialValue = '') => {
      if (!textarea) return null;
      if (!window.tinymce) { textarea.value = initialValue; return null; }
      const existing = window.tinymce.get(textarea.id);
      if (existing) { existing.setContent(initialValue || ''); existing.focus(); return existing; }
      if (!textarea.id) textarea.id = `inquiry-inline-editor-${Math.random().toString(36).slice(2, 10)}`;

      const editors = await tinymce.init({
        target: textarea, height: 320, menubar: false, resize: false, plugins: 'lists link table wordcount preview',
        toolbar: 'blocks fontsize | bold italic underline strikethrough | forecolor backcolor | alignleft aligncenter alignright | bullist numlist | link table | removeformat',
        block_formats: '본문=p; 제목1=h2; 제목2=h3; 제목3=h4', font_size_formats: '10px 12px 14px 16px 18px 20px 24px 28px 36px', placeholder: '답변 내용을 입력해 주세요.',
        content_style: 'body { font-family: Inter, Noto Sans KR, sans-serif; font-size: 14px; color: #1a1c1c; }', branding: false, promotion: false, license_key: 'gpl'
      });
      const editor = Array.isArray(editors) ? editors[0] : editors;
      editor.setContent(initialValue || '');
      return editor;
    };

    const removeInlineEditor = (textarea) => {
      if (!textarea || !window.tinymce) return;
      const editor = window.tinymce.get(textarea.id);
      if (editor) editor.remove();
    };

    const getInlineEditorContent = (textarea) => {
      if (!textarea) return '';
      if (window.tinymce) {
        const editor = window.tinymce.get(textarea.id);
        if (editor) return editor.getContent().trim();
      }
      return (textarea.value || '').trim();
    };

    // 답변 업로드용 공통 데이터 빌더 (분리된 파일 모듈 연동)
    const buildReplyFormData = (formRoot, content) => {
      const formData = new FormData();
      formData.append('brdCon', content);

      if (!formRoot || !window.InquiryUploader) return formData;

      const uploadRoot = formRoot.querySelector('[data-inquiry-upload]');
      const uploader = InquiryUploader.get(uploadRoot);

      if (uploader) {
        uploader.appendToFormData(formData);
      }
      return formData;
    };

    const inquiryId = detailPage.getAttribute('data-inquiry-id');

    // 답변 작성 바인딩
    const replyCreateBtn = detailPage.querySelector('[data-inquiry-reply-create-id]');
    const replyCreateForm = detailPage.querySelector('[data-reply-create-form]');
    const replyCreateEmpty = detailPage.querySelector('[data-reply-create-empty]');
    const replyCreateTextarea = detailPage.querySelector('[data-reply-create-textarea]');

    if (replyCreateBtn && replyCreateForm) {
      replyCreateBtn.addEventListener('click', async () => {
        if (replyCreateEmpty) replyCreateEmpty.classList.add('is-hidden');
        replyCreateForm.classList.remove('is-hidden');
        detailPage.querySelector('[data-reply-create-error]')?.classList.add('is-hidden');
        await initInlineEditor(replyCreateTextarea, '');
      });
    }

    const replyCreateCancel = detailPage.querySelector('[data-reply-create-cancel]');
    if (replyCreateCancel) {
      replyCreateCancel.addEventListener('click', () => {
        if (replyCreateForm) replyCreateForm.classList.add('is-hidden');
        if (replyCreateEmpty) replyCreateEmpty.classList.remove('is-hidden');
        removeInlineEditor(replyCreateTextarea);
      });
    }

    const replyCreateSubmit = detailPage.querySelector('[data-reply-create-submit]');
    if (replyCreateSubmit) {
      replyCreateSubmit.addEventListener('click', async () => {
        const createInquiryId = replyCreateSubmit.getAttribute('data-inquiry-reply-create-id');
        const content = getInlineEditorContent(replyCreateTextarea);
        const errorEl = detailPage.querySelector('[data-reply-create-error]');

        if (!content || content === '<p>&nbsp;</p>') {
          if (errorEl) errorEl.classList.remove('is-hidden');
          return;
        }

        try {
          const response = await request(`/api/admin/inquiries/${createInquiryId}/reply`, {
            method: 'POST',
            body: buildReplyFormData(replyCreateForm, content)
          });
          const result = await response.json();
          if (result.boardResultMessage) {
            openBoardResultModal(result.boardResultMessage, refreshDetailPage);
          } else {
            await refreshDetailPage();
          }
        } catch (error) { handleError(); }
      });
    }

    // 답변 수정 바인딩
    const bindReplyEditFormHandlers = () => {
      const currentReplyDisplay = detailPage.querySelector('[data-reply-display]');
      const currentReplyEditForm = detailPage.querySelector('[data-reply-edit-form]');
      const currentReplyEditTextarea = detailPage.querySelector('[data-reply-edit-textarea]');
      const currentReplyEditSubmit = detailPage.querySelector('[data-reply-edit-submit]');
      const currentReplyEditCancel = detailPage.querySelector('[data-reply-edit-cancel]');

      if (currentReplyEditCancel && currentReplyEditCancel.dataset.bound !== 'true') {
        currentReplyEditCancel.dataset.bound = 'true';
        currentReplyEditCancel.addEventListener('click', () => {
          if (currentReplyEditForm) currentReplyEditForm.classList.add('is-hidden');
          if (currentReplyDisplay) currentReplyDisplay.classList.remove('is-hidden');
          removeInlineEditor(currentReplyEditTextarea);
        });
      }

      if (currentReplyEditSubmit && currentReplyEditSubmit.dataset.bound !== 'true') {
        currentReplyEditSubmit.dataset.bound = 'true';
        currentReplyEditSubmit.addEventListener('click', async () => {
          const replyId = currentReplyEditSubmit.getAttribute('data-inquiry-reply-edit-id');
          const content = getInlineEditorContent(currentReplyEditTextarea);
          const errorEl = detailPage.querySelector('[data-reply-edit-error]');

          if (!content || content === '<p>&nbsp;</p>') {
            if (errorEl) errorEl.classList.remove('is-hidden');
            return;
          }

          try {
            const response = await request(`/api/admin/inquiries/${replyId}/reply/edit`, {
              method: 'POST',
              body: buildReplyFormData(currentReplyEditForm, content)
            });
            const result = await response.json();
            if (result.boardResultMessage) {
              openBoardResultModal(result.boardResultMessage, refreshDetailPage);
            } else {
              await refreshDetailPage();
            }
          } catch (error) { handleError(); }
        });
      }
    };

    const initialEditButton = detailPage.querySelector('[data-inquiry-reply-edit-id]');
    if (initialEditButton && initialEditButton.dataset.bound !== 'true') {
      initialEditButton.dataset.bound = 'true';
      initialEditButton.addEventListener('click', async () => {
        const display = detailPage.querySelector('[data-reply-display]');
        const editForm = detailPage.querySelector('[data-reply-edit-form]');
        const textarea = detailPage.querySelector('[data-reply-edit-textarea]');
        const replyContent = detailPage.querySelector('.inquiry-detail-reply__content');
        const currentContent = replyContent ? replyContent.innerHTML.trim() : '';

        if (display) display.classList.add('is-hidden');
        if (editForm) editForm.classList.remove('is-hidden');
        await initInlineEditor(textarea, currentContent);
      });
    }

    bindReplyEditFormHandlers();

    // 상세 페이지 내 업로더 모듈 초기화
    if (useCustomUpload && window.InquiryUploader) {
      InquiryUploader.init();
    }
  }
})();