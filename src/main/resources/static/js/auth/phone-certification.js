const PhoneNumberVerification = {
    async verify(impUid) {
        try {
            const response = await axios.post("/api/auth/phone-certification", {
                impUid: impUid
            });
            console.log("응답완료", response.data);
            return response.data;
        } catch (error) {
            console.error("검증 중 에러발생: " + error.response?.data || error.message);
            throw error;
        }
    }
}