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

            // 2. 드래그 앤 드롭
            this.uploadZone.addEventListener('dragover', (e) => {
                e.preventDefault();
                this.uploadZone.classList.add('bg-zinc-50', 'border-primary'); // 드래그 시 효과
            });
            this.uploadZone.addEventListener('dragleave', () => {
                this.uploadZone.classList.remove('bg-zinc-50', 'border-primary');
            });
            this.uploadZone.addEventListener('drop', (e) => {
                e.preventDefault();
                this.uploadZone.classList.remove('bg-zinc-50', 'border-primary');
                this.addFiles(e.dataTransfer.files);
            });

            // 3. 파일 선택 이벤트
            this.fileInput.addEventListener('change', (e) => {
                this.addFiles(e.target.files);
                this.fileInput.value = '';
            });

            // 4. 기존 파일 삭제 이벤트 (답변 수정 시)
            this.fileListContainer.querySelectorAll('.inquiry-upload__item.existing').forEach(item => {
                const removeBtn = item.querySelector('.inquiry-upload__remove, [data-remove-file]');
                const deleteInput = item.querySelector('.delete-input');

                if (removeBtn && deleteInput) {
                    removeBtn.addEventListener('click', (e) => {
                        e.stopPropagation();
                        const isDeleting = !item.classList.contains('to-delete');
                        item.classList.toggle('to-delete', isDeleting);
                        item.style.opacity = isDeleting ? '0.3' : '1';
                        deleteInput.name = isDeleting ? 'deleteUuids' : '';
                        deleteInput.value = isDeleting ? (deleteInput.dataset.uuid || '') : '';
                        this.updateCount();
                    });
                }
            });

            this.updateCount();
        }

        addFiles(files) {
            if (!files?.length) return;

            const currentTotal = this.selectedFiles.length + this._getExistingCount();
            const remaining = this.maxCount - currentTotal;

            if (remaining <= 0) {
                window.alert(`최대 ${this.maxCount}개까지 첨부할 수 있습니다.`);
                return;
            }

            Array.from(files).slice(0, remaining).forEach(file => {
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