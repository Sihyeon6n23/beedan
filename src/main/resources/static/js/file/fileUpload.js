/**
 * 범용 파일 업로드 모듈 (FileUploadModule)
 * - 드래그 앤 드롭, 클릭 업로드, 기존 파일 삭제, 용량 계산 등을 처리합니다.
 */
class FileUploadModule {
    constructor(rootElement) {
        if (!rootElement || rootElement.dataset.bound === 'true') {
            return;
        }

        this.root = rootElement;
        // 호환성을 위해 data 속성과 기존 class 선택자를 모두 지원합니다.
        this.uploadZone = this.root.querySelector('[data-upload-zone]') || this.root.querySelector('.inquiry-upload__zone');
        this.fileInput = this.root.querySelector('[data-upload-input]') || this.root.querySelector('input[type="file"]');
        this.fileListContainer = this.root.querySelector('[data-upload-list]') || this.root.querySelector('.inquiry-upload__list');
        this.countInfo = this.root.querySelector('[data-upload-count]') || this.root.querySelector('#fileCountInfo');

        if (!this.uploadZone || !this.fileInput || !this.fileListContainer) {
            return;
        }

        // HTML의 data-max-count 속성을 최우선으로 읽고, 없으면 input의 multiple 여부에 따라 기본값 설정
        const maxMatch = this.countInfo ? String(this.countInfo.textContent || '').match(/\/\s*(\d+)\)/) : null;
        this.maxCount = Number(this.root.dataset.maxCount) || (maxMatch ? Number(maxMatch[1]) : (this.fileInput.multiple ? 5 : 1));

        this.selectedFiles = [];

        // 외부 스크립트(inquiry.js 등)에서 이 인스턴스에 접근할 수 있도록 DOM에 바인딩
        this.root.fileUploadModule = this;
        this.root.dataset.bound = 'true';

        this.init();
    }

    init() {
        this.bindEvents();
        this.bindExistingFiles();
        this.updateCount();
    }

    formatFileSize(size) {
        if (!Number.isFinite(size) || size <= 0) return '0 B';
        if (size < 1024) return `${size} B`;
        if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
        return `${(size / (1024 * 1024)).toFixed(1)} MB`;
    }

    syncFileInput() {
        const dataTransfer = new DataTransfer();
        this.selectedFiles.forEach((file) => dataTransfer.items.add(file));
        this.fileInput.files = dataTransfer.files;
    }

    getExistingCount() {
        return this.fileListContainer.querySelectorAll('.existing:not(.to-delete)').length;
    }

    updateCount() {
        if (this.countInfo) {
            this.countInfo.textContent = `(${this.selectedFiles.length + this.getExistingCount()} / ${this.maxCount})`;
        }
    }

    // 외부(AJAX 전송 시)에서 새롭게 추가된 파일 목록을 가져갈 때 사용하는 API
    getNewFiles() {
        return this.selectedFiles.slice();
    }

    removeNewFile(targetName, targetSize, targetLastModified, element) {
        this.selectedFiles = this.selectedFiles.filter((file) => !(
            file.name === targetName &&
            file.size === targetSize &&
            file.lastModified === targetLastModified
        ));
        this.syncFileInput();
        if (element) element.remove();
        this.updateCount();
    }

    renderNewFile(file) {
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
          <small class="text-zinc-400 ml-1">(${this.formatFileSize(fileSize)})</small>
        </span>
        <mark class="badge-new bg-primary-container text-on-primary-container text-[10px] font-black px-1.5 py-0.5 uppercase tracking-wider flex-shrink-0">New</mark>
      </div>
      <button type="button" class="inquiry-upload__remove text-secondary hover:text-error transition-colors flex-shrink-0 ml-3" aria-label="첨부 파일 삭제">
        <span class="material-symbols-outlined !text-lg">close</span>
      </button>
    `;

        item.querySelector('button')?.addEventListener('click', () => {
            this.removeNewFile(fileName, fileSize, lastModified, item);
        });

        this.fileListContainer.appendChild(item);
    }

    processFiles(files) {
        if (!files?.length) return;

        const remaining = this.maxCount - (this.selectedFiles.length + this.getExistingCount());
        if (remaining <= 0) {
            window.alert(`최대 ${this.maxCount}개까지 첨부할 수 있습니다.`);
            return;
        }

        Array.from(files).slice(0, remaining).forEach((file) => {
            const duplicated = this.selectedFiles.some((selected) =>
                selected.name === file.name &&
                selected.size === file.size &&
                selected.lastModified === file.lastModified
            );

            if (!duplicated) {
                this.selectedFiles.push(file);
                this.renderNewFile(file);
            }
        });

        this.syncFileInput();
        this.updateCount();
    }

    bindEvents() {
        this.uploadZone.addEventListener('click', () => this.fileInput.click());
        this.fileInput.addEventListener('click', (event) => event.stopPropagation());

        this.uploadZone.addEventListener('dragover', (event) => {
            event.preventDefault();
            this.uploadZone.classList.add('is-dragover');
        });

        this.uploadZone.addEventListener('dragleave', () => this.uploadZone.classList.remove('is-dragover'));

        this.uploadZone.addEventListener('drop', (event) => {
            event.preventDefault();
            this.uploadZone.classList.remove('is-dragover');
            this.processFiles(event.dataTransfer.files);
        });

        this.fileInput.addEventListener('change', (event) => this.processFiles(event.target.files));
    }

    bindExistingFiles() {
        this.fileListContainer.querySelectorAll('.existing').forEach((item) => {
            const deleteInput = item.querySelector('.delete-input');
            const removeButton = item.querySelector('.inquiry-upload__remove');
            if (!deleteInput || !removeButton) return;

            removeButton.onclick = null; // 인라인 속성 제거
            removeButton.addEventListener('click', () => {
                const isDeleting = !item.classList.contains('to-delete');
                item.classList.toggle('to-delete', isDeleting);
                item.style.opacity = isDeleting ? '0.5' : '1';
                deleteInput.name = isDeleting ? 'deleteUuids' : '';
                deleteInput.value = isDeleting ? (deleteInput.dataset.uuid || '') : '';
                this.updateCount();
            });
        });
    }
}

// 브라우저 전역 객체에 노출 (모듈 번들러를 쓰지 않는 환경 지원)
window.FileUploadModule = FileUploadModule;

document.addEventListener('DOMContentLoaded', () => {
    // 페이지 안에 data-inquiry-upload 속성을 가진 모든 요소를 찾아서 업로드 모듈을 붙여줌
    document.querySelectorAll('[data-inquiry-upload]').forEach(element => {
        new FileUploadModule(element);
    });
});