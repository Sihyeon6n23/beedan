(function () {
  initInquiryListPage();
  initInquiryEditorForm();
  initInquiryDetailActions();

  function initInquiryListPage() {
    const listPage = document.querySelector('.inquiry-list-page');
    if (!listPage) {
      return;
    }

    if (window.__inquiryListAsyncBound) {
      return;
    }
    window.__inquiryListAsyncBound = true;

    const listPathPattern = /^\/(admin\/)?inquiry\/list$/;

    const isInquiryListPath = (pathname) => listPathPattern.test(pathname || '');

    const replaceListPage = (htmlText) => {
      const parser = new DOMParser();
      const nextDocument = parser.parseFromString(htmlText, 'text/html');
      const nextListPage = nextDocument.querySelector('.inquiry-list-page');
      const currentListPage = document.querySelector('.inquiry-list-page');

      if (!nextListPage || !currentListPage) {
        throw new Error('list_page_not_found');
      }

      currentListPage.replaceWith(nextListPage);
      if (nextDocument.title) {
        document.title = nextDocument.title;
      }
    };

    // 문의 목록 필터, 검색, 페이지네이션 비동기 처리
    // 같은 목록 URL의 HTML을 다시 받아 목록 영역만 교체
    const fetchListPage = async (url, options = {}) => {
      const { pushState = true } = options;
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'X-Requested-With': 'XMLHttpRequest'
        }
      });

      if (!response.ok) {
        throw new Error('list_request_failed');
      }

      const htmlText = await response.text();
      replaceListPage(htmlText);

      if (pushState) {
        window.history.pushState({ inquiryList: true }, '', url);
      }
    };

    document.addEventListener('click', async (event) => {
      const currentListPage = document.querySelector('.inquiry-list-page');
      if (!currentListPage) {
        return;
      }

      const link = event.target.closest('.inquiry-filter[href], .inquiry-page-button[href]');
      if (!link || !currentListPage.contains(link)) {
        return;
      }

      event.preventDefault();

      try {
        await fetchListPage(link.href);
      } catch (error) {
        window.location.href = link.href;
      }
    });

    document.addEventListener('submit', async (event) => {
      const currentListPage = document.querySelector('.inquiry-list-page');
      if (!currentListPage) {
        return;
      }

      const form = event.target.closest('.inquiry-search-form');
      if (!form || !currentListPage.contains(form)) {
        return;
      }

      event.preventDefault();

      const url = new URL(form.action, window.location.origin);
      const formData = new FormData(form);
      formData.forEach((value, key) => {
        const stringValue = String(value).trim();
        if (stringValue) {
          url.searchParams.set(key, stringValue);
        }
      });

      try {
        await fetchListPage(url.toString());
      } catch (error) {
        window.location.href = url.toString();
      }
    });

    window.addEventListener('popstate', async () => {
      if (!isInquiryListPath(window.location.pathname)) {
        return;
      }

      if (!document.querySelector('.inquiry-list-page')) {
        return;
      }

      try {
        await fetchListPage(window.location.href, { pushState: false });
      } catch (error) {
        window.location.reload();
      }
    });
  }

  function initInquiryEditorForm() {
    const form = document.getElementById('inquiry-write-form');
    if (!form) {
      return;
    }

    const titleInput = form.querySelector('#brdTtl');
    const contentTextarea = form.querySelector('#brdCon');
    const titleError = form.querySelector('[data-inquiry-title-error]');
    const contentError = form.querySelector('[data-inquiry-content-error]');

    const hideEditorErrors = () => {
      titleError?.classList.add('is-hidden');
      contentError?.classList.add('is-hidden');
    };

    const getEditorContent = () => {
      if (window.tinymce) {
        const editor = window.tinymce.get('brdCon');
        if (editor) {
          return editor.getContent({ format: 'text' }).trim();
        }
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
      if ((titleInput.value || '').trim()) {
        titleError?.classList.add('is-hidden');
      }
    });

    contentTextarea?.addEventListener('input', () => {
      if ((contentTextarea.value || '').trim()) {
        contentError?.classList.add('is-hidden');
      }
    });

    if (window.tinymce) {
      tinymce.init({
        selector: '#brdCon',
        height: 420,
        menubar: false,
        resize: false,
        plugins: 'lists link table wordcount preview',
        toolbar:
          'blocks fontsize | bold italic underline strikethrough | forecolor backcolor | ' +
          'alignleft aligncenter alignright | bullist numlist | link table | removeformat',
        block_formats: '본문=p; 제목1=h2; 제목2=h3; 제목3=h4',
        font_size_formats: '10px 12px 14px 16px 18px 20px 24px 28px 36px',
        placeholder: '문의 내용을 자세히 입력해 주세요.',
        content_style: 'body { font-family: Inter, Noto Sans KR, sans-serif; font-size: 14px; color: #1a1c1c; }',
        branding: false,
        promotion: false,
        license_key: 'gpl',
        setup: (editor) => {
          editor.on('input change keyup setcontent', () => {
            if (editor.getContent({ format: 'text' }).trim()) {
              contentError?.classList.add('is-hidden');
            }
          });
        }
      });
    }

    form.addEventListener('submit', (event) => {
      if (window.tinymce) {
        tinymce.triggerSave();
      }
      if (!validateEditorForm()) {
        event.preventDefault();
      }
    });

    // 첨부파일 업로드는 fileUpload fragment가 자체적으로 처리
    return;

    const uploadRoot = document.querySelector('[data-inquiry-upload]');
    if (!uploadRoot) {
      form.addEventListener('submit', (event) => {
        if (window.tinymce) {
          tinymce.triggerSave();
        }
        if (!validateEditorForm()) {
          event.preventDefault();
        }
      });
      return;
    }

    const uploadZone = uploadRoot.querySelector('[data-upload-zone]');
    const fileInput = uploadRoot.querySelector('[data-upload-input]');
    const fileList = uploadRoot.querySelector('[data-upload-list]');
    let selectedFiles = [];

    const formatFileSize = (size) => {
      if (size < 1024) return `${size} B`;
      if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
      return `${(size / (1024 * 1024)).toFixed(1)} MB`;
    };

    const syncFileInput = () => {
      const dataTransfer = new DataTransfer();
      selectedFiles.forEach((file) => dataTransfer.items.add(file));
      fileInput.files = dataTransfer.files;
    };

    const renderFileList = () => {
      if (!selectedFiles.length) {
        fileList.innerHTML = '';
        syncFileInput();
        return;
      }

      fileList.innerHTML = selectedFiles.map((file, index) => `
        <div class="inquiry-upload__item">
          <span class="inquiry-upload__item-name">
            <span class="material-symbols-outlined">description</span>
            <span class="inquiry-upload__item-meta">${file.name} <small>(${formatFileSize(file.size)})</small></span>
          </span>
          <button type="button" class="inquiry-upload__item-remove" data-remove-file="${index}" aria-label="첨부 파일 삭제">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
      `).join('');

      syncFileInput();
    };

    const addFiles = (files) => {
      Array.from(files).forEach((file) => {
        const duplicated = selectedFiles.some((selected) =>
          selected.name === file.name &&
          selected.size === file.size &&
          selected.lastModified === file.lastModified
        );
        if (!duplicated) {
          selectedFiles.push(file);
        }
      });
      renderFileList();
    };

    uploadZone.addEventListener('click', () => fileInput.click());
    fileInput.addEventListener('change', (event) => addFiles(event.target.files));
    uploadZone.addEventListener('dragover', (event) => {
      event.preventDefault();
      uploadZone.classList.add('is-dragover');
    });
    uploadZone.addEventListener('dragleave', () => uploadZone.classList.remove('is-dragover'));
    uploadZone.addEventListener('drop', (event) => {
      event.preventDefault();
      uploadZone.classList.remove('is-dragover');
      addFiles(event.dataTransfer.files);
    });
    fileList.addEventListener('click', (event) => {
      const removeButton = event.target.closest('[data-remove-file]');
      if (!removeButton) {
        return;
      }
      const index = Number(removeButton.getAttribute('data-remove-file'));
      selectedFiles.splice(index, 1);
      renderFileList();
    });

    form.addEventListener('submit', (event) => {
      if (window.tinymce) {
        tinymce.triggerSave();
      }
      syncFileInput();
      if (!validateEditorForm()) {
        event.preventDefault();
      }
    });
  }

  function initInquiryDetailActions(useCustomUpload = true) {
    const detailPage = document.querySelector('.inquiry-detail-page');
    if (!detailPage) {
      return;
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

    // 상세 액션 공통 비동기 요청 처리
    // CSRF 헤더를 붙여 취소, 상태 변경, 답변 작성/수정 요청을 같은 방식으로 보냄
    const request = async (url, options = {}) => {
      const headers = { ...(options.headers || {}) };
      if (csrfToken) {
        headers[csrfHeader] = csrfToken;
      }
      const response = await fetch(url, { ...options, headers });
      if (!response.ok) {
        throw new Error('request_failed');
      }
      return response;
    };

    const handleError = (message = '처리 중 오류가 발생했습니다.') => {
      if (!confirmModal || !confirmTitle || !confirmLead || !confirmDescription || !confirmButton) {
        window.alert(message);
        return;
      }
      openConfirmModal({
        title: '안내',
        lead: message,
        description: '잠시 후 다시 시도해 주세요.',
        confirmText: '확인',
        hideCancel: true,
        onConfirm: null
      });
    };

    // 상세 화면 부분 갱신 처리
    // 상태 변경 후에는 현재 상세 HTML만 다시 받아와 상세 영역만 교체
    const refreshDetailPage = async () => {
      const response = await fetch(window.location.href, {
        method: 'GET',
        headers: {
          'X-Requested-With': 'XMLHttpRequest'
        }
      });
      if (!response.ok) {
        throw new Error('detail_request_failed');
      }
      const htmlText = await response.text();
      const parser = new DOMParser();
      const nextDocument = parser.parseFromString(htmlText, 'text/html');
      const nextDetailPage = nextDocument.querySelector('.inquiry-detail-page');
      const currentDetailPage = document.querySelector('.inquiry-detail-page');
      if (!nextDetailPage || !currentDetailPage) {
        throw new Error('detail_page_not_found');
      }
      currentDetailPage.replaceWith(nextDetailPage);
      if (nextDocument.title) {
        document.title = nextDocument.title;
      }
      initInquiryDetailActions(true);
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
    const modalClosers = detailPage.querySelectorAll('[data-inquiry-modal-close]');

    const initReplyUploadRoot = (uploadRoot) => {
      if (!uploadRoot || uploadRoot.dataset.bound === 'true') {
        return;
      }

      const uploadZone = uploadRoot.querySelector('.inquiry-upload__zone');
      const fileInput = uploadRoot.querySelector('input[name="newFiles"]');
      const fileListContainer = uploadRoot.querySelector('.inquiry-upload__list');
      const countInfo = uploadRoot.querySelector('#fileCountInfo');

      if (!uploadZone || !fileInput || !fileListContainer || !countInfo) {
        return;
      }

      uploadRoot.dataset.bound = 'true';

      const maxMatch = String(countInfo.textContent || '').match(/\/\s*(\d+)\)/);
      const maxCount = maxMatch ? Number(maxMatch[1]) : (fileInput.multiple ? 5 : 1);
      let selectedFiles = [];
      uploadRoot._getSelectedFiles = () => selectedFiles.slice();

      const formatFileSize = (size) => {
        if (!Number.isFinite(size) || size <= 0) {
          return '0 B';
        }
        if (size < 1024) {
          return `${size} B`;
        }
        if (size < 1024 * 1024) {
          return `${(size / 1024).toFixed(1)} KB`;
        }
        return `${(size / (1024 * 1024)).toFixed(1)} MB`;
      };

      const syncFileInput = () => {
        const dataTransfer = new DataTransfer();
        selectedFiles.forEach((file) => dataTransfer.items.add(file));
        fileInput.files = dataTransfer.files;
      };

      const getExistingCount = () => fileListContainer.querySelectorAll('.inquiry-upload__item.existing:not(.to-delete)').length;

      const updateCount = () => {
        countInfo.textContent = `(${selectedFiles.length + getExistingCount()} / ${maxCount})`;
      };

      const removeNewFile = (targetName, targetSize, targetLastModified, element) => {
        selectedFiles = selectedFiles.filter((file) => !(
          file.name === targetName
          && file.size === targetSize
          && file.lastModified === targetLastModified
        ));
        syncFileInput();
        element?.remove();
        updateCount();
      };

      const renderNewFile = (file) => {
        const item = document.createElement('div');
        item.className = 'inquiry-upload__item new flex items-center justify-between gap-3 p-3 mb-2 border-l-4 border-primary bg-white shadow-sm';

        const fileName = file.name;
        const fileSize = file.size;
        const lastModified = file.lastModified;

        item.innerHTML = `
          <div class="flex items-center gap-2 overflow-hidden flex-1">
            <span class="material-symbols-outlined text-secondary !text-lg flex-shrink-0">description</span>
            <span class="text-sm font-medium text-on-background truncate">
              ${fileName}
              <small class="text-zinc-400 ml-1">(${formatFileSize(fileSize)})</small>
            </span>
            <mark class="badge-new bg-primary-container text-on-primary-container text-[10px] font-black px-1.5 py-0.5 uppercase tracking-wider flex-shrink-0">New</mark>
          </div>
          <button type="button" class="inquiry-upload__remove text-secondary hover:text-error transition-colors flex-shrink-0 ml-3" aria-label="첨부 파일 삭제">
            <span class="material-symbols-outlined !text-lg">close</span>
          </button>
        `;

        item.querySelector('button')?.addEventListener('click', () => {
          removeNewFile(fileName, fileSize, lastModified, item);
        });

        fileListContainer.appendChild(item);
      };

      const processFiles = (files) => {
        if (!files?.length) {
          return;
        }

        const remaining = maxCount - (selectedFiles.length + getExistingCount());
        if (remaining <= 0) {
          window.alert(`최대 ${maxCount}개까지 첨부할 수 있습니다.`);
          return;
        }

        Array.from(files).slice(0, remaining).forEach((file) => {
          const duplicated = selectedFiles.some((selected) =>
            selected.name === file.name
            && selected.size === file.size
            && selected.lastModified === file.lastModified
          );

          if (!duplicated) {
            selectedFiles.push(file);
            renderNewFile(file);
          }
        });

        syncFileInput();
        updateCount();
        fileInput.value = '';
      };

      uploadZone.addEventListener('click', () => fileInput.click());
      fileInput.addEventListener('click', (event) => event.stopPropagation());
      uploadZone.addEventListener('dragover', (event) => {
        event.preventDefault();
        uploadZone.classList.add('is-dragover');
      });
      uploadZone.addEventListener('dragleave', () => uploadZone.classList.remove('is-dragover'));
      uploadZone.addEventListener('drop', (event) => {
        event.preventDefault();
        uploadZone.classList.remove('is-dragover');
        processFiles(event.dataTransfer.files);
      });
      fileInput.addEventListener('change', (event) => processFiles(event.target.files));

      fileListContainer.querySelectorAll('.inquiry-upload__item.existing').forEach((item) => {
        const deleteInput = item.querySelector('.delete-input');
        const removeButton = item.querySelector('.inquiry-upload__remove');
        if (!deleteInput || !removeButton) {
          return;
        }

        removeButton.onclick = null;
        removeButton.addEventListener('click', () => {
          const isDeleting = !item.classList.contains('to-delete');
          item.classList.toggle('to-delete', isDeleting);
          item.style.opacity = isDeleting ? '0.5' : '1';
          deleteInput.name = isDeleting ? 'deleteUuids' : '';
          deleteInput.value = isDeleting ? (deleteInput.dataset.uuid || '') : '';
          updateCount();
        });
      });

      updateCount();
    };

    const initReplyUploadRoots = () => {
      detailPage.querySelectorAll('[data-inquiry-upload]').forEach((uploadRoot) => {
        initReplyUploadRoot(uploadRoot);
      });
    };

    let pendingConfirmAction = null;
    let pendingFormAction = null;

    const closeModals = () => {
      detailPage.querySelectorAll('.inquiry-modal').forEach((modal) => {
        modal.classList.add('is-hidden');
        modal.setAttribute('aria-hidden', 'true');
      });
      pendingConfirmAction = null;
      pendingFormAction = null;
      if (confirmCancelButton) {
        confirmCancelButton.classList.remove('is-hidden');
      }
      if (formTextarea) {
        formTextarea.value = '';
      }
      if (formError) {
        formError.classList.add('is-hidden');
      }
    };

    const openConfirmModal = (options) => {
      if (!confirmModal) {
        return;
      }
      pendingConfirmAction = options.onConfirm || null;
      confirmTitle.textContent = options.title;
      confirmLead.textContent = options.lead;
      confirmDescription.textContent = options.description;
      confirmButton.textContent = options.confirmText;
      if (confirmCancelButton) {
        confirmCancelButton.classList.toggle('is-hidden', Boolean(options.hideCancel));
      }
      confirmModal.classList.remove('is-hidden');
      confirmModal.setAttribute('aria-hidden', 'false');
    };

    const openGuideModal = (message, onConfirm = null) => {
      openConfirmModal({
        title: '안내',
        lead: message,
        description: '',
        confirmText: '확인',
        hideCancel: true,
        onConfirm
      });
    };

    const openFormModal = (options) => {
      if (!formModal) {
        return;
      }
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

    modalClosers.forEach((closer) => {
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
        try {
          await action();
        } catch (error) {
          handleError();
        }
      });
    }

    if (formSubmitButton) {
      formSubmitButton.addEventListener('click', async () => {
        if (!pendingFormAction) {
          return;
        }
        const value = (formTextarea.value || '').trim();
        if (!value) {
          formError.classList.remove('is-hidden');
          formTextarea.focus();
          return;
        }
        const action = pendingFormAction;
        closeModals();
        try {
          await action(value);
        } catch (error) {
          handleError();
        }
      });
    }

    const deleteSuccess = Number(detailPage.dataset.resultDeleteSuccess || 0);
    const uploadSuccess = Number(detailPage.dataset.resultUploadSuccess || 0);
    const failCount = Number(detailPage.dataset.resultFail || 0);
    const failReasons = (detailPage.dataset.resultFailReasons || '')
      .split('||')
      .map((value) => value.trim())
      .filter(Boolean);

    const openBoardResultModal = (result, onConfirm = null) => {
        if (!result) {
          return;
        }

        const lines = [];

        if (result.deleteSuccess > 0) {
            lines.push(`기존 파일 삭제: ${result.deleteSuccess}건`);
        }
        if (result.uploadSuccess > 0) {
            lines.push(`파일 업로드 성공: ${result.uploadSuccess}건`);
        }
        if (result.fail > 0) {
            lines.push(`파일 처리 실패: ${result.fail}건`);
        }

        const description = result.failReason.length
          ? result.failReason.join('\n')
          : '';

        openConfirmModal({
            title: '처리 결과',
            lead: lines.join('\n'),
            description,
            confirmText: '확인',
            hideCancel: true,
            onConfirm
        });
    };

    if (deleteSuccess > 0 || uploadSuccess > 0 || failCount > 0) {
        openBoardResultModal({
          deleteSuccess,
          uploadSuccess,
          fail: failCount,
          failReason: failReasons
        });
    }

    // 사용자 문의 취소 비동기 처리
    const userCancelButton = detailPage.querySelector('[data-inquiry-cancel-id]');
    if (userCancelButton) {
      userCancelButton.addEventListener('click', () => {
        const inquiryId = userCancelButton.getAttribute('data-inquiry-cancel-id');
        openConfirmModal({
          title: '문의 취소',
          lead: '이 문의를 취소할까요?',
          description: '취소 후에는 다시 이전 상태로 되돌릴 수 없습니다.',
          confirmText: '확인',
          onConfirm: async () => {
            await request(`/api/inquiries/${inquiryId}/cancel`, { method: 'PATCH' });
            window.location.href = '/inquiry/list';
          }
        });
      });
    }

    // 관리자 문의 상태 변경 비동기 처리
    const adminProgressButton = detailPage.querySelector('[data-inquiry-progress-id]');
    if (adminProgressButton) {
      adminProgressButton.addEventListener('click', () => {
        const inquiryId = adminProgressButton.getAttribute('data-inquiry-progress-id');
        openConfirmModal({
          title: '처리 시작',
          lead: '이 문의를 처리중 상태로 전환할까요?',
          description: '확인 후 답변을 작성할 수 있습니다.',
          confirmText: '확인',
          onConfirm: async () => {
            await request(`/api/admin/inquiries/${inquiryId}/status?status=IN_PROGRESS`, { method: 'PATCH' });
            await refreshDetailPage();
          }
        });
      });
    }

    // 관리자 문의 취소 비동기 처리
    const adminCancelButton = detailPage.querySelector('[data-inquiry-admin-cancel-id]');
    if (adminCancelButton) {
      adminCancelButton.addEventListener('click', () => {
        const inquiryId = adminCancelButton.getAttribute('data-inquiry-admin-cancel-id');
        openFormModal({
          title: '문의 취소',
          lead: '취소 사유를 작성해 주세요.',
          description: '작성한 사유는 사용자에게 보여집니다.',
          submitText: '확인',
          placeholder: '예: 중복 문의로 확인되어 취소 처리합니다.',
          errorText: '취소 사유를 입력해 주세요.',
          onSubmit: async (reason) => {
            await request(`/api/admin/inquiries/${inquiryId}/status?status=CANCELLED&brdCanRe=${encodeURIComponent(reason)}`, { method: 'PATCH' });
            await refreshDetailPage();
          }
        });
      });
    }

    const inquiryId = detailPage.getAttribute('data-inquiry-id');
    const replyCreateButton = detailPage.querySelector('[data-inquiry-reply-create-id]');
    const replyCreateBody = detailPage.querySelector('[data-reply-create-body]');
    const replyCreateEmpty = detailPage.querySelector('[data-reply-create-empty]');
    const replyCreateForm = detailPage.querySelector('[data-reply-create-form]');
    const replyCreateTextarea = detailPage.querySelector('[data-reply-create-textarea]');
    const replyCreateError = detailPage.querySelector('[data-reply-create-error]');
    const replyCreateSubmit = detailPage.querySelector('[data-reply-create-submit]');
    const replyCreateCancel = detailPage.querySelector('[data-reply-create-cancel]');
    const replyDisplay = detailPage.querySelector('[data-reply-display]');
    const replyEditForm = detailPage.querySelector('[data-reply-edit-form]');
    const replyEditTextarea = detailPage.querySelector('[data-reply-edit-textarea]');
    const replyEditError = detailPage.querySelector('[data-reply-edit-error]');
    const replyEditSubmit = detailPage.querySelector('[data-reply-edit-submit]');
    const replyEditCancel = detailPage.querySelector('[data-reply-edit-cancel]');

    // 관리자 답변 조회 비동기 처리
    // 답변 저장 직후 최신 답변 DTO를 다시 읽어와 작성일, 수정 여부, 본문을 즉시 반영
    const fetchReplyDto = async () => {
      if (!inquiryId) {
        return null;
      }
      const response = await request(`/api/admin/inquiries/${inquiryId}/reply`, { method: 'GET' });
      return response.json();
    };

    const renderReplyMeta = (reply) => `
      <div class="inquiry-detail-reply__meta">
        <strong>BEEDAN</strong>
        <span>${String(reply.brdCreDt || '').slice(0, 10).replace(/-/g, '.')}</span>
        ${reply.edited ? '<span>(수정됨)</span>' : ''}
      </div>
    `;
    const renderReplyContent = (reply) => `${renderReplyMeta(reply)}<div class="inquiry-detail-reply__content">${reply.brdCon}</div>`;

    // 관리자 답변 작성/수정 인라인 에디터 처리
    // 상세 안 인라인 폼에 TinyMCE를 붙이고, 이미 있으면 재사용
    const initInlineEditor = async (textarea, initialValue = '') => {
      if (!textarea) {
        return null;
      }
      if (!window.tinymce) {
        textarea.value = initialValue;
        return null;
      }
      const existing = window.tinymce.get(textarea.id);
      if (existing) {
        existing.setContent(initialValue || '');
        existing.focus();
        return existing;
      }
      if (!textarea.id) {
        textarea.id = `inquiry-inline-editor-${Math.random().toString(36).slice(2, 10)}`;
      }
      const editors = await tinymce.init({
        target: textarea,
        height: 320,
        menubar: false,
        resize: false,
        plugins: 'lists link table wordcount preview',
        toolbar:
          'blocks fontsize | bold italic underline strikethrough | forecolor backcolor | ' +
          'alignleft aligncenter alignright | bullist numlist | link table | removeformat',
        block_formats: '본문=p; 제목1=h2; 제목2=h3; 제목3=h4',
        font_size_formats: '10px 12px 14px 16px 18px 20px 24px 28px 36px',
        placeholder: '답변 내용을 입력해 주세요.',
        content_style: 'body { font-family: Inter, Noto Sans KR, sans-serif; font-size: 14px; color: #1a1c1c; }',
        branding: false,
        promotion: false,
        license_key: 'gpl'
      });
      const editor = Array.isArray(editors) ? editors[0] : editors;
      editor.setContent(initialValue || '');
      return editor;
    };

    const removeInlineEditor = (textarea) => {
      if (!textarea || !window.tinymce) {
        return;
      }
      const editor = window.tinymce.get(textarea.id);
      if (editor) {
        editor.remove();
      }
    };

    const getInlineEditorContent = (textarea) => {
      if (!textarea) {
        return '';
      }
      if (window.tinymce) {
        const editor = window.tinymce.get(textarea.id);
        if (editor) {
          return editor.getContent().trim();
        }
      }
      return (textarea.value || '').trim();
    };

    // 답글 작성/수정은 파일 첨부를 같이 보내야 하므로 JSON 대신 FormData로 전송
    const buildReplyFormData = (formRoot, content) => {
      const formData = new FormData();
      formData.append('brdCon', content);

      if (!formRoot) {
        return formData;
      }

      const uploadRoot = formRoot.querySelector('[data-inquiry-upload]');
      const selectedFiles = typeof uploadRoot?._getSelectedFiles === 'function'
        ? uploadRoot._getSelectedFiles()
        : [];
      const fileInput = formRoot.querySelector('input[name="newFiles"]');

      if (selectedFiles.length) {
        selectedFiles.forEach((file) => formData.append('newFiles', file));
      } else if (fileInput?.files?.length) {
        Array.from(fileInput.files).forEach((file) => formData.append('newFiles', file));
      }

      const deleteInputs = formRoot.querySelectorAll('input[name="deleteUuids"]');
      deleteInputs.forEach((input) => {
        if (input.value) {
          formData.append('deleteUuids', input.value);
        }
      });

      return formData;
    };

    const ensureEditButton = (replyId) => {
      const replyHeader = detailPage.querySelector('.inquiry-detail-reply__header');
      if (!replyHeader) {
        return null;
      }

      let editButton = replyHeader.querySelector('[data-inquiry-reply-edit-id]');

      if (editButton) {
        const freshEditButton = editButton.cloneNode(true);
        editButton.replaceWith(freshEditButton);
        editButton = freshEditButton;
      } else {
        const createButton = replyHeader.querySelector('[data-inquiry-reply-create-id]');
        if (!createButton) {
          return null;
        }
        editButton = createButton.cloneNode(true);
        createButton.replaceWith(editButton);
      }

      editButton.type = 'button';
      editButton.className = 'inquiry-detail-action';
      editButton.textContent = '답변 수정';
      editButton.removeAttribute('data-inquiry-reply-create-id');
      editButton.setAttribute('data-inquiry-reply-edit-id', replyId);
      delete editButton.dataset.bound;

      return editButton;
    };

    const bindReplyEditFormHandlers = () => {
    const currentReplyDisplay = detailPage.querySelector('[data-reply-display]');
    const currentReplyEditForm = detailPage.querySelector('[data-reply-edit-form]');
    const currentReplyEditTextarea = detailPage.querySelector('[data-reply-edit-textarea]');
    const currentReplyEditError = detailPage.querySelector('[data-reply-edit-error]');
    const currentReplyEditSubmit = detailPage.querySelector('[data-reply-edit-submit]');
    const currentReplyEditCancel = detailPage.querySelector('[data-reply-edit-cancel]');

      if (currentReplyEditCancel && currentReplyEditCancel.dataset.bound !== 'true') {
        currentReplyEditCancel.dataset.bound = 'true';
        currentReplyEditCancel.addEventListener('click', () => {
          if (currentReplyEditForm) {
            currentReplyEditForm.classList.add('is-hidden');
          }
          if (currentReplyDisplay) {
            currentReplyDisplay.classList.remove('is-hidden');
          }
          if (currentReplyEditError) {
            currentReplyEditError.classList.add('is-hidden');
          }
          removeInlineEditor(currentReplyEditTextarea);
        });
      }

      if (currentReplyEditSubmit && currentReplyEditSubmit.dataset.bound !== 'true') {
        currentReplyEditSubmit.dataset.bound = 'true';
        currentReplyEditSubmit.addEventListener('click', async () => {
          const replyId = currentReplyEditSubmit.getAttribute('data-inquiry-reply-edit-id');
          const content = getInlineEditorContent(currentReplyEditTextarea);
          if (!content || content === '<p>&nbsp;</p>') {
            if (currentReplyEditError) {
              currentReplyEditError.classList.remove('is-hidden');
            }
            return;
          }

          try {
            // 답글 수정은 첨부 삭제/추가를 같이 보내기 위해 multipart/form-data로 전송
            const response = await request(`/api/admin/inquiries/${replyId}/reply/edit`, {
                method: 'POST',
                body: buildReplyFormData(currentReplyEditForm, content)
            });
            const result = await response.json();

            if (result.boardResultMessage) {
                openBoardResultModal(result.boardResultMessage, async () => {
                  await refreshDetailPage();
                });
            } else {
                await refreshDetailPage();
            }
          } catch (error) {
            handleError();
          }
        });
      }
    };

    const bindReplyEditButton = (button) => {
      if (!button || button.dataset.bound === 'true') {
        return;
      }
      button.dataset.bound = 'true';
      button.addEventListener('click', async () => {
        const currentReplyDisplay = detailPage.querySelector('[data-reply-display]');
        const currentReplyEditForm = detailPage.querySelector('[data-reply-edit-form]');
        const currentReplyEditTextarea = detailPage.querySelector('[data-reply-edit-textarea]');
        const currentReplyEditError = detailPage.querySelector('[data-reply-edit-error]');
        const replyContent = detailPage.querySelector('.inquiry-detail-reply__content');
        const currentContent = replyContent ? replyContent.innerHTML.trim() : '';

        if (currentReplyDisplay) {
          currentReplyDisplay.classList.add('is-hidden');
        }
        if (currentReplyEditForm) {
          currentReplyEditForm.classList.remove('is-hidden');
        }
        if (currentReplyEditError) {
          currentReplyEditError.classList.add('is-hidden');
        }
        await initInlineEditor(currentReplyEditTextarea, currentContent);
      });
    };

    if (replyCreateButton && replyCreateForm) {
      replyCreateButton.addEventListener('click', async () => {
        if (replyCreateEmpty) {
          replyCreateEmpty.classList.add('is-hidden');
        }
        replyCreateForm.classList.remove('is-hidden');
        if (replyCreateError) {
          replyCreateError.classList.add('is-hidden');
        }
        await initInlineEditor(replyCreateTextarea, '');
      });
    }

    if (replyCreateCancel) {
      replyCreateCancel.addEventListener('click', () => {
        if (replyCreateForm) {
          replyCreateForm.classList.add('is-hidden');
        }
        if (replyCreateEmpty) {
          replyCreateEmpty.classList.remove('is-hidden');
        }
        if (replyCreateError) {
          replyCreateError.classList.add('is-hidden');
        }
        removeInlineEditor(replyCreateTextarea);
        if (replyCreateTextarea) {
          replyCreateTextarea.value = '';
        }
      });
    }

    // 관리자 답변 작성 비동기 처리
    // 첫 등록 시 안내 영역을 숨기고 실제 답변 영역을 만들어 같은 화면에서 수정 흐름으로
    if (replyCreateSubmit) {
      replyCreateSubmit.addEventListener('click', async () => {
        const createInquiryId = replyCreateSubmit.getAttribute('data-inquiry-reply-create-id');
        const content = getInlineEditorContent(replyCreateTextarea);
        if (!content || content === '<p>&nbsp;</p>') {
          if (replyCreateError) {
            replyCreateError.classList.remove('is-hidden');
          }
          return;
        }

        try {
            // 답글 작성도 첨부를 같이 받기 위해 multipart/form-data로 전송
            const response = await request(`/api/admin/inquiries/${createInquiryId}/reply`, {
              method: 'POST',
              body: buildReplyFormData(replyCreateForm, content)
            });
            const result = await response.json();

            if (result.boardResultMessage) {
                openBoardResultModal(result.boardResultMessage, async () => {
                  await refreshDetailPage();
                });
            } else {
                await refreshDetailPage();
            }
        } catch (error) {
            handleError();
        }
      });
    }

    if (useCustomUpload) {
      initReplyUploadRoots();
    }

    const initialEditButton = detailPage.querySelector('[data-inquiry-reply-edit-id]');
    bindReplyEditButton(initialEditButton);
    bindReplyEditFormHandlers();
  }
})();
