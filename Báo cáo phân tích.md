# BÁO CÁO NGHIỆP VỤ ĐẶT VÉ CONCERT (SAGA PATTERN & KAFKA)

---

## 1. Luồng sự kiện Choreography Saga & Vai trò từng Service

Mô hình **Choreography Saga** xử lý giao dịch phân tán không cần điều phối trung tâm. Các service phản ứng độc lập thông qua các sự kiện trên Kafka Topics:

```
[Client] --- (POST) --- [ConcertBookingService]
                           │
                      Kafka Topic: concert-events
                     [SeatAssignmentService] (@CircuitBreaker)
                           │
                      Kafka Topic: seat-events
                     [NotificationService] (@CircuitBreaker)
```

### Vai trò các Services:
1. **`ConcertBookingService` (Port 8081 - Producer khởi tạo)**:
   - Nhận yêu cầu đặt vé từ Client qua REST API (`POST /api/bookings`).
   - Đóng gói sự kiện `ConcertBookingEvent` và publish lên topic **`concert-events`**.
2. **`SeatAssignmentService` (Port 8082 - Consumer & Producer)**:
   - Lắng nghe topic **`concert-events`**, giải mã sự kiện và giữ chỗ ghế (tích hợp Resilience4j Circuit Breaker).
   - Tạo sự kiện `SeatReservedEvent` và publish lên topic **`seat-events`**.
3. **`NotificationService` (Port 8083 - Consumer)**:
   - Lắng nghe topic **`seat-events`** và gửi email/thông báo xác nhận đến khách hàng (tích hợp Resilience4j Circuit Breaker).

---

## 2. Cách Correlation ID được truyền xuyên suốt

**Correlation ID** (ví dụ: `CONCERT-2024-999`) đóng vai trò là mã định danh vết giao dịch (Tracing ID) xuyên suốt hệ thống phân tán:

1. **Khởi tạo**: `ConcertBookingService` tiếp nhận hoặc tạo `correlationId` và gán vào `ConcertBookingEvent`.
2. **Kế thừa & Truyền tiếp**: `SeatAssignmentService` khi tiêu thụ sự kiện từ `concert-events` sẽ trích xuất `correlationId` và **bắt buộc gán nguyên vẹn `correlationId` này** vào `SeatReservedEvent` đẩy lên `seat-events`.
3. **Truy vết kết thúc**: `NotificationService` sử dụng `correlationId` thu được từ `seat-events` để in log xác nhận gửi email, giúp quản trị viên truy vết toàn bộ vòng đời của yêu cầu từ đầu đến cuối.

---

## 3. Hướng dẫn Cài đặt và Chạy dự án

### Bước 1: Khởi động Kafka Cluster bằng Docker
Tại thư mục gốc dự án [PTIT_CNTT1_IT214_Session15_Ex3](file:///d:/Microsevices%20In%20Action/PTIT_CNTT1_IT214_Session15_Ex3), chạy lệnh:
```bash
docker compose up -d
```
> *Hệ thống tự động kích hoạt Zookeeper (`2181`), Kafka Broker (`9092`), Kafka UI (`8080`) và tự động tạo 02 Topics `concert-events` & `seat-events` thông qua Spring Kafka `NewTopic` Config.*

### Bước 2: Chạy 03 Microservices
Khởi chạy Main Class tương ứng của từng dịch vụ trong IDE hoặc terminal:
- **ConcertBookingService**: [com.example.concert.ConcertBookingApplication](file:///d:/Microsevices%20In%20Action/PTIT_CNTT1_IT214_Session15_Ex3/ConcertBookingService/src/main/java/com/example/concert/ConcertBookingApplication.java)
- **seatAssignment-service**: [com.example.seat.SeatAssignmentApplication](file:///d:/Microsevices%20In%20Action/PTIT_CNTT1_IT214_Session15_Ex3/seatAssignment-service/src/main/java/com/example/seat/SeatAssignmentApplication.java)
- **notification-service**: [com.example.notification.NotificationApplication](file:///d:/Microsevices%20In%20Action/PTIT_CNTT1_IT214_Session15_Ex3/notification-service/src/main/java/com/example/notification/NotificationApplication.java)

---

## 4. Kết quả Chạy thử nghiệm

### Dữ liệu đầu vào (Input):
- **Endpoint**: `POST http://localhost:8081/api/bookings`
- **Body JSON**:
```json
{
 "correlationId": "CONCERT-2024-999",
 "concertCode": "LIVE-HCM-2024",
 "customerEmail": "nguyenvanA@email.com",
 "ticketQuantity": 3
}
```

### Log Nghiệp vụ Đầu ra (Console Logs):

1. **`ConcertBookingService`**:
   ```text
   [BookingService] Created booking request with correlationId: CONCERT-2024-999
   [BookingService] Published event to topic: concert-events
   ```

2. **`seatAssignment-service`**:
   ```text
   [SeatService] Received event with correlationId: CONCERT-2024-999
   [SeatService] Seat reserved successfully for correlationId: CONCERT-2024-999
   [SeatService] Publishing SeatReserved event with correlationId: CONCERT-2024-999 to topic: seat-events
   ```

3. **`notification-service`**:
   ```text
   [NotifyService] Received confirmation for correlationId: CONCERT-2024-999 - Sending email to nguyenvanA@email.com
   ```