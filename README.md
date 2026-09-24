# 🎵 MusicKhangSMP — Plugin Nhạc Nền Tự Động Server KhangSMP

Plugin phát nhạc nền MP3 chất lượng cao xoay vòng liên tục toàn máy chủ Paper 1.21.4 (hỗ trợ cả Java & Bedrock).

## 🚀 Tính năng cốt lõi
- **Tự động chuyển bài:** Tự động phát bài tiếp theo ngay khi bài trước kết thúc (dựa trên thời lượng chính xác của từng bài).
- **Phát trực tiếp toàn server:** Mọi người chơi online đều nghe cùng một bài hát theo thời gian thực.
- **Chặn Member bỏ qua bài:** Người chơi thông thường không thể tự ý `/next` hay `/skip` làm gián đoạn mọi người.
- **Tùy chọn cá nhân hóa:** Người chơi có thể tự tắt nhạc (`/nhac off`) hoặc bật lại (`/nhac on`) bất cứ lúc nào mà không làm ảnh hưởng đến người khác. Hệ thống tự lưu trạng thái cá nhân ngay cả khi thoát game.

## 📋 Hệ Thống Lệnh
### 👤 Dành cho Member:
- `/nhac on` : Bật nghe nhạc nền cho bản thân.
- `/nhac off` : Tắt nghe nhạc nền cho bản thân (im lặng).
- `/nhac check` : Xem bài hát đang phát, tiến độ thời gian và thanh tiến trình.
- `/nhac list` : Xem danh sách 15 bài hát trong hệ thống kèm thời lượng.

### 👑 Dành cho Admin / OP (`musickhangsmp.admin`):
- `/nhacad on` : Bật hệ thống phát nhạc toàn server.
- `/nhacad off` : Tắt hệ thống phát nhạc toàn server (dừng âm thanh tất cả người chơi).
- `/nhacad next` : Chuyển ngay sang bài kế tiếp.
- `/nhacad check` : Kiểm tra trạng thái hệ thống và bài đang phát.
- `/nhacad list` : Danh sách toàn bộ bài hát.
- `/nhacad reload` : Nạp lại cấu hình `config.yml`.
