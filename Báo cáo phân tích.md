# BÁO CÁO PHÂN TÍCH HỆ THỐNG ĐẶT VÉ CONCERT VỚI ORCHESTRATION SAGA


## 1. So sánh Choreography Saga và Orchestration Saga với State Machine

| Tiêu chí | Choreography Saga | Orchestration Saga (State Machine) |
|---|---|---|
| Cơ chế điều phối | Phân tán (các service tự lắng nghe event từ Kafka) | Tập trung (State Machine Orchestrator điều phối) |
| Mức độ phụ thuộc | Các service phải biết event của nhau (Event Spaghetti) | Các service độc lập, chỉ nhận lệnh từ Orchestrator |
| Quản lý trạng thái | Không có nơi lưu vết trạng thái tập trung | State Machine quản lý tập trung chính xác trạng thái |
| Độ phức tạp luồng | Khó theo dõi và debug khi quy trình mở rộng | Rõ ràng, trực quan qua Ma trận chuyển trạng thái |
| Xử lý bồi hoàn (Compensation) | Phức tạp, dễ bỏ sót rollback khi lỗi bước trung gian | Đơn giản, Orchestrator chủ động kích hoạt rollback |

### Tại sao cần State Machine khi quy trình phức tạp hơn:
- Khi hệ thống mở rộng thêm nhiều bước (kiểm tra ưu đãi, thanh toán, giữ chỗ, phát hành vé, gửi thông báo), Choreography Saga khiến luồng sự kiện trở nên chồng chéo ("Event Spaghetti"), mất kiểm soát và cực kỳ khó debug khi xảy ra sự cố.
- State Machine đóng vai trò "Nhạc trưởng" tập trung (Centralized Conductor), kiểm soát chính xác trạng thái hiện tại (Current State), quyết định hành động tiếp theo (Command) và xử lý bồi hoàn an toàn khi có lỗi, giúp hệ thống minh bạch và dễ bảo trì.


## 2. Mô tả Chi tiết Trạng thái, Sự kiện và Luồng chuyển đổi Trạng thái

### Danh sách Trạng thái (States):
- INITIATED: Khởi tạo yêu cầu đặt vé.
- PAYMENT_PENDING: Đang xử lý thanh toán.
- PAYMENT_COMPLETED: Thanh toán thành công.
- SEAT_RESERVING: Đang giữ chỗ và gán ghế.
- BOOKING_CONFIRMED: Đặt vé hoàn tất thành công.
- CANCELLED: Giao dịch bị hủy hoặc đã bồi hoàn thành công.

### Danh sách Sự kiện (Events):
- PROCESS_PAYMENT: Bắt đầu xử lý thanh toán.
- PAYMENT_SUCCESS: Thanh toán thành công.
- PAYMENT_FAILED: Thanh toán thất bại.
- RESERVE_SEATS: Bắt đầu giữ chỗ.
- RESERVATION_SUCCESS: Giữ chỗ thành công.
- RESERVATION_FAILED: Giữ chỗ thất bại.

### Luồng chuyển đổi trạng thái (State Transition Flow):
1. INITIATED -> Event: PROCESS_PAYMENT -> PAYMENT_PENDING (Kích hoạt PaymentService)
2. PAYMENT_PENDING -> Event: PAYMENT_SUCCESS -> PAYMENT_COMPLETED (Thanh toán thành công)
   *(Nếu thất bại: PAYMENT_PENDING -> Event: PAYMENT_FAILED -> CANCELLED)*
3. PAYMENT_COMPLETED -> Event: RESERVE_SEATS -> SEAT_RESERVING (Kích hoạt ConcertService)
4. SEAT_RESERVING -> Event: RESERVATION_SUCCESS -> BOOKING_CONFIRMED (Hoàn tất đơn vé)
   *(Nếu thất bại: SEAT_RESERVING -> Event: RESERVATION_FAILED -> CANCELLED và hoàn tiền)*


## 3. Retry Policy và Cơ chế Bù trừ (Compensation)

### Retry Policy:
- Mục đích: Xử lý các lỗi gián đoạn tạm thời (Network Timeout / Transient Error) ở bước thanh toán mà không hủy ngay giao dịch.
- Cấu hình: Thử lại tối đa 3 lần (maxAttempts = 3), khoảng thời gian chờ giữa các lần thử là 2 giây (delayMs = 2000).
- Hoạt động: Khi gọi bước thanh toán, Orchestrator thử thực hiện lần 1. Nếu gặp sự cố, hệ thống tạm dừng 2 giây và thử tiếp lần 2, lần 3. Nếu sau 3 lần vẫn thất bại, sự kiện PAYMENT_FAILED mới được phát ra để chuyển trạng thái sang CANCELLED.

### Cơ chế Bù trừ (Compensation):
- Nguyên tắc: Dựa trên trạng thái hiện tại (currentState) để thực hiện hành động đảo ngược giao dịch (Rollback), đảm bảo tính toàn vẹn dữ liệu (Eventual Consistency).
- Trường hợp thất bại tại thanh toán: Trạng thái chưa vượt qua PAYMENT_PENDING, tiền chưa bị trừ nên chỉ chuyển trạng thái sang CANCELLED mà không cần refund.
- Trường hợp thất bại tại giữ chỗ: Tiền đã được thanh toán thành công (PAYMENT_COMPLETED) nhưng giữ chỗ thất bại tại SEAT_RESERVING, Orchestrator tự động gọi hàm refund() của PaymentService để hoàn tiền lại cho khách hàng và đưa trạng thái về CANCELLED.


## 4. Hướng dẫn Cài đặt và Chạy dự án

### Cấu trúc các dự án:
- ConcertBookingService (Port 8081): Chứa State Machine Orchestrator Engine.
- seatAssignment-service (Port 8082): Chứa Concert Seat Reservation Activity Implementation.
- notification-service (Port 8083): Chứa Payment Activity Implementation.

### Các bước khởi chạy:
1. Khởi chạy ConcertBookingService: Khởi chạy Main Class com.example.orchestrator.ConcertBookingApplication (Port 8081).
2. Khởi chạy seatAssignment-service: Khởi chạy Main Class com.example.seat.SeatAssignmentApplication (Port 8082).
3. Khởi chạy notification-service: Khởi chạy Main Class com.example.payment.PaymentApplication (Port 8083).


## 5. Kết quả Chạy thử với Dữ liệu Đầu vào

### Dữ liệu đầu vào (Input):
HTTP POST http://localhost:8081/api/orchestrator/bookings
Body JSON:
```json
{
  "bookingId": "CONCERT-2026-088",
  "concertCode": "LIVE-HCM-2026-ULTRA",
  "customerId": "VIP-2024",
  "customerEmail": "rika@email.com",
  "ticketQuantity": 3,
  "amount": 5500000
}
```

### Log đầu ra trên Console (Output):
```text
[Orchestrator] State: INITIATED -> Event: PROCESS_PAYMENT -> New State: PAYMENT_PENDING
[Orchestrator] RetryPolicy: Activity 'processPayment' - Attempt 1/3
[Orchestrator] State: PAYMENT_PENDING -> Event: PAYMENT_SUCCESS -> New State: PAYMENT_COMPLETED
[Orchestrator] State: PAYMENT_COMPLETED -> Event: RESERVE_SEATS -> New State: SEAT_RESERVING
[Orchestrator] State: SEAT_RESERVING -> Event: RESERVATION_SUCCESS -> New State: BOOKING_CONFIRMED
[Orchestrator] Final State: BOOKING_CONFIRMED for booking CONCERT-2026-088
```