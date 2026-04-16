/**
 * fileUpload.js
 * 문의/답변 공통 파일 업로드 관리 모듈
 */
window.InquiryUploader = (() => {
    const formatFileSize = (size) => {
        if (!Number.isFinite(size) || size <= 0) return '0 B';
        if (size < 1024) return `${size} B`;
        if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
        return `${(size / (1024 * 1024)).toFixed(1)} MB`;
    };

    class Uploader {
        constructor(root) {
            this.root = root;
            this.uploadZone = root.querySelector('.inquiry-upload__zone, [data-upload-zone]');
            this.fileInput = root.querySelector('input[name="newFiles"], [data-upload-input]');
            this.fileListContainer = root.querySelector('.inquiry-upload__list, [data-upload-list]');

            // HTML 구조에 맞춰 파일 카운트 엘리먼트 찾기
            this.countInfo = root.querySelector('#fileCountInfo, [data-upload-count]');

            this.selectedFiles = [];
            this.maxCount = this._parseMaxCount();

            if (this.root.dataset.bound === 'true') return;
            this.root.dataset.bound = 'true';

            this._init();
        }

        _parseMaxCount() {
            // 태그 내부의 (0 / 5) 같은 숫자 텍스트를 읽어와 최대 개수 파악
            if (this.countInfo) {
                const match = String(this.countInfo.textContent || '').match(/\/\s*(\d+)\)/);
                if (match) return Number(match[1]);
            }
            return this.fileInput?.multiple ? 5 : 1;
        }

        _init() {
            if (!this.uploadZone || !this.fileInput || !this.fileListContainer) return;

            // 1. 클릭 업로드 바인딩
            this.uploadZone.addEventListener('click', () => this.fileInput.click());
            this.fileInput.addEventListener('click', (e) => e.stopPropagation());

            // 2. 드래그 앤 드롭 (수정됨)
            this.uploadZone.addEventListener('dragover', (e) => {
                e.preventDefault();
                this.uploadZone.classList.add('bg-zinc-50', 'border-primary');
            });
            this.uploadZone.addEventListener('dragleave', () => {
                this.uploadZone.classList.remove('bg-zinc-50', 'border-primary');
            });
            this.uploadZone.addEventListener('drop', (e) => {
                e.preventDefault();
                this.uploadZone.classList.remove('bg-zinc-50', 'border-primary');
                // ✅ 화살표 함수를 사용하여 this.addFiles가 Uploader 객체의 메서드임을 보장
                this.addFiles(e.dataTransfer.files);
            });

            // 3. 파일 선택 이벤트 (수정됨)
            this.fileInput.addEventListener('change', (e) => {
                // ✅ 화살표 함수를 사용하여 this 바인딩 해결
                this.addFiles(e.target.files);
                this.fileInput.value = ''; // 같은 파일 재선택 가능하도록 초기화
            });

            // 4. 기존 파일 삭제/복구 이벤트 (이전 수정본 유지)
            this.fileListContainer.querySelectorAll('.inquiry-upload__item.existing').forEach(item => {
                const removeBtn = item.querySelector('.inquiry-upload__remove, [data-remove-file]');
                const deleteInput = item.querySelector('.delete-input');

                if (removeBtn && deleteInput) {
                    removeBtn.addEventListener('click', (e) => {
                        e.preventDefault();
                        e.stopPropagation();

                        const isCurrentlyDeleted = item.classList.contains('to-delete');

                        if (isCurrentlyDeleted) {
                            const currentTotal = this.selectedFiles.length + this._getExistingCount();
                            if (currentTotal >= this.maxCount) {
                                if (typeof showGuideModal === 'function') {
                                    showGuideModal(`파일은 최대 ${this.maxCount}개까지 첨부할 수 있습니다.\n복구하려면 먼저 다른 파일을 삭제해주세요.`, null, 'LIMIT EXCEEDED', 'warning');
                                } else {
                                    alert(`최대 ${this.maxCount}개까지 첨부할 수 있습니다.`);
                                }
                                return;
                            }
                        }

                        const willBeDeleting = !isCurrentlyDeleted;
                        item.classList.toggle('to-delete', willBeDeleting);
                        item.style.opacity = willBeDeleting ? '0.3' : '1';
                        deleteInput.name = willBeDeleting ? 'deleteUuids' : '';
                        deleteInput.value = willBeDeleting ? (deleteInput.dataset.uuid || '') : '';

                        this.updateCount();
                    });
                }
            });

            this.updateCount();
        }

        addFiles(files) {
            if (!files?.length) return;

            const incomingFiles = Array.from(files);
            const currentTotal = this.getActiveFileCount();

            if (currentTotal + incomingFiles.length > this.maxCount) {
                if (typeof showGuideModal === 'function') {
                    showGuideModal(
                        `최대 ${this.maxCount}개까지 첨부할 수 있습니다.\n(현재 ${currentTotal}개 / 추가 시도 ${incomingFiles.length}개)`,
                        null,
                        'LIMIT EXCEEDED',
                        'warning'
                    );
                } else {
                    alert(`최대 ${this.maxCount}개까지 첨부할 수 있습니다.`);
                }
                return;
            }

            incomingFiles.forEach(file => {
                const isDuplicate = this.selectedFiles.some(s =>
                    s.name === file.name && s.size === file.size && s.lastModified === file.lastModified
                );

                if (!isDuplicate) {
                    this.selectedFiles.push(file);
                    this._renderNewFile(file);
                }
            });

            this.syncToInput();
            this.updateCount();
        }

        _renderNewFile(file) {
            const item = document.createElement('div');
            item.className = 'inquiry-upload__item new flex items-center justify-between gap-3 p-3 mb-2 border-l-4 border-primary bg-white shadow-sm transition-all';

            item.innerHTML = `
        <div class="flex items-center gap-2 overflow-hidden flex-1">
          <span class="material-symbols-outlined text-secondary !text-lg flex-shrink-0">description</span>
          <span class="text-sm font-medium text-on-background truncate">
            ${file.name}
            <small class="text-zinc-400 ml-1">(${formatFileSize(file.size)})</small>
          </span>
          <mark class="badge-new bg-primary-container text-on-primary-container text-[10px] font-black px-1.5 py-0.5 uppercase tracking-wider flex-shrink-0 rounded">NEW</mark>
        </div>
        <button type="button" class="inquiry-upload__remove text-secondary hover:text-error transition-colors flex-shrink-0" aria-label="첨부 파일 삭제">
          <span class="material-symbols-outlined !text-lg">close</span>
        </button>
      `;

            item.querySelector('button')?.addEventListener('click', (e) => {
                e.stopPropagation();
                this.selectedFiles = this.selectedFiles.filter(f => f !== file);
                item.remove();
                this.syncToInput();
                this.updateCount();
            });

            this.fileListContainer.appendChild(item);
        }

        _getExistingCount() {
            return this.fileListContainer.querySelectorAll('.inquiry-upload__item.existing:not(.to-delete)').length;
        }

        updateCount() {
            if (this.countInfo) {
                const total = this.selectedFiles.length + this._getExistingCount();
                this.countInfo.textContent = `(${total} / ${this.maxCount})`;
            }
        }

        syncToInput() {
            if (!this.fileInput) return;
            const dataTransfer = new DataTransfer();
            this.selectedFiles.forEach(file => dataTransfer.items.add(file));
            this.fileInput.files = dataTransfer.files;
        }

        appendToFormData(formData) {
            this.selectedFiles.forEach(file => formData.append('newFiles', file));
            this.fileListContainer.querySelectorAll('.delete-input').forEach(input => {
                if (input.name === 'deleteUuids' && input.value) {
                    formData.append('deleteUuids', input.value);
                }
            });
        }
        getActiveFileCount() {
            return this.selectedFiles.length + this._getExistingCount();
        }

        // [추가] 파일 개수 유효성 검사 실행 (모달 노출까지 포함)
        validateFileCount() {
            const currentCount = this.getActiveFileCount();
            if (currentCount > this.maxCount) {
                if (typeof showGuideModal === 'function') {
                    showGuideModal(`파일은 최대 ${this.maxCount}개까지만 업로드 가능합니다.`, null, 'LIMIT EXCEEDED', 'warning');
                } else {
                    alert(`최대 ${this.maxCount}개까지만 업로드 가능합니다.`);
                }
                return false;
            }
            return true;
        }
    }

    return {
        init: (selector = '[data-inquiry-upload]') => {
            const roots = document.querySelectorAll(selector);
            return Array.from(roots).map(root => {
                if (!root._uploader) {
                    root._uploader = new Uploader(root);
                }
                return root._uploader;
            });
        },
        get: (element) => element?._uploader
    };
})();