
           document.addEventListener('DOMContentLoaded', function () {
            const swiper = new Swiper('.mySwiper', {
                // 한 번에 1개만 보여줌
                slidesPerView: 1,
                spaceBetween: 0,
                loop: true, // 마지막에서 다시 처음으로 회전 (선택)

                // 내부 페이지네이션 설정
                pagination: {
                    el: '.swiper-pagination',
                    clickable: true, // 점 클릭 시 이동 가능
                },

                // 화살표 설정
                navigation: {
                    nextEl: '.swiper-button-next',
                    prevEl: '.swiper-button-prev',
                },
            });
            });


            function cancelOrder(ordId) {
                if (!ordId) return;
                if (confirm("이 주문을 정말로 취소하시겠습니까?\n(모든 배송지의 발송이 취소됩니다.)")) {
                    const form = document.getElementById('cancelForm-' + ordId);
                    console.log(form);
                    if (form) form.submit();
                }
            }

            async function loadShipmentDetail(shId) {
                const modal = document.getElementById('orderTrackingModal');
                const modalBody = modal.querySelector('.modal-body');

                modal.style.display = 'flex';
                modalBody.innerHTML = `
                    <div style="text-align: center; padding: 50px;">
                        <p style="color: #666;">배송 정보를 불러오는 중입니다...</p>
                    </div>
                `;

                try {
                    const trackResponse = await fetch(`/api/shipments/${shId}/track`);

                    if (!trackResponse.ok) throw new Error("배송 현황을 가져올 수 없습니다.");
                    const trackData = await trackResponse.json();

                    modalBody.innerHTML = buildTrackingHtml(trackData);

                } catch (error) {
                    console.error(error);
                    modalBody.innerHTML = `<div style="text-align: center; padding: 40px; color: red;">오류: ${error.message}</div>`;
                }
            }

            function buildTrackingHtml(data) {
                let html = `
                   <div class="tracking-info-summary">
                           <div class="info-item">
                               <span class="label">택배사</span>
                               <div class="value">${data.carrierName}</div>
                           </div>

                           <div class="info-item">
                               <span class="label">수령인</span>
                               <div class="value">${data.shRcvNm}</div>
                           </div>

                           <div class="info-item info-item--address">
                               <span class="label">배송지</span>
                               <div class="value" style="line-height: 1.4; word-break: keep-all;">${data.shAdr}</div>
                           </div>

                           <div class="info-item">
                               <span class="label">송장번호</span>
                               <div class="value">${data.trackingNumber}</div>
                           </div>

                           <div class="info-item info-item--status">
                               <span class="label">현재 상태</span>
                               <div class="value">
                                   <span style="font-size: 12px; color: #0056b3; font-weight: normal; margin-right: 4px;">${data.statusText}</span>
                               </div>
                           </div>
                       </div>

                       <div class="tracking-timeline">
                           </div>
                `;

                if (!data.details || data.details.length === 0) {
                    html += `
                        <div style="text-align: center; padding: 20px; color: #888; background: #fafafa; border-radius: 8px;">
                            <p style="margin: 0;">아직 택배사에 배송 정보가 등록되지 않았습니다.</p>
                        </div>
                    `;
                    return html;
                }

                html += '<ul class="tracking-timeline-list" style="padding-left: 20px;">';
                data.details.forEach((item, index) => {
                    const activeClass = (index === 0) ? 'active' : '';
                    html += `
                        <li class="timeline-step ${activeClass}" style="margin-bottom: 15px;">
                            <div style="font-size: 12px; color: #999;">${item.time.replace('T', ' ').substring(0, 16)}</div>
                            <div style="margin-top: 4px;">
                                <strong style="font-size: 14px; color: #333;">${item.status}</strong>
                                <p style="font-size: 13px; color: #666; margin: 2px 0 0 0;">${item.description}</p>
                            </div>
                        </li>`;
                });
                html += '</ul><hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">';

                return html;
            }

            function closeModal() {
                document.getElementById('orderTrackingModal').style.display = 'none';
            }

            window.addEventListener('click', function(event) {
                const modal = document.getElementById('orderTrackingModal');
                if (event.target === modal) {
                    closeModal();
                }
            });